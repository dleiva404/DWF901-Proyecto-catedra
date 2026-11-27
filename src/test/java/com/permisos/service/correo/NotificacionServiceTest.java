package com.permisos.service.correo;

import com.permisos.service.NotificacionService;
import org.junit.Test;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotificacionServiceTest {

    @Test
    public void noIntentaEnviarCuandoElCorreoEstaDeshabilitado() {
        AtomicBoolean enviado = new AtomicBoolean();
        EnviadorCorreo enviador = (destino, asunto, texto) ->
                enviado.set(true);
        NotificacionService servicio = new NotificacionService(
                enviador, configuracion(false));

        try {
            servicio.solicitudAprobada("empleado@example.test", "Empleado",
                    "VACACIONES", LocalDate.now(), LocalDate.now());
        } finally {
            servicio.cerrar();
        }

        assertFalse(enviado.get());
    }

    @Test
    public void unFalloDeEnvioSeProcesaAsincronicamenteSinPropagarse() throws Exception {
        CountDownLatch intentoFinalizado = new CountDownLatch(1);
        EnviadorCorreo enviador = (destino, asunto, texto) -> {
            try {
                throw new MessagingException("fallo de prueba");
            } finally {
                intentoFinalizado.countDown();
            }
        };
        NotificacionService servicio = new NotificacionService(
                enviador, configuracion(true));

        try {
            servicio.solicitudAprobada("empleado@example.test", "Empleado",
                    "VACACIONES", LocalDate.now(), LocalDate.now());
            assertTrue(intentoFinalizado.await(2, TimeUnit.SECONDS));
        } finally {
            servicio.cerrar();
        }
    }

    private ConfiguracionCorreo configuracion(boolean habilitada) {
        Properties propiedades = new Properties();
        propiedades.setProperty("mail.habilitado",
                Boolean.toString(habilitada));
        return new ConfiguracionCorreo(propiedades,
                new HashMap<String, String>(), new Properties());
    }
}
