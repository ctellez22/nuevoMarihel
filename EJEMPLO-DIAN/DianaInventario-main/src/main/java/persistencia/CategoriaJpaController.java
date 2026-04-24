package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import logica.Categoria;

import java.io.Serializable;
import java.util.List;

public class CategoriaJpaController implements Serializable {

    private final EntityManagerFactory emf;

    public CategoriaJpaController() {
        this.emf = PersistenceManager.getEntityManagerFactory();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Categoria categoria) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(categoria);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Categoria find(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Categoria.class, id);
        } finally {
            em.close();
        }
    }

    public Categoria findByNombre(String nombre) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Categoria> query = em.createQuery(
                    "SELECT c FROM Categoria c WHERE LOWER(c.nombre) = LOWER(:nombre)",
                    Categoria.class
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

    public List<Categoria> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Categoria c ORDER BY c.nombre ASC", Categoria.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Categoria categoria = em.find(Categoria.class, id);
            if (categoria != null) {
                em.remove(categoria);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
