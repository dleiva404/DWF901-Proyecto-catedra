package com.permisos.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión JDBC a MySQL. Los parámetros se leen de
 * db.properties (configuración externa), tal como acordó el equipo.
 */
public class DBConnection {

    private static Properties props = new Properties();

    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("No se encontró db.properties en el classpath.");
            }
            props.load(input);
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar db.properties", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de MySQL en el classpath", e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
