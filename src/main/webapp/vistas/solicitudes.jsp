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
<body style="margin: 0; background-color: #f0f2f5; font-family: 'Public Sans', sans-serif;">

    <jsp:include page="/vistas/header.jsp" />
    <jsp:include page="/vistas/sidebar.jsp" />
    <main class="app-contenido" style="margin-left: 260px; padding-top: 90px; padding-left: 30px; padding-right: 30px; padding-bottom: 40px; box-sizing: border-box;">
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
    </main>
</body>
</html>