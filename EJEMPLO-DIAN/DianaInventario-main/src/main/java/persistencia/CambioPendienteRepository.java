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
                    WHERE estado = 'PENDIENTE' AND entidad IN ('joya', 'lote')
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
                    SELECT operacion, entidad_id, entidad
                    FROM cambio_pendiente
                    WHERE id = ?1 AND estado = 'PENDIENTE' AND entidad IN ('joya', 'lote')
                    """);
            qInfo.setParameter(1, pendienteId);
            List<?> rows = qInfo.getResultList();
            if (rows.isEmpty()) {
                throw new IllegalStateException("El cambio pendiente no existe o ya fue procesado.");
            }

            Object[] info = (Object[]) rows.get(0);
            String operacion = (String) info[0];
            Long entidadId = info[1] == null ? null : ((Number) info[1]).longValue();
            String entidad = (String) info[2];

            if ("lote".equalsIgnoreCase(entidad)) {
                if ("INSERT".equalsIgnoreCase(operacion)) {
                    Query approveLote = em.createNativeQuery("""
                            UPDATE lote
                            SET autorizado = TRUE,
                                actualizado_en = NOW(),
                                actualizado_por = ?2
                            WHERE id = ?1
                            """);
                    approveLote.setParameter(1, entidadId);
                    approveLote.setParameter(2, adminId);
                    approveLote.executeUpdate();
                    // Si updated == 0 el lote ya no existe en la BD (registro huérfano); se marca aprobado igual para limpiar.
                }
                // UPDATE de lote: cambio ya aplicado en BD, solo se marca aprobado.
            } else if ("INSERT".equalsIgnoreCase(operacion)) {
                // El peso del lote ya fue descontado al momento de crear la joya (sea admin o vendedor).
                // Solo se necesita marcar la joya como autorizada.
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

    private void descontarPesoLotesPorInfo(EntityManager em, String infoPiedra) {
        for (var entry : PiedraLoteParser.extraerPesoPorLote(infoPiedra).entrySet()) {
            Long loteId = entry.getKey();
            double pesoADescontar = entry.getValue();

            Query qLote = em.createNativeQuery("SELECT nombre, peso_total FROM lote WHERE id = ?1");
            qLote.setParameter(1, loteId);
            List<?> loteRows = qLote.getResultList();
            if (loteRows.isEmpty()) {
                throw new IllegalStateException("No se encontró el lote " + loteId + " asociado a la piedra.");
            }

            Object[] loteInfo = (Object[]) loteRows.get(0);
            String nombreLote = loteInfo[0] == null ? ("#" + loteId) : loteInfo[0].toString();
            double pesoDisponible = loteInfo[1] == null ? 0.0 : ((Number) loteInfo[1]).doubleValue();
            if (pesoDisponible < pesoADescontar) {
                throw new IllegalStateException("El lote '" + nombreLote + "' no tiene peso suficiente. Disponible: " + pesoDisponible);
            }

            Query qUpdate = em.createNativeQuery("""
                    UPDATE lote
                    SET peso_total = peso_total - ?2,
                        estado = CASE WHEN (peso_total - ?2) <= 0 THEN 'agotado' ELSE estado END,
                        actualizado_en = NOW()
                    WHERE id = ?1
                    """);
            qUpdate.setParameter(1, loteId);
            qUpdate.setParameter(2, pesoADescontar);
            qUpdate.executeUpdate();
        }
    }

    private void restaurarPesoLotesPorInfo(EntityManager em, String infoPiedra) {
        for (var entry : PiedraLoteParser.extraerPesoPorLote(infoPiedra).entrySet()) {
            Long loteId = entry.getKey();
            double pesoARestaurar = entry.getValue();
            Query qUpdate = em.createNativeQuery("""
                    UPDATE lote
                    SET peso_total = peso_total + ?2,
                        estado = CASE WHEN estado = 'agotado' AND (peso_total + ?2) > 0 THEN 'disponible' ELSE estado END,
                        actualizado_en = NOW()
                    WHERE id = ?1
                    """);
            qUpdate.setParameter(1, loteId);
            qUpdate.setParameter(2, pesoARestaurar);
            qUpdate.executeUpdate();
        }
    }

    public void rechazarPendiente(Long pendienteId, Long adminId, String comentario) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            Query qInfo = em.createNativeQuery("""
                    SELECT operacion, entidad_id, entidad
                    FROM cambio_pendiente
                    WHERE id = ?1 AND estado = 'PENDIENTE' AND entidad IN ('joya', 'lote')
                    """);
            qInfo.setParameter(1, pendienteId);
            List<?> rows = qInfo.getResultList();
            if (rows.isEmpty()) {
                throw new IllegalStateException("El cambio pendiente no existe o ya fue procesado.");
            }

            Object[] info = (Object[]) rows.get(0);
            String operacion = (String) info[0];
            Long entidadId = info[1] == null ? null : ((Number) info[1]).longValue();
            String entidad = (String) info[2];

            if ("lote".equalsIgnoreCase(entidad)) {
                if ("INSERT".equalsIgnoreCase(operacion)) {
                    Query rollbackLote = em.createNativeQuery("DELETE FROM lote WHERE id = ?1");
                    rollbackLote.setParameter(1, entidadId);
                    rollbackLote.executeUpdate();
                    // Si deleted == 0 el lote ya no existe (registro huérfano); se marca rechazado igual para limpiar.
                } else if ("UPDATE".equalsIgnoreCase(operacion)) {
                    Query rollbackLoteUpdate = em.createNativeQuery("""
                            UPDATE lote l
                            SET nombre = cp.before_json ->> 'nombre',
                                peso_total = COALESCE(CAST(cp.before_json ->> 'pesoTotal' AS double precision), l.peso_total),
                                cantidad_piedras = COALESCE(CAST(cp.before_json ->> 'cantidadPiedras' AS integer), l.cantidad_piedras),
                                tipo_piedra = cp.before_json ->> 'tipoPiedra',
                                calidad_piedra = cp.before_json ->> 'calidadPiedra',
                                descripcion = cp.before_json ->> 'descripcion',
                                precio_estimado = cp.before_json ->> 'precioEstimado',
                                socio = cp.before_json ->> 'socio',
                                categoria = cp.before_json ->> 'categoria',
                                observaciones = cp.before_json ->> 'observaciones',
                                actualizado_en = NOW(),
                                actualizado_por = ?2
                            FROM cambio_pendiente cp
                            WHERE cp.id = ?1
                              AND l.id = cp.entidad_id
                            """);
                    rollbackLoteUpdate.setParameter(1, pendienteId);
                    rollbackLoteUpdate.setParameter(2, adminId);
                    rollbackLoteUpdate.executeUpdate();
                    // Si updated == 0 el lote ya no existe (registro huérfano); se marca rechazado igual para limpiar.
                }
            } else if ("INSERT".equalsIgnoreCase(operacion)) {
                // Restaurar peso en los lotes antes de eliminar la joya (el peso fue descontado al crear).
                Query qInfoPiedra = em.createNativeQuery("SELECT info_piedra FROM joya WHERE id = ?1");
                qInfoPiedra.setParameter(1, entidadId);
                List<?> infoPiedraRows = qInfoPiedra.getResultList();
                if (!infoPiedraRows.isEmpty() && infoPiedraRows.get(0) != null) {
                    String infoPiedra = infoPiedraRows.get(0).toString();
                    if (infoPiedra != null && !infoPiedra.isBlank()) {
                        restaurarPesoLotesPorInfo(em, infoPiedra);
                    }
                }
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
                            precio_venta_real = NULLIF(cp.before_json ->> 'precioVentaReal', ''),
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

