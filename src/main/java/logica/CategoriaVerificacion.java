package logica;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categoria_verificacion")  // Nombre de la tabla
public class CategoriaVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_categoria", nullable = false)
    private String nombreCategoria;

    @Column(name = "ultima_fecha_verificacion")
    private LocalDateTime ultimaFechaVerificacion;

    // Constructor vacío (requerido por JPA)
    public CategoriaVerificacion() {
    }

    // Constructor opcional para instanciar rápido
    public CategoriaVerificacion(String nombreCategoria, LocalDateTime ultimaFechaVerificacion) {
        this.nombreCategoria = nombreCategoria;
        this.ultimaFechaVerificacion = ultimaFechaVerificacion;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public LocalDateTime getUltimaFechaVerificacion() {
        return ultimaFechaVerificacion;
    }

    public void setUltimaFechaVerificacion(LocalDateTime ultimaFechaVerificacion) {
        this.ultimaFechaVerificacion = ultimaFechaVerificacion;
    }
}
