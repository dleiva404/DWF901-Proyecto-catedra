package com.permisos.bean;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.dao.UsuarioDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.Usuario;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@ManagedBean(name = "verificarBean")
@RequestScoped
public class VerificarBean {

    private static final String SIN_TOKEN = "SIN_TOKEN";
    private static final String NO_ENCONTRADA = "NO_ENCONTRADA";
    private static final String NO_VIGENTE = "NO_VIGENTE";
    private static final String VALIDA = "VALIDA";

    private String resultado = NO_ENCONTRADA;
    private String codigo = "";
    private String empleado = "";
    private String tipo = "";
    private String empresa = "";
    private String institucion = "";
    private String cargo = "";
    private String fechaSolicitud = "";
    private String fechaAprobacion = "";
    private String aprobadaPor = "";
    private String observaciones = "";

    @PostConstruct
    public void init() {
        String token = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("token");

        if (token == null || token.trim().isEmpty()) {
            resultado = SIN_TOKEN;
            return;
        }
        codigo = token.trim();

        Constancia c;
        try {
            c = new ConstanciaDAO().obtenerPorToken(codigo);
        } catch (RuntimeException e) {
            c = null;
        }
        if (c == null) {
            resultado = NO_ENCONTRADA;
            return;
        }

        if (!"APROBADA".equals(Formato.estado(c.getEstado()))) {
            resultado = NO_VIGENTE;
            return;
        }

        int idEmp = c.getIdEmpleado();
        EmpleadoDAO empleadoDAO = new EmpleadoDAO();
        try {
            Empleado e = empleadoDAO.buscarPorId(idEmp);
            if (e != null) {
                empleado = (e.getNombre() + " " + e.getApellido()).trim();
                cargo = e.getCargo() == null ? "" : e.getCargo();
            }
        } catch (SQLException e) {
            empleado = "";
        }

        tipo = c.getTipo() == null ? "" : c.getTipo();
        empresa = c.getEmpresaEmisora() == null ? "" : c.getEmpresaEmisora();
        institucion = c.getInstitucion() == null ? "" : c.getInstitucion();
        observaciones = c.getObservacionesRrhh() == null ? "" : c.getObservacionesRrhh().trim();
        fechaSolicitud = c.getFechaSolicitud() == null ? ""
                : Formato.fecha(c.getFechaSolicitud().toLocalDate());
        fechaAprobacion = formatearFechaHora(c.getFechaRespuesta());
        aprobadaPor = buscarAprobador(c, empleadoDAO);
        resultado = VALIDA;
    }

    /** Nombre y cargo de quien aprobó la constancia, o vacío si no se puede determinar. */
    private String buscarAprobador(Constancia c, EmpleadoDAO empleadoDAO) {
        try {
            Integer idUsuario = c.getIdUsuarioRrhh();
            if (idUsuario == null) {
                return "";
            }
            Usuario u = new UsuarioDAO().buscarPorId(idUsuario);
            if (u == null) {
                return "";
            }
            String nombre = u.getUsername() == null ? "" : u.getUsername();
            String puesto = "";
            Empleado e = empleadoDAO.buscarPorId(u.getIdEmpleado());
            if (e != null) {
                nombre = (e.getNombre() + " " + e.getApellido()).trim();
                puesto = e.getCargo() == null ? "" : e.getCargo();
            }
            return puesto.isEmpty() ? nombre : nombre + " · " + puesto;
        } catch (SQLException | RuntimeException e) {
            return "";
        }
    }

    private static String formatearFechaHora(LocalDateTime f) {
        if (f == null) {
            return "";
        }
        return Formato.fecha(f.toLocalDate()) + ", " + f.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isValida() {
        return VALIDA.equals(resultado);
    }

    public String getTitulo() {
        switch (resultado) {
            case VALIDA:
                return "Constancia auténtica";
            case NO_VIGENTE:
                return "Constancia no vigente";
            case SIN_TOKEN:
                return "Falta el código de verificación";
            default:
                return "Constancia no encontrada";
        }
    }

    public String getMensaje() {
        switch (resultado) {
            case VALIDA:
                return "Este documento fue emitido por el sistema y coincide con nuestros registros.";
            case NO_VIGENTE:
                return "El código existe, pero la constancia no está aprobada, por lo que no es un documento válido.";
            case SIN_TOKEN:
                return "Escanea el código QR del documento para verificarlo.";
            default:
                return "No encontramos ninguna constancia con ese código. El documento podría no ser auténtico.";
        }
    }

    public String getClaseResultado() {
        return isValida() ? "verif-ok" : "verif-mal";
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEmpleado() {
        return empleado;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public String getInstitucion() {
        return institucion;
    }

    public String getCargo() {
        return cargo;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getFechaAprobacion() {
        return fechaAprobacion;
    }

    public String getAprobadaPor() {
        return aprobadaPor;
    }

    public boolean isHayAprobador() {
        return !aprobadaPor.isEmpty();
    }

    public String getObservaciones() {
        return observaciones;
    }

    public boolean isHayObservaciones() {
        return !observaciones.isEmpty();
    }
}