package com.permisos.controller;

import com.permisos.dao.IncapacidadDAO;
import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.Incapacidad;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;
import com.permisos.model.Usuario;
import com.permisos.dao.SolicitudDAO;
import com.permisos.service.ReglasVacaciones;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "IncapacidadServlet", urlPatterns = {"/incapacidad"})
public class IncapacidadServlet extends HttpServlet {
private static final Logger LOG = Logger.getLogger(IncapacidadServlet.class.getName());
    private static final String NOMBRE_TIPO_INCAPACIDAD = "INCAPACIDAD";

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();
    private final TipoSolicitudDAO tipoSolicitudDAO = new TipoSolicitudDAO();
    private final IncapacidadDAO incapacidadDAO = new IncapacidadDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuarioDeSesion(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/incapacidad.xhtml");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuarioDeSesion(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String numeroDocumento = request.getParameter("numeroDocumento");
        String inicioParam = request.getParameter("fechaInicio");
        String finParam = request.getParameter("fechaFin");
        String observaciones = request.getParameter("observaciones");

        StringBuilder errores = new StringBuilder();

        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            errores.append("Debe indicar el numero de documento (ISSS). ");
        }
        if (inicioParam == null || finParam == null || inicioParam.trim().isEmpty() || finParam.trim().isEmpty()) {
            errores.append("Debe indicar fecha de inicio y fin. ");
        }

        LocalDate inicio = null;
        LocalDate fin = null;
        int diasSolicitados = 0;

        if (inicioParam != null && finParam != null && !inicioParam.trim().isEmpty() && !finParam.trim().isEmpty()) {
            try {
                inicio = LocalDate.parse(inicioParam);
                fin = LocalDate.parse(finParam);

                if (fin.isBefore(inicio)) {
                    errores.append("La fecha fin no puede ser anterior al inicio. ");
                } else {
                    diasSolicitados = ReglasVacaciones.contarDiasLaborables(inicio, fin);
                    if (diasSolicitados == 0) {
                        errores.append("El rango seleccionado solo incluye domingos. Elija al menos un día de lunes a sábado. ");
                    }
                }
            } catch (DateTimeParseException e) {
                errores.append("Formato de fecha invalido. ");
            }
        }

        if (errores.length() > 0) {
            request.setAttribute("error", errores.toString());
            request.getRequestDispatcher("/vistas/incapacidad.jsp").forward(request, response);
            return;
        }

        try {
            TipoSolicitud tipo = tipoSolicitudDAO.buscarPorNombre(NOMBRE_TIPO_INCAPACIDAD);
            if (tipo == null) {
                request.setAttribute("error", "No se encontro el tipo de solicitud INCAPACIDAD en el catalogo.");
                request.getRequestDispatcher("/vistas/incapacidad.jsp").forward(request, response);
                return;
            }

            String numeroDocumentoLimpio = numeroDocumento.trim();

            Solicitud solicitud = new Solicitud();
            solicitud.setIdEmpleado(usuario.getIdEmpleado());
            solicitud.setIdTipoSolicitud(tipo.getIdTipoSolicitud());
            solicitud.setFechaInicio(inicio);
            solicitud.setFechaFin(fin);
            solicitud.setDiasSolicitados(diasSolicitados);
            solicitud.setMotivo("Incapacidad medica - documento " + numeroDocumentoLimpio);

            Incapacidad incapacidad = new Incapacidad();
            incapacidad.setNumeroDocumento(numeroDocumentoLimpio);
            incapacidad.setFechaInicio(inicio);
            incapacidad.setFechaFin(fin);
            incapacidad.setObservaciones(observaciones != null ? observaciones.trim() : null);

            incapacidadDAO.registrar(solicitud, incapacidad);

            response.sendRedirect(request.getContextPath() + "/solicitudes");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al registrar incapacidad", e);
            request.setAttribute("error", "Ocurrio un error al registrar la incapacidad.");
            request.getRequestDispatcher("/vistas/incapacidad.jsp").forward(request, response);
        }
    }

    private Usuario obtenerUsuarioDeSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    }
}