package igu;

import logica.Controladora;
import logica.Joya;
import logica.PuntosFisicos;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VentasAdminPanel {
    private enum ModoFiltro {
        DIA,
        SEMANA,
        MES,
        ANIO,
        RANGO
    }

    private final JPanel mainPanel;
    private final Controladora controladora;
    private final JSpinner desdeSpinner;
    private final JSpinner hastaSpinner;
    private final JComboBox<String> puntoFisicoCombo;
    private final JPanel rangoPanel;
    private final JLabel totalMontoLabel;
    private final JLabel totalRegistrosLabel;
    private final DefaultTableModel tableModel;
    private final Timer recargaTimer;
    private ModoFiltro modoActual;
    private boolean actualizandoModo;

    public VentasAdminPanel() {
        this.controladora = new Controladora();
        this.mainPanel = new RoundedPanel(new BorderLayout(12, 12), new Color(245, 248, 255), new Color(220, 228, 244), 28, 1, new Color(150, 170, 220, 28), 6);
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        this.mainPanel.setBackground(new Color(245, 248, 255));

        JPanel header = new RoundedPanel(new BorderLayout(), new Color(38, 70, 156), new Color(63, 95, 184), 22, 1, new Color(30, 40, 80, 40), 5);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel titulo = new JLabel("Panel de Ventas  ✨");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel subtitulo = new JLabel("Filtros rapidos + fechas personalizadas con actualizacion automatica");
        subtitulo.setForeground(new Color(220, 230, 255));
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        header.add(titulo, BorderLayout.NORTH);
        header.add(subtitulo, BorderLayout.SOUTH);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(12, 12));
        centro.setOpaque(false);

        JPanel filtros = new RoundedPanel(new BorderLayout(10, 10), Color.WHITE, new Color(220, 225, 235), 20, 1, new Color(30, 40, 80, 26), 6);
        filtros.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        // Alto suficiente para mostrar filtros y controles en el panel derecho.
        filtros.setPreferredSize(new Dimension(320, 410));
        filtros.setMinimumSize(new Dimension(300, 360));

        JLabel filtrosLabel = new JLabel("Filtros de ventas");
        filtrosLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        filtrosLabel.setForeground(new Color(49, 70, 120));

        JButton colapsarBtn = new JButton("Ocultar ▲");
        colapsarBtn.setFocusPainted(false);
        colapsarBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        colapsarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        colapsarBtn.setForeground(new Color(49, 70, 120));
        colapsarBtn.setBackground(new Color(236, 242, 255));
        colapsarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel botonesRapidosPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        botonesRapidosPanel.setOpaque(false);

        JToggleButton diaBtn = crearBotonFiltro("📅 Dia", "Ventas del dia actual");
        JToggleButton semanaBtn = crearBotonFiltro("🗓 Semana", "Ventas de lunes a domingo");
        JToggleButton mesBtn = crearBotonFiltro("🧾 Mes", "Ventas del mes actual");
        JToggleButton anioBtn = crearBotonFiltro("📈 Año", "Ventas del año actual");
        JToggleButton rangoBtn = crearBotonFiltro("🎯 Rango personalizado", "Selecciona fechas manualmente");

        ButtonGroup filtrosGroup = new ButtonGroup();
        filtrosGroup.add(diaBtn);
        filtrosGroup.add(semanaBtn);
        filtrosGroup.add(mesBtn);
        filtrosGroup.add(anioBtn);
        filtrosGroup.add(rangoBtn);

        botonesRapidosPanel.add(diaBtn);
        botonesRapidosPanel.add(semanaBtn);
        botonesRapidosPanel.add(mesBtn);
        botonesRapidosPanel.add(anioBtn);
        botonesRapidosPanel.add(rangoBtn);

        desdeSpinner = crearDateSpinner();
        hastaSpinner = crearDateSpinner();
        puntoFisicoCombo = new JComboBox<>();
        puntoFisicoCombo.addItem("Todos");
        for (String punto : PuntosFisicos.opciones()) {
            puntoFisicoCombo.addItem(punto);
        }
        puntoFisicoCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        puntoFisicoCombo.setBackground(Color.WHITE);

        JPanel filtrosFechaPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        filtrosFechaPanel.setOpaque(false);

        JPanel puntoPanel = new RoundedPanel(new BorderLayout(6, 0), new Color(249, 251, 255), new Color(214, 223, 245), 16, 1, new Color(30, 40, 80, 18), 4);
        puntoPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JLabel puntoLabel = new JLabel("Punto fisico");
        puntoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        puntoLabel.setForeground(new Color(66, 86, 134));
        puntoPanel.add(puntoLabel, BorderLayout.NORTH);
        puntoPanel.add(puntoFisicoCombo, BorderLayout.CENTER);

        rangoPanel = new RoundedPanel(new GridLayout(0, 1, 0, 6), new Color(249, 251, 255), new Color(214, 223, 245), 16, 1, new Color(30, 40, 80, 18), 4);
        rangoPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        rangoPanel.add(new JLabel("Desde"));
        rangoPanel.add(desdeSpinner);
        rangoPanel.add(new JLabel("Hasta"));
        rangoPanel.add(hastaSpinner);

        filtrosFechaPanel.add(puntoPanel);
        filtrosFechaPanel.add(rangoPanel);

        JPanel filtrosContenido = new JPanel(new BorderLayout(0, 10));
        filtrosContenido.setOpaque(false);
        filtrosContenido.add(botonesRapidosPanel, BorderLayout.NORTH);
        filtrosContenido.add(filtrosFechaPanel, BorderLayout.CENTER);

        JPanel filtrosHeader = new JPanel(new BorderLayout(8, 0));
        filtrosHeader.setOpaque(false);
        filtrosHeader.add(filtrosLabel, BorderLayout.WEST);
        filtrosHeader.add(colapsarBtn, BorderLayout.EAST);

        filtros.add(filtrosHeader, BorderLayout.NORTH);
        filtros.add(filtrosContenido, BorderLayout.CENTER);

        colapsarBtn.addActionListener(e -> {
            boolean visible = filtrosContenido.isVisible();
            filtrosContenido.setVisible(!visible);
            colapsarBtn.setText(visible ? "Mostrar ▼" : "Ocultar ▲");
            filtros.revalidate();
            filtros.repaint();
        });

        tableModel = new DefaultTableModel(new Object[]{"Fecha", "ID", "Nombre", "Categoria", "Valor venta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(tableModel);
        tabla.setRowHeight(30);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(230, 238, 255));
        tabla.getTableHeader().setForeground(new Color(41, 65, 122));
        tabla.setSelectionBackground(new Color(214, 232, 255));
        tabla.setSelectionForeground(new Color(28, 44, 80));
        tabla.setShowHorizontalLines(false);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setBorder(BorderFactory.createEmptyBorder());
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : new Color(247, 250, 255));
                }
                return c;
            }
        });

        JScrollPane tablaScroll = new JScrollPane(tabla);
        tablaScroll.setBorder(BorderFactory.createEmptyBorder());
        tablaScroll.setOpaque(false);
        tablaScroll.getViewport().setOpaque(false);
        tablaScroll.getViewport().setBackground(Color.WHITE);

        JPanel tablaContainer = new RoundedPanel(new BorderLayout(), Color.WHITE, new Color(220, 225, 235), 20, 1, new Color(30, 40, 80, 22), 6);
        tablaContainer.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tablaContainer.add(tablaScroll, BorderLayout.CENTER);

        JPanel resumen = new RoundedPanel(new GridLayout(2, 1, 0, 10), new Color(245, 248, 255), new Color(220, 228, 244), 18, 1, new Color(30, 40, 80, 18), 4);
        resumen.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        totalRegistrosLabel = crearTarjetaResumen("🧾 Ventas encontradas", "0", new Color(255, 247, 230), new Color(255, 153, 0), new Color(92, 58, 0));
        totalMontoLabel = crearTarjetaResumen("💰 Total vendido", "$0", new Color(232, 248, 238), new Color(18, 157, 88), new Color(11, 74, 39));
        resumen.add(wrapLabel(totalRegistrosLabel, 90));
        resumen.add(wrapLabel(totalMontoLabel, 110));

        JPanel lateralDerecha = new JPanel(new BorderLayout(0, 10));
        lateralDerecha.setOpaque(false);
        lateralDerecha.setPreferredSize(new Dimension(380, 10));
        lateralDerecha.add(filtros, BorderLayout.NORTH);
        lateralDerecha.add(resumen, BorderLayout.SOUTH);

        centro.add(tablaContainer, BorderLayout.CENTER);
        centro.add(lateralDerecha, BorderLayout.EAST);

        mainPanel.add(centro, BorderLayout.CENTER);

        recargaTimer = new Timer(220, e -> cargarVentas());
        recargaTimer.setRepeats(false);

        diaBtn.addActionListener(e -> seleccionarModo(ModoFiltro.DIA));
        semanaBtn.addActionListener(e -> seleccionarModo(ModoFiltro.SEMANA));
        mesBtn.addActionListener(e -> seleccionarModo(ModoFiltro.MES));
        anioBtn.addActionListener(e -> seleccionarModo(ModoFiltro.ANIO));
        rangoBtn.addActionListener(e -> seleccionarModo(ModoFiltro.RANGO));

        desdeSpinner.addChangeListener(e -> recargaAutomatica());
        hastaSpinner.addChangeListener(e -> recargaAutomatica());
        puntoFisicoCombo.addActionListener(e -> recargaAutomatica());

        semanaBtn.setSelected(true);
        seleccionarModo(ModoFiltro.SEMANA);
    }

    private JToggleButton crearBotonFiltro(String texto, String tooltip) {
        JToggleButton btn = new JToggleButton(texto);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(new Color(41, 61, 109));
        btn.setBackground(new Color(242, 246, 255));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(roundedBorder(new Color(197, 209, 240), 8, 10, 8, 10));

        btn.addItemListener(e -> {
            boolean seleccionado = btn.isSelected();
            btn.setBackground(seleccionado ? new Color(214, 228, 255) : new Color(242, 246, 255));
            // Mantener texto oscuro para asegurar legibilidad siempre.
            btn.setForeground(new Color(41, 61, 109));
            btn.setBorder(roundedBorder(
                    seleccionado ? new Color(94, 125, 205) : new Color(197, 209, 240),
                    8, 10, 8, 10
            ));
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.isSelected()) {
                    btn.setBackground(new Color(232, 240, 255));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.isSelected()) {
                    btn.setBackground(new Color(242, 246, 255));
                }
            }
        });
        return btn;
    }

    private JSpinner crearDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(115, 28));
        spinner.getEditor().setBorder(roundedBorder(new Color(203, 214, 242), 2, 6, 2, 6));
        return spinner;
    }

    private JLabel crearTarjetaResumen(String titulo, String valorInicial, Color fondo, Color borde, Color textoTitulo) {
        JLabel label = new RoundedLabel(18, borde, fondo, 2);
        label.setOpaque(false);
        label.setBackground(fondo);
        label.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.putClientProperty("titulo", titulo);
        label.putClientProperty("colorTitulo", textoTitulo);
        actualizarTarjetaResumen(label, valorInicial);
        return label;
    }

    private void actualizarTarjetaResumen(JLabel label, String valor) {
        String titulo = String.valueOf(label.getClientProperty("titulo"));
        Color colorTitulo = (Color) label.getClientProperty("colorTitulo");
        String colorHex = String.format("#%02X%02X%02X", colorTitulo.getRed(), colorTitulo.getGreen(), colorTitulo.getBlue());
        int valorFontSize = valor.length() > 14 ? 21 : 26;
        label.setText("<html><div style='font-size:16px;color:" + colorHex + ";'><b>" + titulo + "</b></div>"
                + "<div style='margin-top:4px;font-size:" + valorFontSize + "px;'><b>" + valor + "</b></div></html>");
    }

    private JPanel wrapLabel(JLabel label, int alturaPreferida) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, alturaPreferida));
        panel.setMinimumSize(new Dimension(0, alturaPreferida));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void seleccionarModo(ModoFiltro nuevoModo) {
        if (actualizandoModo) {
            return;
        }
        actualizandoModo = true;
        this.modoActual = nuevoModo;
        rangoPanel.setVisible(nuevoModo == ModoFiltro.RANGO);
        rangoPanel.revalidate();
        actualizandoModo = false;
        recargaAutomatica();
    }

    private void recargaAutomatica() {
        if (actualizandoModo) {
            return;
        }
        recargaTimer.restart();
    }

    private void cargarVentas() {
        try {
            Rango rango = resolverRango();
            String puntoFisicoSeleccionado = puntoFisicoCombo.getSelectedItem() == null
                    ? "Todos"
                    : puntoFisicoCombo.getSelectedItem().toString();
            String puntoFiltro = "Todos".equalsIgnoreCase(puntoFisicoSeleccionado) ? null : puntoFisicoSeleccionado;
            List<Joya> ventas = controladora.obtenerVentasEntre(rango.inicio(), rango.finExclusive(), puntoFiltro);

            tableModel.setRowCount(0);
            BigDecimal suma = BigDecimal.ZERO;

            for (Joya joya : ventas) {
                String valorVenta = obtenerValorVenta(joya);
                BigDecimal monto = parseMonto(valorVenta);
                suma = suma.add(monto);

                String idVisible = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank())
                        ? joya.getDisplayId()
                        : String.valueOf(joya.getId());

                tableModel.addRow(new Object[]{
                        joya.getFechaVendida() == null ? "-" : joya.getFechaVendida().toLocalDate().toString(),
                        idVisible,
                        joya.getNombre(),
                        joya.getCategoria(),
                        "$" + formatearMonto(monto)
                });
            }

            actualizarTarjetaResumen(totalRegistrosLabel, String.valueOf(ventas.size()));
            actualizarTarjetaResumen(totalMontoLabel, "$" + formatearMonto(suma));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar las ventas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Rango resolverRango() {
        LocalDate hoy = LocalDate.now();
        ModoFiltro tipo = modoActual == null ? ModoFiltro.DIA : modoActual;

        if (tipo == ModoFiltro.SEMANA) {
            LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
            return new Rango(inicioSemana.atStartOfDay(), inicioSemana.plusDays(7).atStartOfDay());
        }
        if (tipo == ModoFiltro.MES) {
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            return new Rango(inicioMes.atStartOfDay(), inicioMes.plusMonths(1).atStartOfDay());
        }
        if (tipo == ModoFiltro.ANIO) {
            LocalDate inicioAnio = hoy.withDayOfYear(1);
            return new Rango(inicioAnio.atStartOfDay(), inicioAnio.plusYears(1).atStartOfDay());
        }
        if (tipo == ModoFiltro.RANGO) {
            LocalDate desde = toLocalDate((Date) desdeSpinner.getValue());
            LocalDate hasta = toLocalDate((Date) hastaSpinner.getValue());
            if (hasta.isBefore(desde)) {
                throw new IllegalArgumentException("La fecha final no puede ser menor que la inicial.");
            }
            return new Rango(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        }

        return new Rango(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String obtenerValorVenta(Joya joya) {
        if (joya.getPrecioVentaReal() != null && !joya.getPrecioVentaReal().isBlank()) {
            return joya.getPrecioVentaReal();
        }
        return joya.getPrecio();
    }

    private BigDecimal parseMonto(String valor) {
        if (valor == null || valor.isBlank()) {
            return BigDecimal.ZERO;
        }
        String limpio = valor.trim()
                .replace("$", "")
                .replace("'", "")
                .replace(" ", "")
                .replace(",", "");
        if (limpio.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String formatearMonto(BigDecimal monto) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('\'');
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);
        return formatter.format(monto);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private Border roundedBorder(Color color, int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                BorderFactory.createEmptyBorder(top, left, bottom, right)
        );
    }

    private static class RoundedPanel extends JPanel {
        private final Color fillColor;
        private final Color strokeColor;
        private final int arc;
        private final int strokeWidth;
        private final Color shadowColor;
        private final int shadowSize;

        RoundedPanel(LayoutManager layout, Color fillColor, Color strokeColor, int arc, int strokeWidth) {
            this(layout, fillColor, strokeColor, arc, strokeWidth, null, 0);
        }

        RoundedPanel(LayoutManager layout, Color fillColor, Color strokeColor, int arc, int strokeWidth, Color shadowColor, int shadowSize) {
            super(layout);
            this.fillColor = fillColor;
            this.strokeColor = strokeColor;
            this.arc = arc;
            this.strokeWidth = strokeWidth;
            this.shadowColor = shadowColor;
            this.shadowSize = shadowSize;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int halfStroke = strokeWidth / 2;
            int inset = Math.max(shadowSize, 0);
            int w = getWidth() - strokeWidth - inset;
            int h = getHeight() - strokeWidth - inset;
            int x = halfStroke;
            int y = halfStroke;

            if (shadowColor != null && shadowSize > 0) {
                for (int i = shadowSize; i >= 1; i--) {
                    int alpha = Math.max(6, shadowColor.getAlpha() / (i + 1));
                    g2.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), alpha));
                    g2.fillRoundRect(x + i, y + i, w, h, arc, arc);
                }
            }

            g2.setColor(fillColor);
            g2.fillRoundRect(x, y, w, h, arc, arc);
            if (strokeColor != null && strokeWidth > 0) {
                g2.setColor(strokeColor);
                g2.setStroke(new BasicStroke(strokeWidth));
                g2.drawRoundRect(x, y, w, h, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedLabel extends JLabel {
        private final int arc;
        private final Color strokeColor;
        private final Color fillColor;
        private final int strokeWidth;

        RoundedLabel(int arc, Color strokeColor, Color fillColor, int strokeWidth) {
            this.arc = arc;
            this.strokeColor = strokeColor;
            this.fillColor = fillColor;
            this.strokeWidth = strokeWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int halfStroke = strokeWidth / 2;
            int w = getWidth() - strokeWidth;
            int h = getHeight() - strokeWidth;
            g2.setColor(fillColor);
            g2.fillRoundRect(halfStroke, halfStroke, w, h, arc, arc);
            if (strokeColor != null && strokeWidth > 0) {
                g2.setColor(strokeColor);
                g2.setStroke(new BasicStroke(strokeWidth));
                g2.drawRoundRect(halfStroke, halfStroke, w, h, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private record Rango(LocalDateTime inicio, LocalDateTime finExclusive) {
    }
}
