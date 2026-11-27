# Correo y contraseñas

## Cambiar la contraseña con sesión

Envía `POST /api/auth/cambiar-contrasena` con el token Bearer de la sesión:

```json
{
  "actual": "contraseñaActual",
  "nueva": "contraseñaNueva"
}
```

La nueva contraseña debe tener al menos 8 caracteres, no puede ser igual a la actual ni al nombre de usuario y tiene un máximo de 128 caracteres. Si responde `204`, vuelve a iniciar sesión: el token anterior queda invalidado. Una contraseña actual incorrecta responde `400`.

## Recuperar una contraseña

1. Envía `POST /api/auth/recuperar` sin token:

   ```json
   { "username": "tuUsuario" }
   ```

   El servidor responde `204` exista o no la cuenta. Si está activa y tiene un correo asociado, recibirás un código de un solo uso válido durante 30 minutos.

2. Envía `POST /api/auth/restablecer` con el código recibido y la nueva contraseña:

   ```json
   {
     "token": "codigoRecibidoPorCorreo",
     "nueva": "contraseñaNueva"
   }
   ```

   Una respuesta `204` confirma el cambio. Los códigos incorrectos, vencidos o ya usados reciben el mismo `400`. Inicia sesión con la contraseña nueva.

## Correo local de desarrollo

El correo está deshabilitado por defecto. Copia `src/main/resources/mail.properties.example` a `src/main/resources/mail.properties` y completa allí la configuración local de una cuenta dedicada al proyecto. Para Gmail, activa la verificación en dos pasos y usa una clave de aplicación, no la contraseña normal de la cuenta. Reinicia Tomcat después de cambiar la configuración. Nunca incluyas contraseñas, claves de aplicación o códigos de recuperación en Git, logs ni capturas. Con `mail.habilitado=false`, no se envía correo y los eventos se registran en el log.

Las notificaciones de solicitudes y constancias se procesan después de guardar los cambios y de forma asíncrona. Un fallo SMTP no cancela la operación ni altera la respuesta de la API.

## Pruebas manuales

Usa una cuenta local de prueba independiente y confirma que puedes restaurarla al terminar. No cambies cuentas compartidas por los scripts existentes. Comprueba el cambio con contraseña actual incorrecta y correcta; para recuperación, prueba un usuario inexistente, un código válido y un código vencido o ya usado. La solicitud de recuperación debe responder `204` en ambos casos de usuario y no debe devolver el código en la respuesta.
