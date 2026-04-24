package org.example;

import igu.ModernUI;
import igu.Principal;
import persistencia.AuthService;
import persistencia.PersistenceManager;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    // Respaldo local: reemplaza estos valores con tus credenciales reales si no usas variables de entorno.
    private static final String HARDCODED_DB_URL = "jdbc:postgresql://ep-calm-feather-an9bqvv1-pooler.c-6.us-east-1.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require";
    private static final String HARDCODED_DB_USER = "neondb_owner";
    private static final String HARDCODED_DB_PASSWORD = "npg_BnUV3mArXZ5y";

    public static void main(String[] args) {
        configurarTemaVisual();
        configurarPropiedadesBDDesdeEntorno();

        try {
            PersistenceManager.validateConnection();
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
            JFrame frame = new JFrame("Inventario - " + finalSession.username() + " (" + finalSession.role() + ")");
            frame.setContentPane(new Principal(finalSession).getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1200, 780));
            frame.setSize(1280, 820);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void configurarTemaVisual() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        ModernUI.applyGlobalTheme();
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
        aplicarDatabaseUrlSiExiste();
        aplicarSiExiste("db.url", "DB_URL");
        aplicarSiExiste("db.user", "DB_USER");
        aplicarSiExiste("db.password", "DB_PASSWORD");

        aplicarHardcodeSiFalta("db.url", HARDCODED_DB_URL);
        aplicarHardcodeSiFalta("db.user", HARDCODED_DB_USER);
        aplicarHardcodeSiFalta("db.password", HARDCODED_DB_PASSWORD);

        validarConfiguracionDbRequerida();
    }

    private static void aplicarDatabaseUrlSiExiste() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            URI uri = new URI(databaseUrl);
            String scheme = uri.getScheme();
            if (!"postgresql".equalsIgnoreCase(scheme) && !"postgres".equalsIgnoreCase(scheme)) {
                throw new IllegalStateException("DATABASE_URL debe usar el esquema postgresql://");
            }

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                int separator = userInfo.indexOf(':');
                if (separator >= 0) {
                    System.setProperty("db.user", userInfo.substring(0, separator));
                    System.setProperty("db.password", userInfo.substring(separator + 1));
                } else {
                    System.setProperty("db.user", userInfo);
                }
            }

            String rawPath = uri.getRawPath();
            String dbPath = (rawPath == null || rawPath.isBlank()) ? "" : rawPath;
            String query = uri.getRawQuery();
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "") + dbPath
                    + (query != null && !query.isBlank() ? "?" + query.replace("channel_binding=", "channelBinding=") : "");
            System.setProperty("db.url", jdbcUrl);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("DATABASE_URL no tiene un formato valido.", e);
        }
    }

    private static void aplicarSiExiste(String systemProperty, String envVar) {
        String valor = System.getenv(envVar);
        if (valor != null && !valor.isBlank()) {
            System.setProperty(systemProperty, valor);
        }
    }

    private static void aplicarHardcodeSiFalta(String systemProperty, String fallbackValue) {
        String actual = System.getProperty(systemProperty);
        if ((actual == null || actual.isBlank()) && fallbackValue != null && !fallbackValue.isBlank()) {
            System.setProperty(systemProperty, fallbackValue);
        }
    }

    private static void validarConfiguracionDbRequerida() {
        String url = System.getProperty("db.url");
        String user = System.getProperty("db.user");
        String password = System.getProperty("db.password");
        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Falta configuracion de BD. Define db.url/db.user/db.password o DB_URL/DB_USER/DB_PASSWORD o DATABASE_URL.");
        }
        if (url.contains("<HOST>") || password.contains("<PASSWORD_AQUI>")) {
            throw new IllegalStateException("Reemplaza HARDCODED_DB_URL/HARDCODED_DB_PASSWORD en Main.java con credenciales reales o define variables de entorno.");
        }
    }
}
