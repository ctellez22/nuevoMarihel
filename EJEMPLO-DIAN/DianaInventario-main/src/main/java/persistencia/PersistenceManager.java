package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public final class PersistenceManager {
    private static final String PERSISTENCE_UNIT = "miUnidadDePersistencia";

    private PersistenceManager() {
    }

    private static class Holder {
        private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, buildOverrides());
    }

    private static Map<String, String> buildOverrides() {
        Map<String, String> resolved = resolveDbConfig();
        Map<String, String> overrides = new HashMap<>();
        putResolvedIfPresent(overrides, "jakarta.persistence.jdbc.url", resolved.get("db.url"));
        putResolvedIfPresent(overrides, "jakarta.persistence.jdbc.user", resolved.get("db.user"));
        putResolvedIfPresent(overrides, "jakarta.persistence.jdbc.password", resolved.get("db.password"));
        return overrides;
    }

    private static void putResolvedIfPresent(Map<String, String> overrides, String persistenceKey, String value) {
        if (value != null && !value.isBlank()) {
            overrides.put(persistenceKey, value);
        }
    }

    private static Map<String, String> resolveDbConfig() {
        Map<String, String> resolved = new HashMap<>();

        // 1) Prioridad: system properties explícitas.
        putIfPresent(resolved, "db.url", System.getProperty("db.url"));
        putIfPresent(resolved, "db.user", System.getProperty("db.user"));
        putIfPresent(resolved, "db.password", System.getProperty("db.password"));

        // 2) Fallback: variables de entorno directas.
        putIfAbsentAndPresent(resolved, "db.url", System.getenv("DB_URL"));
        putIfAbsentAndPresent(resolved, "db.user", System.getenv("DB_USER"));
        putIfAbsentAndPresent(resolved, "db.password", System.getenv("DB_PASSWORD"));

        // 3) Último fallback: DATABASE_URL tipo postgres://user:pass@host:port/db.
        applyDatabaseUrlFallback(resolved, System.getenv("DATABASE_URL"));

        return resolved;
    }

    private static void putIfPresent(Map<String, String> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value);
        }
    }

    private static void putIfAbsentAndPresent(Map<String, String> target, String key, String value) {
        if (!target.containsKey(key) && value != null && !value.isBlank()) {
            target.put(key, value);
        }
    }

    private static void applyDatabaseUrlFallback(Map<String, String> resolved, String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        URI uri;
        try {
            uri = new URI(databaseUrl);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("DATABASE_URL no tiene un formato valido.", e);
        }

        String scheme = uri.getScheme();
        if (!"postgresql".equalsIgnoreCase(scheme) && !"postgres".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException("DATABASE_URL debe usar el esquema postgresql://");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("DATABASE_URL no incluye host de base de datos.");
        }

        String rawPath = uri.getRawPath();
        if ((rawPath == null || rawPath.isBlank() || "/".equals(rawPath)) && !resolved.containsKey("db.url")) {
            throw new IllegalStateException("DATABASE_URL no incluye nombre de base de datos.");
        }

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            int separator = userInfo.indexOf(':');
            if (separator >= 0) {
                putIfAbsentAndPresent(resolved, "db.user", userInfo.substring(0, separator));
                putIfAbsentAndPresent(resolved, "db.password", userInfo.substring(separator + 1));
            } else {
                putIfAbsentAndPresent(resolved, "db.user", userInfo);
            }
        }

        if (!resolved.containsKey("db.url")) {
            String query = uri.getRawQuery();
            String jdbcUrl = "jdbc:postgresql://" + host + (uri.getPort() > 0 ? ":" + uri.getPort() : "") + rawPath
                    + (query != null && !query.isBlank() ? "?" + query.replace("channel_binding=", "channelBinding=") : "");
            putIfPresent(resolved, "db.url", jdbcUrl);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return Holder.EMF;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void validateConnection() {
        EntityManager entityManager = createEntityManager();
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
        } catch (RuntimeException e) {
            throw new IllegalStateException("No se pudo conectar a la base de datos. Revisa db.url/db.user/db.password, luego DB_URL/DB_USER/DB_PASSWORD o DATABASE_URL.", e);
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    public static void shutdown() {
        EntityManagerFactory emf = getEntityManagerFactory();
        if (emf.isOpen()) {
            emf.close();
        }
    }
}

