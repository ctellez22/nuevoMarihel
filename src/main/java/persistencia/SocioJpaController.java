package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import logica.Socio;

import java.io.Serializable;
import java.util.List;

public class SocioJpaController implements Serializable {

    private final EntityManagerFactory emf;

    public SocioJpaController() {
        this.emf = PersistenceManager.getEntityManagerFactory();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Socio socio) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(socio);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Socio find(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Socio.class, id);
        } finally {
            em.close();
        }
    }

    public Socio findByNombre(String nombre) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Socio> query = em.createQuery(
                    "SELECT s FROM Socio s WHERE LOWER(s.nombre) = LOWER(:nombre)",
                    Socio.class
            );
            query.setParameter("nombre", nombre);
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Socio> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Socio s ORDER BY s.nombre ASC", Socio.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Devuelve socios cuya tienda coincida con la indicada O sea NULL (compartidos). */
    public List<Socio> findByTienda(String tienda) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM Socio s WHERE s.tienda IS NULL OR UPPER(s.tienda) = UPPER(:tienda) ORDER BY s.nombre ASC",
                            Socio.class)
                    .setParameter("tienda", tienda)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Socio socio = em.find(Socio.class, id);
            if (socio != null) {
                em.remove(socio);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}

