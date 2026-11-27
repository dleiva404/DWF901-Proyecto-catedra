package com.permisos.service.correo;

import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class SmtpEnviadorCorreo implements EnviadorCorreo {

    private final ConfiguracionCorreo configuracion;

    public SmtpEnviadorCorreo(ConfiguracionCorreo configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public void enviar(String destinatario, String asunto, String texto)
            throws MessagingException {
        String usuario = configuracion.getUsuario();
        String clave = configuracion.getClave();
        String remitente = configuracion.getRemitente();
        if (vacio(configuracion.getHost()) || vacio(usuario)
                || vacio(clave) || vacio(remitente)) {
            throw new MessagingException("Configuración SMTP incompleta.");
        }

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", configuracion.getHost());
        propiedades.put("mail.smtp.port",
                Integer.toString(configuracion.getPuerto()));
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.starttls.required", "true");
        propiedades.put("mail.smtp.connectiontimeout", "10000");
        propiedades.put("mail.smtp.timeout", "10000");
        propiedades.put("mail.smtp.writetimeout", "10000");

        Session sesion = Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });

        MimeMessage mensaje = new MimeMessage(sesion);
        mensaje.setFrom(new InternetAddress(remitente));
        mensaje.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(destinatario, true));
        mensaje.setSubject(asunto, "UTF-8");
        mensaje.setText(texto, "UTF-8");
        Transport.send(mensaje);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
