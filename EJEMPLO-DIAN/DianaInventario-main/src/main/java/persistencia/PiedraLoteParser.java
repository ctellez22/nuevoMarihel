package persistencia;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PiedraLoteParser {
    private static final Pattern PIEDRA_PATTERN = Pattern.compile("loteId=(\\d+).*?peso=([^,|)]+)");

    private PiedraLoteParser() {
    }

    static Map<Long, Double> extraerPesoPorLote(String infoPiedra) {
        Map<Long, Double> pesoPorLote = new LinkedHashMap<>();
        if (infoPiedra == null || infoPiedra.isBlank()) {
            return pesoPorLote;
        }

        Matcher matcher = PIEDRA_PATTERN.matcher(infoPiedra);
        boolean encontro = false;
        while (matcher.find()) {
            encontro = true;
            Long loteId = Long.parseLong(matcher.group(1));
            double peso = parseNumero(matcher.group(2));
            pesoPorLote.merge(loteId, peso, Double::sum);
        }

        if (!encontro && infoPiedra.contains("loteId=")) {
            throw new IllegalArgumentException("No se pudo interpretar la información de piedras por lote.");
        }
        return pesoPorLote;
    }

    private static double parseNumero(String valor) {
        String normalizado = valor == null ? "" : valor.trim().replace("'", "").replace(',', '.');
        if (normalizado.isBlank()) {
            return 0.0;
        }
        return Double.parseDouble(normalizado);
    }
}

