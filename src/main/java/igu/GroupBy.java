package igu;

import com.marihel.utils.FormatterUtils;
import logica.Categoria;
import logica.CategoriaVerificacion;
import logica.Controladora;
import logica.Joya;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;

public class GroupBy {
    private JCheckBox todasLasJoyasCheckBox;
    private JComboBox<String> comboBoxCategoria;
    private JButton btnContar;
    private JList<Joya> listaJoyas;
    private JPanel mainPanel;
    private JButton btnVerificar;
    private JScrollPane panelScrollVerificados;

    private Controladora controladora;
    private DefaultListModel<Joya> listModel;
    private boolean enVerificacion = false;
    private JDialog dialogVerificacion;
    private String categoriaSeleccionadaActual;
    private SwingWorker<List<Joya>, Void> cargaJoyasWorker;
    private SwingWorker<List<CategoriaVerificacion>, Void> cargaCategoriasWorker;
    private final SessionContext session;

    public GroupBy(JFrame parent) {
        this(parent, null);
    }

    public GroupBy(JFrame parent, SessionContext session) {
        this.session = session;
        controladora = new Controladora(session);
        listModel = new DefaultListModel<>();

        construirUI();
        registrarAcciones();
        cargarCategoriasDesdeBD();
        cargarCategoriasVerificacion();
    }

    // ── Construcción de la UI en código puro ─────────────────────────────────

    private void construirUI() {
        mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBackground(UITheme.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Título
        JLabel title = new JLabel("Group By", SwingConstants.CENTER);
        title.setFont(UITheme.F_TITLE);
        title.setForeground(UITheme.TEXT);
        mainPanel.add(title, BorderLayout.NORTH);

        // Panel izquierdo: categorías verificadas — título como label, no TitledBorder
        JPanel leftWrapper = new JPanel(new BorderLayout(0, 6));
        leftWrapper.setBackground(UITheme.BG);
        leftWrapper.setPreferredSize(new Dimension(380, 400));

        JLabel verifiedHeader = new JLabel("Estado de verificación");
        verifiedHeader.setFont(UITheme.F_SECTION);
        verifiedHeader.setForeground(UITheme.TEXT);
        verifiedHeader.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
        leftWrapper.add(verifiedHeader, BorderLayout.NORTH);

        panelScrollVerificados = UITheme.styledScroll(new JPanel());
        leftWrapper.add(panelScrollVerificados, BorderLayout.CENTER);

        mainPanel.add(leftWrapper, BorderLayout.WEST);

        // Panel central: lista de joyas con encabezado
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 6));
        centerWrapper.setBackground(UITheme.BG);

        JLabel joyasHeader = new JLabel("Joyas");
        joyasHeader.setFont(UITheme.F_SECTION);
        joyasHeader.setForeground(UITheme.TEXT);
        joyasHeader.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
        centerWrapper.add(joyasHeader, BorderLayout.NORTH);

        listaJoyas = new JList<>(listModel);
        listaJoyas.setCellRenderer(new JoyaListCellRenderer());
        listaJoyas.setFont(UITheme.F_BODY);
        listaJoyas.setBackground(UITheme.BG);
        listaJoyas.setSelectionBackground(UITheme.ACCENT);
        listaJoyas.setSelectionForeground(Color.WHITE);
        listaJoyas.setFixedCellHeight(32);

        centerWrapper.add(UITheme.styledScroll(listaJoyas), BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // Panel derecho: controles
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(UITheme.BG);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.roundedBorder(UITheme.BORDER, 14),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        rightPanel.setPreferredSize(new Dimension(180, 400));

        // Imagen decorativa
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/estin.png"));
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rightPanel.add(imgLabel);
        } catch (Exception ignored) {}

        rightPanel.add(Box.createVerticalStrut(12));

        todasLasJoyasCheckBox = new JCheckBox("Todas las joyas");
        todasLasJoyasCheckBox.setFont(UITheme.F_FILTER);
        todasLasJoyasCheckBox.setForeground(UITheme.TEXT);
        todasLasJoyasCheckBox.setBackground(UITheme.BG);
        todasLasJoyasCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(todasLasJoyasCheckBox);

        rightPanel.add(Box.createVerticalStrut(6));

        comboBoxCategoria = new JComboBox<>();
        UITheme.styleCombo(comboBoxCategoria);
        comboBoxCategoria.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboBoxCategoria.setMaximumSize(new Dimension(160, 30));
        rightPanel.add(comboBoxCategoria);

        rightPanel.add(Box.createVerticalStrut(8));

        btnContar = UITheme.primaryBtn("Contar");
        btnContar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContar.setMaximumSize(new Dimension(140, 34));
        rightPanel.add(btnContar);

        rightPanel.add(Box.createVerticalStrut(4));

        JLabel moneyLabel = new JLabel("$$$", SwingConstants.CENTER);
        moneyLabel.setFont(UITheme.F_SECTION);
        moneyLabel.setForeground(UITheme.TEXT_MUTED);
        moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(moneyLabel);

        rightPanel.add(Box.createVerticalStrut(8));

        btnVerificar = UITheme.primaryBtn("Verificar");
        btnVerificar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVerificar.setMaximumSize(new Dimension(140, 34));
        rightPanel.add(btnVerificar);

        rightPanel.add(Box.createVerticalGlue());

        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void registrarAcciones() {
        todasLasJoyasCheckBox.addActionListener(e -> {
            if (!enVerificacion && todasLasJoyasCheckBox.isSelected()) {
                cargarTodasLasJoyas();
                comboBoxCategoria.setEnabled(false);
            }
        });

        comboBoxCategoria.addActionListener(e -> {
            if (!enVerificacion) {
                String categoriaSeleccionada = (String) comboBoxCategoria.getSelectedItem();
                if (categoriaSeleccionada == null || categoriaSeleccionada.isBlank() || "Seleccione categoria...".equals(categoriaSeleccionada)) {
                    return;
                }
                cargarJoyasPorCategoria(categoriaSeleccionada);
                todasLasJoyasCheckBox.setSelected(false);
            }
        });

        btnContar.addActionListener(e -> {
            int cantidad = listModel.getSize();
            ImageIcon icon;
            try {
                icon = new ImageIcon(new ImageIcon(getClass().getResource("/inversor.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            } catch (Exception ex) {
                icon = null;
            }
            Object[] opciones = {"OK", "Marcar todo verificado"};
            int seleccion = JOptionPane.showOptionDialog(
                    mainPanel,
                    "Total de joyas: " + cantidad,
                    "Conteo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    icon,
                    opciones,
                    opciones[0]
            );
            if (seleccion == 1) {
                controladora.actualizarFechaVerificacionCategoria(categoriaSeleccionadaActual, LocalDateTime.now());
            }
        });

        btnVerificar.addActionListener(e -> {
            if (!enVerificacion) {
                bloquearOpciones();
                iniciarVerificacion();
            } else {
                dialogVerificacion.setVisible(true);
            }
        });

        listaJoyas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = listaJoyas.locationToIndex(e.getPoint());
                    if (index != -1) {
                        mostrarDetallesJoya2(listModel.getElementAt(index));
                    }
                }
            }
        });
    }

    // ── Lógica (sin cambios respecto al original) ─────────────────────────────

    private void cargarCategoriasDesdeBD() {
        comboBoxCategoria.removeAllItems();
        comboBoxCategoria.addItem("Seleccione categoria...");
        try {
            for (Categoria categoria : controladora.obtenerCategorias()) {
                comboBoxCategoria.addItem(categoria.getNombre());
            }
            comboBoxCategoria.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar las categorias: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTodasLasJoyas() {
        cargarJoyasAsync(false, null, Arrays.asList("disponible", "prestado"));
    }

    private void cargarJoyasPorCategoria(String categoriaSeleccionada) {
        categoriaSeleccionadaActual = categoriaSeleccionada;
        cargarJoyasAsync(true, categoriaSeleccionada, Arrays.asList("disponible", "prestado"));
    }

    private void cargarJoyasAsync(boolean filterByCategory, String categoriaSeleccionada, List<String> estado) {
        if (cargaJoyasWorker != null && !cargaJoyasWorker.isDone()) {
            cargaJoyasWorker.cancel(true);
        }
        setCargandoJoyas(true);
        List<String> estadoFiltro = List.copyOf(estado);
        cargaJoyasWorker = new SwingWorker<>() {
            @Override
            protected List<Joya> doInBackground() {
                return controladora.filtrarJoyas(
                        false, null, filterByCategory, categoriaSeleccionada,
                        false, null, false, null, true, estadoFiltro
                );
            }
            @Override
            protected void done() {
                setCargandoJoyas(false);
                if (isCancelled()) return;
                try {
                    actualizarLista(get());
                } catch (CancellationException ignored) {
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error al cargar joyas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        cargaJoyasWorker.execute();
    }

    private void setCargandoJoyas(boolean cargando) {
        btnContar.setEnabled(!cargando);
        btnVerificar.setEnabled(!cargando);
        todasLasJoyasCheckBox.setEnabled(!cargando && !enVerificacion);
        comboBoxCategoria.setEnabled(!cargando && !todasLasJoyasCheckBox.isSelected() && !enVerificacion);
    }

    private void actualizarLista(List<Joya> joyas) {
        listModel.clear();
        for (Joya joya : joyas) {
            listModel.addElement(joya);
        }
    }

    private void mostrarDetallesJoya2(Joya joya) {
        JDialog detallesDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Detalles de la Joya", true);
        detallesDialog.setSize(650, 900);
        detallesDialog.setLocationRelativeTo(mainPanel);
        detallesDialog.add(new DetallesJoya(joya, null, session).getMainPanel());
        detallesDialog.setVisible(true);
    }

    private void iniciarVerificacion() {
        enVerificacion = true;
        if (dialogVerificacion == null) {
            dialogVerificacion = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Verificación de Joyas", true);
            dialogVerificacion.setSize(600, 400);
            dialogVerificacion.setLocationRelativeTo(mainPanel);

            JPanel dialogPanel = new JPanel(new BorderLayout());
            JPanel inputPanel = new JPanel(new BorderLayout());
            JLabel lblInfo = new JLabel("Ingrese el ID de la joya y presione Enter:");
            JTextField txtInputId = new JTextField();
            inputPanel.add(lblInfo, BorderLayout.NORTH);
            inputPanel.add(txtInputId, BorderLayout.CENTER);

            JPanel detallesPanel = new JPanel();
            detallesPanel.setLayout(new BoxLayout(detallesPanel, BoxLayout.Y_AXIS));
            detallesPanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Joya"));

            dialogPanel.add(inputPanel, BorderLayout.NORTH);
            dialogPanel.add(new JScrollPane(detallesPanel), BorderLayout.CENTER);
            dialogVerificacion.add(dialogPanel);

            txtInputId.addActionListener(e -> {
                String inputId = txtInputId.getText().trim();
                if (!inputId.isEmpty()) {
                    verificarYMostrarJoya(inputId, detallesPanel);
                    txtInputId.setText("");
                }
            });

            dialogVerificacion.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
            dialogVerificacion.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cerrarVerificacion();
                }
            });
        }
        dialogVerificacion.setVisible(true);
    }

    private void cerrarVerificacion() {
        if (dialogVerificacion != null) {
            dialogVerificacion.setVisible(false);
            enVerificacion = false;
            desbloquearOpciones();
        }
    }

    private JPanel crearTarjetaAtributo(String titulo, String valor) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        tarjeta.setMaximumSize(new Dimension(450, 50));

        JLabel lblTitulo = new JLabel(titulo + ": ");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(80, 80, 80));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblValor.setForeground(new Color(50, 50, 50));

        tarjeta.add(lblTitulo, BorderLayout.WEST);
        tarjeta.add(lblValor, BorderLayout.CENTER);
        return tarjeta;
    }

    private void verificarYMostrarJoya(String id, JPanel detallesPanel) {
        boolean encontrada = false;
        for (int i = 0; i < listModel.getSize(); i++) {
            Joya joya = listModel.getElementAt(i);
            String buscado = id.trim();
            boolean coincide = (joya.getDisplayId() != null && joya.getDisplayId().equalsIgnoreCase(buscado))
                    || (joya.getId() != null && joya.getId().toString().equals(buscado));

            if (coincide) {
                encontrada = true;
                detallesPanel.removeAll();
                String idAMostrar = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank())
                        ? joya.getDisplayId() : String.valueOf(joya.getId());
                detallesPanel.add(crearTarjetaAtributo("ID", idAMostrar));
                detallesPanel.add(crearTarjetaAtributo("Nombre", joya.getNombre()));
                detallesPanel.add(crearTarjetaAtributo("Categoría", joya.getCategoria()));
                detallesPanel.add(crearTarjetaAtributo("Peso", FormatterUtils.formatearPeso(joya.getPeso()) + " gramos"));
                detallesPanel.add(crearTarjetaAtributo("Precio", "$" + joya.getPrecio()));
                detallesPanel.add(crearTarjetaAtributo("Tiene Piedra", joya.isTienePiedra() ? "Sí" : "No"));
                if (joya.isTienePiedra()) {
                    detallesPanel.add(crearTarjetaAtributo("Información de Piedra", joya.getInfoPiedra()));
                }
                detallesPanel.add(crearTarjetaAtributo("Observación", joya.getObservacion()));
                detallesPanel.revalidate();
                detallesPanel.repaint();
                listModel.remove(i);
                break;
            }
        }

        if (!encontrada) {
            try {
                ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource("/llora.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                JOptionPane.showMessageDialog(mainPanel, "La joya con ID " + id + " no se encuentra en la lista.", "No Encontrada", JOptionPane.ERROR_MESSAGE, icon);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "La joya con ID " + id + " no se encuentra en la lista.", "No Encontrada", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (listModel.getSize() == 0) {
            try {
                ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource("/nino.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                JOptionPane.showMessageDialog(mainPanel, "Todas las joyas han sido verificadas.", "Lista Completa", JOptionPane.INFORMATION_MESSAGE, icon);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Todas las joyas han sido verificadas.", "Lista Completa", JOptionPane.INFORMATION_MESSAGE);
            }
            if (categoriaSeleccionadaActual != null) {
                controladora.actualizarFechaVerificacionCategoria(categoriaSeleccionadaActual, LocalDateTime.now());
            }
        }
    }

    private void bloquearOpciones() {
        todasLasJoyasCheckBox.setEnabled(false);
        comboBoxCategoria.setEnabled(false);
    }

    private void desbloquearOpciones() {
        todasLasJoyasCheckBox.setEnabled(true);
        comboBoxCategoria.setEnabled(true);
    }

    private void cargarCategoriasVerificacion() {
        if (cargaCategoriasWorker != null && !cargaCategoriasWorker.isDone()) {
            cargaCategoriasWorker.cancel(true);
        }
        JPanel cargandoPanel = new JPanel(new BorderLayout());
        cargandoPanel.setBackground(UITheme.BG);
        cargandoPanel.add(new JLabel("Cargando categorías...", SwingConstants.CENTER), BorderLayout.CENTER);
        panelScrollVerificados.setViewportView(cargandoPanel);

        cargaCategoriasWorker = new SwingWorker<>() {
            @Override
            protected List<CategoriaVerificacion> doInBackground() {
                return controladora.obtenerCategoriasOrdenadasPorVerificacion();
            }
            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<CategoriaVerificacion> categorias = get();
                    JPanel panelCategorias = new JPanel();
                    panelCategorias.setLayout(new BoxLayout(panelCategorias, BoxLayout.Y_AXIS));
                    panelCategorias.setBackground(UITheme.BG);
                    for (CategoriaVerificacion cat : categorias) {
                        long dias = ChronoUnit.DAYS.between(cat.getUltimaFechaVerificacion(), LocalDateTime.now());
                        String icono = (dias > 20) ? "\u2717 " : "\u2713 ";
                        String texto = icono + cat.getNombreCategoria() + " - " + dias + " días sin verificación";
                        JLabel lblCategoria = new JLabel(texto);
                        lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 14));
                        lblCategoria.setForeground(Color.BLACK);
                        lblCategoria.setOpaque(true);
                        lblCategoria.setBackground((dias > 20) ? new Color(255, 204, 204) : new Color(204, 255, 204));
                        panelCategorias.add(lblCategoria);
                        panelCategorias.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                    panelScrollVerificados.setViewportView(panelCategorias);
                } catch (CancellationException ignored) {
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error al cargar categorías: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        cargaCategoriasWorker.execute();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
