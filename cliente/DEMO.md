# Guion de demostración | Cliente consumidor (Parte 5)

## Objetivo

Demostrar que la aplicación React, ejecutada de forma independiente, permite registrar una solicitud, resolverla desde Jefatura, consultar el resultado como empleado y mostrar un error de validación de las reglas del negocio.

## Preparación

- Mantener disponibles MySQL, el backend Java en Tomcat 9 (`http://localhost:8080/SistemaRRHH`) y el cliente React en `http://localhost:5500/`.
- Tener a mano credenciales válidas de un usuario con rol **EMPLEADO** y otro con rol **JEFATURA**. No incluir contraseñas en capturas ni en este documento.
- Emplear un rango **futuro**, de lunes a sábado, que no se solape con solicitudes existentes y que cuente con saldo disponible. Para el error usar un domingo futuro.
- Mostrar la interfaz de React (`localhost:5500`), no la pantalla JSF del backend.

## Flujo 1. El empleado crea una solicitud

**Cuenta:** Empleado (por ejemplo, Juan Pérez).

1. Iniciar sesión en React y abrir **Mis solicitudes**.
2. Mostrar el saldo de vacaciones antes de crear la solicitud.
3. En **Nueva solicitud**, seleccionar **Vacaciones**, ingresar un rango válido de fechas futuras y escribir un motivo, por ejemplo: `Solicitud de vacaciones para demostración`.
4. Seleccionar **Enviar solicitud**.
5. Verificar que aparece el mensaje de éxito, la solicitud en **Actividad reciente** con estado **PENDIENTE** y que aumenta el contador de pendientes.

**Explicación sugerida:** «La solicitud se envía desde nuestra aplicación React y queda registrada para su revisión por Jefatura».

## Flujo 2. Jefatura aprueba la solicitud

**Cuenta:** Jefatura autorizada para el departamento del empleado.

1. Cerrar sesión de Empleado e ingresar con la cuenta de Jefatura.
2. Abrir **Solicitudes de empleados** y localizar la solicitud anterior por nombre, fechas y motivo.
3. Mostrar que se encuentra **PENDIENTE** y elegir **Aprobar**.
4. Verificar el mensaje **Solicitud aprobada correctamente**, que la solicitud deja de aparecer en la bandeja de pendientes y que el contador se actualiza.

**Explicación sugerida:** «Jefatura recibe y resuelve las solicitudes de su departamento desde el cliente React».

## Flujo 3. El empleado consulta la aprobación

**Cuenta:** El mismo Empleado del flujo 1.

1. Cerrar sesión de Jefatura y volver a ingresar con la cuenta del empleado.
2. Entrar a **Mis solicitudes** y localizar la solicitud creada.
3. Comprobar que ahora figura como **APROBADA**, que aumentó el contador de aprobadas y que el saldo de vacaciones refleja los días descontados cuando corresponda.
4. Si se desea, seleccionar el filtro **APROBADA** para mostrar la búsqueda por estado.

**Explicación sugerida:** «El empleado consulta la decisión sin salir del sistema y ve su saldo actualizado».

## Caso de error. Solicitud que incluye únicamente domingo

**Cuenta:** Empleado.

1. En **Nueva solicitud**, elegir **Vacaciones**.
2. Elegir la **misma fecha de domingo futuro** tanto en inicio como en fin.
3. Escribir un motivo válido, por ejemplo: `Prueba de validación de domingo`.
4. Seleccionar **Enviar solicitud**.
5. Verificar que el formulario muestra **La solicitud debe incluir al menos un día de lunes a sábado.** y que no se registra una nueva solicitud.

**Explicación sugerida:** «El sistema aplica las reglas del negocio e impide registrar solicitudes con rangos formados únicamente por domingos».

## Cierre de la demostración

«La Parte 5 es un cliente React independiente que se comunica con la API REST del proyecto. Hemos demostrado el registro de solicitudes, la aprobación por rol, la consulta del resultado y el manejo de una validación del negocio».

**Nota:** Las solicitudes creadas en la demo afectan la base de datos de prueba. Utilizar siempre cuentas y datos de prueba.
