package com.permisos.bean;

import com.permisos.dao.SolicitudDAO;
import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.dao.VacacionesEmpleadoDAO;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;
import com.permisos.model.Usuario;
import com.permisos.model.VacacionesEmpleado;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.SolicitudService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bean de la vista "Mis solicitudes" (solicitudes.xhtml).
 * Reutiliza los DAO y las reglas de negocio que ya existían en SolicitudServlet.
 */
@ManagedBean(name = "solicitudesBean")
@ViewScoped
public class SolicitudesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String TIPO_VACACIONES = "VACACIONES";
    private static final String TIPO_INCAPACIDAD = "INCAPACIDAD";
    private static final String[] MESES = {"ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final int MAX_ACTIVIDAD = 4;

    private Usuario usuario;
    private VacacionesEmpleado saldo;
    private final List<ActividadItem> actividad = new ArrayList<>();
    private final List<SelectItem> tipos = new ArrayList<>();
    private final Map<Integer, String> nombresTipo = new HashMap<>();

    private int pendientes;
    private int aprobadasAnio;
    private String ultimaAprobada;
    private int diasPendientesVacaciones;

    private Integer idTipo;
    private String fechaInicio;
    private String fechaFin;
    private String motivo;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        usuario = (Usuario) ec.getSessionMap().get("usuario");
        if (usuario == null) {
            redirigir(fc, "/login");
            return;
        }
        cargar();
    }

    private void cargar() {
        try {
            tipos.clear();
            nombresTipo.clear();
            List<TipoSolicitud> activos = new ArrayList<>(new TipoSolicitudDAO().listarActivos());
            Collections.sort(activos, new Comparator<TipoSolicitud>() {
                @Override
                public int compare(TipoSolicitud a, TipoSolicitud b) {
                    return Integer.compare(ordenTipo(a.getNombre()), ordenTipo(b.getNombre()));
                }
            });
            for (TipoSolicitud t : activos) {
                nombresTipo.put(t.getIdTipoSolicitud(), t.getNombre());
                tipos.add(new SelectItem(t.getIdTipoSolicitud(), capitalizar(t.getNombre())));
                if (idTipo == null && TIPO_VACACIONES.equals(t.getNombre())) {
                    idTipo = t.getIdTipoSolicitud();
                }
            }

            int anio = LocalDate.now().getYear();
            saldo = new VacacionesEmpleadoDAO().buscarPorEmpleadoYAnio(usuario.getIdEmpleado(), anio);

            actividad.clear();
            pendientes = 0;
            aprobadasAnio = 0;
            ultimaAprobada = null;
            diasPendientesVacaciones = 0;

            List<Solicitud> todas = new SolicitudDAO().listarPorEmpleado(usuario.getIdEmpleado());
            for (Solicitud s : todas) {
                String estado = normalizarEstado(s.getEstado());
                String tipo = nombresTipo.get(s.getIdTipoSolicitud());

                if ("PENDIENTE".equals(estado)) {
                    pendientes++;
                    if (TIPO_VACACIONES.equals(tipo) && s.getFechaInicio().getYear() == anio) {
                        diasPendientesVacaciones += s.getDiasSolicitados();
                    }
                } else if ("APROBADA".equals(estado) && s.getFechaInicio().getYear() == anio) {
                    aprobadasAnio++;
                    if (ultimaAprobada == null) {
                        LocalDate f = s.getFechaRespuesta() != null
                                ? s.getFechaRespuesta().toLocalDate() : s.getFechaInicio();
                        ultimaAprobada = formatearFecha(f);
                    }
                }

                if (actividad.size() < MAX_ACTIVIDAD) {
                    actividad.add(new ActividadItem(
                            capitalizar(tipo != null ? tipo : "Solicitud"),
                            estado,
                            rango(s.getFechaInicio(), s.getFechaFin()),
                            s.getDiasSolicitados(),
                            s.getMotivoRechazo()));
                }
            }
        } catch (SQLException e) {
            error("No se pudieron cargar los datos.");
        }
    }

    public String enviar() {
        if (usuario == null) {
            return null;
        }

        String nombreTipo = idTipo != null ? nombresTipo.get(idTipo) : null;

        if (TIPO_INCAPACIDAD.equals(nombreTipo)) {
            redirigir(FacesContext.getCurrentInstance(), "/incapacidad.xhtml");
            return null;
        }

        List<String> errores = new ArrayList<>();
        if (idTipo == null) {
            errores.add("Debe seleccionar un tipo de solicitud.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            errores.add("Debe indicar el motivo.");
        }

        LocalDate inicio = parsear(fechaInicio);
        LocalDate fin = parsear(fechaFin);
        int dias = 0;

        if (isVacio(fechaInicio) || isVacio(fechaFin)) {
            errores.add("Debe indicar fecha de inicio y fin.");
        } else if (inicio == null || fin == null) {
            errores.add("Formato de fecha inválido.");
        } else if (fin.isBefore(inicio)) {
            errores.add("La fecha fin no puede ser anterior al inicio.");
        } else if (inicio.isBefore(LocalDate.now())) {
            errores.add("No se pueden solicitar fechas pasadas.");
        } else {
            dias = (int) (ChronoUnit.DAYS.between(inicio, fin) + 1);
        }

        if (errores.isEmpty() && TIPO_VACACIONES.equals(nombreTipo)) {
            try {
                new SolicitudService().validarVacaciones(usuario.getIdEmpleado(), inicio, fin);
            } catch (ReglaNegocioException e) {
                errores.add(e.getMessage());
            } catch (SQLException e) {
                errores.add("No se pudo validar el saldo de vacaciones.");
            }
        }

        if (!errores.isEmpty()) {
            for (String e : errores) {
                error(e);
            }
            return null;
        }

        try {
            Solicitud nueva = new Solicitud();
            nueva.setIdEmpleado(usuario.getIdEmpleado());
            nueva.setIdTipoSolicitud(idTipo);
            nueva.setFechaInicio(inicio);
            nueva.setFechaFin(fin);
            nueva.setDiasSolicitados(dias);
            nueva.setMotivo(motivo.trim());
            new SolicitudDAO().insertar(nueva);
        } catch (SQLException e) {
            error("Ocurrió un error al registrar la solicitud.");
            return null;
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getExternalContext().getFlash().setKeepMessages(true);
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Solicitud enviada a tu jefatura.", null));
        return "solicitudes?faces-redirect=true";
    }

    public boolean isHayHero() {
        return saldo != null;
    }

    public int getAnio() {
        return LocalDate.now().getYear();
    }

    public int getDiasDisponibles() {
        return saldo != null ? saldo.getDiasDisponibles() : 0;
    }

    public int getDiasAsignados() {
        return saldo != null ? saldo.getDiasAsignados() : 0;
    }

    public String getHeroTitulo() {
        int d = getDiasDisponibles();
        return "Te quedan " + d + (d == 1 ? " día" : " días");
    }

    public String getHeroDetalle() {
        if (saldo == null) {
            return "";
        }
        String texto = "Has usado " + saldo.getDiasUtilizados() + " de tus " + saldo.getDiasAsignados() + " días.";
        if (diasPendientesVacaciones > 0) {
            texto += " Tienes " + diasPendientesVacaciones + (diasPendientesVacaciones == 1 ? " día" : " días")
                    + " en solicitudes pendientes que aún no se descuentan.";
        }
        return texto;
    }

    public String getDashArray() {
        double fraccion = getDiasAsignados() > 0
                ? Math.max(0, Math.min(1, (double) getDiasDisponibles() / getDiasAsignados())) : 0;
        return String.format(Locale.US, "%.1f 301.6", 301.6 * fraccion);
    }

    public String getResumen() {
        String nombreTipo = idTipo != null ? nombresTipo.get(idTipo) : null;
        if (TIPO_INCAPACIDAD.equals(nombreTipo)) {
            return "Las incapacidades se registran con el número de documento del ISSS. "
                    + "Al continuar te llevaremos a ese formulario.";
        }
        LocalDate inicio = parsear(fechaInicio);
        LocalDate fin = parsear(fechaFin);
        if (inicio == null || fin == null) {
            return "Elige las fechas para calcular los días.";
        }
        if (fin.isBefore(inicio)) {
            return "La fecha fin no puede ser anterior al inicio.";
        }
        int dias = (int) (ChronoUnit.DAYS.between(inicio, fin) + 1);
        String textoDias = "<strong>" + dias + (dias == 1 ? " día" : " días") + "</strong>";

        if (TIPO_VACACIONES.equals(nombreTipo) && saldo != null) {
            int libres = saldo.getDiasDisponibles() - diasPendientesVacaciones;
            if (dias > libres) {
                return "Pides " + textoDias + " pero solo tienes <strong>" + Math.max(libres, 0)
                        + "</strong> libres después de tus solicitudes pendientes.";
            }
            return "Se descontarán " + textoDias + " de tus " + saldo.getDiasDisponibles() + " disponibles.";
        }
        return "La solicitud es por " + textoDias + ".";
    }

    public String getResumenClase() {
        String r = getResumen();
        return r.contains("pero solo tienes") || r.startsWith("La fecha fin")
                ? "rh-resumen rh-resumen-alerta" : "rh-resumen";
    }

    public int getPendientes() {
        return pendientes;
    }

    public int getAprobadasAnio() {
        return aprobadasAnio;
    }

    public String getUltimaAprobada() {
        return ultimaAprobada != null ? "Última: " + ultimaAprobada : "Aún no tienes aprobadas";
    }

    public List<ActividadItem> getActividad() {
        return actividad;
    }

    public List<SelectItem> getTipos() {
        return tipos;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public boolean isIncapacidad() {
        return idTipo != null && TIPO_INCAPACIDAD.equals(nombresTipo.get(idTipo));
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    private void redirigir(FacesContext fc, String ruta) {
        try {
            ExternalContext ec = fc.getExternalContext();
            ec.redirect(ec.getRequestContextPath() + ruta);
            fc.responseComplete();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo redirigir a " + ruta, e);
        }
    }

    private void error(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    private static int ordenTipo(String nombre) {
        if (TIPO_VACACIONES.equals(nombre)) {
            return 0;
        }
        if (TIPO_INCAPACIDAD.equals(nombre)) {
            return 2;
        }
        return 1;
    }

    private static boolean isVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static LocalDate parsear(String s) {
        if (isVacio(s)) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String normalizarEstado(String estado) {
        if (estado == null) {
            return "";
        }
        String e = estado.toUpperCase(Locale.ROOT);
        if (e.startsWith("APROBAD")) {
            return "APROBADA";
        }
        if (e.startsWith("RECHAZAD")) {
            return "RECHAZADA";
        }
        if (e.startsWith("CANCELAD")) {
            return "CANCELADA";
        }
        return e;
    }

    private static String capitalizar(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String formatearFecha(LocalDate f) {
        return f.getDayOfMonth() + " " + MESES[f.getMonthValue() - 1] + " " + f.getYear();
    }

    private static String rango(LocalDate ini, LocalDate fin) {
        if (ini.equals(fin)) {
            return formatearFecha(ini);
        }
        if (ini.getYear() != fin.getYear()) {
            return formatearFecha(ini) + " – " + formatearFecha(fin);
        }
        if (ini.getMonthValue() == fin.getMonthValue()) {
            return ini.getDayOfMonth() + " – " + fin.getDayOfMonth() + " "
                    + MESES[fin.getMonthValue() - 1] + " " + fin.getYear();
        }
        return ini.getDayOfMonth() + " " + MESES[ini.getMonthValue() - 1] + " – "
                + fin.getDayOfMonth() + " " + MESES[fin.getMonthValue() - 1] + " " + fin.getYear();
    }

    /** Datos ya formateados de una solicitud para las tarjetas de actividad reciente. */
    public static class ActividadItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String tipo;
        private final String estado;
        private final String rango;
        private final int dias;
        private final String motivoRechazo;

        ActividadItem(String tipo, String estado, String rango, int dias, String motivoRechazo) {
            this.tipo = tipo;
            this.estado = estado;
            this.rango = rango;
            this.dias = dias;
            this.motivoRechazo = motivoRechazo;
        }

        public String getTipo() {
            return tipo;
        }

        public String getEstado() {
            return capitalizar(estado);
        }

        public String getClaseEstado() {
            return estado.toLowerCase(Locale.ROOT);
        }

        public String getRango() {
            return rango;
        }

        public String getDiasTexto() {
            return dias + (dias == 1 ? " día" : " días");
        }

        public boolean isEnProceso() {
            return "PENDIENTE".equals(estado) || "APROBADA".equals(estado);
        }

        public String getEtiquetaFinal() {
            return "APROBADA".equals(estado) ? "Aprobada" : "Jefatura";
        }

        public boolean isRechazadaConMotivo() {
            return "RECHAZADA".equals(estado) && motivoRechazo != null && !motivoRechazo.trim().isEmpty();
        }

        public String getMotivoRechazo() {
            return motivoRechazo;
        }
    }
}