<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Detalle de solicitud</title>

    <link rel="preconnect" href="https://googleapis.com">
    <link rel="preconnect" href="https://gstatic.com" crossorigin>
    <link href="https://googleapis.com/css2?family=Space+Grotesk:wght=600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body>

    <header class="app-header" style="position: absolute; top: 0; left: 0; width: 100%; box-sizing: border-box;">
        <div class="app-header-logo-chip">
            <img src="${pageContext.request.contextPath}/img/logo.png" alt="Invercalma">
        </div>
        <div class="app-header-usuario">
            <span>
                <c:out value="${sessionScope.empleado.nombre} ${sessionScope.empleado.apellido}"/>
                &middot; <c:out value="${sessionScope.rol.nombre}"/>
            </span>

            &middot;
            <a class="app-header-salir" href="${pageContext.request.contextPath}/logout">Cerrar sesión</a>
        </div>
    </header>

    <h1 style="max-width: 1200px; margin: 120px auto 0 auto; padding: 0 20px; font-family: 'Space Grotesk', sans-serif;">
        Detalle de la solicitud
    </h1>

<div class="contenedor" style="margin-top: 40px !important;">

    <main class="app-contenido">

        <c:if test="${not empty error}">
            <p class="mensaje-error">
                <c:out value="${error}"/>
            </p>
        </c:if>

        <c:if test="${not empty solicitud}">

            <p>
                <strong>ID:</strong>
                <c:out value="${solicitud.idSolicitud}"/>
            </p>

            <p>
                <strong>ID empleado:</strong>
                <c:out value="${solicitud.idEmpleado}"/>
            </p>

            <p>
                <strong>Tipo de solicitud:</strong>
                <c:out value="${solicitud.idTipoSolicitud}"/>
            </p>

            <p>
                <strong>Fecha de inicio:</strong>
                <c:out value="${solicitud.fechaInicio}"/>
            </p>

            <p>
                <strong>Fecha de fin:</strong>
                <c:out value="${solicitud.fechaFin}"/>
            </p>

            <p>
                <strong>Días solicitados:</strong>
                <c:out value="${solicitud.diasSolicitados}"/>
            </p>

            <p>
                <strong>Motivo:</strong>
                <c:out value="${solicitud.motivo}"/>
            </p>

            <p>
                <strong>Estado:</strong>
                <span class="estado-pendiente">
                    <c:out value="${solicitud.estado}"/>
                </span>
            </p>

            <hr>

            <h2>Acciones de jefatura</h2>

            <form action="${pageContext.request.contextPath}/jefatura"
                  method="post">

                <input type="hidden"
                       name="accion"
                       value="aprobar">

                <input type="hidden"
                       name="idSolicitud"
                       value="${solicitud.idSolicitud}">

                <button type="submit">
                    Aprobar solicitud
                </button>

            </form>

            <form action="${pageContext.request.contextPath}/jefatura"
                  method="post">

                <input type="hidden"
                       name="accion"
                       value="rechazar">

                <input type="hidden"
                       name="idSolicitud"
                       value="${solicitud.idSolicitud}">

                <label for="motivoRechazo">
                    Motivo del rechazo:
                </label>

                <textarea id="motivoRechazo"
                          name="motivoRechazo"
                          required
                          minlength="5"
                          maxlength="250"
                          rows="4"
                          placeholder="Explique el motivo del rechazo"></textarea>

                <button type="submit">
                    Rechazar solicitud
                </button>

            </form>

        </c:if>

        <br>

        <a href="${pageContext.request.contextPath}/jefatura">
            Volver a solicitudes pendientes
        </a>

    </main>

</div>

</body>

</html>
