package igu;

import logica.Controladora;
import logica.Categoria;
import logica.Socio;
import org.example.SessionContext;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class CargarDatos {
    private String socioPorDefecto;

    private JTextField txtNombre;
    private JTextArea txtObs;
    private JTextField txtPeso;
    private JComboBox<String> cmbCategoria;
    private JButton btnLimpiar;
    private JButton btnGuardar;
    private JPanel mainPanel;
    private JCheckBox siCheckBox;
    private JTextArea txtInfoPiedra;
    private JPanel fotoPanel;
    private JLabel txtPrecioTotal;
    private JFormattedTextField txtPrecioGramo;
    private JFormattedTextField txtPrecioTotalManual;
    private JComboBox<String> socios;

    private Controladora logicaController;
    private final SessionContext session;

    public CargarDatos(JFrame parent) {
        this(parent, null);
    }

    public CargarDatos(JFrame parent, SessionContext session) {
        this.session = session;
        this.socioPorDefecto = (session != null && session.isQueens()) ? "joyeria queens" : "joyeria marihel";
        construirUI();
        inicializarTodo();
    }

    // ── Construcción de la UI en código puro ─────────────────────────────────

    private void construirUI() {
        mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBackground(UITheme.BG);
        mainPanel.setBorder(UITheme.paddedRound(UITheme.BORDER, 14, 10, 16));

        // Título
        JLabel title = new JLabel("Cargar Datos", SwingConstants.CENTER);
        title.setFont(UITheme.F_TITLE);
        title.setForeground(UITheme.TEXT);
        mainPanel.add(title, BorderLayout.NORTH);

        // Imagen decorativa (nube)
        fotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fotoPanel.setBackground(UITheme.BG);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/nube.png"));
            Image scaled = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            fotoPanel.add(new JLabel(new ImageIcon(scaled)));
        } catch (Exception ignored) {}

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // ── Descripción ──────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(label("Descripción: "), gbc);
        txtNombre = UITheme.styledField();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.gridwidth = 3;
        formPanel.add(txtNombre, gbc);
        gbc.gridwidth = 1;

        // ── Peso ─────────────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(label("Peso: "), gbc);
        txtPeso = UITheme.styledField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        formPanel.add(txtPeso, gbc);
        gbc.gridwidth = 1;

        // ── Precio Total / Manual / Gramo ─────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(label("Precio Total: "), gbc);

        txtPrecioTotal = new JLabel("0");
        txtPrecioTotal.setFont(UITheme.F_LABEL);
        txtPrecioTotal.setForeground(UITheme.TEXT);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.5;
        formPanel.add(txtPrecioTotal, gbc);

        txtPrecioTotalManual = new JFormattedTextField();
        txtPrecioTotalManual.setFont(UITheme.F_BODY);
        txtPrecioTotalManual.setForeground(UITheme.TEXT);
        txtPrecioTotalManual.setBorder(UITheme.paddedRound(UITheme.BORDER, 10, 5, 8));
        txtPrecioTotalManual.setVisible(false);
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.5;
        formPanel.add(txtPrecioTotalManual, gbc);

        JLabel precioGramoLabel = new JLabel("Precio Gramo:");
        precioGramoLabel.setFont(UITheme.F_LABEL);
        precioGramoLabel.setForeground(UITheme.TEXT);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(precioGramoLabel, gbc);

        txtPrecioGramo = new JFormattedTextField();
        txtPrecioGramo.setFont(UITheme.F_BODY);
        txtPrecioGramo.setForeground(UITheme.TEXT);
        txtPrecioGramo.setBorder(UITheme.paddedRound(UITheme.BORDER, 10, 5, 8));
        gbc.gridx = 4; gbc.gridy = 2; gbc.weightx = 0.5;
        formPanel.add(txtPrecioGramo, gbc);

        // ── Categoría ─────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(label("Categoria: "), gbc);
        cmbCategoria = new JComboBox<>();
        UITheme.styleCombo(cmbCategoria);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1; gbc.gridwidth = 3;
        formPanel.add(cmbCategoria, gbc);
        gbc.gridwidth = 1;

        // ── Socio ─────────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(label("Socio"), gbc);
        socios = new JComboBox<>();
        UITheme.styleCombo(socios);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1;
        formPanel.add(socios, gbc);

        // ── Tiene Piedra ──────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        formPanel.add(label("Tiene Piedra:"), gbc);
        siCheckBox = new JCheckBox("si");
        siCheckBox.setFont(UITheme.F_BODY);
        siCheckBox.setForeground(UITheme.TEXT);
        siCheckBox.setBackground(UITheme.BG);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0;
        formPanel.add(siCheckBox, gbc);
        txtInfoPiedra = new JTextArea(2, 20);
        UITheme.styleArea(txtInfoPiedra);
        txtInfoPiedra.setBorder(UITheme.paddedRound(UITheme.BORDER, 10, 5, 8));
        txtInfoPiedra.setVisible(false);
        gbc.gridx = 2; gbc.gridy = 5; gbc.weightx = 1; gbc.gridwidth = 2;
        formPanel.add(UITheme.styledScroll(txtInfoPiedra), gbc);
        gbc.gridwidth = 1;

        // ── Observaciones ─────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(label("Observacion: "), gbc);
        txtObs = new JTextArea(4, 30);
        UITheme.styleArea(txtObs);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        formPanel.add(UITheme.styledScroll(txtObs), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // ── Botones ────────────────────────────────────────────────────────
        btnLimpiar = UITheme.secondaryBtn("Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(130, 36));
        btnGuardar = UITheme.primaryBtn("Guardar");
        btnGuardar.setPreferredSize(new Dimension(130, 36));
        try {
            btnLimpiar.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/limpiar.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}
        try {
            btnGuardar.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/guardar.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        btnPanel.setBackground(UITheme.BG);
        btnPanel.add(btnLimpiar);
        btnPanel.add(btnGuardar);

        // Combinar foto + form
        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        centerPanel.setBackground(UITheme.BG);
        centerPanel.add(fotoPanel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.F_LABEL);
        lbl.setForeground(UITheme.TEXT);
        return lbl;
    }

    // ── Inicialización de lógica ─────────────────────────────────────────────

    private void inicializarTodo() {
        logicaController = new Controladora(session);

        ImageIcon customIcon;
        ImageIcon scaledSuccessIcon;
        try {
            customIcon = new ImageIcon(new ImageIcon(getClass().getResource("/imprimir.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        } catch (Exception e) { customIcon = null; }
        try {
            scaledSuccessIcon = new ImageIcon(new ImageIcon(getClass().getResource("/nino.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        } catch (Exception e) { scaledSuccessIcon = null; }
        final ImageIcon finalCustomIcon = customIcon;
        final ImageIcon finalSuccessIcon = scaledSuccessIcon;

        txtPrecioTotal.setText("0");
        txtPrecioTotalManual.setVisible(false);

        // Listeners para cálculo automático de precio
        txtPeso.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { actualizarPrecioTotal(); }
        });
        txtPrecioGramo.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { actualizarPrecioTotal(); }
        });

        // Formato numérico
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('\'');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.###", symbols);
        NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setValueClass(Double.class);
        txtPrecioGramo.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
        txtPrecioTotalManual.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
        txtPrecioTotalManual.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        txtPrecioTotalManual.setValue(0.0);

        cargarCategoriasDesdeBD();
        cargarSociosDesdeBD();

        siCheckBox.addActionListener(e -> {
            boolean conPiedra = siCheckBox.isSelected();
            txtInfoPiedra.setVisible(conPiedra);
            txtPrecioGramo.setEnabled(!conPiedra);
            txtPrecioTotal.setVisible(!conPiedra);
            txtPrecioTotalManual.setVisible(conPiedra);
            if (conPiedra) {
                txtPrecioTotalManual.setValue(Double.parseDouble(txtPrecioTotal.getText().replace("'", "")));
            } else {
                actualizarPrecioTotal();
            }
        });

        btnLimpiar.addActionListener(e -> {
            txtNombre.setText("");
            txtObs.setText("");
            txtPeso.setText("");
            txtPrecioGramo.setText("");
            cmbCategoria.setSelectedIndex(0);
            seleccionarSocioPorDefecto();
            txtInfoPiedra.setText("");
            siCheckBox.setSelected(false);
        });

        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText();
                String categoria = (String) cmbCategoria.getSelectedItem();
                String socioSeleccionado = (String) socios.getSelectedItem();
                boolean si = siCheckBox.isSelected();
                String infoPiedra = txtInfoPiedra.getText();
                String obs = txtObs.getText();

                ImageIcon errorIcon;
                try { errorIcon = new ImageIcon(new ImageIcon(getClass().getResource("/llora.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); }
                catch (Exception ex) { errorIcon = null; }

                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "El nombre de la joya no puede estar vacío.", "Error de entrada", JOptionPane.ERROR_MESSAGE, errorIcon);
                    return;
                }
                if (cmbCategoria.getItemCount() <= 1) {
                    JOptionPane.showMessageDialog(mainPanel, "No hay categorías creadas. Debe crear al menos una antes de guardar.", "Error de entrada", JOptionPane.ERROR_MESSAGE, errorIcon);
                    return;
                }
                if (categoria == null || cmbCategoria.getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(mainPanel, "Debe seleccionar una categoría válida.", "Error de entrada", JOptionPane.ERROR_MESSAGE, errorIcon);
                    return;
                }
                if (socios.getItemCount() <= 1) {
                    JOptionPane.showMessageDialog(mainPanel, "No hay socios creados. Debe crear al menos uno antes de guardar.", "Error de entrada", JOptionPane.ERROR_MESSAGE, errorIcon);
                    return;
                }
                if (socioSeleccionado == null || socios.getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(mainPanel, "Debe seleccionar un socio válido.", "Error de entrada", JOptionPane.ERROR_MESSAGE, errorIcon);
                    return;
                }
                if (obs.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "Debe proporcionar una descripción u observaciones.", "Error de entrada", JOptionPane.ERROR_MESSAGE, errorIcon);
                    return;
                }

                double peso = Double.parseDouble(txtPeso.getText());
                double precioTotal;
                if (si) {
                    Number valorManual = (Number) txtPrecioTotalManual.getValue();
                    precioTotal = (valorManual != null) ? valorManual.doubleValue() : 0.0;
                } else {
                    double precioGramo = txtPrecioGramo.getText().isEmpty()
                            ? 0.0 : Double.parseDouble(txtPrecioGramo.getText().replace("'", ""));
                    precioTotal = peso * precioGramo;
                }

                String precioTotalStr = formatearNumero(precioTotal);

                boolean aplicadaDirecto = logicaController.crearJoyaConAutorizacion(
                        session, nombre, precioTotalStr, peso, categoria, socioSeleccionado, obs, si, infoPiedra
                );

                String mensaje = (aplicadaDirecto ? "Joya guardada correctamente:\n" : "Solicitud enviada para aprobación:\n")
                        + "Nombre: " + nombre + "\n"
                        + "Precio: " + precioTotalStr + "\n"
                        + "Peso: " + peso + "\n"
                        + "Categoría: " + categoria + "\n"
                        + "Socio: " + socioSeleccionado + "\n"
                        + "Observaciones: " + obs;

                boolean reimprimir = true;
                while (reimprimir) {
                    Object[] opciones = {"Aceptar", "Volver a imprimir"};
                    int opcionSeleccionada = JOptionPane.showOptionDialog(
                            mainPanel, mensaje, "Éxito",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            finalSuccessIcon, opciones, opciones[0]
                    );
                    if (opcionSeleccionada == JOptionPane.NO_OPTION) {
                        logicaController.volverImprimir(nombre, precioTotalStr, peso, categoria, obs, si, infoPiedra);
                        JOptionPane.showMessageDialog(mainPanel, "Imprimiendo la joya nuevamente:\n" + mensaje, "Reimpresión", JOptionPane.INFORMATION_MESSAGE, finalCustomIcon);
                    } else {
                        reimprimir = false;
                    }
                }

                txtNombre.setText("");
                txtPeso.setText("");
                txtObs.setText("");
                cmbCategoria.setSelectedIndex(0);
                seleccionarSocioPorDefecto();
                txtInfoPiedra.setText("");
                siCheckBox.setSelected(false);
                txtPrecioGramo.setText("");
                txtPrecioTotal.setText("0");
                txtPrecioTotalManual.setValue(null);

            } catch (Exception ex) {
                ImageIcon errorIcon;
                try { errorIcon = new ImageIcon(new ImageIcon(getClass().getResource("/llora.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); }
                catch (Exception e2) { errorIcon = null; }
                JOptionPane.showMessageDialog(mainPanel, "Error al guardar la joya: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
            }
        });
    }

    private void cargarCategoriasDesdeBD() {
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem("Seleccione categoría...");
        try {
            for (Categoria categoria : logicaController.obtenerCategorias()) {
                cmbCategoria.addItem(categoria.getNombre());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar las categorías: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSociosDesdeBD() {
        socios.removeAllItems();
        socios.addItem("Seleccione socio...");
        try {
            for (Socio socio : logicaController.obtenerSocios()) {
                socios.addItem(socio.getNombre());
            }
            seleccionarSocioPorDefecto();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar los socios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarSocioPorDefecto() {
        for (int i = 0; i < socios.getItemCount(); i++) {
            String item = socios.getItemAt(i);
            if (item != null && item.trim().equalsIgnoreCase(socioPorDefecto)) {
                socios.setSelectedIndex(i);
                return;
            }
        }
        socios.setSelectedIndex(0);
    }

    private void actualizarPrecioTotal() {
        if (siCheckBox != null && siCheckBox.isSelected()) return;
        try {
            String textoGramo = txtPrecioGramo.getText().replace("'", "");
            String textoPeso = txtPeso.getText();
            if (!textoGramo.isEmpty() && !textoPeso.isEmpty()) {
                txtPrecioTotal.setText(formatearNumero(Double.parseDouble(textoPeso) * Double.parseDouble(textoGramo)));
            } else {
                txtPrecioTotal.setText("0");
            }
        } catch (NumberFormatException ex) {
            txtPrecioTotal.setText("0");
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @FunctionalInterface
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }

    public static String formatearNumero(double numero) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('\'');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.###", symbols).format(numero);
    }
}
