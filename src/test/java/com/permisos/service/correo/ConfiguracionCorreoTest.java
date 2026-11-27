package com.permisos.service.correo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfiguracionCorreoTest {

    @Test
    public void tomaVariablesDeEntornoAntesQuePropiedadesYArchivo() {
        Properties archivo = new Properties();
        archivo.setProperty("mail.usuario", "archivo@example.test");
        Properties sistema = new Properties();
        sistema.setProperty("mail.usuario", "sistema@example.test");
        Map<String, String> entorno = new HashMap<>();
        entorno.put("MAIL_USUARIO", "entorno@example.test");

        ConfiguracionCorreo configuracion =
                new ConfiguracionCorreo(archivo, entorno, sistema);

        assertEquals("entorno@example.test", configuracion.getUsuario());
    }

    @Test
    public void usaPropiedadDeSistemaAntesQueArchivo() {
        Properties archivo = new Properties();
        archivo.setProperty("mail.usuario", "archivo@example.test");
        Properties sistema = new Properties();
        sistema.setProperty("mail.usuario", "sistema@example.test");

        ConfiguracionCorreo configuracion = new ConfiguracionCorreo(
                archivo, new HashMap<String, String>(), sistema);

        assertEquals("sistema@example.test", configuracion.getUsuario());
    }

    @Test
    public void elCorreoPermaneceDeshabilitadoPorDefecto() {
        ConfiguracionCorreo configuracion = new ConfiguracionCorreo(
                new Properties(), new HashMap<String, String>(),
                new Properties());

        assertFalse(configuracion.isHabilitado());
    }

    @Test
    public void elRemitenteUsaElUsuarioSiNoSeDefineOtro() {
        Properties archivo = new Properties();
        archivo.setProperty("mail.usuario", "sistema@example.test");
        archivo.setProperty("mail.habilitado", "true");
        ConfiguracionCorreo configuracion = new ConfiguracionCorreo(
                archivo, new HashMap<String, String>(), new Properties());

        assertTrue(configuracion.isHabilitado());
        assertEquals("sistema@example.test", configuracion.getRemitente());
    }
}
