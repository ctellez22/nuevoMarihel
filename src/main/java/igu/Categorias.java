package igu;

import logica.Categoria;
import logica.Controladora;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Categorias {

    private final JPanel mainPanel;
    private final JTextField txtNombre;
    private final DefaultListModel<String> listModel;
    private final Controladora controladora;

    public Categorias(JFrame parent) {
        this(parent, null);
    }

    public Categorias(JFrame parent, SessionContext session) {
        this.controladora = new Controladora(session);

        this.mainPanel = new JPanel(new BorderLayout(12, 12));
        this.mainPanel.setBackground(UITheme.BG);
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Panel superior ───────────────────────────────────────────────────────
        JPanel panelSuperior = new JPanel(new BorderLayout(8, 8));
        panelSuperior.setBackground(UITheme.BG);

        JLabel lblNombre = new JLabel("Nueva categoría:");
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
        JList<String> listCategorias = new JList<>(listModel);
        listCategorias.setFont(UITheme.F_BODY);
        listCategorias.setBackground(UITheme.BG);
        listCategorias.setForeground(UITheme.TEXT);
        listCategorias.setSelectionBackground(UITheme.ACCENT);
        listCategorias.setSelectionForeground(Color.WHITE);
        listCategorias.setFixedCellHeight(32);

        JScrollPane scrollPane = UITheme.styledScroll(listCategorias);

        this.mainPanel.add(panelSuperior, BorderLayout.NORTH);
        this.mainPanel.add(scrollPane,    BorderLayout.CENTER);

        // ── Listeners ────────────────────────────────────────────────────────────
        btnGuardar.addActionListener(e -> guardarCategoria());
        btnEliminar.addActionListener(e -> eliminarCategoriaSeleccionada(listCategorias));
        btnRefrescar.addActionListener(e -> cargarCategorias());

        this.mainPanel.registerKeyboardAction(
                e -> txtNombre.requestFocusInWindow(),
                KeyStroke.getKeyStroke("ctrl N"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        cargarCategorias();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void guardarCategoria() {
        String nombre = txtNombre.getText();
        try {
            controladora.crearCategoria(nombre);
            txtNombre.setText("");
            cargarCategorias();
            JOptionPane.showMessageDialog(mainPanel, "Categoría guardada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudo guardar la categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCategoriaSeleccionada(JList<String> listCategorias) {
        String seleccion = listCategorias.getSelectedValue();
        if (seleccion == null || seleccion.isBlank()) {
            JOptionPane.showMessageDialog(mainPanel, "Seleccione una categoría para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long id = extraerId(seleccion);
            int confirmacion = JOptionPane.showConfirmDialog(
                    mainPanel,
                    "¿Seguro que desea eliminar la categoría seleccionada?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmacion == JOptionPane.YES_OPTION) {
                controladora.eliminarCategoria(id);
                cargarCategorias();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudo eliminar la categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Long extraerId(String textoFila) {
        String[] partes = textoFila.split(" - ", 2);
        return Long.parseLong(partes[0].trim());
    }

    private void cargarCategorias() {
        listModel.clear();
        try {
            List<Categoria> categorias = controladora.obtenerCategorias();
            for (Categoria categoria : categorias) {
                listModel.addElement(categoria.getId() + " - " + categoria.getNombre());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar las categorías: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
