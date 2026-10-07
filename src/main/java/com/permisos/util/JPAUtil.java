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

            if (in == null) {
                throw new RuntimeException(
                        "No se encontró db.properties en el classpath.");
            }
            props.load(in);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar db.properties", e);
        }

        Map<String, String> conexion = new HashMap<>();
        conexion.put("javax.persistence.jdbc.url",
                props.getProperty("db.url", ""));
        conexion.put("javax.persistence.jdbc.user",
                props.getProperty("db.user", ""));
        conexion.put("javax.persistence.jdbc.password",
                props.getProperty("db.password", ""));

        return Persistence.createEntityManagerFactory("SistemaRRHHPU", conexion);
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
