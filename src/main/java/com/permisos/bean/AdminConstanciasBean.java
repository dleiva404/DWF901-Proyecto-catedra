package com.permisos.bean;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bean de la bandeja de constancias de RRHH (admin-constancias.xhtml).
 * La búsqueda, el estado y la paginación se resuelven en memoria por AJAX.
 */
@ManagedBean(name = "adminConstanciasBean")
@ViewScoped
public class AdminConstanciasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int POR_PAGINA = 10;

    private final List<Fila> todas = new ArrayList<>();

    private String busqueda = "";
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
        if (!nombreRol.contains("RRHH") && !nombreRol.contains("ADMIN")) {
            redirigir(fc, "/solicitudes.xhtml");
            return;
        }

        try {
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            Map<Integer, String> nombres = new HashMap<>();
            for (Constancia c : new ConstanciaDAO().obtenerTodas()) {
                int idEmp = c.getIdEmpleado();
                String nombre = nombres.get(idEmp);
                if (nombre == null) {
                    Empleado e = empleadoDAO.buscarPorId(idEmp);
                    nombre = e == null ? "Empleado #" + idEmp
                            : (e.getNombre() + " " + e.getApellido()).trim();
                    nombres.put(idEmp, nombre);
                }
                todas.add(new Fila(c, nombre));
            }
        } catch (SQLException | RuntimeException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No fue posible consultar las constancias.", null));
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
        return "Mostrando " + getFilas().size() + " de " + total + (total == 1 ? " constancia" : " constancias");
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

    public int getPendientes() {
        return contar("pendiente");
    }

    public int getResueltas() {
        return todas.size() - getPendientes();
    }

    private int contar(String estadoClave) {
        int n = 0;
        for (Fila f : todas) {
            if (estadoClave.equals(f.estadoClave)) {
                n++;
            }
        }
        return n;
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
        private final String tipo;
        private final String institucion;
        private final String motivo;
        private final String fecha;
        private final String estadoClave;
        private final String textoBusqueda;

        Fila(Constancia c, String nombreEmpleado) {
            this.id = c.getIdConstancia();
            this.empleado = nombreEmpleado;
            this.tipo = c.getTipo() != null ? c.getTipo() : "";
            this.institucion = c.getInstitucion() != null ? c.getInstitucion() : "";
            this.motivo = c.getMotivo() != null ? c.getMotivo() : "";
            this.fecha = c.getFechaSolicitud() == null ? "" : Formato.fecha(c.getFechaSolicitud().toLocalDate());
            this.estadoClave = Formato.estado(c.getEstado()).toLowerCase(Locale.ROOT);
            this.textoBusqueda = (empleado + " " + institucion + " " + motivo + " " + tipo + " " + estadoClave)
                    .toLowerCase(Locale.ROOT);
        }

        public int getId() {
            return id;
        }

        public String getEmpleado() {
            return empleado;
        }

        public String getTipo() {
            return tipo;
        }

        public String getInstitucion() {
            return institucion;
        }

        public String getMotivo() {
            return motivo;
        }

        public String getFecha() {
            return fecha;
        }

        public String getClaseEstado() {
            return estadoClave;
        }

        public String getEstado() {
            return Formato.capitalizar(estadoClave);
        }
    }
}