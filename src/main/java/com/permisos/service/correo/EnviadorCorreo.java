package com.permisos.service.correo;

import javax.mail.MessagingException;

public interface EnviadorCorreo {

    void enviar(String destinatario, String asunto, String texto)
            throws MessagingException;
}
