package com.permisos.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PasswordUtilTest {

    private static final int RAPIDO = 1000;

    @Test
    public void elHashTieneElFormatoEsperadoYNoContieneLaClave() {
        String guardado = PasswordUtil.hash("123456", RAPIDO);

        assertTrue(guardado.startsWith("pbkdf2$1000$"));
        assertEquals(4, guardado.split("\\$").length);
        assertFalse(guardado.contains("123456"));
        assertTrue(PasswordUtil.esHash(guardado));
    }

    @Test
    public void laClaveCorrectaSeVerificaYLaIncorrectaNo() {
        String guardado = PasswordUtil.hash("claveSegura", RAPIDO);

        assertTrue(PasswordUtil.verificar("claveSegura", guardado));
        assertFalse(PasswordUtil.verificar("clavesegura", guardado));
        assertFalse(PasswordUtil.verificar("", guardado));
        assertFalse(PasswordUtil.verificar(null, guardado));
    }

    @Test
    public void lamismaClaveGeneraHashesDistintosPorLaSal() {
        String uno = PasswordUtil.hash("123456", RAPIDO);
        String dos = PasswordUtil.hash("123456", RAPIDO);

        assertNotEquals(uno, dos);
        assertTrue(PasswordUtil.verificar("123456", uno));
        assertTrue(PasswordUtil.verificar("123456", dos));
    }

    @Test
    public void unValorDanadoNuncaSeAcepta() {
        assertFalse(PasswordUtil.verificar("123456", "pbkdf2$abc$xx$yy"));
        assertFalse(PasswordUtil.verificar("123456", "pbkdf2$1000$solo"));
        assertFalse(PasswordUtil.verificar("123456", "123456"));
        assertFalse(PasswordUtil.verificar("123456", null));
    }

    @Test
    public void reconoceTextoPlanoYLoComparaSinHash() {
        assertFalse(PasswordUtil.esHash("123456"));
        assertFalse(PasswordUtil.esHash(null));
        assertTrue(PasswordUtil.coincideTextoPlano("123456", "123456"));
        assertFalse(PasswordUtil.coincideTextoPlano("123457", "123456"));
        assertFalse(PasswordUtil.coincideTextoPlano(null, "123456"));
    }

    @Test
    public void detectaQueValoresAntiguosNecesitanActualizarse() {
        assertTrue(PasswordUtil.necesitaActualizar("123456"));
        assertTrue(PasswordUtil.necesitaActualizar(
                PasswordUtil.hash("123456", RAPIDO)));
        assertFalse(PasswordUtil.necesitaActualizar(
                PasswordUtil.hash("123456")));
    }
}