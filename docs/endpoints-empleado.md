# Parte 3 — Endpoints del empleado

Implementa, siguiendo el **contrato v1.2** (`docs/contrato-api-rest.md`) y la base de
las partes 1 y 2 (token Bearer, `@RequiresRole`, `ApiException`, `ValidadorApi`):

| # del contrato | Endpoint | Clase | Éxito | Errores |
|---|---|---|---|---|
| 4 | `GET /tipos-solicitud` | `TipoSolicitudResource` | 200 | 401 |
| 5 | `GET /vacaciones/saldo?anio=` | `VacacionesResource` | 200 | 400, 401, 404 |
| 8 | `GET /solicitudes` (parte del empleado) | `SolicitudResource` | 200 | 400, 401 |
| 9 | `GET /solicitudes/{id}` | `SolicitudResource` | 200 | 400, 401, 404 |
| 10 | `POST /solicitudes` | `SolicitudResource` | 201 + `Location` | 400, 401, 409 |
| 11 | `DELETE /solicitudes/{id}` | `SolicitudResource` | 204 | 400, 401, 404, 409 |
| 17 | `POST /incapacidades` | `IncapacidadResource` | 201 + `Location` | 400, 401 |

Los recursos son delgados: validan la forma del JSON, toman al usuario del
`SecurityContext` (`ApiPrincipal`, nunca de la sesión JSF) y traducen excepciones. La
lógica está en `SolicitudEmpleadoService` e `IncapacidadEmpleadoService`
(paquete `com.permisos.service`), que no dependen de JAX-RS.

## Cómo salen los códigos de error

`ApiExceptionMapper` convierte **cualquier** `ReglaNegocioException` en 409. Eso daría un
código equivocado para los datos inválidos (400) y los recursos inexistentes (404), por
eso los recursos pasan por `TraductorErroresServicio`:

| Excepción de servicio | HTTP |
|---|---|
| `ValidacionException` (lleva la lista `campo: problema`) | 400, con `campos` |
| `RecursoNoEncontradoException` | 404 |
| `ConflictoException` | 409 |
| `ReglaNegocioException` genérica (p. ej. saldo insuficiente) | 409 |
| `SQLException` | 500 |

Alternativa para David: agregar esos mismos casos al inicio de la rama de
`ReglaNegocioException` en `ApiExceptionMapper`; así ningún recurso (de la parte 3 ni
de la 4) necesitaría el `try/catch`.

## Diferencias entre la especificación del punto 3 y el contrato (decidir con David)

El contrato manda; esto es lo que se hizo y lo que hay que confirmar.

| La especificación decía | El contrato dice | Qué se hizo |
|---|---|---|
| `GET /solicitudes/mias` | `GET /solicitudes` (el empleado ve solo lo propio) | Se siguió el **contrato**. Si David quiere también `/mias`, es un método de 3 líneas |
| `GET /empleados/me/vacaciones` | `GET /vacaciones/saldo` | Se siguió el **contrato** |
| Saldo con tomados, **pendientes** y disponibles | Solo asignados, utilizados y disponibles | Se agregó `diasPendientes`. El **contrato debe actualizarse** |
| 403 o 404 para una solicitud ajena | 404 | **404** |
| "Cancelar o pedir cancelación" | Solo `DELETE`, solo si está PENDIENTE | Solo lo del contrato. Una solicitud ya aprobada responde 409 |
| (no indicaba el límite del motivo) | Motivo de 5 a 250 caracteres | 5 a 250. El formulario web permite 500: es una diferencia del equipo, no de la API |

## Pendiente de otras partes

- **"Pedir la cancelación" de una solicitud aprobada**: nadie definió dónde se guarda la
  petición ni quién la responde (probablemente cambia el SQL). Punto de extensión:
  `SolicitudEmpleadoService.cancelar`.
- **Parte 4 (jefatura y RRHH)**: `GET /solicitudes` y `GET /solicitudes/{id}` hoy devuelven
  solo lo propio para cualquier rol. Los filtros `propias`, `empleadoId` y
  `departamentoId` del contrato, y la vista de jefatura/RRHH, van en esos mismos
  métodos. `SolicitudResource` e `IncapacidadResource` tienen comentarios que lo indican.
  No he podido comprobar qué hace Jersey si dos clases distintas usan el mismo `@Path`;
  lo seguro es agregar los métodos de la parte 4 en estas mismas clases.
- **Adjuntar el documento de una incapacidad**: la API recibe solo datos en JSON.

## Cómo probarlo a mano (PowerShell, servidor corriendo)

```powershell
$base = "http://localhost:8080/SistemaRRHH/api"
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" `
         -Body '{"username":"juan","password":"123456"}'
$h = @{ Authorization = "Bearer $($login.token)" }

Invoke-RestMethod -Uri "$base/tipos-solicitud" -Headers $h
Invoke-RestMethod -Uri "$base/vacaciones/saldo" -Headers $h
Invoke-RestMethod -Method Post -Uri "$base/solicitudes" -Headers $h -ContentType "application/json" `
         -Body '{"tipoSolicitudId":1,"fechaInicio":"2026-11-09","fechaFin":"2026-11-11","motivo":"Prueba de API"}'
```

Usa fechas **futuras**. Para ver el código y el cuerpo de un error (Invoke-RestMethod lanza
excepción en 4xx):

```powershell
try { Invoke-RestMethod -Uri "$base/vacaciones/saldo?anio=26" -Headers $h }
catch { $_.Exception.Response.StatusCode.value__; $_.ErrorDetails.Message }
```

## Casos de prueba por endpoint (para la parte 6)

Con `juan` / `123456`, base recién importada (15 días de vacaciones). **Estos casos no se
ejecutaron contra un servidor**; los de la columna "Automatizada" sí tienen una prueba
unitaria que pasa.

| Endpoint | Caso exitoso | Caso de error | Automatizada |
|---|---|---|---|
| `GET /tipos-solicitud` | con token → 200, 3 tipos | sin token → 401 | no |
| `GET /vacaciones/saldo` | sin parámetro → 200, año actual | `?anio=26` → 400 `anio: debe ser un número de cuatro dígitos` | el 400 sí |
| `GET /solicitudes` | → 200, lista de las propias | `?estado=ARCHIVADA` → 400 con `campos` | el 400 sí |
| `GET /solicitudes/{id}` | id propio → 200 | id de otro empleado → 404 | no |
| `POST /solicitudes` | vacaciones de 3 días → 201 y `Location` | sin cuerpo o campos vacíos → 400; 20 días con 15 de saldo → 409 | el 400 sí |
| `DELETE /solicitudes/{id}` | pendiente propia → 204 | la misma otra vez → 409; de otro → 404 | no |
| `POST /incapacidades` | datos válidos → 201 y `Location` | sin `numeroDocumento` → 400 | el 400 sí |

Además, `mvn test -Dtest=SolicitudEmpleadoServiceIT` ejecuta contra la base real el
recorrido completo de los servicios (crear, saldo, listar, detalle ajeno, cancelar dos
veces, incapacidad). Escribe datos: reimporta el SQL después.
