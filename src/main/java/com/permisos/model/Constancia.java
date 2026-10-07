package com.permisos.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Constancia {
    private int idConstancia;
    private int idEmpleado;
    private String tipo;
    private String institucion;
    private String motivo;
    private BigDecimal salarioReferencia;
    private String empresaEmisora;
    private String tokenVerificacion;
    private String estado;
    private Date fechaCreacion;

    public Constancia() {}

    public int getIdConstancia() { return idConstancia; }
    public void setIdConstancia(int idConstancia) { this.idConstancia = idConstancia; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public BigDecimal getSalarioReferencia() { return salarioReferencia; }
    public void setSalarioReferencia(BigDecimal salarioReferencia) { this.salarioReferencia = salarioReferencia; }

    public String getEmpresaEmisora() { return empresaEmisora; }
    public void setEmpresaEmisora(String empresaEmisora) { this.empresaEmisora = empresaEmisora; }

    public String getTokenVerificacion() { return tokenVerificacion; }
    public void setTokenVerificacion(String tokenVerificacion) { this.tokenVerificacion = tokenVerificacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}