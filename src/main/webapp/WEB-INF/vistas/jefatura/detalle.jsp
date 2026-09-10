<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Detalle de solicitud</title>
</head>

<body>

<h1>Detalle de la solicitud</h1>

<c:if test="${not empty error}">
    <p>${error}</p>
</c:if>

<c:if test="${not empty solicitud}">

    <p>
        <strong>ID:</strong>
        ${solicitud.idSolicitud}
    </p>

    <p>
        <strong>ID empleado:</strong>
        ${solicitud.idEmpleado}
    </p>

    <p>
        <strong>Tipo de solicitud:</strong>
        ${solicitud.idTipoSolicitud}
    </p>

    <p>
        <strong>Fecha de inicio:</strong>
        ${solicitud.fechaInicio}
    </p>

    <p>
        <strong>Fecha de fin:</strong>
        ${solicitud.fechaFin}
    </p>

    <p>
        <strong>Días solicitados:</strong>
        ${solicitud.diasSolicitados}
    </p>

    <p>
        <strong>Motivo:</strong>
        ${solicitud.motivo}
    </p>

    <p>
        <strong>Estado:</strong>
        ${solicitud.estado}
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

    <br>

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

        <br>

        <textarea id="motivoRechazo"
                  name="motivoRechazo"
                  required
                  minlength="5"
                  maxlength="250"
                  rows="4"
                  cols="50"></textarea>

        <br>

        <button type="submit">
            Rechazar solicitud
        </button>

    </form>

</c:if>

<br>

<a href="${pageContext.request.contextPath}/jefatura">
    Volver a solicitudes pendientes
</a>

</body>

</html>