package igu;

import logica.Joya;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class JoyaListCellRenderer extends JPanel implements ListCellRenderer<Joya> {
    private JLabel lblNombre;
    private JLabel lblPrecio;
    private JLabel lblPeso;
    private JLabel lblAutorizacion;

    public JoyaListCellRenderer() {
        // Configuración del panel principal
        setLayout(new GridBagLayout()); // Usar GridBagLayout para control preciso
        setBackground(Color.WHITE); // Fondo blanco
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), // Borde con esquinas redondeadas
                new EmptyBorder(10, 15, 10, 15) // Márgenes internos
        ));

        // Crear etiquetas
        lblNombre = new JLabel();
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNombre.setForeground(new Color(60, 60, 60));

        lblPrecio = new JLabel();
        lblPrecio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPrecio.setForeground(new Color(80, 80, 80));

        lblPeso = new JLabel();
        lblPeso.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPeso.setForeground(new Color(80, 80, 80));

        lblAutorizacion = new JLabel();
        lblAutorizacion.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Configuración del diseño
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Espaciado entre componentes

        // Nombre (Izquierda, fila 0, columna 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Alinear a la izquierda
        add(lblNombre, gbc);

        // Precio (Derecha, fila 0, columna 1)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // Alinear a la derecha
        add(lblPrecio, gbc);

        // Peso (Debajo del precio, fila 1, columna 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST; // Alinear a la derecha
        add(lblPeso, gbc);

        // Estado autorización (debajo del nombre, fila 1, columna 0)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(lblAutorizacion, gbc);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Joya> list, Joya joya, int index, boolean isSelected, boolean cellHasFocus) {
        // Asignar valores de la joya a las etiquetas
        String idVisible = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank()) ? joya.getDisplayId() : String.valueOf(joya.getId());
        lblNombre.setText("[" + idVisible + "] " + joya.getNombre());
        lblNombre.setToolTipText("ID: " + idVisible);
        lblPrecio.setText("Precio: $" + joya.getPrecio());
        lblPeso.setText("Peso: " + joya.getPeso() + " gramos");
        lblAutorizacion.setText("Autorización: " + joya.getEstadoAutorizacionTexto());
        lblAutorizacion.setForeground(joya.isAutorizado() ? new Color(34, 139, 34) : new Color(204, 128, 0));

        // Estilo para elementos seleccionados
        if (isSelected) {
            setBackground(new Color(220, 230, 255)); // Fondo azul claro
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(150, 150, 255), 2, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
        } else {
            setBackground(Color.WHITE); // Fondo blanco
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
        }

        return this;
    }
}
