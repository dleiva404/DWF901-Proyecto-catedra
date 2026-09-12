package com.permisos.model;

/**
 * Saldo de vacaciones de un Empleado para un anio determinado.
 */
public class VacacionesEmpleado {

    private int idVacaciones;
    private int idEmpleado;
    private int anio;
    private int diasAsignados;
    private int diasUtilizados;
    private int diasDisponibles;

    public VacacionesEmpleado() {
    }

    public VacacionesEmpleado(int idVacaciones, int idEmpleado, int anio,
                              int diasAsignados, int diasUtilizados, int diasDisponibles) {
        this.idVacaciones = idVacaciones;
        this.idEmpleado = idEmpleado;
        this.anio = anio;
        this.diasAsignados = diasAsignados;
        this.diasUtilizados = diasUtilizados;
        this.diasDisponibles = diasDisponibles;
    }

    public int getIdVacaciones() { return idVacaciones; }
    public void setIdVacaciones(int idVacaciones) { this.idVacaciones = idVacaciones; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getDiasAsignados() { return diasAsignados; }
    public void setDiasAsignados(int diasAsignados) { this.diasAsignados = diasAsignados; }

    public int getDiasUtilizados() { return diasUtilizados; }
    public void setDiasUtilizados(int diasUtilizados) { this.diasUtilizados = diasUtilizados; }

    public int getDiasDisponibles() { return diasDisponibles; }
    public void setDiasDisponibles(int diasDisponibles) { this.diasDisponibles = diasDisponibles; }
}