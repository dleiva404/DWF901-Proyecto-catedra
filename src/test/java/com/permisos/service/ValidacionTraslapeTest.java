package com.permisos.service;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidacionTraslapeTest {

    private static LocalDate dic(int dia) {
        return LocalDate.of(2026, 12, dia);
    }

    @Test
    public void mismoRangoSeTraslapa() {
        assertTrue(ValidacionTraslape.seTraslapan(dic(10), dic(12), dic(10), dic(12)));
    }

    @Test
    public void compartenUnSoloDiaEnElExtremo() {
        assertTrue(ValidacionTraslape.seTraslapan(dic(10), dic(15), dic(15), dic(18)));
    }

    @Test
    public void unoContieneAlOtro() {
        assertTrue(ValidacionTraslape.seTraslapan(dic(10), dic(20), dic(12), dic(14)));
    }

    @Test
    public void consecutivosSinCruceNoSeTraslapan() {
        assertFalse(ValidacionTraslape.seTraslapan(dic(10), dic(14), dic(15), dic(18)));
    }

    @Test
    public void separadosNoSeTraslapan() {
        assertFalse(ValidacionTraslape.seTraslapan(dic(1), dic(5), dic(20), dic(25)));
    }

    @Test
    public void laReglaEsSimetrica() {
        assertTrue(ValidacionTraslape.seTraslapan(dic(15), dic(18), dic(10), dic(15)));
    }
}
