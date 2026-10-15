package com.permisos.bean;

import com.permisos.dao.SolicitudDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Solicitud;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bean de la bandeja de solicitudes (jefatura.xhtml).
 * Jefatura ve las pendientes de su equipo; RRHH y ADMIN ven todas.
 * La búsqueda, el tipo, el estado y la paginación se resuelven en memoria por AJAX.
 */
@ManagedBean(name = "jefaturaBean")
@ViewScoped
public class JefaturaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int POR_PAGINA = 10;

    private final List<Fila> todas = new ArrayList<>();
    private final List<SelectItem> tipos = new ArrayList<>();
    private boolean verTodas;

    private String busqueda = "";
    private String tipoFiltro = "todos";
    private String estadoFiltro = "todas";
    private int pagina = 1;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        Empleado empleado = (Empleado) ec.getSessionMap().get("empleado");
        Rol rol = (Rol) ec.getSessionMap().get("rol");

        if (empleado == null || rol == null) {
            redirigir(fc, "/login");
            return;
        }

        String nombreRol = rol.getNombre() == null ? "" : rol.getNombre().toUpperCase(Locale.ROOT);
        verTodas = nombreRol.contains("RRHH") || nombreRol.contains("ADMIN");

        if (!verTodas && !nombreRol.contains("JEFATURA")) {
            redirigir(fc, "/solicitudes.xhtml");
            return;
        }

        try {
            SolicitudDAO dao = new SolicitudDAO();
            List<Solicitud> lista = verTodas
                    ? dao.listarTodasGlobal()
                    : dao.listarPendientesPorJefatura(empleado.getIdEmpleado());

            Map<String, String> tiposEncontrados = new LinkedHashMap<>();
            for (Solicitud s : lista) {
                todas.add(new Fila(s));
                String clave = s.getTipo() == null ? "" : s.getTipo().toUpperCase(Locale.ROOT);
                if (!clave.isEmpty()) {
                    tiposEncontrados.put(clave, Formato.capitalizar(clave));
                }
            }
            tipos.add(new SelectItem("todos", "Todos"));
            for (Map.Entry<String, String> e : tiposEncontrados.entrySet()) {
                tipos.add(new SelectItem(e.getKey(), e.getValue()));
            }
        } catch (SQLException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No fue posible consultar las solicitudes.", null));
        }
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
            if (!q.isEmpty() && !f.textoBusqueda.contains(q)) {
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
        return "Mostrando " + getFilas().size() + " de " + total + (total == 1 ? " solicitud" : " solicitudes");
    }

    public int getTotalPaginas() {
        return Math.max(1, (int) Math.ceil(filtradas().size() / (double) POR_PAGINA));
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

    public boolean isVerTodas() {
        return verTodas;
    }

    public String getSubtitulo() {
        return verTodas
                ? "Todas las solicitudes de los empleados."
                : "Solicitudes de tu equipo que esperan tu revisión.";
    }

    public int getPendientes() {
        int n = 0;
        for (Fila f : todas) {
            if ("pendiente".equals(f.estadoClave)) {
                n++;
            }
        }
        return n;
    }

    public int getDiasPendientes() {
        int n = 0;
        for (Fila f : todas) {
            if ("pendiente".equals(f.estadoClave)) {
                n += f.dias;
            }
        }
        return n;
    }

    public List<SelectItem> getTipos() {
        return tipos;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        if (!igual(this.busqueda, busqueda)) {
            pagina = 1;
        }
        this.busqueda = busqueda;
    }

    public String getTipoFiltro() {
        return tipoFiltro;
    }

    public void setTipoFiltro(String tipoFiltro) {
        if (!igual(this.tipoFiltro, tipoFiltro)) {
            pagina = 1;
        }
        this.tipoFiltro = tipoFiltro;
    }

    public String getEstadoFiltro() {
        return estadoFiltro;
    }

    public void setEstadoFiltro(String estadoFiltro) {
        if (!igual(this.estadoFiltro, estadoFiltro)) {
            pagina = 1;
        }
        this.estadoFiltro = estadoFiltro;
    }

    private static boolean igual(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    /** Fila lista para mostrar en la tabla. */
    public static class Fila implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int id;
        private final String empleado;
        private final String empresa;
        private final String tipo;
        private final String tipoClave;
        private final String rango;
        private final int dias;
        private final String motivo;
        private final String estadoClave;
        private final String textoBusqueda;

        Fila(Solicitud s) {
            this.id = s.getIdSolicitud();
            this.empleado = s.getNombreEmpleado() != null ? s.getNombreEmpleado() : "";
            this.empresa = s.getNombreEmpresa() != null ? s.getNombreEmpresa() : "";
            String nombreTipo = s.getTipo() != null ? s.getTipo() : "";
            this.tipo = Formato.capitalizar(nombreTipo);
            this.tipoClave = nombreTipo.toUpperCase(Locale.ROOT);
            this.rango = Formato.rango(s.getFechaInicio(), s.getFechaFin());
            this.dias = s.getDiasSolicitados();
            this.motivo = s.getMotivo() != null ? s.getMotivo() : "";
            this.estadoClave = Formato.estado(s.getEstado()).toLowerCase(Locale.ROOT);
            this.textoBusqueda = (empleado + " " + empresa + " " + motivo + " " + estadoClave)
                    .toLowerCase(Locale.ROOT);
        }

        public int getId() {
            return id;
        }

        public String getEmpleado() {
            return empleado;
        }

        public String getEmpresa() {
            return empresa;
        }

        public String getTipo() {
            return tipo;
        }

        public String getRango() {
            return rango;
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
            return Formato.capitalizar(estadoClave);
        }
    }
}