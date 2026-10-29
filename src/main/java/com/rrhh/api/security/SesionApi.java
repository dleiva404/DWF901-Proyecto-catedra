package com.rrhh.api.security;

import java.time.Instant;

public final class SesionApi {

    private final String token;
    private final int usuarioId;
    private final int empleadoId;
    private final String username;
    private final String nombre;
    private final String rol;
    private final Instant expiraEn;

    public SesionApi(String token, int usuarioId, int empleadoId,
                     String username, String nombre, String rol,
                     Instant expiraEn) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.empleadoId = empleadoId;
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
        this.expiraEn = expiraEn;
    }

    public boolean estaVencida(Instant ahora) {
        return !ahora.isBefore(expiraEn);
    }

    public String getToken() { return token; }

    public int getUsuarioId() { return usuarioId; }

    public int getEmpleadoId() { return empleadoId; }

    public String getUsername() { return username; }

    public String getNombre() { return nombre; }

    public String getRol() { return rol; }

    public Instant getExpiraEn() { return expiraEn; }
}