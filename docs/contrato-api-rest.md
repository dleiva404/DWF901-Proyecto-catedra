
# Contrato de API REST - SistemaRRHH

## 1. Convenciones generales

- Prefijo de todas las rutas: `/api`.
- Formato de intercambio: JSON.
- Solicitudes con cuerpo JSON: `Content-Type: application/json`.
- Respuestas JSON: `Accept: application/json`.
- Fechas: `yyyy-MM-dd`.
- Fechas con hora: `yyyy-MM-dd'T'HH:mm:ss`.
- Las rutas de colecciones se expresan en plural.
- No se devuelven entidades JPA directamente.
- No se exponen contraseñas ni rutas internas de documentos.

## 2. Formato de errores

Todas las respuestas de error deben seguir esta estructura:

    {
      "codigo": 404,
      "mensaje": "Recurso no encontrado",
      "ruta": "/api/empleados/999",
      "fechaHora": "2026-10-25T10:00:00"
    }

## 3. Códigos HTTP

| Código | Significado |
|---|---|
| 200 | Operación correcta |
| 201 | Recurso creado |
| 204 | Operación correcta sin contenido |
| 400 | Solicitud incorrecta o datos inválidos |
| 401 | Autenticación requerida |
| 403 | Sin permisos |
| 404 | Recurso no encontrado |
| 409 | Conflicto con el estado del recurso |

Los errores inesperados podrán devolver 500.

## 4. Endpoints

### GET /api/ping

- Acceso: público.
- Cuerpo de solicitud: ninguno.
- Respuesta 200:

  {
  "status": "ok"
  }

### GET /api/empleados

- Acceso propuesto: RRHH (por confirmar).
- Cuerpo de solicitud: ninguno.
- Respuesta 200: lista de EmpleadoDTO.
- Errores posibles: 401, 403, 500.

### GET /api/empleados/{id}

- Acceso propuesto: RRHH (por confirmar).
- Cuerpo de solicitud: ninguno.
- Respuesta 200: EmpleadoDTO.
- Errores posibles: 400, 401, 403, 404, 500.

### GET /api/solicitudes

- Acceso propuesto: usuario autenticado.
- Cuerpo de solicitud: ninguno.
- Respuesta 200: lista de SolicitudDTO.
- Regla pendiente: definir si cada empleado solo puede consultar sus solicitudes.
- Errores posibles: 401, 403, 500.

### POST /api/solicitudes

- Acceso propuesto: empleado autenticado.
- Cuerpo de ejemplo:

  {
  "tipoSolicitudId": 1,
  "fechaInicio": "2026-11-02",
  "fechaFin": "2026-11-03",
  "motivo": "Trámite personal"
  }

- Respuesta 201: SolicitudDTO creada.
- Errores posibles: 400, 401, 403, 409, 500.

### GET /api/incapacidades

- Acceso propuesto: usuario autenticado, sujeto a autorización por registro.
- Cuerpo de solicitud: ninguno.
- Respuesta 200: lista de IncapacidadDTO.
- Errores posibles: 401, 403, 500.

### POST /api/incapacidades

- Acceso propuesto: usuario autorizado.
- Cuerpo de ejemplo:

  {
  "solicitudId": 15,
  "numeroDocumento": "INC-2026-001",
  "fechaInicio": "2026-10-20",
  "fechaFin": "2026-10-22",
  "documentoNombre": "incapacidad.pdf",
  "observaciones": "Documento recibido"
  }

- Respuesta 201: IncapacidadDTO creada.
- Errores posibles: 400, 401, 403, 409, 500.

### GET /api/constancias

- Acceso propuesto: usuario autenticado, sujeto a autorización por registro.
- Cuerpo de solicitud: ninguno.
- Respuesta 200: lista de ConstanciaDTO.
- Errores posibles: 401, 403, 500.

### POST /api/constancias

- Acceso propuesto: usuario autenticado.
- Cuerpo de ejemplo:

  {
  "tipo": "LABORAL",
  "institucion": "Institución solicitante",
  "motivo": "Trámite administrativo"
  }

- Respuesta 201: ConstanciaDTO creada.
- Errores posibles: 400, 401, 403, 409, 500.

## 5. Estado de implementación

- GET /api/ping: implementación inicial.
- DTO y mapeadores: estructura inicial.
- Manejo común de errores: estructura inicial.
- Filtro de roles: estructura inicial.
- Operaciones de empleados, solicitudes, incapacidades y constancias: pendientes.
- Roles definitivos y reglas de acceso por registro: pendientes de validar con el proyecto y la base de datos.


### GET /api/empleados

- Acceso: rol RRHH configurado en `EmpleadoResource`.
- Cuerpo de solicitud: ninguno.
- Respuesta 200: lista JSON de EmpleadoDTO.
- Errores: 401, 403, 500.

### GET /api/empleados/{id}

- Acceso: rol RRHH configurado en `EmpleadoResource`.
- Cuerpo de solicitud: ninguno.
- Respuesta 200: EmpleadoDTO.
- Errores: 400, 401, 403, 404, 500.
