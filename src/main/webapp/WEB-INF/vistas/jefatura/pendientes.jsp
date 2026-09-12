<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Solicitudes pendientes - Jefatura</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body>

      <header class="app-header" style="position: absolute; top: 0; left: 0; width: 100%; box-sizing: border-box;">
          <div class="app-header-logo-chip">
              <img src="${pageContext.request.contextPath}/img/logo.png" alt="Invercalma">
          </div>
          <div class="app-header-usuario">
              <span>
                  <c:out value="${sessionScope.empleado.nombre} ${sessionScope.empleado.apellido}"/>
                  &middot; <c:out value="${sessionScope.rol.nombre}"/>
              </span>

              &middot;
              <a class="app-header-salir" href="${pageContext.request.contextPath}/logout">Cerrar sesión</a>
          </div>
      </header>

    <h1 style="max-width: 1200px; margin: 120px auto 0 auto; padding: 0 20px; font-family: 'Space Grotesk', sans-serif;">
        Solicitudes Activas de Empleados
    </h1>

    <div class="contenedor">

        <main class="app-contenido">

            <div style="margin-top: 15px; margin-bottom: 20px;">
                <a href="${pageContext.request.contextPath}/solicitudes"
                   style="background-color: #ff6b35; color: white; padding: 10px 20px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block; font-size: 14px; font-family: 'Public Sans', sans-serif;">
                    Ir a Mis Solicitudes Personales
                </a>
            </div>

            <c:if test="${param.resultado eq 'aprobada'}">
                <p class="info-saldo">
                    La solicitud fue aprobada correctamente.
                </p>
            </c:if>

            <c:if test="${param.resultado eq 'rechazada'}">
                <p class="info-saldo">
                    La solicitud fue rechazada correctamente.
                </p>
            </c:if>

            <c:if test="${not empty error}">
                <p class="mensaje-error">
                    <c:out value="${error}"/>
                </p>
            </c:if>

            <c:choose>

                <c:when test="${empty solicitudes}">
                    <p>No hay solicitudes pendientes para revisar.</p>
                </c:when>

                <c:otherwise>

                    <table>

                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Empleado</th>
                            <th>Tipo</th>
                            <th>Fecha inicio</th>
                            <th>Fecha fin</th>
                            <th>Días</th>
                            <th>Motivo</th>
                            <th>Estado</th>
                            <th>Acción</th>
                        </tr>
                        </thead>

                        <tbody>

                        <c:forEach var="solicitud" items="${solicitudes}">

                            <tr>

                                <td>
                                    <c:out value="${solicitud.idSolicitud}"/>
                                </td>

                                <td>
                                    <c:out value="${solicitud.idEmpleado}"/>
                                </td>

                                <td>
                                    <c:out value="${solicitud.idTipoSolicitud}"/>
                                </td>

                                <td>
                                    <c:out value="${solicitud.fechaInicio}"/>
                                </td>

                                <td>
                                    <c:out value="${solicitud.fechaFin}"/>
                                </td>

                                <td>
                                    <c:out value="${solicitud.diasSolicitados}"/>
                                </td>

                                <td>
                                    <c:out value="${solicitud.motivo}"/>
                                </td>

                                <td>
                                    <span class="estado-pendiente">
                                        <c:out value="${solicitud.estado}"/>
                                    </span>
                                </td>

                                <td>
                                    <a href="${pageContext.request.contextPath}/jefatura?accion=ver&id=${solicitud.idSolicitud}">
                                        Ver detalle
                                    </a>
                                </td>

                            </tr>

                        </c:forEach>

                        </tbody>

                    </table>

                </c:otherwise>

            </c:choose>
        </main>

</div>

</body>

</html>
