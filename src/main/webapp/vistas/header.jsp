<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<header style="width: 100%; height: 70px; position: fixed; top: 0; left: 0; background-color: var(--color-primario, #1b2233); color: #ffffff; z-index: 1040; display: flex; justify-content: space-between; align-items: center; padding: 0 32px; box-sizing: border-box; box-shadow: 0 1px 4px rgba(0,0,0,0.15);">
    <!-- Logo Grupo Calma -->
    <div style="background: #ffffff; display: inline-flex; align-items: center; padding: 6px 12px; border-radius: 8px;">
        <img src="${pageContext.request.contextPath}/img/logo.png" alt="Invercalma" style="height: 35px; width: auto; display: block;">
    </div>

    <!-- Usuario, Rol y botón Cerrar sesión -->
    <div style="display: flex; align-items: center; gap: 24px;">
        <span style="color: #ffffff; font-size: 0.9em; font-weight: 500;">
            <c:out value="${sessionScope.empleado.nombre} ${sessionScope.empleado.apellido}"/>
            — <span style="color: #cfd8e3; font-size: 0.85em;">Rol:
                <c:choose>
                    <c:when test="${not empty sessionScope.rol.nombre}">
                        <c:out value="${sessionScope.rol.nombre}"/>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${sessionScope.rol}"/>
                    </c:otherwise>
                </c:choose>
            </span>
        </span>

        <a href="${pageContext.request.contextPath}/logout" style="color: #ffffff; text-decoration: none; border: 1px solid rgba(255, 255, 255, 0.4); padding: 6px 16px; border-radius: 8px; font-size: 0.85em; font-weight: 600; background-color: rgba(255, 255, 255, 0.05);">Cerrar sesión</a>
    </div>
</header>