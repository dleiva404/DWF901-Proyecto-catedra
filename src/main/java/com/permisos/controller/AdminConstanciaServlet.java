package com.permisos.controller;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;

import java.io.IOException;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("empleado") == null) {
            response.sendRedirect(request.getContextPath() + "/vistas/login.jsp");
            return;
        }

        Rol rol = (Rol) session.getAttribute("rol");
        if (rol == null || rol.getNombre() == null || !"rrhh".equalsIgnoreCase(rol.getNombre().trim())) {
            response.sendRedirect(request.getContextPath() + "/vistas/error.jsp?mensaje=Acceso%20denegado");
            return;
        }

        List<Constancia> listaConstancias = constanciaDAO.obtenerTodas();

        Map<Integer, String> mapNombresEmpleados = new HashMap<>();
        for (Constancia c : listaConstancias) {
            if (!mapNombresEmpleados.containsKey(c.getIdEmpleado())) {
                try {
                    Empleado emp = empleadoDAO.buscarPorId(c.getIdEmpleado());
                    if (emp != null) {
                        mapNombresEmpleados.put(c.getIdEmpleado(), emp.getNombre() + " " + emp.getApellido());
                    } else {
                        mapNombresEmpleados.put(c.getIdEmpleado(), "Colaborador ID: " + c.getIdEmpleado());
                    }
                } catch (Exception e) {
                    mapNombresEmpleados.put(c.getIdEmpleado(), "Colaborador ID: " + c.getIdEmpleado());
                }
            }
        }

        request.setAttribute("listaConstancias", listaConstancias);
        request.setAttribute("mapNombresEmpleados", mapNombresEmpleados);

        request.getRequestDispatcher("/vistas/admin_constancias.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Rol rol = (session != null) ? (Rol) session.getAttribute("rol") : null;
        if (session == null || session.getAttribute("empleado") == null || rol == null || !"rrhh".equalsIgnoreCase(rol.getNombre().trim())) {
            response.sendRedirect(request.getContextPath() + "/vistas/error.jsp?mensaje=Acceso%20denegado");
            return;
        }

        String accion = request.getParameter("accion");
        String idConstanciaStr = request.getParameter("idConstancia");

        if (accion != null && idConstanciaStr != null) {
            try {
                int idConstancia = Integer.parseInt(idConstanciaStr);
                String nuevoEstado = "";

                if ("aprobar".equals(accion)) {
                    nuevoEstado = "APROBADO";
                } else if ("rechazar".equals(accion)) {
                    nuevoEstado = "RECHAZADO";
                }

                if (!nuevoEstado.isEmpty()) {
                    boolean actualizado = constanciaDAO.actualizarEstado(idConstancia, nuevoEstado);
                    if (actualizado) {
                        request.setAttribute("mensajeExito", "Solicitud actualizada a " + nuevoEstado);
                    } else {
                        request.setAttribute("error", "No se pudo actualizar el estado de la constancia.");
                    }
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "ID de constancia inválido.");
            }
        }

        doGet(request, response);
    }
}