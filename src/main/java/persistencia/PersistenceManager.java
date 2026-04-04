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

    public static void ensureSchemaCompatibility() {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();

            ensureJoyaSchema(em);
            ensureCambioPendienteSchema(em);

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException("No se pudo sincronizar el esquema minimo de base de datos.", e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private static void ensureJoyaSchema(EntityManager em) {
        if (!tableExists(em, "joya")) {
            em.createNativeQuery("""
                    CREATE TABLE joya (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        nombre VARCHAR(255),
                        precio VARCHAR(255),
                        peso DOUBLE NOT NULL DEFAULT 0,
                        categoria VARCHAR(255),
                        observacion VARCHAR(255),
                        tiene_piedra BOOLEAN NOT NULL DEFAULT FALSE,
                        info_piedra VARCHAR(255),
                        fue_editada BOOLEAN NOT NULL DEFAULT FALSE,
                        vendido BOOLEAN NOT NULL DEFAULT FALSE,
                        fecha_ingreso DATETIME,
                        fecha_vendida DATETIME,
                        precio_venta VARCHAR(255),
                        estado VARCHAR(50),
                        autorizado BOOLEAN NOT NULL DEFAULT TRUE,
                        socio VARCHAR(255),
                        display_id VARCHAR(255),
                        actualizado_en DATETIME,
                        actualizado_por BIGINT,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_joya_display_id (display_id)
                    )
                    """).executeUpdate();
            return;
        }

        addColumnIfMissing(em, "joya", "autorizado", "BOOLEAN NOT NULL DEFAULT TRUE");
        addColumnIfMissing(em, "joya", "actualizado_en", "DATETIME NULL");
        addColumnIfMissing(em, "joya", "actualizado_por", "BIGINT NULL");
        addColumnIfMissing(em, "joya", "display_id", "VARCHAR(255) NULL");
        addColumnIfMissing(em, "joya", "precio_venta", "VARCHAR(255) NULL");

        if (!indexExists(em, "joya", "uk_joya_display_id")) {
            em.createNativeQuery("CREATE UNIQUE INDEX uk_joya_display_id ON joya (display_id)").executeUpdate();
        }
    }

    private static void ensureCambioPendienteSchema(EntityManager em) {
        if (!tableExists(em, "cambio_pendiente")) {
            em.createNativeQuery("""
                    CREATE TABLE cambio_pendiente (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        entidad VARCHAR(50) NOT NULL,
                        entidad_id BIGINT,
                        operacion VARCHAR(10) NOT NULL,
                        before_json JSON,
                        after_json JSON,
                        estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
                        solicitado_por BIGINT NOT NULL,
                        solicitado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        revisado_por BIGINT,
                        revisado_en DATETIME,
                        comentario VARCHAR(500),
                        PRIMARY KEY (id)
                    )
                    """).executeUpdate();
        } else {
            addColumnIfMissing(em, "cambio_pendiente", "entidad", "VARCHAR(50) NULL");
            addColumnIfMissing(em, "cambio_pendiente", "entidad_id", "BIGINT NULL");
            addColumnIfMissing(em, "cambio_pendiente", "operacion", "VARCHAR(10) NULL");
            addColumnIfMissing(em, "cambio_pendiente", "before_json", "JSON NULL");
            addColumnIfMissing(em, "cambio_pendiente", "after_json", "JSON NULL");
            addColumnIfMissing(em, "cambio_pendiente", "estado", "VARCHAR(15) NULL DEFAULT 'PENDIENTE'");
            addColumnIfMissing(em, "cambio_pendiente", "solicitado_por", "BIGINT NULL");
            addColumnIfMissing(em, "cambio_pendiente", "solicitado_en", "DATETIME NULL DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing(em, "cambio_pendiente", "revisado_por", "BIGINT NULL");
            addColumnIfMissing(em, "cambio_pendiente", "revisado_en", "DATETIME NULL");
            addColumnIfMissing(em, "cambio_pendiente", "comentario", "VARCHAR(500) NULL");
        }

        if (!indexExists(em, "cambio_pendiente", "idx_cambio_pendiente_estado_fecha")) {
            em.createNativeQuery("CREATE INDEX idx_cambio_pendiente_estado_fecha ON cambio_pendiente (estado, solicitado_en)").executeUpdate();
        }
    }

    private static void addColumnIfMissing(EntityManager em, String tableName, String columnName, String definition) {
        if (!columnExists(em, tableName, columnName)) {
            em.createNativeQuery("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition).executeUpdate();
        }
    }

    private static boolean tableExists(EntityManager em, String tableName) {
        Number count = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name = ?1
                """)
                .setParameter(1, tableName)
                .getSingleResult();
        return count.longValue() > 0;
    }

    private static boolean columnExists(EntityManager em, String tableName, String columnName) {
        Number count = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?1
                  AND column_name = ?2
                """)
                .setParameter(1, tableName)
                .setParameter(2, columnName)
                .getSingleResult();
        return count.longValue() > 0;
    }

    private static boolean indexExists(EntityManager em, String tableName, String indexName) {
        Number count = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = ?1
                  AND index_name = ?2
                """)
                .setParameter(1, tableName)
                .setParameter(2, indexName)
                .getSingleResult();
        return count.longValue() > 0;
    }

    public static void shutdown() {
        EntityManagerFactory emf = getEntityManagerFactory();
        if (emf.isOpen()) {
            emf.close();
        }
    }
}

