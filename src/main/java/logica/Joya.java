package logica;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "joya")
public class Joya {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nombre;

    @Column
    private String precio;

    @Column
    private double peso;

    @Column
    private String categoria;

    @Column
    private String observacion;

    @Column(name = "tiene_piedra")
    private boolean tienePiedra;

    @Column(name = "info_piedra")
    private String infoPiedra;

    @Column(name = "fue_editada")
    private boolean fueEditada;

    @Column
    private boolean vendido; // Nuevo
    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_vendida")
    private LocalDateTime fechaVendida;

    @Column
    private String estado ;

    @Column(name = "autorizado")
    private boolean autorizado;

    // Socio asociado (valor seleccionado desde la tabla socios)
    @Column
    private String socio;

    // Nuevo campo para mostrar IDs legibles (ej. M1, M2...) sin tocar la PK
    @Column(name = "display_id", unique = true)
    private String displayId;

    // Constructor vacío
    public Joya() {
      this.vendido = false;
      this.estado = "disponible";
      this.autorizado = true;
    }

    // Constructor con parámetros
    public Joya(String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra) {
        this.nombre = nombre;
        this.precio = precio;
        this.peso = peso;
        this.categoria = categoria;
        this.socio = socio;
        this.observacion = observacion;
        this.tienePiedra = tienePiedra;
        this.infoPiedra = infoPiedra;
        this.fueEditada = false; // Inicializado como falso por defecto
        this.vendido = false;   // Inicializado como falso por defecto
        this.fechaIngreso = LocalDateTime.now();
        this.estado= "disponible";
        this.autorizado = true;

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

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean isTienePiedra() {
        return tienePiedra;
    }

    public void setTienePiedra(boolean tienePiedra) {
        this.tienePiedra = tienePiedra;
    }

    public String getInfoPiedra() {
        return infoPiedra;
    }

    public void setInfoPiedra(String infoPiedra) {
        this.infoPiedra = infoPiedra;
    }

    public boolean isFueEditada() {
        return fueEditada;
    }

    public void setFueEditada(boolean fueEditada) {
        this.fueEditada = fueEditada;
    }

    public boolean isVendido() {
        return vendido;
    }

    public void setVendido(boolean vendido) {
        this.vendido = vendido;
    }
    // Getters y Setters
    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public LocalDateTime getFechaVendida() {
        return fechaVendida;
    }

    public void setFechaVendida(LocalDateTime fechaVendida) {
        this.fechaVendida = fechaVendida;
    }
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public String getSocio() {
        return socio;
    }

    public void setSocio(String socio) {
        this.socio = socio;
    }

    public boolean isAutorizado() {
        return autorizado;
    }

    public void setAutorizado(boolean autorizado) {
        this.autorizado = autorizado;
    }

    public String getEstadoAutorizacionTexto() {
        return autorizado ? "Aprobado" : "Pendiente";
    }

    // Método toString
    @Override
    public String toString() {
        return "Joya{" +
                "id=" + id +
                ", displayId='" + displayId + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", peso=" + peso +
                ", categoria='" + categoria + '\'' +
                ", observacion='" + observacion + '\'' +
                ", tienePiedra=" + tienePiedra +
                ", infoPiedra='" + infoPiedra + '\'' +
                ", fueEditada=" + fueEditada +
                ", vendido=" + vendido +
                ", estado='" + estado + '\'' +
                ", autorizado=" + autorizado +
                ", socio='" + socio + '\'' +
                '}';
    }
}
