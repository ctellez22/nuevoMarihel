package igu;

import logica.CambioPendiente;
import logica.Controladora;
import org.example.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AprobacionesDialog extends JDialog {
    private final Controladora controladora;
    private final SessionContext session;
    private final DefaultListModel<CambioPendiente> listModel;
    private final JList<CambioPendiente> listPendientes;
    private final JEditorPane detallePane;

    public AprobacionesDialog(Frame owner, SessionContext session) {
        super(owner, "Aprobaciones pendientes", true);
        if (session == null || !session.isAdmin()) {
            throw new IllegalStateException("Solo un administrador puede abrir el panel de aprobaciones.");
        }
        this.controladora = new Controladora();
        this.session = session;
        this.listModel = new DefaultListModel<>();
        this.listPendientes = new JList<>(listModel);
        this.detallePane = new JEditorPane();

        construirUI();
        cargarPendientes();
    }

    private void construirUI() {
        setSize(980, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        listPendientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPendientes.setFont(new Font("SansSerif", Font.BOLD, 13));
        listPendientes.addListSelectionListener(e -> mostrarDetalleSeleccionado());

        JScrollPane scrollList = new JScrollPane(listPendientes);
        scrollList.setPreferredSize(new Dimension(360, 500));

        detallePane.setEditable(false);
        detallePane.setContentType("text/html");
        detallePane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        detallePane.setText(renderMensajeVacio());
        JScrollPane scrollDetalle = new JScrollPane(detallePane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollList, scrollDetalle);
        splitPane.setDividerLocation(360);

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnAprobar = new JButton("Aprobar");
        JButton btnRechazar = new JButton("Rechazar");
        JButton btnCerrar = new JButton("Cerrar");

        btnRefrescar.addActionListener(e -> cargarPendientes());
        btnAprobar.addActionListener(e -> aprobarSeleccionado());
        btnRechazar.addActionListener(e -> rechazarSeleccionado());
        btnCerrar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnRefrescar);
        botones.add(btnRechazar);
        botones.add(btnAprobar);
        botones.add(btnCerrar);

        add(splitPane, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private void cargarPendientes() {
        listModel.clear();
        List<CambioPendiente> pendientes = controladora.obtenerPendientesJoya();
        for (CambioPendiente pendiente : pendientes) {
            listModel.addElement(pendiente);
        }
        if (!listModel.isEmpty()) {
            listPendientes.setSelectedIndex(0);
        } else {
            detallePane.setText(renderMensajeVacio());
        }
    }

    private void mostrarDetalleSeleccionado() {
        CambioPendiente selected = listPendientes.getSelectedValue();
        if (selected == null) {
            detallePane.setText(renderMensajeVacio());
            return;
        }

        Map<String, String> beforeMap = parseJsonFlat(selected.getBeforeJson());
        Map<String, String> afterMap = parseJsonFlat(selected.getAfterJson());

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:SansSerif;padding:14px;'>");
        html.append("<h2 style='margin:0 0 8px 0;'>Revision de cambio #").append(selected.getId()).append("</h2>");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        html.append("<div style='margin-bottom:10px;color:#333;'>")
                .append("<b>Operacion:</b> ").append(escapeHtml(selected.getOperacion())).append(" | ")
                .append("<b>Entidad:</b> ").append(escapeHtml(selected.getEntidad())).append(" | ")
                .append("<b>Entidad ID:</b> ").append(selected.getEntidadId() == null ? "nuevo" : selected.getEntidadId()).append("<br/>")
                .append("<b>Solicitado por usuario ID:</b> ").append(selected.getSolicitadoPor()).append(" | ")
                .append("<b>Fecha:</b> ").append(selected.getSolicitadoEn().format(formatter))
                .append("</div>");

        html.append("<table style='width:100%;border-collapse:collapse;font-size:13px;'>");
        html.append("<tr style='background:#f5f7fb;'>")
                .append("<th style='text-align:left;padding:8px;border:1px solid #d7dbe3;'>Campo</th>")
                .append("<th style='text-align:left;padding:8px;border:1px solid #d7dbe3;'>Antes</th>")
                .append("<th style='text-align:left;padding:8px;border:1px solid #d7dbe3;'>Ahora</th>")
                .append("</tr>");

        for (String key : orderedKeys(beforeMap, afterMap)) {
            String before = normalizeForView(beforeMap.get(key));
            String after = normalizeForView(afterMap.get(key));
            boolean changed = !before.equals(after);

            String rowBg = changed ? "#fff4e5" : "#ffffff";
            html.append("<tr style='background:").append(rowBg).append(";'>")
                    .append("<td style='padding:8px;border:1px solid #e0e4eb;'><b>").append(escapeHtml(labelFor(key))).append("</b></td>")
                    .append("<td style='padding:8px;border:1px solid #e0e4eb;'>").append(escapeHtml(before)).append("</td>")
                    .append("<td style='padding:8px;border:1px solid #e0e4eb;'>").append(escapeHtml(after)).append(changed ? " <span style='color:#b26a00;'>(modificado)</span>" : "").append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        html.append("</body></html>");

        detallePane.setText(html.toString());
        detallePane.setCaretPosition(0);
    }

    private String renderMensajeVacio() {
        return "<html><body style='font-family:SansSerif;padding:18px;color:#444;'><h3>No hay cambios pendientes de aprobacion.</h3></body></html>";
    }

    private List<String> orderedKeys(Map<String, String> beforeMap, Map<String, String> afterMap) {
        List<String> preferred = List.of("nombre", "categoria", "socio", "precio", "precioVenta", "peso", "estado", "vendido", "tienePiedra", "infoPiedra", "observacion", "fechaVendida");
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(preferred);
        keys.addAll(beforeMap.keySet());
        keys.addAll(afterMap.keySet());
        return new ArrayList<>(keys);
    }

    private String labelFor(String key) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("nombre", "Nombre");
        labels.put("categoria", "Categoria");
        labels.put("socio", "Socio");
        labels.put("precio", "Precio");
        labels.put("precioVenta", "Precio venta");
        labels.put("peso", "Peso");
        labels.put("estado", "Estado");
        labels.put("vendido", "Vendido");
        labels.put("tienePiedra", "Tiene piedra");
        labels.put("infoPiedra", "Info piedra");
        labels.put("observacion", "Observacion");
        labels.put("fechaVendida", "Fecha vendida");
        return labels.getOrDefault(key, key);
    }

    private String normalizeForView(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    private Map<String, String> parseJsonFlat(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return map;
        }

        Pattern pattern = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(\\\"((?:\\\\.|[^\\\"])*)\\\"|true|false|null|-?\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String raw = matcher.group(2);
            String strVal;
            if (raw.startsWith("\"")) {
                strVal = matcher.group(3);
                strVal = strVal.replace("\\\"", "\"")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\\\", "\\");
            } else if ("null".equalsIgnoreCase(raw)) {
                strVal = "";
            } else {
                strVal = raw;
            }
            map.put(key, strVal);
        }
        return map;
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void aprobarSeleccionado() {
        CambioPendiente selected = listPendientes.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cambio pendiente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            controladora.aprobarPendienteJoya(selected.getId(), session.userId());
            JOptionPane.showMessageDialog(this, "Cambio aprobado y confirmado en la base de datos.", "Exito", JOptionPane.INFORMATION_MESSAGE);
            cargarPendientes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo aprobar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechazarSeleccionado() {
        CambioPendiente selected = listPendientes.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cambio pendiente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motivo = JOptionPane.showInputDialog(this, "Motivo de rechazo (opcional):", "Rechazar cambio", JOptionPane.QUESTION_MESSAGE);
        try {
            controladora.rechazarPendienteJoya(selected.getId(), session.userId(), motivo);
            JOptionPane.showMessageDialog(this, "Cambio rechazado.", "Exito", JOptionPane.INFORMATION_MESSAGE);
            cargarPendientes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo rechazar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
