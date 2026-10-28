# Contrato de API REST — SistemaRRHH

**Versión:** 1.2  
**Base URL:** `/SistemaRRHH/api` (en este documento las rutas se escriben sin el prefijo, por ejemplo `/ping`)  
**Formato:** JSON (`application/json`, codificación UTF-8), salvo el PDF de constancia  
**Fechas:** `yyyy-MM-dd` · **Fecha y hora:** `yyyy-MM-dd'T'HH:mm:ss`  
**Autenticación:** token `Bearer` (ver sección 2).

## 1. Convenciones generales

- Las rutas usan nombres en plural y minúsculas; los nombres compuestos van con guion (`/tipos-solicitud`).
- Los identificadores son enteros positivos. Un identificador menor o igual a cero responde `400`.
- Los recursos no devuelven entidades JPA, solo DTO. Nunca se exponen contraseñas, rutas internas de archivos, DUI, salario de referencia ni tokens de verificación de constancias.
- Los campos que no tienen valor se devuelven como `null`.
- **Autenticado** significa cualquier usuario con rol EMPLEADO, JEFATURA o RRHH, actuando sobre sus propios datos.
- El servidor calcula los días de una solicitud (de lunes a sábado, los domingos no cuentan). El cliente nunca envía `diasSolicitados`.
- Un dato que pertenece a otro empleado no se revela aunque se conozca su identificador: responde `404`.

## 2. Autenticación

El cliente inicia sesión con `POST /auth/login`, recibe un `token` y lo envía en cada petición protegida:

```
Authorization: Bearer <token>
```

> **Supuesto pendiente de decisión del grupo:** el token es opaco (cadena aleatoria guardada en el servidor). Si el grupo elige JWT, solo cambia el contenido de `token` y su duración; las rutas y los cuerpos de este contrato no cambian.

El token dura 30 minutos (`expiraEnSegundos: 1800`; el valor se configura en el servidor) y termina antes si el cliente llama a `POST /auth/logout`. Solo `GET /ping` y `POST /auth/login` se pueden llamar sin token.

Respuestas `401` de autenticación, siempre con la cabecera `WWW-Authenticate: Bearer`:

| Situación | `mensaje` |
|---|---|
| Falta la cabecera `Authorization` | `Autenticación requerida` |
| Token alterado, vencido o cerrado con logout | `Token inválido o vencido` |

Un usuario autenticado cuyo rol no puede usar el endpoint recibe `403` con el mensaje `No tiene permisos para esta operación`.

Un cliente en otro origen (Parte 5) necesita CORS: el servidor debe permitir su origen, los métodos `GET, POST, DELETE, OPTIONS` y las cabeceras `Authorization` y `Content-Type`. Los orígenes permitidos se configuran en el servidor (`cors.origenes`); si el origen de la petición no está en la lista, la respuesta no incluye las cabeceras `Access-Control-*` y el navegador bloquea la llamada. Las consultas previas `OPTIONS` no exigen token.

## 3. Códigos HTTP

| Código | Significado |
|---|---|
| 200 | Operación realizada correctamente |
| 201 | Recurso creado (incluye cabecera `Location`) |
| 204 | Operación correcta sin contenido de respuesta |
| 400 | Solicitud mal formada (incluido un cuerpo JSON inválido), campo inválido o identificador inválido |
| 401 | Usuario no autenticado o token vencido |
| 403 | Usuario autenticado sin permiso para esa operación |
| 404 | Recurso inexistente o no accesible para el usuario |
| 405 | Método HTTP no permitido para esa ruta |
| 409 | Regla de negocio o estado del recurso lo impide |
| 415 | El cuerpo de la solicitud no es `application/json` |
| 500 | Error interno inesperado |

## 4. Endpoints

| # | Método | Ruta | Rol | Respuesta correcta | Errores posibles |
|---|---|---|---|---|---|
| 1 | GET | `/ping` | Público | 200 estado del servicio | — |
| 2 | POST | `/auth/login` | Público | 200 token y datos del usuario | 400, 401, 403 |
| 3 | POST | `/auth/logout` | Autenticado | 204 | 401 |
| 4 | GET | `/tipos-solicitud` | Autenticado | 200 catálogo de tipos | 401 |
| 5 | GET | `/vacaciones/saldo` | Autenticado | 200 saldo del año | 400, 401, 404 |
| 6 | GET | `/empleados` | RRHH | 200 lista de empleados activos | 401, 403 |
| 7 | GET | `/empleados/{id}` | RRHH | 200 empleado | 400, 401, 403, 404 |
| 8 | GET | `/solicitudes` | Autenticado | 200 lista según rol | 400, 401 |
| 9 | GET | `/solicitudes/{id}` | Autenticado | 200 solicitud | 400, 401, 404 |
| 10 | POST | `/solicitudes` | Autenticado | 201 solicitud creada | 400, 401, 409 |
| 11 | DELETE | `/solicitudes/{id}` | Autenticado (dueño) | 204 solicitud cancelada | 400, 401, 404, 409 |
| 12 | POST | `/solicitudes/{id}/aprobar` | Jefatura | 204 | 400, 401, 403, 404, 409 |
| 13 | POST | `/solicitudes/{id}/rechazar` | Jefatura | 204 | 400, 401, 403, 404, 409 |
| 14 | GET | `/solicitudes/{id}/historial` | Autenticado | 200 cambios de estado | 400, 401, 404 |
| 15 | GET | `/incapacidades` | Autenticado | 200 lista según rol | 401 |
| 16 | GET | `/incapacidades/{id}` | Autenticado | 200 incapacidad | 400, 401, 404 |
| 17 | POST | `/incapacidades` | Autenticado | 201 incapacidad registrada | 400, 401 |
| 18 | GET | `/constancias` | Autenticado | 200 lista según rol | 401 |
| 19 | GET | `/constancias/{id}` | Autenticado | 200 constancia | 400, 401, 404 |
| 20 | POST | `/constancias` | Autenticado | 201 constancia registrada | 400, 401 |
| 21 | POST | `/constancias/{id}/aprobar` | RRHH | 204 | 400, 401, 403, 404, 409 |
| 22 | POST | `/constancias/{id}/rechazar` | RRHH | 204 | 400, 401, 403, 404, 409 |
| 23 | GET | `/constancias/{id}/pdf` | Dueño o RRHH | 200 `application/pdf` | 400, 401, 404, 409 |

Qué ve cada rol en las listas (endpoints 8, 15 y 18):

- **Empleado:** solo lo propio.
- **Jefatura:** las solicitudes de los empleados de su departamento. Con `?propias=true` ve solo las suyas.
- **RRHH:** todas, solo lectura. Con `?propias=true` ve solo las suyas.

Filtros opcionales de `GET /solicitudes`: `estado` (`PENDIENTE`, `APROBADA`, `RECHAZADA`, `CANCELADA`), `tipoSolicitudId`, `propias`, `desde` y `hasta` (`yyyy-MM-dd`, se comparan con `fechaInicio`), `empleadoId` y `departamentoId`. Los filtros solo reducen lo que el rol ya puede ver: una jefatura no obtiene solicitudes de otro departamento aunque lo pida. Un valor inválido responde `400`.

## 5. Detalle de cada operación

### 5.1. Ping — `GET /ping`

Respuesta `200`:

```json
{
  "status": "ok"
}
```

### 5.2. Login — `POST /auth/login`

Solicitud:

```json
{
  "username": "david.leiva",
  "password": "claveDeEjemplo"
}
```

Respuesta `200`:

```json
{
  "token": "b7c1f0a2-5d3e-4c8a-9e61-2f4d7a90c3b5",
  "tipo": "Bearer",
  "expiraEnSegundos": 1800,
  "usuario": {
    "id": 5,
    "username": "david.leiva",
    "empleadoId": 5,
    "nombre": "David Leiva",
    "rol": "EMPLEADO"
  }
}
```

Errores: `400` si falta `username` o `password` (la respuesta lista los campos faltantes en `campos`, ver sección 6); `401` si las credenciales son incorrectas o el usuario está inactivo (mismo mensaje en ambos casos: `Usuario o contraseña incorrectos.`); `403` si el usuario no tiene un rol asignado.

### 5.3. Logout — `POST /auth/logout`

Sin cuerpo. Respuesta `204`. El token deja de ser válido.

### 5.4. Catálogo de tipos — `GET /tipos-solicitud`

Devuelve solo los tipos activos. Respuesta `200`:

```json
[
  { "id": 1, "nombre": "VACACIONES", "descripcion": "Solicitud de días de vacaciones" },
  { "id": 2, "nombre": "INCAPACIDAD", "descripcion": "Solicitud por incapacidad médica" },
  { "id": 3, "nombre": "AUSENCIA", "descripcion": "Solicitud de ausencia laboral" }
]
```

### 5.5. Saldo de vacaciones — `GET /vacaciones/saldo?anio=2026`

`anio` es opcional; si no se envía se usa el año actual. Respuesta `200`:

```json
{
  "empleadoId": 5,
  "anio": 2026,
  "diasAsignados": 15,
  "diasUtilizados": 3,
  "diasDisponibles": 12
}
```

Errores: `400` si `anio` no es un número de cuatro dígitos; `404` si el empleado no tiene saldo registrado para ese año.

### 5.6. Empleados — `GET /empleados` y `GET /empleados/{id}`

Respuesta `200` de `/empleados/5` (la lista devuelve un arreglo de estos objetos):

```json
{
  "id": 5,
  "nombre": "David",
  "apellido": "Leiva",
  "correo": "david.leiva@empresa.com",
  "telefono": "74444444",
  "sucursalAreaId": 1,
  "departamentoId": 4,
  "cargo": "Analista de Sistemas",
  "fechaIngreso": "2024-02-01",
  "activo": true,
  "empresa": null
}
```

### 5.7. Listar solicitudes — `GET /solicitudes`

Respuesta `200` (arreglo de solicitudes):

```json
[
  {
    "id": 15,
    "empleadoId": 5,
    "tipoSolicitudId": 1,
    "fechaSolicitud": "2026-10-27T18:30:00",
    "fechaInicio": "2026-11-09",
    "fechaFin": "2026-11-13",
    "diasSolicitados": 5,
    "motivo": "Vacaciones familiares",
    "estado": "PENDIENTE",
    "motivoRechazo": null
  }
]
```

### 5.8. Consultar una solicitud — `GET /solicitudes/{id}`

Respuesta `200`: un objeto con los mismos campos del ejemplo anterior.

### 5.9. Crear solicitud — `POST /solicitudes`

Solicitud:

```json
{
  "tipoSolicitudId": 1,
  "fechaInicio": "2026-11-09",
  "fechaFin": "2026-11-13",
  "motivo": "Vacaciones familiares"
}
```

Respuesta `201` (cabecera `Location: /SistemaRRHH/api/solicitudes/15`):

```json
{
  "id": 15,
  "empleadoId": 5,
  "tipoSolicitudId": 1,
  "fechaSolicitud": "2026-10-27T18:30:00",
  "fechaInicio": "2026-11-09",
  "fechaFin": "2026-11-13",
  "diasSolicitados": 5,
  "motivo": "Vacaciones familiares",
  "estado": "PENDIENTE",
  "motivoRechazo": null
}
```

Reglas:

- Todos los campos son obligatorios. `motivo` tiene entre 5 y 250 caracteres.
- `fechaFin` no puede ser anterior a `fechaInicio` y `fechaInicio` no puede ser una fecha pasada.
- Si el rango solo incluye domingos, se rechaza.
- El tipo `INCAPACIDAD` no se crea aquí: se usa `POST /incapacidades`.
- Si el tipo es `VACACIONES`, los días no pueden superar el saldo disponible menos los días ya comprometidos en solicitudes pendientes del mismo año.

Errores: `400` por campo faltante, formato de fecha inválido, fechas incorrectas, tipo inexistente o tipo `INCAPACIDAD`; `409` por saldo insuficiente o por no tener saldo registrado para ese año.

Ejemplo de error `409`:

```json
{
  "codigo": 409,
  "mensaje": "No tiene suficientes días de vacaciones: solicita 5, tiene 3 disponibles y 0 ya comprometidos en solicitudes pendientes.",
  "ruta": "/api/solicitudes",
  "fechaHora": "2026-10-27T18:30:00"
}
```

### 5.10. Cancelar solicitud — `DELETE /solicitudes/{id}`

Sin cuerpo. Respuesta `204`. Solo el dueño puede cancelar y solo si está `PENDIENTE`; si ya no lo está responde `409`. Si la solicitud es de otro empleado responde `404`.

### 5.11. Aprobar solicitud — `POST /solicitudes/{id}/aprobar`

Sin cuerpo. Respuesta `204`. Si es de vacaciones, descuenta los días del saldo y registra el cambio en el historial.

Errores:

- `403` si la solicitud es del propio usuario o de un empleado de otro departamento.
- `404` si la solicitud no existe.
- `409` si la solicitud no está `PENDIENTE` o el saldo ya no alcanza.

### 5.12. Rechazar solicitud — `POST /solicitudes/{id}/rechazar`

Solicitud:

```json
{
  "motivoRechazo": "Las fechas solicitadas requieren corrección."
}
```

Respuesta `204`. `motivoRechazo` es obligatorio (`400` si falta o está vacío). Los demás errores son los mismos de aprobar.

### 5.13. Historial de una solicitud — `GET /solicitudes/{id}/historial`

Lista los cambios de estado, del más antiguo al más reciente. Respuesta `200`:

```json
[
  {
    "id": 31,
    "solicitudId": 15,
    "estadoAnterior": "PENDIENTE",
    "estadoNuevo": "APROBADA",
    "comentario": "Se descontaron 5 día(s) del saldo de vacaciones 2026.",
    "usuarioId": 2,
    "fecha": "2026-10-28T09:15:00"
  }
]
```

### 5.14. Incapacidades — `GET /incapacidades` y `GET /incapacidades/{id}`

Respuesta `200` de `/incapacidades/4` (la lista devuelve un arreglo de estos objetos):

```json
{
  "id": 4,
  "solicitudId": 16,
  "numeroDocumento": "ISSS-2026-004512",
  "fechaInicio": "2026-10-26",
  "fechaFin": "2026-10-28",
  "documentoNombre": null,
  "observaciones": "Reposo por indicación médica."
}
```

### 5.15. Registrar incapacidad — `POST /incapacidades`

Crea de una sola vez la solicitud de tipo `INCAPACIDAD` y su registro de incapacidad. Solicitud:

```json
{
  "numeroDocumento": "ISSS-2026-004512",
  "fechaInicio": "2026-10-26",
  "fechaFin": "2026-10-28",
  "observaciones": "Reposo por indicación médica."
}
```

Respuesta `201` (cabecera `Location: /SistemaRRHH/api/incapacidades/4`): el mismo objeto del ejemplo 5.14.

Reglas: `numeroDocumento`, `fechaInicio` y `fechaFin` son obligatorios; `observaciones` es opcional. `fechaFin` no puede ser anterior a `fechaInicio`. Se permiten fechas pasadas porque una incapacidad se registra después de ocurrir. Si el rango solo incluye domingos, se rechaza. En esta fase no se sube el archivo adjunto, por eso `documentoNombre` es `null`.

Errores: `400` por campo obligatorio faltante, formato de fecha inválido o rango incorrecto.

### 5.16. Constancias — `GET /constancias` y `GET /constancias/{id}`

Respuesta `200` de `/constancias/8` (la lista devuelve un arreglo de estos objetos):

```json
{
  "id": 8,
  "empleadoId": 5,
  "tipo": "Laboral",
  "institucion": "Banco de ejemplo",
  "motivo": "Trámite de crédito",
  "empresaEmisora": "Didelco Santa Ana",
  "fechaSolicitud": "2026-10-27T18:40:00",
  "estado": "PENDIENTE",
  "fechaRespuesta": null
}
```

Los estados posibles son `PENDIENTE`, `APROBADA` y `RECHAZADA`.

### 5.17. Solicitar constancia — `POST /constancias`

Solicitud:

```json
{
  "tipo": "Laboral",
  "institucion": "Banco de ejemplo",
  "motivo": "Trámite de crédito"
}
```

Respuesta `201` (cabecera `Location: /SistemaRRHH/api/constancias/8`): el mismo objeto del ejemplo 5.16, con `estado` en `PENDIENTE`.

Reglas: `tipo` solo acepta `Salarial` o `Laboral` (igual que la aplicación web); `institucion` es obligatoria; `motivo` es opcional. La empresa emisora la define el servidor según el empleado.

Errores: `400` por tipo inválido o `institucion` vacía.

### 5.18. Aprobar constancia — `POST /constancias/{id}/aprobar`

Solicitud (el cuerpo es opcional):

```json
{
  "observaciones": "Solicitud revisada."
}
```

Respuesta `204`. Errores: `404` si no existe; `409` si la constancia ya fue procesada.

### 5.19. Rechazar constancia — `POST /constancias/{id}/rechazar`

Solicitud:

```json
{
  "motivoRechazo": "Falta información necesaria.",
  "observaciones": "Debe presentar los datos completos."
}
```

Respuesta `204`. `motivoRechazo` es obligatorio (`400` si falta). Los demás errores son los mismos de aprobar.

### 5.20. PDF de constancia — `GET /constancias/{id}/pdf`

Sin cuerpo. Respuesta `200` con estas cabeceras:

```
Content-Type: application/pdf
Content-Disposition: inline; filename=constancia_8.pdf
```

Solo puede descargarla el empleado dueño o RRHH; para otro empleado responde `404`. Si la constancia no está `APROBADA` responde `409`. El PDF incluye el código QR de verificación.

## 6. Formato común de error

Todas las respuestas de error usan el mismo JSON:

```json
{
  "codigo": 400,
  "mensaje": "El identificador debe ser mayor que cero",
  "ruta": "/api/empleados/0",
  "fechaHora": "2026-10-27T18:30:00"
}
```

`codigo` es el estado HTTP, `mensaje` explica el problema en español, `ruta` identifica la petición (siempre empieza con `/api`) y `fechaHora` indica cuándo ocurrió. Los errores `500` nunca incluyen detalles técnicos.

Los errores de validación (`400`) incluyen además `campos`, con un texto por cada campo inválido:

```json
{
  "codigo": 400,
  "mensaje": "Hay campos inválidos.",
  "ruta": "/api/auth/login",
  "fechaHora": "2026-10-28T15:10:13",
  "campos": [
    "username: es obligatorio",
    "password: es obligatorio"
  ]
}
```

`campos` solo aparece en los errores de validación; en los demás no existe. Un cuerpo JSON mal formado también responde `400` con este formato y sin `campos`.

## 7. Reglas de autorización

- **Empleado:** consulta y gestiona solo sus solicitudes, incapacidades y constancias; puede cancelar sus solicitudes pendientes.
- **Jefatura:** además de lo propio, ve las solicitudes de los empleados de su departamento y puede aprobarlas o rechazarlas. No puede resolver las suyas.
- **RRHH:** además de lo propio, consulta empleados, solicitudes e incapacidades de todos, y aprueba o rechaza constancias.
- **No autenticado:** solo accede a `/ping` y `/auth/login`.
- Datos de otro empleado: `404`, aunque se conozca el identificador.

## 8. Pendientes de validación

- Decisión del grupo: token opaco o JWT (sección 2).
- Hecho en la Parte 2: login, logout, `401` sin token o con token vencido, `403` por rol, `400` con `campos`, JSON mal formado, CORS y registro de operaciones, probados manualmente.
- Confirmar que cada endpoint responde según este contrato cuando lo implementen las partes 3 y 4.
- Probar permisos con cuentas de los tres roles.
- Verificar que las fechas se serialicen con el formato documentado.
- Verificar la compatibilidad con las pantallas JSF existentes.