package com.permisos.controller;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "AdminConstanciaServlet", urlPatterns = {"/AdminConstanciaServlet"})
public class AdminConstanciaServlet extends HttpServlet {

    private ConstanciaDAO constanciaDAO;
    private EmpleadoDAO empleadoDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        constanciaDAO = new ConstanciaDAO();
        empleadoDAO = new EmpleadoDAO();
    }

        @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin-constancias.xhtml");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        Rol rol = session != null
                ? (Rol) session.getAttribute("rol")
                : null;

        Usuario usuario = session != null
                ? (Usuario) session.getAttribute("usuario")
                : null;

        if (session == null
                || session.getAttribute("empleado") == null
                || usuario == null
                || rol == null
                || !"RRHH".equalsIgnoreCase(rol.getNombre().trim())) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/vistas/error.jsp?mensaje=Acceso%20denegado"
            );
            return;
        }

        String accion =
                request.getParameter("accion");

        String idConstanciaStr =
                request.getParameter("idConstancia");

        if (accion == null || idConstanciaStr == null) {
            request.setAttribute(
                    "error",
                    "No se recibió una acción válida."
            );
            doGet(request, response);
            return;
        }

        try {

            int idConstancia =
                    Integer.parseInt(idConstanciaStr);

            if ("aprobar".equalsIgnoreCase(accion)) {

                constanciaDAO.aprobar(
                        idConstancia,
                        usuario.getIdUsuario(),
                        null
                );

                request.setAttribute(
                        "mensajeExito",
                        "Constancia aprobada correctamente."
                );

            } else if ("rechazar".equalsIgnoreCase(accion)) {

                constanciaDAO.rechazar(
                        idConstancia,
                        usuario.getIdUsuario(),
                        "Rechazada por RRHH",
                        null
                );

                request.setAttribute(
                        "mensajeExito",
                        "Constancia rechazada correctamente."
                );

            } else {

                request.setAttribute(
                        "error",
                        "La acción indicada no es válida."
                );
            }

        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "ID de constancia inválido."
            );

        } catch (SQLException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );
        }

        doGet(request, response);
    }
}