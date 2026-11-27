package com.permisos.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenRecuperacionTest {

    @Test
    public void generaCodigosAleatoriosUrlSafeYDeUnSoloFormato() {
        String primero = TokenRecuperacion.generar();
        String segundo = TokenRecuperacion.generar();

        assertTrue(primero.matches("[A-Za-z0-9_-]{43}"));
        assertFalse(primero.equals(segundo));
    }

    @Test
    public void almacenaUnHashYComparaEnTiempoConstante() {
        String token = TokenRecuperacion.generar();
        String hash = TokenRecuperacion.hash(token);

        assertTrue(hash.matches("[0-9a-f]{64}"));
        assertFalse(hash.equals(token));
        assertTrue(TokenRecuperacion.coincide(token, hash));
        assertFalse(TokenRecuperacion.coincide(token + "x", hash));
        assertFalse(TokenRecuperacion.coincide(null, hash));
    }
}
