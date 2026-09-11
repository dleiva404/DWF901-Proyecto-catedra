<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Solicitudes pendientes - Jefatura</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body>

<div class="contenedor">

    <header>
        <h1>Solicitudes pendientes de jefatura</h1>

        <c:if test="${not empty sessionScope.empleado}">
            <p>
                Bienvenido(a),
                <strong>
                    <c:out value="${sessionScope.empleado.nombre}"/>
                    <c:out value="${sessionScope.empleado.apellido}"/>
                </strong>
            </p>
        </c:if>
    </header>

    <c:if test="${param.resultado eq 'aprobada'}">
        <p class="info-saldo">
            La solicitud fue aprobada correctamente.
        </p>
    </c:if>

    <c:if test="${param.resultado eq 'rechazada'}">
        <p class="info-saldo">
            La solicitud fue rechazada correctamente.
        </p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="mensaje-error">
            <c:out value="${error}"/>
        </p>
    </c:if>

    <c:choose>

        <c:when test="${empty solicitudes}">
            <p>No hay solicitudes pendientes para revisar.</p>
        </c:when>

        <c:otherwise>

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Empleado</th>
                    <th>Tipo</th>
                    <th>Fecha inicio</th>
                    <th>Fecha fin</th>
                    <th>Días</th>
                    <th>Motivo</th>
                    <th>Estado</th>
                    <th>Acción</th>
                </tr>
                </thead>

                <tbody>

                <c:forEach var="solicitud" items="${solicitudes}">

                    <tr>

                        <td>
                            <c:out value="${solicitud.idSolicitud}"/>
                        </td>

                        <td>
                            <c:out value="${solicitud.idEmpleado}"/>
                        </td>

                        <td>
                            <c:out value="${solicitud.idTipoSolicitud}"/>
                        </td>

                        <td>
                            <c:out value="${solicitud.fechaInicio}"/>
                        </td>

                        <td>
                            <c:out value="${solicitud.fechaFin}"/>
                        </td>

                        <td>
                            <c:out value="${solicitud.diasSolicitados}"/>
                        </td>

                        <td>
                            <c:out value="${solicitud.motivo}"/>
                        </td>

                        <td>
                            <span class="estado-pendiente">
                                <c:out value="${solicitud.estado}"/>
                            </span>
                        </td>

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

</div>

</body>

</html>