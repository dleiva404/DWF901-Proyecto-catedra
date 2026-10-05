<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Historial General de Solicitudes</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body style="margin: 0; background-color: #f0f2f5; font-family: 'Public Sans', sans-serif;">

    <jsp:include page="/vistas/header.jsp" />
    <jsp:include page="/vistas/sidebar.jsp" />
    <main class="app-contenido" style="margin-left: 260px; padding-top: 90px; padding-left: 30px; padding-right: 30px; padding-bottom: 40px; box-sizing: border-box;">
        <h1>Historial General de Solicitudes</h1>

        <section class="tarjeta" style="width: 100%; box-sizing: border-box;">
            <table style="width: 100%;">
                <thead>
                <tr>
                    <th>Tipo</th>
                    <th style="white-space: nowrap;">Inicio</th>
                    <th style="white-space: nowrap;">Fin</th>
                    <th>Días</th>
                    <th>Motivo</th>
                    <th>Estado</th>
                    <th>Motivo de rechazo</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${listaHistorial}">
                    <tr>
                        <td><c:out value="${s.tipo}"/></td>
                        <td style="white-space: nowrap;">${s.fechaInicio}</td>
                        <td style="white-space: nowrap;">${s.fechaFin}</td>
                        <td>${s.diasSolicitados}</td>
                        <td><c:out value="${s.motivo}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${s.estado == 'APROBADO' || s.estado == 'APROBADA'}">
                                    <span class="badge" style="background-color: #d1e7dd; color: #0f5132; padding: 5px 10px; border-radius: 20px; font-weight: bold; display: inline-block;">APROBADA</span>
                                </c:when>
                                <c:when test="${s.estado == 'RECHAZADO' || s.estado == 'RECHAZADA'}">
                                    <span class="badge" style="background-color: #f8d7da; color: #842029; padding: 5px 10px; border-radius: 20px; font-weight: bold; display: inline-block;">RECHAZADA</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge" style="background-color: #fff3cd; color: #664d03; padding: 5px 10px; border-radius: 20px; font-weight: bold; display: inline-block;">PENDIENTE</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${s.motivoRechazo}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty listaHistorial}">
                    <tr><td colspan="7">No hay solicitudes registradas en el historial.</td></tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </main>
</body>
</html>