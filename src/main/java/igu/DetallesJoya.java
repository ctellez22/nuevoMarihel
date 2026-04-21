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

    public DetallesJoya(Joya joya, VerDatos interfazPrincipal) {
        this(joya, interfazPrincipal, null);
    }

    public DetallesJoya(Joya joya, VerDatos interfazPrincipal, SessionContext session) {
        this.controladora = new Controladora(session);
        this.interfazPrincipal = interfazPrincipal;
        this.session = session;

        construirUI();
        rellenarDatos(joya);
        configurarBotones(joya);
    }

    // ── Construcción de la UI en código puro ────────────────────────────────────

    private void construirUI() {
        mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(UITheme.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Título
        JLabel title = new JLabel("Detalle Joya", SwingConstants.CENTER);
        title.setFont(UITheme.F_TITLE);
        title.setForeground(UITheme.TEXT);
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel izquierdo: imagen + botones
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(UITheme.BG);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 0, 0, 16)
        ));

        // Imagen
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/bolsita.png"));
            Image scaled = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftPanel.add(imgLabel);
        } catch (Exception ignored) {}

        leftPanel.add(Box.createVerticalStrut(16));

        marcarVendidoButton = UITheme.dangerBtn("Marcar como Vendido");
        marcarVendidoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        marcarVendidoButton.setPreferredSize(new Dimension(160, 36));
        marcarVendidoButton.setMaximumSize(new Dimension(160, 36));

        editarJoyaButton = UITheme.primaryBtn("Editar Joya");
        editarJoyaButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editarJoyaButton.setPreferredSize(new Dimension(160, 36));
        editarJoyaButton.setMaximumSize(new Dimension(160, 36));

        reimprimirRefButton = UITheme.secondaryBtn("Reimprimir ref.");
        reimprimirRefButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reimprimirRefButton.setPreferredSize(new Dimension(160, 36));
        reimprimirRefButton.setMaximumSize(new Dimension(160, 36));

        leftPanel.add(marcarVendidoButton);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(editarJoyaButton);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(reimprimirRefButton);
        leftPanel.add(Box.createVerticalGlue());

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Panel de datos (centro)
        JPanel dataPanel = new JPanel(new GridBagLayout());
        dataPanel.setBackground(UITheme.BG);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        lblNombre       = new JLabel();
        lblCategoria    = new JLabel();
        lblPeso         = new JLabel();
        lblPrecio       = new JLabel();
        lblTienePiedra  = new JLabel();
        lblaFechaIngreso = new JLabel();
        lblObservaciones = new JLabel();
        lblFechaVendida = new JLabel();
        lblInfoPiedra   = new JLabel();
        lblEstado       = new JLabel();

        String[] keys = {
            "Descripción:", "Categoría:", "Peso:", "Precio:",
            "Tiene piedra:", "Fecha Ingreso:", "Observaciones:",
            "Fecha Vendida:", "Info. piedra:", "Estado:"
        };
        JLabel[] values = {
            lblNombre, lblCategoria, lblPeso, lblPrecio,
            lblTienePiedra, lblaFechaIngreso, lblObservaciones,
            lblFechaVendida, lblInfoPiedra, lblEstado
        };

        for (int i = 0; i < keys.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel key = new JLabel(keys[i]);
            key.setFont(UITheme.F_LABEL);
            key.setForeground(UITheme.TEXT);
            dataPanel.add(key, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            values[i].setFont(UITheme.F_BODY);
            values[i].setForeground(UITheme.TEXT);
            dataPanel.add(values[i], gbc);
            gbc.fill = GridBagConstraints.NONE;
        }

        // Push everything to the top
        gbc.gridx = 0; gbc.gridy = keys.length;
        gbc.weighty = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        dataPanel.add(Box.createVerticalGlue(), gbc);

        mainPanel.add(dataPanel, BorderLayout.CENTER);
    }

    // ── Relleno de datos ────────────────────────────────────────────────────────

    private void rellenarDatos(Joya joya) {
        String idVisible = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank())
                ? joya.getDisplayId() : String.valueOf(joya.getId());
        lblNombre.setText("[" + idVisible + "] " + joya.getNombre());
        lblNombre.setToolTipText("ID: " + idVisible);

        lblCategoria.setText(joya.getCategoria());
        lblPeso.setText(FormatterUtils.formatearPeso(joya.getPeso()) + " gramos");
        lblPrecio.setText("$" + joya.getPrecio());
        lblTienePiedra.setText(joya.isTienePiedra() ? "Sí" : "No");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblaFechaIngreso.setText(joya.getFechaIngreso() != null ? joya.getFechaIngreso().format(fmt) : "—");

        lblObservaciones.setText(joya.getObservacion());

        lblEstado.setText(joya.getEstado() + " | " + joya.getEstadoAutorizacionTexto());
        lblEstado.setForeground(joya.isAutorizado() ? new Color(34, 139, 34) : new Color(204, 128, 0));

        String fechaVendidaTexto = joya.getFechaVendida() != null
                ? joya.getFechaVendida().format(fmt)
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
    }

    // ── Botones ─────────────────────────────────────────────────────────────────

    private void configurarBotones(Joya joya) {
        configurarBotonVendido(marcarVendidoButton, joya.isVendido());
        marcarVendidoButton.addActionListener(e -> confirmarMarcarVendido(joya));
        reimprimirRefButton.addActionListener(e -> reimprimirReferencia(joya));
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
            JOptionPane.showMessageDialog(null, "La referencia se ha reimpreso correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al reimprimir la referencia: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmarMarcarVendido(Joya joya) {
        String sugerido = (joya.getPrecioVenta() != null && !joya.getPrecioVenta().isBlank())
                ? joya.getPrecioVenta() : joya.getPrecio();
        String precioVenta = JOptionPane.showInputDialog(null, "Ingresa el precio real de venta:", sugerido);

        if (precioVenta == null) return;

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
                JOptionPane.showMessageDialog(null, "Error al marcar como vendida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
