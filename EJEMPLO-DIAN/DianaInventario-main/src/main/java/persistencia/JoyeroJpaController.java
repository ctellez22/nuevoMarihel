package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import logica.Joyero;

import java.io.Serializable;
import java.util.List;

public class JoyeroJpaController implements Serializable {

	private final EntityManagerFactory emf;

	public JoyeroJpaController() {
		this.emf = PersistenceManager.getEntityManagerFactory();
	}

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(Joyero joyero) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(joyero);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public Joyero findByNombre(String nombre) {
		EntityManager em = getEntityManager();
		try {
			TypedQuery<Joyero> query = em.createQuery(
					"SELECT j FROM Joyero j WHERE LOWER(j.nombre) = LOWER(:nombre)",
					Joyero.class
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

	public List<Joyero> findAll() {
		EntityManager em = getEntityManager();
		try {
			return em.createQuery("SELECT j FROM Joyero j ORDER BY j.nombre ASC", Joyero.class)
					.getResultList();
		} finally {
			em.close();
		}
	}

	public void delete(Long id) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			Joyero joyero = em.find(Joyero.class, id);
			if (joyero != null) {
				em.remove(joyero);
			}
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}
}

