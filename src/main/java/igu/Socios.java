package igu;

import logica.Controladora;
import logica.Socio;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Socios {

    private final JPanel mainPanel;
    private final JTextField txtNombre;
    private final DefaultListModel<String> listModel;
    private final Controladora controladora;

    public Socios(JFrame parent) {
        this(parent, null);
    }

    public Socios(JFrame parent, SessionContext session) {
        this.controladora = new Controladora(session);

        this.mainPanel = new JPanel(new BorderLayout(12, 12));
        this.mainPanel.setBackground(UITheme.BG);
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Panel superior ───────────────────────────────────────────────────────
        JPanel panelSuperior = new JPanel(new BorderLayout(8, 8));
        panelSuperior.setBackground(UITheme.BG);

        JLabel lblNombre = new JLabel("Nuevo socio:");
        lblNombre.setFont(UITheme.F_LABEL);
        lblNombre.setForeground(UITheme.TEXT);

        this.txtNombre = new JTextField();
        UITheme.styleField(this.txtNombre);

        JButton btnGuardar   = UITheme.primaryBtn("Guardar");
        JButton btnEliminar  = UITheme.dangerBtn("Eliminar");
        JButton btnRefrescar = UITheme.secondaryBtn("Refrescar");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotones.setBackground(UITheme.BG);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGuardar);

        panelSuperior.add(lblNombre,    BorderLayout.WEST);
        panelSuperior.add(txtNombre,    BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.EAST);

        // ── Lista ────────────────────────────────────────────────────────────────
        this.listModel = new DefaultListModel<>();
        JList<String> listSocios = new JList<>(listModel);
        listSocios.setFont(UITheme.F_BODY);
        listSocios.setBackground(UITheme.BG);
        listSocios.setForeground(UITheme.TEXT);
        listSocios.setSelectionBackground(UITheme.ACCENT);
        listSocios.setSelectionForeground(Color.WHITE);
        listSocios.setFixedCellHeight(32);

        JScrollPane scrollPane = UITheme.styledScroll(listSocios);

        this.mainPanel.add(panelSuperior, BorderLayout.NORTH);
        this.mainPanel.add(scrollPane,    BorderLayout.CENTER);

        // ── Listeners ────────────────────────────────────────────────────────────
        btnGuardar.addActionListener(e -> guardarSocio());
        btnEliminar.addActionListener(e -> eliminarSocioSeleccionado(listSocios));
        btnRefrescar.addActionListener(e -> cargarSocios());

        this.mainPanel.registerKeyboardAction(
                e -> txtNombre.requestFocusInWindow(),
                KeyStroke.getKeyStroke("ctrl N"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        cargarSocios();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void guardarSocio() {
        String nombre = txtNombre.getText();
        try {
            controladora.crearSocio(nombre);
            txtNombre.setText("");
            cargarSocios();
            JOptionPane.showMessageDialog(mainPanel, "Socio guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudo guardar el socio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSocioSeleccionado(JList<String> listSocios) {
        String seleccion = listSocios.getSelectedValue();
        if (seleccion == null || seleccion.isBlank()) {
            JOptionPane.showMessageDialog(mainPanel, "Seleccione un socio para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long id = extraerId(seleccion);
            int confirmacion = JOptionPane.showConfirmDialog(
                    mainPanel,
                    "¿Seguro que desea eliminar el socio seleccionado?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmacion == JOptionPane.YES_OPTION) {
                controladora.eliminarSocio(id);
                cargarSocios();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudo eliminar el socio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Long extraerId(String textoFila) {
        String[] partes = textoFila.split(" - ", 2);
        return Long.parseLong(partes[0].trim());
    }

    private void cargarSocios() {
        listModel.clear();
        try {
            List<Socio> socios = controladora.obtenerSocios();
            for (Socio socio : socios) {
                listModel.addElement(socio.getId() + " - " + socio.getNombre());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar los socios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
