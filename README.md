# Sistema de Gestión de Recursos Humanos

### Proyecto de Cátedra DWF901 | Universidad Don Bosco

Aplicación web para la gestión de solicitudes de Recursos Humanos, desarrollada como parte de la asignatura **Desarrollo de Aplicaciones Web con Framework (DWF901)** de la Universidad Don Bosco.

El proyecto integra una aplicación Java con **JSF**, una **API REST** implementada con Jersey y Jackson y un **cliente consumidor independiente** desarrollado con React y Vite. Permite gestionar solicitudes, vacaciones, constancias e incapacidades, con autenticación y autorización según el perfil del usuario.

## Tecnologías

| Componente | Tecnología |
| --- | --- |
| Backend y aplicación web | Java, JSF 2.2 y Servlets |
| API REST | JAX-RS con Jersey 2.41 |
| Serialización de datos | Jackson (JSON) |
| Acceso a datos | JPA, Hibernate y MySQL |
| Base de datos | MySQL 8 |
| Cliente consumidor | React y Vite |
| Servidor de aplicaciones | Apache Tomcat 9 |
| Gestión de dependencias | Maven (proyecto WAR) y npm (cliente) |
| Autenticación | Tokens Bearer opacos y autorización por rol |
| Pruebas y documentación | Postman, matriz Excel y contrato REST |
| Control de versiones | Git y GitHub |

La API y el cliente consumidor funcionan como componentes separados: el backend se despliega en Tomcat y el cliente se ejecuta mediante Vite.

## Funcionalidades

### Autenticación y control de acceso

- Inicio y cierre de sesión mediante la API REST.
- Emisión y validación de un token Bearer para las operaciones protegidas.
- Restricción de recursos y operaciones según el rol del usuario.
- Manejo de respuestas HTTP ante ausencia de token, token inválido o permisos insuficientes.

### Solicitudes de Recursos Humanos

- Registro de solicitudes de vacaciones y permisos.
- Consulta de solicitudes propias, filtros e historial.
- Consulta del saldo de vacaciones.
- Cancelación de solicitudes pendientes, según las reglas del contrato REST.
- Validación de datos y estados de las solicitudes.

### Jefatura y aprobaciones

- Consulta de solicitudes correspondientes al perfil de Jefatura.
- Aprobación o rechazo de solicitudes.
- Registro de motivos de rechazo y validación de permisos.

### Constancias e incapacidades

- Solicitud, consulta y seguimiento de constancias.
- Aprobación o rechazo de constancias por Recursos Humanos.
- Descarga de constancias en PDF, cuando corresponde.
- Registro y consulta de incapacidades conforme a las operaciones autorizadas.

### Cliente consumidor REST

- Pantallas para iniciar sesión y utilizar los módulos habilitados según el rol.
- Consumo de servicios REST desde una aplicación React independiente.
- Presentación de estados de carga, validaciones y mensajes de respuesta de la API.

## Roles y permisos

El script de base de datos contempla los roles `ADMIN`, `EMPLEADO`, `JEFATURA` y `RRHH`. La integración REST documentada para el Sprint III se centra en los siguientes perfiles:

| Rol | Funciones principales en el cliente y la API |
| --- | --- |
| Empleado | Iniciar sesión, registrar y consultar solicitudes propias, revisar vacaciones, solicitar constancias y registrar incapacidades. |
| Jefatura | Consultar solicitudes del ámbito autorizado y aprobarlas o rechazarlas. |
| Recursos Humanos (RRHH) | Consultar información autorizada de empleados, constancias e incapacidades y gestionar constancias. |

El rol `ADMIN` está definido en los datos iniciales; no se presenta aquí como un módulo funcional del cliente consumidor del Sprint III. La autorización efectiva siempre debe comprobarse en el servidor.

## Instalación y ejecución

### Requisitos previos

- JDK que permita compilar el proyecto con nivel de lenguaje **Java 8** (configurado en `pom.xml`).
- **Apache Maven**.
- **Apache Tomcat 9**.
- **MySQL 8** y MySQL Workbench u otro cliente compatible.
- **Node.js y npm** para el cliente React (el README de `cliente/` utiliza Node.js 24).
- **Git** y un navegador actualizado.
- **IntelliJ IDEA** u otro IDE compatible (opcional).
- **Postman** para importar las solicitudes REST (opcional).

### 1. Clonar el repositorio

```bash
git clone https://github.com/dleiva404/DWF901-Proyecto-catedra.git
cd DWF901-Proyecto-catedra
```

### 2. Preparar la base de datos

El archivo [`sistema_permisos.sql`](sistema_permisos.sql), ubicado en la raíz del repositorio, contiene el esquema y los datos iniciales de la base de datos **`sistema_permisos`**.

**Advertencia:** el script comienza con `DROP DATABASE IF EXISTS sistema_permisos;`. **Ejecutarlo elimina la base existente y sus registros antes de recrearla.** Utilizarlo únicamente en una instalación de pruebas nueva o después de realizar y comprobar un respaldo. No ejecutarlo sobre una base con datos que deban conservarse.

Para preparar un entorno de pruebas nuevo:

1. Iniciar el servicio de MySQL.
2. Abrir `sistema_permisos.sql` en MySQL Workbench o en un cliente compatible.
3. Confirmar que la instancia y la base de destino son las correctas.
4. Ejecutar el script únicamente si es seguro recrear `sistema_permisos`.
5. Verificar que las tablas y los registros iniciales se hayan creado.

### 3. Configurar la conexión local

Revisar `src/main/resources/db.properties` y adaptar estos valores **solo en el entorno local**:

```properties
db.url=jdbc:mysql://localhost:3306/sistema_permisos
db.user=USUARIO_LOCAL
db.password=CLAVE_LOCAL
```

Los valores mostrados son ejemplos, **no credenciales reales**. Evitar publicar contraseñas o sobrescribir la configuración local de otros integrantes. La configuración general de la API (incluidos los orígenes CORS y la duración de los tokens) está en `src/main/resources/api.properties`.

### 4. Compilar y ejecutar la API REST

Abrir el repositorio como proyecto Maven en IntelliJ IDEA o ejecutar, desde su carpeta raíz:

```bash
mvn clean package
```

Desplegar el WAR resultante en **Tomcat 9** usando el contexto `/SistemaRRHH`. En IntelliJ IDEA puede utilizarse la configuración de Tomcat o SmartTomcat definida para el proyecto.

Con Tomcat iniciado, verificar el servicio desde un navegador o Postman:

```text
http://localhost:8080/SistemaRRHH/api/ping
```

La URL base de la API es:

```text
http://localhost:8080/SistemaRRHH/api
```

Si cambia el puerto o contexto de Tomcat, ajustar también las URL del cliente y de Postman.

### 5. Ejecutar el cliente consumidor

Con la API y MySQL activos, abrir **otra terminal** desde la raíz del repositorio y ejecutar:

```bash
cd cliente
npm ci
npm run dev
```

Abrir **http://localhost:5500** en el navegador. La dirección de la API utilizada por React se define en `cliente/src/config.js` y los orígenes permitidos se configuran en `src/main/resources/api.properties`.

Para información específica del frontend, consultar el [README del cliente](cliente/README.md).

## Usuarios y credenciales de prueba

El archivo SQL incluye cuentas iniciales con contraseñas almacenadas mediante **hash**, utilizadas durante las pruebas del proyecto.

| Perfil de prueba | Usuario |
| --- | --- |
| Empleado | `juan` |
| Jefatura | `carlos` |
| Recursos Humanos | `ana` |

**Las contraseñas de las cuentas de prueba se obtienen del equipo responsable por un canal privado.** No se publican contraseñas, tokens activos ni valores locales de conexión en el README. Los usuarios disponibles pueden variar si la base de pruebas se ha modificado.

## Estructura del proyecto

```text
DWF901-Proyecto-catedra/
├── cliente/
│   ├── src/
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
├── docs/
│   ├── contrato-api-rest.md
│   ├── endpoints-empleado.md
│   ├── DWF901_SprintIII_Postman_Limpio_Entrega.json
│   ├── DWF901 Matriz de Pruebas.xlsx
│   ├── prueba-api.ps1
│   └── prueba-constancias.ps1
├── src/
│   └── main/
│       ├── java/
│       ├── resources/
│       └── webapp/
├── pom.xml
├── sistema_permisos.sql
└── README.md
```

- **`src/main/java/`:** código Java, lógica del servidor y recursos REST.
- **`src/main/resources/`:** configuración de conexión, persistencia y API.
- **`src/main/webapp/`:** vistas y recursos de la aplicación JSF, además de la configuración web.
- **`cliente/`:** cliente consumidor React + Vite, independiente del WAR.
- **`docs/`:** contrato de API, pruebas, matriz, colección Postman y scripts de comprobación.

## API REST

El servidor publica los recursos bajo el prefijo `/SistemaRRHH/api`.

| Ruta o grupo de rutas | Responsabilidad |
| --- | --- |
| `/ping` | Comprobar la disponibilidad del servicio. |
| `/auth/login`, `/auth/logout` | Iniciar y cerrar sesión. |
| `/tipos-solicitud` | Consultar el catálogo de tipos de solicitud. |
| `/vacaciones/saldo` | Consultar los días de vacaciones. |
| `/solicitudes` | Registrar y consultar solicitudes. |
| `/solicitudes/{id}` | Consultar o cancelar solicitudes, según operación y permisos. |
| `/solicitudes/{id}/aprobar`, `/solicitudes/{id}/rechazar` | Resolver solicitudes desde Jefatura. |
| `/empleados`, `/empleados/{id}` | Consultar empleados según permisos. |
| `/incapacidades`, `/incapacidades/{id}` | Registrar o consultar incapacidades. |
| `/constancias`, `/constancias/{id}` | Solicitar y consultar constancias. |
| `/constancias/{id}/aprobar`, `/constancias/{id}/rechazar` | Gestionar constancias desde RRHH. |
| `/constancias/{id}/pdf` | Descargar una constancia en PDF autorizada. |

Los detalles de métodos HTTP, parámetros, validaciones, códigos de respuesta y autorización se encuentran en el [contrato REST v1.3](docs/contrato-api-rest.md).

**Nota de seguridad:** a diferencia de un JWT, el token Bearer de esta implementación es **opaco** y se conserva en memoria del servidor. Su duración configurada es de 30 minutos; al reiniciar el servidor, los tokens emitidos dejan de ser válidos.

## Validaciones y pruebas

Durante el Sprint III se documentaron casos funcionales, negativos, de límite y de seguridad mediante Postman y una matriz de resultados.

Documentación disponible:

- [Colección Postman — Sprint III](docs/DWF901_SprintIII_Postman_Limpio_Entrega.json).
- [Matriz de pruebas REST — Excel](docs/DWF901%20Matriz%20de%20Pruebas.xlsx).
- [Contrato de la API REST](docs/contrato-api-rest.md).
- [Documentación de endpoints de Empleado](docs/endpoints-empleado.md).
- Scripts de comprobación: [`prueba-api.ps1`](docs/prueba-api.ps1) y [`prueba-constancias.ps1`](docs/prueba-constancias.ps1).

Para importar la colección en Postman:

1. Seleccionar **Import** y elegir el archivo JSON.
2. Revisar la variable `baseUrl` y configurar **localmente** los usuarios y contraseñas de prueba autorizados.
3. Ejecutar el login correspondiente al perfil de Empleado, Jefatura o RRHH para generar los tokens.
4. Ejecutar las solicitudes y contrastar las respuestas con la matriz y el contrato.

## Consideraciones técnicas

- **Protección de datos:** no incorporar a GitHub contraseñas, tokens reales, respaldos locales o datos personales de prueba que no deban publicarse.
- **Base de datos:** la ejecución de `sistema_permisos.sql` es destructiva respecto de una instalación existente; realizar un respaldo antes de recrearla.
- **Separación de componentes:** el backend necesita Tomcat y MySQL; el cliente React se ejecuta por separado.
- **Alcance de la entrega:** este repositorio contiene el proyecto académico y la documentación del Sprint III. No debe considerarse una configuración lista para producción sin revisiones adicionales de seguridad y operación.

## Información académica

**Institución:** Universidad Don Bosco  
**Asignatura:** Desarrollo de Aplicaciones Web con Framework (DWF901)  
**Proyecto:** Sistema de Gestión de Recursos Humanos  
**Etapa:** Sprint III — API REST y cliente consumidor  
**Año:** 2026

---

Proyecto de cátedra desarrollado en equipo por estudiantes de la Universidad Don Bosco.
