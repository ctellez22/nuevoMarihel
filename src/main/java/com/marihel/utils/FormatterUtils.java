package com.marihel.utils;

import java.text.DecimalFormat;

public class FormatterUtils {
    private static final DecimalFormat PESO_FORMAT = new DecimalFormat("0.00");

    private FormatterUtils() {
        // Clase de utilidad, no se debe instanciar
    }

    /**
     * Formatea un peso (double) a 2 decimales.
     * 
     * @param peso valor en gramos
     * @return peso formateado con 2 decimales
     */
    public static String formatearPeso(double peso) {
        synchronized (PESO_FORMAT) {
            return PESO_FORMAT.format(peso);
        }
    }

    /**
     * Formatea un número genérico a 2 decimales.
     * 
     * @param valor número a formatear
     * @return valor formateado con 2 decimales
     */
    public static String formatearDecimal(double valor) {
        synchronized (PESO_FORMAT) {
            return PESO_FORMAT.format(valor);
        }
    }
}

