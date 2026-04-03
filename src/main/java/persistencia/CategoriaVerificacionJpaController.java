package persistencia;

import jakarta.persistence.*;
import logica.CategoriaVerificacion;

import java.io.Serializable;
import java.util.List;

public class CategoriaVerificacionJpaController implements Serializable {

    private final EntityManagerFactory emf;

    public CategoriaVerificacionJpaController() {
        this.emf = PersistenceManager.getEntityManagerFactory();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // Método para crear un nuevo registro
    public void create(CategoriaVerificacion catVerif) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(catVerif);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Método para editar un registro existente
    public void edit(CategoriaVerificacion catVerif) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(catVerif);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }


    public CategoriaVerificacion findByNombre(String nombreCategoria) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createQuery("SELECT c FROM CategoriaVerificacion c WHERE c.nombreCategoria = :nombre");
            query.setParameter("nombre", nombreCategoria);
            return (CategoriaVerificacion) query.getSingleResult();
        } catch (NoResultException e) {
            // No se encontró la categoría, retornamos null
            return null;
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error para depurar
            return null;
        } finally {
            em.close();
        }
    }


    // Método para obtener todas las categorías ordenadas por la última fecha de verificación
    // de la más antigua a la más reciente
    public List<CategoriaVerificacion> findAllOrderedByFechaVerificacion() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<CategoriaVerificacion> query = em.createQuery(
                    "SELECT c FROM CategoriaVerificacion c ORDER BY c.ultimaFechaVerificacion ASC",
                    CategoriaVerificacion.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

}
