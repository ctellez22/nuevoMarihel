package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import logica.Lote;

import java.io.Serializable;
import java.util.List;

public class LoteJpaController implements Serializable {

    private final EntityManagerFactory emf;

    public LoteJpaController() {
        this.emf = PersistenceManager.getEntityManagerFactory();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Lote lote) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(lote);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(Lote lote) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(lote);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Lote lote = em.find(Lote.class, id);
            if (lote != null) {
                em.remove(lote);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Lote find(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Lote.class, id);
        } finally {
            em.close();
        }
    }

    public List<Lote> findAll() {
        EntityManager em = getEntityManager();

        try {
            Query q = em.createQuery("SELECT l FROM Lote l");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Lote> findByEstado(String estado) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("SELECT l FROM Lote l WHERE l.estado = :estado");
            q.setParameter("estado", estado);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Lote obtenerUltimoLote() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("SELECT l FROM Lote l ORDER BY l.id DESC");
            q.setMaxResults(1);
            List<Lote> result = q.getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }
}

