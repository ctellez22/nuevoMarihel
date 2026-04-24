package igu;

import logica.Controladora;
import logica.Joya;
import org.example.SessionContext;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetallesJoya {
    private JPanel mainPanel;
    private JButton marcarVendidoButton;
    private JButton editarJoyaButton;
    private JButton reimprimirRefButton;
    private JLabel lblNombre;
    private JLabel lblCategoria;
    private JLabel lblPeso;
    private JLabel lblPrecio;
    private JLabel lblInfoPiedra;
    private JLabel lblFechaVendida;
    private JLabel lblObservaciones;
    private JLabel lblaFechaIngreso;
    private JLabel lblTienePiedra;
    private JLabel lblEstado;

    private final Controladora controladora;
    private final VerDatos interfazPrincipal;
    private final SessionContext session;


    public DetallesJoya(Joya joya, VerDatos interfazPrincipal){
        this(joya, interfazPrincipal, null);
    }

    public DetallesJoya(Joya joya, VerDatos interfazPrincipal, SessionContext session){
        // Instancia de la controladora
        this.controladora = new Controladora();
        this.interfazPrincipal = interfazPrincipal;
        this.session = session;
        
        // Recargar la joya de la BD para obtener el estado más reciente
        Joya joyaActualizada = controladora.obtenerJoyaPorId(joya.getId());
        if (joyaActualizada != null) {
            joya = joyaActualizada;
        }

        // Configuración inicial de los componentes con la información de la joya
        String idVisible = (joya.getDisplayId() != null && !joya.getDisplayId().isBlank()) ? joya.getDisplayId() : String.valueOf(joya.getId());
        lblNombre.setText("[" + idVisible + "] " + joya.getNombre());
        lblNombre.setToolTipText("ID: " + idVisible);
        lblCategoria.setText(joya.getCategoria());
        lblPeso.setText(joya.getPeso() + " gramos");
        lblPrecio.setText("$" + joya.getPrecio());
        lblTienePiedra.setText(joya.isTienePiedra() ? "Sí 💎" : "No 🪨");
        DateTimeFormatter formatterr = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblaFechaIngreso.setText(joya.getFechaIngreso().format(formatterr));

        lblObservaciones.setText(joya.getObservacion());
        lblEstado.setText(joya.getEstado() + " | " + joya.getEstadoAutorizacionTexto());
        lblEstado.setForeground(joya.isAutorizado() ? new Color(34, 139, 34) : new Color(204, 128, 0));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaVendidaTexto = joya.getFechaVendida() != null
                ? joya.getFechaVendida().format(formatter)
                : "No vendida";
        lblFechaVendida.setText(fechaVendidaTexto);

        if (joya.isTienePiedra()) {
            lblInfoPiedra.setText(joya.getInfoPiedra());
        } else {
            lblInfoPiedra.setVisible(false);
        }

        configurarBotones(joya);

    }

    private void configurarBotones(Joya joya) {
        // Configurar botón "Marcar como Vendido"
        configurarBotonVendido(marcarVendidoButton, joya.isVendido());
        marcarVendidoButton.addActionListener(e -> confirmarMarcarVendido(joya));

        // Configurar botón "Reimprimir Referencia"
        reimprimirRefButton.addActionListener(e -> reimprimirReferencia(joya));

        // Configurar botón "Editar"
        editarJoyaButton.addActionListener(e -> editarJoya(joya));
    }


    private void editarJoya(Joya joya) {
        EditarJoyaDialog editarDialog = new EditarJoyaDialog(joya, controladora, interfazPrincipal, session);
        editarDialog.setVisible(true);
    }

    private void reimprimirReferencia(Joya joya) {
        try {
            controladora.reImprimirDespues(
                    joya.getId(),
                    joya.getNombre(),
                    joya.getPrecio(),
                    joya.getPeso(),
                    joya.getCategoria(),
                    joya.getObservacion(),
                    joya.isTienePiedra(),
                    joya.getInfoPiedra()
            );

            JOptionPane.showMessageDialog(
                    null,
                    "La referencia se ha reimpreso correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE

            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error al reimprimir la referencia: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void confirmarMarcarVendido(Joya joya) {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Estás seguro de que deseas marcar esta joya como vendida?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String precioVentaReal = solicitarPrecioVentaRealSiAplica(joya);
                if (precioVentaReal == null) {
                    return;
                }

                boolean aplicadaDirecto = controladora.marcarComoVendidaConAutorizacion(session, joya.getId(), precioVentaReal);
                
                // Actualizar el objeto joya local para reflejar los cambios
                joya.setVendido(true);
                LocalDateTime fechaAhora = LocalDateTime.now();
                joya.setFechaVendida(fechaAhora);
                joya.setPrecioVentaReal(precioVentaReal);
                
                // Actualizar la interfaz con los nuevos valores
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                lblFechaVendida.setText(fechaAhora.format(formatter));
                
                JOptionPane.showMessageDialog(
                        null,
                        aplicadaDirecto
                                ? "La joya ha sido marcada como vendida."
                                : "Solicitud enviada para aprobación de administrador.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );
                marcarVendidoButton.setEnabled(false);
                marcarVendidoButton.setText(aplicadaDirecto ? "Ya Vendido" : "Pendiente aprobación");
                marcarVendidoButton.setBackground(Color.GRAY);
                marcarVendidoButton.repaint();
                if (mainPanel != null) {
                    mainPanel.repaint();
                }
                if (interfazPrincipal != null) {
                    interfazPrincipal.actualizarListaFiltrada();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error al marcar como vendida: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private String solicitarPrecioVentaRealSiAplica(Joya joya) {
        // Todos (admin y vendedor) deben ingresar el precio real de venta
        String valorInicial = joya.getPrecioVentaReal();
        if (valorInicial == null || valorInicial.isBlank()) {
            valorInicial = joya.getPrecio();
        }

        while (true) {
            String valorFormateado = mostrarDialogoPrecioFormateado(valorInicial);
            if (valorFormateado == null) {
                return null;
            }

            String digitos = extraerDigitos(valorFormateado);
            if (digitos.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un valor para continuar.", "Validacion", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return formatearConApostrofes(digitos);
        }
    }

    private String mostrarDialogoPrecioFormateado(String valorInicial) {
        JTextField campo = new JTextField(16);
        String digitosIniciales = extraerDigitos(valorInicial == null ? "" : valorInicial);
        campo.setText(digitosIniciales.isEmpty() ? "" : formatearConApostrofes(digitosIniciales));
        instalarMascaraPrecio(campo);

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Ingrese el precio real de venta:"));
        panel.add(campo);

        SwingUtilities.invokeLater(() -> {
            campo.requestFocusInWindow();
            campo.selectAll();
        });

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Precio real de venta",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        return result == JOptionPane.OK_OPTION ? campo.getText().trim() : null;
    }

    private void instalarMascaraPrecio(JTextField campo) {
        AbstractDocument doc = (AbstractDocument) campo.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            private boolean actualizando;

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (actualizando) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }

                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String reemplazo = text == null ? "" : text;
                String propuesto = actual.substring(0, offset) + reemplazo + actual.substring(offset + length);
                int caretPropuesto = offset + reemplazo.length();
                aplicarMascara(fb, campo, propuesto, caretPropuesto);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                replace(fb, offset, length, "", null);
            }

            private void aplicarMascara(FilterBypass fb, JTextField campo, String propuesto, int caretPropuesto) throws BadLocationException {
                String digitos = extraerDigitos(propuesto);
                String formateado = digitos.isEmpty() ? "" : formatearConApostrofes(digitos);

                int digitosAntesCaret = contarDigitosHasta(propuesto, caretPropuesto);
                int nuevaPosCaret = posicionSegunDigitos(formateado, digitosAntesCaret);

                actualizando = true;
                fb.replace(0, fb.getDocument().getLength(), formateado, null);
                actualizando = false;

                SwingUtilities.invokeLater(() -> campo.setCaretPosition(Math.min(nuevaPosCaret, campo.getText().length())));
            }
        });
    }

    private int contarDigitosHasta(String texto, int limite) {
        int max = Math.min(Math.max(limite, 0), texto.length());
        int cuenta = 0;
        for (int i = 0; i < max; i++) {
            if (Character.isDigit(texto.charAt(i))) {
                cuenta++;
            }
        }
        return cuenta;
    }

    private int posicionSegunDigitos(String textoFormateado, int cantidadDigitos) {
        if (cantidadDigitos <= 0) {
            return 0;
        }
        int vistos = 0;
        for (int i = 0; i < textoFormateado.length(); i++) {
            if (Character.isDigit(textoFormateado.charAt(i))) {
                vistos++;
                if (vistos == cantidadDigitos) {
                    return i + 1;
                }
            }
        }
        return textoFormateado.length();
    }

    private String extraerDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        String soloDigitos = valor.replaceAll("\\D", "");
        return soloDigitos.replaceFirst("^0+(?!$)", "");
    }

    private String formatearConApostrofes(String digitos) {
        if (digitos == null || digitos.isBlank()) {
            return "";
        }
        StringBuilder invertido = new StringBuilder(digitos).reverse();
        StringBuilder conSeparador = new StringBuilder();
        for (int i = 0; i < invertido.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                conSeparador.append('\'');
            }
            conSeparador.append(invertido.charAt(i));
        }
        return conSeparador.reverse().toString();
    }

    private void configurarBotonVendido(JButton boton, boolean isVendido) {
        boton.setEnabled(!isVendido);
        boton.setText(isVendido ? "Ya Vendido" : "Marcar como Vendido");
        boton.setBackground(isVendido ? Color.GRAY : new Color(255, 69, 58));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


}
