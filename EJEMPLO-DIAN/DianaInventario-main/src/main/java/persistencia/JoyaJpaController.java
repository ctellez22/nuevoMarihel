package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import logica.Joya;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class JoyaJpaController implements Serializable {

    private final EntityManagerFactory emf;

    public JoyaJpaController() {
        this.emf = PersistenceManager.getEntityManagerFactory();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Joya joya) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            if (joya.getDisplayId() == null || joya.getDisplayId().isBlank()) {
                try {
                    joya.setDisplayId(generarDisplayId(em, joya.getCategoria()));
                } catch (Exception e) {
                    System.err.println("No se pudo asignar displayId automáticamente: " + e.getMessage());
                }
            }

            em.persist(joya);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Genera un displayId basado en las primeras 2 letras de la categoría.
     * Usa un EntityManager propio para no contaminar la transacción principal.
     * Ejemplo: "Anillo" → "an1", "an2" | "Collar" → "co1", "co2"
     */
    private String generarDisplayId(EntityManager ignorado, String categoria) {
        String prefijo = calcularPrefijo(categoria);
        EntityManager em2 = getEntityManager();
        try {
            // 1. Crear tabla si no existe (transacción propia)
            try {
                em2.getTransaction().begin();
                em2.createNativeQuery(
                    "CREATE TABLE IF NOT EXISTS display_id_seq " +
                    "(prefix VARCHAR(20) PRIMARY KEY, last_val BIGINT NOT NULL DEFAULT 0)"
                ).executeUpdate();
                em2.getTransaction().commit();
            } catch (Exception e) {
                if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
                System.err.println("[displayId] No se pudo crear tabla seq: " + e.getMessage());
            }

            // 2. Leer y actualizar el contador (transacción propia con parámetros posicionales)
            long next;
            try {
                em2.getTransaction().begin();

                @SuppressWarnings("unchecked")
                List<Object> rows = em2.createNativeQuery(
                    "SELECT last_val FROM display_id_seq WHERE prefix = ?1 FOR UPDATE"
                ).setParameter(1, prefijo).getResultList();

                long lastVal;
                if (rows.isEmpty()) {
                    lastVal = 0L;
                    em2.createNativeQuery(
                        "INSERT INTO display_id_seq (prefix, last_val) VALUES (?1, 0)"
                    ).setParameter(1, prefijo).executeUpdate();
                } else {
                    Object o = rows.get(0);
                    lastVal = (o instanceof Number) ? ((Number) o).longValue() : Long.parseLong(o.toString());
                }

                next = lastVal + 1L;

                em2.createNativeQuery(
                    "UPDATE display_id_seq SET last_val = ?1 WHERE prefix = ?2"
                ).setParameter(1, next).setParameter(2, prefijo).executeUpdate();

                em2.getTransaction().commit();
            } catch (Exception e) {
                if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
                System.err.println("[displayId] Error con seq table, usando fallback: " + e.getMessage());

                // Fallback: calcular desde los displayIds existentes en la tabla joya
                next = calcularSiguienteDesdeJoyas(em2, prefijo);
            }

            return prefijo + next;

        } finally {
            em2.close();
        }
    }

    /**
     * Fallback: determina el siguiente número buscando el máximo ya usado para el prefijo.
     */
    private long calcularSiguienteDesdeJoyas(EntityManager em2, String prefijo) {
        try {
            EntityManager em3 = getEntityManager();
            try {
                @SuppressWarnings("unchecked")
                List<String> ids = em3.createQuery(
                    "SELECT j.displayId FROM Joya j WHERE j.displayId LIKE :p", String.class
                ).setParameter("p", prefijo + "%").getResultList();

                long max = 0L;
                for (String d : ids) {
                    if (d == null || d.length() <= prefijo.length()) continue;
                    try {
                        long v = Long.parseLong(d.substring(prefijo.length()));
                        if (v > max) max = v;
                    } catch (NumberFormatException ignored) {}
                }
                return max + 1L;
            } finally {
                em3.close();
            }
        } catch (Exception ignored) {
            return System.currentTimeMillis() % 100000; // último recurso: timestamp corto
        }
    }

    /**
     * Calcula el prefijo de 2 letras en minúscula a partir de la categoría.
     * Si la categoría es nula o muy corta se usa "xx" como prefijo genérico.
     */
    private String calcularPrefijo(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "xx";
        }
        // Eliminar tildes y caracteres especiales para tener letras limpias
        String normalizada = java.text.Normalizer.normalize(categoria.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        if (normalizada.isBlank()) {
            return "xx";
        }
        return normalizada.length() >= 2 ? normalizada.substring(0, 2) : normalizada;
    }

    public void edit(Joya joya) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(joya);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Joya joya = em.find(Joya.class, id);
            if (joya != null) {
                em.remove(joya);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Joya find(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Joya.class, id);
        } finally {
            em.close();
        }
    }

    public List<Joya> findAll() {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT * FROM Joya", Joya.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Joya> findAllOrderedByIdDesc() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT j FROM Joya j ORDER BY j.id ASC", Joya.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Joya> filtrarJoyas(String id, String categoria, String socio, String nombre, Boolean noVendido, List<String> estado) {
        return filtrarJoyas(id, categoria, socio, nombre, noVendido, estado, null);
    }

    public List<Joya> filtrarJoyas(String id, String categoria, String socio, String nombre, Boolean noVendido, List<String> estado, String puntoFisico) {
        EntityManager em = getEntityManager();
        try {
            String trimmedId = (id != null) ? id.trim() : null;
            boolean searchByDisplayId = false;
            if (trimmedId != null && !trimmedId.isEmpty()) {
                if (trimmedId.charAt(0) == 'M' || trimmedId.charAt(0) == 'm') {
                    searchByDisplayId = true;
                }
            }

            StringBuilder sql = new StringBuilder("SELECT * FROM Joya WHERE 1=1");

            if (trimmedId != null && !trimmedId.isEmpty()) {
                if (searchByDisplayId) {
                    sql.append(" AND LOWER(display_id) = :displayId");
                } else {
                    sql.append(" AND id = :id");
                }
            }
            if (puntoFisico != null && !puntoFisico.isBlank()) {
                sql.append(" AND LOWER(TRIM(COALESCE(punto_fisico, ''))) = :puntoFisico");
            }
            if (categoria != null && !categoria.isEmpty()) {
                sql.append(" AND categoria = :categoria");
            }
            if (socio != null && !socio.isEmpty()) {
                sql.append(" AND socio = :socio");
            }
            if (nombre != null && !nombre.isEmpty()) {
                sql.append(" AND LOWER(nombre) LIKE :nombre");
            }
            if (noVendido != null) {
                sql.append(" AND vendido = :vendido");
            }
            if (estado != null && !estado.isEmpty()) {
                StringBuilder inClause = new StringBuilder(" AND (");
                for (int i = 0; i < estado.size(); i++) {
                    if (i > 0) {
                        inClause.append(" OR ");
                    }
                    inClause.append("estado = :estado").append(i);
                }
                inClause.append(")");
                sql.append(inClause);
            }

            Query query = em.createNativeQuery(sql.toString(), Joya.class);

            if (trimmedId != null && !trimmedId.isEmpty()) {
                if (searchByDisplayId) {
                    query.setParameter("displayId", trimmedId.toLowerCase());
                } else {
                    try {
                        query.setParameter("id", Long.parseLong(trimmedId));
                    } catch (NumberFormatException nfe) {
                        return List.of();
                    }
                }
            }
            if (puntoFisico != null && !puntoFisico.isBlank()) {
                query.setParameter("puntoFisico", puntoFisico.trim().toLowerCase());
            }
            if (categoria != null && !categoria.isEmpty()) {
                query.setParameter("categoria", categoria);
            }
            if (socio != null && !socio.isEmpty()) {
                query.setParameter("socio", socio);
            }
            if (nombre != null && !nombre.isEmpty()) {
                query.setParameter("nombre", "%" + nombre.toLowerCase() + "%");
            }
            if (noVendido != null) {
                query.setParameter("vendido", !noVendido);
            }
            if (estado != null && !estado.isEmpty()) {
                for (int i = 0; i < estado.size(); i++) {
                    query.setParameter("estado" + i, estado.get(i));
                }
            }

            List<Joya> results = query.getResultList();

            if ((results == null || results.isEmpty()) && searchByDisplayId) {
                String suffix = trimmedId.substring(1);
                try {
                    long numericId = Long.parseLong(suffix);
                    StringBuilder sql2 = new StringBuilder("SELECT * FROM Joya WHERE 1=1");
                    sql2.append(" AND id = :id");
                    if (puntoFisico != null && !puntoFisico.isBlank()) sql2.append(" AND LOWER(TRIM(COALESCE(punto_fisico, ''))) = :puntoFisico");
                    if (categoria != null && !categoria.isEmpty()) sql2.append(" AND categoria = :categoria");
                    if (socio != null && !socio.isEmpty()) sql2.append(" AND socio = :socio");
                    if (nombre != null && !nombre.isEmpty()) sql2.append(" AND LOWER(nombre) LIKE :nombre");
                    if (noVendido != null) sql2.append(" AND vendido = :vendido");
                    if (estado != null && !estado.isEmpty()) {
                        StringBuilder inClause2 = new StringBuilder(" AND (");
                        for (int i = 0; i < estado.size(); i++) {
                            if (i > 0) inClause2.append(" OR ");
                            inClause2.append("estado = :estado2").append(i);
                        }
                        inClause2.append(")");
                        sql2.append(inClause2);
                    }

                    Query query2 = em.createNativeQuery(sql2.toString(), Joya.class);
                    query2.setParameter("id", numericId);
                    if (puntoFisico != null && !puntoFisico.isBlank()) query2.setParameter("puntoFisico", puntoFisico.trim().toLowerCase());
                    if (categoria != null && !categoria.isEmpty()) query2.setParameter("categoria", categoria);
                    if (socio != null && !socio.isEmpty()) query2.setParameter("socio", socio);
                    if (nombre != null && !nombre.isEmpty()) query2.setParameter("nombre", "%" + nombre.toLowerCase() + "%");
                    if (noVendido != null) query2.setParameter("vendido", !noVendido);
                    if (estado != null && !estado.isEmpty()) {
                        for (int i = 0; i < estado.size(); i++) {
                            query2.setParameter("estado2" + i, estado.get(i));
                        }
                    }

                    results = query2.getResultList();
                } catch (NumberFormatException ignored) {
                }
            }

            return results;
        } catch (Exception e) {
            System.err.println("Error al filtrar joyas: " + e.getMessage());
            return List.of();
        } finally {
            em.close();
        }
    }

    public Joya obtenerUltimaJoya() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT j FROM Joya j ORDER BY j.id DESC", Joya.class)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            System.err.println("Error al obtener la última joya: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<Joya> obtenerVentasEntre(LocalDateTime inicioInclusive, LocalDateTime finExclusive) {
        return obtenerVentasEntre(inicioInclusive, finExclusive, null);
    }

    public List<Joya> obtenerVentasEntre(LocalDateTime inicioInclusive, LocalDateTime finExclusive, String puntoFisico) {
        EntityManager em = getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT j FROM Joya j " +
                            "WHERE j.vendido = true " +
                            "AND j.fechaVendida IS NOT NULL " +
                            "AND j.fechaVendida >= :inicio " +
                            "AND j.fechaVendida < :fin " +
                            "AND LOWER(TRIM(COALESCE(j.estado, ''))) <> 'anulado' "
            );
            if (puntoFisico != null && !puntoFisico.isBlank()) {
                jpql.append("AND LOWER(TRIM(COALESCE(j.puntoFisico, ''))) = :puntoFisico ");
            }
            jpql.append("ORDER BY j.fechaVendida DESC");

            var query = em.createQuery(jpql.toString(), Joya.class)
                    .setParameter("inicio", inicioInclusive)
                    .setParameter("fin", finExclusive);
            if (puntoFisico != null && !puntoFisico.isBlank()) {
                query.setParameter("puntoFisico", puntoFisico.trim().toLowerCase());
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
