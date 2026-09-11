<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ocurrió un error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="contenedor">
    <h1>Ocurrió un error</h1>
    <p class="mensaje-error">
        <c:out value="${not empty error ? error : 'Error inesperado.'}"/>
    </p>
    <a href="${pageContext.request.contextPath}/login">Volver al inicio</a>
</div>
</body>
</html>
