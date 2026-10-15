package com.permisos.bean;

import java.time.LocalDate;
import java.util.Locale;

/** Utilidades de formato compartidas por los beans de las vistas. */
public final class Formato {

    private static final String[] MESES = {"ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic"};

    private Formato() {
    }

    public static String capitalizar(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /** Devuelve PENDIENTE, APROBADA, RECHAZADA o CANCELADA. */
    public static String estado(String estado) {
        if (estado == null) {
            return "PENDIENTE";
        }
        String e = estado.toUpperCase(Locale.ROOT);
        if (e.startsWith("APROBAD")) {
            return "APROBADA";
        }
        if (e.startsWith("RECHAZAD")) {
            return "RECHAZADA";
        }
        if (e.startsWith("CANCELAD")) {
            return "CANCELADA";
        }
        return "PENDIENTE";
    }

    public static String fecha(LocalDate f) {
        return f == null ? "" : f.getDayOfMonth() + " " + MESES[f.getMonthValue() - 1] + " " + f.getYear();
    }

    public static String rango(LocalDate ini, LocalDate fin) {
        if (ini == null || fin == null) {
            return "";
        }
        if (ini.equals(fin)) {
            return fecha(ini);
        }
        if (ini.getYear() != fin.getYear()) {
            return fecha(ini) + " – " + fecha(fin);
        }
        if (ini.getMonthValue() == fin.getMonthValue()) {
            return ini.getDayOfMonth() + " – " + fin.getDayOfMonth() + " "
                    + MESES[fin.getMonthValue() - 1] + " " + fin.getYear();
        }
        return ini.getDayOfMonth() + " " + MESES[ini.getMonthValue() - 1] + " – "
                + fin.getDayOfMonth() + " " + MESES[fin.getMonthValue() - 1] + " " + fin.getYear();
    }

    public static String dias(int n) {
        return n + (n == 1 ? " día" : " días");
    }
}