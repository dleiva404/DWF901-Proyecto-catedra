package com.permisos.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmpresasGrupoTest {

    // ---------- emisora ----------

    @Test
    public void conEmpresaDevuelveLaEmpresa() {
        assertEquals("Copro", EmpresasGrupo.emisora("Copro", "Santa Ana"));
    }

    @Test
    public void conEmpresaVaciaUsaDidelcoMasSucursal() {
        assertEquals("Didelco Santa Ana", EmpresasGrupo.emisora("", "Santa Ana"));
    }

    @Test
    public void noRepiteElPrefijoDidelco() {
        assertEquals("Didelco Apopa", EmpresasGrupo.emisora(null, "Didelco Apopa"));
    }

    @Test
    public void sinEmpresaNiSucursalDevuelveDidelcoCentral() {
        assertEquals("Didelco Central", EmpresasGrupo.emisora(null, null));
    }

    // ---------- logo ----------

    @Test
    public void logoDidelco() {
        assertEquals("LogoDidelco.png", EmpresasGrupo.logo("Didelco Santa Ana"));
    }

    @Test
    public void logoCopro() {
        assertEquals("LogoCopro.png", EmpresasGrupo.logo("Copro"));
    }

    @Test
    public void logoDca() {
        assertEquals("LogoDCA-pmg.png", EmpresasGrupo.logo("DCA"));
    }

    @Test
    public void logoElf() {
        assertEquals("LogoElf.png", EmpresasGrupo.logo("Elf"));
    }

    @Test
    public void logoPropultran() {
        assertEquals("LogoPropultran.png", EmpresasGrupo.logo("Propultran"));
    }

    @Test
    public void logoSteel() {
        assertEquals("LogoSteel.png", EmpresasGrupo.logo("Steel"));
    }

    @Test
    public void cualquierOtraUsaElLogoPorDefecto() {
        assertEquals("logoGC.png", EmpresasGrupo.logo("Invercalma"));
    }
}
