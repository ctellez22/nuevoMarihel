package igu;

import logica.Controladora;
import logica.Joyero;
import logica.Joya;
import logica.OrdenTrabajo;
import org.example.SessionContext;
import persistencia.ControladoraPersistencia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdenTrabajoPanel {

    private final JPanel mainPanel;
    private final Controladora controladora;
    private final ControladoraPersistencia persistencia;
    private final SessionContext session;

    private final JTextField txtFiltroJoya;
    private final JComboBox<JoyaOption> cmbJoya;
    private final JComboBox<String> cmbJoyero;
    private final JComboBox<String> cmbFiltroSede;
    private final JSpinner spFechaEntrega;
    private final JTextArea txtDetalle;
    private final JButton btnGuardar;
    private final DefaultTableModel tableModel;
    private final List<JoyaOption> joyasDisponiblesCache = new ArrayList<>();

    public OrdenTrabajoPanel(JFrame parent, SessionContext session) {
        this.controladora = new Controladora();
        this.persistencia = new ControladoraPersistencia();
        this.session = session;

        this.mainPanel = new JPanel(new BorderLayout(14, 14));
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        this.mainPanel.setBackground(ModernUI.BG);

        JPanel header = crearTarjeta(new BorderLayout(10, 10));
        header.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitulo = new JLabel("Orden de trabajo");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblTitulo.setForeground(ModernUI.TEXT);

        JLabel lblSubtitulo = new JLabel("Asigna joyas ya creadas a un joyero y marca su estado como pendiente.");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtitulo.setForeground(ModernUI.MUTED);

        JPanel titulos = new JPanel(new GridLayout(0, 1, 0, 6));
        titulos.setOpaque(false);
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        header.add(titulos, BorderLayout.WEST);

        JPanel formularioCard = crearTarjeta(new BorderLayout(12, 12));
        formularioCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        formularioCard.setPreferredSize(new Dimension(640, 900));
        formularioCard.setMinimumSize(new Dimension(560, 800));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        txtFiltroJoya = new JTextField();
        ModernUI.styleTextField(txtFiltroJoya);
        txtFiltroJoya.setPreferredSize(new Dimension(580, 60));

        cmbJoya = new JComboBox<>();
        ModernUI.styleCombo(cmbJoya);
        cmbJoya.setPreferredSize(new Dimension(100, 62));
        cmbJoya.setMaximumRowCount(16);

        cmbJoyero = new JComboBox<>();
        ModernUI.styleCombo(cmbJoyero);
        cmbJoyero.setPreferredSize(new Dimension(100, 62));
        cmbJoyero.setMaximumRowCount(12);

        cmbFiltroSede = new JComboBox<>();
        ModernUI.styleCombo(cmbFiltroSede);
        cmbFiltroSede.setPreferredSize(new Dimension(270, 44));

        spFechaEntrega = crearSpinnerFecha(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));

        txtDetalle = new JTextArea(14, 40);
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);
        ModernUI.styleTextArea(txtDetalle);
        JScrollPane scrollDetalle = ModernUI.wrapScroll(txtDetalle);
        scrollDetalle.setPreferredSize(new Dimension(100, 520));

        int row = 0;
        gbc.gridy = row++;
        formulario.add(crearCampo("Filtrar joya por ID", txtFiltroJoya), gbc);
        gbc.gridy = row++;
        formulario.add(crearCampo("Joya para la orden", cmbJoya), gbc);
        gbc.gridy = row++;
        formulario.add(crearCampo("Joyero", cmbJoyero), gbc);

        JPanel fechas = new JPanel(new GridLayout(1, 1, 10, 0));
        fechas.setOpaque(false);
        spFechaEntrega.setPreferredSize(new Dimension(200, 60));
        fechas.add(crearCampo("Fecha entrega", spFechaEntrega));
        gbc.gridy = row++;
        formulario.add(fechas, gbc);

        JPanel accesosFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accesosFecha.setOpaque(false);
        JButton btnHoy = new JButton("Hoy");
        JButton btn7Dias = new JButton("+7 días");
        JButton btn15Dias = new JButton("+15 días");
        ModernUI.styleSecondaryButton(btnHoy, "Hoy");
        ModernUI.styleSecondaryButton(btn7Dias, "+7 días");
        ModernUI.styleSecondaryButton(btn15Dias, "+15 días");
        accesosFecha.add(btnHoy);
        accesosFecha.add(btn7Dias);
        accesosFecha.add(btn15Dias);
        gbc.gridy = row++;
        formulario.add(accesosFecha, gbc);

        gbc.gridy = row;
        formulario.add(crearCampo("Detalle", scrollDetalle), gbc);

        btnGuardar = new JButton("Crear orden");
        JButton btnRefrescar = new JButton("Refrescar");
        ModernUI.stylePrimaryButton(btnGuardar, "Crear orden");
        ModernUI.styleSecondaryButton(btnRefrescar, "Refrescar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        btnRefrescar.setPreferredSize(new Dimension(130, 46));
        btnGuardar.setPreferredSize(new Dimension(150, 46));
        acciones.add(btnRefrescar);
        acciones.add(btnGuardar);

        formularioCard.add(formulario, BorderLayout.CENTER);
        formularioCard.add(acciones, BorderLayout.SOUTH);

        String[] cols = {"OT", "Joya", "Joyero", "Punto", "Fecha envío", "Fecha entrega", "Estado", "Creada"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(tableModel);
        tabla.setRowHeight(30);
        tabla.getTableHeader().setBackground(ModernUI.PRIMARY);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        JScrollPane scrollTabla = ModernUI.wrapScroll(tabla);
        scrollTabla.setPreferredSize(new Dimension(100, 170));

        JPanel tablaCard = crearTarjeta(new BorderLayout(10, 10));
        tablaCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        tablaCard.setPreferredSize(new Dimension(780, 260));
        tablaCard.setMinimumSize(new Dimension(620, 240));
        JLabel lblTabla = new JLabel("Órdenes registradas");
        lblTabla.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTabla.setForeground(ModernUI.TEXT);

        JPanel tablaHeader = new JPanel(new BorderLayout(10, 0));
        tablaHeader.setOpaque(false);
        tablaHeader.add(lblTabla, BorderLayout.WEST);

        JPanel filtroSedePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtroSedePanel.setOpaque(false);
        JLabel lblSede = new JLabel("Sede:");
        lblSede.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSede.setForeground(ModernUI.MUTED);
        filtroSedePanel.add(lblSede);
        filtroSedePanel.add(cmbFiltroSede);
        tablaHeader.add(filtroSedePanel, BorderLayout.EAST);

        tablaCard.add(tablaHeader, BorderLayout.NORTH);
        tablaCard.add(scrollTabla, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formularioCard, tablaCard);
        split.setResizeWeight(0.45);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(split, BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> guardarOrden());
        btnRefrescar.addActionListener(e -> cargarDatos());
        btnHoy.addActionListener(e -> spFechaEntrega.setValue(new Date()));
        btn7Dias.addActionListener(e -> spFechaEntrega.setValue(Date.from(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        btn15Dias.addActionListener(e -> spFechaEntrega.setValue(Date.from(LocalDate.now().plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        cmbFiltroSede.addActionListener(e -> cargarOrdenes());
        txtFiltroJoya.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltroJoyas();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltroJoyas();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltroJoyas();
            }
        });

        cargarDatos();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void cargarDatos() {
        cargarJoyasDisponibles();
        cargarJoyerosDisponibles();
        cargarFiltroSede();
        cargarOrdenes();
    }

    private void cargarFiltroSede() {
        String seleccionActual = cmbFiltroSede.getSelectedItem() == null ? null : cmbFiltroSede.getSelectedItem().toString();
        cmbFiltroSede.removeAllItems();

        String puntoRestringido = obtenerPuntoFisicoRestringido();
        if (puntoRestringido != null) {
            cmbFiltroSede.addItem(puntoRestringido);
            cmbFiltroSede.setSelectedIndex(0);
            cmbFiltroSede.setEnabled(false);
            return;
        }

        cmbFiltroSede.addItem("Todas");
        for (String sede : logica.PuntosFisicos.opciones()) {
            cmbFiltroSede.addItem(sede);
        }
        cmbFiltroSede.setEnabled(true);

        if (seleccionActual != null) {
            cmbFiltroSede.setSelectedItem(seleccionActual);
        }
        if (cmbFiltroSede.getSelectedIndex() < 0) {
            cmbFiltroSede.setSelectedIndex(0);
        }
    }

    private void cargarJoyerosDisponibles() {
        String seleccionado = cmbJoyero.getSelectedItem() == null ? null : cmbJoyero.getSelectedItem().toString();
        cmbJoyero.removeAllItems();
        cmbJoyero.addItem("Seleccione joyero...");

        List<Joyero> joyeros = controladora.obtenerJoyeros(session);
        for (Joyero joyero : joyeros) {
            if (joyero == null || joyero.getNombre() == null || joyero.getNombre().isBlank()) {
                continue;
            }
            cmbJoyero.addItem(joyero.getNombre().trim());
        }

        // Útil cuando el usuario trabaja siempre con su propio nombre.
        if (session != null && session.username() != null && !session.username().isBlank()) {
            String usuarioSesion = session.username().trim();
            boolean existe = false;
            for (int i = 0; i < cmbJoyero.getItemCount(); i++) {
                String item = cmbJoyero.getItemAt(i);
                if (item != null && item.equalsIgnoreCase(usuarioSesion)) {
                    existe = true;
                    break;
                }
            }
            if (!usuarioSesion.isBlank() && !existe) {
                cmbJoyero.addItem(usuarioSesion);
            }
        }

        if (seleccionado != null) {
            cmbJoyero.setSelectedItem(seleccionado);
        }
        if (cmbJoyero.getSelectedIndex() < 0) {
            cmbJoyero.setSelectedIndex(0);
        }
    }

    private void cargarJoyasDisponibles() {
        joyasDisponiblesCache.clear();
        cmbJoya.removeAllItems();
        List<Joya> joyas = controladora.obtenerTodasLasJoyas(session);

        for (Joya joya : joyas) {
            if (joya == null || joya.getId() == null || joya.isVendido()) {
                continue;
            }
            String estado = joya.getEstado() == null ? "" : joya.getEstado().trim();
            if ("pendiente".equalsIgnoreCase(estado)) {
                continue;
            }
            String display = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank())
                    ? joya.getDisplayId().trim()
                    : String.valueOf(joya.getId());
            joyasDisponiblesCache.add(new JoyaOption(joya.getId(), display, joya.getNombre() == null ? "" : joya.getNombre().trim()));
        }

        aplicarFiltroJoyas();
        boolean hayJoyas = !joyasDisponiblesCache.isEmpty();
        btnGuardar.setEnabled(hayJoyas);
    }

    private void aplicarFiltroJoyas() {
        Object seleccion = cmbJoya.getSelectedItem();
        Long idSeleccionada = (seleccion instanceof JoyaOption j && j.id() != null) ? j.id() : null;

        cmbJoya.removeAllItems();
        cmbJoya.addItem(new JoyaOption(null, "", "Seleccione joya..."));
        String filtro = txtFiltroJoya.getText() == null ? "" : txtFiltroJoya.getText().trim().toLowerCase();

        for (JoyaOption joya : joyasDisponiblesCache) {
            if (filtro.isBlank()) {
                cmbJoya.addItem(joya);
                continue;
            }

            String idDisplay = joya.displayId() == null ? "" : joya.displayId().toLowerCase();
            String nombre = joya.nombre() == null ? "" : joya.nombre().toLowerCase();
            String idNumerico = joya.id() == null ? "" : String.valueOf(joya.id());
            if (idDisplay.contains(filtro) || idNumerico.contains(filtro) || nombre.contains(filtro)) {
                cmbJoya.addItem(joya);
            }
        }

        if (cmbJoya.getItemCount() == 1) {
            cmbJoya.removeAllItems();
            cmbJoya.addItem(new JoyaOption(null, "", "Sin resultados para el filtro"));
            return;
        }

        if (idSeleccionada != null) {
            for (int i = 0; i < cmbJoya.getItemCount(); i++) {
                JoyaOption item = cmbJoya.getItemAt(i);
                if (item != null && idSeleccionada.equals(item.id())) {
                    cmbJoya.setSelectedIndex(i);
                    return;
                }
            }
        }
        cmbJoya.setSelectedIndex(0);
    }

    private void cargarOrdenes() {
        tableModel.setRowCount(0);
        List<OrdenTrabajo> ordenes = persistencia.obtenerOrdenesTrabajo(resolverFiltroSedeOrdenes());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (OrdenTrabajo orden : ordenes) {
            String joyaRef = (orden.getJoyaDisplayId() == null || orden.getJoyaDisplayId().isBlank())
                    ? "#" + orden.getJoyaId()
                    : orden.getJoyaDisplayId();
            String joyaTexto = joyaRef + (orden.getJoyaNombre() == null || orden.getJoyaNombre().isBlank() ? "" : " - " + orden.getJoyaNombre());

            tableModel.addRow(new Object[]{
                    orden.getId(),
                    joyaTexto,
                    orden.getJoyero(),
                    (orden.getPuntoFisico() == null || orden.getPuntoFisico().isBlank()) ? "-" : orden.getPuntoFisico(),
                    orden.getFechaEnvio() == null ? "-" : orden.getFechaEnvio().format(formatter),
                    orden.getFechaEntrega() == null ? "-" : orden.getFechaEntrega().format(formatter),
                    orden.getEstado(),
                    orden.getCreadoEn() == null ? "-" : orden.getCreadoEn().format(formatter)
            });
        }
    }

    private void guardarOrden() {
        Object seleccionJoya = cmbJoya.getSelectedItem();
        if (!(seleccionJoya instanceof JoyaOption joya) || joya.id() == null) {
            JOptionPane.showMessageDialog(mainPanel, "Debe seleccionar una joya válida.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object seleccionJoyero = cmbJoyero.getSelectedItem();
        String joyero = seleccionJoyero == null ? "" : seleccionJoyero.toString().trim();
        if (joyero.isBlank() || "Seleccione joyero...".equalsIgnoreCase(joyero)) {
            JOptionPane.showMessageDialog(mainPanel, "Debe indicar el joyero responsable.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fechaEntregaLocal = toLocalDate(spFechaEntrega);
        LocalDateTime fechaEntrega = fechaEntregaLocal.atStartOfDay();
        LocalDateTime fechaEnvio = LocalDateTime.now();
        if (fechaEntregaLocal.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(mainPanel, "La fecha de entrega no puede ser anterior a hoy.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String detalle = txtDetalle.getText() == null ? "" : txtDetalle.getText().trim();

        try {
            OrdenTrabajo orden = controladora.crearOrdenTrabajo(session, joya.id(), joyero, fechaEnvio, fechaEntrega, detalle);

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Orden creada para la joya seleccionada.\nQuedó en estado pendiente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

            txtDetalle.setText("");
            cmbJoya.setSelectedIndex(0);
            cmbJoyero.setSelectedIndex(0);
            spFechaEntrega.setValue(Date.from(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cargarDatos();

            if (orden != null && orden.getId() != null) {
                mainPanel.putClientProperty("ultimaOrdenId", orden.getId());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No se pudo crear la orden de trabajo: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private LocalDateTime toLocalDateTime(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private JSpinner crearSpinnerFecha(Date initialDate) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(initialDate, null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        return spinner;
    }

    private LocalDate toLocalDate(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JPanel crearCampo(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(ModernUI.TEXT);
        if (!(field instanceof JScrollPane)) {
            Dimension pref = field.getPreferredSize();
            int alto = Math.max(pref.height, 60);
            field.setPreferredSize(new Dimension(pref.width, alto));
        }
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearTarjeta(LayoutManager layout) {
        return new ModernUI.RoundedPanel(
                layout,
                ModernUI.SURFACE,
                new Color(198, 209, 230),
                30,
                2,
                new Color(20, 30, 64, 28),
                10
        );
    }

    private String obtenerPuntoFisicoRestringido() {
        if (session == null || session.isAdmin()) {
            return null;
        }
        String punto = session.puntoFisico();
        if (punto == null || punto.isBlank()) {
            return "";
        }
        return punto.trim();
    }

    private String resolverFiltroSedeOrdenes() {
        String restringido = obtenerPuntoFisicoRestringido();
        if (restringido != null) {
            return restringido;
        }

        Object seleccion = cmbFiltroSede.getSelectedItem();
        if (seleccion == null) {
            return null;
        }
        String sede = seleccion.toString().trim();
        if (sede.isBlank() || "Todas".equalsIgnoreCase(sede)) {
            return null;
        }
        return sede;
    }

    private record JoyaOption(Long id, String displayId, String nombre) {
        @Override
        public String toString() {
            if (id == null) {
                return nombre;
            }
            return displayId + " - " + nombre;
        }
    }
}

