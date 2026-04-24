package com.marihel.inventario.utils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class PrinterUtils {
    // Intenta usar 'lp' para imprimir el archivo zplFile. Lanza IOException si 'lp' no existe o falla.
    public static void printFileWithLp(File zplFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("lp", zplFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            // Normalmente "No such file or directory" si lp no está instalado
            throw new IOException("Comando 'lp' no disponible en el sistema.", e);
        }
        boolean finished = p.waitFor(15, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("Proceso 'lp' no terminó en tiempo esperado.");
        }
        int exit = p.exitValue();
        if (exit != 0) {
            String output = readStream(p.getInputStream());
            throw new IOException("Error al ejecutar 'lp'. Código: " + exit + ". Salida: " + output);
        }
    }

    // Enviar ZPL directamente al IP de impresora usando socket TCP (puerto 9100 por defecto)
    public static void sendZplOverSocket(String printerIp, int port, String zpl) throws IOException {
        try (Socket socket = new Socket(printerIp, port)) {
            socket.setSoTimeout(5000);
            OutputStream os = socket.getOutputStream();
            os.write(zpl.getBytes(StandardCharsets.UTF_8));
            os.flush();
            // algunos modelos necesitan pequeña espera antes de cerrar
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (IOException e) {
            throw new IOException("Error enviando ZPL por socket a " + printerIp + ":" + port, e);
        }
    }

    // Nuevo helper: comprueba si un comando está disponible en el sistema (Unix-like).
    private static boolean isCommandAvailable(String cmd) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // En Windows no usamos 'lp'
            return false;
        }
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "command -v " + cmd + " >/dev/null 2>&1 && echo yes || echo no");
        try {
            Process p = pb.start();
            p.waitFor(5, TimeUnit.SECONDS);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String r = br.readLine();
                return "yes".equalsIgnoreCase(r);
            }
        } catch (Exception e) {
            return false;
        }
    }

    // Helper para decidir y ejecutar: primero intenta lp (si disponible), si no, usa socket.
    // Si printerIp==null o vacío y lp no existe, lanza excepción.
    public static void printZpl(String zplContent, File zplTempFile, String printerIp, int port) throws Exception {
        // Si existe archivo temporal y 'lp' está disponible en el sistema, intentar lp primero
        if (zplTempFile != null && zplTempFile.exists() && isCommandAvailable("lp")) {
            try {
                printFileWithLp(zplTempFile);
                return;
            } catch (IOException | InterruptedException e) {
                // Registrar y continuar al fallback por socket
                System.err.println("Intento con 'lp' falló: " + e.getMessage() + " — se intentará fallback por socket si está configurado.");
            }
        } else if (zplTempFile != null && zplTempFile.exists()) {
            // lp no disponible: informar y continuar al fallback
            System.err.println("Comando 'lp' no encontrado: se usará envío por socket si se proporciona IP de impresora.");
        }

        // Fallback: enviar por socket si tenemos IP
        if (printerIp != null && !printerIp.isBlank()) {
            try {
                sendZplOverSocket(printerIp, port, zplContent);
                return;
            } catch (IOException e) {
                throw new IOException("Falló el envío por socket y también el intento con 'lp'. Detalle: " + e.getMessage(), e);
            }
        }

        throw new IllegalStateException("No es posible imprimir: ni 'lp' disponible ni IP de impresora proporcionada.");
    }

    // Nuevo: imprimir usando lp con dispositivo y opción raw (para Mac específico)
    public static void printFileWithLpDevice(String device, File zplFile) throws IOException, InterruptedException {
        if (device == null || device.isBlank()) {
            throw new IllegalArgumentException("Device de impresora no puede ser vacío");
        }
        ProcessBuilder pb = new ProcessBuilder("lp", "-d", device, "-o", "raw", zplFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new IOException("Comando 'lp' no disponible o fallo al iniciar proceso con device: " + device, e);
        }
        boolean finished = p.waitFor(15, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("Proceso 'lp' con device no terminó en tiempo esperado.");
        }
        int exit = p.exitValue();
        if (exit != 0) {
            String output = readStream(p.getInputStream());
            throw new IOException("Error al ejecutar 'lp' con device. Código: " + exit + ". Salida: " + output);
        }
    }

    // Nuevo: detecta si este host es el Mac principal (ajustable por usuario/hostname)
    private static boolean isPrimaryMac() {
        try {
            String user = System.getProperty("user.name", "");
            if (!"camilotellez".equals(user)) {
                return false;
            }
            String host = java.net.InetAddress.getLocalHost().getHostName();
            if (host != null && host.contains("MacBook-Pro-de-Camilo")) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    // Nuevo helper público: si estamos en el Mac principal, intenta lp -d <device> -o raw; si falla, cae al flujo existente.
    // No elimina ni cambia las demás funciones; es una ruta adicional para este Mac.
    public static void printZplWithPrimaryMacSupport(String zplContent, File zplTempFile, String printerIp, int port, String macDeviceName) throws Exception {
        if (isPrimaryMac() && zplTempFile != null && zplTempFile.exists() && macDeviceName != null && !macDeviceName.isBlank()) {
            try {
                printFileWithLpDevice(macDeviceName, zplTempFile);
                return;
            } catch (Exception e) {
                System.err.println("Intento lp con device específico falló: " + e.getMessage() + " — aplicando fallback a rutas existentes.");
                // continuar al fallback sin detener otras máquinas
            }
        }

        // Reutiliza la lógica existente para imprimir (lp normal o socket fallback).
        // Por ejemplo, llamar a printZpl(zplContent, zplTempFile, printerIp, port);
        printZpl(zplContent, zplTempFile, printerIp, port);
    }

    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}
