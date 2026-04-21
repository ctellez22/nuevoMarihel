package persistencia;


import logica.CambioPendiente;
import logica.Categoria;
import logica.CategoriaVerificacion;
import logica.Joya;
import logica.Socio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.example.SessionContext;

import java.time.LocalDateTime;
import java.util.List;

public class ControladoraPersistencia {

    private JoyaJpaController joyaController;
    private CategoriaVerificacionJpaController catVerifController;
    private CategoriaJpaController categoriaController;
    private SocioJpaController socioController;
    private CambioPendienteRepository cambioPendienteRepository;
    private final String joyaTableName;
    private final SessionContext.Tienda tienda;

    // Constructor sin tienda (Marihel por defecto)
    public ControladoraPersistencia() {
        this(null);
    }

    // Constructor con tienda seleccionada
    public ControladoraPersistencia(SessionContext.Tienda tienda) {
        boolean esQueens = tienda == SessionContext.Tienda.QUEENS;
        this.tienda              = tienda;
        this.joyaController      = esQueens ? new QueensJoyaJpaController() : new JoyaJpaController();
        this.catVerifController  = new CategoriaVerificacionJpaController();
        this.categoriaController = new CategoriaJpaController(); // categorías compartidas entre ambas joyerías
        this.socioController     = new SocioJpaController();
        this.cambioPendienteRepository = new CambioPendienteRepository();
        this.joyaTableName       = esQueens ? "queens_Joya" : "joya";
    }

    // Métodos para interactuar con JoyaJpaController

    // Crear una nueva Joya
    public void agregarJoya(String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra) {
        Joya nuevaJoya = new Joya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);
        joyaController.create(nuevaJoya);
        System.out.println("Joya agregada: " + nuevaJoya);
    }

    public Joya registrarPendienteCrearJoya(Long solicitadoPor,
                                            String nombre,
                                            String precio,
                                            double peso,
                                            String categoria,
                                            String socio,
                                            String observacion,
                                            boolean tienePiedra,
                                            String infoPiedra) {
        Joya nuevaJoya = new Joya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);
        nuevaJoya.setAutorizado(false);
        joyaController.create(nuevaJoya);

        String afterJson = jsonJoya(
                nuevaJoya.getNombre(),
                nuevaJoya.getPrecio(),
                nuevaJoya.getPeso(),
                nuevaJoya.getCategoria(),
                nuevaJoya.getSocio(),
                nuevaJoya.getObservacion(),
                nuevaJoya.isTienePiedra(),
                nuevaJoya.getInfoPiedra(),
                nuevaJoya.isVendido(),
                nuevaJoya.getEstado(),
                null,
                nuevaJoya.getPrecioVenta()
        );
        cambioPendienteRepository.crearPendiente("joya", nuevaJoya.getId(), "INSERT", null, afterJson, solicitadoPor);
        return nuevaJoya;
    }

    // Editar una Joya existente
    public void editarJoya(Long id, String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra, Boolean vendido, LocalDateTime fechaVendida, String estado, String precioVenta) {
        Joya joya = joyaController.find(id);
        if (joya != null) {
            joya.setNombre(nombre);
            joya.setPrecio(precio);
            joya.setPeso(peso);
            joya.setCategoria(categoria);
            joya.setSocio(socio);
            joya.setObservacion(observacion);
            joya.setTienePiedra(tienePiedra);
            joya.setInfoPiedra(infoPiedra);
            joya.setVendido(vendido);
            joya.setFechaVendida(fechaVendida);
            joya.setEstado(estado);
            joya.setPrecioVenta(precioVenta);
            joyaController.edit(joya);
            System.out.println("Joya editada: " + joya);
        } else {
            System.out.println("Joya con ID " + id + " no encontrada.");
        }
    }

    public void registrarPendienteActualizarJoya(Long solicitadoPor,
                                                  Long id,
                                                  String nombre,
                                                  String precio,
                                                  double peso,
                                                  String categoria,
                                                  String socio,
                                                  String observacion,
                                                  boolean tienePiedra,
                                                  String infoPiedra,
                                                  Boolean vendido,
                                                  LocalDateTime fechaVendida,
                                                  String estado,
                                                  String precioVenta) {
        Joya actual = joyaController.find(id);
        if (actual == null) {
            throw new IllegalArgumentException("Joya con ID " + id + " no encontrada.");
        }

        String beforeJson = jsonJoya(
                actual.getNombre(),
                actual.getPrecio(),
                actual.getPeso(),
                actual.getCategoria(),
                actual.getSocio(),
                actual.getObservacion(),
                actual.isTienePiedra(),
                actual.getInfoPiedra(),
                actual.isVendido(),
                actual.getEstado(),
                actual.getFechaVendida(),
                actual.getPrecioVenta()
        );

        String afterJson = jsonJoya(
                nombre,
                precio,
                peso,
                categoria,
                socio,
                observacion,
                tienePiedra,
                infoPiedra,
                vendido != null && vendido,
                estado,
                fechaVendida,
                precioVenta
        );

        editarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, precioVenta);
        marcarJoyaNoAutorizada(id);

        cambioPendienteRepository.crearPendiente("joya", id, "UPDATE", beforeJson, afterJson, solicitadoPor);
    }

    public void registrarPendienteMarcarVendida(Long solicitadoPor, Long id, String precioVenta) {
        Joya actual = joyaController.find(id);
        if (actual == null) {
            throw new IllegalArgumentException("Joya con ID " + id + " no encontrada.");
        }
        if (actual.isVendido()) {
            throw new IllegalStateException("La joya ya esta marcada como vendida.");
        }

        LocalDateTime fechaVenta = LocalDateTime.now();
        registrarPendienteActualizarJoya(
                solicitadoPor,
                id,
                actual.getNombre(),
                actual.getPrecio(),
                actual.getPeso(),
                actual.getCategoria(),
                actual.getSocio(),
                actual.getObservacion(),
                actual.isTienePiedra(),
                actual.getInfoPiedra(),
                true,
                fechaVenta,
                actual.getEstado(),
                precioVenta
        );
    }

    // Eliminar una Joya
    public void eliminarJoya(Long id) {
        Joya joya = joyaController.find(id);
        if (joya != null) {
            joyaController.delete(id);
            System.out.println("Joya eliminada con ID: " + id);
        } else {
            System.out.println("Joya con ID " + id + " no encontrada.");
        }
    }

    // Listar todas las Joyas
    public List<Joya> obtenerTodasLasJoyas() {
        return joyaController.findAll();
    }

    public List<Joya> obtenerTodasLasJoyasByIdDes() {
        return joyaController.findAllOrderedByIdDesc();
    }




    // Buscar una Joya por ID
    public Joya obtenerJoyaPorId(Long id) {
        Joya joya = joyaController.find(id);
        if (joya != null) {
            return joya;
        } else {
            System.out.println("Joya con ID " + id + " no encontrada.");
            return null;
        }
    }
    //Filtrar Joyas
    public List<Joya> filtrarJoyas(String id, String categoria, String socio, String nombre, Boolean noVendido, List<String> estado) {
        return joyaController.filtrarJoyas(id, categoria, socio, nombre, noVendido, estado);
    }
    public List<Joya> obtenerVentasPorRango(LocalDateTime desde, LocalDateTime hasta) {
        return joyaController.obtenerVentasPorRango(desde, hasta);
    }

    //Obtener  la ultima joya
    public Joya obtenerUltimaJoya() {
        return joyaController.obtenerUltimaJoya();
    }

    // Método para actualizar o insertar la fecha de verificación de una categoría
    public void actualizarFechaVerificacionCategoria(String nombreCategoria, LocalDateTime fechaVerificacion) {
        CategoriaVerificacion catVerif = catVerifController.findByNombre(nombreCategoria);
        if (catVerif != null) {
            // Si ya existe, actualizamos la fecha
            catVerif.setUltimaFechaVerificacion(fechaVerificacion);
            catVerifController.edit(catVerif);
        } else {
            // Si no existe, creamos uno nuevo
            catVerif = new CategoriaVerificacion(nombreCategoria, fechaVerificacion);
            catVerifController.create(catVerif);
        }
        System.out.println("Registro de verificación actualizado para la categoría: " + nombreCategoria);
    }

    // Método para obtener las categorías ordenadas por fecha de verificación (de la más antigua a la más reciente)
    public List<CategoriaVerificacion> obtenerCategoriasOrdenadasPorFecha() {
        return catVerifController.findAllOrderedByFechaVerificacion();
    }

    // Crear categoria
    public void crearCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        agregarCategoria(nombre.trim());
    }

    // Crear una nueva categoria (evita duplicados por nombre)
    public void agregarCategoria(String nombre) {
        Categoria existente = categoriaController.findByNombre(nombre);
        if (existente != null) {
            throw new IllegalArgumentException("La categoría '" + nombre + "' ya existe.");
        }
        categoriaController.create(new Categoria(nombre));
    }

    public List<CambioPendiente> listarPendientesJoya() {
        return cambioPendienteRepository.listarPendientesJoya();
    }

    public void aprobarPendienteJoya(Long pendienteId, Long adminId) {
        cambioPendienteRepository.aprobarPendienteJoya(pendienteId, adminId);
    }

    public void rechazarPendiente(Long pendienteId, Long adminId, String comentario) {
        cambioPendienteRepository.rechazarPendiente(pendienteId, adminId, comentario);
    }

    private String jsonJoya(String nombre,
                            String precio,
                            double peso,
                            String categoria,
                            String socio,
                            String observacion,
                            boolean tienePiedra,
                            String infoPiedra,
                            boolean vendido,
                            String estado,
                            LocalDateTime fechaVendida,
                            String precioVenta) {
        return "{" +
                "\"nombre\":\"" + escapeJson(nombre) + "\"," +
                "\"precio\":\"" + escapeJson(precio) + "\"," +
                "\"peso\":" + peso + "," +
                "\"categoria\":\"" + escapeJson(categoria) + "\"," +
                "\"socio\":\"" + escapeJson(socio) + "\"," +
                "\"observacion\":\"" + escapeJson(observacion) + "\"," +
                "\"tienePiedra\":" + tienePiedra + "," +
                "\"infoPiedra\":\"" + escapeJson(infoPiedra) + "\"," +
                "\"vendido\":" + vendido + "," +
                "\"estado\":\"" + escapeJson(estado) + "\"," +
                "\"fechaVendida\":\"" + escapeJson(fechaVendida == null ? "" : fechaVendida.toString()) + "\"," +
                "\"precioVenta\":\"" + escapeJson(precioVenta) + "\"" +
                "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void marcarJoyaNoAutorizada(Long id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Query q = em.createNativeQuery("UPDATE " + joyaTableName + " SET autorizado = FALSE WHERE id = ?1");
            q.setParameter(1, id);
            q.executeUpdate();
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

    // Listar categorias
    public List<Categoria> obtenerCategorias() {
        return categoriaController.findAll();
    }

    // Eliminar categoría por id
    public void eliminarCategoria(Long id) {
        categoriaController.delete(id);
    }

    // Crear socio evitando duplicados por nombre, etiquetado con la tienda actual
    public void agregarSocio(String nombre) {
        Socio existente = socioController.findByNombre(nombre);
        if (existente != null) {
            throw new IllegalArgumentException("El socio '" + nombre + "' ya existe.");
        }
        String tiendaTag = (tienda != null) ? tienda.name() : null;
        socioController.create(new Socio(nombre, tiendaTag));
    }

    // Listar socios filtrados por tienda (NULL = compartido, visible en ambas)
    public List<Socio> obtenerSocios() {
        if (tienda != null) {
            return socioController.findByTienda(tienda.name());
        }
        return socioController.findAll();
    }

    // Eliminar socio por id
    public void eliminarSocio(Long id) {
        socioController.delete(id);
    }
}
