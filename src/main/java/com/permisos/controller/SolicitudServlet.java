package com.permisos.controller;

import com.permisos.dao.SolicitudDAO;
import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.dao.VacacionesEmpleadoDAO;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;
import com.permisos.model.Usuario;
import com.permisos.model.VacacionesEmpleado;

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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del flujo de EMPLEADO: crear solicitud (vacaciones o ausencia;
 * incapacidad tiene su propio Servlet aparte por el archivo adjunto) y
 * consultar su historial de solicitudes.
 *
 * GET  -> muestra el formulario y el historial.
 * POST -> valida y registra una nueva solicitud.
 */
@WebServlet(name = "SolicitudServlet", urlPatterns = {"/solicitudes"})
public class SolicitudServlet extends HttpServlet {

    private static final String NOMBRE_TIPO_VACACIONES = "VACACIONES";
    private static final String NOMBRE_TIPO_INCAPACIDAD = "INCAPACIDAD";

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();
    private final TipoSolicitudDAO tipoSolicitudDAO = new TipoSolicitudDAO();
    private final VacacionesEmpleadoDAO vacacionesDAO = new VacacionesEmpleadoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuarioDeSesion(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            registrarSolicitud(request, response, usuario);
            return;
        }

        mostrarFormularioEHistorial(request, response, usuario);
    }

    private Usuario obtenerUsuarioDeSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    }

    private void mostrarFormularioEHistorial(HttpServletRequest request, HttpServletResponse response,
                                             Usuario usuario) throws ServletException, IOException {
        try {
            List<Solicitud> historial = solicitudDAO.listarPorEmpleado(usuario.getIdEmpleado());
            List<TipoSolicitud> tipos = tipoSolicitudDAO.listarActivos();
            VacacionesEmpleado saldo = vacacionesDAO.buscarPorEmpleadoYAnio(
                    usuario.getIdEmpleado(), LocalDate.now().getYear());

            request.setAttribute("historial", historial);
            request.setAttribute("tipos", tipos);
            request.setAttribute("saldoVacaciones", saldo);
            // Mapa id -> nombre de tipo, para que el JSP pueda mostrar el
            // nombre del tipo de cada solicitud del historial sin tener
            // que guardar ese texto duplicado en la tabla `solicitudes`.
            request.setAttribute("tiposPorId", construirMapaTipos(tipos));

            request.getRequestDispatcher("/vistas/solicitudes.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "No se pudieron cargar los datos.");
            request.getRequestDispatcher("/vistas/error.jsp").forward(request, response);
        }
    }

    private void registrarSolicitud(HttpServletRequest request, HttpServletResponse response, Usuario usuario)
            throws ServletException, IOException {

        String tipoParam = request.getParameter("idTipoSolicitud");
        String inicioParam = request.getParameter("fechaInicio");
        String finParam = request.getParameter("fechaFin");
        String motivo = request.getParameter("motivo");

        StringBuilder errores = new StringBuilder();

        if (tipoParam == null || tipoParam.trim().isEmpty()) {
            errores.append("Debe seleccionar un tipo de solicitud. ");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            errores.append("Debe indicar el motivo. ");
        }
        if (inicioParam == null || finParam == null || inicioParam.isEmpty() || finParam.isEmpty()) {
            errores.append("Debe indicar fecha de inicio y fin. ");
        }

        LocalDate inicio = null, fin = null;
        int diasSolicitados = 0;

        if (errores.length() == 0) {
            try {
                inicio = LocalDate.parse(inicioParam);
                fin = LocalDate.parse(finParam);

                if (fin.isBefore(inicio)) {
                    errores.append("La fecha fin no puede ser anterior al inicio. ");
                } else if (inicio.isBefore(LocalDate.now())) {
                    errores.append("No se pueden solicitar fechas pasadas. ");
                } else {
                    diasSolicitados = (int) (ChronoUnit.DAYS.between(inicio, fin) + 1);
                }
            } catch (DateTimeParseException e) {
                errores.append("Formato de fecha inválido. ");
            }
        }

        try {
            int idTipo = tipoParam != null && !tipoParam.trim().isEmpty() ? Integer.parseInt(tipoParam) : -1;
            TipoSolicitud tipoSeleccionado = idTipo != -1 ? tipoSolicitudDAO.buscarPorId(idTipo) : null;

            // La incapacidad se gestiona en su propio formulario (con archivo adjunto)
            if (errores.length() == 0 && tipoSeleccionado != null
                    && NOMBRE_TIPO_INCAPACIDAD.equals(tipoSeleccionado.getNombre())) {
                response.sendRedirect(request.getContextPath() + "/incapacidad");
                return;
            }

            // Regla de negocio: si es VACACIONES, validar contra el saldo disponible
            if (errores.length() == 0 && tipoSeleccionado != null
                    && NOMBRE_TIPO_VACACIONES.equals(tipoSeleccionado.getNombre())) {

                VacacionesEmpleado saldo = vacacionesDAO.buscarPorEmpleadoYAnio(
                        usuario.getIdEmpleado(), inicio.getYear());

                if (saldo == null || saldo.getDiasDisponibles() < diasSolicitados) {
                    errores.append("No tiene suficientes días de vacaciones disponibles. ");
                }
            }

            if (errores.length() > 0) {
                reenviarConError(request, response, usuario, errores.toString());
                return;
            }

            Solicitud nueva = new Solicitud();
            nueva.setIdEmpleado(usuario.getIdEmpleado());
            nueva.setIdTipoSolicitud(idTipo);
            nueva.setFechaInicio(inicio);
            nueva.setFechaFin(fin);
            nueva.setDiasSolicitados(diasSolicitados);
            nueva.setMotivo(motivo.trim());

            solicitudDAO.insertar(nueva);

            response.sendRedirect(request.getContextPath() + "/solicitudes");

        } catch (NumberFormatException | SQLException e) {
            reenviarConError(request, response, usuario, "Ocurrió un error al registrar la solicitud.");
        }
    }

    private void reenviarConError(HttpServletRequest request, HttpServletResponse response,
                                  Usuario usuario, String mensaje) throws ServletException, IOException {
        try {
            List<TipoSolicitud> tipos = tipoSolicitudDAO.listarActivos();

            request.setAttribute("error", mensaje);
            request.setAttribute("historial", solicitudDAO.listarPorEmpleado(usuario.getIdEmpleado()));
            request.setAttribute("tipos", tipos);
            request.setAttribute("tiposPorId", construirMapaTipos(tipos));
            request.setAttribute("saldoVacaciones", vacacionesDAO.buscarPorEmpleadoYAnio(
                    usuario.getIdEmpleado(), LocalDate.now().getYear()));
            request.getRequestDispatcher("/vistas/solicitudes.jsp").forward(request, response);
        } catch (SQLException e) {
            request.getRequestDispatcher("/vistas/error.jsp").forward(request, response);
        }
    }

    private Map<Integer, String> construirMapaTipos(List<TipoSolicitud> tipos) {
        Map<Integer, String> mapa = new HashMap<>();
        for (TipoSolicitud t : tipos) {
            mapa.put(t.getIdTipoSolicitud(), t.getNombre());
        }
        return mapa;
    }
}
