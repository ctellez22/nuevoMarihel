package igu;

import logica.CategoriaVerificacion;
import logica.Controladora;
import logica.Joya;
import org.example.SessionContext;
import persistencia.ControladoraPersistencia;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;

public class GroupBy {
    private JCheckBox todasLasJoyasCheckBox;
    private JComboBox<String> comboBoxCategoria;
    private JButton btnContar;
    private JList<Joya> listaJoyas; // Lista de Joya en lugar de String
    private JPanel mainPanel;
    private JButton btnVerificar;
    private JScrollPane panelScrollVerificados;
    private JScrollBar scrollBar1;


    private Controladora controladora; // Instancia de la capa lógica
    private ControladoraPersistencia controladoraPersistencia;

    private DefaultListModel<Joya> listModel; // Modelo de datos para la lista
    private boolean enVerificacion = false; // Para controlar si estamos en modo verificación
    private JDialog dialogVerificacion; // Referencia al cuadro de diálogo de verificación
    private String categoriaSeleccionadaActual;
    private SwingWorker<List<Joya>, Void> cargaJoyasWorker;
    private SwingWorker<List<CategoriaVerificacion>, Void> cargaCategoriasWorker;
    private final SessionContext session;



    public GroupBy(JFrame parent) {
        this(parent, null);
    }

    public GroupBy(JFrame parent, SessionContext session) {
        this.session = session;
        controladora = new Controladora();
        listModel = new DefaultListModel<>();
        listaJoyas.setModel(listModel);
        listaJoyas.setCellRenderer(new JoyaListCellRenderer());
        cargarCategoriasVerificacion();




        // Acción para "Todas las Joyas"
        todasLasJoyasCheckBox.addActionListener(e -> {
            if (!enVerificacion && todasLasJoyasCheckBox.isSelected()) {
                cargarTodasLasJoyas();
                //checkBoxCategoria.setSelected(false);
                comboBoxCategoria.setEnabled(false);
            }
        });

        // Acción para "Categoría"
        comboBoxCategoria.addActionListener(e -> {
            if (!enVerificacion) {
                cargarJoyasPorCategoria((String) comboBoxCategoria.getSelectedItem());
                todasLasJoyasCheckBox.setSelected(false);
            }
        });

        // Acción para el botón "Contar"
        /*
        btnContar.addActionListener(e -> {
            int cantidad = listModel.getSize();
            JOptionPane.showMessageDialog(mainPanel, "Total de joyas: " + cantidad, "Conteo", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/inversor.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        });
        */



        btnContar.addActionListener(e -> {
            int cantidad = listModel.getSize();

            // Prepara el ícono que quieras mostrar
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(getClass().getResource("/inversor.png"))
                            .getImage()
                            .getScaledInstance(50, 50, Image.SCALE_SMOOTH)
            );

            // Definir las opciones a mostrar
            Object[] opciones = { "OK", "Marcar todo verificado" };

            int seleccion = JOptionPane.showOptionDialog(
                    mainPanel,
                    "Total de joyas: " + cantidad,
                    "Conteo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    icon,
                    opciones,
                    opciones[0]  // Opción por defecto
            );

            // Revisar la opción que seleccionó el usuario
            if (seleccion == 1) {
                // "Marcar todo verificado"
                //verificarTodo();
                controladora.actualizarFechaVerificacionCategoria(categoriaSeleccionadaActual,LocalDateTime.now() );
            }
            // Si seleccion == 0 ó -1, no hacemos nada (simplemente “OK” o cerrar diálogo)
        });







        // Acción para el botón "Verificar"
        btnVerificar.addActionListener(e -> {
            if (!enVerificacion) {
                bloquearOpciones();
                iniciarVerificacion();
            }else{
                dialogVerificacion.setVisible(true);
            }
        });
        listaJoyas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = listaJoyas.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Joya joyaSeleccionada = listModel.getElementAt(index);
                        mostrarDetallesJoya2(joyaSeleccionada);
                    }
                }
            }
        });

    }

    private void cargarTodasLasJoyas() {
        List<String> estado = Arrays.asList("disponible", "prestado");
        cargarJoyasAsync(false, null, estado);
    }

    private void cargarJoyasPorCategoria(String categoriaSeleccionada) {
        // Imprime la categoría que se está cargando
        //System.out.println("Cargando joyas para la categoría: '" + categoriaSeleccionada + "'");

        // Asigna la categoría actual para poder utilizarla luego al finalizar la verificación
        categoriaSeleccionadaActual = categoriaSeleccionada;

        // Filtrar joyas por categoría y que no estén vendidas
        List<String> estado = Arrays.asList("disponible", "prestado");
        cargarJoyasAsync(true, categoriaSeleccionada, estado);
    }

    private void cargarJoyasAsync(boolean filterByCategory, String categoriaSeleccionada, List<String> estado) {
        if (cargaJoyasWorker != null && !cargaJoyasWorker.isDone()) {
            cargaJoyasWorker.cancel(true);
        }

        setCargandoJoyas(true);
        List<String> estadoFiltro = List.copyOf(estado);
        cargaJoyasWorker = new SwingWorker<>() {
            @Override
            protected List<Joya> doInBackground() {
                return controladora.filtrarJoyas(
                        false,
                        null,
                        filterByCategory,
                        categoriaSeleccionada,
                        false,
                        null,
                        false,
                        null,
                        true,
                        estadoFiltro
                );
            }

            @Override
            protected void done() {
                setCargandoJoyas(false);
                if (isCancelled()) {
                    return;
                }
                try {
                    actualizarLista(get());
                } catch (CancellationException ignored) {
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error al cargar joyas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        cargaJoyasWorker.execute();
    }

    private void setCargandoJoyas(boolean cargando) {
        btnContar.setEnabled(!cargando);
        btnVerificar.setEnabled(!cargando);
        todasLasJoyasCheckBox.setEnabled(!cargando && !enVerificacion);
        comboBoxCategoria.setEnabled(!cargando && !todasLasJoyasCheckBox.isSelected() && !enVerificacion);
    }


    private void actualizarLista(List<Joya> joyas) {
        listModel.clear();
        for (Joya joya : joyas) {
            listModel.addElement(joya);
        }
    }
    private void mostrarDetallesJoya2(Joya joya) {
        JDialog detallesDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Detalles de la Joya", true);
        detallesDialog.setSize(650, 900);
        detallesDialog.setLocationRelativeTo(mainPanel);
        DetallesJoya detallesJoya = new DetallesJoya(joya, null, session);
        detallesDialog.add(detallesJoya.getMainPanel());
        detallesDialog.setVisible(true);
    }


    private void iniciarVerificacion() {
        enVerificacion = true;

        if (dialogVerificacion == null) {
            // Crear el cuadro de diálogo si no existe
            dialogVerificacion = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Verificación de Joyas", true);
            dialogVerificacion.setSize(600, 400);
            dialogVerificacion.setLocationRelativeTo(mainPanel);

            // Panel principal del diálogo
            JPanel dialogPanel = new JPanel(new BorderLayout());

            // Panel de entrada de ID
            JPanel inputPanel = new JPanel(new BorderLayout());
            JLabel lblInfo = new JLabel("Ingrese el ID de la joya y presione Enter:");
            JTextField txtInputId = new JTextField();
            inputPanel.add(lblInfo, BorderLayout.NORTH);
            inputPanel.add(txtInputId, BorderLayout.CENTER);

            // Panel para mostrar los detalles de la joya
            JPanel detallesPanel = new JPanel();
            detallesPanel.setLayout(new BoxLayout(detallesPanel, BoxLayout.Y_AXIS));
            detallesPanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Joya"));

            // Agregar los paneles al diálogo
            dialogPanel.add(inputPanel, BorderLayout.NORTH);
            dialogPanel.add(new JScrollPane(detallesPanel), BorderLayout.CENTER);

            dialogVerificacion.add(dialogPanel);

            // Listener para verificar el ID al presionar Enter
            txtInputId.addActionListener(e -> {
                String inputId = txtInputId.getText().trim();
                if (!inputId.isEmpty()) {
                    verificarYMostrarJoya(inputId, detallesPanel);
                    txtInputId.setText(""); // Limpiar el campo para el siguiente ID
                }
            });
            // Listener para cerrar el cuadro de diálogo al presionar Escape
            dialogVerificacion.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
            dialogVerificacion.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cerrarVerificacion(); // Cierra el cuadro sin destruirlo
                }
            });
        }

        dialogVerificacion.setVisible(true); // Reutilizar el cuadro si ya existe
    }




    private void cerrarVerificacion() {
        if (dialogVerificacion != null) {
            dialogVerificacion.setVisible(false); // Ocultar el cuadro sin destruirlo
            enVerificacion = false;
            desbloquearOpciones(); // Desbloquear las opciones si es necesario
        }
    }




    private JPanel crearTarjetaAtributo(String titulo, String valor) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBackground(new Color(255, 255, 255)); // Fondo blanco
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), // Borde redondeado
                BorderFactory.createEmptyBorder(10, 15, 10, 15) // Espaciado interno
        ));
        tarjeta.setMaximumSize(new Dimension(450, 50));

        JLabel lblTitulo = new JLabel(titulo + ": ");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(80, 80, 80));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblValor.setForeground(new Color(50, 50, 50));

        tarjeta.add(lblTitulo, BorderLayout.WEST);
        tarjeta.add(lblValor, BorderLayout.CENTER);

        return tarjeta;
    }

    private void verificarYMostrarJoya(String id, JPanel detallesPanel) {
        boolean encontrada = false;

        // Iterar sobre el modelo de la lista
        for (int i = 0; i < listModel.getSize(); i++) {
            Joya joya = listModel.getElementAt(i);

            // Verificar si el ID coincide
            String buscado = (id == null) ? "" : id.trim();
            boolean coincide = false;
            // Primero comparar displayId (si existe), insensible a mayúsculas
            if (joya.getDisplayId() != null && !joya.getDisplayId().isBlank()) {
                if (joya.getDisplayId().equalsIgnoreCase(buscado)) {
                    coincide = true;
                }
            }
            // Si no coincidió por displayId, comparar por id numérico
            if (!coincide) {
                if (joya.getId() != null && joya.getId().toString().equals(buscado)) {
                    coincide = true;
                }
            }

            if (coincide) {
                encontrada = true;

                // Actualizar los detalles de la joya en el panel
                detallesPanel.removeAll();
                // Mostrar displayId preferentemente si existe
                String idAMostrar = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank()) ? joya.getDisplayId() : String.valueOf(joya.getId());
                detallesPanel.add(crearTarjetaAtributo("ID", idAMostrar));
                detallesPanel.add(crearTarjetaAtributo("Nombre", joya.getNombre()));
                detallesPanel.add(crearTarjetaAtributo("Categoría", joya.getCategoria()));
                detallesPanel.add(crearTarjetaAtributo("Peso", joya.getPeso() + " gramos"));
                detallesPanel.add(crearTarjetaAtributo("Precio", "$" + joya.getPrecio()));
                detallesPanel.add(crearTarjetaAtributo("Tiene Piedra", joya.isTienePiedra() ? "Sí 💎" : "No 🪨"));
                if (joya.isTienePiedra()) {
                    detallesPanel.add(crearTarjetaAtributo("Información de Piedra", joya.getInfoPiedra()));
                }
                detallesPanel.add(crearTarjetaAtributo("Observación", joya.getObservacion()));
                detallesPanel.revalidate();
                detallesPanel.repaint();

                // Eliminar la joya de la lista
                listModel.remove(i);
                break; // Salir del bucle después de encontrar y procesar
            }
        }

        if (!encontrada) {
            JOptionPane.showMessageDialog(mainPanel, "La joya con ID " + id + " no se encuentra en la lista.", "No Encontrada", JOptionPane.ERROR_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/llora.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        }

        // Comprobar si la lista está vacía
        if (listModel.getSize() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Todas las joyas han sido verificadas y la lista está vacía.", "Lista Completa", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(new ImageIcon(getClass().getResource("/nino.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
            // Actualiza la fecha de verificación para la categoría
            if (categoriaSeleccionadaActual != null) {
                System.out.println("Actualizando categoría: '" + categoriaSeleccionadaActual + "' con fecha " + LocalDateTime.now());
                controladora.actualizarFechaVerificacionCategoria(categoriaSeleccionadaActual, LocalDateTime.now());
            } else {
                System.out.println("categoriaSeleccionadaActual es null");
            }
        }
    }


    private void bloquearOpciones() {
        todasLasJoyasCheckBox.setEnabled(false);
        //checkBoxCategoria.setEnabled(false);
        comboBoxCategoria.setEnabled(false);
    }

    private void desbloquearOpciones() {
        todasLasJoyasCheckBox.setEnabled(true);
        //checkBoxCategoria.setEnabled(true);
        comboBoxCategoria.setEnabled(true);
    }



    private void cargarCategoriasVerificacion() {
        if (cargaCategoriasWorker != null && !cargaCategoriasWorker.isDone()) {
            cargaCategoriasWorker.cancel(true);
        }

        JPanel cargandoPanel = new JPanel(new BorderLayout());
        cargandoPanel.add(new JLabel("Cargando categorías...", SwingConstants.CENTER), BorderLayout.CENTER);
        panelScrollVerificados.setViewportView(cargandoPanel);

        cargaCategoriasWorker = new SwingWorker<>() {
            @Override
            protected List<CategoriaVerificacion> doInBackground() {
                return controladora.obtenerCategoriasOrdenadasPorVerificacion();
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    List<CategoriaVerificacion> categorias = get();
                    JPanel panelCategorias = new JPanel();
                    panelCategorias.setLayout(new BoxLayout(panelCategorias, BoxLayout.Y_AXIS));

                    for (CategoriaVerificacion cat : categorias) {
                        long dias = ChronoUnit.DAYS.between(cat.getUltimaFechaVerificacion(), LocalDateTime.now());
                        String icono = (dias > 20) ? "\u2717 " : "\u2713 ";
                        String texto = icono + cat.getNombreCategoria() + " - " + dias + " días sin verificación";
                        JLabel lblCategoria = new JLabel(texto);
                        lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 14));
                        lblCategoria.setForeground(Color.BLACK);

                        Color bgColor = (dias > 20) ? new Color(255, 204, 204) : new Color(204, 255, 204);
                        lblCategoria.setOpaque(true);
                        lblCategoria.setBackground(bgColor);

                        panelCategorias.add(lblCategoria);
                        panelCategorias.add(Box.createRigidArea(new Dimension(0, 5)));
                    }

                    panelScrollVerificados.setViewportView(panelCategorias);
                } catch (CancellationException ignored) {
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error al cargar categorías: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        cargaCategoriasWorker.execute();
    }




    public JPanel getMainPanel() {
        return mainPanel;
    }
}
