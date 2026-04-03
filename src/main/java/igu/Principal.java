package igu;

import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Principal {
    private JButton cargarDatosButton;
    private JButton verDatosButton;
    private JButton salirButton;
    private JPanel mainPanel; // Contenedor principal de la ventana
    private JButton GROUP_BYButton;
    private JPanel panelFoto;
    private JButton categorias;
    private JButton socios;
    private final SessionContext session;

    public Principal() {
        this(null);
    }

    public Principal(SessionContext session) {
        this.session = session;

        //Cargar una imagen en panelFoto al iniciar
        cargarImagenEnPanelFoto();


        // Acción para el botón "Cargar Datos"
        cargarDatosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Crear y mostrar la ventana de cargar datos
                JFrame frame = new JFrame("Cargar Datos");
                frame.setContentPane(new CargarDatos(null, session).getMainPanel());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);



            }
        });

        // Acción para el botón "Ver Datos" (implementa según necesidad)
        verDatosButton.addActionListener(e -> {
            JFrame frame = new JFrame("Ver Datos");
            frame.setContentPane(new VerDatos(null, session).getMainPanel());
            frame.setSize(1400, 1000);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
        // Acción para el botón "Contar"
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

        // Acción para el botón "Salir"
        salirButton.addActionListener(e -> System.exit(0));

        // Acción para el botón "Categorías"
        categorias.addActionListener(e -> {
            JFrame frame = new JFrame("Categorías");
            frame.setContentPane(new Categorias(null).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        // Acción para el botón "Socios"
        socios.addActionListener(e -> {
            JFrame frame = new JFrame("Socios");
            frame.setContentPane(new Socios(null).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        aplicarPermisosPorRol();
    }

    private void aplicarPermisosPorRol() {
        if (session == null) {
            return;
        }
        boolean esAdmin = session.isAdmin();
        categorias.setEnabled(esAdmin);
        socios.setEnabled(esAdmin);
    }

    private void cargarImagenEnPanelFoto() {
        // Ruta de la imagen (ajusta la ruta según la ubicación de tu imagen)
        String imagePath = "/aca.png";

        try {
            // Cargar y escalar la imagen
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(imagePath));
            Image scaledImage = originalIcon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            // Crear un JLabel con la imagen
            JLabel imageLabel = new JLabel(scaledIcon);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Configurar el panelFoto para mostrar la imagen
            panelFoto.setLayout(new BorderLayout());
            panelFoto.add(imageLabel, BorderLayout.CENTER);
            panelFoto.revalidate(); // Asegurar que el panel se actualice
            panelFoto.repaint();   // Redibujar el panel
        } catch (Exception e) {
            // Mostrar un mensaje si no se encuentra la imagen
            JLabel errorLabel = new JLabel("Imagen no disponible");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            panelFoto.setLayout(new BorderLayout());
            panelFoto.add(errorLabel, BorderLayout.CENTER);
            panelFoto.revalidate();
            panelFoto.repaint();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
