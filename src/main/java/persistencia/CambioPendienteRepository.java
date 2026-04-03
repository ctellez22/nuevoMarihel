package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import logica.CambioPendiente;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CambioPendienteRepository {

    public void crearPendiente(String entidad,
                               Long entidadId,
                               String operacion,
                               String beforeJson,
                               String afterJson,
                               Long solicitadoPor) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Query query = em.createNativeQuery("""
                    INSERT INTO cambio_pendiente (entidad, entidad_id, operacion, before_json, after_json, estado, solicitado_por, solicitado_en)
                    VALUES (?1, ?2, ?3, CAST(?4 AS jsonb), CAST(?5 AS jsonb), 'PENDIENTE', ?6, NOW())
                    """);
            query.setParameter(1, entidad);
            query.setParameter(2, entidadId);
            query.setParameter(3, operacion);
            query.setParameter(4, beforeJson);
            query.setParameter(5, afterJson);
            query.setParameter(6, solicitadoPor);
            query.executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<CambioPendiente> listarPendientesJoya() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            Query query = em.createNativeQuery("""
                    SELECT id, entidad, entidad_id, operacion, CAST(before_json AS text), CAST(after_json AS text), solicitado_por, solicitado_en
                    FROM cambio_pendiente
                    WHERE estado = 'PENDIENTE' AND entidad = 'joya'
                    ORDER BY solicitado_en ASC
                    """);
            List<?> rows = query.getResultList();
            List<CambioPendiente> salida = new ArrayList<>();
            for (Object rowObj : rows) {
                Object[] row = (Object[]) rowObj;
                Long id = ((Number) row[0]).longValue();
                String entidad = (String) row[1];
                Long entidadId = row[2] == null ? null : ((Number) row[2]).longValue();
                String operacion = (String) row[3];
                String beforeJson = row[4] == null ? "{}" : row[4].toString();
                String afterJson = row[5] == null ? "{}" : row[5].toString();
                Long solicitadoPor = ((Number) row[6]).longValue();
                LocalDateTime solicitadoEn = ((Timestamp) row[7]).toLocalDateTime();
                salida.add(new CambioPendiente(id, entidad, entidadId, operacion, beforeJson, afterJson, solicitadoPor, solicitadoEn));
            }
            return salida;
        } finally {
            em.close();
        }
    }

    public void aprobarPendienteJoya(Long pendienteId, Long adminId) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            Query qInfo = em.createNativeQuery("""
                    SELECT operacion, entidad_id
                    FROM cambio_pendiente
                    WHERE id = ?1 AND estado = 'PENDIENTE' AND entidad = 'joya'
                    """);
            qInfo.setParameter(1, pendienteId);
            List<?> rows = qInfo.getResultList();
            if (rows.isEmpty()) {
                throw new IllegalStateException("El cambio pendiente no existe o ya fue procesado.");
            }

            Object[] info = (Object[]) rows.get(0);
            String operacion = (String) info[0];
            Long entidadId = info[1] == null ? null : ((Number) info[1]).longValue();

            if ("INSERT".equalsIgnoreCase(operacion)) {
                Query approveInsert = em.createNativeQuery("""
                        UPDATE joya
                        SET autorizado = TRUE,
                            actualizado_en = NOW(),
                            actualizado_por = ?2
                        WHERE id = ?1
                        """);
                approveInsert.setParameter(1, entidadId);
                approveInsert.setParameter(2, adminId);
                int updated = approveInsert.executeUpdate();
                if (updated == 0) {
                    throw new IllegalStateException("No se encontro la joya creada para aprobar.");
                }
            } else if ("UPDATE".equalsIgnoreCase(operacion)) {
                Query approveUpdate = em.createNativeQuery("""
                        UPDATE joya
                        SET autorizado = TRUE,
                            actualizado_en = NOW(),
                            actualizado_por = ?2,
                            fue_editada = TRUE
                        WHERE id = ?1
                        """);
                approveUpdate.setParameter(1, entidadId);
                approveUpdate.setParameter(2, adminId);
                int updated = approveUpdate.executeUpdate();
                if (updated == 0) {
                    throw new IllegalStateException("No se encontro la joya a actualizar para la aprobacion.");
                }
            } else {
                throw new IllegalStateException("Operacion no soportada: " + operacion);
            }

            Query markApproved = em.createNativeQuery("""
                    UPDATE cambio_pendiente
                    SET estado = 'APROBADO', revisado_por = ?2, revisado_en = NOW(), comentario = 'Aprobado por administrador'
                    WHERE id = ?1
                    """);
            markApproved.setParameter(1, pendienteId);
            markApproved.setParameter(2, adminId);
            markApproved.executeUpdate();

            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void rechazarPendiente(Long pendienteId, Long adminId, String comentario) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            Query qInfo = em.createNativeQuery("""
                    SELECT operacion, entidad_id
                    FROM cambio_pendiente
                    WHERE id = ?1 AND estado = 'PENDIENTE' AND entidad = 'joya'
                    """);
            qInfo.setParameter(1, pendienteId);
            List<?> rows = qInfo.getResultList();
            if (rows.isEmpty()) {
                throw new IllegalStateException("El cambio pendiente no existe o ya fue procesado.");
            }

            Object[] info = (Object[]) rows.get(0);
            String operacion = (String) info[0];
            Long entidadId = info[1] == null ? null : ((Number) info[1]).longValue();

            if ("INSERT".equalsIgnoreCase(operacion)) {
                Query rollbackInsert = em.createNativeQuery("DELETE FROM joya WHERE id = ?1");
                rollbackInsert.setParameter(1, entidadId);
                int deleted = rollbackInsert.executeUpdate();
                if (deleted == 0) {
                    throw new IllegalStateException("No se encontro la joya provisional para revertir el rechazo.");
                }
            } else if ("UPDATE".equalsIgnoreCase(operacion)) {
                Query rollbackUpdate = em.createNativeQuery("""
                        UPDATE joya j
                        SET nombre = cp.before_json ->> 'nombre',
                            precio = cp.before_json ->> 'precio',
                            peso = COALESCE(CAST(cp.before_json ->> 'peso' AS double precision), j.peso),
                            categoria = cp.before_json ->> 'categoria',
                            socio = cp.before_json ->> 'socio',
                            observacion = cp.before_json ->> 'observacion',
                            tiene_piedra = COALESCE(CAST(cp.before_json ->> 'tienePiedra' AS boolean), j.tiene_piedra),
                            info_piedra = cp.before_json ->> 'infoPiedra',
                            vendido = COALESCE(CAST(cp.before_json ->> 'vendido' AS boolean), j.vendido),
                            estado = COALESCE(cp.before_json ->> 'estado', j.estado),
                            fecha_vendida = CAST(NULLIF(cp.before_json ->> 'fechaVendida', '') AS timestamp),
                            autorizado = TRUE,
                            actualizado_en = NOW(),
                            actualizado_por = ?2
                        FROM cambio_pendiente cp
                        WHERE cp.id = ?1
                          AND j.id = cp.entidad_id
                        """);
                rollbackUpdate.setParameter(1, pendienteId);
                rollbackUpdate.setParameter(2, adminId);
                int updated = rollbackUpdate.executeUpdate();
                if (updated == 0) {
                    throw new IllegalStateException("No se encontro la joya para revertir el rechazo.");
                }
            } else {
                throw new IllegalStateException("Operacion no soportada para rechazo: " + operacion);
            }

            Query reject = em.createNativeQuery("""
                    UPDATE cambio_pendiente
                    SET estado = 'RECHAZADO', revisado_por = ?2, revisado_en = NOW(), comentario = ?3
                    WHERE id = ?1 AND estado = 'PENDIENTE'
                    """);
            reject.setParameter(1, pendienteId);
            reject.setParameter(2, adminId);
            reject.setParameter(3, (comentario == null || comentario.isBlank()) ? "Rechazado por administrador" : comentario.trim());
            int changed = reject.executeUpdate();
            if (changed == 0) {
                throw new IllegalStateException("El cambio pendiente no existe o ya fue procesado.");
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}

