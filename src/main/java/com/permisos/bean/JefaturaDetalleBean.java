package com.permisos.bean;

import com.permisos.dao.SolicitudDAO;
import com.permisos.dao.VacacionesEmpleadoDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Solicitud;
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
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Bean del detalle de una solicitud (jefatura-detalle.xhtml?id=N).
 * Permite a la jefatura aprobar (con las reglas de SolicitudService) o rechazar con motivo.
 */
@ManagedBean(name = "jefaturaDetalleBean")
@ViewScoped
public class JefaturaDetalleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario;
    private Empleado empleado;
    private boolean soloLectura;

    private Solicitud solicitud;
    private String saldoTexto = "";
    private String motivoRechazo;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        usuario = (Usuario) ec.getSessionMap().get("usuario");
        empleado = (Empleado) ec.getSessionMap().get("empleado");
        Rol rol = (Rol) ec.getSessionMap().get("rol");

        if (usuario == null || empleado == null || rol == null) {
            redirigir(fc, "/login");
            return;
        }

        String nombreRol = rol.getNombre() == null ? "" : rol.getNombre().toUpperCase(Locale.ROOT);
        soloLectura = nombreRol.contains("RRHH") || nombreRol.contains("ADMIN");
        if (!soloLectura && !nombreRol.contains("JEFATURA")) {
            redirigir(fc, "/solicitudes.xhtml");
            return;
        }

        String id = ec.getRequestParameterMap().get("id");
        try {
            solicitud = new SolicitudDAO().buscarPorId(Integer.parseInt(id == null ? "" : id.trim()));
        } catch (NumberFormatException e) {
            solicitud = null;
        } catch (SQLException e) {
            solicitud = null;
        }

        if (solicitud == null) {
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "La solicitud indicada no existe o no se pudo consultar.", null));
            redirigir(fc, "/jefatura.xhtml");
            return;
        }

        calcularSaldo();
    }

    private void calcularSaldo() {
        if (!"VACACIONES".equalsIgnoreCase(solicitud.getTipo()) || solicitud.getFechaInicio() == null) {
            return;
        }
        try {
            VacacionesEmpleado v = new VacacionesEmpleadoDAO().buscarPorEmpleadoYAnio(
                    solicitud.getIdEmpleado(), solicitud.getFechaInicio().getYear());
            if (v == null) {
                saldoTexto = "El empleado no tiene saldo de vacaciones registrado para "
                        + solicitud.getFechaInicio().getYear() + ".";
            } else {
                int quedarian = v.getDiasDisponibles() - solicitud.getDiasSolicitados();
                saldoTexto = "Saldo de " + v.getAnio() + ": " + v.getDiasDisponibles() + " de "
                        + v.getDiasAsignados() + " días disponibles. Si apruebas, le quedarían "
                        + Math.max(quedarian, 0) + ".";
            }
        } catch (SQLException e) {
            saldoTexto = "";
        }
    }

    public String aprobar() {
        if (!puedeDecidir()) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            new SolicitudService().aprobar(solicitud.getIdSolicitud(),
                    empleado.getIdEmpleado(), usuario.getIdUsuario());
        } catch (ReglaNegocioException e) {
            error(fc, e.getMessage());
            return null;
        } catch (SQLException e) {
            error(fc, "No fue posible aprobar la solicitud.");
            return null;
        }
        return terminar(fc, "La solicitud fue aprobada correctamente.");
    }

    public String rechazar() {
        if (!puedeDecidir()) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String motivo = motivoRechazo == null ? "" : motivoRechazo.trim();

        if (motivo.isEmpty()) {
            error(fc, "Debe indicar el motivo del rechazo.");
            return null;
        }
        if (motivo.length() < 5 || motivo.length() > 250) {
            error(fc, "El motivo del rechazo debe tener entre 5 y 250 caracteres.");
            return null;
        }
        try {
            new SolicitudDAO().rechazar(solicitud.getIdSolicitud(), empleado.getIdEmpleado(),
                    motivo, usuario.getIdUsuario());
        } catch (SQLException e) {
            error(fc, "No fue posible rechazar la solicitud.");
            return null;
        }
        return terminar(fc, "La solicitud fue rechazada correctamente.");
    }

    private boolean puedeDecidir() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (solicitud == null || soloLectura) {
            return false;
        }
        if (!"PENDIENTE".equalsIgnoreCase(solicitud.getEstado())) {
            error(fc, "Solo se pueden resolver solicitudes pendientes.");
            return false;
        }
        return true;
    }

    private String terminar(FacesContext fc, String mensaje) {
        fc.getExternalContext().getFlash().setKeepMessages(true);
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
        return "jefatura?faces-redirect=true";
    }

    private static void error(FacesContext fc, String mensaje) {
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
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

    public boolean isCargada() {
        return solicitud != null;
    }

    public boolean isPuedeDecidir() {
        return solicitud != null && !soloLectura && "PENDIENTE".equalsIgnoreCase(solicitud.getEstado());
    }

    public String getEmpleado() {
        return solicitud.getNombreEmpleado();
    }

    public String getEmpresa() {
        return solicitud.getNombreEmpresa();
    }

    public String getTipo() {
        return Formato.capitalizar(solicitud.getTipo());
    }

    public String getFechas() {
        return Formato.rango(solicitud.getFechaInicio(), solicitud.getFechaFin());
    }

    public String getDias() {
        return Formato.dias(solicitud.getDiasSolicitados());
    }

    public String getMotivo() {
        return solicitud.getMotivo();
    }

    public String getEstado() {
        return Formato.capitalizar(Formato.estado(solicitud.getEstado()));
    }

    public String getClaseEstado() {
        return Formato.estado(solicitud.getEstado()).toLowerCase(Locale.ROOT);
    }

    public boolean isRechazada() {
        return "RECHAZADA".equals(Formato.estado(solicitud.getEstado()))
                && solicitud.getMotivoRechazo() != null && !solicitud.getMotivoRechazo().trim().isEmpty();
    }

    public String getMotivoRechazoGuardado() {
        return solicitud.getMotivoRechazo();
    }

    public String getSaldoTexto() {
        return saldoTexto;
    }

    public boolean isHaySaldo() {
        return !saldoTexto.isEmpty();
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}