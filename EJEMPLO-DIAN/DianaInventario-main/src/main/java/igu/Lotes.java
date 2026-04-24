package igu;

import logica.Controladora;
import logica.Lote;
import org.example.SessionContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Lotes {

    private final JPanel mainPanel;
    private final DefaultTableModel tableModel;
    private final Controladora controladora;
    private final SessionContext session;

    public Lotes(JFrame parent, SessionContext session) {
        this.controladora = new Controladora();
        this.session = session;

        this.mainPanel = new JPanel(new BorderLayout(14, 14));
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        this.mainPanel.setBackground(ModernUI.BG);

        JPanel panelSuperior = crearTarjeta(new BorderLayout(10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitulo = new JLabel("Lotes Creados");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblTitulo.setForeground(ModernUI.TEXT);

        JLabel lblSubtitulo = new JLabel("Gestión de lotes con vista clara, rápida y elegante.");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtitulo.setForeground(ModernUI.MUTED);

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        JButton btnDetalles = new JButton("👁️ Ver Detalles");

        ModernUI.styleSecondaryButton(btnRefrescar, "🔄 Refrescar");
        ModernUI.stylePrimaryButton(btnDetalles, "👁️ Ver Detalles");

        btnRefrescar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnDetalles.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnDetalles);
        panelBotones.add(btnRefrescar);

        JPanel panelTitulos = new JPanel(new GridLayout(0, 1, 0, 6));
        panelTitulos.setOpaque(false);
        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSubtitulo);

        panelSuperior.add(panelTitulos, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);

        // Tabla de lotes
        String[] columnNames = {"ID", "Nombre", "Peso Total (qt)", "Piedras", "Estado", "Fecha Creación", "Autorizado"};
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tableLotes = new JTable(tableModel);
        tableLotes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableLotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableLotes.setRowHeight(34);
        tableLotes.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tableLotes.getTableHeader().setBackground(ModernUI.PRIMARY);
        tableLotes.getTableHeader().setForeground(Color.WHITE);
        tableLotes.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        tableLotes.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tableLotes.getTableHeader().setReorderingAllowed(false);
        tableLotes.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ModernUI.PRIMARY_DARK));
        tableLotes.setGridColor(ModernUI.BORDER);
        tableLotes.setShowGrid(true);
        tableLotes.setIntercellSpacing(new Dimension(0, 1));
        tableLotes.setSelectionBackground(new Color(226, 234, 255));
        tableLotes.setSelectionForeground(ModernUI.TEXT);
        tableLotes.setFillsViewportHeight(true);
        aplicarEstiloTabla(tableLotes);
        configurarAnchosColumnas(tableLotes);

        JScrollPane scrollPane = new JScrollPane(tableLotes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(198, 209, 230), 2, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel panelTabla = crearTarjeta(new BorderLayout(12, 12));
        panelTabla.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel lblTabla = new JLabel("Lotes registrados");
        lblTabla.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTabla.setForeground(ModernUI.TEXT);
        panelTabla.add(lblTabla, BorderLayout.NORTH);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        this.mainPanel.add(panelSuperior, BorderLayout.NORTH);
        this.mainPanel.add(panelTabla, BorderLayout.CENTER);

        // Event listeners
        btnRefrescar.addActionListener(e -> cargarLotes());
        btnDetalles.addActionListener(e -> verDetallesLote(tableLotes));

        // Atajo de teclado
        this.mainPanel.registerKeyboardAction(
                e -> cargarLotes(),
                KeyStroke.getKeyStroke("F5"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        cargarLotes();
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

    private void aplicarEstiloTabla(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tabla, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(226, 234, 255));
                    c.setForeground(ModernUI.TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 250, 255));
                    c.setForeground(ModernUI.TEXT);
                }
                if (c instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    private void configurarAnchosColumnas(JTable table) {
        int[] widths = {70, 240, 140, 110, 170, 210, 130};
        TableColumnModel model = table.getColumnModel();
        for (int i = 0; i < widths.length && i < model.getColumnCount(); i++) {
            model.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void cargarLotes() {
        tableModel.setRowCount(0);
        try {
            List<Lote> lotes = controladora.obtenerTodosLosLotes(session);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int lotesVisibles = 0;

            for (Lote lote : lotes) {
                // Solo ocultar en vista los lotes agotados (peso <= 0), sin eliminarlos.
                if (lote == null || lote.getPesoTotal() <= 0) {
                    continue;
                }
                Object[] row = {
                        lote.getId(),
                        lote.getNombre(),
                        String.format("%.2f qt", lote.getPesoTotal()),
                        lote.getCantidadPiedras(),
                        lote.getEstado() != null ? lote.getEstado() : "-",
                        lote.getFechaCreacion() != null ? lote.getFechaCreacion().format(formatter) : "-",
                        lote.isAutorizado() ? "Sí" : "No"
                };
                tableModel.addRow(row);
                lotesVisibles++;
            }

            if (lotes.isEmpty()) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "No hay lotes registrados aún.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if (lotesVisibles == 0) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "No hay lotes con peso disponible para mostrar.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No se pudieron cargar los lotes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void verDetallesLote(JTable tableLotes) {
        int selectedRow = tableLotes.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Seleccione un lote para ver sus detalles.",
                    "Atención",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Long loteId = (Long) tableModel.getValueAt(selectedRow, 0);
            Lote lote = controladora.obtenerLotePorId(loteId);

            if (lote == null) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "No se encontró el lote solicitado.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            mostrarDetallesLote(lote);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No se pudieron cargar los detalles del lote: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void mostrarDetallesLote(Lote lote) {
        JPanel panelDetalles = new JPanel();
        panelDetalles.setLayout(new BoxLayout(panelDetalles, BoxLayout.Y_AXIS));
        panelDetalles.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelDetalles.setBackground(ModernUI.BG);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Título
        JLabel lblTituloDetalles = new JLabel("📦 " + lote.getNombre());
        lblTituloDetalles.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTituloDetalles.setForeground(ModernUI.PRIMARY);
        lblTituloDetalles.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panelDetalles.add(lblTituloDetalles);
        panelDetalles.add(Box.createVerticalStrut(20));

        // Información en dos columnas
        JPanel panelInfoDos = new JPanel(new GridLayout(0, 2, 30, 18));
        panelInfoDos.setOpaque(false);
        panelInfoDos.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        agregarDetalle(panelInfoDos, "🔢 ID:", String.valueOf(lote.getId()));
        agregarDetalle(panelInfoDos, "⚖️ Peso Total:", String.format("%.2f qt", lote.getPesoTotal()));
        agregarDetalle(panelInfoDos, "💎 Cantidad Piedras:", String.valueOf(lote.getCantidadPiedras()));
        agregarDetalle(panelInfoDos, "🔹 Tipo Piedra:", lote.getTipoPiedra() != null ? lote.getTipoPiedra() : "-");
        agregarDetalle(panelInfoDos, "✨ Calidad Piedra:", lote.getCalidadPiedra() != null ? lote.getCalidadPiedra() : "-");

        panelDetalles.add(panelInfoDos);
        panelDetalles.add(Box.createVerticalStrut(25));

        // Separador
        JSeparator sep1 = new JSeparator();
        sep1.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panelDetalles.add(sep1);
        panelDetalles.add(Box.createVerticalStrut(25));

        // Más información en dos columnas
        JPanel panelInfoDos2 = new JPanel(new GridLayout(0, 2, 30, 18));
        panelInfoDos2.setOpaque(false);
        panelInfoDos2.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        agregarDetalle(panelInfoDos2, "👤 Socio:", lote.getSocio() != null ? lote.getSocio() : "-");
        agregarDetalle(panelInfoDos2, "📊 Estado:", lote.getEstado() != null ? lote.getEstado() : "-");
        agregarDetalle(panelInfoDos2, "📅 Fecha Creación:", lote.getFechaCreacion() != null ? lote.getFechaCreacion().format(formatter) : "-");
        agregarDetalle(panelInfoDos2, "✅ Autorizado:", lote.isAutorizado() ? "Sí" : "No");

        panelDetalles.add(panelInfoDos2);
        panelDetalles.add(Box.createVerticalStrut(25));

        // Separador
        JSeparator sep2 = new JSeparator();
        sep2.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panelDetalles.add(sep2);
        panelDetalles.add(Box.createVerticalStrut(25));

        // Información de precios
        JPanel panelPrecios = new JPanel(new GridLayout(0, 2, 30, 18));
        panelPrecios.setOpaque(false);
        panelPrecios.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        agregarDetalle(panelPrecios, "💰 Precio Estimado:", lote.getPrecioEstimado() != null ? lote.getPrecioEstimado() : "-");
        agregarDetalle(panelPrecios, "💵 Precio Venta Real:", lote.getPrecioVentaReal() != null ? lote.getPrecioVentaReal() : "-");

        panelDetalles.add(panelPrecios);
        panelDetalles.add(Box.createVerticalStrut(25));

        // Separador
        JSeparator sep3 = new JSeparator();
        sep3.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panelDetalles.add(sep3);
        panelDetalles.add(Box.createVerticalStrut(25));

        // Punto físico y descripción
        agregarEtiqueta(panelDetalles, "📍 Punto Físico:");
        panelDetalles.add(Box.createVerticalStrut(8));
        agregarDetalle2(panelDetalles, lote.getPuntoFisico() != null ? lote.getPuntoFisico() : "-");

        panelDetalles.add(Box.createVerticalStrut(20));

        agregarEtiqueta(panelDetalles, "📝 Descripción:");
        panelDetalles.add(Box.createVerticalStrut(8));
        
        JTextArea descArea = new JTextArea(lote.getDescripcion() != null ? lote.getDescripcion() : "-");
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descArea.setBackground(Color.WHITE);
        descArea.setBorder(BorderFactory.createLineBorder(ModernUI.BORDER, 1));
        descArea.setRows(6);
        JScrollPane scrollDesc = new JScrollPane(descArea);
        scrollDesc.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panelDetalles.add(scrollDesc);

        panelDetalles.add(Box.createVerticalGlue());

        JScrollPane scrollDetalles = new JScrollPane(panelDetalles);
        scrollDetalles.setBorder(null);

        JOptionPane.showMessageDialog(
                mainPanel,
                scrollDetalles,
                "Detalles del Lote",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void agregarDetalle(JPanel panel, String etiqueta, String valor) {
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblEtiqueta.setForeground(ModernUI.PRIMARY);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblValor.setForeground(ModernUI.TEXT);

        panel.add(lblEtiqueta);
        panel.add(lblValor);
    }

    private void agregarEtiqueta(JPanel panel, String etiqueta) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(ModernUI.PRIMARY);
        lbl.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        panel.add(lbl);
    }

    private void agregarDetalle2(JPanel panel, String valor) {
        JLabel lbl = new JLabel(valor);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(ModernUI.TEXT);
        lbl.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lbl);
    }
}

