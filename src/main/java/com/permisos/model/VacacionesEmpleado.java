package com.permisos.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(
        name = "vacaciones_empleado",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vacaciones_empleado_anio",
                        columnNames = {"id_empleado", "anio"}
                )
        }
)
public class VacacionesEmpleado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacaciones")
    private int idVacaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "dias_asignados", nullable = false)
    private int diasAsignados;

    @Column(name = "dias_utilizados", nullable = false)
    private int diasUtilizados;

    @Column(name = "dias_disponibles", nullable = false)
    private int diasDisponibles;

    public VacacionesEmpleado() {
    }

    public VacacionesEmpleado(int idVacaciones, int idEmpleado, int anio,
                              int diasAsignados, int diasUtilizados,
                              int diasDisponibles) {

        this.idVacaciones = idVacaciones;

        this.empleado = new Empleado();
        this.empleado.setIdEmpleado(idEmpleado);

        this.anio = anio;
        this.diasAsignados = diasAsignados;
        this.diasUtilizados = diasUtilizados;
        this.diasDisponibles = diasDisponibles;
    }

    public int getIdVacaciones() {
        return idVacaciones;
    }

    public void setIdVacaciones(int idVacaciones) {
        this.idVacaciones = idVacaciones;
    }

    public int getIdEmpleado() {
        return empleado != null ? empleado.getIdEmpleado() : 0;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (this.empleado == null) {
            this.empleado = new Empleado();
        }
        this.empleado.setIdEmpleado(idEmpleado);
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getDiasAsignados() {
        return diasAsignados;
    }

    public void setDiasAsignados(int diasAsignados) {
        this.diasAsignados = diasAsignados;
    }

    public int getDiasUtilizados() {
        return diasUtilizados;
    }

    public void setDiasUtilizados(int diasUtilizados) {
        this.diasUtilizados = diasUtilizados;
    }

    public int getDiasDisponibles() {
        return diasDisponibles;
    }

    public void setDiasDisponibles(int diasDisponibles) {
        this.diasDisponibles = diasDisponibles;
    }
}