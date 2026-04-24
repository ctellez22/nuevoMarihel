package logica;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orden_trabajo")
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "joya_id", nullable = false)
    private Long joyaId;

    @Column(name = "joya_display_id")
    private String joyaDisplayId;

    @Column(name = "joya_nombre")
    private String joyaNombre;

    @Column(nullable = false)
    private String joyero;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "punto_fisico", nullable = false)
    private String puntoFisico;

    public OrdenTrabajo() {
        this.estado = "pendiente";
        this.creadoEn = LocalDateTime.now();
        this.puntoFisico = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJoyaId() {
        return joyaId;
    }

    public void setJoyaId(Long joyaId) {
        this.joyaId = joyaId;
    }

    public String getJoyaDisplayId() {
        return joyaDisplayId;
    }

    public void setJoyaDisplayId(String joyaDisplayId) {
        this.joyaDisplayId = joyaDisplayId;
    }

    public String getJoyaNombre() {
        return joyaNombre;
    }

    public void setJoyaNombre(String joyaNombre) {
        this.joyaNombre = joyaNombre;
    }

    public String getJoyero() {
        return joyero;
    }

    public void setJoyero(String joyero) {
        this.joyero = joyero;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getPuntoFisico() {
        return puntoFisico;
    }

    public void setPuntoFisico(String puntoFisico) {
        this.puntoFisico = puntoFisico;
    }
}

