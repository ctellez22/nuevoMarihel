package persistencia;


import logica.CambioPendiente;
import logica.Categoria;
import logica.CategoriaVerificacion;
import logica.Joyero;
import logica.Joya;
import logica.Lote;
import logica.OrdenTrabajo;
import logica.Socio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.List;

public class ControladoraPersistencia {

    private JoyaJpaController joyaController;
    private CategoriaVerificacionJpaController catVerifController;
    private CategoriaJpaController categoriaController;
    private SocioJpaController socioController;
    private JoyeroJpaController joyeroController;
    private CambioPendienteRepository cambioPendienteRepository;
    private LoteJpaController loteController;
    private OrdenTrabajoJpaController ordenTrabajoController;

    // Constructor
    public ControladoraPersistencia() {
        this.joyaController = new JoyaJpaController();
        this.catVerifController = new CategoriaVerificacionJpaController();
        this.categoriaController = new CategoriaJpaController();
        this.socioController = new SocioJpaController();
        this.joyeroController = new JoyeroJpaController();
        this.cambioPendienteRepository = new CambioPendienteRepository();
        this.loteController = new LoteJpaController();
        this.ordenTrabajoController = new OrdenTrabajoJpaController();
    }

    // Métodos para interactuar con JoyaJpaController

    // Crear una nueva Joya
    public Joya agregarJoya(String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra) {
        return agregarJoya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, null);
    }

    public Joya agregarJoya(String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra, String puntoFisico) {
        validarDisponibilidadPiedrasEnLotes(infoPiedra);
        Joya nuevaJoya = new Joya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);
        nuevaJoya.setPuntoFisico(puntoFisico);
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            descontarPesoLotes(em, infoPiedra);
            em.persist(nuevaJoya);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
        System.out.println("Joya agregada: " + nuevaJoya);
        return nuevaJoya;
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
        return registrarPendienteCrearJoya(solicitadoPor, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, null);
    }

    public Joya registrarPendienteCrearJoya(Long solicitadoPor,
                                            String nombre,
                                            String precio,
                                            double peso,
                                            String categoria,
                                            String socio,
                                            String observacion,
                                            boolean tienePiedra,
                                            String infoPiedra,
                                            String puntoFisico) {
        validarDisponibilidadPiedrasEnLotes(infoPiedra);
        Joya nuevaJoya = new Joya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);
        nuevaJoya.setPuntoFisico(puntoFisico);
        nuevaJoya.setAutorizado(false);

        // Descontar el peso del lote inmediatamente (igual que el admin),
        // para que el inventario refleje el uso desde el momento de la creación.
        // Si la joya es rechazada, el peso se devuelve al lote.
        EntityManager emCreacion = PersistenceManager.createEntityManager();
        try {
            emCreacion.getTransaction().begin();
            descontarPesoLotes(emCreacion, infoPiedra);
            emCreacion.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (emCreacion.getTransaction().isActive()) {
                emCreacion.getTransaction().rollback();
            }
            throw ex;
        } finally {
            emCreacion.close();
        }

        joyaController.create(nuevaJoya);

        String afterJson = jsonJoya(
                nuevaJoya.getId(),
                nuevaJoya.getDisplayId(),
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
                nuevaJoya.getPrecioVentaReal(),
                nuevaJoya.getPuntoFisico()
        );
        cambioPendienteRepository.crearPendiente("joya", nuevaJoya.getId(), "INSERT", null, afterJson, solicitadoPor);
        return nuevaJoya;
    }

    // Editar una Joya existente
    public void editarJoya(Long id, String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra, Boolean vendido, LocalDateTime fechaVendida, String estado) {
        editarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, null);
    }

    public void editarJoya(Long id, String nombre, String precio, double peso, String categoria, String socio, String observacion, boolean tienePiedra, String infoPiedra, Boolean vendido, LocalDateTime fechaVendida, String estado, String precioVentaReal) {
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
            joya.setPrecioVentaReal(precioVentaReal);
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
                                                  String estado) {
        registrarPendienteActualizarJoya(
                solicitadoPor,
                id,
                nombre,
                precio,
                peso,
                categoria,
                socio,
                observacion,
                tienePiedra,
                infoPiedra,
                vendido,
                fechaVendida,
                estado,
                null
        );
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
                                                  String precioVentaReal) {
        Joya actual = joyaController.find(id);
        if (actual == null) {
            throw new IllegalArgumentException("Joya con ID " + id + " no encontrada.");
        }

        String beforeJson = jsonJoya(
                actual.getId(),
                actual.getDisplayId(),
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
                actual.getPrecioVentaReal(),
                actual.getPuntoFisico()
        );

        String afterJson = jsonJoya(
                actual.getId(),
                actual.getDisplayId(),
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
                precioVentaReal,
                actual.getPuntoFisico()
        );

        editarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, precioVentaReal);
        marcarJoyaNoAutorizada(id);

        cambioPendienteRepository.crearPendiente("joya", id, "UPDATE", beforeJson, afterJson, solicitadoPor);
    }

    public void registrarPendienteMarcarVendida(Long solicitadoPor, Long id) {
        registrarPendienteMarcarVendida(solicitadoPor, id, null);
    }

    public void registrarPendienteMarcarVendida(Long solicitadoPor, Long id, String precioVentaReal) {
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
                precioVentaReal
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

    public List<Joya> obtenerTodasLasJoyas(String puntoFisico) {
        return filtrarPorPuntoFisicoJoyas(joyaController.findAll(), puntoFisico);
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

    public List<Joya> filtrarJoyas(String id, String categoria, String socio, String nombre, Boolean noVendido, List<String> estado, String puntoFisico) {
        return joyaController.filtrarJoyas(id, categoria, socio, nombre, noVendido, estado, puntoFisico);
    }
    //Obtener  la ultima joya
    public Joya obtenerUltimaJoya() {
        return joyaController.obtenerUltimaJoya();
    }

    public List<Joya> obtenerVentasEntre(LocalDateTime inicioInclusive, LocalDateTime finExclusive) {
        return obtenerVentasEntre(inicioInclusive, finExclusive, null);
    }

    public List<Joya> obtenerVentasEntre(LocalDateTime inicioInclusive, LocalDateTime finExclusive, String puntoFisico) {
        List<Joya> ventas = joyaController.obtenerVentasEntre(inicioInclusive, finExclusive);
        if (puntoFisico == null || puntoFisico.isBlank()) {
            return ventas;
        }
        String filtro = puntoFisico.trim();
        return ventas.stream()
                .filter(j -> j.getPuntoFisico() != null && j.getPuntoFisico().trim().equalsIgnoreCase(filtro))
                .toList();
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

    private String jsonJoya(Long id,
                            String displayId,
                            String nombre,
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
                            String precioVentaReal,
                            String puntoFisico) {
        return "{" +
                "\"id\":" + (id == null ? "null" : id) + "," +
                "\"displayId\":\"" + escapeJson(displayId) + "\"," +
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
                "\"precioVentaReal\":\"" + escapeJson(precioVentaReal) + "\"," +
                "\"puntoFisico\":\"" + escapeJson(puntoFisico) + "\"" +
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
            Query q = em.createNativeQuery("UPDATE joya SET autorizado = FALSE WHERE id = ?1");
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

    // Crear socio evitando duplicados por nombre
    public void agregarSocio(String nombre) {
        Socio existente = socioController.findByNombre(nombre);
        if (existente != null) {
            throw new IllegalArgumentException("El socio '" + nombre + "' ya existe.");
        }
        socioController.create(new Socio(nombre));
    }

    // Listar socios
    public List<Socio> obtenerSocios() {
        return socioController.findAll();
    }

    public void agregarJoyero(String nombre) {
        Joyero existente = joyeroController.findByNombre(nombre);
        if (existente != null) {
            throw new IllegalArgumentException("El joyero '" + nombre + "' ya existe.");
        }
        joyeroController.create(new Joyero(nombre));
    }

    public List<Joyero> obtenerJoyeros() {
        return joyeroController.findAll();
    }

    public void eliminarJoyero(Long id) {
        joyeroController.delete(id);
    }


    // Eliminar socio por id
    public void eliminarSocio(Long id) {
        socioController.delete(id);
    }

    // ============ MÉTODOS PARA LOTE ============

    // Crear un nuevo Lote
    public void agregarLote(String nombre, double pesoTotal, int cantidadPiedras, String tipoPiedra,
                            String calidadPiedra, String descripcion, String precioEstimado,
                            String socio, String categoria, String observaciones) {
        agregarLote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra, descripcion, precioEstimado,
                socio, categoria, observaciones, null);
    }

    public void agregarLote(String nombre, double pesoTotal, int cantidadPiedras, String tipoPiedra,
                            String calidadPiedra, String descripcion, String precioEstimado,
                            String socio, String categoria, String observaciones, String puntoFisico) {
        Lote nuevoLote = new Lote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones);
        nuevoLote.setPuntoFisico(puntoFisico);
        loteController.create(nuevoLote);
        System.out.println("Lote agregado: " + nuevoLote);
    }

    // Registrar pendiente de crear lote
    public void registrarPendienteCrearLote(Long solicitadoPor, String nombre, double pesoTotal,
                                            int cantidadPiedras, String tipoPiedra, String calidadPiedra,
                                            String descripcion, String precioEstimado, String socio,
                                            String categoria, String observaciones) {
        registrarPendienteCrearLote(solicitadoPor, nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones, null);
    }

    public void registrarPendienteCrearLote(Long solicitadoPor, String nombre, double pesoTotal,
                                            int cantidadPiedras, String tipoPiedra, String calidadPiedra,
                                            String descripcion, String precioEstimado, String socio,
                                            String categoria, String observaciones, String puntoFisico) {
        Lote nuevoLote = new Lote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones);
        nuevoLote.setPuntoFisico(puntoFisico);
        nuevoLote.setAutorizado(false);
        loteController.create(nuevoLote);

        String afterJson = jsonLote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones, puntoFisico);
        cambioPendienteRepository.crearPendiente("lote", nuevoLote.getId(), "INSERT", null, afterJson, solicitadoPor);
    }

    // Registrar pendiente de actualizar lote (aplica el cambio inmediatamente y deja registro para revertir si se rechaza)
    public void registrarPendienteActualizarLote(Long solicitadoPor, Long id, String nombre,
                                                 double pesoTotal, int cantidadPiedras,
                                                 String tipoPiedra, String calidadPiedra,
                                                 String descripcion, String precioEstimado,
                                                 String socio, String categoria, String observaciones) {
        Lote actual = loteController.find(id);
        if (actual == null) {
            throw new IllegalArgumentException("No se encontro el lote con ID " + id);
        }
        String beforeJson = jsonLote(actual.getNombre(), actual.getPesoTotal(), actual.getCantidadPiedras(),
                actual.getTipoPiedra(), actual.getCalidadPiedra(), actual.getDescripcion(),
                actual.getPrecioEstimado(), actual.getSocio(), actual.getCategoria(),
                actual.getObservaciones(), actual.getPuntoFisico());
        String afterJson = jsonLote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones, actual.getPuntoFisico());

        editarLote(id, nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, actual.getPrecioVentaReal(), socio, categoria,
                actual.getEstado(), actual.getFechaVenta(), observaciones);

        cambioPendienteRepository.crearPendiente("lote", id, "UPDATE", beforeJson, afterJson, solicitadoPor);
    }

    // Editar un lote existente
    public void editarLote(Long id, String nombre, double pesoTotal, int cantidadPiedras,
                           String tipoPiedra, String calidadPiedra, String descripcion,
                           String precioEstimado, String precioVentaReal, String socio,
                           String categoria, String estado, LocalDateTime fechaVenta, String observaciones) {
        Lote lote = loteController.find(id);
        if (lote != null) {
            lote.setNombre(nombre);
            lote.setPesoTotal(pesoTotal);
            lote.setCantidadPiedras(cantidadPiedras);
            lote.setTipoPiedra(tipoPiedra);
            lote.setCalidadPiedra(calidadPiedra);
            lote.setDescripcion(descripcion);
            lote.setPrecioEstimado(precioEstimado);
            lote.setPrecioVentaReal(precioVentaReal);
            lote.setSocio(socio);
            lote.setCategoria(categoria);
            lote.setEstado(estado);
            lote.setFechaVenta(fechaVenta);
            lote.setObservaciones(observaciones);
            lote.setActualizadoEn(LocalDateTime.now());
            loteController.edit(lote);
            System.out.println("Lote editado: " + lote);
        } else {
            System.out.println("Lote con ID " + id + " no encontrado.");
        }
    }

    // Eliminar un lote
    public void eliminarLote(Long id) {
        loteController.delete(id);
        System.out.println("Lote con ID " + id + " eliminado.");
    }

    // Obtener un lote por ID
    public Lote obtenerLotePorId(Long id) {
        return loteController.find(id);
    }

    // Obtener todos los lotes
    public List<Lote> obtenerTodosLosLotes() {
        return loteController.findAll();
    }

    public List<Lote> obtenerTodosLosLotes(String puntoFisico) {
        return filtrarPorPuntoFisicoLotes(loteController.findAll(), puntoFisico);
    }

    public OrdenTrabajo crearOrdenTrabajo(Long joyaId,
                                          String joyero,
                                          LocalDateTime fechaEnvio,
                                          LocalDateTime fechaEntrega,
                                          String detalle) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            Joya joya = em.find(Joya.class, joyaId);
            if (joya == null) {
                throw new IllegalArgumentException("No se encontró la joya seleccionada para la orden de trabajo.");
            }

            joya.setEstado("pendiente");

            OrdenTrabajo orden = new OrdenTrabajo();
            orden.setJoyaId(joya.getId());
            orden.setJoyaDisplayId(joya.getDisplayId());
            orden.setJoyaNombre(joya.getNombre());
            orden.setJoyero(joyero);
            orden.setFechaEnvio(fechaEnvio);
            orden.setFechaEntrega(fechaEntrega);
            orden.setDetalle(detalle);
            orden.setEstado("pendiente");
            String puntoJoya = joya.getPuntoFisico() == null ? "" : joya.getPuntoFisico().trim();
            orden.setPuntoFisico(puntoJoya);

            em.persist(orden);
            em.merge(joya);

            em.getTransaction().commit();
            return orden;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<OrdenTrabajo> obtenerOrdenesTrabajo() {
        return ordenTrabajoController.findAllOrderedByNewest();
    }

    public List<OrdenTrabajo> obtenerOrdenesTrabajo(String puntoFisico) {
        if (puntoFisico == null) {
            return ordenTrabajoController.findAllOrderedByNewest();
        }
        if (puntoFisico.isBlank()) {
            return List.of();
        }
        return ordenTrabajoController.findByPuntoFisico(puntoFisico);
    }

    public void completarOrdenTrabajo(Long ordenId) {
        ordenTrabajoController.completarOrden(ordenId);
    }

    public void descontarPesoLote(Long loteId, double pesoADescontar) {
        Lote lote = loteController.find(loteId);
        if (lote == null) {
            throw new IllegalArgumentException("No se encontro el lote seleccionado para descontar piedra.");
        }
        if (pesoADescontar <= 0) {
            throw new IllegalArgumentException("El peso a descontar debe ser mayor a 0.");
        }

        double nuevoPeso = lote.getPesoTotal() - pesoADescontar;
        if (nuevoPeso < 0) {
            throw new IllegalArgumentException("El lote '" + lote.getNombre() + "' no tiene peso suficiente. Disponible: " + lote.getPesoTotal());
        }

        lote.setPesoTotal(nuevoPeso);
        if (nuevoPeso == 0.0d) {
            lote.setEstado("agotado");
        }
        lote.setActualizadoEn(LocalDateTime.now());
        loteController.edit(lote);
    }

    public void validarDisponibilidadPiedrasEnLotes(String infoPiedra) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            validarDisponibilidadPiedrasEnLotes(em, infoPiedra);
        } finally {
            em.close();
        }
    }

    // Obtener lotes por estado
    public List<Lote> obtenerLotesPorEstado(String estado) {
        return loteController.findByEstado(estado);
    }

    // Obtener último lote creado
    public Lote obtenerUltimoLote() {
        return loteController.obtenerUltimoLote();
    }

    // Método auxiliar para convertir lote a JSON
    private String jsonLote(String nombre, double pesoTotal, int cantidadPiedras, String tipoPiedra,
                            String calidadPiedra, String descripcion, String precioEstimado,
                            String socio, String categoria, String observaciones, String puntoFisico) {
        // Usar Double.toString evita separador decimal por locale (coma) y mantiene JSON válido.
        return "{" +
                "\"nombre\":\"" + escapeJson(nombre) + "\"," +
                "\"pesoTotal\":" + Double.toString(pesoTotal) + "," +
                "\"cantidadPiedras\":" + cantidadPiedras + "," +
                "\"tipoPiedra\":\"" + escapeJson(tipoPiedra) + "\"," +
                "\"calidadPiedra\":\"" + escapeJson(calidadPiedra) + "\"," +
                "\"descripcion\":\"" + escapeJson(descripcion) + "\"," +
                "\"precioEstimado\":\"" + escapeJson(precioEstimado) + "\"," +
                "\"socio\":\"" + escapeJson(socio) + "\"," +
                "\"categoria\":\"" + escapeJson(categoria) + "\"," +
                "\"observaciones\":\"" + escapeJson(observaciones) + "\"," +
                "\"puntoFisico\":\"" + escapeJson(puntoFisico == null ? "" : puntoFisico) + "\"" +
                "}";
    }

    private void descontarPesoLotes(EntityManager em, String infoPiedra) {
        for (var entry : PiedraLoteParser.extraerPesoPorLote(infoPiedra).entrySet()) {
            Long loteId = entry.getKey();
            double pesoADescontar = entry.getValue();
            Lote lote = em.find(Lote.class, loteId);
            if (lote == null) {
                throw new IllegalArgumentException("No se encontro el lote seleccionado para descontar piedra.");
            }

            double nuevoPeso = lote.getPesoTotal() - pesoADescontar;
            if (nuevoPeso < 0) {
                throw new IllegalArgumentException("El lote '" + lote.getNombre() + "' no tiene peso suficiente. Disponible: " + lote.getPesoTotal());
            }

            lote.setPesoTotal(nuevoPeso);
            lote.setEstado(nuevoPeso == 0.0d ? "agotado" : lote.getEstado());
            lote.setActualizadoEn(LocalDateTime.now());
        }
    }

    private void validarDisponibilidadPiedrasEnLotes(EntityManager em, String infoPiedra) {
        for (var entry : PiedraLoteParser.extraerPesoPorLote(infoPiedra).entrySet()) {
            Lote lote = em.find(Lote.class, entry.getKey());
            if (lote == null) {
                throw new IllegalArgumentException("No se encontro el lote seleccionado para la piedra.");
            }
            if (entry.getValue() <= 0) {
                throw new IllegalArgumentException("El peso de la piedra debe ser mayor a 0.");
            }
            if (lote.getPesoTotal() < entry.getValue()) {
                throw new IllegalArgumentException("El lote '" + lote.getNombre() + "' no tiene peso suficiente. Disponible: " + lote.getPesoTotal());
            }
        }
    }

    private List<Joya> filtrarPorPuntoFisicoJoyas(List<Joya> joyas, String puntoFisico) {
        if (puntoFisico == null) {
            return joyas;
        }
        if (puntoFisico.isBlank()) {
            return List.of();
        }
        return joyas.stream()
                .filter(j -> puntoFisico.equalsIgnoreCase(j.getPuntoFisico()))
                .toList();
    }

    private List<Lote> filtrarPorPuntoFisicoLotes(List<Lote> lotes, String puntoFisico) {
        if (puntoFisico == null) {
            return lotes;
        }
        if (puntoFisico.isBlank()) {
            return List.of();
        }
        return lotes.stream()
                .filter(l -> puntoFisico.equalsIgnoreCase(l.getPuntoFisico()))
                .toList();
    }
}
