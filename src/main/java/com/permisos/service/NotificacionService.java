package com.permisos.service;

import com.permisos.service.correo.ConfiguracionCorreo;
import com.permisos.service.correo.EnviadorCorreo;
import com.permisos.service.correo.SmtpEnviadorCorreo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NotificacionService {

    private static final Logger LOG =
            Logger.getLogger(NotificacionService.class.getName());

    private final EnviadorCorreo enviador;
    private final ConfiguracionCorreo configuracion;
    private final ExecutorService executor;

    public NotificacionService() {
        this(new ConfiguracionCorreo());
    }

    private NotificacionService(ConfiguracionCorreo configuracion) {
        this(new SmtpEnviadorCorreo(configuracion), configuracion);
    }

    @Autowired
    public NotificacionService(EnviadorCorreo enviador,
                              ConfiguracionCorreo configuracion) {
        this.enviador = enviador;
        this.configuracion = configuracion;
        this.executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private int numero;

            @Override
            public synchronized Thread newThread(Runnable tarea) {
                Thread hilo = new Thread(tarea, "correo-notificacion-" + ++numero);
                hilo.setDaemon(true);
                return hilo;
            }
        });
    }

    public void solicitudAprobada(String destinatario, String empleado,
                                  String tipo, LocalDate inicio, LocalDate fin) {
        encolar("solicitud aprobada", destinatario,
                "Tu solicitud fue aprobada",
                "Hola " + empleado + ",\n\n"
                        + "Tu solicitud de " + tipo + " para el período "
                        + periodo(inicio, fin) + " fue aprobada.");
    }

    public void solicitudRechazada(String destinatario, String empleado,
                                   String tipo, LocalDate inicio, LocalDate fin,
                                   String motivo) {
        encolar("solicitud rechazada", destinatario,
                "Tu solicitud fue rechazada",
                "Hola " + empleado + ",\n\n"
                        + "Tu solicitud de " + tipo + " para el período "
                        + periodo(inicio, fin) + " fue rechazada.\n"
                        + "Motivo: " + texto(motivo, "No se indicó un motivo."));
    }

    public void solicitudPorRevisar(String destinatario, String empleado,
                                    String tipo, LocalDate inicio, LocalDate fin) {
        encolar("solicitud por revisar", destinatario,
                "Nueva solicitud pendiente de revisión",
                "La persona " + empleado + " registró una solicitud de "
                        + tipo + " para el período " + periodo(inicio, fin)
                        + ". Inicia sesión para revisarla.");
    }

    public void constanciaResuelta(String destinatario, String empleado,
                                   String tipo, boolean aprobada,
                                   String motivo) {
        String estado = aprobada ? "aprobada" : "rechazada";
        String cuerpo = "Hola " + empleado + ",\n\nTu solicitud de constancia "
                + tipo + " fue " + estado + ".";
        if (!aprobada && !vacio(motivo)) {
            cuerpo += "\nMotivo: " + motivo;
        }
        encolar("constancia " + estado, destinatario,
                "Tu solicitud de constancia fue " + estado, cuerpo);
    }

    public void codigoRecuperacion(String destinatario, String empleado,
                                   String codigo) {
        encolar("recuperación de contraseña", destinatario,
                "Código para recuperar tu contraseña",
                "Hola " + empleado + ",\n\n"
                        + "Tu código de recuperación es:\n" + codigo + "\n\n"
                        + "El código vence en 30 minutos y solo se puede usar una vez. "
                        + "Si no solicitaste este cambio, ignora este mensaje.");
    }

    @PreDestroy
    public void cerrar() {
        executor.shutdown();
    }

    private void encolar(String evento, String destinatario,
                         String asunto, String cuerpo) {
        if (vacio(destinatario)) {
            LOG.info("No se envió correo para " + evento
                    + ": el destinatario no tiene correo registrado.");
            return;
        }
        if (!configuracion.isHabilitado()) {
            LOG.info("No se envió correo para " + evento
                    + ": mail.habilitado=false.");
            return;
        }

        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        enviador.enviar(destinatario, asunto, cuerpo);
                    } catch (MessagingException ex) {
                        registrarFallo(evento, ex);
                    } catch (RuntimeException ex) {
                        registrarFallo(evento, ex);
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            registrarFallo(evento, ex);
        }
    }

    private static void registrarFallo(String evento, Exception ex) {
        LOG.log(Level.WARNING,
                "No se pudo enviar correo para " + evento + " ("
                        + ex.getClass().getSimpleName() + ").");
    }

    private static String periodo(LocalDate inicio, LocalDate fin) {
        return String.valueOf(inicio) + " al " + String.valueOf(fin);
    }

    private static String texto(String valor, String defecto) {
        return vacio(valor) ? defecto : valor.trim();
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
