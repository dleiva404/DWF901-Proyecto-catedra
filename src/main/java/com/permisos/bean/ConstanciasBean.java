package com.permisos.bean;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.dao.SucursalAreaDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.SucursalArea;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Bean de la vista "Constancias" del empleado (constancias.xhtml).
 * Reutiliza ConstanciaDAO y la lógica de empresa emisora de ConstanciaServlet.
 */
@ManagedBean(name = "constanciasBean")
@ViewScoped
public class ConstanciasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Empleado empleado;
    private String empresaEmisora = "";
    private final List<ConstanciaItem> constancias = new ArrayList<>();
    private int aprobadas;
    private int pendientes;

    private String tipo = "Salarial";
    private String institucion;
    private String motivo;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        empleado = (Empleado) ec.getSessionMap().get("empleado");
        if (empleado == null) {
            try {
                ec.redirect(ec.getRequestContextPath() + "/login");
                fc.responseComplete();
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo redirigir al login", e);
            }
            return;
        }
        empresaEmisora = calcularEmpresaEmisora();
        cargar();
    }

    private void cargar() {
        constancias.clear();
        aprobadas = 0;
        pendientes = 0;
        for (Constancia c : new ConstanciaDAO().obtenerPorEmpleado(empleado.getIdEmpleado())) {
            String estado = normalizarEstado(c.getEstado());
            if ("APROBADA".equals(estado)) {
                aprobadas++;
            } else if ("PENDIENTE".equals(estado)) {
                pendientes++;
            }
            constancias.add(new ConstanciaItem(c.getIdConstancia(), c.getTipo(), c.getInstitucion(),
                    c.getMotivo(), estado, c.getMotivoRechazo()));
        }
    }

    private String calcularEmpresaEmisora() {
        if (empleado.getEmpresa() != null && !empleado.getEmpresa().trim().isEmpty()) {
            return empleado.getEmpresa();
        }
        String nombreSucursal = null;
        try {
            SucursalArea sucursal = new SucursalAreaDAO().buscarPorId(empleado.getIdSucursalArea());
            if (sucursal != null) {
                nombreSucursal = sucursal.getNombre();
            }
        } catch (Exception e) {
            nombreSucursal = null;
        }
        return "Didelco " + (nombreSucursal != null ? nombreSucursal : "Central");
    }

    public String enviar() {
        if (empleado == null) {
            return null;
        }

        List<String> errores = new ArrayList<>();
        if (tipo == null || tipo.trim().isEmpty()) {
            errores.add("Debe seleccionar el tipo de constancia.");
        }
        if (institucion == null || institucion.trim().isEmpty()) {
            errores.add("Debe indicar la institución a la que va dirigida.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            errores.add("Debe indicar el motivo.");
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        if (!errores.isEmpty()) {
            for (String e : errores) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e, null));
            }
            return null;
        }

        try {
            Constancia nueva = new Constancia();
            nueva.setIdEmpleado(empleado.getIdEmpleado());
            nueva.setTipo(tipo);
            nueva.setInstitucion(institucion.trim());
            nueva.setMotivo(motivo.trim());
            nueva.setSalarioReferencia(BigDecimal.ZERO);
            nueva.setEmpresaEmisora(empresaEmisora);
            nueva.setTokenVerificacion(UUID.randomUUID().toString().substring(0, 15));

            if (!new ConstanciaDAO().insertar(nueva)) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "No se pudo registrar la solicitud en la base de datos.", null));
                return null;
            }
        } catch (RuntimeException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ocurrió un error al procesar la solicitud.", null));
            return null;
        }

        fc.getExternalContext().getFlash().setKeepMessages(true);
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Solicitud enviada. Recursos Humanos la revisará.", null));
        return "constancias?faces-redirect=true";
    }

    public String getResumenConteo() {
        return aprobadas + (aprobadas == 1 ? " aprobada" : " aprobadas") + " · "
                + pendientes + (pendientes == 1 ? " pendiente" : " pendientes");
    }

    public String getEmpresaEmisora() {
        return empresaEmisora;
    }

    public List<ConstanciaItem> getConstancias() {
        return constancias;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
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
        return "PENDIENTE";
    }

    public static class ConstanciaItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int id;
        private final String tipo;
        private final String institucion;
        private final String motivo;
        private final String estado;
        private final String motivoRechazo;

        ConstanciaItem(int id, String tipo, String institucion, String motivo, String estado, String motivoRechazo) {
            this.id = id;
            this.tipo = tipo;
            this.institucion = institucion;
            this.motivo = motivo;
            this.estado = estado;
            this.motivoRechazo = motivoRechazo;
        }

        public int getId() {
            return id;
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

        public String getEstado() {
            return estado.substring(0, 1) + estado.substring(1).toLowerCase(Locale.ROOT);
        }

        public String getClaseEstado() {
            return estado.toLowerCase(Locale.ROOT);
        }

        public boolean isAprobada() {
            return "APROBADA".equals(estado);
        }

        public boolean isPendiente() {
            return "PENDIENTE".equals(estado);
        }

        public boolean isRechazada() {
            return "RECHAZADA".equals(estado);
        }

        public String getMotivoRechazo() {
            return motivoRechazo != null && !motivoRechazo.trim().isEmpty() ? "Motivo: " + motivoRechazo : "";
        }
    }
}