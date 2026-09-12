<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mis solicitudes</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <header class="app-header">
        <div class="app-header-logo-chip">
            <<img src="${pageContext.request.contextPath}/img/logo.png" alt="Invercalma">
        </div>
        <div class="app-header-usuario">
            <span>
                <c:out value="${sessionScope.empleado.nombre} ${sessionScope.empleado.apellido}"/>
                &middot; <c:out value="${sessionScope.rol.nombre}"/>
            </span>
            <a class="app-header-salir" href="${pageContext.request.contextPath}/logout">Cerrar sesión</a>
        </div>
    </header>

    <main class="app-contenido">
        <h1>Mis solicitudes de permiso</h1>

        <c:if test="${not empty error}">
            <p class="mensaje-error">${error}</p>
        </c:if>

        <c:if test="${not empty saldoVacaciones}">
            <div class="tarjeta tarjeta-saldo">
                <div class="saldo-texto">
                    <p class="saldo-etiqueta">Vacaciones ${saldoVacaciones.anio}</p>
                    <p class="saldo-numero">${saldoVacaciones.diasDisponibles}<span> / ${saldoVacaciones.diasAsignados} días disponibles</span></p>
                </div>
                <div class="saldo-barra">
                    <c:set var="porcentaje" value="${saldoVacaciones.diasAsignados > 0 ? (saldoVacaciones.diasDisponibles * 100) / saldoVacaciones.diasAsignados : 0}"/>
                    <div class="saldo-barra-relleno" style="width: ${porcentaje}%;"></div>
                </div>
            </div>
        </c:if>

        <section class="tarjeta">
            <h2>Nueva solicitud</h2>
            <form method="post" action="${pageContext.request.contextPath}/solicitudes">
                <label for="idTipoSolicitud">Tipo de solicitud:</label>
                <select id="idTipoSolicitud" name="idTipoSolicitud" required>
                    <option value="">-- Seleccione --</option>
                    <c:forEach var="tipo" items="${tipos}">
                        <option value="${tipo.idTipoSolicitud}"><c:out value="${tipo.nombre}"/></option>
                    </c:forEach>
                </select>
                <p class="nota">Si selecciona INCAPACIDAD, se le redirigirá al formulario de carga de documento.</p>

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

        <section class="tarjeta">
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
                        <td>${s.fechaInicio}</td>
                        <td>${s.fechaFin}</td>
                        <td>${s.diasSolicitados}</td>
                        <td><c:out value="${s.motivo}"/></td>
                        <td><span class="badge badge-${fn:toLowerCase(s.estado)}"><c:out value="${s.estado}"/></span></td>
                        <td><c:out value="${s.motivoRechazo}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty historial}">
                    <tr><td colspan="7">Aún no tiene solicitudes registradas.</td></tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </main>
</body>
</html>