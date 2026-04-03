package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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
        Map<String, String> overrides = new HashMap<>();
        putIfPresent(overrides, "jakarta.persistence.jdbc.url", "db.url");
        putIfPresent(overrides, "jakarta.persistence.jdbc.user", "db.user");
        putIfPresent(overrides, "jakarta.persistence.jdbc.password", "db.password");
        return overrides;
    }

    private static void putIfPresent(Map<String, String> overrides, String persistenceKey, String systemPropertyKey) {
        String value = System.getProperty(systemPropertyKey);
        if (value != null && !value.isBlank()) {
            overrides.put(persistenceKey, value);
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
            throw new IllegalStateException("No se pudo conectar a la base de datos. Revisa DB_URL, DB_USER y DB_PASSWORD.", e);
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

