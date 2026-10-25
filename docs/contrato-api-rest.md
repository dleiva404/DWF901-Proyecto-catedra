# Contrato de API REST — SistemaRRHH

**Versión:** 1.0  
**Base URL:** `/api`  
**Formato:** JSON (`application/json`)  
**Fechas:** `yyyy-MM-dd`  
**Autenticación actual:** sesión HTTP de la aplicación web.

## 1. Convenciones generales

- Las rutas utilizan nombres en plural.
- Las solicitudes y respuestas usan JSON, salvo las respuestas sin contenido.
- Los identificadores deben ser enteros positivos.
- Los recursos no deben devolver directamente entidades JPA.
- Las contraseñas, rutas internas de archivos y tokens de verificación no deben exponerse en DTO públicos.
- Las operaciones protegidas requieren una sesión autenticada y el rol autorizado.
- Las fechas de tipo fecha se representan como `yyyy-MM-dd`. Los campos de fecha y hora deberán documentar expresamente su formato.

## 2. Códigos HTTP

| Código | Significado |
|---|---|
| 200 | Operación realizada correctamente |
| 201 | Recurso creado |
| 204 | Operación correcta sin contenido de respuesta |
| 400 | Solicitud o identificador inválido |
| 401 | Usuario no autenticado |
| 403 | Usuario sin permisos |
| 404 | Recurso inexistente o no accesible |
| 409 | Conflicto con el estado actual del recurso |
| 500 | Error interno inesperado |

## 3. Endpoints

| Método | Ruta | Rol autorizado | Solicitud | Respuesta esperada |
|---|---|---|---|---|
| GET | `/ping` | Público | Ninguna | 200: estado del servicio |
| GET | `/empleados` | RRHH | Ninguna | 200: lista de empleados |
| GET | `/empleados/{id}` | RRHH | ID en la ruta | 200: empleado |
| GET | `/solicitudes` | Empleado, Jefatura, RRHH | Ninguna | 200: lista filtrada según rol |
| GET | `/solicitudes/{id}` | Empleado, Jefatura, RRHH | ID en la ruta | 200: solicitud autorizada |
| DELETE | `/solicitudes/{id}` | Empleado | ID en la ruta | 204: solicitud cancelada |
| POST | `/solicitudes/{id}/aprobar` | Jefatura | ID en la ruta | 204: solicitud aprobada |
| POST | `/solicitudes/{id}/rechazar` | Jefatura | ID y motivo de rechazo | 204: solicitud rechazada |
| GET | `/incapacidades` | Empleado, RRHH | Ninguna | 200: lista autorizada |
| GET | `/incapacidades/{id}` | Empleado, RRHH | ID en la ruta | 200: incapacidad autorizada |
| GET | `/constancias` | Empleado, RRHH | Ninguna | 200: lista autorizada |
| GET | `/constancias/{id}` | Empleado, RRHH | ID en la ruta | 200: constancia autorizada |
| POST | `/constancias` | Empleado | Datos de la constancia | 201: constancia registrada |
| POST | `/constancias/{id}/aprobar` | RRHH | ID y observaciones opcionales | 204: constancia aprobada |
| POST | `/constancias/{id}/rechazar` | RRHH | ID, motivo y observaciones opcionales | 204: constancia rechazada |

> La tabla representa el contrato previsto según los recursos implementados. Debe contrastarse con las anotaciones y firmas definitivas del código antes de considerarla una especificación cerrada.

## 4. Ejemplos JSON

### 4.1. Ping

Respuesta `200 OK`:

```json
{
  "status": "ok"
}
```

### 4.2. Solicitud de rechazo

`POST /api/solicitudes/15/rechazar`

```json
{
  "motivoRechazo": "Las fechas solicitadas requieren corrección."
}
```

### 4.3. Registro de constancia

`POST /api/constancias`

```json
{
  "tipo": "LABORAL",
  "institucion": "Institución de ejemplo",
  "motivo": "Trámite personal"
}
```

### 4.4. Aprobación de constancia

`POST /api/constancias/8/aprobar`

```json
{
  "observaciones": "Solicitud revisada."
}
```

### 4.5. Rechazo de constancia

`POST /api/constancias/8/rechazar`

```json
{
  "motivoRechazo": "Falta información necesaria.",
  "observaciones": "Debe presentar los datos completos."
}
```

## 5. Formato común de error

Las respuestas de error deberán utilizar un único formato JSON:

```json
{
  "codigo": 400,
  "mensaje": "El identificador debe ser mayor que cero.",
  "ruta": "/api/empleados/0",
  "fechaHora": "2026-10-25T15:30:00"
}
```

El campo `codigo` contiene el estado HTTP, `mensaje` explica el problema, `ruta` identifica la solicitud y `fechaHora` indica cuándo se produjo el error.

## 6. Reglas de autorización

- **Empleado:** consulta únicamente sus solicitudes, incapacidades y constancias; puede cancelar sus solicitudes pendientes.
- **Jefatura:** consulta las solicitudes que le corresponden y puede aprobarlas o rechazarlas conforme a las reglas del negocio.
- **RRHH:** consulta empleados, solicitudes dentro de su ámbito, incapacidades y constancias; puede aprobar o rechazar constancias.
- **No autenticado:** no accede a recursos protegidos.
- **Datos de otro empleado:** no deben revelarse aunque se conozca el identificador.

## 7. Pendientes de validación

- Confirmar que cada endpoint compila y responde según el código.
- Unificar el tratamiento de errores para que todos los errores tengan el mismo JSON.
- Verificar que las fechas se serialicen con el formato documentado.
- Probar permisos con cuentas de los tres roles.
- Confirmar que los endpoints de incapacidad y constancia no exponen datos sensibles.
- Verificar la compatibilidad con las pantallas JSF existentes.
- Definir autenticación apropiada para futuros clientes móviles; la sesión web actual no sustituye un mecanismo de autenticación independiente.
