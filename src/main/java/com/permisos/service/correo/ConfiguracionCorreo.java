package com.permisos.service.correo;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Component
public final class ConfiguracionCorreo {

    private final Properties archivo;
    private final Map<String, String> entorno;
    private final Properties sistema;

    public ConfiguracionCorreo() {
        this(cargarArchivo(), System.getenv(), System.getProperties());
    }

    ConfiguracionCorreo(Properties archivo, Map<String, String> entorno,
                        Properties sistema) {
        this.archivo = copiar(archivo);
        this.entorno = entorno == null
                ? Collections.<String, String>emptyMap()
                : entorno;
        this.sistema = copiar(sistema);
    }

    public boolean isHabilitado() {
        return Boolean.parseBoolean(valor(
                "mail.habilitado", "MAIL_HABILITADO", "false"));
    }

    public String getHost() {
        return valor("mail.host", "MAIL_HOST", "");
    }

    public int getPuerto() {
        return Integer.parseInt(valor("mail.puerto", "MAIL_PUERTO", "587"));
    }

    public String getUsuario() {
        return valor("mail.usuario", "MAIL_USUARIO", "");
    }

    public String getClave() {
        return valor("mail.clave", "MAIL_CLAVE", "");
    }

    public String getRemitente() {
        String remitente = valor("mail.remitente", "MAIL_REMITENTE", "");
        return remitente.isEmpty() ? getUsuario() : remitente;
    }

    private String valor(String clave, String variable, String defecto) {
        String valor = entorno.get(variable);
        if (vacio(valor)) {
            valor = sistema.getProperty(clave);
        }
        if (vacio(valor)) {
            valor = archivo.getProperty(clave);
        }
        return vacio(valor) ? defecto : valor.trim();
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static Properties cargarArchivo() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConfiguracionCorreo.class
                .getClassLoader().getResourceAsStream("mail.properties")) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo cargar mail.properties.", ex);
        }
        return propiedades;
    }

    private static Properties copiar(Properties propiedades) {
        Properties copia = new Properties();
        if (propiedades != null) {
            copia.putAll(propiedades);
        }
        return copia;
    }
}
