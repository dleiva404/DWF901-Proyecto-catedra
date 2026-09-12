<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Registrar incapacidad</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <header class="app-header">
        <div class="app-header-logo-chip">
            <img src="${pageContext.request.contextPath}/img/logo.png" alt="Invercalma">
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
        <h1>Registrar incapacidad</h1>

        <c:if test="${not empty error}">
            <p class="mensaje-error">${error}</p>
        </c:if>

        <section class="tarjeta">
            <form method="post" action="${pageContext.request.contextPath}/incapacidad">
                <label for="numeroDocumento">Número de documento (ISSS):</label>
                <input type="text" id="numeroDocumento" name="numeroDocumento" maxlength="100" required>

                <label for="fechaInicio">Fecha inicio:</label>
                <input type="date" id="fechaInicio" name="fechaInicio" required>

                <label for="fechaFin">Fecha fin:</label>
                <input type="date" id="fechaFin" name="fechaFin" required>

                <label for="observaciones">Observaciones:</label>
                <textarea id="observaciones" name="observaciones" maxlength="500"
                          placeholder="Detalle adicional (opcional)"></textarea>

                <p class="nota">
                    La carga del documento PDF del ISSS se habilitará en una fase posterior;
                    por ahora solo se registra el número de documento.
                </p>

                <button type="submit">Registrar incapacidad</button>
            </form>
        </section>
    </main>
</body>
</html>