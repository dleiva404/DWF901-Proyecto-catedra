package com.permisos.bean;

import com.permisos.dao.IncapacidadDAO;
import com.permisos.dao.SolicitudDAO;
import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.Incapacidad;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de la vista "Registrar incapacidad" (incapacidad.xhtml).
 * Mantiene las mismas validaciones y el mismo registro que tenía IncapacidadServlet.
 */
@ManagedBean(name = "incapacidadBean")
@ViewScoped
public class IncapacidadBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String NOMBRE_TIPO_INCAPACIDAD = "INCAPACIDAD";

    private Usuario usuario;

    private String numeroDocumento;
    private String fechaInicio;
    private String fechaFin;
    private String observaciones;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        usuario = (Usuario) ec.getSessionMap().get("usuario");
        if (usuario == null) {
            try {
                ec.redirect(ec.getRequestContextPath() + "/login");
                fc.responseComplete();
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo redirigir al login", e);
            }
        }
    }

    public String registrar() {
        if (usuario == null) {
            return null;
        }

        List<String> errores = new ArrayList<>();

        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            errores.add("Debe indicar el número de documento (ISSS).");
        }

        LocalDate inicio = null;
        LocalDate fin = null;
        int dias = 0;

        if (isVacio(fechaInicio) || isVacio(fechaFin)) {
            errores.add("Debe indicar fecha de inicio y fin.");
        } else {
            try {
                inicio = LocalDate.parse(fechaInicio.trim());
                fin = LocalDate.parse(fechaFin.trim());
                if (fin.isBefore(inicio)) {
                    errores.add("La fecha fin no puede ser anterior al inicio.");
                } else {
                    dias = (int) (ChronoUnit.DAYS.between(inicio, fin) + 1);
                }
            } catch (DateTimeParseException e) {
                errores.add("Formato de fecha inválido.");
            }
        }

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!errores.isEmpty()) {
            for (String e : errores) {
                error(fc, e);
            }
            return null;
        }

        try {
            TipoSolicitud tipo = new TipoSolicitudDAO().buscarPorNombre(NOMBRE_TIPO_INCAPACIDAD);
            if (tipo == null) {
                error(fc, "No se encontró el tipo de solicitud INCAPACIDAD en el catálogo.");
                return null;
            }

            String documento = numeroDocumento.trim();

            Solicitud solicitud = new Solicitud();
            solicitud.setIdEmpleado(usuario.getIdEmpleado());
            solicitud.setIdTipoSolicitud(tipo.getIdTipoSolicitud());
            solicitud.setFechaInicio(inicio);
            solicitud.setFechaFin(fin);
            solicitud.setDiasSolicitados(dias);
            solicitud.setMotivo("Incapacidad medica - documento " + documento);

            int idSolicitud = new SolicitudDAO().insertar(solicitud);

            Incapacidad incapacidad = new Incapacidad();
            incapacidad.setIdSolicitud(idSolicitud);
            incapacidad.setNumeroDocumento(documento);
            incapacidad.setFechaInicio(inicio);
            incapacidad.setFechaFin(fin);
            incapacidad.setObservaciones(observaciones != null ? observaciones.trim() : null);

            new IncapacidadDAO().insertar(incapacidad);

        } catch (SQLException e) {
            error(fc, "Ocurrió un error al registrar la incapacidad.");
            return null;
        }

        fc.getExternalContext().getFlash().setKeepMessages(true);
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Incapacidad registrada. Tu jefatura la revisará.", null));
        return "solicitudes?faces-redirect=true";
    }

    private static boolean isVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void error(FacesContext fc, String mensaje) {
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}