package com.permisos.controller;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.service.ConstanciaPdfGenerador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "GenerarConstanciaPDFServlet", urlPatterns = {"/GenerarConstanciaPDFServlet"})
public class GenerarConstanciaPDFServlet extends HttpServlet {

    private ConstanciaDAO constanciaDAO;
    private EmpleadoDAO empleadoDAO;
    private ConstanciaPdfGenerador generador;

    @Override
    public void init() throws ServletException {
        super.init();
        constanciaDAO = new ConstanciaDAO();
        empleadoDAO = new EmpleadoDAO();
        generador = new ConstanciaPdfGenerador();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("empleado") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de constancia no proporcionado.");
            return;
        }

        try {
            int idConstancia = Integer.parseInt(idParam);
            Constancia constancia = constanciaDAO.obtenerPorId(idConstancia);

            if (constancia == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Constancia no encontrada.");
                return;
            }

            Empleado sesionEmpleado = (Empleado) session.getAttribute("empleado");
            Rol rolSesion = (Rol) session.getAttribute("rol");
            String nombreRolSesion = (rolSesion != null && rolSesion.getNombre() != null)
                    ? rolSesion.getNombre().toUpperCase() : "";
            boolean esRrhh = nombreRolSesion.contains("RRHH") || nombreRolSesion.contains("ADMIN");

            if (!esRrhh && constancia.getIdEmpleado() != sesionEmpleado.getIdEmpleado()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para ver esta constancia.");
                return;
            }

            if (!"APROBADO".equalsIgnoreCase(constancia.getEstado()) && !"APROBADA".equalsIgnoreCase(constancia.getEstado())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "La constancia no está aprobada.");
                return;
            }

            Empleado emp = empleadoDAO.buscarPorId(constancia.getIdEmpleado());

            String carpetaLogos = request.getServletContext().getRealPath("/img/logos");
            String urlVerificacion = request.getRequestURL().toString().replace(request.getRequestURI(), "")
                    + request.getContextPath() + "/verificar?token=" + constancia.getTokenVerificacion();

            byte[] pdfBytes = generador.generar(constancia, emp, carpetaLogos, urlVerificacion);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=constancia_" + idConstancia + ".pdf");
            response.setContentLength(pdfBytes.length);

            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de constancia inválido.");
        } catch (Exception e) {
            throw new ServletException("Error al generar el PDF de la constancia", e);
        }
    }
}