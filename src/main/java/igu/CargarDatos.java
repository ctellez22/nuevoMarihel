package igu;

import logica.Controladora;
import logica.Categoria;
import logica.Impresora;
import logica.Joya;
import logica.Socio;
import org.example.SessionContext;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;


public class CargarDatos {
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
    private JButton btnAgregarPiedraIndependiente;

    private Controladora logicaController; // Instancia de la controladora de lógica
    private final SessionContext session;
    private final List<PiedraCosteo> piedrasIngresadas = new ArrayList<>();

    public CargarDatos(JFrame parent) {
        this(parent, null);
    }

    public CargarDatos(JFrame parent, SessionContext session) {
        this.session = session;
        inicializarTodo();
    }


    private void inicializarTodo() {
        // Instanciar la controladora de lógica
        logicaController = new Controladora(session);
        ImageIcon Icon = new ImageIcon(getClass().getResource("/imprimir.png"));
        ImageIcon customIcon = new ImageIcon(Icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        //
        ImageIcon successIcon = new ImageIcon(getClass().getResource("/nino.png"));
        ImageIcon scaledSuccessIcon = new ImageIcon(successIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        txtPrecioTotal.setText("0"); // Valor inicial
        txtPrecioTotalManual.setVisible(false);
        txtInfoPiedra.setVisible(true);
        txtInfoPiedra.setEditable(false);
        txtInfoPiedra.setLineWrap(true);
        txtInfoPiedra.setWrapStyleWord(true);
        txtInfoPiedra.setText("Sin piedras registradas.");
        if (btnAgregarPiedraIndependiente == null) {
            btnAgregarPiedraIndependiente = new JButton();
        }
        aplicarEstiloVisual();

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

        siCheckBox.setText("Agregar una piedra independiente");
        siCheckBox.setSelected(false);
        siCheckBox.addActionListener(e -> {
            if (siCheckBox.isSelected()) {
                siCheckBox.setSelected(false);
                solicitarYAgregarPiedraIndependiente();
            }
        });
        btnAgregarPiedraIndependiente.addActionListener(e -> solicitarYAgregarPiedraIndependiente());

        btnLimpiar.addActionListener(e -> {
            txtNombre.setText("");
            txtObs.setText("");
            txtPeso.setText("");
            txtPrecioGramo.setText("");
            cmbCategoria.setSelectedIndex(0);
            aplicarSocioPorDefecto();
            txtInfoPiedra.setText("");
            siCheckBox.setSelected(false);
            piedrasIngresadas.clear();
            refrescarResumenPiedras();
        });

        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText();
                String categoria = (String) cmbCategoria.getSelectedItem();
                String socioSeleccionado = (String) socios.getSelectedItem();
                boolean tienePiedra = !piedrasIngresadas.isEmpty();
                String infoPiedra = construirInfoPiedra();
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

                double precioGramo = txtPrecioGramo.getText().isEmpty()
                        ? 0.0
                        : Double.parseDouble(txtPrecioGramo.getText().replace("'", ""));
                double precioBase = peso * precioGramo;
                double precioTotal = precioBase + calcularTotalPiedras();

                String precioTotalStr = formatearNumero(precioTotal);

                boolean aplicadaDirecto = logicaController.crearJoyaConAutorizacion(
                        session,
                        nombre,
                        precioTotalStr,
                        peso,
                        categoria,
                        socioSeleccionado,
                        obs,
                        tienePiedra,
                        infoPiedra
                );
                String idGenerado = ""; // No disponible en esta versión

                if (!aplicadaDirecto) {
                    imprimirEtiquetaPendiente(
                            idGenerado,
                            precioTotalStr,
                            peso,
                            tienePiedra,
                            infoPiedra,
                            categoria
                    );
                }

                String mensaje = (aplicadaDirecto ? "Joya guardada correctamente:\n" : "Solicitud enviada para aprobación:\n") +
                        (idGenerado.isBlank() ? "" : "ID: " + idGenerado + "\n") +
                        "Nombre: " + nombre + "\n" +
                        "Precio: " + precioTotalStr + "\n" +
                        "Peso: " + peso + "\n" +
                        "Categoría: " + categoria + "\n" +
                        "Socio: " + socioSeleccionado + "\n" +
                        "Observaciones: " + obs;

                boolean reimprimir = true;
                while (reimprimir) {
                    Object[] opciones = new Object[]{"Aceptar", "Volver a imprimir"};
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
                        if (aplicadaDirecto) {
                            logicaController.volverImprimir(
                                    nombre,
                                    precioTotalStr,
                                    peso,
                                    categoria,
                                    obs,
                                    tienePiedra,
                                    infoPiedra
                            );
                        } else {
                            imprimirEtiquetaPendiente(
                                    idGenerado,
                                    precioTotalStr,
                                    peso,
                                    tienePiedra,
                                    infoPiedra,
                                    categoria
                            );
                        }

                        JOptionPane.showMessageDialog(
                                mainPanel,
                                aplicadaDirecto
                                        ? "Imprimiendo la joya nuevamente:\n" + mensaje
                                        : "Imprimiendo la etiqueta nuevamente:\n" + mensaje,
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
                aplicarSocioPorDefecto();
                txtInfoPiedra.setText("");
                siCheckBox.setSelected(false);
                piedrasIngresadas.clear();
                txtPrecioGramo.setText("");
                txtPrecioTotal.setText("0");
                txtPrecioTotalManual.setValue(null);
                refrescarResumenPiedras();

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
            aplicarSocioPorDefecto();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar los socios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarSocioPorDefecto() {
        if (socios == null || socios.getItemCount() == 0) {
            return;
        }

        // Si existe el socio "punto", se selecciona por defecto.
        for (int i = 0; i < socios.getItemCount(); i++) {
            String opcion = socios.getItemAt(i);
            if (opcion != null && "punto".equalsIgnoreCase(opcion.trim())) {
                socios.setSelectedIndex(i);
                return;
            }
        }

        socios.setSelectedIndex(0);
    }

    private void imprimirEtiquetaPendiente(String idEtiqueta, String precio, double peso, Boolean tienePiedra, String infoPiedra, String categoria) {
        Impresora impresora = new Impresora();
        String zplData = logicaController.generarZPLEtiqueta(
                (idEtiqueta == null || idEtiqueta.isBlank()) ? "PEND" : idEtiqueta,
                precio,
                peso,
                Boolean.TRUE.equals(tienePiedra),
                infoPiedra,
                categoria
        );
        impresora.imprimirEtiqueta(zplData);
    }

    private void actualizarPrecioTotal() {
        try {
            String textoGramo = txtPrecioGramo.getText().replace("'", "");
            String textoPeso = txtPeso.getText();

            if (!textoGramo.isEmpty() && !textoPeso.isEmpty()) {
                double precioGramo = Double.parseDouble(textoGramo);
                double peso = Double.parseDouble(textoPeso);
                double precioBase = peso * precioGramo;
                double precioConPiedras = precioBase + calcularTotalPiedras();
                String precioFormateado = formatearNumero(precioConPiedras);
                txtPrecioTotal.setText(precioFormateado);
            } else {
                txtPrecioTotal.setText(formatearNumero(calcularTotalPiedras()));
            }
        } catch (NumberFormatException ex) {
            txtPrecioTotal.setText(formatearNumero(calcularTotalPiedras()));
        }
    }

    private void solicitarYAgregarPiedraIndependiente() {
        JTextField quilatesField = new JTextField();
        JTextField precioQuilateField = new JTextField();
        instalarMascaraPrecio(precioQuilateField);
        JComboBox<String> tipoPiedraCombo = new JComboBox<>(new String[]{
                "Diamante", "Esmeralda", "Rubí", "Zafiro", "Topacio", "Hechura", "Otro"
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Quilates de la piedra:"));
        panel.add(quilatesField);
        panel.add(new JLabel("Tipo de piedra:"));
        panel.add(tipoPiedraCombo);
        panel.add(new JLabel("Precio por quilate:"));
        panel.add(precioQuilateField);

        int result = JOptionPane.showConfirmDialog(
                mainPanel,
                panel,
                "Agregar piedra independiente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double quilates = parseNumeroLibre(quilatesField.getText());
            double precioPorQuilate = parseNumeroLibre(precioQuilateField.getText());
            String tipoPiedra = tipoPiedraCombo.getSelectedItem() == null
                    ? "Otro"
                    : tipoPiedraCombo.getSelectedItem().toString().trim();
            if (quilates <= 0 || precioPorQuilate <= 0) {
                throw new IllegalArgumentException("Los quilates y el precio por quilate deben ser mayores a 0.");
            }

            piedrasIngresadas.add(new PiedraCosteo(
                    quilates,
                    precioPorQuilate,
                    null,
                    "independiente",
                    tipoPiedra
            ));
            refrescarResumenPiedras();
            actualizarPrecioTotal();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Datos de piedra inválidos: " + ex.getMessage(),
                    "Error de entrada",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private double parseNumeroLibre(String valor) {
        String normalizado = valor == null ? "" : valor.trim().replace("'", "").replace(',', '.');
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar un número.");
        }
        return Double.parseDouble(normalizado);
    }

    private void instalarMascaraPrecio(JTextField campo) {
        AbstractDocument doc = (AbstractDocument) campo.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            private boolean actualizando;

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (actualizando) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }

                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String reemplazo = text == null ? "" : text;
                String propuesto = actual.substring(0, offset) + reemplazo + actual.substring(offset + length);
                int caretPropuesto = offset + reemplazo.length();
                aplicarMascaraPrecio(fb, campo, propuesto, caretPropuesto);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                replace(fb, offset, length, "", null);
            }

            private void aplicarMascaraPrecio(FilterBypass fb, JTextField campo, String propuesto, int caretPropuesto) throws BadLocationException {
                String digitos = extraerDigitos(propuesto);
                String formateado = digitos.isEmpty() ? "" : formatearConApostrofes(digitos);

                int digitosAntesCaret = contarDigitosHasta(propuesto, caretPropuesto);
                int nuevaPosCaret = posicionSegunDigitos(formateado, digitosAntesCaret);

                actualizando = true;
                fb.replace(0, fb.getDocument().getLength(), formateado, null);
                actualizando = false;

                SwingUtilities.invokeLater(() -> campo.setCaretPosition(Math.min(nuevaPosCaret, campo.getText().length())));
            }
        });
    }

    private int contarDigitosHasta(String texto, int limite) {
        int max = Math.min(Math.max(limite, 0), texto.length());
        int cuenta = 0;
        for (int i = 0; i < max; i++) {
            if (Character.isDigit(texto.charAt(i))) {
                cuenta++;
            }
        }
        return cuenta;
    }

    private int posicionSegunDigitos(String textoFormateado, int cantidadDigitos) {
        if (cantidadDigitos <= 0) {
            return 0;
        }
        int vistos = 0;
        for (int i = 0; i < textoFormateado.length(); i++) {
            if (Character.isDigit(textoFormateado.charAt(i))) {
                vistos++;
                if (vistos == cantidadDigitos) {
                    return i + 1;
                }
            }
        }
        return textoFormateado.length();
    }

    private String extraerDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        String soloDigitos = valor.replaceAll("\\D", "");
        return soloDigitos.replaceFirst("^0+(?!$)", "");
    }

    private String formatearConApostrofes(String digitos) {
        if (digitos == null || digitos.isBlank()) {
            return "";
        }
        StringBuilder invertido = new StringBuilder(digitos).reverse();
        StringBuilder conSeparador = new StringBuilder();
        for (int i = 0; i < invertido.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                conSeparador.append('\'');
            }
            conSeparador.append(invertido.charAt(i));
        }
        return conSeparador.reverse().toString();
    }

    private void refrescarResumenPiedras() {
        if (piedrasIngresadas.isEmpty()) {
            txtInfoPiedra.setText("Sin piedras registradas.");
            txtInfoPiedra.setCaretPosition(0);
            actualizarPrecioTotal();
            return;
        }

        StringBuilder sb = new StringBuilder();
        String separador = "----------------------------------------";
        double totalPiedras = 0.0;
        for (int i = 0; i < piedrasIngresadas.size(); i++) {
            PiedraCosteo p = piedrasIngresadas.get(i);
            totalPiedras += p.total();
            String origen = p.loteNombre();
            sb.append("Piedra ").append(i + 1).append('\n')
                    .append("  Origen    : ").append(origen).append('\n')
                    .append("  Tipo      : ").append(p.tipoPiedra() != null && !p.tipoPiedra().isBlank() ? p.tipoPiedra() : "-").append('\n')
                    .append("  Quilates  : ").append(formatearNumero(p.peso())).append('\n')
                    .append("  P/quilate : ").append(formatearNumero(p.precioPorQuilate())).append('\n')
                    .append("  Subtotal  : ").append(formatearNumero(p.total())).append('\n');
            if (i < piedrasIngresadas.size() - 1) {
                sb.append(separador).append('\n');
            }
        }
        sb.append(separador).append('\n')
                .append("TOTAL PIEDRAS (QUILATES): ")
                .append(formatearNumero(totalPiedras));
        txtInfoPiedra.setText(sb.toString());
        txtInfoPiedra.setCaretPosition(0);
        actualizarPrecioTotal();
    }

    private double calcularTotalPiedras() {
        double total = 0.0;
        for (PiedraCosteo p : piedrasIngresadas) {
            total += p.total();
        }
        return total;
    }

    private String construirInfoPiedra() {
        if (piedrasIngresadas.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < piedrasIngresadas.size(); i++) {
            PiedraCosteo p = piedrasIngresadas.get(i);
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append("P").append(i + 1).append("(");
            sb.append("origen=independiente,");
            if (p.tipoPiedra() != null && !p.tipoPiedra().isBlank()) {
                sb.append("tipo=")
                        .append(p.tipoPiedra())
                        .append(",");
            }
            sb.append("peso=")
                    .append(formatearNumero(p.peso()))
                    .append(",precioQ=")
                    .append(formatearNumero(p.precioPorQuilate()))
                    .append(",total=")
                    .append(formatearNumero(p.total()))
                    .append(")");
        }
        return sb.toString();
    }

     private void aplicarEstiloVisual() {
         mainPanel.removeAll();
         mainPanel.setLayout(new BorderLayout(20, 20));
         mainPanel.setBackground(UITheme.BG);
         mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

         // Inicializar componentes si no existen
         if (txtNombre == null) txtNombre = UITheme.styledField();
         if (txtPeso == null) txtPeso = UITheme.styledField();
         if (txtPrecioGramo == null) txtPrecioGramo = new JFormattedTextField();
         if (cmbCategoria == null) cmbCategoria = new JComboBox<>();
         if (socios == null) socios = new JComboBox<>();
         if (txtObs == null) txtObs = new JTextArea(4, 30);
         if (txtInfoPiedra == null) txtInfoPiedra = new JTextArea(5, 30);
         if (txtPrecioTotal == null) txtPrecioTotal = new JLabel("0");
         if (siCheckBox == null) siCheckBox = new JCheckBox();
         if (btnAgregarPiedraIndependiente == null) btnAgregarPiedraIndependiente = UITheme.secondaryBtn("Agregar piedra independiente");
         if (btnLimpiar == null) btnLimpiar = UITheme.secondaryBtn("Limpiar");
         if (btnGuardar == null) btnGuardar = UITheme.primaryBtn("Guardar joya");

         // Aplicar estilos
         UITheme.styleField(txtNombre);
         UITheme.styleField(txtPeso);
         UITheme.styleField(txtPrecioGramo);
         UITheme.styleCombo(cmbCategoria);
         UITheme.styleCombo(socios);
         UITheme.styleArea(txtObs);
         UITheme.styleArea(txtInfoPiedra);
         txtInfoPiedra.setEditable(false);
         txtInfoPiedra.setBackground(Color.WHITE);
         txtInfoPiedra.setFont(new Font("Monospaced", Font.PLAIN, 12));
         txtPrecioTotal.setFont(new Font("SansSerif", Font.BOLD, 24));
         txtPrecioTotal.setForeground(new Color(34, 197, 94)); // Verde para precio

         // Header
         JPanel headerPanel = UITheme.card(15);
         headerPanel.setLayout(new BorderLayout());
         headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
         JLabel titleLabel = new JLabel("Cargar Datos de Joya 💎", SwingConstants.CENTER);
         titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
         titleLabel.setForeground(UITheme.TEXT);
         JLabel subtitleLabel = new JLabel("Ingresa la información de la joya y calcula el costo total.", SwingConstants.CENTER);
         subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
         subtitleLabel.setForeground(UITheme.TEXT_MUTED);
         headerPanel.add(titleLabel, BorderLayout.NORTH);
         headerPanel.add(subtitleLabel, BorderLayout.CENTER);

         // Panel izquierdo: Formulario
         JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
         leftPanel.setOpaque(false);

         // Información básica
         JPanel basicInfoPanel = UITheme.card(10);
         basicInfoPanel.setLayout(new GridBagLayout());
         basicInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0;

         gbc.gridx = 0; gbc.gridy = 0;
         basicInfoPanel.add(createLabeledField("Nombre:", txtNombre), gbc);
         gbc.gridy = 1;
         basicInfoPanel.add(createLabeledField("Categoría:", cmbCategoria), gbc);
         gbc.gridy = 2;
         basicInfoPanel.add(createLabeledField("Socio:", socios), gbc);
         gbc.gridy = 3;
         basicInfoPanel.add(createLabeledField("Peso (g):", txtPeso), gbc);
         gbc.gridy = 4;
         basicInfoPanel.add(createLabeledField("Precio por gramo:", txtPrecioGramo), gbc);

         // Observaciones
         JPanel obsPanel = UITheme.card(10);
         obsPanel.setLayout(new BorderLayout());
         obsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         obsPanel.add(new JLabel("Observaciones:"), BorderLayout.NORTH);
         JScrollPane obsScroll = UITheme.styledScroll(txtObs);
         obsScroll.setPreferredSize(new Dimension(0, 80));
         obsPanel.add(obsScroll, BorderLayout.CENTER);

         leftPanel.add(basicInfoPanel, BorderLayout.NORTH);
         leftPanel.add(obsPanel, BorderLayout.CENTER);

         // Panel derecho: Resumen y piedras
         JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
         rightPanel.setOpaque(false);

         // Resumen de precio
         JPanel pricePanel = UITheme.card(10);
         pricePanel.setLayout(new BorderLayout());
         pricePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
         pricePanel.add(new JLabel("Precio Total:", SwingConstants.CENTER), BorderLayout.NORTH);
         txtPrecioTotal.setHorizontalAlignment(SwingConstants.CENTER);
         pricePanel.add(txtPrecioTotal, BorderLayout.CENTER);

         // Piedras
         JPanel stonesPanel = UITheme.card(10);
         stonesPanel.setLayout(new BorderLayout());
         stonesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         stonesPanel.add(new JLabel("Piedras:"), BorderLayout.NORTH);
         JScrollPane stonesScroll = UITheme.styledScroll(txtInfoPiedra);
         stonesScroll.setPreferredSize(new Dimension(0, 150));
         stonesPanel.add(stonesScroll, BorderLayout.CENTER);

         // Botones para piedras
         JPanel stonesButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
         stonesButtons.setOpaque(false);
         stonesButtons.add(siCheckBox);
         stonesButtons.add(btnAgregarPiedraIndependiente);
         stonesPanel.add(stonesButtons, BorderLayout.SOUTH);

         rightPanel.add(pricePanel, BorderLayout.NORTH);
         rightPanel.add(stonesPanel, BorderLayout.CENTER);

         // Panel inferior: Botones de acción
         JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
         actionPanel.setOpaque(false);
         actionPanel.add(btnLimpiar);
         actionPanel.add(btnGuardar);

         // Combinar todo
         JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
         contentPanel.setOpaque(false);
         contentPanel.add(leftPanel, BorderLayout.WEST);
         contentPanel.add(rightPanel, BorderLayout.EAST);

         mainPanel.add(headerPanel, BorderLayout.NORTH);
         mainPanel.add(contentPanel, BorderLayout.CENTER);
         mainPanel.add(actionPanel, BorderLayout.SOUTH);

         mainPanel.revalidate();
         mainPanel.repaint();
     }

     private JPanel createLabeledField(String label, JComponent field) {
         JPanel panel = new JPanel(new BorderLayout(5, 5));
         panel.setOpaque(false);
         panel.add(new JLabel(label), BorderLayout.WEST);
         panel.add(field, BorderLayout.CENTER);
         return panel;
     }

    private record PiedraCosteo(double peso, double precioPorQuilate, Long loteId, String loteNombre, String tipoPiedra) {
        double total() {
            return peso * precioPorQuilate;
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
