package com.rrhh.api.security;

import com.rrhh.api.ApiConfig;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public final class TokenService {

    private final ConcurrentHashMap<String, SesionApi> sesiones =
            new ConcurrentHashMap<>();
    private final long duracionSegundos;
    private final Clock reloj;

    public TokenService() {
        this(ApiConfig.tokenMinutos() * 60L, Clock.systemUTC());
    }

    TokenService(long duracionSegundos, Clock reloj) {
        this.duracionSegundos = duracionSegundos;
        this.reloj = reloj;
    }

    public SesionApi crear(int usuarioId, int empleadoId, String username,
                           String nombre, String rol) {
        purgarVencidas();
        String token = UUID.randomUUID().toString();
        Instant expiraEn = reloj.instant().plusSeconds(duracionSegundos);
        SesionApi sesion = new SesionApi(token, usuarioId, empleadoId,
                username, nombre, rol, expiraEn);
        sesiones.put(token, sesion);
        return sesion;
    }

    public SesionApi validar(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        SesionApi sesion = sesiones.get(token);
        if (sesion == null) {
            return null;
        }
        if (sesion.estaVencida(reloj.instant())) {
            sesiones.remove(token);
            return null;
        }
        return sesion;
    }

    public void invalidar(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    public long getDuracionSegundos() {
        return duracionSegundos;
    }

    private void purgarVencidas() {
        Instant ahora = reloj.instant();
        for (SesionApi sesion : sesiones.values()) {
            if (sesion.estaVencida(ahora)) {
                sesiones.remove(sesion.getToken());
            }
        }
    }

    public static String enmascarar(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 6) + "...";
    }
}