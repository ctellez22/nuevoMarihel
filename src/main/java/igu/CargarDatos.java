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
    private static final String SOCIO_POR_DEFECTO = "joyeria marihel";

    private JTextField txtNombre;
    private JTextArea txtObs;
    private JTextField txtPeso;
    private JComboBox<String> cmbCategoria;
    private JButton btnLimpiar;
    private JButton btnGuardar;
    private JPanel mainPanel; // Contenedor principal de la ventana
    private JCheckBox siCheckBox;
    private JTextArea textArea1;
    private JTextArea txtInfoPiedra;
    private JPanel fotoPanel;
    private JLabel txtPrecioTotal;
    private JFormattedTextField txtPrecioGramo;
    private JFormattedTextField txtPrecioTotalManual;
    private JComboBox<String> socios;
    private JLabel socio;

    private Controladora logicaController; // Instancia de la controladora de lógica
    private final SessionContext session;

    public CargarDatos(JFrame parent) {
        this(parent, null);
    }

    public CargarDatos(JFrame parent, SessionContext session) {
        this.session = session;
        inicializarTodo();
    }


    private void inicializarTodo() {
        // Instanciar la controladora de lógica
        logicaController = new Controladora();
        ImageIcon Icon = new ImageIcon(getClass().getResource("/imprimir.png"));
        ImageIcon customIcon = new ImageIcon(Icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        //
        ImageIcon successIcon = new ImageIcon(getClass().getResource("/nino.png"));
        ImageIcon scaledSuccessIcon = new ImageIcon(successIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        txtPrecioTotal.setText("0"); // Valor inicial
        txtPrecioTotalManual.setVisible(false);

        // Listener para txtPeso y txtPrecioGramo
        txtPeso.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                actualizarPrecioTotal();
            }
        });

        txtPrecioGramo.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                actualizarPrecioTotal();
            }
        });


        // Configurar símbolos personalizados para el formato
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('\''); // Separador de miles: apóstrofe
        symbols.setDecimalSeparator('.');  // Separador decimal: punto

        // Configurar el formato con los símbolos personalizados
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.###", symbols);
        decimalFormat.setGroupingUsed(true);

        NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
        numberFormatter.setAllowsInvalid(false); // No permitir valores no válidos
        numberFormatter.setValueClass(Double.class);

        txtPrecioGramo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(numberFormatter));
        txtPrecioTotalManual.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
        txtPrecioTotalManual.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        txtPrecioTotalManual.setValue(0.0);
        txtPrecioTotalManual.setVisible(false);

        if (cmbCategoria == null || socios == null) {
            throw new IllegalStateException("Faltan componentes en CargarDatos.form (cmbCategoria/socios). Revisa el binding del formulario.");
        }

        cargarCategoriasDesdeBD();
        cargarSociosDesdeBD();

        siCheckBox.addActionListener(e -> {
            boolean conPiedra = siCheckBox.isSelected();

            txtInfoPiedra.setVisible(conPiedra);
            txtPrecioGramo.setEnabled(!conPiedra);

            txtPrecioTotal.setVisible(!conPiedra);
            txtPrecioTotalManual.setVisible(conPiedra);

            if (conPiedra) {
                txtPrecioTotalManual.setValue(
                        Double.parseDouble(txtPrecioTotal.getText().replace("'", ""))
                );
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

                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "El nombre de la joya no puede estar vacío.",
                            "Error de entrada",
                            JOptionPane.ERROR_MESSAGE,
                            new ImageIcon(
                                    new ImageIcon(getClass().getResource("/llora.png"))
                                            .getImage()
                                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                            )
                    );
                    return;
                }

                if (cmbCategoria.getItemCount() <= 1) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "No hay categorías creadas. Debe crear al menos una antes de guardar.",
                            "Error de entrada",
                            JOptionPane.ERROR_MESSAGE,
                            new ImageIcon(
                                    new ImageIcon(getClass().getResource("/llora.png"))
                                            .getImage()
                                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                            )
                    );
                    return;
                }

                if (categoria == null || cmbCategoria.getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Debe seleccionar una categoría válida.",
                            "Error de entrada",
                            JOptionPane.ERROR_MESSAGE,
                            new ImageIcon(
                                    new ImageIcon(getClass().getResource("/llora.png"))
                                            .getImage()
                                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                            )
                    );
                    return;
                }

                if (socios.getItemCount() <= 1) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "No hay socios creados. Debe crear al menos uno antes de guardar.",
                            "Error de entrada",
                            JOptionPane.ERROR_MESSAGE,
                            new ImageIcon(
                                    new ImageIcon(getClass().getResource("/llora.png"))
                                            .getImage()
                                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                            )
                    );
                    return;
                }

                if (socioSeleccionado == null || socios.getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Debe seleccionar un socio válido.",
                            "Error de entrada",
                            JOptionPane.ERROR_MESSAGE,
                            new ImageIcon(
                                    new ImageIcon(getClass().getResource("/llora.png"))
                                            .getImage()
                                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                            )
                    );
                    return;
                }

                if (obs.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Debe proporcionar una descripción u observaciones.",
                            "Error de entrada",
                            JOptionPane.ERROR_MESSAGE,
                            new ImageIcon(
                                    new ImageIcon(getClass().getResource("/llora.png"))
                                            .getImage()
                                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                            )
                    );
                    return;
                }

                double peso = Double.parseDouble(txtPeso.getText());

                double precioTotal;
                if (si) {
                    Number valorManual = (Number) txtPrecioTotalManual.getValue();
                    precioTotal = (valorManual != null)
                            ? valorManual.doubleValue()
                            : 0.0;
                } else {
                    double precioGramo = txtPrecioGramo.getText().isEmpty()
                            ? 0.0
                            : Double.parseDouble(txtPrecioGramo.getText().replace("'", ""));
                    precioTotal = peso * precioGramo;
                }

                String precioTotalStr = formatearNumero(precioTotal);

                boolean aplicadaDirecto = logicaController.crearJoyaConAutorizacion(
                        session,
                        nombre,
                        precioTotalStr,
                        peso,
                        categoria,
                        socioSeleccionado,
                        obs,
                        si,
                        infoPiedra
                );

                String mensaje = (aplicadaDirecto ? "Joya guardada correctamente:\n" : "Solicitud enviada para aprobación:\n") +
                        "Etiqueta enviada correctamente.\n" +
                        "Nombre: " + nombre + "\n" +
                        "Precio: " + precioTotalStr + "\n" +
                        "Peso: " + peso + "\n" +
                        "Categoría: " + categoria + "\n" +
                        "Socio: " + socioSeleccionado + "\n" +
                        "Observaciones: " + obs;

                boolean reimprimir = true;
                while (reimprimir) {
                    Object[] opciones = {"Aceptar", "Volver a imprimir"};
                    int opcionSeleccionada = JOptionPane.showOptionDialog(
                            mainPanel,
                            mensaje,
                            "Éxito",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            scaledSuccessIcon,
                            opciones,
                            opciones[0]
                    );

                    if (opcionSeleccionada == JOptionPane.NO_OPTION) {
                        logicaController.volverImprimir(
                                nombre,
                                precioTotalStr,
                                peso,
                                categoria,
                                obs,
                                si,
                                infoPiedra
                        );

                        JOptionPane.showMessageDialog(
                                mainPanel,
                                "Imprimiendo la joya nuevamente:\n" + mensaje,
                                "Reimpresión",
                                JOptionPane.INFORMATION_MESSAGE,
                                customIcon
                        );
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
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "Error al guardar la joya: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE,
                        new ImageIcon(
                                new ImageIcon(getClass().getResource("/llora.png"))
                                        .getImage()
                                        .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
                        )
                );
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void cargarCategoriasDesdeBD() {
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem("Seleccione categoría...");
        try {
            List<Categoria> categorias = logicaController.obtenerCategorias();
            for (Categoria categoria : categorias) {
                cmbCategoria.addItem(categoria.getNombre());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar las categorías: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSociosDesdeBD() {
        if (socios == null) {
            return;
        }
        socios.removeAllItems();
        socios.addItem("Seleccione socio...");
        try {
            List<Socio> socios = logicaController.obtenerSocios();
            for (Socio socio : socios) {
                this.socios.addItem(socio.getNombre());
            }
            seleccionarSocioPorDefecto();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar los socios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarSocioPorDefecto() {
        if (socios == null || socios.getItemCount() == 0) {
            return;
        }

        for (int i = 0; i < socios.getItemCount(); i++) {
            String item = socios.getItemAt(i);
            if (item != null && item.trim().equalsIgnoreCase(SOCIO_POR_DEFECTO)) {
                socios.setSelectedIndex(i);
                return;
            }
        }

        socios.setSelectedIndex(0);
    }

    private void actualizarPrecioTotal() {
        if (siCheckBox.isSelected()) {
            return;
        }

        try {
            String textoGramo = txtPrecioGramo.getText().replace("'", "");
            String textoPeso = txtPeso.getText();

            if (!textoGramo.isEmpty() && !textoPeso.isEmpty()) {
                double precioGramo = Double.parseDouble(textoGramo);
                double peso = Double.parseDouble(textoPeso);
                String precioFormateado = formatearNumero(peso * precioGramo);
                txtPrecioTotal.setText(precioFormateado);
            } else {
                txtPrecioTotal.setText("0");
            }
        } catch (NumberFormatException ex) {
            txtPrecioTotal.setText("0");
        }
    }

    @FunctionalInterface
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }
    }

    public static String formatearNumero(double numero) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('\'');
        symbols.setDecimalSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#,##0.###", symbols);
        return formatter.format(numero);
    }
}

