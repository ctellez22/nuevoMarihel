package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import logica.Joya;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador JPA para las joyas de Queens, operando sobre la tabla queens_Joya.
 * Usa exclusivamente consultas nativas para evitar conflictos con la entidad Joya
 * que apunta a la tabla 'joya' de Marihel.
 */
public class QueensJoyaJpaController extends JoyaJpaController {

    private static final String TABLA = "queens_Joya";
    private static final String PREFIJO_ID = "Q";

    /**
     * La tabla queens_Joya usa nombres de columna sin guion bajo (FECHAINGRESO, FUEEDITADA, etc.).
     * Este SELECT incluye alias para que Hibernate pueda mapear al entity Joya correctamente.
     */
    private static final String SEL =
            "SELECT id, nombre, precio, peso, categoria, observacion, " +
            "FUEEDITADA AS fue_editada, INFOPIEDRA AS info_piedra, TIENEPIEDRA AS tiene_piedra, " +
            "VENDIDO AS vendido, FECHAINGRESO AS fecha_ingreso, FECHAVENDIDA AS fecha_vendida, " +
            "ESTADO AS estado, precio_venta, autorizado, socio, display_id " +
            "FROM queens_Joya";

    @Override
    public void create(Joya joya) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            if (joya.getDisplayId() == null || joya.getDisplayId().isBlank()) {
                joya.setDisplayId(generarDisplayId(em));
            }

            em.createNativeQuery("""
                    INSERT INTO queens_Joya
                        (nombre, precio, precio_venta, peso, categoria, observacion,
                         TIENEPIEDRA, INFOPIEDRA, FUEEDITADA, VENDIDO, FECHAINGRESO,
                         FECHAVENDIDA, ESTADO, autorizado, socio, display_id)
                    VALUES
                        (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16)
                    """)
                    .setParameter(1, joya.getNombre())
                    .setParameter(2, joya.getPrecio())
                    .setParameter(3, joya.getPrecioVenta())
                    .setParameter(4, joya.getPeso())
                    .setParameter(5, joya.getCategoria())
                    .setParameter(6, joya.getObservacion())
                    .setParameter(7, joya.isTienePiedra())
                    .setParameter(8, joya.getInfoPiedra())
                    .setParameter(9, joya.isFueEditada())
                    .setParameter(10, joya.isVendido())
                    .setParameter(11, joya.getFechaIngreso())
                    .setParameter(12, joya.getFechaVendida())
                    .setParameter(13, joya.getEstado())
                    .setParameter(14, joya.isAutorizado())
                    .setParameter(15, joya.getSocio())
                    .setParameter(16, joya.getDisplayId())
                    .executeUpdate();

            @SuppressWarnings("unchecked")
            List<Object> ids = em.createNativeQuery("SELECT LAST_INSERT_ID()").getResultList();
            if (!ids.isEmpty()) {
                joya.setId(((Number) ids.get(0)).longValue());
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void edit(Joya joya) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("""
                    UPDATE queens_Joya SET
                        nombre=?1, precio=?2, precio_venta=?3, peso=?4, categoria=?5,
                        observacion=?6, TIENEPIEDRA=?7, INFOPIEDRA=?8, FUEEDITADA=?9,
                        VENDIDO=?10, FECHAVENDIDA=?11, ESTADO=?12, autorizado=?13,
                        socio=?14, display_id=?15
                    WHERE id=?16
                    """)
                    .setParameter(1, joya.getNombre())
                    .setParameter(2, joya.getPrecio())
                    .setParameter(3, joya.getPrecioVenta())
                    .setParameter(4, joya.getPeso())
                    .setParameter(5, joya.getCategoria())
                    .setParameter(6, joya.getObservacion())
                    .setParameter(7, joya.isTienePiedra())
                    .setParameter(8, joya.getInfoPiedra())
                    .setParameter(9, joya.isFueEditada())
                    .setParameter(10, joya.isVendido())
                    .setParameter(11, joya.getFechaVendida())
                    .setParameter(12, joya.getEstado())
                    .setParameter(13, joya.isAutorizado())
                    .setParameter(14, joya.getSocio())
                    .setParameter(15, joya.getDisplayId())
                    .setParameter(16, joya.getId())
                    .executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM queens_Joya WHERE id = ?1")
                    .setParameter(1, id)
                    .executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public Joya find(Long id) {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Joya> result = em.createNativeQuery(SEL + " WHERE id = ?1", Joya.class)
                    .setParameter(1, id)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Joya> findAll() {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery(SEL, Joya.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Joya> findAllOrderedByIdDesc() {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Joya> result = em.createNativeQuery(SEL + " ORDER BY id ASC", Joya.class)
                    .getResultList();
            return result;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Joya> filtrarJoyas(String id, String categoria, String socio, String nombre,
                                    Boolean noVendido, List<String> estado) {
        EntityManager em = getEntityManager();
        try {
            String trimmedId = (id != null) ? id.trim() : null;
            boolean searchByDisplayId = false;
            if (trimmedId != null && !trimmedId.isEmpty()) {
                char first = Character.toLowerCase(trimmedId.charAt(0));
                if (first == 'q') {
                    searchByDisplayId = true;
                }
            }

            StringBuilder sql = new StringBuilder(SEL + " WHERE 1=1");

            if (trimmedId != null && !trimmedId.isEmpty()) {
                if (searchByDisplayId) {
                    sql.append(" AND LOWER(display_id) = :displayId");
                } else {
                    sql.append(" AND id = :id");
                }
            }
            if (categoria != null && !categoria.isEmpty()) sql.append(" AND categoria = :categoria");
            if (socio != null && !socio.isEmpty())           sql.append(" AND socio = :socio");
            if (nombre != null && !nombre.isEmpty())         sql.append(" AND LOWER(nombre) LIKE :nombre");
            if (noVendido != null)                           sql.append(" AND VENDIDO = :vendido");
            if (estado != null && !estado.isEmpty()) {
                StringBuilder inClause = new StringBuilder(" AND (");
                for (int i = 0; i < estado.size(); i++) {
                    if (i > 0) inClause.append(" OR ");
                    inClause.append("ESTADO = :estado").append(i);
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
            if (categoria != null && !categoria.isEmpty()) query.setParameter("categoria", categoria);
            if (socio != null && !socio.isEmpty())           query.setParameter("socio", socio);
            if (nombre != null && !nombre.isEmpty())         query.setParameter("nombre", "%" + nombre.toLowerCase() + "%");
            if (noVendido != null)                           query.setParameter("vendido", !noVendido);
            if (estado != null && !estado.isEmpty()) {
                for (int i = 0; i < estado.size(); i++) {
                    query.setParameter("estado" + i, estado.get(i));
                }
            }

            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error al filtrar joyas Queens: " + e.getMessage());
            return List.of();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Joya> obtenerVentasPorRango(LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Joya> result = em.createNativeQuery(
                    SEL + " WHERE VENDIDO = TRUE AND FECHAVENDIDA >= ?1 AND FECHAVENDIDA <= ?2 ORDER BY FECHAVENDIDA DESC",
                    Joya.class)
                    .setParameter(1, desde)
                    .setParameter(2, hasta)
                    .getResultList();
            return result;
        } catch (Exception e) {
            System.err.println("Error al obtener ventas Queens por rango: " + e.getMessage());
            return List.of();
        } finally {
            em.close();
        }
    }

    @Override
    public Joya obtenerUltimaJoya() {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Joya> result = em.createNativeQuery(
                    SEL + " ORDER BY id DESC LIMIT 1", Joya.class)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            System.err.println("Error al obtener la última joya Queens: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    private String generarDisplayId(EntityManager em) {
        try {
            @SuppressWarnings("unchecked")
            List<String> displayIds = em.createNativeQuery(
                    "SELECT display_id FROM queens_Joya WHERE display_id LIKE 'Q%'", String.class)
                    .getResultList();
            int max = 0;
            for (String d : displayIds) {
                if (d == null) continue;
                try {
                    int v = Integer.parseInt(d.substring(1));
                    if (v > max) max = v;
                } catch (NumberFormatException ignored) {}
            }
            return PREFIJO_ID + (max + 1);
        } catch (Exception e) {
            System.err.println("No se pudo generar displayId Queens: " + e.getMessage());
            return PREFIJO_ID + System.currentTimeMillis();
        }
    }
}
