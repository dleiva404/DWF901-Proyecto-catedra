<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Gestión de Constancias - RRHH</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body style="margin: 0; background-color: #f0f2f5; font-family: 'Public Sans', sans-serif;">

    <jsp:include page="/vistas/header.jsp" />
    <jsp:include page="/vistas/sidebar.jsp" />
    <main class="app-contenido" style="margin-left: 260px; padding-top: 90px; padding-left: 30px; padding-right: 30px; padding-bottom: 40px; box-sizing: border-box;">
        <h1>Panel de RRHH - Solicitudes de Constancias</h1>

        <c:if test="${not empty mensajeExito}">
            <p class="mensaje-exito" style="color: green; margin-bottom: 1rem;"><c:out value="${mensajeExito}"/></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mensaje-error"><c:out value="${error}"/></p>
        </c:if>

        <!-- Modificamos la tarjeta para que aproveche mejor el ancho -->
        <section class="tarjeta" style="width: 100%; box-sizing: border-box;">
            <h2>Listado de solicitudes</h2>
            <table style="width: 100%;">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>ID Empleado</th>
                    <th>Tipo</th>
                    <th>Institución</th>
                    <th>Motivo</th>
                    <th>Estado</th>
                    <th style="white-space: nowrap;">Fecha</th> <!-- Evita que el encabezado se parta -->
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="c" items="${listaConstancias}">
                    <tr>
                        <td>${c.idConstancia}</td>
                        <td>${c.idEmpleado}</td>
                        <td><c:out value="${c.tipo}"/></td>
                        <td><c:out value="${c.institucion}"/></td>
                        <td><c:out value="${c.motivo}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${c.estado == 'APROBADO' || c.estado == 'APROBADA'}">
                                    <span class="badge" style="background-color: #d1e7dd; color: #0f5132; padding: 5px 10px; border-radius: 20px; font-weight: bold; display: inline-block;">APROBADA</span>
                                </c:when>
                                <c:when test="${c.estado == 'RECHAZADO' || c.estado == 'RECHAZADA'}">
                                    <span class="badge" style="background-color: #f8d7da; color: #842029; padding: 5px 10px; border-radius: 20px; font-weight: bold; display: inline-block;">RECHAZADA</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge" style="background-color: #fff3cd; color: #664d03; padding: 5px 10px; border-radius: 20px; font-weight: bold; display: inline-block;">PENDIENTE</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td style="white-space: nowrap;">${c.fechaCreacion}</td> <!-- Evita que la fecha se parta en dos renglones -->
                        <td>
                            <c:if test="${c.estado == 'PENDIENTE'}">
                                <div style="display: flex; gap: 6px; align-items: center;">
                                    <form action="${pageContext.request.contextPath}/AdminConstanciaServlet" method="POST" style="margin: 0;">
                                        <input type="hidden" name="idConstancia" value="${c.idConstancia}">
                                        <input type="hidden" name="accion" value="aprobar">
                                        <button type="submit" style="background-color: #28a745; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 4px;">Aprobar</button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/AdminConstanciaServlet" method="POST" style="margin: 0;">
                                        <input type="hidden" name="idConstancia" value="${c.idConstancia}">
                                        <input type="hidden" name="accion" value="rechazar">
                                        <button type="submit" style="background-color: #dc3545; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 4px;">Rechazar</button>
                                    </form>
                                </div>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty listaConstancias}">
                    <tr><td colspan="8">No hay solicitudes registradas en el sistema.</td></tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </main>
</body>
</html>