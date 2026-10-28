package com.rrhh.api.security;

import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokenServiceTest {

    private static class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-11-02T15:00:00Z");

        void avanzarSegundos(long segundos) {
            ahora = ahora.plusSeconds(segundos);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private SesionApi nueva(TokenService servicio) {
        return servicio.crear(5, 5, "david.leiva", "David Leiva", "EMPLEADO");
    }

    @Test
    public void tokenValidoDevuelveLaSesionDelUsuario() {
        TokenService servicio = new TokenService(1800, new RelojManual());
        SesionApi sesion = nueva(servicio);

        SesionApi encontrada = servicio.validar(sesion.getToken());

        assertNotNull(encontrada);
        assertEquals("david.leiva", encontrada.getUsername());
        assertEquals("EMPLEADO", encontrada.getRol());
        assertEquals(5, encontrada.getEmpleadoId());
    }

    @Test
    public void tokenDesconocidoOVacioNoEsValido() {
        TokenService servicio = new TokenService(1800, new RelojManual());

        assertNull(servicio.validar("no-existe"));
        assertNull(servicio.validar(""));
        assertNull(servicio.validar(null));
    }

    @Test
    public void tokenVencidoNoEsValido() {
        RelojManual reloj = new RelojManual();
        TokenService servicio = new TokenService(1800, reloj);
        SesionApi sesion = nueva(servicio);

        reloj.avanzarSegundos(1799);
        assertNotNull(servicio.validar(sesion.getToken()));

        reloj.avanzarSegundos(1);
        assertNull(servicio.validar(sesion.getToken()));
    }

    @Test
    public void logoutInvalidaElToken() {
        TokenService servicio = new TokenService(1800, new RelojManual());
        SesionApi sesion = nueva(servicio);

        servicio.invalidar(sesion.getToken());

        assertNull(servicio.validar(sesion.getToken()));
    }

    @Test
    public void cadaLoginGeneraUnTokenDistinto() {
        TokenService servicio = new TokenService(1800, new RelojManual());

        assertNotEquals(nueva(servicio).getToken(), nueva(servicio).getToken());
    }

    @Test
    public void elTokenNuncaSeMuestraCompletoEnElLog() {
        String token = "b7c1f0a2-5d3e-4c8a-9e61-2f4d7a90c3b5";
        String visible = TokenService.enmascarar(token);

        assertEquals("b7c1f0...", visible);
        assertTrue(!visible.contains("9e61"));
        assertEquals("***", TokenService.enmascarar(null));
    }

    @Test
    public void elRolSeCompruebaSinImportarMayusculas() {
        TokenService servicio = new TokenService(1800, new RelojManual());
        SesionApi sesion = servicio.crear(2, 2, "carlos", "Carlos Gómez", "JEFATURA");
        ApiSecurityContext contexto = new ApiSecurityContext(new ApiPrincipal(sesion), false);

        assertTrue(contexto.isUserInRole("jefatura"));
        assertTrue(contexto.isUserInRole("JEFATURA"));
        assertTrue(!contexto.isUserInRole("RRHH"));
        assertTrue(!contexto.isUserInRole(null));
    }
}