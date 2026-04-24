package logica;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lote")
public class Lote {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nombre;

    @Column(name = "peso_total")
    private double pesoTotal;

    @Column(name = "cantidad_piedras")
    private int cantidadPiedras;

    @Column(name = "tipo_piedra")
    private String tipoPiedra;

    @Column(name = "calidad_piedra")
    private String calidadPiedra;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_estimado")
    private String precioEstimado;

    @Column(name = "precio_venta_real")
    private String precioVentaReal;

    @Column
    private String socio;

    @Column(name = "punto_fisico")
    private String puntoFisico;

    @Column
    private String categoria;

    @Column
    private String estado;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(name = "vendido")
    private boolean vendido;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column
    private boolean autorizado;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "actualizado_por")
    private Long actualizadoPor;

    // Constructor vacío
    public Lote() {
        this.estado = "disponible";
        this.autorizado = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public Lote(String nombre, double pesoTotal, int cantidadPiedras, String tipoPiedra,
                String calidadPiedra, String descripcion, String precioEstimado,
                String socio, String categoria, String observaciones) {
        this.nombre = nombre;
        this.pesoTotal = pesoTotal;
        this.cantidadPiedras = cantidadPiedras;
        this.tipoPiedra = tipoPiedra;
        this.calidadPiedra = calidadPiedra;
        this.descripcion = descripcion;
        this.precioEstimado = precioEstimado;
        this.socio = socio;
        this.categoria = categoria;
        this.observaciones = observaciones;
        this.estado = "disponible";
        this.autorizado = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPesoTotal() {
        return pesoTotal;
    }

    public void setPesoTotal(double pesoTotal) {
        this.pesoTotal = pesoTotal;
    }

    public int getCantidadPiedras() {
        return cantidadPiedras;
    }

    public void setCantidadPiedras(int cantidadPiedras) {
        this.cantidadPiedras = cantidadPiedras;
    }

    public String getTipoPiedra() {
        return tipoPiedra;
    }

    public void setTipoPiedra(String tipoPiedra) {
        this.tipoPiedra = tipoPiedra;
    }

    public String getCalidadPiedra() {
        return calidadPiedra;
    }

    public void setCalidadPiedra(String calidadPiedra) {
        this.calidadPiedra = calidadPiedra;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecioEstimado() {
        return precioEstimado;
    }

    public void setPrecioEstimado(String precioEstimado) {
        this.precioEstimado = precioEstimado;
    }

    public String getPrecioVentaReal() {
        return precioVentaReal;
    }

    public void setPrecioVentaReal(String precioVentaReal) {
        this.precioVentaReal = precioVentaReal;
    }

    public String getSocio() {
        return socio;
    }

    public void setSocio(String socio) {
        this.socio = socio;
    }

    public String getPuntoFisico() {
        return puntoFisico;
    }

    public void setPuntoFisico(String puntoFisico) {
        this.puntoFisico = puntoFisico;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public boolean isVendido() {
        return vendido;
    }

    public void setVendido(boolean vendido) {
        this.vendido = vendido;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isAutorizado() {
        return autorizado;
    }

    public void setAutorizado(boolean autorizado) {
        this.autorizado = autorizado;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public Long getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(Long actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }

    public String getEstadoAutorizacionTexto() {
        return autorizado ? "Aprobado" : "Pendiente";
    }

    @Override
    public String toString() {
        return "Lote{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", pesoTotal=" + pesoTotal +
                ", cantidadPiedras=" + cantidadPiedras +
                ", tipoPiedra='" + tipoPiedra + '\'' +
                ", calidadPiedra='" + calidadPiedra + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", descripcion='" + descripcion + '\'' +
                ", precioEstimado='" + precioEstimado + '\'' +
                ", precioVentaReal='" + precioVentaReal + '\'' +
                ", socio='" + socio + '\'' +
                ", puntoFisico='" + puntoFisico + '\'' +
                ", categoria='" + categoria + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaVenta=" + fechaVenta +
                ", observaciones='" + observaciones + '\'' +
                ", autorizado=" + autorizado +
                '}';
    }
}

