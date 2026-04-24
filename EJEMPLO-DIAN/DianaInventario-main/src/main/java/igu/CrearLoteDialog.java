package igu;

import logica.Controladora;
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
import java.text.SimpleDateFormat;
import java.util.List;

public class CrearLoteDialog extends JDialog {
    private JPanel mainPanel;
    private JTextField txtNombre;
    private JFormattedTextField txtPesoTotal;
    private JFormattedTextField txtCantidadPiedras;
    private JComboBox<String> cmbTipoPiedra;
    private JComboBox<String> cmbCalidadPiedra;
    private JTextArea txtDescripcion;
    private JFormattedTextField txtPrecioEstimado;
    private JComboBox<String> cmbSocio;
    private JComboBox<String> cmbPuntoFisico;
    private JTextArea txtObservaciones;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JLabel lblFechaCreacion;

    private final Controladora logicaController;
    private final SessionContext session;
    private final String puntoFisicoAdminSeleccionado;

    public CrearLoteDialog(JFrame parent, SessionContext session) {
        this(parent, session, null);
    }

    public CrearLoteDialog(JFrame parent, SessionContext session, String puntoFisicoAdminSeleccionado) {
        super(parent, "Crear Lote", true);
        this.session = session;
        this.puntoFisicoAdminSeleccionado = puntoFisicoAdminSeleccionado;
        this.logicaController = new Controladora();
        inicializarComponentes();
        configurarFormato();
        cargarDatosBD();
        configurarBotones();
        configurarVentana();
    }

    private void inicializarComponentes() {
        mainPanel = new JPanel(new BorderLayout(18, 18));
        mainPanel.setBackground(ModernUI.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        txtNombre = new JTextField();
        txtPesoTotal = new JFormattedTextField();
        txtCantidadPiedras = new JFormattedTextField();
        txtPrecioEstimado = new JFormattedTextField();
        cmbTipoPiedra = new JComboBox<>();
        cmbCalidadPiedra = new JComboBox<>();
        cmbSocio = new JComboBox<>();
        cmbPuntoFisico = new JComboBox<>();
        txtDescripcion = new JTextArea(5, 30);
        txtObservaciones = new JTextArea(5, 30);
        btnGuardar = new JButton();
        btnCancelar = new JButton();
        lblFechaCreacion = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));

        cmbTipoPiedra.addItem("Diamante");
        cmbTipoPiedra.addItem("Esmeralda");
        cmbTipoPiedra.addItem("Rubí");
        cmbTipoPiedra.addItem("Zafiro");
        cmbTipoPiedra.addItem("Topacio");
        cmbTipoPiedra.addItem("Otro");

        cmbCalidadPiedra.addItem("Premium");
        cmbCalidadPiedra.addItem("Alta");
        cmbCalidadPiedra.addItem("Media");
        cmbCalidadPiedra.addItem("Regular");

        aplicarEstiloVisual();

        JPanel header = new ModernUI.RoundedPanel(new BorderLayout(12, 12), ModernUI.PRIMARY, new Color(92, 108, 224), 30, 1, new Color(32, 42, 88, 34), 8);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel textHeader = new JPanel(new GridLayout(0, 1, 0, 6));
        textHeader.setOpaque(false);
        JLabel title = new JLabel("Crear lote de piedras ✨");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Organiza tus lotes con una vista más clara, moderna y elegante.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(228, 234, 255));
        textHeader.add(title);
        textHeader.add(subtitle);

        JPanel fechaChip = new ModernUI.RoundedPanel(new FlowLayout(FlowLayout.CENTER, 10, 8), new Color(245, 247, 255), new Color(214, 223, 245), 22, 1, null, 0);
        fechaChip.add(new JLabel("Creación: " + lblFechaCreacion.getText()));

        header.add(textHeader, BorderLayout.CENTER);
        header.add(fechaChip, BorderLayout.EAST);

        JPanel contenido = new JPanel(new GridLayout(1, 2, 18, 18));
        contenido.setOpaque(false);

        JPanel izquierda = ModernUI.createRoundedPanel(new BorderLayout(0, 14));
        izquierda.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        izquierda.add(ModernUI.createSectionTitle("Información del lote"), BorderLayout.NORTH);

        JPanel gridIzq = new JPanel(new GridBagLayout());
        gridIzq.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;
        agregarCampo(gridIzq, gbc, row++, "Nombre del lote", txtNombre);
        agregarCampo(gridIzq, gbc, row++, "Socio", cmbSocio);
        agregarCampo(gridIzq, gbc, row++, "Punto físico", cmbPuntoFisico);
        agregarCampo(gridIzq, gbc, row++, "Tipo de piedra", cmbTipoPiedra);
        agregarCampo(gridIzq, gbc, row++, "Calidad", cmbCalidadPiedra);

        JPanel fisico = new JPanel(new GridLayout(1, 2, 12, 0));
        fisico.setOpaque(false);
        fisico.add(crearCampoCompacto("Peso total (quilates)", txtPesoTotal));
        fisico.add(crearCampoCompacto("Cantidad de piedras", txtCantidadPiedras));
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gridIzq.add(fisico, gbc);

        JPanel precioPanel = crearCampoCompacto("Precio estimado", txtPrecioEstimado);
        gbc.gridy = row;
        gridIzq.add(precioPanel, gbc);
        izquierda.add(gridIzq, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, 16));
        derecha.setOpaque(false);

        JPanel descripcionCard = ModernUI.createRoundedPanel(new BorderLayout(0, 12));
        descripcionCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        descripcionCard.add(ModernUI.createSectionTitle("Descripción"), BorderLayout.NORTH);
        descripcionCard.add(ModernUI.wrapScroll(txtDescripcion), BorderLayout.CENTER);

        JPanel observacionesCard = ModernUI.createRoundedPanel(new BorderLayout(0, 12));
        observacionesCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        observacionesCard.add(ModernUI.createSectionTitle("Observaciones"), BorderLayout.NORTH);
        observacionesCard.add(ModernUI.wrapScroll(txtObservaciones), BorderLayout.CENTER);

        derecha.add(descripcionCard);
        derecha.add(observacionesCard);

        contenido.add(izquierda);
        contenido.add(derecha);

        JPanel acciones = new JPanel(new GridLayout(1, 2, 12, 0));
        acciones.setOpaque(false);
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);

        JScrollPane scroll = ModernUI.wrapScroll(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(ModernUI.BG);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(acciones, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void configurarFormato() {
        // Configurar símbolos personalizados
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('\'');
        symbols.setDecimalSeparator('.');

        // Formato para peso (double)
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.###", symbols);
        decimalFormat.setGroupingUsed(true);
        NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
        numberFormatter.setAllowsInvalid(true);
        numberFormatter.setCommitsOnValidEdit(true);
        numberFormatter.setOverwriteMode(false);
        numberFormatter.setValueClass(Double.class);
        txtPesoTotal.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
        txtPesoTotal.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

        // Formato para cantidad (entero, sin separador de miles)
        DecimalFormat intFormat = new DecimalFormat("#0", symbols);
        intFormat.setGroupingUsed(false);
        NumberFormatter intFormatter = new NumberFormatter(intFormat);
        intFormatter.setAllowsInvalid(false);
        intFormatter.setValueClass(Integer.class);
        txtCantidadPiedras.setFormatterFactory(new DefaultFormatterFactory(intFormatter));

        // Formato para precio
        txtPrecioEstimado.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
        txtPrecioEstimado.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        instalarMascaraPrecio(txtPrecioEstimado);
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

    private void cargarDatosBD() {
        try {
            cmbPuntoFisico.removeAllItems();
            cmbPuntoFisico.addItem("Seleccione punto físico...");
            for (String punto : PuntosFisicos.opciones()) {
                cmbPuntoFisico.addItem(punto);
            }

            // Cargar socios
            cmbSocio.removeAllItems();
            cmbSocio.addItem("Seleccione socio...");
            List<Socio> socios = logicaController.obtenerSocios(session);
            for (Socio socio : socios) {
                cmbSocio.addItem(socio.getNombre());
            }
            aplicarSocioPorDefecto();
            aplicarPuntoFisicoPorDefecto();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarPuntoFisicoPorDefecto() {
        if (cmbPuntoFisico == null || cmbPuntoFisico.getItemCount() == 0) {
            return;
        }

        if (session != null && !session.isAdmin()) {
            String puntoVendedor = session.puntoFisico();
            if (puntoVendedor != null && !puntoVendedor.isBlank()) {
                seleccionarPuntoFisico(cmbPuntoFisico, puntoVendedor.trim());
            }
            cmbPuntoFisico.setEnabled(false);
            return;
        }

        if (puntoFisicoAdminSeleccionado != null && !puntoFisicoAdminSeleccionado.isBlank()) {
            seleccionarPuntoFisico(cmbPuntoFisico, puntoFisicoAdminSeleccionado.trim());
            return;
        }

        cmbPuntoFisico.setSelectedIndex(0);
    }

    private void seleccionarPuntoFisico(JComboBox<String> combo, String valor) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(valor)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private void aplicarSocioPorDefecto() {
        if (cmbSocio == null || cmbSocio.getItemCount() == 0) {
            return;
        }

        for (int i = 0; i < cmbSocio.getItemCount(); i++) {
            String opcion = cmbSocio.getItemAt(i);
            if (opcion != null && "punto".equalsIgnoreCase(opcion.trim())) {
                cmbSocio.setSelectedIndex(i);
                return;
            }
        }

        // Si no existe el socio "punto", se mantiene el placeholder.
        cmbSocio.setSelectedIndex(0);
    }

    private void configurarBotones() {
        btnGuardar.addActionListener(e -> guardarLote());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void guardarLote() {
        try {
            // Validar campos obligatorios
            if (txtNombre.getText().trim().isEmpty()) {
                mostrarError("El nombre del lote no puede estar vacío.");
                return;
            }

            if (cmbSocio.getSelectedIndex() == 0) {
                mostrarError("Debe seleccionar un socio.");
                return;
            }

            if (cmbTipoPiedra.getSelectedItem() == null) {
                mostrarError("Debe seleccionar un tipo de piedra.");
                return;
            }

            // Obtener valores
            String nombre = txtNombre.getText().trim();
            double pesoTotal = obtenerDouble(txtPesoTotal);
            int cantidadPiedras = obtenerEntero(txtCantidadPiedras);
            String tipoPiedra = (String) cmbTipoPiedra.getSelectedItem();
            String calidadPiedra = (String) cmbCalidadPiedra.getSelectedItem();
            String descripcion = txtDescripcion.getText().trim();
            String precioEstimado = formatearNumero(obtenerDouble(txtPrecioEstimado));
            String socio = (String) cmbSocio.getSelectedItem();
            String observaciones = txtObservaciones.getText().trim();
            String puntoFisico = resolverPuntoFisicoCreacion();

            // Validaciones adicionales
            if (pesoTotal <= 0) {
                mostrarError("El peso total debe ser mayor a 0.");
                return;
            }

            if (cantidadPiedras <= 0) {
                mostrarError("La cantidad de piedras debe ser mayor a 0.");
                return;
            }

            // Crear el lote
            boolean creado = logicaController.crearLoteConAutorizacion(
                    session,
                    nombre,
                    pesoTotal,
                    cantidadPiedras,
                    tipoPiedra,
                    calidadPiedra,
                    descripcion,
                    precioEstimado,
                    socio,
                    null,
                    observaciones,
                    puntoFisico
            );

            String mensaje = (creado ? "Lote guardado correctamente:\n" : "Solicitud de lote enviada para aprobación:\n") +
                    "Nombre: " + nombre + "\n" +
                    "Peso Total: " + pesoTotal + " qt\n" +
                    "Cantidad de Piedras: " + cantidadPiedras + "\n" +
                    "Tipo de Piedra: " + tipoPiedra + "\n" +
                    "Socio: " + socio;

            JOptionPane.showMessageDialog(this, mensaje,
                    creado ? "Éxito" : "Solicitud Pendiente",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception ex) {
            mostrarError("Error al guardar el lote: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        java.net.URL iconUrl = getClass().getResource("/llora.png");
        Icon icon = null;
        if (iconUrl != null) {
            icon = new ImageIcon(new ImageIcon(iconUrl)
                    .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        }
        JOptionPane.showMessageDialog(this, mensaje, "Error de entrada", JOptionPane.ERROR_MESSAGE, icon);
    }

    private double obtenerDouble(JFormattedTextField field) {
        try {
            field.commitEdit();
            Object value = field.getValue();
            if (value == null) {
                String texto = field.getText();
                if (texto == null || texto.trim().isEmpty()) {
                    return 0.0;
                }
                return Double.parseDouble(texto.trim().replace("'", "").replace(',', '.'));
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString().replace("'", "").replace(',', '.'));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int obtenerEntero(JFormattedTextField field) {
        try {
            Object value = field.getValue();
            if (value == null) return 0;
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString().replace("'", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatearNumero(double numero) {
        return CargarDatos.formatearNumero(numero);
    }

    private String resolverPuntoFisicoCreacion() {
        String puntoSeleccionado = cmbPuntoFisico != null && cmbPuntoFisico.getSelectedItem() != null
                ? cmbPuntoFisico.getSelectedItem().toString().trim()
                : "";

        if (session != null && !session.isAdmin()) {
            String puntoVendedor = puntoSeleccionado.isBlank() || puntoSeleccionado.startsWith("Seleccione")
                    ? session.puntoFisico()
                    : puntoSeleccionado;
            if (puntoVendedor == null || puntoVendedor.isBlank()) {
                throw new IllegalArgumentException("El vendedor no tiene punto fisico asignado. Contacte a un administrador.");
            }
            return puntoVendedor.trim();
        }

        if (!puntoSeleccionado.isBlank() && !puntoSeleccionado.startsWith("Seleccione")) {
            return puntoSeleccionado;
        }
        if (puntoFisicoAdminSeleccionado != null && !puntoFisicoAdminSeleccionado.isBlank()) {
            return puntoFisicoAdminSeleccionado.trim();
        }
        
        if (puntoSeleccionado.isBlank() || puntoSeleccionado.startsWith("Seleccione")) {
            throw new IllegalArgumentException("Debe ingresar un punto fisico para guardar el lote.");
        }
        return puntoSeleccionado;
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 720));
        setSize(1180, 820);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void aplicarEstiloVisual() {
        ModernUI.styleTextField(txtNombre);
        ModernUI.styleTextField(txtPesoTotal);
        ModernUI.styleTextField(txtCantidadPiedras);
        ModernUI.styleTextField(txtPrecioEstimado);
        ModernUI.styleCombo(cmbSocio);
        ModernUI.styleCombo(cmbPuntoFisico);
        ModernUI.styleCombo(cmbTipoPiedra);
        ModernUI.styleCombo(cmbCalidadPiedra);
        ModernUI.styleTextArea(txtDescripcion);
        ModernUI.styleTextArea(txtObservaciones);
        ModernUI.styleSecondaryButton(btnCancelar, "Cancelar");
        ModernUI.stylePrimaryButton(btnGuardar, "Guardar lote");

        // Campos un poco más altos para mejorar legibilidad en el formulario de lote.
        ajustarAlturaCampo(txtNombre, 40);
        ajustarAlturaCampo(txtPesoTotal, 40);
        ajustarAlturaCampo(txtCantidadPiedras, 40);
        ajustarAlturaCampo(txtPrecioEstimado, 40);
        ajustarAlturaCampo(cmbSocio, 40);
        ajustarAlturaCampo(cmbPuntoFisico, 40);
        ajustarAlturaCampo(cmbTipoPiedra, 40);
        ajustarAlturaCampo(cmbCalidadPiedra, 40);

        lblFechaCreacion.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblFechaCreacion.setForeground(ModernUI.TEXT);
    }

    private void ajustarAlturaCampo(JComponent componente, int altura) {
        if (componente == null) {
            return;
        }
        Dimension pref = componente.getPreferredSize();
        Dimension min = componente.getMinimumSize();
        componente.setPreferredSize(new Dimension(pref.width, altura));
        componente.setMinimumSize(new Dimension(min.width, altura));
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.32;
        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(ModernUI.TEXT);
        panel.add(title, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.68;
        panel.add(field, gbc);
    }

    private JPanel crearCampoCompacto(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(ModernUI.TEXT);
        panel.add(title, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }
}

