package com.rrhh.modelo;

// Clase para registrar las diferentes solicitudes y permisos del personal
public class Solicitud {
    private String idSolicitud;
    private Empleado empleado;
    private String empresa;
    private String sucursalArea;
    private String tipoSolicitud;

    // Datos generales
    private String fechaInicio;
    private String fechaFin;
    private String motivo;

    // Campos exclusivos de permisos
    private boolean conGoceSueldo;
    private boolean esPermisoISSS;

    // Campos exclusivos de vacaciones
    private String diasPendientes;
    private String tareasCriticas;
    private String personaSustituta;

    public Solicitud(String idSolicitud, Empleado empleado, String empresa, String sucursalArea, String tipoSolicitud) {
        this.idSolicitud = idSolicitud;
        this.empleado = empleado;
        this.empresa = empresa;
        this.sucursalArea = sucursalArea;
        this.tipoSolicitud = tipoSolicitud;
    }

    // Getters y Setters
    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getSucursalArea() { return sucursalArea; }
    public void setSucursalArea(String sucursalArea) { this.sucursalArea = sucursalArea; }

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public boolean isConGoceSueldo() { return conGoceSueldo; }
    public void setConGoceSueldo(boolean conGoceSueldo) { this.conGoceSueldo = conGoceSueldo; }

    public boolean isEsPermisoISSS() { return esPermisoISSS; }
    public void setEsPermisoISSS(boolean esPermisoISSS) { this.esPermisoISSS = esPermisoISSS; }

    public String getDiasPendientes() { return diasPendientes; }
    public void setDiasPendientes(String diasPendientes) { this.diasPendientes = diasPendientes; }

    public String getTareasCriticas() { return tareasCriticas; }
    public void setTareasCriticas(String tareasCriticas) { this.tareasCriticas = tareasCriticas; }

    public String getPersonaSustituta() { return personaSustituta; }
    public void setPersonaSustituta(String personaSustituta) { this.personaSustituta = personaSustituta; }
}