package igu;

import com.marihel.utils.FormatterUtils;
import logica.Joya;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class JoyaListCellRenderer extends JPanel implements ListCellRenderer<Joya> {
    private final JLabel lblNombre;
    private final JLabel lblPrecio;
    private final JLabel lblPeso;
    private final JLabel lblAutorizacion;

    public JoyaListCellRenderer() {
        setLayout(new BorderLayout(0, 6));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(14, 20, 14, 20)
        ));

        lblNombre = new JLabel();
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNombre.setForeground(new Color(20, 20, 20));

        lblPrecio = new JLabel();
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblPrecio.setForeground(new Color(30, 30, 30));
        lblPrecio.setHorizontalAlignment(SwingConstants.RIGHT);

        lblPeso = new JLabel();
        lblPeso.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblPeso.setForeground(new Color(90, 90, 90));
        lblPeso.setHorizontalAlignment(SwingConstants.RIGHT);

        lblAutorizacion = new JLabel();
        lblAutorizacion.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Fila superior: nombre (izquierda) | precio (derecha)
        JPanel filaTop = new JPanel(new BorderLayout());
        filaTop.setOpaque(false);
        filaTop.add(lblNombre, BorderLayout.CENTER);
        filaTop.add(lblPrecio, BorderLayout.EAST);

        // Fila inferior: autorización (izquierda) | peso (derecha)
        JPanel filaBot = new JPanel(new BorderLayout());
        filaBot.setOpaque(false);
        filaBot.add(lblAutorizacion, BorderLayout.CENTER);
        filaBot.add(lblPeso, BorderLayout.EAST);

        add(filaTop, BorderLayout.NORTH);
        add(filaBot, BorderLayout.SOUTH);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Joya> list, Joya joya, int index, boolean isSelected, boolean cellHasFocus) {
        String idVisible = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank())
                ? joya.getDisplayId() : String.valueOf(joya.getId());

        lblNombre.setText("[" + idVisible + "] " + joya.getNombre());
        lblPrecio.setText("$" + joya.getPrecio());
        lblPeso.setText(FormatterUtils.formatearPeso(joya.getPeso()) + " g");
        lblAutorizacion.setText(joya.getEstadoAutorizacionTexto());
        lblAutorizacion.setForeground(joya.isAutorizado() ? new Color(34, 139, 34) : new Color(204, 128, 0));

        if (isSelected) {
            setBackground(new Color(235, 240, 255));
        } else {
            setBackground(index % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
        }

        return this;
    }
}
