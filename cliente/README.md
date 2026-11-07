# Cliente consumidor REST — DWF901 / Sprint III

Este cliente React + Vite funciona **fuera del WAR Java** y consume la API REST implementada por el equipo. Solo el contenido de `cliente/` pertenece a este desarrollo de frontend.

## Requisitos

- Node.js 24 LTS y npm
- API REST desplegada en Tomcat y MySQL funcionando **para pruebas de integración**

## Ejecución

Desde la raíz del repositorio:

```powershell
cd cliente
npm ci
npm run dev
```

Abrir `http://localhost:5500`. El puerto está definido en `vite.config.js` y debe estar permitido por el servidor en CORS.

## Configuración

El archivo `src/config.js` define `API_BASE_URL`, actualmente `http://localhost:8080/SistemaRRHH/api`. Ajustarlo solo si el despliegue real utiliza otro puerto o contexto. No hay contraseñas en el frontend.

## Organización del código

- `src/ClienteRRHH.jsx`: coordina la sesión y la navegación.
- `src/pages/`: pantallas de acceso, solicitudes, jefatura, constancias, gestión de constancias por RRHH e incapacidad.
- `src/components/`: elementos compartidos (menú, avisos, estados e íconos).
- `src/services/`: comunicación con los recursos REST y manejo de la sesión.
- `src/utils/formato.js`: formatos de fechas, estados y mensajes.
- `src/config.js`: dirección base de la API.
- `src/login.css`, `src/plantilla.css` y `src/cliente.css`: estilos del cliente; la interfaz original bajo `src/main/webapp/` permanece intacta.

## Funcionalidades de este cliente

- Login con token Bearer guardado en `sessionStorage`, logout y cierre ante 401.
- Empleado: solicitudes propias, filtros, saldo de vacaciones, nuevas solicitudes, cancelar pendientes y registrar incapacidad.
- Jefatura: solicitudes pendientes del departamento, aprobar y rechazar con motivo.
- Constancias: solicitar, consultar y descargar PDF autenticado.
- Recursos Humanos: consultar las constancias de todos los empleados, filtrar por estado, revisar detalles, aprobar o rechazar con motivo.
- Estados de carga, validaciones básicas y mensajes de errores de la API.
- Diseño basado en `src/main/webapp/login.xhtml`, `css/login.css`, `css/plantilla.css` y `WEB-INF/templates/template.xhtml` del proyecto original.

## Dependencias del backend (contrato REST v1.3)

Para que todas las pantallas funcionen, el equipo debe haber integrado `/auth/login`, `/auth/logout`, `/tipos-solicitud`, `/vacaciones/saldo`, `/solicitudes`, `/solicitudes/{id}/aprobar`, `/solicitudes/{id}/rechazar`, `/incapacidades`, `/constancias`, `/constancias/{id}/aprobar`, `/constancias/{id}/rechazar`, `/constancias/{id}/pdf` y `/empleados` (solo RRHH).

**Pendiente:** el contrato v1.3 únicamente permite `DELETE /solicitudes/{id}` si la solicitud está `PENDIENTE`. No define aún una operación para *pedir cancelación* de una solicitud ya resuelta. El cliente lo indica sin simular una acción.

**Comprobado en integración:** `GET /constancias?propias=true` está implementado; como medida adicional, la pantalla filtra los resultados del usuario en el frontend, pero el servidor siempre impone la autorización real.

Las reglas de negocio, seguridad del token, generación del PDF, endpoints y base de datos son parte del backend del equipo, no del frontend.

## Demostración acordada

1. Iniciar sesión como empleado y crear una solicitud válida.
2. Entrar como jefatura, aprobarla, y volver a empleado para ver el estado aprobado.
3. Mostrar un error de negocio de la API (p. ej. un rango que incluya solo domingo); el cliente muestra su mensaje.

## Seguridad y control de versiones

`npm audit` para revisar dependencias; no subir `node_modules`, configuraciones locales ni credenciales.
