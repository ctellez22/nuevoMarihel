package igu;

import com.marihel.utils.FormatterUtils;
import logica.Controladora;
import logica.Joya;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class DetallesJoya {
    private JPanel mainPanel;
    private JButton marcarVendidoButton;
    private JButton editarJoyaButton;
    private JButton reimprimirRefButton;
    private JLabel lblNombre;
    private JLabel lblCategoria;
    private JLabel lblPeso;
    private JLabel lblPrecio;
    private JLabel lblInfoPiedra;
    private JLabel lblFechaVendida;
    private JLabel lblObservaciones;
    private JLabel lblaFechaIngreso;
    private JLabel lblTienePiedra;
    private JLabel lblEstado;

    private final Controladora controladora;
    private final VerDatos interfazPrincipal;
    private final SessionContext session;


    public DetallesJoya(Joya joya, VerDatos interfazPrincipal){
        this(joya, interfazPrincipal, null);
    }

    public DetallesJoya(Joya joya, VerDatos interfazPrincipal, SessionContext session){
        // Instancia de la controladora
        this.controladora = new Controladora();
        this.interfazPrincipal = interfazPrincipal;
        this.session = session;

        // Configuración inicial de los componentes con la información de la joya
        String idVisible = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank()) ? joya.getDisplayId() : String.valueOf(joya.getId());
        lblNombre.setText("[" + idVisible + "] " + joya.getNombre());
        lblNombre.setToolTipText("ID: " + idVisible);
        lblCategoria.setText(joya.getCategoria());
        lblPeso.setText(FormatterUtils.formatearPeso(joya.getPeso()) + " gramos");
        lblPrecio.setText("$" + joya.getPrecio());
        lblTienePiedra.setText(joya.isTienePiedra() ? "Sí 💎" : "No 🪨");
        DateTimeFormatter formatterr = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblaFechaIngreso.setText(joya.getFechaIngreso().format(formatterr));

        lblObservaciones.setText(joya.getObservacion());
        lblEstado.setText(joya.getEstado() + " | " + joya.getEstadoAutorizacionTexto());
        lblEstado.setForeground(joya.isAutorizado() ? new Color(34, 139, 34) : new Color(204, 128, 0));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaVendidaTexto = joya.getFechaVendida() != null
                ? joya.getFechaVendida().format(formatter)
                : "No vendida";
        if (joya.getPrecioVenta() != null && !joya.getPrecioVenta().isBlank()) {
            fechaVendidaTexto += " | Precio venta: $" + joya.getPrecioVenta();
        }
        lblFechaVendida.setText(fechaVendidaTexto);

        if (joya.isTienePiedra()) {
            lblInfoPiedra.setText(joya.getInfoPiedra());
        } else {
            lblInfoPiedra.setVisible(false);
        }

        configurarBotones(joya);

    }

    private void configurarBotones(Joya joya) {
        // Configurar botón "Marcar como Vendido"
        configurarBotonVendido(marcarVendidoButton, joya.isVendido());
        marcarVendidoButton.addActionListener(e -> confirmarMarcarVendido(joya));

        // Configurar botón "Reimprimir Referencia"
        reimprimirRefButton.addActionListener(e -> reimprimirReferencia(joya));

        // Configurar botón "Editar"
        editarJoyaButton.addActionListener(e -> editarJoya(joya));
    }


    private void editarJoya(Joya joya) {
        EditarJoyaDialog editarDialog = new EditarJoyaDialog(joya, controladora, interfazPrincipal, session);
        editarDialog.setVisible(true);
    }

    private void reimprimirReferencia(Joya joya) {
        try {
            controladora.reImprimirDespues(
                    joya.getId(),
                    joya.getNombre(),
                    joya.getPrecio(),
                    joya.getPeso(),
                    joya.getCategoria(),
                    joya.getObservacion(),
                    joya.isTienePiedra(),
                    joya.getInfoPiedra()
            );

            JOptionPane.showMessageDialog(
                    null,
                    "La referencia se ha reimpreso correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE

            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al reimprimir la referencia: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void confirmarMarcarVendido(Joya joya) {
        String sugerido = (joya.getPrecioVenta() != null && !joya.getPrecioVenta().isBlank()) ? joya.getPrecioVenta() : joya.getPrecio();
        String precioVenta = JOptionPane.showInputDialog(
                null,
                "Ingresa el precio real de venta:",
                sugerido
        );

        if (precioVenta == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Estás seguro de que deseas marcar esta joya como vendida por $" + precioVenta.trim() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean aplicadaDirecto = controladora.marcarComoVendidaConAutorizacion(session, joya.getId(), precioVenta);
                JOptionPane.showMessageDialog(
                        null,
                        aplicadaDirecto
                                ? "La joya ha sido marcada como vendida."
                                : "Solicitud enviada para aprobación de administrador.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );
                marcarVendidoButton.setEnabled(false);
                marcarVendidoButton.setText(aplicadaDirecto ? "Ya Vendido" : "Pendiente aprobación");
                marcarVendidoButton.setBackground(Color.GRAY);
                joya.setPrecioVenta(precioVenta.trim());
                if (interfazPrincipal != null) {
                    interfazPrincipal.actualizarListaFiltrada();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error al marcar como vendida: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    private void configurarBotonVendido(JButton boton, boolean isVendido) {
        boton.setEnabled(!isVendido);
        boton.setText(isVendido ? "Ya Vendido" : "Marcar como Vendido");
        boton.setBackground(isVendido ? Color.GRAY : new Color(255, 69, 58));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


}
