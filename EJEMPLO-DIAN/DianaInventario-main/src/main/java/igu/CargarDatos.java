package igu;

import logica.Controladora;
import logica.Categoria;
import logica.Impresora;
import logica.Joya;
import logica.Lote;
import logica.PuntosFisicos;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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
    private JRadioButton crearLoteRadioButton;
    private JRadioButton crearJoyaRadioButton;
    private JComboBox<String> puntoFisico;
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
        logicaController = new Controladora();
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
        inicializarPuntoFisico();

        // Listeners para los radio buttons
        crearJoyaRadioButton.addActionListener(e -> {
            if (crearJoyaRadioButton.isSelected()) {
                // Mostrar interfaz de joya
                mostrarInterfazJoya();
            }
        });

        crearLoteRadioButton.addActionListener(e -> {
            if (crearLoteRadioButton.isSelected()) {
                // Abrir diálogo de lote
                abrirDialogoCrearLote();
            }
        });

        siCheckBox.setText("Agregar una piedra de un lote");
        siCheckBox.setSelected(false);
        siCheckBox.addActionListener(e -> {
            if (siCheckBox.isSelected()) {
                siCheckBox.setSelected(false);
                solicitarYAgregarPiedra();
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
                String puntoFisico = resolverPuntoFisicoCreacion();

                validarDisponibilidadActualDeLotes();

                Joya joyaCreada = logicaController.crearJoyaConAutorizacion(
                        session,
                        nombre,
                        precioTotalStr,
                        peso,
                        categoria,
                        socioSeleccionado,
                        obs,
                        tienePiedra,
                        infoPiedra,
                        puntoFisico
                );
                boolean aplicadaDirecto = joyaCreada != null && joyaCreada.isAutorizado();
                String idGenerado = (joyaCreada != null && joyaCreada.getDisplayId() != null && !joyaCreada.getDisplayId().isBlank())
                        ? joyaCreada.getDisplayId()
                        : (joyaCreada != null && joyaCreada.getId() != null ? String.valueOf(joyaCreada.getId()) : "");

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
            List<Socio> socios = logicaController.obtenerSocios(session);
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

    private String resolverPuntoFisicoCreacion() {
        if (session != null && !session.isAdmin()) {
            String puntoVendedor = session.puntoFisico();
            if (puntoVendedor == null || puntoVendedor.isBlank()) {
                throw new IllegalArgumentException("El vendedor no tiene punto fisico asignado. Contacte a un administrador.");
            }
            return puntoVendedor.trim();
        }

        if (puntoFisico == null || puntoFisico.getSelectedItem() == null || puntoFisico.getSelectedIndex() <= 0) {
            throw new IllegalArgumentException("Debe ingresar un punto fisico para guardar la joya.");
        }
        return puntoFisico.getSelectedItem().toString().trim();
    }

    private void inicializarPuntoFisico() {
        if (puntoFisico == null) {
            return;
        }

        puntoFisico.removeAllItems();
        if (session != null && !session.isAdmin()) {
            String puntoVendedor = session.puntoFisico();
            if (puntoVendedor == null || puntoVendedor.isBlank()) {
                puntoFisico.addItem("Sin punto asignado");
                puntoFisico.setSelectedIndex(0);
            } else {
                puntoFisico.addItem(puntoVendedor.trim());
                puntoFisico.setSelectedIndex(0);
            }
            puntoFisico.setEnabled(false);
            return;
        }

        puntoFisico.addItem("Seleccione punto físico...");
        for (String opcion : PuntosFisicos.opciones()) {
            puntoFisico.addItem(opcion);
        }
        puntoFisico.setSelectedIndex(0);
        puntoFisico.setEnabled(true);
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

    private void solicitarYAgregarPiedra() {
        List<Lote> lotesDisponibles = obtenerLotesDisponiblesParaPiedra();
        if (lotesDisponibles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No hay lotes disponibles con peso para asignar piedras.",
                    "Lotes no disponibles",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JComboBox<LoteOption> loteCombo = new JComboBox<>();
        for (Lote lote : lotesDisponibles) {
            loteCombo.addItem(new LoteOption(lote.getId(), lote.getNombre(), lote.getPesoTotal()));
        }

        JTextField pesoPiedraField = new JTextField();
        JTextField precioQuilateField = new JTextField();
        instalarMascaraPrecio(precioQuilateField);
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Lote origen:"));
        panel.add(loteCombo);
        panel.add(new JLabel("Quilates de la piedra:"));
        panel.add(pesoPiedraField);
        panel.add(new JLabel("Precio por quilate:"));
        panel.add(precioQuilateField);

        int result = JOptionPane.showConfirmDialog(
                mainPanel,
                panel,
                "Agregar piedra",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double pesoPiedra = parseNumeroLibre(pesoPiedraField.getText());
            double precioPorQuilate = parseNumeroLibre(precioQuilateField.getText());
            LoteOption loteSeleccionado = (LoteOption) loteCombo.getSelectedItem();

            if (loteSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un lote.");
            }
            if (pesoPiedra <= 0 || precioPorQuilate <= 0) {
                throw new IllegalArgumentException("El peso y el precio por quilate deben ser mayores a 0.");
            }
            double disponibleRestante = loteSeleccionado.pesoDisponible() - calcularPesoSeleccionadoParaLote(loteSeleccionado.id());
            if (pesoPiedra > disponibleRestante) {
                throw new IllegalArgumentException("Los quilates de la piedra superan lo disponible del lote (" + formatearNumero(disponibleRestante) + ").");
            }

            piedrasIngresadas.add(new PiedraCosteo(
                    pesoPiedra,
                    precioPorQuilate,
                    loteSeleccionado.id(),
                    loteSeleccionado.nombre(),
                    null
            ));
            refrescarResumenPiedras();
            actualizarPrecioTotal();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Datos de piedra invalidos: " + ex.getMessage(),
                    "Error de entrada",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void solicitarYAgregarPiedraIndependiente() {
        JTextField quilatesField = new JTextField();
        JTextField precioQuilateField = new JTextField();
        instalarMascaraPrecio(precioQuilateField);
        JComboBox<String> tipoPiedraCombo = new JComboBox<>(new String[]{
                "Diamante", "Esmeralda", "Rubí", "Zafiro", "Topacio", "Otro"
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
            String origen = p.loteId() == null ? "independiente" : p.loteNombre();
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
            if (p.loteId() != null) {
                sb.append("loteId=")
                        .append(p.loteId())
                        .append(",lote=")
                        .append(p.loteNombre())
                        .append(",");
            } else {
                sb.append("origen=independiente,");
            }
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

    private List<Lote> obtenerLotesDisponiblesParaPiedra() {
        List<Lote> lotes = logicaController.obtenerTodosLosLotes(session);
        return lotes.stream()
                .filter(l -> l != null && l.getId() != null)
                .filter(l -> !l.isVendido())
                .filter(l -> l.getPesoTotal() > 0)
                .filter(l -> l.getEstado() == null || !"vendido".equalsIgnoreCase(l.getEstado()))
                .sorted(Comparator.comparing(Lote::getId).reversed())
                .toList();
    }

    private void descontarPesoLotesUsados() {
        if (piedrasIngresadas.isEmpty()) {
            return;
        }
        Map<Long, Double> pesoPorLote = new LinkedHashMap<>();
        for (PiedraCosteo p : piedrasIngresadas) {
            if (p.loteId() != null) {
                pesoPorLote.merge(p.loteId(), p.peso(), Double::sum);
            }
        }
        for (Map.Entry<Long, Double> entry : pesoPorLote.entrySet()) {
            logicaController.descontarPesoLote(entry.getKey(), entry.getValue());
        }
    }

    private double calcularPesoSeleccionadoParaLote(Long loteId) {
        double total = 0.0;
        for (PiedraCosteo piedra : piedrasIngresadas) {
            if (loteId != null && loteId.equals(piedra.loteId())) {
                total += piedra.peso();
            }
        }
        return total;
    }

    private void validarDisponibilidadActualDeLotes() {
        for (Map.Entry<Long, Double> entry : agruparPesoPorLote().entrySet()) {
            Lote lote = logicaController.obtenerLotePorId(entry.getKey());
            if (lote == null) {
                throw new IllegalArgumentException("No se encontró el lote seleccionado para una de las piedras.");
            }
            if (lote.getPesoTotal() < entry.getValue()) {
                throw new IllegalArgumentException("El lote '" + lote.getNombre() + "' no tiene peso suficiente. Disponible: " + formatearNumero(lote.getPesoTotal()));
            }
        }
    }

    private Map<Long, Double> agruparPesoPorLote() {
        Map<Long, Double> pesoPorLote = new LinkedHashMap<>();
        for (PiedraCosteo p : piedrasIngresadas) {
            if (p.loteId() != null) {
                pesoPorLote.merge(p.loteId(), p.peso(), Double::sum);
            }
        }
        return pesoPorLote;
    }

     private void aplicarEstiloVisual() {
         mainPanel.removeAll();
         mainPanel.setLayout(new BorderLayout(18, 18));
         mainPanel.setBackground(ModernUI.BG);
         mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

         ModernUI.styleTextField(txtNombre);
         ModernUI.styleTextField(txtPeso);
         ModernUI.styleTextField(txtPrecioGramo);
         ModernUI.styleCombo(cmbCategoria);
         ModernUI.styleCombo(socios);
         ModernUI.styleCombo(puntoFisico);
         ajustarAlturaCombo(cmbCategoria, 42);
         ajustarAlturaCombo(socios, 42);
         ajustarAlturaCombo(puntoFisico, 42);
         
         ModernUI.styleTextArea(txtObs);
         ModernUI.styleTextArea(txtInfoPiedra);
         txtObs.setRows(2);
         txtObs.setPreferredSize(new Dimension(0, 64));
         txtInfoPiedra.setEditable(false);
         txtInfoPiedra.setBackground(Color.WHITE);
         txtInfoPiedra.setFont(new Font("Monospaced", Font.PLAIN, 13));
         txtInfoPiedra.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         txtInfoPiedra.setMargin(new Insets(8, 8, 8, 8));

        ModernUI.stylePrimaryButton(btnGuardar, "Guardar joya");
        ModernUI.styleSecondaryButton(btnLimpiar, "Limpiar formulario");
        ModernUI.styleChip(siCheckBox, "Agregar una piedra de un lote");
        ModernUI.styleSecondaryButton(btnAgregarPiedraIndependiente, "Agregar una piedra independiente");
        btnAgregarPiedraIndependiente.setFont(new Font("SansSerif", Font.BOLD, 12));

        configurarRadio(crearJoyaRadioButton, "Joya");
        configurarRadio(crearLoteRadioButton, "Lote");
        ButtonGroup group = new ButtonGroup();
        group.add(crearJoyaRadioButton);
        group.add(crearLoteRadioButton);
        if (!crearJoyaRadioButton.isSelected() && !crearLoteRadioButton.isSelected()) {
            crearJoyaRadioButton.setSelected(true);
        }

        JPanel header = new ModernUI.RoundedPanel(new BorderLayout(12, 12), ModernUI.PRIMARY, new Color(92, 108, 224), 30, 1, new Color(32, 42, 88, 34), 8);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 6));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Cargar datos de joya 💎");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Costeo claro, piedras por lote y una experiencia mucho más linda para trabajar.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(228, 234, 255));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        switchPanel.setOpaque(false);
        switchPanel.add(crearJoyaRadioButton);
        switchPanel.add(crearLoteRadioButton);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(switchPanel, BorderLayout.EAST);

        JPanel contenido = new JPanel(new GridLayout(1, 2, 18, 18));
        contenido.setOpaque(false);

        JPanel formulario = ModernUI.createRoundedPanel(new BorderLayout(0, 18));
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formulario.add(crearCabeceraFormulario(), BorderLayout.NORTH);
        formulario.add(crearGridFormulario(), BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout(0, 12));
        lateral.setOpaque(false);
        lateral.add(crearPanelResumen(), BorderLayout.NORTH);
        lateral.add(crearPanelPiedrasYNotas(), BorderLayout.CENTER);
        lateral.add(crearPanelAcciones(), BorderLayout.SOUTH);

        contenido.add(formulario);
        contenido.add(lateral);

        JScrollPane scroll = ModernUI.wrapScroll(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(ModernUI.BG);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JPanel crearGridFormulario() {
         JPanel panel = new JPanel(new GridBagLayout());
         panel.setOpaque(false);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(8, 4, 8, 4);
         gbc.gridx = 0;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0;

         int row = 0;
         gbc.gridy = row++;
         panel.add(crearCampoCompacto("Nombre", txtNombre), gbc);

         gbc.gridy = row++;
         panel.add(crearCampoCompacto("Categoría", cmbCategoria), gbc);

         gbc.gridy = row++;
         panel.add(crearCampoCompacto("Socio", socios), gbc);

         gbc.gridy = row++;
         panel.add(crearCampoCompacto("Punto físico", puntoFisico), gbc);

         JScrollPane scrollObsCompacto = ModernUI.wrapScroll(txtObs);
         scrollObsCompacto.setPreferredSize(new Dimension(0, 72));
         scrollObsCompacto.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(new Color(205, 215, 236), 1, true),
                 BorderFactory.createEmptyBorder(1, 1, 1, 1)
         ));
         gbc.gridy = row++;
         panel.add(crearCampoCompacto("Observaciones", scrollObsCompacto), gbc);

         JPanel filaCosteo = new JPanel(new GridLayout(1, 2, 12, 0));
         filaCosteo.setOpaque(false);
         filaCosteo.add(crearCampoCompacto("Peso", txtPeso));
         filaCosteo.add(crearCampoCompacto("Precio por gramo", txtPrecioGramo));

         gbc.gridy = row;
         panel.add(filaCosteo, gbc);

        return panel;
    }

    private JPanel crearCabeceraFormulario() {
        JPanel cabecera = ModernUI.createTintedPanel(new BorderLayout(0, 6), new Color(244, 247, 255));
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titulo = new JLabel("Información principal");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(ModernUI.TEXT);

        JLabel subtitulo = new JLabel("Completa los datos base de la joya para calcular y guardar correctamente.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(ModernUI.MUTED);

        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(subtitulo, BorderLayout.CENTER);
        return cabecera;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = ModernUI.createRoundedPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(ModernUI.createSectionTitle("Resumen de costeo"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 1, 0, 8));
        body.setOpaque(false);

        JPanel totalCard = new ModernUI.RoundedPanel(new BorderLayout(0, 8), new Color(236, 248, 241), new Color(200, 232, 214), 24, 1, null, 0);
        totalCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel totalTitle = new JLabel("Precio total" );
        totalTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalTitle.setForeground(new Color(32, 86, 56));
        txtPrecioTotal.setFont(new Font("SansSerif", Font.BOLD, 30));
        txtPrecioTotal.setForeground(ModernUI.SUCCESS);
        totalCard.add(totalTitle, BorderLayout.NORTH);
        totalCard.add(txtPrecioTotal, BorderLayout.CENTER);

        body.add(totalCard);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private void ajustarAlturaCombo(JComboBox<?> combo, int altura) {
        if (combo == null) {
            return;
        }
        Dimension pref = combo.getPreferredSize();
        Dimension min = combo.getMinimumSize();
        combo.setPreferredSize(new Dimension(pref.width, altura));
        combo.setMinimumSize(new Dimension(min.width, altura));
    }

    private JPanel crearPanelPiedrasYNotas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel piedras = ModernUI.createRoundedPanel(new BorderLayout(0, 12));
        piedras.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        piedras.add(ModernUI.createSectionTitle("Piedras y trazabilidad"), BorderLayout.NORTH);
        JScrollPane scrollPiedras = ModernUI.wrapScroll(txtInfoPiedra);
        scrollPiedras.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 215, 236), 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        scrollPiedras.getViewport().setBackground(Color.WHITE);
        scrollPiedras.setPreferredSize(new Dimension(0, 180));
        piedras.add(scrollPiedras, BorderLayout.CENTER);
        JPanel accionesPiedra = new JPanel(new GridLayout(1, 2, 10, 0));
        accionesPiedra.setOpaque(false);
        accionesPiedra.add(siCheckBox);
        accionesPiedra.add(btnAgregarPiedraIndependiente);
        piedras.add(accionesPiedra, BorderLayout.SOUTH);

        panel.add(piedras, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.add(btnLimpiar);
        panel.add(btnGuardar);
        return panel;
    }

    private JPanel crearCampoCompacto(String label, JComponent field) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(ModernUI.TEXT);
        card.add(title, BorderLayout.NORTH);
        card.add(field, BorderLayout.CENTER);
        return card;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.28;
        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(ModernUI.TEXT);
        panel.add(title, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.72;
        panel.add(field, gbc);
    }

    private void configurarRadio(JRadioButton radio, String texto) {
        ModernUI.styleChip(radio, texto);
        radio.setOpaque(true);
        radio.addItemListener(e -> {
            boolean selected = radio.isSelected();
            radio.setBackground(selected ? new Color(223, 231, 255) : new Color(239, 243, 255));
            radio.setBorder(ModernUI.compoundBorder(selected ? new Color(124, 139, 224) : new Color(214, 223, 245), 8, 14, 8, 14));
        });
    }

    private void cargarImagenDecorativa() {
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/logoDiana.png"));
            Image scaled = originalIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            JLabel label = new JLabel(new ImageIcon(scaled));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            fotoPanel.setLayout(new BorderLayout());
            fotoPanel.add(label, BorderLayout.CENTER);
        } catch (Exception ignored) {
            fotoPanel.removeAll();
        }
    }

    private record PiedraCosteo(double peso, double precioPorQuilate, Long loteId, String loteNombre, String tipoPiedra) {
        double total() {
            return peso * precioPorQuilate;
        }
    }

    private record LoteOption(Long id, String nombre, double pesoDisponible) {
        @Override
        public String toString() {
            return "#" + id + " - " + nombre + " (disp: " + formatearNumero(pesoDisponible) + ")";
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

    private void mostrarInterfazJoya() {
        // Mostrar componentes de joya y ocultar el diálogo de lote
        mainPanel.setVisible(true);
    }

    private void abrirDialogoCrearLote() {
        // Obtener la ventana padre
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        
        // Crear y mostrar el diálogo de lote
        String puntoFisicoSeleccionado = null;
        if (session == null || session.isAdmin()) {
            if (puntoFisico != null && puntoFisico.getSelectedIndex() > 0 && puntoFisico.getSelectedItem() != null) {
                puntoFisicoSeleccionado = puntoFisico.getSelectedItem().toString();
            }
        }
        CrearLoteDialog dialogoLote = new CrearLoteDialog(frame, session, puntoFisicoSeleccionado);
        dialogoLote.setVisible(true);
        
        // Después de cerrar, volver a seleccionar Joya
        crearJoyaRadioButton.setSelected(true);
    }
}



