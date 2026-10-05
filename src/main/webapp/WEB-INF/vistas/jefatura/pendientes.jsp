<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Solicitudes pendientes - Jefatura</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body>

    <jsp:include page="/vistas/sidebar.jsp" />
    <jsp:include page="/vistas/header.jsp" />

    <main class="app-contenido" style="margin-left: 260px; padding: 90px 30px 40px 30px; max-width: 100%; box-sizing: border-box;">

        <h1 style="font-family: 'Space Grotesk', sans-serif; margin-top: 0; margin-bottom: 20px;">
            Solicitudes Activas de Empleados
        </h1>

        <!-- BARRA DE BÚSQUEDA -->
        <form action="${pageContext.request.contextPath}/jefatura" method="get" style="margin-bottom: 20px; display: flex; gap: 10px; align-items: center;">
            <input type="text"
                   name="busqueda"
                   value="${not empty busquedaActual ? busquedaActual : ''}"
                   placeholder="Buscar por nombre, apellido o empresa..."
                   style="padding: 10px; width: 350px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px;">

            <button type="submit" class="btn-aprobar" style="padding: 10px 20px; margin: 0; cursor: pointer;">Buscar</button>

            <c:if test="${not empty busquedaActual}">
                <a href="${pageContext.request.contextPath}/jefatura" style="padding: 10px 15px; background: #6c757d; color: white; text-decoration: none; border-radius: 4px; font-size: 14px; display: inline-flex; align-items: center;">Limpiar</a>
            </c:if>
        </form>

        <div class="tarjeta" style="width: 100%; overflow-x: auto; box-sizing: border-box;">

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

                    <table style="margin-top: 0; width: 100%; border-collapse: collapse; min-width: 950px;">
                        <thead>
                        <tr style="text-align: left; border-bottom: 2px solid #dee2e6;">
                            <th style="padding: 12px 10px;">ID</th>
                            <th style="padding: 12px 10px;">Empleado / Empresa</th>
                            <th style="padding: 12px 10px;">Tipo</th>
                            <th style="padding: 12px 10px; white-space: nowrap;">Fecha inicio</th>
                            <th style="padding: 12px 10px; white-space: nowrap;">Fecha fin</th>
                            <th style="padding: 12px 10px;">Días</th>
                            <th style="padding: 12px 10px; width: 22%;">Motivo</th>
                            <th style="padding: 12px 10px;">Estado</th>
                            <th style="padding: 12px 10px;">Acción</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="solicitud" items="${solicitudes}">
                            <tr style="border-bottom: 1px solid #dee2e6;">
                                <td style="padding: 12px 10px;"><c:out value="${solicitud.idSolicitud}"/></td>

                                <!-- EMPLEADO Y EMPRESA -->
                                <td style="padding: 12px 10px;">
                                    <strong><c:out value="${solicitud.nombreEmpleado}"/></strong><br>
                                    <span style="font-size: 0.85em; color: #6c757d;"><c:out value="${solicitud.nombreEmpresa}"/></span>
                                </td>

                                <td style="padding: 12px 10px;"><c:out value="${solicitud.idTipoSolicitud}"/></td>
                                <td style="padding: 12px 10px; white-space: nowrap;"><c:out value="${solicitud.fechaInicio}"/></td>
                                <td style="padding: 12px 10px; white-space: nowrap;"><c:out value="${solicitud.fechaFin}"/></td>
                                <td style="padding: 12px 10px;"><c:out value="${solicitud.diasSolicitados}"/></td>
                                <td style="padding: 12px 10px;"><c:out value="${solicitud.motivo}"/></td>

                                <!-- ESTADO CON PASTILLAS DE COLORES -->
                                <td style="padding: 12px 10px;">
                                    <c:choose>
                                        <c:when test="${solicitud.estado == 'PENDIENTE'}">
                                            <span style="background-color: #fef08a; color: #854d0e; padding: 4px 12px; border-radius: 12px; font-weight: 600; font-size: 0.85em; display: inline-block;">
                                                PENDIENTE
                                            </span>
                                        </c:when>
                                        <c:when test="${solicitud.estado == 'APROBADA'}">
                                            <span style="background-color: #d1fae5; color: #065f46; padding: 4px 12px; border-radius: 12px; font-weight: 600; font-size: 0.85em; display: inline-block;">
                                                APROBADA
                                            </span>
                                        </c:when>
                                        <c:when test="${solicitud.estado == 'RECHAZADA'}">
                                            <span style="background-color: #fee2e2; color: #991b1b; padding: 4px 12px; border-radius: 12px; font-weight: 600; font-size: 0.85em; display: inline-block;">
                                                RECHAZADA
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="padding: 4px 12px; border-radius: 12px; font-weight: 600; font-size: 0.85em; display: inline-block;">
                                                <c:out value="${solicitud.estado}"/>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td style="padding: 12px 10px;">
                                    <a href="${pageContext.request.contextPath}/jefatura?accion=ver&id=${solicitud.idSolicitud}" style="color: #0056b3; text-decoration: none; font-weight: 600;">
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

    </main>

</body>

</html>