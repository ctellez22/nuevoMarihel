package igu;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Paleta de colores, fuentes y componentes estilizados para toda la app. */
public final class UITheme {

    private UITheme() {}

    // ── Colores ───────────────────────────────────────────────────────────────
    public static final Color BG         = new Color(255, 255, 255);
    public static final Color BG_LIGHT   = new Color(250, 250, 250);
    public static final Color TEXT       = new Color(20,  20,  20);
    public static final Color TEXT_MUTED = new Color(110, 110, 110);
    public static final Color BORDER     = new Color(220, 220, 220);
    public static final Color ACCENT     = new Color(20,  20,  20);   // negro
    public static final Color DANGER     = new Color(192, 57,  43);
    public static final Color HOVER_BTN  = new Color(55,  55,  55);
    public static final Color HOVER_SEC  = new Color(245, 245, 245);

    // ── Fuentes ───────────────────────────────────────────────────────────────
    public static final Font F_TITLE   = new Font("Times New Roman", Font.BOLD | Font.ITALIC, 44);
    public static final Font F_SECTION = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font F_LABEL   = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font F_BODY    = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font F_SMALL   = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font F_FILTER  = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 13);

    // ── Bordes ────────────────────────────────────────────────────────────────
    public static Border roundedBorder(Color color, int arc) {
        return new Border() {
            private final Insets ins = new Insets(1, 1, 1, 1);
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
                g2.dispose();
            }
            @Override public Insets getBorderInsets(Component c) { return ins; }
            @Override public boolean isBorderOpaque() { return false; }
        };
    }

    public static Border paddedRound(Color color, int arc, int v, int h) {
        return BorderFactory.createCompoundBorder(
                roundedBorder(color, arc),
                BorderFactory.createEmptyBorder(v, h, v, h)
        );
    }

    // ── Botones ───────────────────────────────────────────────────────────────
    /** Botón primario: fondo negro, texto blanco */
    public static JButton primaryBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled()
                        ? (getModel().isRollover() ? HOVER_BTN : ACCENT)
                        : BORDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(F_LABEL);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Botón secundario: fondo blanco con borde gris */
    public static JButton secondaryBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? HOVER_SEC : BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(TEXT);
        btn.setFont(F_LABEL);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Botón de peligro: fondo rojo, texto blanco */
    public static JButton dangerBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled()
                        ? (getModel().isRollover() ? new Color(211, 68, 50) : DANGER)
                        : BORDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(F_LABEL);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Campos de texto ───────────────────────────────────────────────────────
    public static JTextField styledField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    public static void styleField(JTextField f) {
        f.setFont(F_BODY);
        f.setForeground(TEXT);
        f.setBackground(BG);
        f.setBorder(paddedRound(BORDER, 10, 5, 8));
    }

    public static void styleArea(JTextArea a) {
        a.setFont(F_BODY);
        a.setForeground(TEXT);
        a.setBackground(BG);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(F_BODY);
        c.setBackground(BG);
        c.setForeground(TEXT);
    }

    // ── Paneles con borde redondeado ──────────────────────────────────────────
    public static JPanel card(int arc) {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                roundedBorder(BORDER, arc),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        return p;
    }

    /** ScrollPane sin borde estándar, con borde redondeado propio */
    public static JScrollPane styledScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(roundedBorder(BORDER, 12));
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG);
        return sp;
    }

    /** Separador de sección con título */
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SECTION);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_BODY);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    /** Configura el fondo base de un panel (blanco). */
    public static void applyBase(JPanel p) {
        p.setBackground(BG);
    }
}
