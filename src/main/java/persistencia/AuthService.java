package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.example.SessionContext;

import java.util.List;

public class AuthService {

    public SessionContext authenticate(String username, String plainPassword) {
        if (username == null || username.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            return null;
        }

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            Query query = em.createNativeQuery("""
                    SELECT u.id, u.username, r.nombre
                    FROM app_usuario u
                    JOIN app_usuario_rol ur ON ur.usuario_id = u.id
                    JOIN app_rol r ON r.id = ur.rol_id
                    WHERE u.username = ?1
                      AND u.activo = TRUE
                      AND u.password_hash = SHA2(?2, 256)
                    ORDER BY u.id
                    LIMIT 1
                    """);
            query.setParameter(1, username.trim());
            query.setParameter(2, plainPassword);

            List<?> rows = query.getResultList();
            if (rows.isEmpty()) {
                return null;
            }

            Object[] row = (Object[]) rows.get(0);
            Long userId = ((Number) row[0]).longValue();
            String normalizedUsername = (String) row[1];
            SessionContext.Role role = SessionContext.Role.fromDbValue((String) row[2]);

            em.getTransaction().begin();
            Query updateLogin = em.createNativeQuery("UPDATE app_usuario SET ultimo_login = NOW() WHERE id = ?1");
            updateLogin.setParameter(1, userId);
            updateLogin.executeUpdate();
            em.getTransaction().commit();

            return new SessionContext(userId, normalizedUsername, role);
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException("No se pudo autenticar. Verifica usuario, contrasena y tablas de autenticacion.", ex);
        } finally {
            em.close();
        }
    }
}
