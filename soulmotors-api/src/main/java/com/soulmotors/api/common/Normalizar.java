package com.soulmotors.api.common;

import java.util.Locale;

/** Limpieza de datos antes de guardar y comparar unicidad. */
public final class Normalizar {

    private Normalizar() {
    }

    /** " abc-12d " -> "ABC12D" */
    public static String placa(String placa) {
        return placa == null ? null : placa.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    /** " 1.010.101 " -> "1010101" (los guiones de NIT se conservan) */
    public static String documento(String documento) {
        return documento == null ? null : documento.replaceAll("[\\s.]", "").toUpperCase(Locale.ROOT);
    }

    /** Recorta espacios y colapsa espacios internos repetidos. */
    public static String texto(String texto) {
        return texto == null ? null : texto.trim().replaceAll("\\s+", " ");
    }

    /** Texto opcional: vacío se guarda como null. */
    public static String opcional(String texto) {
        String limpio = texto(texto);
        return limpio == null || limpio.isEmpty() ? null : limpio;
    }

    /** Patrón LIKE en minúsculas, sin comodines del usuario. */
    public static String patronBusqueda(String q) {
        return "%" + q.trim().toLowerCase(Locale.ROOT).replaceAll("[%_\\\\]", "") + "%";
    }
}
