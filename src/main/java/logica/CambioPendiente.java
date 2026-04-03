package logica;

import java.time.LocalDateTime;

public class CambioPendiente {
    private final Long id;
    private final String entidad;
    private final Long entidadId;
    private final String operacion;
    private final String beforeJson;
    private final String afterJson;
    private final Long solicitadoPor;
    private final LocalDateTime solicitadoEn;

    public CambioPendiente(Long id,
                          String entidad,
                          Long entidadId,
                          String operacion,
                          String beforeJson,
                          String afterJson,
                          Long solicitadoPor,
                          LocalDateTime solicitadoEn) {
        this.id = id;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.operacion = operacion;
        this.beforeJson = beforeJson;
        this.afterJson = afterJson;
        this.solicitadoPor = solicitadoPor;
        this.solicitadoEn = solicitadoEn;
    }

    public Long getId() {
        return id;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getAfterJson() {
        return afterJson;
    }

    public String getBeforeJson() {
        return beforeJson;
    }

    public Long getSolicitadoPor() {
        return solicitadoPor;
    }

    public LocalDateTime getSolicitadoEn() {
        return solicitadoEn;
    }

    @Override
    public String toString() {
        return "#" + id + " | " + operacion + " | " + entidad + "(" + (entidadId == null ? "nuevo" : entidadId) + ")";
    }
}

