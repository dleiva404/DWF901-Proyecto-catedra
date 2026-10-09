package com.permisos.bean;

import com.permisos.dao.SolicitudDAO;
import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;
import com.permisos.model.Usuario;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Bean de la vista "Historial" (historial.xhtml).
 * Carga las solicitudes del empleado una vez y filtra/pagina en memoria,
 * de modo que los filtros responden por AJAX sin recargar la página.
 */
@ManagedBean(name = "historialBean")
@ViewScoped
public class HistorialBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int POR_PAGINA = 10;
    private static final String[] MESES = {"ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic"};

    private final List<Fila> todas = new ArrayList<>();
    private final List<SelectItem> tipos = new ArrayList<>();

    private String busqueda = "";
    private String tipoFiltro = "todos";
    private String estadoFiltro = "todas";
    private int pagina = 1;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        Usuario usuario = (Usuario) ec.getSessionMap().get("usuario");
        if (usuario == null) {
            try {
                ec.redirect(ec.getRequestContextPath() + "/login");
                fc.responseComplete();
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo redirigir al login", e);
            }
            return;
        }

        try {
            tipos.add(new SelectItem("todos", "Todos"));
            List<TipoSolicitud> activos = new ArrayList<>(new TipoSolicitudDAO().listarActivos());
            Collections.sort(activos, new Comparator<TipoSolicitud>() {
                @Override
                public int compare(TipoSolicitud a, TipoSolicitud b) {
                    return Integer.compare(orden(a.getNombre()), orden(b.getNombre()));
                }
            });
            for (TipoSolicitud t : activos) {
                tipos.add(new SelectItem(t.getNombre().toUpperCase(Locale.ROOT), capitalizar(t.getNombre())));
            }

            for (Solicitud s : new SolicitudDAO().obtenerHistorialPorTipo(usuario.getIdEmpleado(), "todos")) {
                todas.add(new Fila(s));
            }
        } catch (SQLException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al cargar el historial de solicitudes.", null));
        }
    }

    private List<Fila> filtradas() {
        String q = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        List<Fila> resultado = new ArrayList<>();
        for (Fila f : todas) {
            if (!"todos".equals(tipoFiltro) && !f.tipoClave.equals(tipoFiltro)) {
                continue;
            }
            if (!"todas".equals(estadoFiltro) && !f.estadoClave.equals(estadoFiltro)) {
                continue;
            }
            if (!q.isEmpty() && !f.motivoMinuscula.contains(q)) {
                continue;
            }
            resultado.add(f);
        }
        return resultado;
    }

    public List<Fila> getFilas() {
        List<Fila> lista = filtradas();
        int desde = (pagina - 1) * POR_PAGINA;
        if (desde >= lista.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(lista.subList(desde, Math.min(desde + POR_PAGINA, lista.size())));
    }

    public boolean isHayFilas() {
        return !filtradas().isEmpty();
    }

    public String getConteo() {
        int total = filtradas().size();
        int enPagina = getFilas().size();
        return "Mostrando " + enPagina + " de " + total + (total == 1 ? " solicitud" : " solicitudes");
    }

    public int getTotalPaginas() {
        return Math.max(1, (int) Math.ceil(filtradas().size() / (double) POR_PAGINA));
    }

    public int getPagina() {
        return pagina;
    }

    public boolean isHayAnterior() {
        return pagina > 1;
    }

    public boolean isHaySiguiente() {
        return pagina < getTotalPaginas();
    }

    public boolean isHayPaginacion() {
        return getTotalPaginas() > 1;
    }

    public String getTextoPagina() {
        return "Página " + pagina + " de " + getTotalPaginas();
    }

    public void anterior() {
        if (pagina > 1) {
            pagina--;
        }
    }

    public void siguiente() {
        if (pagina < getTotalPaginas()) {
            pagina++;
        }
    }

    public List<SelectItem> getTipos() {
        return tipos;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        if (!equalsSeguro(this.busqueda, busqueda)) {
            pagina = 1;
        }
        this.busqueda = busqueda;
    }

    public String getTipoFiltro() {
        return tipoFiltro;
    }

    public void setTipoFiltro(String tipoFiltro) {
        if (!equalsSeguro(this.tipoFiltro, tipoFiltro)) {
            pagina = 1;
        }
        this.tipoFiltro = tipoFiltro;
    }

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        if (!equalsSeguro(this.estadoFiltro, estadoFiltro)) {
            pagina = 1;
        }
        this.estadoFiltro = estadoFiltro;
    }

    private static boolean equalsSeguro(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static int orden(String nombre) {
        if ("VACACIONES".equals(nombre)) {
            return 0;
        }
        if ("INCAPACIDAD".equals(nombre)) {
            return 2;
        }
        return 1;
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

    private static String normalizarEstado(String estado) {
        if (estado == null) {
            return "PENDIENTE";
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
        return "PENDIENTE";
    }

    /** Fila lista para mostrar en la tabla del historial. */
    public static class Fila implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String tipo;
        private final String tipoClave;
        private final String inicio;
        private final String fin;
        private final int dias;
        private final String motivo;
        private final String motivoMinuscula;
        private final String estadoClave;
        private final String motivoRechazo;

        Fila(Solicitud s) {
            String nombreTipo = s.getTipo() != null ? s.getTipo() : "";
            this.tipo = capitalizar(nombreTipo);
            this.tipoClave = nombreTipo.toUpperCase(Locale.ROOT);
            this.inicio = formatearFecha(s.getFechaInicio());
            this.fin = formatearFecha(s.getFechaFin());
            this.dias = s.getDiasSolicitados();
            this.motivo = s.getMotivo() != null ? s.getMotivo() : "";
            this.motivoMinuscula = this.motivo.toLowerCase(Locale.ROOT);
            this.estadoClave = normalizarEstado(s.getEstado()).toLowerCase(Locale.ROOT);
            this.motivoRechazo = s.getMotivoRechazo() != null ? s.getMotivoRechazo().trim() : "";
        }

        public String getTipo() {
            return tipo;
        }

        public String getInicio() {
            return inicio;
        }

        public String getFin() {
            return fin;
        }

        public int getDias() {
            return dias;
        }

        public String getMotivo() {
            return motivo;
        }

        public String getClaseEstado() {
            return estadoClave;
        }

        public String getEstado() {
            return capitalizar(estadoClave);
        }

        public String getMotivoRechazo() {
            return motivoRechazo.isEmpty() ? "—" : motivoRechazo;
        }

        public boolean isConMotivoRechazo() {
            return !motivoRechazo.isEmpty();
        }
    }
}