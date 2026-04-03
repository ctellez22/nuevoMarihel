package logica;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.marihel.inventario.utils.PrinterUtils;

public class Impresora {

    public void imprimirEtiqueta(String zplData) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            imprimirEnWindows(zplData);
        } else if (osName.contains("mac")) {
            imprimirEnMac(zplData);
        } else {
            System.err.println("Sistema operativo no soportado para la impresión.");
        }
    }

    private void imprimirEnWindows(String zplData) {
        String filePath = System.getProperty("user.home") + "\\Desktop\\bebeBoste.zpl";
        String printerName = "ZDesigner ZD421-300dpi ZPL";
        String rawPrintDirectory = "C:\\Users\\ASUS\\OneDrive\\Escritorio";

        try {
            // Crear el archivo ZPL
            File zplFile = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(zplFile, StandardCharsets.UTF_8))) {
                writer.write(zplData);
            }

            System.out.println("Archivo ZPL creado exitosamente en: " + zplFile.getAbsolutePath());

            // Crear el comando para enviar el archivo ZPL a la impresora
            String rawPrintCommand = String.format("RawPrint /f \"%s\" /pr \"%s\"", zplFile.getAbsolutePath(), printerName);
            ProcessBuilder rawPrintProcessBuilder = new ProcessBuilder("cmd", "/c", rawPrintCommand);
            rawPrintProcessBuilder.directory(new File(rawPrintDirectory));
            rawPrintProcessBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            rawPrintProcessBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process rawPrintProcess = rawPrintProcessBuilder.start();

            // Esperar a que termine la impresión
            int printExitCode = rawPrintProcess.waitFor();
            if (printExitCode == 0) {
                System.out.println("Etiqueta enviada a la impresora exitosamente.");
            } else {
                System.err.println("Error al enviar la etiqueta a la impresora. Código de salida: " + printExitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al crear el archivo ZPL o al encontrar RawPrint.exe.");
            e.printStackTrace();
        }
    }

    private void imprimirEnMacc(String zplData) {
        String filePath = System.getProperty("user.home") + "/Desktop/bebeBoste.zpl";
        String printerName = "Zebra_Technologies_ZTC_ZD421-203dpi_ZPL"; // Cambiar por el nombre exacto de tu impresora en macOS

        try {
            // Crear el archivo ZPL
            File zplFile = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(zplFile, StandardCharsets.UTF_8))) {
                writer.write(zplData);
            }

            System.out.println("Archivo ZPL creado exitosamente en: " + zplFile.getAbsolutePath());

            // Crear el comando para enviar el archivo ZPL en modo raw a la impresora
            String lpCommand = String.format("lp -d \"%s\" -o raw \"%s\"", printerName, zplFile.getAbsolutePath());

            // Configurar el proceso para ejecutar el comando
            ProcessBuilder lpProcessBuilder = new ProcessBuilder("bash", "-c", lpCommand);

            // Redirigir salida estándar y errores para mayor claridad
            lpProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            lpProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            // Iniciar el proceso
            Process lpProcess = lpProcessBuilder.start();

            // Esperar a que termine la impresión
            int printExitCode = lpProcess.waitFor();
            if (printExitCode == 0) {
                System.out.println("Etiqueta enviada a la impresora exitosamente.");
            } else {
                System.err.println("Error al enviar la etiqueta a la impresora. Código de salida: " + printExitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al crear el archivo ZPL o al enviar la etiqueta a la impresora.");
            e.printStackTrace();
        }
    }


    private void imprimirEnMac(String zplData) {
        String defaultPrinterName = "Zebra_Technologies_ZTC_ZD421_203dpi_ZPL"; // <- con guion_bajo

        // Nombre de impresora que se usará si no se especifica otro
        String printerName = System.getProperty("printer.name", defaultPrinterName);

        // Ruta del archivo en el escritorio
        File zplFile = new File(System.getProperty("user.home") + "/Desktop/bebeBoste.zpl");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(zplFile, StandardCharsets.US_ASCII))) { // ZPL = ASCII
                writer.write(zplData);
            }
            System.out.println("Archivo ZPL creado en: " + zplFile.getAbsolutePath());

            // Intento 1: /usr/bin/lp (ruta absoluta)
            ProcessBuilder pb1 = new ProcessBuilder(
                    "/usr/bin/lp",
                    "-d", printerName,
                    "-o", "raw",
                    zplFile.getAbsolutePath()
            );
            pb1.redirectErrorStream(true);
            try {
                Process p1 = pb1.start();
                String out1 = readProcessOutput(p1.getInputStream());
                int exit1 = p1.waitFor();
                if (exit1 == 0) {
                    System.out.println("Etiqueta enviada con /usr/bin/lp correctamente.");
                    return;
                } else {
                    System.err.println("/usr/bin/lp falló con código: " + exit1 + ". Salida:\n" + out1 + "\nIntentando 'lp' en PATH...");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Fallo al ejecutar /usr/bin/lp: " + e.getMessage() + ". Intentando 'lp' en PATH...");
            }

            // Intento 2: 'lp' (sin ruta) — algunas configuraciones tienen PATH configurado
            ProcessBuilder pb2 = new ProcessBuilder(
                    "lp",
                    "-d", printerName,
                    "-o", "raw",
                    zplFile.getAbsolutePath()
            );
            pb2.redirectErrorStream(true);
            try {
                Process p2 = pb2.start();
                String out2 = readProcessOutput(p2.getInputStream());
                int exit2 = p2.waitFor();
                if (exit2 == 0) {
                    System.out.println("Etiqueta enviada con 'lp' en PATH correctamente.");
                    return;
                } else {
                    System.err.println("'lp' en PATH falló con código: " + exit2 + ". Salida:\n" + out2 + "\nIntentando envío por socket si está configurado...");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Fallo al ejecutar 'lp' en PATH: " + e.getMessage() + ". Intentando envío por socket si está configurado...");
            }

            // Intento 3: envío por socket directo (fallback). Busca IP/PORT en sistema o env vars.
            String printerIp = System.getProperty("printer.ip");
            if (printerIp == null || printerIp.isBlank()) {
                printerIp = System.getenv("PRINTER_IP");
            }
            String portProp = System.getProperty("printer.port");
            if (portProp == null || portProp.isBlank()) {
                portProp = System.getenv("PRINTER_PORT");
            }
            int port = 9100; // puerto por defecto
            if (portProp != null && !portProp.isBlank()) {
                try {
                    port = Integer.parseInt(portProp);
                } catch (NumberFormatException ignored) {}
            }

            if (printerIp != null && !printerIp.isBlank()) {
                try {
                    PrinterUtils.sendZplOverSocket(printerIp, port, zplData);
                    System.out.println("Etiqueta enviada por socket a " + printerIp + ":" + port);
                    return;
                } catch (IOException e) {
                    System.err.println("Falló el envío por socket a " + printerIp + ":" + port + " — " + e.getMessage());
                }
            }

            // Si llegamos acá, todos los intentos fallaron
            System.err.println("No se pudo enviar la etiqueta: ni /usr/bin/lp ni 'lp' en PATH funcionaron, y no hay IP/PORT configurada o el envío por socket falló.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al crear o escribir el archivo ZPL en macOS: " + e.getMessage());
        }
    }

    // Nuevo helper local para leer la salida del proceso (no bloqueante cuando proceso ya terminó)
    private static String readProcessOutput(InputStream is) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            return "(error leyendo salida del proceso: " + e.getMessage() + ")";
        }
    }
}
