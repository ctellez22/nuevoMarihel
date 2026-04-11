package org.example;

import igu.Principal;
import persistencia.AuthService;
import persistencia.PersistenceManager;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final String DEFAULT_DB_URL = "jdbc:mysql://marihel.ck38iw4amrdb.us-east-1.rds.amazonaws.com:3306/marihel?sslMode=REQUIRED&serverTimezone=UTC";
    private static final String DEFAULT_DB_USER = "admin";
    private static final String DEFAULT_DB_PASSWORD = "adminadmin";

    public static void main(String[] args) {
        configurarPropiedadesBDDesdeEntorno();

        try {
            PersistenceManager.validateConnection();
            PersistenceManager.ensureSchemaCompatibility();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(PersistenceManager::shutdown));

        SessionContext session;
        try {
            session = solicitarSesion();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error de autenticación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (session == null) {
            return;
        }

        // Ejecutar la GUI en el Event Dispatch Thread (EDT)
        SessionContext finalSession = session;
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Principal - " + finalSession.username() + " (" + finalSession.role() + ")");
            frame.setContentPane(new Principal(finalSession).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 410);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static SessionContext solicitarSesion() {
        AuthService authService = new AuthService();

        while (true) {
            JTextField usuarioField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
            panel.add(new JLabel("Usuario:"));
            panel.add(usuarioField);
            panel.add(new JLabel("Contraseña:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Iniciar sesión",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String usuario = usuarioField.getText();
            String password = new String(passwordField.getPassword());

            SessionContext session = authService.authenticate(usuario, password);
            if (session != null) {
                return session;
            }

            JOptionPane.showMessageDialog(
                    null,
                    "Credenciales inválidas o usuario inactivo.",
                    "Inicio de sesión",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private static void configurarPropiedadesBDDesdeEntorno() {
        aplicarSiExiste("db.url", "DB_URL");
        aplicarSiExiste("db.user", "DB_USER");
        aplicarSiExiste("db.password", "DB_PASSWORD");

        // Permite correr localmente sin configurar variables de entorno en cada ejecución.
        aplicarDefaultSiFalta("db.url", DEFAULT_DB_URL);
        aplicarDefaultSiFalta("db.user", DEFAULT_DB_USER);
        aplicarDefaultSiFalta("db.password", DEFAULT_DB_PASSWORD);

        validarRequerida("db.url", "DB_URL");
        validarRequerida("db.user", "DB_USER");
        validarRequerida("db.password", "DB_PASSWORD");
    }

    private static void aplicarSiExiste(String systemProperty, String envVar) {
        String valor = System.getenv(envVar);
        if (valor != null && !valor.isBlank()) {
            System.setProperty(systemProperty, valor);
        }
    }

    private static void validarRequerida(String systemProperty, String envVar) {
        String actual = System.getProperty(systemProperty);
        if (actual == null || actual.isBlank()) {
            throw new IllegalStateException("Falta configuración de base de datos. Define la variable " + envVar + ".");
        }
    }

    private static void aplicarDefaultSiFalta(String systemProperty, String defaultValue) {
        String actual = System.getProperty(systemProperty);
        if (actual == null || actual.isBlank()) {
            System.setProperty(systemProperty, defaultValue);
        }
    }
}
