<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Detalle de solicitud</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body>

<div class="contenedor">

    <h1>Detalle de la solicitud</h1>

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

</div>

</body>

</html>