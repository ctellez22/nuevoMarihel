package impr;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// Utilidad manual/experimental de ZPL; se reubica fuera de src/main/java para no formar parte del build principal.
 class ZPLPrinter {

    public static void main(String[] args) {
        // Contenido del archivo ZPL
        String zplContent = "^XA\n" +
                "^PW984\n" +
                "^LL102\n" +
                "^FO50,20^A0N,24,24^FD9.5g^FS\n" +
                "^FO50,50^A0N,24,24^FD1'500.000^FS\n" +
                "^FO227,30^BY1,2,30^BCN,30,Y,N^FD123456789012^FS\n" +
                "^XZ";

        // Ruta del archivo ZPL a crear
        String filePath = System.getProperty("user.home") + "/Desktop/bebeBoste.zpl";

        try {
            // Crear el archivo ZPL
            File zplFile = new File(filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(zplFile))) {
                writer.write(zplContent);
            }

            System.out.println("Archivo ZPL creado exitosamente en: " + zplFile.getAbsolutePath());

            // Comando para enviar el archivo a la impresora
            String printerCommand = "lp -d Zebra_Technologies_ZTC_ZD421-203dpi_ZPL -o raw " + zplFile.getAbsolutePath();
            Process process = new ProcessBuilder("bash", "-c", printerCommand).start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Etiqueta enviada a la impresora exitosamente.");
            } else {
                System.err.println("Error al enviar la etiqueta a la impresora. Código de salida: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error al crear o enviar el archivo ZPL.");
        }
    }
}


