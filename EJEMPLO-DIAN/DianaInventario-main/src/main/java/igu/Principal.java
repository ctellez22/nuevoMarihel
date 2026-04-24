package igu;

import logica.Controladora;
import logica.OrdenTrabajo;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Principal {
    private JButton cargarDatosButton;
    private JButton verDatosButton;
    private JButton salirButton;
    private JPanel mainPanel; // Contenedor principal de la ventana
    private JButton GROUP_BYButton;
    private JPanel panelFoto;
    private JButton categorias;
    private JButton socios;
    private JButton joyeros;
    private JButton ventas;
    private JButton lotes;
    private JButton ordenTrabajo;
    private final SessionContext session;
    private final Controladora controladora;
    private Long ordenSeleccionadaId = null;
    private JPanel badgeSeleccionado = null;
    private static final Color COLOR_BADGE_NORMAL = new Color(247, 249, 255);
    private static final Color COLOR_BADGE_SELECCIONADO = new Color(210, 220, 255);

    public Principal() {
        this(null);
    }

    public Principal(SessionContext session) {
        this.session = session;
        this.controladora = new Controladora();

        // Inicializar botones que no están en el archivo .form
        if (this.lotes == null) {
            this.lotes = new JButton();
        }
        if (this.joyeros == null) {
            this.joyeros = new JButton();
        }
        if (this.ordenTrabajo == null) {
            this.ordenTrabajo = new JButton();
        }

        // Acción para el botón "Cargar Datos"
        cargarDatosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Crear y mostrar la ventana de cargar datos
                JFrame frame = new JFrame("Cargar Datos");
                frame.setContentPane(new CargarDatos(null, session).getMainPanel());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana
                frame.setSize(1180, 900);
                frame.setMinimumSize(new Dimension(1100, 820));
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

        joyeros.addActionListener(e -> {
            JFrame frame = new JFrame("Joyeros");
            frame.setContentPane(new Joyeros(null).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        ventas.addActionListener(e -> {
            if (session != null && !session.isAdmin()) {
                JOptionPane.showMessageDialog(mainPanel, "Solo un administrador puede acceder al panel de ventas.", "Permiso denegado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFrame frame = new JFrame("Ventas");
            frame.setContentPane(new VentasAdminPanel().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1360, 860);
            frame.setMinimumSize(new Dimension(1240, 780));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        lotes.addActionListener(e -> {
            JFrame frame = new JFrame("Lotes");
            frame.setContentPane(new Lotes(frame, session).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        ordenTrabajo.addActionListener(e -> {
            JFrame frame = new JFrame("Orden de trabajo");
            frame.setContentPane(new OrdenTrabajoPanel(frame, session).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1200, 820);
            frame.setMinimumSize(new Dimension(1100, 760));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        aplicarPermisosPorRol();
        reconstruirInterfaz();
    }

    private void reconstruirInterfaz() {
        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout(18, 18));
        mainPanel.setBackground(ModernUI.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel header = new ModernUI.RoundedPanel(new BorderLayout(12, 12), ModernUI.PRIMARY, new Color(92, 108, 224), 30, 1, new Color(32, 42, 88, 36), 8);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titulo = new JLabel("Inventario general ✨");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);

        String rol = session == null ? "Invitado" : (session.isAdmin() ? "Administrador" : "Vendedor");
        String usuario = session == null ? "Modo local" : session.username();
        JLabel subtitulo = new JLabel("Hola, " + usuario + " · Rol: " + rol + " · Gestiona joyas, lotes y ventas desde un panel renovado.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(228, 234, 255));

        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 6));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        JButton salirHero = new JButton();
        ModernUI.styleDangerButton(salirHero, "Salir");
        salirHero.addActionListener(e -> salirButton.doClick());

        header.add(textos, BorderLayout.CENTER);
        header.add(salirHero, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(18, 18));
        center.setOpaque(false);

        JPanel acciones = new JPanel(new GridLayout(0, 2, 16, 16));
        acciones.setOpaque(false);
        acciones.add(crearTarjetaAcceso("Cargar datos", "Registra joyas nuevas, costeo y piedras por lote.", cargarDatosButton, true));
        acciones.add(crearTarjetaAcceso("Ver inventario", "Consulta, filtra y revisa todas las joyas cargadas.", verDatosButton, false));
        acciones.add(crearTarjetaAcceso("Reportes y aprobaciones", "Group by, aprobaciones y monitoreo administrativo.", GROUP_BYButton, false));
        acciones.add(crearTarjetaAcceso("Ventas", "Consulta ventas del negocio con filtros modernos.", ventas, false));
        acciones.add(crearTarjetaAcceso("Categorías", "Gestiona categorías del sistema.", categorias, false));
        acciones.add(crearTarjetaAcceso("Socios", "Administra socios y aliados del inventario.", socios, false));
        acciones.add(crearTarjetaAcceso("Joyeros", "Gestiona el catálogo de joyeros para órdenes de trabajo.", joyeros, false));
        acciones.add(crearTarjetaAcceso("Lotes", "Ve y gestiona todos los lotes creados en el sistema.", lotes, false));
        acciones.add(crearTarjetaAcceso("Orden de trabajo", "Asigna joyas a joyero y deja su estado en pendiente de inmediato.", ordenTrabajo, false));

        JPanel visual = ModernUI.createRoundedPanel(new BorderLayout(12, 12));
        visual.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel sideTitle = ModernUI.createSectionTitle("Resumen rápido");
        JLabel sideText = ModernUI.createSubtitle("Órdenes de trabajo por vencer con días restantes y prioridad visual.");
        JPanel sideHeader = new JPanel(new GridLayout(0, 1, 0, 6));
        sideHeader.setOpaque(false);
        sideHeader.add(sideTitle);
        sideHeader.add(sideText);

        panelFoto.removeAll();
        panelFoto.setOpaque(false);
        panelFoto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actualizarResumenRapidoOrdenes();

        JButton btnRefrescar = new JButton();
        ModernUI.styleSecondaryButton(btnRefrescar, "Refrescar");
        btnRefrescar.addActionListener(e -> {
            ordenSeleccionadaId = null;
            badgeSeleccionado = null;
            actualizarResumenRapidoOrdenes();
        });

        JButton btnCompletar = new JButton();
        ModernUI.stylePrimaryButton(btnCompletar, "Marcar como completado");
        btnCompletar.addActionListener(e -> {
            if (ordenSeleccionadaId == null) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Seleccione una orden de la lista antes de marcarla como completada.",
                        "Sin selección", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                    "¿Marcar la OT #" + ordenSeleccionadaId + " como completada?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                controladora.completarOrdenTrabajo(ordenSeleccionadaId);
                ordenSeleccionadaId = null;
                badgeSeleccionado = null;
                actualizarResumenRapidoOrdenes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Error al completar la orden: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel botonesLateral = new JPanel(new GridLayout(1, 2, 8, 0));
        botonesLateral.setOpaque(false);
        botonesLateral.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        botonesLateral.add(btnRefrescar);
        botonesLateral.add(btnCompletar);

        JPanel northPanel = new JPanel(new BorderLayout(0, 0));
        northPanel.setOpaque(false);
        northPanel.add(sideHeader, BorderLayout.NORTH);
        northPanel.add(botonesLateral, BorderLayout.SOUTH);

        visual.add(northPanel, BorderLayout.NORTH);
        visual.add(panelFoto, BorderLayout.CENTER);

        center.add(acciones, BorderLayout.CENTER);
        center.add(visual, BorderLayout.EAST);

        JScrollPane scroll = ModernUI.wrapScroll(center);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(ModernUI.BG);

        JLabel footer = new JLabel("Camte. 1.1.0");
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setForeground(ModernUI.MUTED);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JPanel crearTarjetaAcceso(String titulo, String descripcion, JButton boton, boolean primaria) {
        JPanel card = ModernUI.createRoundedPanel(new BorderLayout(10, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(280, 150));

        JLabel title = new JLabel(titulo);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ModernUI.TEXT);

        JTextArea desc = new JTextArea(descripcion);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(ModernUI.MUTED);

        if (primaria) {
            ModernUI.stylePrimaryButton(boton, "Abrir");
        } else {
            ModernUI.styleSecondaryButton(boton, "Abrir");
        }

        JPanel texto = new JPanel(new BorderLayout(0, 8));
        texto.setOpaque(false);
        texto.add(title, BorderLayout.NORTH);
        texto.add(desc, BorderLayout.CENTER);

        card.add(texto, BorderLayout.CENTER);
        card.add(boton, BorderLayout.SOUTH);
        return card;
    }

    private JPanel crearBadge(String titulo, String descripcion) {
        JPanel badge = ModernUI.createTintedPanel(new BorderLayout(0, 6), new Color(247, 249, 255));
        badge.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel(titulo);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(ModernUI.TEXT);

        JLabel desc = new JLabel("<html><div style='width:240px'>" + descripcion + "</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(ModernUI.MUTED);

        badge.add(title, BorderLayout.NORTH);
        badge.add(desc, BorderLayout.CENTER);
        return badge;
    }

    private void aplicarPermisosPorRol() {
        if (session == null) {
            return;
        }
        boolean esAdmin = session.isAdmin();
        categorias.setEnabled(esAdmin);
        socios.setEnabled(esAdmin);
        joyeros.setEnabled(esAdmin);
        ventas.setEnabled(esAdmin);
    }

    private void actualizarResumenRapidoOrdenes() {
        panelFoto.removeAll();
        panelFoto.setLayout(new BorderLayout(0, 0));

        List<OrdenResumen> resumenes = obtenerOrdenesProximas();

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        lista.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        if (resumenes.isEmpty()) {
            JLabel vacio = new JLabel("Sin órdenes pendientes.");
            vacio.setFont(new Font("SansSerif", Font.ITALIC, 12));
            vacio.setForeground(ModernUI.MUTED);
            vacio.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            lista.add(vacio);
        } else {
            for (OrdenResumen item : resumenes) {
                JPanel badge = crearBadgeOrden(item);
                badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, badge.getPreferredSize().height));
                lista.add(badge);
                lista.add(Box.createVerticalStrut(2));
            }
        }

        JScrollPane scroll = ModernUI.wrapScroll(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(ModernUI.SURFACE);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelFoto.add(scroll, BorderLayout.CENTER);
        panelFoto.revalidate();
        panelFoto.repaint();
    }

    private List<OrdenResumen> obtenerOrdenesProximas() {
        List<OrdenTrabajo> ordenes = controladora.obtenerOrdenesTrabajo(session);
        List<OrdenResumen> proximas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (OrdenTrabajo orden : ordenes) {
            if (orden == null || orden.getFechaEntrega() == null) {
                continue;
            }
            String estado = orden.getEstado() == null ? "" : orden.getEstado().trim();
            if (!estado.isBlank() && !"pendiente".equalsIgnoreCase(estado)) {
                continue;
            }

            LocalDate fechaEntrega = orden.getFechaEntrega().toLocalDate();
            long dias = ChronoUnit.DAYS.between(hoy, fechaEntrega);

            LocalDate fechaEnvio = orden.getFechaEnvio() == null ? null : orden.getFechaEnvio().toLocalDate();

            String idJoya = (orden.getJoyaDisplayId() == null || orden.getJoyaDisplayId().isBlank())
                    ? "#" + orden.getJoyaId()
                    : orden.getJoyaDisplayId();
            String nombreJoya = orden.getJoyaNombre() == null ? "" : orden.getJoyaNombre().trim();
            String referencia = idJoya + (nombreJoya.isBlank() ? "" : " · " + nombreJoya);

            String detalle = orden.getDetalle() == null ? "" : orden.getDetalle().trim();

            proximas.add(new OrdenResumen(
                    orden.getId(),
                    referencia,
                    orden.getJoyero() == null ? "Sin joyero" : orden.getJoyero().trim(),
                    fechaEnvio,
                    fechaEntrega,
                    dias,
                    detalle
            ));
        }

        proximas.sort(Comparator.comparingLong(OrdenResumen::diasRestantes)
                .thenComparing(OrdenResumen::fechaEntrega));
        return proximas;
    }

    private JPanel crearBadgeOrden(OrdenResumen item) {
        JPanel badge = ModernUI.createTintedPanel(new BorderLayout(6, 0), COLOR_BADGE_NORMAL);
        badge.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        badge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy");
        String envioStr = item.fechaEnvio() != null ? item.fechaEnvio().format(fmt) : "—";
        String detalleStr = item.detalle() == null || item.detalle().isBlank() ? "Sin detalle" : item.detalle();
        if (detalleStr.length() > 90) detalleStr = detalleStr.substring(0, 87) + "...";

        JLabel infoLabel = new JLabel("<html>"
                + "<b>OT #" + item.ordenId() + "</b> &nbsp;"
                + "<span style='color:#444'>" + item.referenciaJoya() + "</span><br>"
                + "<span style='color:#666;font-size:10px'>Joyero: <b>" + item.joyero() + "</b>"
                + " &nbsp;·&nbsp; Envío: " + envioStr
                + " &nbsp;·&nbsp; Entrega: " + item.fechaEntrega().format(fmt) + "</span><br>"
                + "<span style='color:#888;font-size:10px'><i>Detalle: " + detalleStr + "</i></span>"
                + "</html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(ModernUI.TEXT);

        JLabel diasLabel = new JLabel(textoEstadoDias(item.diasRestantes()));
        diasLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        diasLabel.setOpaque(true);
        diasLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        diasLabel.setVerticalAlignment(SwingConstants.TOP);
        aplicarColorEstado(diasLabel, item.diasRestantes());

        badge.add(infoLabel, BorderLayout.CENTER);
        badge.add(diasLabel, BorderLayout.EAST);

        badge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (badgeSeleccionado != null) {
                    badgeSeleccionado.setBackground(COLOR_BADGE_NORMAL);
                    for (Component c : badgeSeleccionado.getComponents()) {
                        c.setBackground(COLOR_BADGE_NORMAL);
                    }
                }
                ordenSeleccionadaId = item.ordenId();
                badgeSeleccionado = badge;
                badge.setBackground(COLOR_BADGE_SELECCIONADO);
                for (Component c : badge.getComponents()) {
                    if (c.isOpaque()) c.setBackground(COLOR_BADGE_SELECCIONADO);
                }
            }
        });

        return badge;
    }

    private String textoEstadoDias(long dias) {
        if (dias < 0) {
            return "Vencida hace " + Math.abs(dias) + " día(s)";
        }
        if (dias == 0) {
            return "Vence hoy";
        }
        if (dias == 1) {
            return "Vence mañana";
        }
        return "Faltan " + dias + " días";
    }

    private void aplicarColorEstado(JLabel estado, long dias) {
        if (dias < 0) {
            estado.setBackground(new Color(255, 232, 232));
            estado.setForeground(new Color(163, 25, 25));
            return;
        }
        if (dias <= 2) {
            estado.setBackground(new Color(255, 243, 224));
            estado.setForeground(new Color(168, 92, 12));
            return;
        }
        estado.setBackground(new Color(230, 245, 236));
        estado.setForeground(new Color(24, 112, 64));
    }

    private record OrdenResumen(Long ordenId, String referenciaJoya, String joyero,
                                LocalDate fechaEnvio, LocalDate fechaEntrega,
                                long diasRestantes, String detalle) {
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
