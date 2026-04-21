package igu;

import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TiendaSelectorDialog extends JDialog {

    private SessionContext.Tienda tiendaSeleccionada = null;

    public TiendaSelectorDialog() {
        super((Frame) null, "Seleccionar Joyería", true);
        setUndecorated(true);
        setSize(580, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        // ── Encabezado ───────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);

        JLabel lblTitulo = new JLabel("Bienvenida", SwingConstants.CENTER);
        lblTitulo.setFont(UITheme.F_TITLE.deriveFont(32f));
        lblTitulo.setForeground(UITheme.TEXT);

        JLabel lblSub = new JLabel("Selecciona la joyería a la que deseas entrar", SwingConstants.CENTER);
        lblSub.setFont(UITheme.F_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // ── Tarjetas ─────────────────────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 0));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));

        cards.add(crearTarjeta("Marihel", "Joyería Marihel", SessionContext.Tienda.MARIHEL));
        cards.add(crearTarjeta("Queens",  "Joyería Queens",  SessionContext.Tienda.QUEENS));

        root.add(cards, BorderLayout.CENTER);

        // ── Pie ──────────────────────────────────────────────────────────────────
        JLabel lblCerrar = new JLabel("Presiona Esc para salir", SwingConstants.CENTER);
        lblCerrar.setFont(UITheme.F_SMALL);
        lblCerrar.setForeground(UITheme.TEXT_MUTED);
        lblCerrar.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        root.add(lblCerrar, BorderLayout.SOUTH);

        setContentPane(root);

        // Esc cierra sin selección
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel crearTarjeta(String titulo, String subtitulo, SessionContext.Tienda tienda) {
        Color colorNormal = new Color(22, 22, 22);
        Color colorHover  = new Color(45, 45, 45);

        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            private boolean hover = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        tiendaSeleccionada = tienda;
                        dispose();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? colorHover : colorNormal);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }

            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Nombre
        JLabel lblNombre = new JLabel(titulo, SwingConstants.CENTER);
        lblNombre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        lblNombre.setForeground(Color.WHITE);

        // Subtítulo
        JLabel lblSub = new JLabel(subtitulo, SwingConstants.CENTER);
        lblSub.setFont(UITheme.F_BODY);
        lblSub.setForeground(new Color(180, 180, 180));

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 6));
        centro.setOpaque(false);
        centro.add(lblNombre);
        centro.add(lblSub);

        card.add(centro, BorderLayout.CENTER);

        JLabel lblEntrar = new JLabel("Entrar →", SwingConstants.CENTER);
        lblEntrar.setFont(UITheme.F_LABEL);
        lblEntrar.setForeground(new Color(200, 200, 200));
        card.add(lblEntrar, BorderLayout.SOUTH);

        return card;
    }

    public SessionContext.Tienda getTiendaSeleccionada() {
        return tiendaSeleccionada;
    }
}
