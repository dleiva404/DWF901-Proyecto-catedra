<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Solicitudes pendientes - Jefatura</title>
</head>

<body>

<h1>Solicitudes pendientes de jefatura</h1>

<c:if test="${param.resultado eq 'aprobada'}">
    <p>La solicitud fue aprobada correctamente.</p>
</c:if>

<c:if test="${param.resultado eq 'rechazada'}">
    <p>La solicitud fue rechazada correctamente.</p>
</c:if>

<c:if test="${not empty error}">
    <p>${error}</p>
</c:if>

<c:choose>

    <c:when test="${empty solicitudes}">
        <p>No hay solicitudes pendientes para revisar.</p>
    </c:when>

    <c:otherwise>

        <table border="1">

            <thead>
            <tr>
                <th>ID</th>
                <th>Empleado</th>
                <th>Tipo de solicitud</th>
                <th>Fecha inicio</th>
                <th>Fecha fin</th>
                <th>Días solicitados</th>
                <th>Motivo</th>
                <th>Estado</th>
                <th>Acción</th>
            </tr>
            </thead>

            <tbody>

            <c:forEach var="solicitud" items="${solicitudes}">

                <tr>

                    <td>${solicitud.idSolicitud}</td>

                    <td>${solicitud.idEmpleado}</td>

                    <td>${solicitud.idTipoSolicitud}</td>

                    <td>${solicitud.fechaInicio}</td>

                    <td>${solicitud.fechaFin}</td>

                    <td>${solicitud.diasSolicitados}</td>

                    <td>${solicitud.motivo}</td>

                    <td>${solicitud.estado}</td>

                    <td>
                        <a href="${pageContext.request.contextPath}/jefatura?accion=ver&id=${solicitud.idSolicitud}">
                            Ver detalle
                        </a>
                    </td>

                </tr>

            </c:forEach>

            </tbody>

        </table>

    </c:otherwise>

</c:choose>

</body>

</html>