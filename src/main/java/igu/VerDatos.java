package igu;

import logica.Controladora;
import logica.Categoria;
import logica.CambioPendiente;
import logica.Joya;
import logica.Socio;
import org.example.SessionContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerDatos {
    private JPanel mainPanel;
    private JCheckBox idCheckBox;
    private JTextField txtId;
    private JCheckBox categoriaCheckBox;
    private JComboBox<String> comboBoxCategoria;
    private JCheckBox socioCheckBox;       // null intencional — no se usa en esta versión del form
    private JComboBox<String> comboBoxSocio; // null intencional
    private JCheckBox nombreCheckBox;
    private JTextField txtNombre;
    private JList<Joya> lstResultados;
    private JPanel panelFoto;
    private JCheckBox noVendidoCheckBox;
    private JComboBox<String> cmbEstado;
    private JCheckBox noAnuladoCheckBox;   // null intencional — se ocultaba en el form original
    private DefaultListModel<Joya> listModel;

    private Controladora controladora;
    private Timer filtroTimer;
    private SwingWorker<List<Joya>, Void> filtroWorker;
    private boolean avisoFiltroEstadoMostrado;
    private final SessionContext session;

    public VerDatos(JFrame parent) {
        this(parent, null);
    }

    public VerDatos(JFrame parent, SessionContext session) {
        this.session = session;
        construirUI();
        inicializarTodo();
        programarActualizacionLista();
    }

    // ── Construcción de la UI en código puro ─────────────────────────────────

    private void construirUI() {
        mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBackground(UITheme.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Título
        JLabel titleLabel = new JLabel("Inventario:", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.F_TITLE);
        titleLabel.setForeground(UITheme.TEXT);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ── Barra de filtros ─────────────────────────────────────────────────
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterPanel.setBackground(UITheme.BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.roundedBorder(UITheme.BORDER, 12),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        idCheckBox = new JCheckBox("id:", true);
        idCheckBox.setFont(UITheme.F_FILTER);
        idCheckBox.setForeground(UITheme.TEXT);
        idCheckBox.setBackground(UITheme.BG);

        txtId = UITheme.styledField();
        txtId.setPreferredSize(new Dimension(80, 28));
        filterPanel.add(idCheckBox);
        filterPanel.add(txtId);

        filterPanel.add(new JSeparator(JSeparator.VERTICAL));

        categoriaCheckBox = new JCheckBox("Categoría");
        categoriaCheckBox.setFont(UITheme.F_FILTER);
        categoriaCheckBox.setForeground(UITheme.TEXT);
        categoriaCheckBox.setBackground(UITheme.BG);
        comboBoxCategoria = new JComboBox<>();
        comboBoxCategoria.addItem("Todos");
        UITheme.styleCombo(comboBoxCategoria);
        filterPanel.add(categoriaCheckBox);
        filterPanel.add(comboBoxCategoria);

        filterPanel.add(new JSeparator(JSeparator.VERTICAL));

        nombreCheckBox = new JCheckBox("Nombre");
        nombreCheckBox.setFont(UITheme.F_FILTER);
        nombreCheckBox.setForeground(UITheme.TEXT);
        nombreCheckBox.setBackground(UITheme.BG);
        txtNombre = UITheme.styledField();
        txtNombre.setPreferredSize(new Dimension(100, 28));
        filterPanel.add(nombreCheckBox);
        filterPanel.add(txtNombre);

        filterPanel.add(new JSeparator(JSeparator.VERTICAL));

        noVendidoCheckBox = new JCheckBox("No incluir vendido", true);
        noVendidoCheckBox.setFont(UITheme.F_FILTER);
        noVendidoCheckBox.setForeground(UITheme.TEXT);
        noVendidoCheckBox.setBackground(UITheme.BG);
        filterPanel.add(noVendidoCheckBox);

        filterPanel.add(new JSeparator(JSeparator.VERTICAL));

        cmbEstado = new JComboBox<>();
        UITheme.styleCombo(cmbEstado);
        JLabel estadoLabel = new JLabel("Estado:");
        estadoLabel.setFont(UITheme.F_LABEL);
        estadoLabel.setForeground(UITheme.TEXT);
        filterPanel.add(estadoLabel);
        filterPanel.add(cmbEstado);

        // ── Área central: foto + lista ───────────────────────────────────────
        panelFoto = new JPanel(new BorderLayout());
        panelFoto.setBackground(UITheme.BG);
        panelFoto.setPreferredSize(new Dimension(310, 310));

        listModel = new DefaultListModel<>();
        lstResultados = new JList<>(listModel);
        lstResultados.setFont(UITheme.F_BODY);
        lstResultados.setBackground(UITheme.BG);
        lstResultados.setSelectionBackground(UITheme.ACCENT);
        lstResultados.setSelectionForeground(Color.WHITE);
        lstResultados.setFixedCellHeight(100);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                panelFoto,
                UITheme.styledScroll(lstResultados)
        );
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.22);
        splitPane.setBackground(UITheme.BG);
        splitPane.setDividerSize(1);

        JPanel centerArea = new JPanel(new BorderLayout(4, 4));
        centerArea.setBackground(UITheme.BG);
        centerArea.add(filterPanel, BorderLayout.NORTH);
        centerArea.add(splitPane, BorderLayout.CENTER);

        mainPanel.add(centerArea, BorderLayout.CENTER);
    }

    // ── Inicialización de lógica ──────────────────────────────────────────────

    private void inicializarTodo() {
        controladora = new Controladora(session);

        // noAnuladoCheckBox es null (no está en la UI), el código ya lo null-chequea

        inicializarOpcionesDeFiltro();

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/gestion-de-materiales.png"));
            Image scaled = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaled));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panelFoto.add(imageLabel, BorderLayout.CENTER);
        } catch (Exception ignored) {}

        lstResultados.setModel(listModel);
        lstResultados.setCellRenderer(new JoyaListCellRenderer());

        lstResultados.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lstResultados.getSelectedValue() != null) {
                mostrarDetalles(lstResultados.getSelectedValue());
            }
        });

        filtroTimer = new Timer(300, e -> ejecutarFiltro(false));
        filtroTimer.setRepeats(false);

        cargarFiltrosDesdeBD();
        agregarListeners();
    }

    private void inicializarOpcionesDeFiltro() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Todos");
        model.addElement("Estado: Disponible");
        model.addElement("Estado: Anulado");
        model.addElement("Estado: Prestado");
        model.addElement("Autorizacion: Aprobado");
        model.addElement("Autorizacion: Pendiente");
        cmbEstado.setModel(model);
    }

    private void cargarFiltrosDesdeBD() {
        comboBoxCategoria.removeAllItems();
        comboBoxCategoria.addItem("Todos");
        for (Categoria categoria : controladora.obtenerCategorias()) {
            comboBoxCategoria.addItem(categoria.getNombre());
        }

        if (comboBoxSocio == null) {
            return;
        }
        comboBoxSocio.removeAllItems();
        comboBoxSocio.addItem("Todos");
        for (Socio socio : controladora.obtenerSocios()) {
            comboBoxSocio.addItem(socio.getNombre());
        }
    }

    private void agregarListeners() {
        txtId.addActionListener(e -> {
            if (idCheckBox.isSelected() && !txtId.getText().isEmpty()) {
                actualizarListaFiltrada();
            }
        });

        comboBoxCategoria.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                programarActualizacionLista();
            }
        });

        if (comboBoxSocio != null) {
            comboBoxSocio.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    programarActualizacionLista();
                }
            });
        }

        txtNombre.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { programarActualizacionLista(); }
            @Override public void removeUpdate(DocumentEvent e) { programarActualizacionLista(); }
            @Override public void changedUpdate(DocumentEvent e) { programarActualizacionLista(); }
        });

        noVendidoCheckBox.addActionListener(e -> programarActualizacionLista());
        idCheckBox.addActionListener(e -> programarActualizacionLista());
        categoriaCheckBox.addActionListener(e -> programarActualizacionLista());
        if (socioCheckBox != null) {
            socioCheckBox.addActionListener(e -> programarActualizacionLista());
        }
        nombreCheckBox.addActionListener(e -> programarActualizacionLista());

        cmbEstado.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                programarActualizacionLista();
            }
        });
    }

    // ── Lógica de filtrado (sin cambios) ─────────────────────────────────────

    public void actualizarListaFiltradaa() {
        actualizarListaFiltrada();
    }

    public void actualizarListaFiltrada() {
        ejecutarFiltro(true);
    }

    private void programarActualizacionLista() {
        if (filtroTimer != null) {
            filtroTimer.restart();
        }
    }

    private void ejecutarFiltro(boolean mostrarAviso) {
        boolean filterById = idCheckBox.isSelected() && !txtId.getText().isEmpty();
        boolean filterByCategory = categoriaCheckBox.isSelected()
                && comboBoxCategoria.getSelectedItem() != null
                && !comboBoxCategoria.getSelectedItem().toString().equalsIgnoreCase("Todos");
        boolean filterBySocio = socioCheckBox != null && comboBoxSocio != null
                && socioCheckBox.isSelected()
                && comboBoxSocio.getSelectedItem() != null
                && !comboBoxSocio.getSelectedItem().toString().equalsIgnoreCase("Todos");
        boolean filterByName = nombreCheckBox.isSelected() && !txtNombre.getText().isEmpty();
        boolean filterByNoVendido = noVendidoCheckBox.isSelected();
        String seleccionEstado = cmbEstado.getSelectedItem() == null ? "Todos" : cmbEstado.getSelectedItem().toString();
        String estadoOperativoSeleccionado = mapearEstadoOperativo(seleccionEstado);
        boolean filterByAutorizado = "Autorizacion: Aprobado".equalsIgnoreCase(seleccionEstado);
        boolean filterByPendienteAutorizacion = "Autorizacion: Pendiente".equalsIgnoreCase(seleccionEstado);
        boolean filterByEstado = estadoOperativoSeleccionado != null;
        boolean filterByAutorizacionTodos = !filterByAutorizado && !filterByPendienteAutorizacion && !filterByEstado;

        if (filterByEstado && !filterById && !filterByCategory && !filterBySocio && !filterByName && !filterByNoVendido) {
            cancelarFiltroEnCurso();
            listModel.clear();
            if (mostrarAviso && !avisoFiltroEstadoMostrado) {
                JOptionPane.showMessageDialog(mainPanel, "Por favor, seleccione más filtros para realizar la búsqueda.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
            avisoFiltroEstadoMostrado = true;
            return;
        }

        avisoFiltroEstadoMostrado = false;

        List<String> estado = null;
        if (filterByEstado) {
            estado = new ArrayList<>();
            estado.add(estadoOperativoSeleccionado);
        }

        String id = txtId.getText();
        String categoria = (String) comboBoxCategoria.getSelectedItem();
        String socio = (comboBoxSocio != null) ? (String) comboBoxSocio.getSelectedItem() : null;
        String nombre = txtNombre.getText();
        List<String> estadoFinal = estado;

        cancelarFiltroEnCurso();
        filtroWorker = new SwingWorker<>() {
            @Override
            protected List<Joya> doInBackground() {
                List<Joya> resultado = controladora.filtrarJoyas(
                        filterById, id,
                        filterByCategory, categoria,
                        filterBySocio, socio,
                        filterByName, nombre,
                        filterByNoVendido,
                        estadoFinal
                );

                if (filterByAutorizado) {
                    return resultado.stream().filter(Joya::isAutorizado).toList();
                }
                if (filterByPendienteAutorizacion) {
                    List<Joya> pendientesBD = resultado.stream().filter(j -> !j.isAutorizado()).toList();
                    List<Joya> pendientesInsert = construirJoyasPendientesInsert(
                            filterById, id, filterByCategory, categoria,
                            filterBySocio, socio, filterByName, nombre,
                            filterByNoVendido, estadoFinal);
                    List<Joya> combinado = new ArrayList<>(pendientesBD);
                    combinado.addAll(pendientesInsert);
                    return combinado;
                }
                if (filterByAutorizacionTodos) {
                    List<Joya> pendientesInsert = construirJoyasPendientesInsert(
                            filterById, id, filterByCategory, categoria,
                            filterBySocio, socio, filterByName, nombre,
                            filterByNoVendido, estadoFinal);
                    if (!pendientesInsert.isEmpty()) {
                        List<Joya> combinado = new ArrayList<>(resultado);
                        combinado.addAll(pendientesInsert);
                        return combinado;
                    }
                }
                return resultado;
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    actualizarLista(get());
                } catch (CancellationException ignored) {
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error al cargar resultados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        filtroWorker.execute();
    }

    private void cancelarFiltroEnCurso() {
        if (filtroWorker != null && !filtroWorker.isDone()) {
            filtroWorker.cancel(true);
        }
    }

    private void actualizarLista(List<Joya> joyas) {
        listModel.clear();
        for (Joya joya : joyas) {
            listModel.addElement(joya);
        }
    }

    private void mostrarDetalles(Joya joya) {
        if (!joya.isAutorizado() && joya.getId() != null && joya.getId() < 0) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Esta joya aún no existe en inventario oficial; está en solicitud pendiente de aprobación.",
                    "Pendiente de aprobación",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        JFrame frame = new JFrame("Detalles de Joya");
        DetallesJoya detallesPanel = new DetallesJoya(joya, this, session);
        frame.setContentPane(detallesPanel.getMainPanel());
        frame.setSize(650, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    // ── Helpers (sin cambios) ─────────────────────────────────────────────────

    private List<Joya> construirJoyasPendientesInsert(boolean filterById, String id,
                                                       boolean filterByCategory, String categoria,
                                                       boolean filterBySocio, String socio,
                                                       boolean filterByName, String nombre,
                                                       boolean filterByNoVendido, List<String> estadoFinal) {
        List<CambioPendiente> pendientes = controladora.obtenerPendientesJoya();
        List<Joya> salida = new ArrayList<>();
        for (CambioPendiente pendiente : pendientes) {
            if (!"INSERT".equalsIgnoreCase(pendiente.getOperacion())) continue;
            Joya joya = mapearPendienteInsertAJoya(pendiente);
            if (!cumpleFiltrosPendiente(joya, filterById, id, filterByCategory, categoria,
                    filterBySocio, socio, filterByName, nombre, filterByNoVendido, estadoFinal)) continue;
            salida.add(joya);
        }
        return salida;
    }

    private Joya mapearPendienteInsertAJoya(CambioPendiente pendiente) {
        String json = pendiente.getAfterJson();
        Joya joya = new Joya();
        joya.setId(-Math.abs(pendiente.getId()));
        joya.setDisplayId("P" + pendiente.getId());
        joya.setNombre(extraerTexto(json, "nombre"));
        joya.setPrecio(extraerTexto(json, "precio"));
        joya.setPeso(extraerNumero(json, "peso", 0d));
        joya.setCategoria(extraerTexto(json, "categoria"));
        joya.setSocio(extraerTexto(json, "socio"));
        joya.setObservacion(extraerTexto(json, "observacion"));
        joya.setTienePiedra(extraerBoolean(json, "tienePiedra", false));
        joya.setInfoPiedra(extraerTexto(json, "infoPiedra"));
        joya.setVendido(extraerBoolean(json, "vendido", false));
        joya.setEstado(extraerTexto(json, "estado"));
        joya.setAutorizado(false);
        return joya;
    }

    private boolean cumpleFiltrosPendiente(Joya joya, boolean filterById, String id,
                                            boolean filterByCategory, String categoria,
                                            boolean filterBySocio, String socio,
                                            boolean filterByName, String nombre,
                                            boolean filterByNoVendido, List<String> estadoFinal) {
        if (filterById) {
            String buscado = id == null ? "" : id.trim();
            String display = joya.getDisplayId() == null ? "" : joya.getDisplayId();
            String idNumerico = joya.getId() == null ? "" : String.valueOf(Math.abs(joya.getId()));
            if (!display.equalsIgnoreCase(buscado) && !idNumerico.equals(buscado)) return false;
        }
        if (filterByCategory && categoria != null && !categoria.equalsIgnoreCase(joya.getCategoria())) return false;
        if (filterBySocio && socio != null && !socio.equalsIgnoreCase(joya.getSocio())) return false;
        if (filterByName && nombre != null) {
            String n = joya.getNombre() == null ? "" : joya.getNombre().toLowerCase();
            if (!n.contains(nombre.toLowerCase())) return false;
        }
        if (filterByNoVendido && joya.isVendido()) return false;
        if (estadoFinal != null && !estadoFinal.isEmpty()) {
            String est = joya.getEstado() == null ? "" : joya.getEstado();
            if (!estadoFinal.contains(est)) return false;
        }
        return true;
    }

    private String extraerTexto(String json, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher m = p.matcher(json == null ? "" : json);
        if (!m.find()) return "";
        return m.group(1)
                .replace("\\\\\"", "\"")
                .replace("\\\\n", "\n")
                .replace("\\\\r", "\r")
                .replace("\\\\\\\\", "\\");
    }

    private boolean extraerBoolean(String json, String key, boolean defaultValue) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json == null ? "" : json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : defaultValue;
    }

    private double extraerNumero(String json, String key, double defaultValue) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(json == null ? "" : json);
        if (!m.find()) return defaultValue;
        try { return Double.parseDouble(m.group(1)); }
        catch (NumberFormatException ex) { return defaultValue; }
    }

    private String mapearEstadoOperativo(String seleccion) {
        if (seleccion == null) return null;
        return switch (seleccion) {
            case "Estado: Disponible", "disponible" -> "disponible";
            case "Estado: Anulado",    "anulado"    -> "anulado";
            case "Estado: Prestado",   "prestado"   -> "prestado";
            default -> null;
        };
    }
}
