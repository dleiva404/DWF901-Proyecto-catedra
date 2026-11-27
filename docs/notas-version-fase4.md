# Notas de versión — Fase 4

## José González

- Se añadieron notificaciones por correo para solicitudes aprobadas, rechazadas y pendientes de revisión, además de constancias aprobadas o rechazadas.
- Se agregó el cambio autenticado de contraseña y la recuperación pública por código de un solo uso, con vigencia de 30 minutos.
- Se incorporaron configuración SMTP local ignorada por Git, almacenamiento de hashes de recuperación y reglas de acceso para las nuevas rutas.

## David Leiva

- El contenedor Spring, Spring Data JPA y Spring Security de la API ya están integrados en la base de `main`; las rutas protegidas mantienen sus reglas centralizadas en `SecurityConfig`.
