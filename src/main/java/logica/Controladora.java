package logica;

import org.example.SessionContext;
import persistencia.ControladoraPersistencia;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class Controladora {

    private final ControladoraPersistencia persistencia;

    // Constructor sin sesión (Marihel por defecto)
    public Controladora() {
        this.persistencia = new ControladoraPersistencia();
    }

    // Constructor con sesión: usa la tienda seleccionada
    public Controladora(SessionContext session) {
        this.persistencia = new ControladoraPersistencia(session != null ? session.tienda() : null);
    }

    // Métodos de lógica que usan la persistencia


    // Agregar una nueva joya

    public void crearJoya(String nombre, String precio, double peso, String categoria, String socio, String observacion, Boolean tienePiedra, String infoPiedra) {

        persistencia.agregarJoya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);

        // Imprimir la etiqueta
        Impresora impresora = new Impresora();
        // Obtener el ID de la joya recién creada (puedes ajustar esto según tu lógica)

        // Obtener la joya recién creada con el último ID
        Joya nuevaJoya = persistencia.obtenerUltimaJoya();
        String idParaImprimir = (nuevaJoya.getDisplayId() != null && !nuevaJoya.getDisplayId().isBlank()) ? nuevaJoya.getDisplayId() : String.valueOf(nuevaJoya.getId());
        String zplData = generarZPLEtiqueta(idParaImprimir, precio, peso, tienePiedra, infoPiedra, categoria);
        impresora.imprimirEtiqueta(zplData);
    }

    public boolean crearJoyaConAutorizacion(SessionContext session,
                                            String nombre,
                                            String precio,
                                            double peso,
                                            String categoria,
                                            String socio,
                                            String observacion,
                                            Boolean tienePiedra,
                                            String infoPiedra) {
        if (session != null && !session.isAdmin()) {
            Joya joyaPendiente = persistencia.registrarPendienteCrearJoya(
                    session.userId(),
                    nombre,
                    precio,
                    peso,
                    categoria,
                    socio,
                    observacion,
                    Boolean.TRUE.equals(tienePiedra),
                    infoPiedra
            );

            // Aunque quede pendiente de aprobación, se imprime la etiqueta al crearla.
            reImprimirDespues(
                    joyaPendiente.getId(),
                    nombre,
                    precio,
                    peso,
                    categoria,
                    observacion,
                    tienePiedra,
                    infoPiedra
            );
            return false;
        }

        crearJoya(nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra);
        return true;
    }


    public void volverImprimir(String nombre, String precio, double peso, String categoria, String observacion, Boolean tienePiedra, String infoPiedra) {
        // Imprimir la etiqueta
        Impresora impresora = new Impresora();
        Joya nuevaJoya = persistencia.obtenerUltimaJoya();
        String idParaImprimir = (nuevaJoya.getDisplayId() != null && !nuevaJoya.getDisplayId().isBlank()) ? nuevaJoya.getDisplayId() : String.valueOf(nuevaJoya.getId());
        String zplData = generarZPLEtiqueta(idParaImprimir, precio, peso, tienePiedra, infoPiedra, categoria);
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
    public void actualizarJoya(Long id, String nombre, String precio, double peso, String categoria, String socio, String observacion, Boolean tienePiedra, String infoPiedra, Boolean vendido, LocalDateTime fechaVendida, String estado, String precioVenta) {
        if (peso < 0) {
            throw new IllegalArgumentException("El precio y el peso deben ser mayores o iguales a 0.");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        persistencia.editarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, precioVenta);
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
                                                 String precioVenta) {
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
                    precioVenta
            );
            return false;
        }

        actualizarJoya(id, nombre, precio, peso, categoria, socio, observacion, tienePiedra, infoPiedra, vendido, fechaVendida, estado, precioVenta);
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


    // Marcar una joya como vendida
    public void marcarComoVendida(Long id, String precioVenta) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo.");
        }

        String precioVentaNormalizado = validarYNormalizarPrecioVenta(precioVenta);

        Joya joya = persistencia.obtenerJoyaPorId(id); // Obtener la joya desde la base de datos
        if (joya == null) {
            throw new IllegalArgumentException("La joya con ID " + id + " no existe.");
        }

        if (joya.isVendido()) {
            throw new IllegalStateException("La joya ya está marcada como vendida.");
        }

        joya.setVendido(true); // Actualizar el atributo "vendido" de la joya
        joya.setFechaVendida(LocalDateTime.now());
        joya.setPrecioVenta(precioVentaNormalizado);
        persistencia.editarJoya(joya.getId(), joya.getNombre(), joya.getPrecio(), joya.getPeso(), joya.getCategoria(), joya.getSocio(), joya.getObservacion(), joya.isTienePiedra(), joya.getInfoPiedra(), joya.isVendido(), joya.getFechaVendida(), joya.getEstado(), joya.getPrecioVenta());
    }

    public boolean marcarComoVendidaConAutorizacion(SessionContext session, Long id, String precioVenta) {
        String precioVentaNormalizado = validarYNormalizarPrecioVenta(precioVenta);
        if (session != null && !session.isAdmin()) {
            persistencia.registrarPendienteMarcarVendida(session.userId(), id, precioVentaNormalizado);
            return false;
        }
        marcarComoVendida(id, precioVentaNormalizado);
        return true;
    }

    private String validarYNormalizarPrecioVenta(String precioVenta) {
        if (precioVenta == null || precioVenta.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar el precio de venta.");
        }

        String valor = precioVenta.trim();
        String numerico = valor
                .replace("$", "")
                .replace("'", "")
                .replace(" ", "")
                .replace(",", "");

        try {
            if (new BigDecimal(numerico).compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio de venta debe ser mayor a 0.");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El precio de venta debe ser numérico.");
        }

        return valor;
    }

    public List<Joya> obtenerVentasPorRango(LocalDateTime desde, LocalDateTime hasta) {
        return persistencia.obtenerVentasPorRango(desde, hasta);
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

    // Eliminar socio
    public void eliminarSocio(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un socio válido.");
        }
        persistencia.eliminarSocio(id);
    }

    //GENERAR ZPL DE ETIQUETA
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
        if (!fuente.isBlank()) {
            return fuente;
        }
        return limpiarCampoZpl(categoria);
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

    // Método auxiliar para determinar el tipo de piedra basado en la categoría
    private String determinarTipoPiedra(String categoria) {
        if (categoria == null || categoria.isEmpty()) {
            return ""; // Por defecto vacío si no hay categoría
        }

        String catLower = categoria.toLowerCase();

        // Mapeo de categorías a tipos de piedra
        if (catLower.contains("esmeralda") || catLower.contains("esm")) {
            return "esm";
        } else if (catLower.contains("rubi") || catLower.contains("ruby")) {
            return "rub";
        } else if (catLower.contains("zafiro") || catLower.contains("sapphire")) {
            return "saf";
        } else if (catLower.contains("diamante") || catLower.contains("diamond")) {
            return "dia";
        } else if (catLower.contains("consignacion piedra")) {
            return "esm"; // Asumiendo que consignaciones son esmeraldas por defecto
        }

        // Si no se reconoce la categoría específica, devolver vacío por defecto
        return "";
    }
}
