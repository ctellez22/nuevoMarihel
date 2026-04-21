package igu;

import logica.Controladora;
import logica.Joya;
import org.example.SessionContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class VentasDialog extends JDialog {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color BG_MAIN    = new Color(245, 246, 250);
    private static final Color BG_CARD    = new Color(255, 255, 255);
    private static final Color BG_CARD2   = new Color(237, 239, 245);
    private static final Color ACCENT     = new Color(99, 102, 241);
    private static final Color ACCENT2    = new Color(59, 130, 246);
    private static final Color SUCCESS    = new Color(16, 185, 129);
    private static final Color TEXT_PRI   = new Color(17, 24, 39);
    private static final Color TEXT_SEC   = new Color(107, 114, 128);
    private static final Color ROW_ALT    = new Color(249, 250, 251);
    private static final Color ROW_NORM   = new Color(255, 255, 255);
    private static final Color HEADER_TBL = new Color(243, 244, 246);
    private static final Color GOLD       = new Color(217, 119, 6);

    private static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD, 22);
    private static final Font FONT_CARD   = new Font("SansSerif", Font.BOLD, 28);
    private static final Font FONT_LABEL  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_BOLD   = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_TABLE  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 12);

    private static final DecimalFormat FMT_PRECIO = new DecimalFormat(
            "#,###", new DecimalFormatSymbols(new Locale("es", "CO")));

    // ── Estado ─────────────────────────────────────────────────────────────────
    private final Controladora controladora;
    private final SessionContext session;

    private DefaultTableModel tableModel;
    private JTable tabla;
    private JLabel lblTotalVentas;
    private JLabel lblCantVentas;
    private JLabel lblTotalDinero;
    private JSpinner spinDesde;
    private JSpinner spinHasta;
    private JButton btnFiltroActivo;

    public VentasDialog(Frame owner, SessionContext session) {
        super(owner, "Reporte de Ventas", true);
        this.session = session;
        this.controladora = new Controladora(session);

        setSize(1150, 700);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        add(construirHeader(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirFooter(), BorderLayout.SOUTH);

        // Cargar hoy por defecto
        aplicarFiltroRapido("HOY");
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(235, 233, 255),
                        getWidth(), 0, new Color(219, 234, 254));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Título izquierda
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        JLabel ico = new JLabel("💎");
        ico.setFont(new Font("SansSerif", Font.PLAIN, 30));
        JLabel titulo = new JLabel("  Reporte de Ventas");
        titulo.setFont(FONT_TITLE);
        titulo.setForeground(new Color(30, 27, 75));
        izq.add(ico);
        izq.add(titulo);
        header.add(izq, BorderLayout.WEST);

        // Subtítulo derecha
        JLabel sub = new JLabel("Solo administradores  •  " + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("es"))));
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_SEC);
        header.add(sub, BorderLayout.EAST);

        return header;
    }

    // ── Centro ─────────────────────────────────────────────────────────────────
    private JPanel construirCentro() {
        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setBackground(BG_MAIN);
        centro.setBorder(new EmptyBorder(16, 24, 0, 24));

        centro.add(construirFiltros(), BorderLayout.NORTH);
        centro.add(construirTabla(), BorderLayout.CENTER);

        return centro;
    }

    // ── Filtros ────────────────────────────────────────────────────────────────
    private JPanel construirFiltros() {
        JPanel wrap = new JPanel(new BorderLayout(16, 0));
        wrap.setBackground(BG_MAIN);

        // Botones rápidos
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG_MAIN);

        JButton btnHoy     = crearBotonFiltro("☀  Hoy",     "HOY");
        JButton btnSemana  = crearBotonFiltro("📅  Semana", "SEMANA");
        JButton btnMes     = crearBotonFiltro("🗓  Mes",    "MES");

        btnFiltroActivo = btnHoy;
        marcarActivo(btnHoy);

        btnHoy   .addActionListener(e -> { marcarActivo(btnHoy);    btnFiltroActivo = btnHoy;    aplicarFiltroRapido("HOY");    });
        btnSemana.addActionListener(e -> { marcarActivo(btnSemana); btnFiltroActivo = btnSemana; aplicarFiltroRapido("SEMANA"); });
        btnMes   .addActionListener(e -> { marcarActivo(btnMes);    btnFiltroActivo = btnMes;    aplicarFiltroRapido("MES");    });

        btnPanel.add(btnHoy);
        btnPanel.add(btnSemana);
        btnPanel.add(btnMes);

        // Rango personalizado
        JPanel rangoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rangoPanel.setBackground(BG_MAIN);

        JLabel lblDesde = estilizarLabel("Desde:");
        JLabel lblHasta = estilizarLabel("Hasta:");

        spinDesde = crearDateSpinner(LocalDate.now());
        spinHasta = crearDateSpinner(LocalDate.now());

        JButton btnBuscar = new JButton("🔍  Buscar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBuscar.setFont(FONT_BOLD);
        btnBuscar.setBackground(ACCENT);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setOpaque(false);
        btnBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(79, 70, 229), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> aplicarRangoPersonalizado());
        aplicarHover(btnBuscar, ACCENT, new Color(110, 60, 220));

        rangoPanel.add(lblDesde);
        rangoPanel.add(spinDesde);
        rangoPanel.add(lblHasta);
        rangoPanel.add(spinHasta);
        rangoPanel.add(btnBuscar);

        // Juntar todo
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(BG_MAIN);
        top.add(btnPanel, BorderLayout.NORTH);
        top.add(rangoPanel, BorderLayout.SOUTH);

        return top;
    }

    private JButton crearBotonFiltro(String texto, String filtro) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_SEC);
        btn.setBackground(BG_CARD);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(8, 22, 8, 22)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void marcarActivo(JButton boton) {
        // Resetear todos los botones del padre
        if (boton.getParent() != null) {
            for (Component c : boton.getParent().getComponents()) {
                if (c instanceof JButton b) {
                    b.setForeground(TEXT_SEC);
                    b.setBackground(BG_CARD);
                    b.setContentAreaFilled(false);
                    b.setOpaque(false);
                    b.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                            BorderFactory.createEmptyBorder(8, 22, 8, 22)
                    ));
                }
            }
        }
        boton.setForeground(Color.WHITE);
        boton.setBackground(ACCENT);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT2, 1, true),
                BorderFactory.createEmptyBorder(7, 21, 7, 21)
        ));
    }

    private JSpinner crearDateSpinner(LocalDate valor) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spin = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spin, "dd/MM/yyyy");
        spin.setEditor(editor);
        spin.setValue(java.sql.Date.valueOf(valor));
        spin.setPreferredSize(new Dimension(120, 34));
        spin.setFont(FONT_LABEL);
        spin.setBackground(BG_CARD2);
        spin.setForeground(TEXT_PRI);
        editor.getTextField().setBackground(BG_CARD2);
        editor.getTextField().setForeground(TEXT_PRI);
        editor.getTextField().setCaretColor(TEXT_PRI);
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return spin;
    }

    private JLabel estilizarLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_SEC);
        return lbl;
    }

    // ── Tabla ──────────────────────────────────────────────────────────────────
    private JScrollPane construirTabla() {
        String[] columnas = {"#", "Ref.", "Nombre", "Categoría", "Socio", "Precio Etiqueta", "Precio Venta", "Fecha Vendida"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(tableModel);
        tabla.setRowHeight(40);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setBackground(ROW_NORM);
        tabla.setForeground(TEXT_PRI);
        tabla.setFont(FONT_TABLE);
        tabla.setSelectionBackground(new Color(224, 231, 255));
        tabla.setSelectionForeground(new Color(49, 46, 129));

        // Header
        JTableHeader th = tabla.getTableHeader();
        th.setBackground(HEADER_TBL);
        th.setForeground(TEXT_SEC);
        th.setFont(FONT_HEADER);
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));

        // Anchos
        int[] anchos = {40, 80, 200, 130, 120, 130, 140, 150};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // Renderer filas alternadas + Precio Venta resaltado
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setOpaque(true);

                if (sel) {
                    setBackground(new Color(224, 231, 255));
                    setForeground(new Color(49, 46, 129));
                } else {
                    setBackground(row % 2 == 0 ? ROW_NORM : ROW_ALT);
                    setForeground(col == 6 ? GOLD : TEXT_PRI);
                    if (col == 6) setFont(FONT_BOLD);
                    else setFont(FONT_TABLE);
                }

                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(col >= 5 && col <= 6 ? RIGHT : LEFT);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        scroll.getViewport().setBackground(ROW_NORM);
        scroll.setBackground(BG_MAIN);
        scroll.getVerticalScrollBar().setUI(new ScrollBarUI());
        return scroll;
    }

    // ── Footer con KPIs ────────────────────────────────────────────────────────
    private JPanel construirFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_MAIN);
        footer.setBorder(new EmptyBorder(12, 24, 18, 24));

        // Tarjetas KPI
        JPanel kpis = new JPanel(new GridLayout(1, 3, 16, 0));
        kpis.setBackground(BG_MAIN);

        lblCantVentas  = new JLabel("0", SwingConstants.CENTER);
        lblTotalVentas = new JLabel("$0", SwingConstants.CENTER);
        lblTotalDinero = new JLabel("$0", SwingConstants.CENTER);

        kpis.add(crearKpiCard("🛍  Total ventas", lblCantVentas, ACCENT2));
        kpis.add(crearKpiCard("💰  Ingresos (precio etiqueta)", lblTotalVentas, ACCENT));
        kpis.add(crearKpiCard("✅  Ingresos (precio real venta)", lblTotalDinero, SUCCESS));

        footer.add(kpis, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(FONT_BOLD);
        btnCerrar.setBackground(new Color(229, 231, 235));
        btnCerrar.setForeground(new Color(75, 85, 99));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        derecha.setBackground(BG_MAIN);
        derecha.add(btnCerrar);
        footer.add(derecha, BorderLayout.EAST);

        return footer;
    }

    private JPanel crearKpiCard(String titulo, JLabel valorLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel tituloLbl = new JLabel(titulo, SwingConstants.CENTER);
        tituloLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tituloLbl.setForeground(TEXT_SEC);

        valorLabel.setFont(FONT_CARD);
        valorLabel.setForeground(accentColor);

        card.add(tituloLbl, BorderLayout.NORTH);
        card.add(valorLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Lógica de filtrado ─────────────────────────────────────────────────────
    private void aplicarFiltroRapido(String tipo) {
        LocalDateTime desde;
        LocalDateTime hasta = LocalDateTime.now().with(LocalTime.MAX);

        switch (tipo) {
            case "HOY"    -> desde = LocalDate.now().atStartOfDay();
            case "SEMANA" -> desde = LocalDate.now().minusDays(6).atStartOfDay();
            case "MES"    -> desde = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            default       -> desde = LocalDate.now().atStartOfDay();
        }

        cargarVentas(desde, hasta);
    }

    private void aplicarRangoPersonalizado() {
        try {
            java.util.Date d = (java.util.Date) spinDesde.getValue();
            java.util.Date h = (java.util.Date) spinHasta.getValue();
            LocalDateTime desde = d.toInstant().atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().atStartOfDay();
            LocalDateTime hasta = h.toInstant().atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().atTime(LocalTime.MAX);
            if (desde.isAfter(hasta)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha 'Desde' no puede ser mayor que 'Hasta'.",
                        "Rango inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cargarVentas(desde, hasta);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al leer fechas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarVentas(LocalDateTime desde, LocalDateTime hasta) {
        tableModel.setRowCount(0);

        List<Joya> ventas = controladora.obtenerVentasPorRango(desde, hasta);

        BigDecimal totalEtiqueta = BigDecimal.ZERO;
        BigDecimal totalReal     = BigDecimal.ZERO;
        int fila = 1;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Joya j : ventas) {
            String ref         = j.getDisplayId() != null ? j.getDisplayId() : String.valueOf(j.getId());
            String nombre      = j.getNombre() != null ? j.getNombre() : "-";
            String categoria   = j.getCategoria() != null ? j.getCategoria() : "-";
            String socio       = j.getSocio() != null ? j.getSocio() : "-";
            String precioEt    = j.getPrecio() != null ? "$" + j.getPrecio() : "-";
            String precioVenta = (j.getPrecioVenta() != null && !j.getPrecioVenta().isBlank())
                    ? "$" + j.getPrecioVenta() : "-";
            String fechaV      = j.getFechaVendida() != null ? j.getFechaVendida().format(fmt) : "-";

            tableModel.addRow(new Object[]{fila++, ref, nombre, categoria, socio, precioEt, precioVenta, fechaV});

            // Acumular precio etiqueta
            if (j.getPrecio() != null) {
                try {
                    String num = j.getPrecio().replaceAll("[^0-9.]", "");
                    if (!num.isBlank()) totalEtiqueta = totalEtiqueta.add(new BigDecimal(num));
                } catch (Exception ignored) {}
            }

            // Acumular precio real
            if (j.getPrecioVenta() != null && !j.getPrecioVenta().isBlank()) {
                try {
                    String num = j.getPrecioVenta().replaceAll("[^0-9.]", "");
                    if (!num.isBlank()) totalReal = totalReal.add(new BigDecimal(num));
                } catch (Exception ignored) {}
            }
        }

        int cant = ventas.size();
        lblCantVentas .setText(String.valueOf(cant));
        lblTotalVentas.setText("$" + FMT_PRECIO.format(totalEtiqueta));
        lblTotalDinero.setText("$" + FMT_PRECIO.format(totalReal));
    }

    // ── Helpers UI ──────────────────────────────────────────────────────────────
    private void aplicarHover(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
    }

    // ── ScrollBar personalizado ─────────────────────────────────────────────────
    private static class ScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor  = new Color(209, 213, 219);
            trackColor  = new Color(245, 246, 250);
        }
        @Override protected JButton createDecreaseButton(int o) { return crearBotonVacio(); }
        @Override protected JButton createIncreaseButton(int o) { return crearBotonVacio(); }
        private JButton crearBotonVacio() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}

