package igu;

import javax.swing.*;
import java.awt.*;

public class LoadingScreen {
    private final JDialog dialog;

    public LoadingScreen(JFrame owner) {
        dialog = new JDialog(owner, "Cargando...", true);
        dialog.setUndecorated(true); // Sin bordes ni barra de título
        dialog.setSize(250, 300);
        dialog.setLocationRelativeTo(owner);

        // Configuración del panel principal con bordes redondeados
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo degradado con colores suaves
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(173, 216, 230), // Azul pastel
                        getWidth(), getHeight(), new Color(135, 206, 235) // Azul cielo
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Bordes redondeados
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Imagen central
        JLabel imageLabel = new JLabel(new ImageIcon(getClass().getResource("/camilo.png")));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Espaciado alrededor de la imagen
        panel.add(imageLabel, BorderLayout.NORTH);

        // Texto principal
        JLabel loadingMessage = new JLabel("Dame un momento...");
        loadingMessage.setFont(new Font("SansSerif", Font.BOLD, 16));
        loadingMessage.setForeground(new Color(60, 60, 60)); // Gris oscuro para contraste
        loadingMessage.setHorizontalAlignment(SwingConstants.CENTER);
        loadingMessage.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(loadingMessage, BorderLayout.CENTER);

        // Texto secundario
        JLabel secondaryMessage = new JLabel("Arreglando una pingada");
        secondaryMessage.setFont(new Font("SansSerif", Font.ITALIC, 12));
        secondaryMessage.setForeground(new Color(90, 90, 90)); // Gris suave
        secondaryMessage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(secondaryMessage, BorderLayout.SOUTH);

        // Añadir el panel al diálogo
        dialog.getContentPane().add(panel);
    }

    public void show() {
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    public void hide() {
        SwingUtilities.invokeLater(() -> dialog.dispose());
    }
}
