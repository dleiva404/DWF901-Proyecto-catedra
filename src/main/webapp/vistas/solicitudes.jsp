<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Mis solicitudes</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="contenedor">
    <header>
        <h1>Mis solicitudes de permiso</h1>
        <p>
            Bienvenido(a), <c:out value="${sessionScope.empleado.nombre} ${sessionScope.empleado.apellido}"/>
            (<c:out value="${sessionScope.rol.nombre}"/>)
            &nbsp;|&nbsp;
            <a href="${pageContext.request.contextPath}/incapacidad">Registrar incapacidad</a>
            &nbsp;|&nbsp;
            <a href="${pageContext.request.contextPath}/logout">Cerrar sesión</a>
        </p>
    </header>

    <c:if test="${not empty error}">
        <p class="mensaje-error">${error}</p>
    </c:if>

    <c:if test="${not empty saldoVacaciones}">
        <p class="info-saldo">
            Saldo de vacaciones ${saldoVacaciones.anio}:
            <strong>${saldoVacaciones.diasDisponibles}</strong> de ${saldoVacaciones.diasAsignados} días disponibles.
        </p>
    </c:if>

    <section>
        <h2>Nueva solicitud</h2>
        <form method="post" action="${pageContext.request.contextPath}/solicitudes">
            <label for="idTipoSolicitud">Tipo de solicitud:</label>
            <select id="idTipoSolicitud" name="idTipoSolicitud" required>
                <option value="">-- Seleccione --</option>
                <c:forEach var="tipo" items="${tipos}">
                    <option value="${tipo.idTipoSolicitud}"><c:out value="${tipo.nombre}"/></option>
                </c:forEach>
            </select>
            <p style="font-size:0.85em;color:#666;">
                Si selecciona INCAPACIDAD, se le redirigirá al formulario de carga de documento.
            </p>

            <label for="fechaInicio">Fecha inicio:</label>
            <input type="date" id="fechaInicio" name="fechaInicio" required>

            <label for="fechaFin">Fecha fin:</label>
            <input type="date" id="fechaFin" name="fechaFin" required>

            <label for="motivo">Motivo:</label>
            <textarea id="motivo" name="motivo" maxlength="500" required
                      placeholder="Detalle el motivo de la solicitud"></textarea>

            <button type="submit">Enviar solicitud</button>
        </form>
    </section>

    <section>
        <h2>Historial</h2>
        <table>
            <thead>
            <tr>
                <th>Tipo</th>
                <th>Inicio</th>
                <th>Fin</th>
                <th>Días</th>
                <th>Motivo</th>
                <th>Estado</th>
                <th>Motivo de rechazo</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="s" items="${historial}">
                <tr>
                    <td><c:out value="${tiposPorId[s.idTipoSolicitud]}"/></td>
                    <!-- Nota: fechaInicio/fechaFin son LocalDate, no java.util.Date,
                         por eso se muestran directo (formato AAAA-MM-DD) en vez de
                         usar fmt:formatDate, que solo funciona con java.util.Date -->
                    <td>${s.fechaInicio}</td>
                    <td>${s.fechaFin}</td>
                    <td>${s.diasSolicitados}</td>
                    <td><c:out value="${s.motivo}"/></td>
                    <td>
                        <span class="estado estado-${fn:toLowerCase(s.estado)}">
                            <c:out value="${s.estado}"/>
                        </span>
                    </td>
                    <td><c:out value="${s.motivoRechazo}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty historial}">
                <tr><td colspan="7">Aún no tiene solicitudes registradas.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</div>
</body>
</html>
