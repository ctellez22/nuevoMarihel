package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import logica.Joya;

import java.io.Serializable;
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
                    try {
                        em.createNativeQuery("CREATE TABLE IF NOT EXISTS display_id_seq (id INT PRIMARY KEY, last_val BIGINT)").executeUpdate();
                    } catch (Exception ignore) {
                    }

                    Long lastVal = null;
                    try {
                        @SuppressWarnings("unchecked")
                        List<Object> rows = em.createNativeQuery("SELECT last_val FROM display_id_seq WHERE id = 1 FOR UPDATE").getResultList();
                        if (rows.isEmpty()) {
                            em.createNativeQuery("INSERT INTO display_id_seq (id, last_val) VALUES (1, 0)").executeUpdate();
                            lastVal = 0L;
                        } else {
                            Object o = rows.get(0);
                            lastVal = (o instanceof Number) ? ((Number) o).longValue() : Long.parseLong(o.toString());
                        }
                    } catch (Exception e) {
                        try {
                            @SuppressWarnings("unchecked")
                            List<Object> rows = em.createNativeQuery("SELECT last_val FROM display_id_seq WHERE id = 1").getResultList();
                            if (rows.isEmpty()) {
                                em.createNativeQuery("INSERT INTO display_id_seq (id, last_val) VALUES (1, 0)").executeUpdate();
                                lastVal = 0L;
                            } else {
                                Object o = rows.get(0);
                                lastVal = (o instanceof Number) ? ((Number) o).longValue() : Long.parseLong(o.toString());
                            }
                        } catch (Exception ex) {
                            lastVal = 0L;
                            try {
                                @SuppressWarnings("unchecked")
                                List<String> displayIds = em.createQuery("SELECT j.displayId FROM Joya j WHERE j.displayId LIKE :p", String.class)
                                        .setParameter("p", "M%")
                                        .getResultList();
                                int max = 0;
                                for (String d : displayIds) {
                                    if (d == null) {
                                        continue;
                                    }
                                    String numPart = d.startsWith("M") ? d.substring(1) : null;
                                    if (numPart != null) {
                                        try {
                                            int v = Integer.parseInt(numPart);
                                            if (v > max) {
                                                max = v;
                                            }
                                        } catch (NumberFormatException ignored) {
                                        }
                                    }
                                }
                                lastVal = (long) max;
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    long next = (lastVal == null ? 1L : lastVal + 1L);

                    try {
                        int updated = em.createNativeQuery("UPDATE display_id_seq SET last_val = ?1 WHERE id = 1")
                                .setParameter(1, next)
                                .executeUpdate();
                        if (updated == 0) {
                            em.createNativeQuery("INSERT INTO display_id_seq (id, last_val) VALUES (1, ?1)")
                                    .setParameter(1, next)
                                    .executeUpdate();
                        }
                    } catch (Exception e) {
                        System.err.println("No se pudo actualizar display_id_seq: " + e.getMessage());
                    }

                    joya.setDisplayId("M" + next);
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
            Query query = em.createNativeQuery("SELECT * FROM joya", Joya.class);
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
        EntityManager em = getEntityManager();
        try {
            String trimmedId = (id != null) ? id.trim() : null;
            boolean searchByDisplayId = false;
            if (trimmedId != null && !trimmedId.isEmpty()) {
                if (trimmedId.charAt(0) == 'M' || trimmedId.charAt(0) == 'm') {
                    searchByDisplayId = true;
                }
            }

            StringBuilder sql = new StringBuilder("SELECT * FROM joya WHERE 1=1");

            if (trimmedId != null && !trimmedId.isEmpty()) {
                if (searchByDisplayId) {
                    sql.append(" AND LOWER(display_id) = :displayId");
                } else {
                    sql.append(" AND id = :id");
                }
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
                    StringBuilder sql2 = new StringBuilder("SELECT * FROM joya WHERE 1=1");
                    sql2.append(" AND id = :id");
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

    public List<Joya> obtenerVentasPorRango(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT j FROM Joya j WHERE j.vendido = true" +
                    " AND j.fechaVendida >= :desde AND j.fechaVendida <= :hasta" +
                    " ORDER BY j.fechaVendida DESC", Joya.class)
                    .setParameter("desde", desde)
                    .setParameter("hasta", hasta)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error al obtener ventas por rango: " + e.getMessage());
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
}
