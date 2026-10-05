<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Solicitud de Constancias</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    <style>
        /* Estilos rápidos para los badges de estado */
        .badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 600;
            display: inline-block;
            text-align: center;
        }
        .badge-pendiente {
            background-color: #fef3c7;
            color: #92400e;
        }
        .badge-aprobada {
            background-color: #d1fae5;
            color: #065f46;
        }
        .badge-rechazada {
            background-color: #fee2e2;
            color: #991b1b;
        }
        .btn-generar {
            background-color: #0f172a;
            color: #ffffff;
            padding: 6px 12px;
            border-radius: 6px;
            text-decoration: none;
            font-size: 0.85rem;
            font-weight: 600;
            display: inline-block;
            transition: background 0.2s;
        }
        .btn-generar:hover {
            background-color: #334155;
        }
    </style>
</head>
<body style="margin: 0; background-color: #f0f2f5; font-family: 'Public Sans', sans-serif;">

    <jsp:include page="/vistas/header.jsp" />
    <jsp:include page="/vistas/sidebar.jsp" />
    <main class="app-contenido" style="margin-left: 260px; padding-top: 90px; padding-left: 30px; padding-right: 30px; padding-bottom: 40px; box-sizing: border-box;">
        <h1>Mis solicitudes de constancia</h1>

        <!-- Mensajes de éxito o error -->
        <c:if test="${not empty mensajeExito}">
            <p class="mensaje-exito" style="color: green; font-weight: 600; margin-bottom: 1rem;"><c:out value="${mensajeExito}"/></p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mensaje-error"><c:out value="${error}"/></p>
        </c:if>

        <!-- Formulario de Nueva Solicitud -->
        <section class="tarjeta">
            <h2>Nueva solicitud</h2>
            <form action="${pageContext.request.contextPath}/ConstanciaServlet" method="POST">

                <label for="tipoConstancia">Tipo de constancia:</label>
                <select id="tipoConstancia" name="tipoConstancia" required>
                    <option value="">-- Seleccione --</option>
                    <option value="Salarial">Constancia Salarial</option>
                    <option value="Laboral">Constancia Laboral</option>
                </select>

                <label for="institucion">Dirigido a (Institución):</label>
                <input type="text" id="institucion" name="institucion" required placeholder="Ej. Banco Agrícola, etc.">

                <label for="motivo">Motivo:</label>
                <textarea id="motivo" name="motivo" maxlength="500" rows="3" required placeholder="Detalle el motivo de la solicitud"></textarea>

                <button type="submit">Enviar solicitud</button>
            </form>
        </section>

        <!-- Historial de Solicitudes -->
        <section class="tarjeta">
            <h2>Historial</h2>
            <table>
                <thead>
                    <tr>
                        <th>Tipo</th>
                        <th>Institución</th>
                        <th>Motivo</th>
                        <th>Estado</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty historialConstancias}">
                            <c:forEach var="c" items="${historialConstancias}">
                                <tr>
                                    <td><c:out value="${c.tipo}"/></td>
                                    <td><c:out value="${c.institucion}"/></td>
                                    <td><c:out value="${c.motivo}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.estado eq 'APROBADO' or c.estado eq 'APROBADA'}">
                                                <span class="badge badge-aprobada">APROBADA</span>
                                            </c:when>
                                            <c:when test="${c.estado eq 'RECHAZADO' or c.estado eq 'RECHAZADO'}">
                                                <span class="badge badge-rechazada">RECHAZADA</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-pendiente">PENDIENTE</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${c.estado eq 'APROBADO' or c.estado eq 'APROBADA'}">
                                            <a href="${pageContext.request.contextPath}/GenerarConstanciaPDFServlet?id=${c.idConstancia}" class="btn-generar" target="_blank">Generar</a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="5" style="text-align: center;">No hay solicitudes recientes registradas.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </section>
    </main>
</body>
</html>