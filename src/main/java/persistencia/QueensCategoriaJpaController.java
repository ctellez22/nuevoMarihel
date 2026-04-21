package persistencia;

import jakarta.persistence.EntityManager;
import logica.Categoria;

import java.util.List;

/**
 * Controlador JPA para las categorías de Queens, operando sobre queens_categoria.
 */
public class QueensCategoriaJpaController extends CategoriaJpaController {

    @Override
    public void create(Categoria categoria) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("INSERT INTO queens_categoria (nombre) VALUES (?1)")
                    .setParameter(1, categoria.getNombre())
                    .executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public Categoria find(Long id) {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Categoria> result = em.createNativeQuery(
                    "SELECT * FROM queens_categoria WHERE id = ?1", Categoria.class)
                    .setParameter(1, id)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public Categoria findByNombre(String nombre) {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Categoria> result = em.createNativeQuery(
                    "SELECT * FROM queens_categoria WHERE LOWER(nombre) = LOWER(?1) LIMIT 1", Categoria.class)
                    .setParameter(1, nombre)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Categoria> findAll() {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Categoria> result = em.createNativeQuery(
                    "SELECT * FROM queens_categoria ORDER BY nombre ASC", Categoria.class)
                    .getResultList();
            return result;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM queens_categoria WHERE id = ?1")
                    .setParameter(1, id)
                    .executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
