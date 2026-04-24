package logica;

public final class PuntosFisicos {

    private static final String[] VALORES = {"Centro", "Norte", "Sur", "Bodega", "Online"};

    private PuntosFisicos() {
    }

    public static String[] opciones() {
        return VALORES.clone();
    }
}

