package igu;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public final class ModernUI {
    static final Color BG = new Color(244, 247, 252);
    static final Color SURFACE = Color.WHITE;
    static final Color SURFACE_ALT = new Color(249, 251, 255);
    static final Color BORDER = new Color(220, 227, 239);
    static final Color PRIMARY = new Color(78, 89, 214);
    static final Color PRIMARY_DARK = new Color(55, 67, 180);
    static final Color SUCCESS = new Color(26, 140, 84);
    static final Color TEXT = new Color(37, 45, 69);
    static final Color MUTED = new Color(108, 117, 146);
    static final Color DANGER = new Color(201, 69, 92);

    private ModernUI() {
    }

    static JPanel createRoundedPanel(LayoutManager layout) {
        return new RoundedPanel(layout, SURFACE, BORDER, 28, 1, new Color(30, 41, 73, 20), 6);
    }

    static JPanel createTintedPanel(LayoutManager layout, Color fill) {
        return new RoundedPanel(layout, fill, BORDER, 28, 1, new Color(30, 41, 73, 18), 5);
    }

    static JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 28));
        label.setForeground(TEXT);
        return label;
    }

    static JLabel createSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(MUTED);
        return label;
    }

    static JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(TEXT);
        return label;
    }

    static void stylePrimaryButton(AbstractButton button, String text) {
        styleButton(button, text, new Color(218, 227, 255), PRIMARY_DARK, new Color(168, 182, 238));
    }

    static void styleSecondaryButton(AbstractButton button, String text) {
        styleButton(button, text, new Color(235, 240, 255), TEXT, new Color(215, 223, 245));
    }

    static void styleDangerButton(AbstractButton button, String text) {
        styleButton(button, text, new Color(255, 235, 239), DANGER, new Color(245, 202, 211));
    }

    static void styleChip(AbstractButton button, String text) {
        button.setText(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(new Color(239, 243, 255));
        button.setForeground(TEXT);
        button.setBorder(compoundBorder(new Color(214, 223, 245), 8, 14, 8, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(SURFACE_ALT);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(compoundBorder(BORDER, 10, 12, 10, 12));
    }

    static void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setBackground(SURFACE_ALT);
        combo.setForeground(TEXT);
        combo.setBorder(compoundBorder(BORDER, 8, 10, 8, 10));
    }

    static void styleTextArea(JTextArea area) {
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setBackground(SURFACE_ALT);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    static JScrollPane wrapScroll(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(compoundBorder(BORDER, 0, 0, 0, 0));
        scrollPane.getViewport().setBackground(SURFACE_ALT);
        scrollPane.setBackground(SURFACE_ALT);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(14);
        stylizeScrollBar(scrollPane.getVerticalScrollBar());
        stylizeScrollBar(scrollPane.getHorizontalScrollBar());
        return scrollPane;
    }

    static Border compoundBorder(Color borderColor, int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(top, left, bottom, right)
        );
    }

    public static void applyGlobalTheme() {
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.disabledText", TEXT.darker());
        UIManager.put("Button.arc", 18);
        UIManager.put("Component.arc", 18);
        UIManager.put("TextComponent.arc", 18);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", new Color(226, 234, 255));
        UIManager.put("ComboBox.selectionForeground", TEXT);
    }

    private static void styleButton(AbstractButton button, String text, Color bg, Color fg, Color border) {
        button.setText(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(compoundBorder(border, 10, 16, 10, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private static void stylizeScrollBar(JScrollBar scrollBar) {
        scrollBar.setOpaque(false);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(189, 198, 222);
                trackColor = new Color(241, 244, 252);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 12, 12);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRoundRect(trackBounds.x + 3, trackBounds.y, trackBounds.width - 6, trackBounds.height, 12, 12);
                g2.dispose();
            }

            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    static final class RoundedPanel extends JPanel {
        private final Color fillColor;
        private final Color strokeColor;
        private final int arc;
        private final int strokeWidth;
        private final Color shadowColor;
        private final int shadowSize;

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
}

