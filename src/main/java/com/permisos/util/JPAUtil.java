package com.permisos.util;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JPAUtil {

    private static final EntityManagerFactory EMF =
            crearFactory();

    private JPAUtil() {

    }

    private static EntityManagerFactory crearFactory() {
        Properties props = new Properties();

        try (InputStream in = JPAUtil.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in != null) {
                props.load(in);
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar db.properties", e);
        }

        Map<String, String> conexion = new HashMap<>();
        conexion.put("javax.persistence.jdbc.url", valor(props, "db.url", "DB_URL"));
        conexion.put("javax.persistence.jdbc.user", valor(props, "db.user", "DB_USER"));
        conexion.put("javax.persistence.jdbc.password", valor(props, "db.password", "DB_PASSWORD"));

        if (conexion.get("javax.persistence.jdbc.url").isEmpty()) {
            throw new RuntimeException("Falta la configuración de la base de datos: copia "
                    + "db.properties.example como db.properties o define DB_URL, DB_USER y DB_PASSWORD.");
        }

        return Persistence.createEntityManagerFactory("SistemaRRHHPU", conexion);
    }

    private static String valor(Properties props, String clave, String variable) {
        String v = System.getenv(variable);
        if (v == null || v.isEmpty()) {
            v = System.getProperty(clave);
        }
        if (v == null || v.isEmpty()) {
            v = props.getProperty(clave, "");
        }
        return v;
    }

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void cerrar() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}
