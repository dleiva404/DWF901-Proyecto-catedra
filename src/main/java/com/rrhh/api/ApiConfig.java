package com.rrhh.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ApiConfig {

    private static final int TOKEN_MINUTOS_POR_DEFECTO = 30;
    private static final String CORS_POR_DEFECTO =
            "http://localhost:5500,http://127.0.0.1:5500";

    private static final Properties PROPIEDADES = cargar();

    private ApiConfig() {
    }

    private static Properties cargar() {
        Properties props = new Properties();
        try (InputStream in = ApiConfig.class.getClassLoader()
                .getResourceAsStream("api.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ex) {
            // Si no se puede leer, se usan los valores por defecto.
        }
        return props;
    }

    private static String texto(String clave, String porDefecto) {
        String delSistema = System.getProperty("api." + clave);
        if (delSistema != null && !delSistema.trim().isEmpty()) {
            return delSistema.trim();
        }
        String delArchivo = PROPIEDADES.getProperty(clave);
        if (delArchivo != null && !delArchivo.trim().isEmpty()) {
            return delArchivo.trim();
        }
        return porDefecto;
    }

    public static int tokenMinutos() {
        try {
            int minutos = Integer.parseInt(
                    texto("token.minutos",
                            String.valueOf(TOKEN_MINUTOS_POR_DEFECTO)));
            return Math.max(1, minutos);
        } catch (NumberFormatException ex) {
            return TOKEN_MINUTOS_POR_DEFECTO;
        }
    }

    public static List<String> origenesCors() {
        List<String> origenes = new ArrayList<>();
        for (String origen : texto("cors.origenes", CORS_POR_DEFECTO).split(",")) {
            String limpio = origen.trim();
            if (!limpio.isEmpty()) {
                origenes.add(limpio);
            }
        }
        return origenes;
    }
}