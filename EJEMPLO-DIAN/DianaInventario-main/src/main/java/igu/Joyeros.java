package igu;

import logica.Controladora;
import logica.Joyero;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Joyeros {

	private final JPanel mainPanel;
	private final JTextField txtNombre;
	private final DefaultListModel<String> listModel;
	private final Controladora controladora;

	public Joyeros(JFrame parent) {
		this.controladora = new Controladora();

		this.mainPanel = new JPanel(new BorderLayout(12, 12));
		this.mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JPanel panelSuperior = new JPanel(new BorderLayout(8, 8));
		JLabel lblNombre = new JLabel("Nuevo joyero:");
		this.txtNombre = new JTextField();
		JButton btnGuardar = new JButton("Guardar");
		JButton btnEliminar = new JButton("Eliminar");
		JButton btnRefrescar = new JButton("Refrescar");

		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		panelBotones.add(btnRefrescar);
		panelBotones.add(btnEliminar);
		panelBotones.add(btnGuardar);

		panelSuperior.add(lblNombre, BorderLayout.WEST);
		panelSuperior.add(txtNombre, BorderLayout.CENTER);
		panelSuperior.add(panelBotones, BorderLayout.EAST);

		this.listModel = new DefaultListModel<>();
		JList<String> listJoyeros = new JList<>(listModel);
		JScrollPane scrollPane = new JScrollPane(listJoyeros);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Joyeros registrados"));

		this.mainPanel.add(panelSuperior, BorderLayout.NORTH);
		this.mainPanel.add(scrollPane, BorderLayout.CENTER);

		btnGuardar.addActionListener(e -> guardarJoyero());
		btnEliminar.addActionListener(e -> eliminarJoyeroSeleccionado(listJoyeros));
		btnRefrescar.addActionListener(e -> cargarJoyeros());

		this.mainPanel.registerKeyboardAction(
				e -> txtNombre.requestFocusInWindow(),
				KeyStroke.getKeyStroke("ctrl N"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
		);

		cargarJoyeros();
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	private void guardarJoyero() {
		String nombre = txtNombre.getText();
		try {
			controladora.crearJoyero(nombre);
			txtNombre.setText("");
			cargarJoyeros();
			JOptionPane.showMessageDialog(mainPanel, "Joyero guardado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(mainPanel, "No se pudo guardar el joyero: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void eliminarJoyeroSeleccionado(JList<String> listJoyeros) {
		String seleccion = listJoyeros.getSelectedValue();
		if (seleccion == null || seleccion.isBlank()) {
			JOptionPane.showMessageDialog(mainPanel, "Seleccione un joyero para eliminar.", "Atencion", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			Long id = extraerId(seleccion);
			int confirmacion = JOptionPane.showConfirmDialog(
					mainPanel,
					"Desea eliminar el joyero seleccionado?",
					"Confirmar eliminacion",
					JOptionPane.YES_NO_OPTION
			);
			if (confirmacion == JOptionPane.YES_OPTION) {
				controladora.eliminarJoyero(id);
				cargarJoyeros();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(mainPanel, "No se pudo eliminar el joyero: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private Long extraerId(String textoFila) {
		String[] partes = textoFila.split(" - ", 2);
		return Long.parseLong(partes[0].trim());
	}

	private void cargarJoyeros() {
		listModel.clear();
		try {
			List<Joyero> joyeros = controladora.obtenerJoyeros();
			for (Joyero joyero : joyeros) {
				listModel.addElement(joyero.getId() + " - " + joyero.getNombre());
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(mainPanel, "No se pudieron cargar los joyeros: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

