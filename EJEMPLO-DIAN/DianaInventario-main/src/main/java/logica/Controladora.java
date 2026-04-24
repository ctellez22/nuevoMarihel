package logica;

import org.example.SessionContext;
import persistencia.ControladoraPersistencia;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class Controladora {

    private final ControladoraPersistencia persistencia;

    // Constructor
    public Controladora() {
        this.persistencia = new ControladoraPersistencia();
    }

    // Métodos de lógica que usan la persistencia


    // Agregar una nueva joya

    public Joya crearJoya(String nombre, String precio, double peso, String categoria, String socio, String observacion, Boolean tienePiedra, String infoPiedra) {

        Joya nuevaJoya = persistencia.agregarJoya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);

        // Imprimir la etiqueta
        Impresora impresora = new Impresora();
        // Obtener el ID de la joya recién creada (puedes ajustar esto según tu lógica)

        String idParaImprimir = (nuevaJoya.getDisplayId() != null && !nuevaJoya.getDisplayId().isBlank()) ? nuevaJoya.getDisplayId() : String.valueOf(nuevaJoya.getId());
        String zplData = generarZPLEtiqueta(idParaImprimir, precio, peso, tienePiedra, infoPiedra, categoria);
        impresora.imprimirEtiqueta(zplData);
        return nuevaJoya;
    }

    public Joya crearJoya(String nombre, String precio, double peso, String categoria, String socio, String observacion, Boolean tienePiedra, String infoPiedra, String puntoFisico) {

        Joya nuevaJoya = persistencia.agregarJoya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, puntoFisico);

        Impresora impresora = new Impresora();
        String idParaImprimir = (nuevaJoya.getDisplayId() != null && !nuevaJoya.getDisplayId().isBlank()) ? nuevaJoya.getDisplayId() : String.valueOf(nuevaJoya.getId());
        String zplData = generarZPLEtiqueta(idParaImprimir, precio, peso, tienePiedra, infoPiedra, categoria);
        impresora.imprimirEtiqueta(zplData);
        return nuevaJoya;
    }

    public Joya crearJoyaConAutorizacion(SessionContext session,
                                            String nombre,
                                            String precio,
                                            double peso,
                                            String categoria,
                                            String socio,
                                            String observacion,
                                            Boolean tienePiedra,
                                            String infoPiedra) {
        return crearJoyaConAutorizacion(session, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, null);
    }

    public Joya crearJoyaConAutorizacion(SessionContext session,
                                            String nombre,
                                            String precio,
                                            double peso,
                                            String categoria,
                                            String socio,
                                            String observacion,
                                            Boolean tienePiedra,
                                            String infoPiedra,
                                            String puntoFisicoAdmin) {
        String puntoFisico = (session != null && !session.isAdmin())
                ? session.puntoFisico()
                : puntoFisicoAdmin;
        if (session != null && !session.isAdmin()) {
            return persistencia.registrarPendienteCrearJoya(
                    session.userId(),
                    nombre,
                    precio,
                    peso,
                    categoria,
                    socio,
                    observacion,
                    Boolean.TRUE.equals(tienePiedra),
                    infoPiedra,
                    puntoFisico
            );
        }

        return crearJoya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, puntoFisico);
    }


    public void volverImprimir(String nombre, String precio, double peso, String categoria, String observacion, Boolean tienePiedra, String infoPiedra) {
        // Imprimir la etiqueta
        Impresora impresora = new Impresora();
        Joya nuevaJoya = persistencia.obtenerUltimaJoya();
        String idParaImprimir = (nuevaJoya.getDisplayId() != null && !nuevaJoya.getDisplayId().isBlank()) ? nuevaJoya.getDisplayId() : String.valueOf(nuevaJoya.getId());
        String zplData = generarZPLEtiqueta(idParaImprimir, precio, peso, tienePiedra, infoPiedra, categoria);
        impresora.imprimirEtiqueta(zplData);
    }

    public void imprimirEtiquetaPendiente(String precio, double peso, Boolean tienePiedra, String infoPiedra, String categoria) {
        Impresora impresora = new Impresora();
        String idTemporal = "PEND";
        String zplData = generarZPLEtiqueta(idTemporal, precio, peso, Boolean.TRUE.equals(tienePiedra), infoPiedra, categoria);
        impresora.imprimirEtiqueta(zplData);
    }

    public void reImprimirDespues(Long id, String nombre, String precio, double peso, String categoria, String observacion, Boolean tienePiedra, String infoPiedra) {
        // Imprimir la etiqueta
        Impresora impresora = new Impresora();
        //Joya nuevaJoya = persistencia.obtenerUltimaJoya();
        Joya j = persistencia.obtenerJoyaPorId(id);
        String idParaImprimir = (j != null && j.getDisplayId() != null && !j.getDisplayId().isBlank()) ? j.getDisplayId() : String.valueOf(id);
        String zplData = generarZPLEtiqueta(idParaImprimir, precio, peso, tienePiedra, infoPiedra, categoria);
        impresora.imprimirEtiqueta(zplData);
    }


    // Editar una joya existente
    public void actualizarJoya(Long id, String nombre, String precio, double peso, String categoria, String socio, String observacion, Boolean tienePiedra, String infoPiedra, Boolean vendido, LocalDateTime fechaVendida, String estado, String precioVentaReal) {
        if (peso < 0) {
            throw new IllegalArgumentException("El precio y el peso deben ser mayores o iguales a 0.");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        persistencia.editarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, precioVentaReal);
    }

    public boolean actualizarJoyaConAutorizacion(SessionContext session,
                                                 Long id,
                                                 String nombre,
                                                 String precio,
                                                 double peso,
                                                 String categoria,
                                                 String socio,
                                                 String observacion,
                                                 Boolean tienePiedra,
                                                 String infoPiedra,
                                                 Boolean vendido,
                                                 LocalDateTime fechaVendida,
                                                 String estado,
                                                 String precioVentaReal) {
        if (session != null && !session.isAdmin()) {
            persistencia.registrarPendienteActualizarJoya(
                    session.userId(),
                    id,
                    nombre,
                    precio,
                    peso,
                    categoria,
                    socio,
                    observacion,
                    Boolean.TRUE.equals(tienePiedra),
                    infoPiedra,
                    vendido,
                    fechaVendida,
                    estado,
                    precioVentaReal
            );
            return false;
        }

        actualizarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, precioVentaReal);
        return true;
    }

    public List<CambioPendiente> obtenerPendientesJoya() {
        return persistencia.listarPendientesJoya();
    }

    public void aprobarPendienteJoya(Long pendienteId, Long adminId) {
        persistencia.aprobarPendienteJoya(pendienteId, adminId);
    }

    public void rechazarPendienteJoya(Long pendienteId, Long adminId, String comentario) {
        persistencia.rechazarPendiente(pendienteId, adminId, comentario);
    }

    // Eliminar una joya por ID
    public void eliminarJoya(Long id) {
      if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }
        persistencia.eliminarJoya(id);
    }

    // Listar todas las joyas
    public List<Joya> obtenerTodasLasJoyas() {
        return persistencia.obtenerTodasLasJoyas();
    }

    public List<Joya> obtenerTodasLasJoyas(SessionContext session) {
        return persistencia.obtenerTodasLasJoyas(obtenerPuntoFisicoRestringido(session));
    }

    public List<Joya> obtenerVentasEntre(LocalDateTime inicioInclusive, LocalDateTime finExclusive) {
        return obtenerVentasEntre(inicioInclusive, finExclusive, null);
    }

    public List<Joya> obtenerVentasEntre(LocalDateTime inicioInclusive, LocalDateTime finExclusive, String puntoFisico) {
        if (inicioInclusive == null || finExclusive == null || !inicioInclusive.isBefore(finExclusive)) {
            throw new IllegalArgumentException("Rango de fechas invalido para consultar ventas.");
        }
        return persistencia.obtenerVentasEntre(inicioInclusive, finExclusive, puntoFisico);
    }
/*
    // Filtrar joyas según los criterios especificados
    public List<Joya> filtrarJoyass(boolean filterById,
                                   String id,
                                   boolean filterByCategory,
                                   String category,
                                   boolean filterByName,
                                   String name,
                                   boolean filterByNoVendido,

                                   String estado) {
        // Delegar la operación a la persistencia con los filtros apropiados
        return persistencia.filtrarJoyas(
                filterById ? id : null,
                filterByCategory ? category : null,
                filterByName ? name : null,
                filterByNoVendido ? true : null,
                filterByEstado ? estado : null
        );
    }
*/


    public List<Joya> filtrarJoyas(boolean filterById,
                                   String id,
                                   boolean filterByCategory,
                                   String category,
                                   boolean filterBySocio,
                                   String socio,
                                   boolean filterByName,
                                   String name,
                                   boolean filterByNoVendido,
                                   List<String> estado) {
        return persistencia.filtrarJoyas(
                filterById ? id : null,
                filterByCategory ? category : null,
                filterBySocio ? socio : null,
                filterByName ? name : null,
                filterByNoVendido ? true : null,
                (estado != null && !estado.isEmpty()) ? estado : null
        );
    }

    public List<Joya> filtrarJoyas(SessionContext session,
                                   boolean filterById,
                                   String id,
                                   boolean filterByCategory,
                                   String category,
                                   boolean filterBySocio,
                                   String socio,
                                   boolean filterByName,
                                   String name,
                                   boolean filterByNoVendido,
                                   List<String> estado) {
        return persistencia.filtrarJoyas(
                filterById ? id : null,
                filterByCategory ? category : null,
                filterBySocio ? socio : null,
                filterByName ? name : null,
                filterByNoVendido ? true : null,
                (estado != null && !estado.isEmpty()) ? estado : null,
                obtenerPuntoFisicoRestringido(session)
        );
    }


    // Marcar una joya como vendida
    public void marcarComoVendida(Long id, String precioVentaReal) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }

        Joya joya = persistencia.obtenerJoyaPorId(id); // Obtener la joya desde la base de datos
        if (joya == null) {
            throw new IllegalArgumentException("La joya con ID " + id + " no existe.");
        }

        if (joya.isVendido()) {
            throw new IllegalStateException("La joya ya está marcada como vendida.");
        }

        joya.setVendido(true); // Actualizar el atributo "vendido" de la joya
        joya.setFechaVendida(LocalDateTime.now());
        joya.setPrecioVentaReal(precioVentaReal);
        persistencia.editarJoya(joya.getId(), joya.getNombre(), joya.getPrecio(), joya.getPeso(), joya.getCategoria(), joya.getSocio(), joya.getObservacion(), joya.isTienePiedra(), joya.getInfoPiedra(), joya.isVendido(), joya.getFechaVendida(), joya.getEstado(), joya.getPrecioVentaReal());
    }

    public boolean marcarComoVendidaConAutorizacion(SessionContext session, Long id, String precioVentaReal) {
        if (session != null && !session.isAdmin()) {
            persistencia.registrarPendienteMarcarVendida(session.userId(), id, precioVentaReal);
            return false;
        }
        marcarComoVendida(id, precioVentaReal);
        return true;
    }

    // Obtener una joya por ID
    public Joya obtenerJoyaPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }
        return persistencia.obtenerJoyaPorId(id);
    }
    //Actualizar fecha verificacion
    public void actualizarFechaVerificacionCategoria(String categoria, LocalDateTime fecha) {
        persistencia.actualizarFechaVerificacionCategoria(categoria, fecha);
    }

    // Obtener la lista de categorías ordenadas por fecha de verificación (de la más antigua a la más reciente)
    public List<CategoriaVerificacion> obtenerCategoriasOrdenadasPorVerificacion() {
        return persistencia.obtenerCategoriasOrdenadasPorFecha();
    }

    // Crear categoria
    public void crearCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        persistencia.agregarCategoria(nombre.trim());
    }

    // Listar categorias
    public List<Categoria> obtenerCategorias() {
        return persistencia.obtenerCategorias();
    }

    // Eliminar categoria
    public void eliminarCategoria(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una categoría válida.");
        }
        persistencia.eliminarCategoria(id);
    }

    // Crear socio
    public void crearSocio(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del socio no puede estar vacío.");
        }
        persistencia.agregarSocio(nombre.trim());
    }

    // Listar socios
    public List<Socio> obtenerSocios() {
        return persistencia.obtenerSocios();
    }

    public List<Socio> obtenerSocios(SessionContext session) {
        return persistencia.obtenerSocios();
    }

    public void crearJoyero(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del joyero no puede estar vacío.");
        }
        persistencia.agregarJoyero(nombre.trim());
    }

    public List<Joyero> obtenerJoyeros() {
        return persistencia.obtenerJoyeros();
    }

    public List<Joyero> obtenerJoyeros(SessionContext session) {
        return persistencia.obtenerJoyeros();
    }

    public void eliminarJoyero(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un joyero válido.");
        }
        persistencia.eliminarJoyero(id);
    }

    // Eliminar socio
    public void eliminarSocio(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un socio válido.");
        }
        persistencia.eliminarSocio(id);
    }

    // ============ MÉTODOS PARA LOTE ============

    // Crear un lote
    public void crearLote(String nombre, double pesoTotal, int cantidadPiedras, String tipoPiedra,
                          String calidadPiedra, String descripcion, String precioEstimado,
                          String socio, String categoria, String observaciones) {
        persistencia.agregarLote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones);
    }

    public void crearLote(String nombre, double pesoTotal, int cantidadPiedras, String tipoPiedra,
                          String calidadPiedra, String descripcion, String precioEstimado,
                          String socio, String categoria, String observaciones, String puntoFisico) {
        persistencia.agregarLote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones, puntoFisico);
    }

    // Crear un lote con autorización (para usuarios no-admin)
    public boolean crearLoteConAutorizacion(SessionContext session, String nombre, double pesoTotal,
                                            int cantidadPiedras, String tipoPiedra, String calidadPiedra,
                                            String descripcion, String precioEstimado, String socio,
                                            String categoria, String observaciones) {
        return crearLoteConAutorizacion(session, nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones, null);
    }

    public boolean crearLoteConAutorizacion(SessionContext session, String nombre, double pesoTotal,
                                            int cantidadPiedras, String tipoPiedra, String calidadPiedra,
                                            String descripcion, String precioEstimado, String socio,
                                            String categoria, String observaciones, String puntoFisicoAdmin) {
        String puntoFisico = (session != null && !session.isAdmin())
                ? session.puntoFisico()
                : puntoFisicoAdmin;
        if (session != null && !session.isAdmin()) {
            persistencia.registrarPendienteCrearLote(session.userId(), nombre, pesoTotal,
                    cantidadPiedras, tipoPiedra, calidadPiedra, descripcion, precioEstimado,
                    socio, categoria, observaciones, puntoFisico);
            return false;
        }

        crearLote(nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, socio, categoria, observaciones, puntoFisico);
        return true;
    }

    // Actualizar un lote (admin: directo; empleado: aplica en BD y deja pendiente de aprobación)
    public void actualizarLote(SessionContext session, Long id, String nombre, double pesoTotal,
                               int cantidadPiedras, String tipoPiedra, String calidadPiedra,
                               String descripcion, String precioEstimado, String precioVentaReal,
                               String socio, String categoria, String estado, LocalDateTime fechaVenta,
                               String observaciones) {
        if (pesoTotal <= 0 || cantidadPiedras <= 0) {
            throw new IllegalArgumentException("El peso y cantidad deben ser mayores a 0.");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (session != null && !session.isAdmin()) {
            persistencia.registrarPendienteActualizarLote(session.userId(), id, nombre, pesoTotal,
                    cantidadPiedras, tipoPiedra, calidadPiedra, descripcion, precioEstimado,
                    socio, categoria, observaciones);
        } else {
            persistencia.editarLote(id, nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                    descripcion, precioEstimado, precioVentaReal, socio, categoria, estado, fechaVenta, observaciones);
        }
    }

    // Actualizar un lote (sin sesión, para uso interno del sistema)
    public void actualizarLote(Long id, String nombre, double pesoTotal, int cantidadPiedras,
                               String tipoPiedra, String calidadPiedra, String descripcion,
                               String precioEstimado, String precioVentaReal, String socio,
                               String categoria, String estado, LocalDateTime fechaVenta,
                               String observaciones) {
        if (pesoTotal <= 0 || cantidadPiedras <= 0) {
            throw new IllegalArgumentException("El peso y cantidad deben ser mayores a 0.");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        persistencia.editarLote(id, nombre, pesoTotal, cantidadPiedras, tipoPiedra, calidadPiedra,
                descripcion, precioEstimado, precioVentaReal, socio, categoria, estado, fechaVenta, observaciones);
    }

    // Eliminar un lote
    public void eliminarLote(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }
        persistencia.eliminarLote(id);
    }

    // Obtener un lote por ID
    public Lote obtenerLotePorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }
        return persistencia.obtenerLotePorId(id);
    }

    // Obtener todos los lotes
    public List<Lote> obtenerTodosLosLotes() {
        return persistencia.obtenerTodosLosLotes();
    }

    public List<Lote> obtenerTodosLosLotes(SessionContext session) {
        return persistencia.obtenerTodosLosLotes(obtenerPuntoFisicoRestringido(session));
    }

    public OrdenTrabajo crearOrdenTrabajo(SessionContext session,
                                          Long joyaId,
                                          String joyero,
                                          LocalDateTime fechaEnvio,
                                          LocalDateTime fechaEntrega,
                                          String detalle) {
        if (joyaId == null || joyaId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una joya válida.");
        }
        if (joyero == null || joyero.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el joyero responsable.");
        }
        if (fechaEnvio == null || fechaEntrega == null) {
            throw new IllegalArgumentException("Debe indicar fechas de envío y entrega.");
        }
        if (fechaEntrega.isBefore(fechaEnvio)) {
            throw new IllegalArgumentException("La fecha de entrega no puede ser anterior a la fecha de envío.");
        }

        Joya joya = persistencia.obtenerJoyaPorId(joyaId);
        if (joya == null) {
            throw new IllegalArgumentException("La joya seleccionada no existe.");
        }
        if (joya.isVendido()) {
            throw new IllegalArgumentException("No se puede crear una orden para una joya vendida.");
        }

        String puntoRestringido = obtenerPuntoFisicoRestringido(session);
        if (puntoRestringido != null) {
            String puntoJoya = joya.getPuntoFisico() == null ? "" : joya.getPuntoFisico().trim();
            if (!puntoRestringido.equalsIgnoreCase(puntoJoya)) {
                throw new IllegalArgumentException("No tiene permiso para crear ordenes sobre joyas de otro punto físico.");
            }
        }

        return persistencia.crearOrdenTrabajo(joyaId, joyero.trim(), fechaEnvio, fechaEntrega, detalle == null ? "" : detalle.trim());
    }

    public List<OrdenTrabajo> obtenerOrdenesTrabajo(SessionContext session) {
        return persistencia.obtenerOrdenesTrabajo(obtenerPuntoFisicoRestringido(session));
    }

    public void completarOrdenTrabajo(Long ordenId) {
        if (ordenId == null || ordenId <= 0) {
            throw new IllegalArgumentException("ID de orden inválido.");
        }
        persistencia.completarOrdenTrabajo(ordenId);
    }

    public void descontarPesoLote(Long loteId, double pesoADescontar) {
        if (loteId == null || loteId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un lote valido para descontar peso.");
        }
        if (pesoADescontar <= 0) {
            throw new IllegalArgumentException("El peso a descontar debe ser mayor que cero.");
        }
        persistencia.descontarPesoLote(loteId, pesoADescontar);
    }

    private String obtenerPuntoFisicoRestringido(SessionContext session) {
        if (session == null || session.isAdmin()) {
            return null;
        }
        if (session.puntoFisico() == null || session.puntoFisico().isBlank()) {
            return "__SIN_PUNTO_ASIGNADO__";
        }
        return session.puntoFisico().trim();
    }

    // Marcar un lote como vendido
    public void marcarLoteComoVendido(Long id, String precioVentaReal) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }

        Lote lote = persistencia.obtenerLotePorId(id);
        if (lote == null) {
            throw new IllegalArgumentException("El lote con ID " + id + " no existe.");
        }

        lote.setVendido(true);
        lote.setFechaVenta(LocalDateTime.now());
        lote.setPrecioVentaReal(precioVentaReal);
        lote.setEstado("vendido");
        persistencia.editarLote(lote.getId(), lote.getNombre(), lote.getPesoTotal(),
                lote.getCantidadPiedras(), lote.getTipoPiedra(), lote.getCalidadPiedra(),
                lote.getDescripcion(), lote.getPrecioEstimado(), lote.getPrecioVentaReal(),
                lote.getSocio(), lote.getCategoria(), lote.getEstado(), lote.getFechaVenta(),
                lote.getObservaciones());
    }

    // GENERAR ZPL DE ETIQUETA
    public String generarZPLEtiqueta(String idDisplayOrId, String precio, double peso, boolean tienePiedra, String infoPiedra, String categoria) {
        String id = limpiarCampoZpl(idDisplayOrId);
        String precioLimpio = limpiarCampoZpl(precio);
        String pesoLimpio = formatearPesoEtiqueta(peso);
        String piedraLimpia = compactarInfoPiedraEtiqueta(infoPiedra, categoria);
        if (piedraLimpia.isBlank()) {
            piedraLimpia = limpiarCampoZpl(categoria);
        }

        if (tienePiedra) {
            return "^XA\n" +
                    "^PW984\n" +
                    "^LL102\n" +
                    "^FO180,28^A0N,20,20^FD  " + precioLimpio + " ^FS\n" +
                    "^FO180,60^A0N,17,17^FD " + pesoLimpio + "/" + piedraLimpia + " ^FS\n" +
                    "^FO350,15^BY1,3,50^BCN,50,N,N^FD" + id + "^FS\n" +
                    "^FO365,70^A0N,19,19^FD" + id + "^FS\n" +
                    "^XZ";
        } else {
            return "^XA\n" +
                    "^PW984\n" +
                    "^LL102\n" +
                    "^FO180,28^A0N,24,24^FD  " + pesoLimpio + " ^FS\n" +
                    "^FO180,58^A0N,19,19^FD" + precioLimpio + " ^FS\n" +
                    "^FO350,15^BY1,3,50^BCN,50,N,N^FD" + id + "^FS\n" +
                    "^FO365,70^A0N,19,19^FD" + id + "^FS\n" +
                    "^XZ";
        }
    }

    private String compactarInfoPiedraEtiqueta(String infoPiedra, String categoria) {
        String fuente = limpiarCampoZpl(infoPiedra);
        if (fuente.isBlank()) {
            return limpiarCampoZpl(categoria);
        }

        String categoriaFallback = categoria == null ? "" : categoria.trim();
        String[] registros = fuente.split("\\|");
        StringBuilder compacto = new StringBuilder();

        for (String registroRaw : registros) {
            String registro = registroRaw == null ? "" : registroRaw.trim();
            if (registro.isBlank()) {
                continue;
            }

            String tipo = extraerValorPiedra(registro, "tipo");
            if (tipo.isBlank()) {
                String loteId = extraerLoteIdPiedra(registro);
                tipo = obtenerTipoPiedraDesdeLote(loteId);
            }
            if (tipo.isBlank()) {
                tipo = extraerValorPiedra(registro, "lote");
            }
            if (tipo.isBlank()) {
                tipo = categoriaFallback;
            }

            String quilates = extraerValorPiedra(registro, "peso");
            String segmento = abreviarTipoPiedra(tipo);
            if (!quilates.isBlank()) {
                segmento = segmento + quilates;
            }

            if (!segmento.isBlank()) {
                if (!compacto.isEmpty()) {
                    compacto.append('/');
                }
                compacto.append(segmento);
            }
        }

        if (!compacto.isEmpty()) {
            return compacto.toString();
        }
        return limpiarCampoZpl(categoria);
    }

    private String extraerLoteIdPiedra(String registroPiedra) {
        String loteId = extraerValorPiedra(registroPiedra, "loteId");
        if (!loteId.isBlank()) {
            return loteId;
        }
        // Compatibilidad con registros históricos/mal formados: "lotedId"
        return extraerValorPiedra(registroPiedra, "lotedId");
    }

    private String obtenerTipoPiedraDesdeLote(String loteIdTexto) {
        if (loteIdTexto == null || loteIdTexto.isBlank()) {
            return "";
        }
        try {
            long loteId = Long.parseLong(loteIdTexto.trim());
            if (loteId <= 0) {
                return "";
            }
            Lote lote = persistencia.obtenerLotePorId(loteId);
            if (lote == null || lote.getTipoPiedra() == null) {
                return "";
            }
            return lote.getTipoPiedra().trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String extraerValorPiedra(String texto, String llave) {
        if (texto == null || texto.isBlank() || llave == null || llave.isBlank()) {
            return "";
        }
        String patron = llave + "=";
        int inicio = texto.indexOf(patron);
        if (inicio < 0) {
            return "";
        }
        int valorInicio = inicio + patron.length();
        int valorFin = texto.indexOf(',', valorInicio);
        int cierre = texto.indexOf(')', valorInicio);
        if (valorFin < 0 || (cierre >= 0 && cierre < valorFin)) {
            valorFin = cierre;
        }
        if (valorFin < 0) {
            valorFin = texto.length();
        }
        return texto.substring(valorInicio, valorFin).trim();
    }

    private String abreviarTipoPiedra(String valor) {
        String base = valor == null ? "" : valor.trim();
        if (base.isBlank()) {
            return "pie";
        }

        String normalizado = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        if (normalizado.contains("esmer")) {
            return "esm";
        }
        if (normalizado.contains("diam")) {
            return "dia";
        }
        if (normalizado.contains("rubi")) {
            return "rub";
        }
        if (normalizado.contains("zafir")) {
            return "zaf";
        }
        if (normalizado.contains("topa")) {
            return "top";
        }
        if (normalizado.contains("amat")) {
            return "ama";
        }

        String soloLetras = normalizado.replaceAll("[^a-z0-9]", "");
        if (soloLetras.isBlank()) {
            return "pie";
        }
        return soloLetras.length() <= 3 ? soloLetras : soloLetras.substring(0, 3);
    }


    private String limpiarCampoZpl(String valor) {
        if (valor == null) {
            return "";
        }
        // Evita que datos del usuario rompan comandos ZPL.
        return valor
                .replace("^", " ")
                .replace("~", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }


    private String formatearPesoEtiqueta(double peso) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("0.##", symbols);
        return formatter.format(peso);
    }
}
