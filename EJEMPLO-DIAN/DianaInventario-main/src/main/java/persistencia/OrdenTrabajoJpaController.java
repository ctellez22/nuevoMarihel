package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import logica.OrdenTrabajo;

import java.io.Serializable;
import java.util.List;

public class OrdenTrabajoJpaController implements Serializable {

    private final EntityManagerFactory emf;

    public OrdenTrabajoJpaController() {
        this.emf = PersistenceManager.getEntityManagerFactory();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(OrdenTrabajo ordenTrabajo) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ordenTrabajo);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<OrdenTrabajo> findAllOrderedByNewest() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT o FROM OrdenTrabajo o ORDER BY o.id DESC", OrdenTrabajo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<OrdenTrabajo> findByPuntoFisico(String puntoFisico) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM OrdenTrabajo o WHERE LOWER(COALESCE(o.puntoFisico, '')) = :punto ORDER BY o.id DESC",
                            OrdenTrabajo.class)
                    .setParameter("punto", puntoFisico == null ? "" : puntoFisico.trim().toLowerCase())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public OrdenTrabajo find(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(OrdenTrabajo.class, id);
        } finally {
            em.close();
        }
    }

    public void completarOrden(Long ordenId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            OrdenTrabajo orden = em.find(OrdenTrabajo.class, ordenId);
            if (orden == null) {
                throw new IllegalStateException("No se encontró la orden de trabajo #" + ordenId);
            }
            orden.setEstado("completado");
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}

