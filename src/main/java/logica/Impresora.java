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
        String printerName = System.getProperty("printer.name", "ZDesigner ZD230");
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
            System.err.println("Detalle del error: " + e.getMessage());
        }
    }


    private void imprimirEnMac(String zplData) {
        // Detectar automáticamente impresora Zebra disponible (ZD230, ZD421, etc.)
        String printerName = System.getProperty("printer.name", detectarImpresoraMac());

        // Ruta del archivo en el escritorio
        File zplFile = new File(System.getProperty("user.home") + "/Desktop/bebeBoste.zpl");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(zplFile, StandardCharsets.US_ASCII))) { // ZPL = ASCII
                writer.write(zplData);
            }
            System.out.println("Archivo ZPL creado en: " + zplFile.getAbsolutePath());

            // Primero, intentar por socket TCP (más confiable en redes modernas)
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

            // Intento 1: Socket TCP si está configurado (RECOMENDADO)
            if (printerIp != null && !printerIp.isBlank()) {
                try {
                    PrinterUtils.sendZplOverSocket(printerIp, port, zplData);
                    System.out.println("✅ Etiqueta enviada por socket TCP a " + printerIp + ":" + port);
                    return;
                } catch (IOException e) {
                    System.err.println("⚠️  Intento por socket falló: " + e.getMessage() + " — intentando 'lp' command...");
                }
            } else {
                System.out.println("ℹ️  Sin configuración de IP. Intentando comando 'lp' del sistema...");
            }

            // Intento 2: /usr/bin/lp (requiere CUPS instalado)
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
                    System.out.println("✅ Etiqueta enviada con /usr/bin/lp a " + printerName + " correctamente.");
                    return;
                } else {
                    System.err.println("⚠️  /usr/bin/lp falló con código: " + exit1);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("⚠️  /usr/bin/lp no está disponible: " + e.getMessage());
            }

            // Intento 3: 'lp' (sin ruta)
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
                    System.out.println("✅ Etiqueta enviada con 'lp' a " + printerName + " correctamente.");
                    return;
                }
            } catch (IOException | InterruptedException ignored) {}

            // Si llegamos aquí, todos los intentos fallaron
            mostrarInstruccionesAyuda(printerIp, port);

        } catch (IOException e) {
            System.err.println("❌ Error al crear el archivo ZPL en macOS: " + e.getMessage());
        }
    }

    // Nuevo helper para mostrar instrucciones útiles cuando falla la impresión
    private void mostrarInstruccionesAyuda(String printerIp, int port) {
        System.err.println("\n" + "=".repeat(70));
        System.err.println("❌ NO SE PUDO IMPRIMIR LA ETIQUETA");
        System.err.println("=".repeat(70));
        System.err.println("\n📌 SOLUCIONES RECOMENDADAS:\n");
        
        System.err.println("OPCIÓN 1: Usar Impresión por Socket TCP (RECOMENDADA)");
        System.err.println("  Obtén la IP de tu impresora ZD230:");
        System.err.println("    1. Panel de control de la impresora (botón en la ZD230)");
        System.err.println("    2. O busca: arp -a | grep -i zebra");
        System.err.println("");
        System.err.println("  Luego ejecuta con:");
        System.err.println("    java -Dprinter.ip=192.168.X.X -jar marihel.jar");
        System.err.println("    O con variables de entorno:");
        System.err.println("    export PRINTER_IP=192.168.X.X && java -jar marihel.jar");
        System.err.println("");
        
        System.err.println("OPCIÓN 2: Instalar CUPS (para usar comando 'lp')");
        System.err.println("  En terminal:");
        System.err.println("    chmod +x scripts/install-cups-macos.sh");
        System.err.println("    ./scripts/install-cups-macos.sh");
        System.err.println("");
        System.err.println("  O manualmente:");
        System.err.println("    brew install cups");
        System.err.println("");
        
        System.err.println("OPCIÓN 3: Agregar impresora a macOS");
        System.err.println("  1. Sistema > Impresoras y escáneres");
        System.err.println("  2. Haz clic en '+' para agregar");
        System.err.println("  3. Selecciona tu Zebra ZD230");
        System.err.println("  4. Confirma el nombre exacto con: lpstat -p -d");
        System.err.println("  5. Usa ese nombre con: java -Dprinter.name=\"NombreExacto\" -jar marihel.jar");
        System.err.println("");
        
        System.err.println("📋 VERIFICACIÓN RÁPIDA:");
        System.err.println("  • Impresoras disponibles: lpstat -p -d");
        System.err.println("  • Conectividad red: ping 192.168.X.X");
        System.err.println("  • Puerto 9100: nc -zv 192.168.X.X 9100");
        System.err.println("=".repeat(70) + "\n");
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

    // Nuevo helper: detecta impresoras Zebra disponibles en macOS (ZD230, ZD421, etc.)
    private static String detectarImpresoraMac() {
        String[] impresoras = {
            "Zebra_Technologies_ZTC_ZD230_203dpi_ZPL",  // ZD230 203dpi - primaria
            "Zebra_Technologies_ZTC_ZD230_ZPL",          // ZD230 alternativo
            "ZD230",
            "Zebra_Technologies_ZTC_ZD421_203dpi_ZPL",  // ZD421 203dpi
            "ZD421",
            "Zebra_ZD230",
            "Zebra_ZD421"
        };
        
        for (String impresora : impresoras) {
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/lpstat", "-p", "-d");
            pb.redirectErrorStream(true);
            try {
                Process p = pb.start();
                String output = readProcessOutput(p.getInputStream());
                p.waitFor();
                if (output.toLowerCase().contains(impresora.toLowerCase())) {
                    System.out.println("Impresora detectada: " + impresora);
                    return impresora;
                }
            } catch (Exception ignored) {}
        }
        
        // Por defecto retorna ZD230
        return "Zebra_Technologies_ZTC_ZD230_ZPL";
    }
}
