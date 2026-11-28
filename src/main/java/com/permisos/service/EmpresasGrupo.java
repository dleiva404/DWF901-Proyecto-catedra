package com.permisos.service;

/**
 * Regla unica para la empresa emisora de una constancia y su logo.
 * La usan la web (ConstanciaServlet, ConstanciasBean), la API (ConstanciaService)
 * y el generador del PDF, para que todos den siempre el mismo resultado.
 */
public final class EmpresasGrupo {

    private EmpresasGrupo() {
    }

    /** Empresa afiliada si la hay; si no, "Didelco " + sucursal (sin duplicar el prefijo). */
    public static String emisora(String empresaEmpleado, String nombreSucursal) {
        if (empresaEmpleado != null && !empresaEmpleado.trim().isEmpty()) {
            return empresaEmpleado.trim();
        }
        String base = (nombreSucursal != null) ? nombreSucursal : "Central";
        return base.toLowerCase().startsWith("didelco") ? base : "Didelco " + base;
    }

    /** Archivo de logo dentro de /img/logos (misma tabla de palabras clave de hoy). */
    public static String logo(String empresaEmisora) {
        String e = empresaEmisora.toLowerCase();
        if (e.contains("didelco")) return "LogoDidelco.png";
        if (e.contains("copro")) return "LogoCopro.png";
        if (e.contains("dca")) return "LogoDCA-pmg.png";
        if (e.contains("elf")) return "LogoElf.png";
        if (e.contains("propultran")) return "LogoPropultran.png";
        if (e.contains("steel")) return "LogoSteel.png";
        return "logoGC.png";
    }
}
