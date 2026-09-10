<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Iniciar sesión - Sistema de Solicitud de Permisos</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <div class="contenedor-login">
        <h1>Sistema de Solicitud de Permisos</h1>
        <h2>Iniciar sesión</h2>

        <c:if test="${not empty error}">
            <p class="mensaje-error">${error}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <label for="username">Usuario:</label>
            <input type="text" id="username" name="username" required autofocus>

            <label for="password">Contraseña:</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Ingresar</button>
        </form>
    </div>
</body>
</html>
