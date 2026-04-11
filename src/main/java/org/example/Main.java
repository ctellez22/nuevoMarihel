package org.example;

import igu.Principal;
import persistencia.AuthService;
import persistencia.PersistenceManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class Main {
    private static final String APP_NAME = "Marihel";
    private static final String DEFAULT_DB_URL = "jdbc:mysql://metro.proxy.rlwy.net:38179/railway?sslMode=REQUIRED&serverTimezone=UTC";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "MZemUaMJeOJDNanLLCHQseOlfgZQEjjR";

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
        Properties config = cargarConfiguracionExterna();

        aplicarDesdeConfigSiExiste("db.url", config, "db.url");
        aplicarDesdeConfigSiExiste("db.user", config, "db.user");
        aplicarDesdeConfigSiExiste("db.password", config, "db.password");
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

    private static void aplicarDesdeConfigSiExiste(String systemProperty, Properties config, String propertyKey) {
        String valor = config.getProperty(propertyKey);
        if (valor != null && !valor.isBlank()) {
            System.setProperty(systemProperty, valor.trim());
        }
    }

    private static Properties cargarConfiguracionExterna() {
        Properties properties = new Properties();
        for (Path ruta : obtenerRutasConfiguracion()) {
            if (!Files.isRegularFile(ruta)) {
                continue;
            }
            try (InputStream inputStream = Files.newInputStream(ruta)) {
                properties.load(inputStream);
                return properties;
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer la configuración de la app en: " + ruta, e);
            }
        }
        return properties;
    }

    private static Set<Path> obtenerRutasConfiguracion() {
        Set<Path> rutas = new LinkedHashSet<>();

        agregarRutaSiExiste(rutas, System.getProperty("app.config"));
        agregarRutaSiExiste(rutas, System.getenv("APP_CONFIG"));

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            Path home = Path.of(userHome);
            rutas.add(home.resolve("Library").resolve("Application Support").resolve(APP_NAME).resolve("config.properties"));
            rutas.add(home.resolve(".marihel").resolve("config.properties"));
        }

        rutas.add(Path.of("config.properties"));
        return rutas;
    }

    private static void agregarRutaSiExiste(Set<Path> rutas, String valor) {
        if (valor != null && !valor.isBlank()) {
            rutas.add(Path.of(valor.trim()));
        }
    }

    private static void validarRequerida(String systemProperty, String envVar) {
        String actual = System.getProperty(systemProperty);
        if (actual == null || actual.isBlank()) {
            throw new IllegalStateException("Falta configuración de base de datos. Define la variable " + envVar + " o crea ~/Library/Application Support/" + APP_NAME + "/config.properties.");
        }
    }

    private static void aplicarDefaultSiFalta(String systemProperty, String defaultValue) {
        String actual = System.getProperty(systemProperty);
        if (actual == null || actual.isBlank()) {
            System.setProperty(systemProperty, defaultValue);
        }
    }
}
