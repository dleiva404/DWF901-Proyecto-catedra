<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión - Sistema de Solicitud de Permisos</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <div class="login-pantalla">
        <div class="login-tarjeta">
            <div class="login-marca">
                <div class="marca-logo-fila">
                    <img src="${pageContext.request.contextPath}/img/logo.png" alt="Invercalma">
                </div>
                <h1 class="marca-titulo">Sistema de Solicitud de Permisos</h1>
                <p class="marca-descripcion">
                    Gestiona tus solicitudes.
                </p>
            </div>

            <div class="login-formulario-panel">
                <div class="login-formulario">
                    <h2>Bienvenido</h2>
                    <p class="subtitulo">Ingresa con tu usuario y contraseña.</p>

                    <c:if test="${not empty error}">
                        <p class="mensaje-error">${error}</p>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/login">
                        <label for="username">Usuario</label>
                        <input type="text" id="username" name="username" required autofocus>

                        <label for="password">Contraseña</label>
                        <input type="password" id="password" name="password" required>

                        <label class="login-recordar">
                            <input type="checkbox" name="recordarme">
                            Recordarme
                        </label>

                        <button type="submit">Ingresar</button>
                    </form>

                    <div class="login-divisor"></div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>