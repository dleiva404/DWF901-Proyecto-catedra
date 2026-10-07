package com.permisos.controller;

import com.permisos.dao.SolicitudDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Solicitud;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "HistorialServlet", urlPatterns = {"/HistorialServlet"})
public class HistorialServlet extends HttpServlet {

    private SolicitudDAO solicitudDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        solicitudDAO = new SolicitudDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("empleado") == null) {
            response.sendRedirect(request.getContextPath() + "/vistas/login.jsp");
            return;
        }

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        String tipoFiltro = request.getParameter("tipoFiltro");
        if (tipoFiltro == null || tipoFiltro.isEmpty()) {
            tipoFiltro = "todos";
        }

        try {
            List<Solicitud> listaHistorial = solicitudDAO.obtenerHistorialPorTipo(empleado.getIdEmpleado(), tipoFiltro);

            request.setAttribute("listaHistorial", listaHistorial);
            request.setAttribute("tipoFiltroActual", tipoFiltro);

            request.getRequestDispatcher("/vistas/historial.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar el historial de solicitudes.");
            request.getRequestDispatcher("/vistas/error.jsp").forward(request, response);
        }
    }
}