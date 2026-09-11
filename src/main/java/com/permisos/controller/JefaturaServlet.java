package com.permisos.controller;

import com.permisos.dao.SolicitudDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Solicitud;
import com.permisos.model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "JefaturaServlet", urlPatterns = {"/jefatura"})
public class JefaturaServlet extends HttpServlet {

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Usuario usuario = obtenerUsuarioSesion(session);
        Empleado empleado = obtenerEmpleadoSesion(session);

        if (usuario == null || empleado == null) {
            request.setAttribute(
                    "error",
                    "Debe iniciar sesión para acceder al módulo de jefatura."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/vistas/jefatura/pendientes.jsp"
            ).forward(request, response);

            return;
        }

        String accion = limpiar(request.getParameter("accion"));

        if ("ver".equalsIgnoreCase(accion)) {
            mostrarDetalle(request, response);
        } else {
            listarPendientes(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        Usuario usuario = obtenerUsuarioSesion(session);
        Empleado empleado = obtenerEmpleadoSesion(session);

        if (usuario == null || empleado == null) {
            request.setAttribute(
                    "error",
                    "Debe iniciar sesión para realizar esta operación."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/vistas/jefatura/pendientes.jsp"
            ).forward(request, response);

            return;
        }

        String accion = limpiar(request.getParameter("accion"));

        if ("aprobar".equalsIgnoreCase(accion)) {

            aprobarSolicitud(request, response);

        } else if ("rechazar".equalsIgnoreCase(accion)) {

            rechazarSolicitud(request, response);

        } else {

            request.setAttribute(
                    "error",
                    "La acción solicitada no es válida."
            );

            listarPendientes(request, response);
        }
    }

    private void listarPendientes(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Empleado empleado = obtenerEmpleadoSesion(session);

        if (empleado == null) {
            request.setAttribute(
                    "error",
                    "No se pudo identificar al empleado de la sesión."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/vistas/jefatura/pendientes.jsp"
            ).forward(request, response);

            return;
        }

        try {

            int idEmpleadoJefe = empleado.getIdEmpleado();

            List<Solicitud> solicitudes =
                    solicitudDAO.listarPendientesPorJefatura(
                            idEmpleadoJefe
                    );

            request.setAttribute("solicitudes", solicitudes);

        } catch (SQLException e) {

            request.setAttribute(
                    "error",
                    "No fue posible consultar las solicitudes pendientes."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/vistas/jefatura/pendientes.jsp"
        ).forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException {

        String id = limpiar(request.getParameter("id"));

        if (id.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Debe indicar una solicitud válida."
            );

            listarPendientes(request, response);
            return;
        }

        try {

            int idSolicitud = Integer.parseInt(id);

            Solicitud solicitud =
                    solicitudDAO.buscarPorId(idSolicitud);

            if (solicitud == null) {

                request.setAttribute(
                        "error",
                        "La solicitud indicada no existe."
                );

                listarPendientes(request, response);
                return;
            }

            request.setAttribute("solicitud", solicitud);

            request.getRequestDispatcher(
                    "/WEB-INF/vistas/jefatura/detalle.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "El identificador de la solicitud no es válido."
            );

            listarPendientes(request, response);

        } catch (SQLException e) {

            request.setAttribute(
                    "error",
                    "No fue posible consultar el detalle de la solicitud."
            );

            listarPendientes(request, response);
        }
    }

    private void aprobarSolicitud(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ServletException, IOException {

        String id = limpiar(
                request.getParameter("idSolicitud")
        );

        if (id.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Debe indicar la solicitud que desea aprobar."
            );

            listarPendientes(request, response);
            return;
        }

        try {

            int idSolicitud = Integer.parseInt(id);

            Solicitud solicitud =
                    solicitudDAO.buscarPorId(idSolicitud);

            if (solicitud == null) {

                request.setAttribute(
                        "error",
                        "La solicitud indicada no existe."
                );

                listarPendientes(request, response);
                return;
            }

            if (!"PENDIENTE".equalsIgnoreCase(
                    solicitud.getEstado()
            )) {

                request.setAttribute(
                        "error",
                        "Solo se pueden aprobar solicitudes pendientes."
                );

                listarPendientes(request, response);
                return;
            }

            HttpSession session =
                    request.getSession(false);

            Empleado empleado =
                    obtenerEmpleadoSesion(session);

            Usuario usuario =
                    obtenerUsuarioSesion(session);

            int idEmpleadoJefe =
                    empleado.getIdEmpleado();

            int idUsuarioJefatura =
                    usuario.getIdUsuario();

            solicitudDAO.aprobar(
                    idSolicitud,
                    idEmpleadoJefe,
                    idUsuarioJefatura
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/jefatura?resultado=aprobada"
            );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "El identificador de la solicitud no es válido."
            );

            listarPendientes(request, response);

        } catch (SQLException e) {

            request.setAttribute(
                    "error",
                    "No fue posible aprobar la solicitud."
            );

            listarPendientes(request, response);
        }
    }

    private void rechazarSolicitud(HttpServletRequest request,
                                   HttpServletResponse response)
            throws ServletException, IOException {

        String id = limpiar(
                request.getParameter("idSolicitud")
        );

        String motivoRechazo = limpiar(
                request.getParameter("motivoRechazo")
        );

        if (id.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Debe indicar la solicitud que desea rechazar."
            );

            listarPendientes(request, response);
            return;
        }

        if (motivoRechazo.isEmpty()) {

            request.setAttribute(
                    "error",
                    "Debe indicar el motivo del rechazo."
            );

            listarPendientes(request, response);
            return;
        }

        if (motivoRechazo.length() < 5
                || motivoRechazo.length() > 250) {

            request.setAttribute(
                    "error",
                    "El motivo del rechazo debe tener entre 5 y 250 caracteres."
            );

            listarPendientes(request, response);
            return;
        }

        try {

            int idSolicitud =
                    Integer.parseInt(id);

            Solicitud solicitud =
                    solicitudDAO.buscarPorId(idSolicitud);

            if (solicitud == null) {

                request.setAttribute(
                        "error",
                        "La solicitud indicada no existe."
                );

                listarPendientes(request, response);
                return;
            }

            if (!"PENDIENTE".equalsIgnoreCase(
                    solicitud.getEstado()
            )) {

                request.setAttribute(
                        "error",
                        "Solo se pueden rechazar solicitudes pendientes."
                );

                listarPendientes(request, response);
                return;
            }

            HttpSession session =
                    request.getSession(false);

            Empleado empleado =
                    obtenerEmpleadoSesion(session);

            Usuario usuario =
                    obtenerUsuarioSesion(session);

            int idEmpleadoJefe =
                    empleado.getIdEmpleado();

            int idUsuarioJefatura =
                    usuario.getIdUsuario();

            solicitudDAO.rechazar(
                    idSolicitud,
                    idEmpleadoJefe,
                    motivoRechazo,
                    idUsuarioJefatura
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/jefatura?resultado=rechazada"
            );

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "El identificador de la solicitud no es válido."
            );

            listarPendientes(request, response);

        } catch (SQLException e) {

            request.setAttribute(
                    "error",
                    "No fue posible rechazar la solicitud."
            );

            listarPendientes(request, response);
        }
    }

    private Usuario obtenerUsuarioSesion(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        Object usuario =
                session.getAttribute("usuario");

        if (usuario instanceof Usuario) {
            return (Usuario) usuario;
        }

        return null;
    }

    private Empleado obtenerEmpleadoSesion(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        Object empleado =
                session.getAttribute("empleado");

        if (empleado instanceof Empleado) {
            return (Empleado) empleado;
        }

        return null;
    }

    private String limpiar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.trim();
    }
}