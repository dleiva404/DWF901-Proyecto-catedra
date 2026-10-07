<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Detalle de solicitud</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Public+Sans:wght@400;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>

<body style="margin: 0; background-color: #eceef2; font-family: 'Public Sans', sans-serif;">

    <jsp:include page="/vistas/sidebar.jsp" />
    <jsp:include page="/vistas/header.jsp" />

    <main class="app-contenido" style="margin-left: 260px; padding: 90px 30px 40px 30px; max-width: 1000px; box-sizing: border-box;">

        <h1 style="font-family: 'Space Grotesk', sans-serif; margin-top: 0; margin-bottom: 20px;">
            Detalle de la solicitud
        </h1>

        <div class="tarjeta">

            <c:if test="${not empty error}">
                <p class="mensaje-error">
                    <c:out value="${error}"/>
                </p>
            </c:if>

            <c:if test="${not empty solicitud}">

                <p>
                    <strong>ID:</strong>
                    <c:out value="${solicitud.idSolicitud}"/>
                </p>

                <p>
                    <strong>Empleado:</strong>
                    <c:out value="${solicitud.nombreEmpleado}"/>
                </p>

                <p>
                    <strong>Empresa:</strong>
                    <c:out value="${solicitud.nombreEmpresa}"/>
                </p>

                <p>
                    <strong>Tipo de solicitud:</strong>
                    <c:out value="${solicitud.idTipoSolicitud}"/>
                </p>

                <p>
                    <strong>Fecha de inicio:</strong>
                    <c:out value="${solicitud.fechaInicio}"/>
                </p>

                <p>
                    <strong>Fecha de fin:</strong>
                    <c:out value="${solicitud.fechaFin}"/>
                </p>

                <p>
                    <strong>Días solicitados:</strong>
                    <c:out value="${solicitud.diasSolicitados}"/>
                </p>

                <p>
                    <strong>Motivo:</strong>
                    <c:out value="${solicitud.motivo}"/>
                </p>

                <p>
                    <strong>Estado:</strong>
                    <span class="estado-pendiente">
                        <c:out value="${solicitud.estado}"/>
                    </span>
                </p>

                <hr style="border: 0; border-top: 1px solid #dde3ea; margin: 20px 0;">

                <%-- BLOQUE CONDICIONAL: Solo mostramos acciones de jefatura si NO es RRHH ni ADMIN --%>
                <c:if test="${not fn:contains(fn:toUpperCase(sessionScope.rol.nombre), 'RRHH') && not fn:contains(fn:toUpperCase(sessionScope.rol.nombre), 'ADMIN')}">

                    <h2>Acciones de jefatura</h2>

                    <form action="${pageContext.request.contextPath}/jefatura"
                          method="post" style="margin-bottom: 20px;">

                        <input type="hidden" name="accion" value="aprobar">
                        <input type="hidden" name="idSolicitud" value="${solicitud.idSolicitud}">

                        <button type="submit" class="btn-aprobar" style="margin-top: 8px;">
                            Aprobar solicitud
                        </button>

                    </form>

                    <form action="${pageContext.request.contextPath}/jefatura"
                          method="post">

                        <input type="hidden" name="accion" value="rechazar">
                        <input type="hidden" name="idSolicitud" value="${solicitud.idSolicitud}">

                        <label for="motivoRechazo" style="display: block; margin-bottom: 5px; font-weight: 600;">
                            Motivo del rechazo:
                        </label>

                        <textarea id="motivoRechazo"
                                  name="motivoRechazo"
                                  required
                                  minlength="5"
                                  maxlength="250"
                                  rows="4"
                                  placeholder="Explique el motivo del rechazo" style="width: 100%; margin-bottom: 10px;"></textarea>

                        <button type="submit" class="btn-rechazar" style="margin-top: 4px;">
                            Rechazar solicitud
                        </button>

                    </form>

                </c:if>

            </c:if>

        </div>

    </main>

</body>

</html>