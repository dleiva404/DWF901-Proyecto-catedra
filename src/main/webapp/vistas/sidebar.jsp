<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="uri" value="${pageContext.request.requestURI}" />
<c:set var="uriLower" value="${fn:toLowerCase(uri)}" />

<%-- Obtenemos el nombre del rol directamente desde el objeto Rol guardado en la sesión --%>
<c:set var="nombreRol" value="${not empty sessionScope.rol ? sessionScope.rol.nombre : ''}" />
<c:set var="rolUpper" value="${fn:toUpperCase(nombreRol)}" />

<style>
    body:not(.login-page) #sidebar.sidebar,
    body #sidebar.sidebar {
        top: 70px !important;
        height: calc(100vh - 70px) !important;
    }
    .sidebar-link {
        display: flex !important;
        align-items: center !important;
        padding: 12px 16px !important;
        color: #1b2233 !important;
        text-decoration: none !important;
        border-radius: 8px !important;
        font-weight: 600 !important;
        font-size: 0.95rem !important;
        transition: background-color 0.2s, color 0.2s !important;
    }
    .sidebar-link:hover {
        background-color: #eceef2 !important;
        color: #f2792e !important;
    }
    .sidebar-link.active {
        background-color: #fef2ea !important;
        color: #f2792e !important;
        border-left: 4px solid #f2792e !important;
    }
</style>

<div id="sidebar" class="sidebar" style="width: 260px !important; height: calc(100vh - 70px) !important; position: fixed !important; top: 70px !important; left: 0 !important; background-color: #ffffff !important; border-right: 1px solid #dde3ea !important; z-index: 1000 !important; overflow-y: auto !important; display: flex !important; flex-direction: column !important; box-sizing: border-box !important;">

    <div style="font-family: 'Space Grotesk', sans-serif; font-size: 1.05rem; font-weight: 700; color: #1b2233; padding: 20px 24px 16px 24px; border-bottom: 1px solid #dde3ea; box-sizing: border-box;">
        SISTEMA DE RRHH
    </div>

    <div style="display: flex; flex-direction: column; padding: 16px; gap: 6px;">
        <span style="font-size: 0.75rem; font-weight: 700; color: #6b7280; letter-spacing: 0.08em; padding: 0 12px 8px 12px; text-transform: uppercase;">
            MENÚ PRINCIPAL
        </span>

        <%-- Si el rol es JEFATURA, RRHH o ADMIN, mostramos la gestión de solicitudes de empleados --%>
        <c:if test="${fn:contains(rolUpper, 'JEFATURA') || fn:contains(rolUpper, 'RRHH') || fn:contains(rolUpper, 'ADMIN')}">
            <a href="${pageContext.request.contextPath}/jefatura"
               class="sidebar-link ${fn:contains(uriLower, 'jefatura') || fn:contains(uriLower, 'pendiente') || fn:contains(uriLower, 'detalle') ? 'active' : ''}">
               Solicitudes Empleados
            </a>
        </c:if>

        <%-- Opciones para todos los usuarios --%>
        <a href="${pageContext.request.contextPath}/solicitudes"
           class="sidebar-link ${fn:contains(uriLower, 'solicitud') && !fn:contains(uriLower, 'constancia') && !fn:contains(uriLower, 'historial') && !fn:contains(uriLower, 'jefatura') ? 'active' : ''}">
           Mis Solicitudes
        </a>

        <a href="${pageContext.request.contextPath}/ConstanciaServlet"
           class="sidebar-link ${fn:contains(uriLower, 'constancia') && !fn:contains(uriLower, 'admin') ? 'active' : ''}">
           Mis Constancias
        </a>

        <c:if test="${fn:contains(rolUpper, 'RRHH') || fn:contains(rolUpper, 'ADMIN')}">
            <a href="${pageContext.request.contextPath}/AdminConstanciaServlet"
               class="sidebar-link ${fn:contains(uriLower, 'adminconstancia') ? 'active' : ''}">
               Gestión Constancias
            </a>
        </c:if>

        <a href="${pageContext.request.contextPath}/HistorialServlet"
           class="sidebar-link ${fn:contains(uriLower, 'historial') ? 'active' : ''}">
           Historial
        </a>
    </div>
</div>