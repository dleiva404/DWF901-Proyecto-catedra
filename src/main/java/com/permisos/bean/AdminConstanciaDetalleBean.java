package com.permisos.bean;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;

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
 * Bean del detalle de una constancia (admin-constancias-detalle.xhtml?id=N).
 * RRHH puede aprobarla o rechazarla con motivo; ADMIN solo la consulta.
 */
@ManagedBean(name = "adminConstanciaDetalleBean")
@ViewScoped
public class AdminConstanciaDetalleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario;
    private boolean soloLectura;

    private Constancia constancia;
    private String nombreEmpleado = "";
    private String motivoRechazo;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        usuario = (Usuario) ec.getSessionMap().get("usuario");
        Empleado empleado = (Empleado) ec.getSessionMap().get("empleado");
        Rol rol = (Rol) ec.getSessionMap().get("rol");

        if (usuario == null || empleado == null || rol == null) {
            redirigir(fc, "/login");
            return;
        }

        String nombreRol = rol.getNombre() == null ? "" : rol.getNombre().toUpperCase(Locale.ROOT);
        boolean esRrhh = nombreRol.contains("RRHH");
        if (!esRrhh && !nombreRol.contains("ADMIN")) {
            redirigir(fc, "/solicitudes.xhtml");
            return;
        }
        soloLectura = !esRrhh;

        String id = ec.getRequestParameterMap().get("id");
        try {
            constancia = new ConstanciaDAO().obtenerPorId(Integer.parseInt(id == null ? "" : id.trim()));
        } catch (RuntimeException e) {
            constancia = null;
        }

        if (constancia == null) {
            ec.getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "La constancia indicada no existe o no se pudo consultar.", null));
            redirigir(fc, "/admin-constancias.xhtml");
            return;
        }

        int idEmp = constancia.getIdEmpleado();
        try {
            Empleado e = new EmpleadoDAO().buscarPorId(idEmp);
            nombreEmpleado = e == null ? "Empleado #" + idEmp : (e.getNombre() + " " + e.getApellido()).trim();
        } catch (SQLException e) {
            nombreEmpleado = "Empleado #" + idEmp;
        }
    }

    public String aprobar() {
        if (!puedeDecidir()) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            new ConstanciaDAO().aprobar(constancia.getIdConstancia(), usuario.getIdUsuario(), null);
        } catch (SQLException e) {
            error(fc, "No fue posible aprobar la constancia.");
            return null;
        }
        return terminar(fc, "La constancia fue aprobada correctamente.");
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
            new ConstanciaDAO().rechazar(constancia.getIdConstancia(), usuario.getIdUsuario(), motivo, null);
        } catch (SQLException e) {
            error(fc, "No fue posible rechazar la constancia.");
            return null;
        }
        return terminar(fc, "La constancia fue rechazada correctamente.");
    }

    private boolean puedeDecidir() {
        if (constancia == null || soloLectura) {
            return false;
        }
        if (!"PENDIENTE".equals(Formato.estado(constancia.getEstado()))) {
            error(FacesContext.getCurrentInstance(), "Solo se pueden resolver constancias pendientes.");
            return false;
        }
        return true;
    }

    private String terminar(FacesContext fc, String mensaje) {
        fc.getExternalContext().getFlash().setKeepMessages(true);
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
        return "admin-constancias?faces-redirect=true";
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
        return constancia != null;
    }

    public boolean isPuedeDecidir() {
        return constancia != null && !soloLectura
                && "PENDIENTE".equals(Formato.estado(constancia.getEstado()));
    }

    public String getEmpleado() {
        return nombreEmpleado;
    }

    public String getTipo() {
        return texto(constancia.getTipo());
    }

    public String getInstitucion() {
        return texto(constancia.getInstitucion());
    }

    public String getMotivo() {
        return texto(constancia.getMotivo());
    }

    public String getEmpresa() {
        return texto(constancia.getEmpresaEmisora());
    }

    public String getFechaSolicitud() {
        return constancia.getFechaSolicitud() == null ? ""
                : Formato.fecha(constancia.getFechaSolicitud().toLocalDate());
    }

    public String getClaseEstado() {
        return Formato.estado(constancia.getEstado()).toLowerCase(Locale.ROOT);
    }

    public String getEstado() {
        return Formato.capitalizar(getClaseEstado());
    }

    public boolean isRechazada() {
        return "rechazada".equals(getClaseEstado());
    }

    public String getMotivoRechazoGuardado() {
        return texto(constancia.getMotivoRechazo());
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    private static String texto(String s) {
        return s == null ? "" : s;
    }
}