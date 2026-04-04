package igu;

import logica.Controladora;
import logica.Joya;
import logica.Categoria;
import logica.Socio;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class EditarJoyaDialog extends JDialog {
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtPeso;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbSocio;
    private JComboBox<String> cmbEstado;
    private JCheckBox chkTienePiedra;
    private JTextArea txtObservacion;
    private JCheckBox chkVendido;
    private JTextField txtInfoPiedra;
    private final Controladora controladora;
    private final VerDatos interfazPrincipal;
    private final SessionContext session;

    public EditarJoyaDialog(Joya joya, Controladora controladora, VerDatos interfazPrincipal) {
        this(joya, controladora, interfazPrincipal, null);
    }

    public EditarJoyaDialog(Joya joya, Controladora controladora, VerDatos interfazPrincipal, SessionContext session) {
        this.controladora = controladora;
        this.interfazPrincipal = interfazPrincipal;
        this.session = session;

        setTitle("Editar Joya");
        setModal(true);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel de edición
        JPanel editPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        editPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Campos de edición
        txtNombre = new JTextField(joya.getNombre());
        txtPrecio = new JTextField(String.valueOf(joya.getPrecio()));
        txtPeso = new JTextField(String.valueOf(joya.getPeso()));

        cmbCategoria = new JComboBox<>();
        cmbSocio = new JComboBox<>();
        cargarCategoriasDesdeBD();
        cargarSociosDesdeBD();
        cmbCategoria.setSelectedItem(joya.getCategoria());
        cmbSocio.setSelectedItem(joya.getSocio());

        // Configurar JComboBox para estado
        cmbEstado = new JComboBox<>(new String[]{"disponible", "anulado", "prestado"});
        cmbEstado.setSelectedItem(joya.getEstado());

        //txtCategoria = new JTextField(joya.getCategoria());
        //txtEstado = new JTextField(joya.getEstado());
        txtObservacion = new JTextArea(joya.getObservacion());
        chkTienePiedra = new JCheckBox("Tiene Piedra", joya.isTienePiedra());
        chkVendido = new JCheckBox("Vendido", joya.isVendido());
        txtInfoPiedra = new JTextField(String.valueOf( joya.getInfoPiedra()));

        // Añadir componentes al panel
        editPanel.add(new JLabel("Nombre:"));
        editPanel.add(txtNombre);
        editPanel.add(new JLabel("Precio:"));
        editPanel.add(txtPrecio);
        editPanel.add(new JLabel("Peso:"));
        editPanel.add(txtPeso);
        editPanel.add(new JLabel("Categoría:"));
        editPanel.add(cmbCategoria);
        editPanel.add(new JLabel("Socio:"));
        editPanel.add(cmbSocio);
        editPanel.add(new JLabel("Estado:"));
        editPanel.add(cmbEstado);
        editPanel.add(new JLabel("Tiene Piedra:"));
        editPanel.add(chkTienePiedra);
        editPanel.add(new JLabel("Vendido:"));
        editPanel.add(chkVendido);
        editPanel.add(new JLabel("Observaciones:"));
        JScrollPane scrollPane = new JScrollPane(txtObservacion);
        editPanel.add(new JLabel("Info Piedra"));
        editPanel.add(txtInfoPiedra);
        editPanel.add(scrollPane);

        // Botones
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnGuardar);

        // Acción del botón Guardar
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCambios(joya);
            }
        });

        // Acción del botón Cancelar
        btnCancelar.addActionListener(e -> dispose());

        // Añadir paneles al diálogo
        add(editPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void guardarCambios(Joya joya) {
        try {
            // Actualizar los valores de la joya
            joya.setNombre(txtNombre.getText());
            joya.setPrecio(txtPrecio.getText());
            joya.setPeso(Double.parseDouble(txtPeso.getText()));
            if (cmbCategoria.getItemCount() <= 1 || cmbCategoria.getSelectedIndex() <= 0) {
                throw new IllegalArgumentException("Debe seleccionar una categoría válida.");
            }
            if (cmbSocio.getItemCount() <= 1 || cmbSocio.getSelectedIndex() <= 0) {
                throw new IllegalArgumentException("Debe seleccionar un socio válido.");
            }

            joya.setCategoria((String) cmbCategoria.getSelectedItem());
            joya.setSocio((String) cmbSocio.getSelectedItem());
            joya.setEstado((String) cmbEstado.getSelectedItem());
            joya.setTienePiedra(chkTienePiedra.isSelected());
            joya.setVendido(chkVendido.isSelected());
            joya.setObservacion(txtObservacion.getText());
            joya.setInfoPiedra(txtInfoPiedra.getText());

            // Llamar a la controladora para guardar los cambios
            boolean aplicadaDirecto = controladora.actualizarJoyaConAutorizacion(
                    session,
                    joya.getId(),
                    joya.getNombre(),
                    joya.getPrecio(),
                    joya.getPeso(),
                    joya.getCategoria(),
                    joya.getSocio(),
                    joya.getObservacion(),
                    joya.isTienePiedra(),
                    joya.getInfoPiedra(),
                    joya.isVendido(),
                    joya.getFechaVendida(),
                    joya.getEstado(),
                    joya.getPrecioVenta()
            );

            // Confirmación al usuario
            JOptionPane.showMessageDialog(
                    this,
                    aplicadaDirecto ? "Joya actualizada correctamente." : "Cambio enviado para aprobación de administrador.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Actualizar lista principal si es necesario
            if (interfazPrincipal != null) {
                interfazPrincipal.actualizarListaFiltrada();
            }

            dispose(); // Cerrar el diálogo
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar los cambios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCategoriasDesdeBD() {
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem("Seleccione categoría...");
        List<Categoria> categorias = controladora.obtenerCategorias();
        for (Categoria categoria : categorias) {
            cmbCategoria.addItem(categoria.getNombre());
        }
    }

    private void cargarSociosDesdeBD() {
        cmbSocio.removeAllItems();
        cmbSocio.addItem("Seleccione socio...");
        List<Socio> socios = controladora.obtenerSocios();
        for (Socio socio : socios) {
            cmbSocio.addItem(socio.getNombre());
        }
    }
}
