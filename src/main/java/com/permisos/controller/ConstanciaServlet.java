package com.permisos.controller;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.SucursalAreaDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.SucursalArea;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "ConstanciaServlet", urlPatterns = {"/ConstanciaServlet"})
public class ConstanciaServlet extends HttpServlet {

    private ConstanciaDAO constanciaDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        constanciaDAO = new ConstanciaDAO();
    }

        @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("empleado") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/constancias.xhtml");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("empleado") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        try {
            String tipoConstancia = request.getParameter("tipoConstancia");
            String institucion = request.getParameter("institucion");
            String motivo = request.getParameter("motivo");

            // Determinamos la empresa o sucursal emisora de forma dinámica
            String empresaEmisoraFinal;

            if (empleado.getEmpresa() != null && !empleado.getEmpresa().trim().isEmpty()) {
                // Si pertenece a una empresa afiliada (Copro, Steel, Inver Calma, etc.)
                empresaEmisoraFinal = empleado.getEmpresa();
            } else {
                // Si es de Didelco, usamos tu SucursalAreaDAO existente con buscarPorId
                SucursalAreaDAO sucursalDAO = new SucursalAreaDAO();
                SucursalArea sucursal = sucursalDAO.buscarPorId(empleado.getIdSucursalArea());

                String nombreSucursal = (sucursal != null) ? sucursal.getNombre() : null;
                empresaEmisoraFinal = "Didelco " + (nombreSucursal != null ? nombreSucursal : "Central");
            }

            // Creamos el objeto Constancia con los datos recolectados
            Constancia nuevaConstancia = new Constancia();
            nuevaConstancia.setIdEmpleado(empleado.getIdEmpleado());
            nuevaConstancia.setTipo(tipoConstancia);
            nuevaConstancia.setInstitucion(institucion);
            nuevaConstancia.setMotivo(motivo);
            nuevaConstancia.setSalarioReferencia(BigDecimal.ZERO);
            nuevaConstancia.setEmpresaEmisora(empresaEmisoraFinal);
            nuevaConstancia.setTokenVerificacion(UUID.randomUUID().toString().substring(0, 15));

            boolean exito = constanciaDAO.insertar(nuevaConstancia);

            if (exito) {
                response.sendRedirect(request.getContextPath() + "/ConstanciaServlet?exito=true");
                return;
            } else {
                request.setAttribute("error", "No se pudo registrar la solicitud en la base de datos.");
            }

            doGet(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "Ocurrió un error al procesar la solicitud: " + e.getMessage());
            doGet(request, response);
        }
    }
}