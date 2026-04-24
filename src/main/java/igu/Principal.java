package igu;

import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Principal {
    private JButton cargarDatosButton;
    private JButton verDatosButton;
    private JButton salirButton;
    private JPanel mainPanel;
    private JButton GROUP_BYButton;
    private JPanel panelFoto;
    private JButton categorias;
    private JButton socios;
    private JButton ventas;
    private final SessionContext session;

    public Principal() {
        this(null);
    }

    public Principal(SessionContext session) {
        this.session = session;
        construirUI();
        registrarAcciones();
        aplicarPermisosPorRol();
    }

    // ── Construcción de la UI en código puro (sin dependencia del compilador de forms) ──

    private void construirUI() {
        mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(UITheme.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Panel central (foto + título + versión) ─────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBackground(UITheme.BG);

        JLabel titleLabel = new JLabel("Inventario", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.F_TITLE);
        titleLabel.setForeground(UITheme.TEXT);
        centerPanel.add(titleLabel, BorderLayout.NORTH);

        panelFoto = new JPanel(new BorderLayout());
        panelFoto.setBackground(UITheme.BG);
        cargarImagenEnPanelFoto();
        centerPanel.add(panelFoto, BorderLayout.CENTER);

        JLabel versionLabel = new JLabel("@CamTe 1.5.7", SwingConstants.CENTER);
        versionLabel.setFont(UITheme.F_SMALL);
        versionLabel.setForeground(UITheme.TEXT_MUTED);
        centerPanel.add(versionLabel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ── Panel de botones (derecha) ──────────────────────────────────────────
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(UITheme.BG);
        buttonsPanel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.roundedBorder(UITheme.BORDER, 14),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)
        ));

        cargarDatosButton = crearBotonPrimary("Cargar Datos", "/file.png");
        verDatosButton    = crearBotonPrimary("Ver Datos",    "/Ver.png");
        GROUP_BYButton    = crearBotonPrimary("Group By",     "/espia.png");
        ventas            = crearBotonSecondary("Ventas");
        socios            = crearBotonSecondary("Socios");
        categorias        = crearBotonSecondary("Categorías");
        salirButton       = UITheme.dangerBtn("Salir");
        salirButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        salirButton.setPreferredSize(new Dimension(150, 36));
        salirButton.setMaximumSize(new Dimension(160, 38));

        buttonsPanel.add(cargarDatosButton);
        buttonsPanel.add(Box.createVerticalStrut(6));
        buttonsPanel.add(verDatosButton);
        buttonsPanel.add(Box.createVerticalStrut(6));
        buttonsPanel.add(GROUP_BYButton);
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(ventas);
        buttonsPanel.add(Box.createVerticalStrut(4));
        buttonsPanel.add(socios);
        buttonsPanel.add(Box.createVerticalStrut(4));
        buttonsPanel.add(categorias);
        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(salirButton);

        mainPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    private JButton crearBotonPrimary(String texto, String iconPath) {
        JButton btn = UITheme.primaryBtn(texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(150, 36));
        btn.setMaximumSize(new Dimension(160, 38));
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                Image scaled = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(scaled));
            } catch (Exception ignored) {}
        }
        return btn;
    }

    private JButton crearBotonSecondary(String texto) {
        JButton btn = UITheme.secondaryBtn(texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(150, 36));
        btn.setMaximumSize(new Dimension(160, 38));
        return btn;
    }

    // ── Acciones ────────────────────────────────────────────────────────────────

    private void registrarAcciones() {
        cargarDatosButton.addActionListener(e -> {
            JFrame frame = new JFrame("Cargar Datos");
            frame.setContentPane(new CargarDatos(null, session).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(800, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        verDatosButton.addActionListener(e -> {
            JFrame frame = new JFrame("Ver Datos");
            frame.setContentPane(new VerDatos(null, session).getMainPanel());
            frame.setSize(1400, 1000);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        GROUP_BYButton.addActionListener(e -> {
            if (session != null && session.isAdmin()) {
                Object[] opciones = {"Group By", "Aprobaciones"};
                int opcion = JOptionPane.showOptionDialog(
                        mainPanel,
                        "Seleccione la vista que desea abrir.",
                        "Panel administrador",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        opciones,
                        opciones[0]
                );
                if (opcion == 1) {
                    AprobacionesDialog dialog = new AprobacionesDialog(null, session);
                    dialog.setVisible(true);
                    return;
                }
            }
            JFrame frame = new JFrame("Group By");
            frame.setContentPane(new GroupBy(null, session).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        salirButton.addActionListener(e -> System.exit(0));

        categorias.addActionListener(e -> {
            JFrame frame = new JFrame("Categorías");
            frame.setContentPane(new Categorias(null, session).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        socios.addActionListener(e -> {
            JFrame frame = new JFrame("Socios");
            frame.setContentPane(new Socios(null, session).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        ventas.addActionListener(e -> {
            VentasDialog dialog = new VentasDialog(
                    (Frame) SwingUtilities.getWindowAncestor(mainPanel),
                    session
            );
            dialog.setVisible(true);
        });
    }

    private void aplicarPermisosPorRol() {
        if (session == null) return;
        boolean esAdmin = session.isAdmin();
        categorias.setEnabled(esAdmin);
        socios.setEnabled(esAdmin);
        ventas.setEnabled(esAdmin);
    }

    private void cargarImagenEnPanelFoto() {
        if (session != null && session.tienda() == SessionContext.Tienda.QUEENS) {
            panelFoto.add(crearTituloQueens(), BorderLayout.CENTER);
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/aca.png"));
            Image scaled = icon.getImage().getScaledInstance(260, 260, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(scaled));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            panelFoto.add(lbl, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel lbl = new JLabel("Imagen no disponible", SwingConstants.CENTER);
            lbl.setForeground(UITheme.TEXT_MUTED);
            panelFoto.add(lbl, BorderLayout.CENTER);
        }
    }

    private JPanel crearTituloQueens() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // fondo con gradiente suave marfil → blanco
                GradientPaint bgGrad = new GradientPaint(0, 0, new Color(255, 252, 240), 0, h, Color.WHITE);
                g2.setPaint(bgGrad);
                g2.fill(new RoundRectangle2D.Float(w * .06f, h * .05f, w * .88f, h * .90f, 28, 28));

                // borde dorado sutil
                g2.setColor(new Color(200, 160, 60, 80));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(new RoundRectangle2D.Float(w * .06f, h * .05f, w * .88f - 1, h * .90f - 1, 28, 28));

                // corona unicode centrada arriba
                Font coronaFont = new Font("Serif", Font.PLAIN, 38);
                g2.setFont(coronaFont);
                FontMetrics fmC = g2.getFontMetrics();
                String corona = "\u2655";
                int cx = (w - fmC.stringWidth(corona)) / 2;
                int cy = (int)(h * 0.35f);
                GradientPaint crownGrad = new GradientPaint(cx, cy - fmC.getAscent(), new Color(218, 165, 32), cx, cy, new Color(255, 200, 50));
                g2.setPaint(crownGrad);
                g2.drawString(corona, cx, cy);

                // línea decorativa izquierda y derecha de la corona
                int lineY  = (int)(h * 0.38f);
                int lineX1 = (int)(w * 0.12f);
                int lineX2 = (int)(w * 0.88f);
                int textCenterX = w / 2;
                int gapHalf = fmC.stringWidth(corona) / 2 + 14;
                g2.setStroke(new BasicStroke(1f));
                GradientPaint lineGrad1 = new GradientPaint(lineX1, lineY, new Color(200,160,60,0), textCenterX - gapHalf, lineY, new Color(200,160,60,180));
                g2.setPaint(lineGrad1);
                g2.drawLine(lineX1, lineY, textCenterX - gapHalf, lineY);
                GradientPaint lineGrad2 = new GradientPaint(textCenterX + gapHalf, lineY, new Color(200,160,60,180), lineX2, lineY, new Color(200,160,60,0));
                g2.setPaint(lineGrad2);
                g2.drawLine(textCenterX + gapHalf, lineY, lineX2, lineY);

                // texto "Queens" con gradiente dorado
                Font queenFont = new Font("Times New Roman", Font.BOLD | Font.ITALIC, 58);
                g2.setFont(queenFont);
                FontMetrics fmQ = g2.getFontMetrics();
                String texto = "Queens";
                int tx = (w - fmQ.stringWidth(texto)) / 2;
                int ty = (int)(h * 0.62f);
                GradientPaint textGrad = new GradientPaint(tx, ty - fmQ.getAscent(), new Color(180, 130, 20), tx, ty, new Color(240, 190, 60));
                g2.setPaint(textGrad);
                g2.drawString(texto, tx, ty);

                // subtítulo "Joyería"
                Font subFont = new Font("Serif", Font.ITALIC, 16);
                g2.setFont(subFont);
                FontMetrics fmS = g2.getFontMetrics();
                String sub = "Joyería";
                int sx = (w - fmS.stringWidth(sub)) / 2;
                int sy = (int)(h * 0.75f);
                g2.setColor(new Color(160, 120, 20, 200));
                g2.drawString(sub, sx, sy);

                // línea separadora inferior dorada (dos mitades: fade-in y fade-out)
                int sepY  = (int)(h * 0.80f);
                int sepX1 = (int)(w * 0.25f);
                int sepMid = w / 2;
                int sepX2 = (int)(w * 0.75f);
                g2.setStroke(new BasicStroke(1f));
                g2.setPaint(new GradientPaint(sepX1, sepY, new Color(200,160,60,0), sepMid, sepY, new Color(200,160,60,200)));
                g2.drawLine(sepX1, sepY, sepMid, sepY);
                g2.setPaint(new GradientPaint(sepMid, sepY, new Color(200,160,60,200), sepX2, sepY, new Color(200,160,60,0)));
                g2.drawLine(sepMid, sepY, sepX2, sepY);
            }
        };
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
