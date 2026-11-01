package com.permisos.service;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FiltroSolicitudesTest {

    private static List<String> errores(String estado, String tipo, String desde, String hasta) {
        try {
            FiltroSolicitudes.desdeParametros(estado, tipo, desde, hasta);
            fail("Debió lanzar ValidacionException");
            return null;
        } catch (ValidacionException e) {
            return e.getErrores();
        }
    }

    @Test
    public void filtroVacioNoRestringeNada() {
        FiltroSolicitudes f = FiltroSolicitudes.vacio();

        assertNull(f.getEstado());
        assertNull(f.getIdTipo());
        assertNull(f.getDesde());
        assertNull(f.getHasta());
    }

    @Test
    public void parametrosNulosOEnBlancoSignificanSinFiltro() throws Exception {
        FiltroSolicitudes deNulos = FiltroSolicitudes.desdeParametros(null, null, null, null);
        FiltroSolicitudes deBlancos = FiltroSolicitudes.desdeParametros("  ", "", " ", "");

        assertNull(deNulos.getEstado());
        assertNull(deNulos.getIdTipo());
        assertNull(deBlancos.getEstado());
        assertNull(deBlancos.getIdTipo());
        assertNull(deBlancos.getDesde());
        assertNull(deBlancos.getHasta());
    }

    @Test
    public void elEstadoSeNormalizaAMayusculas() throws Exception {
        assertEquals("APROBADA", FiltroSolicitudes.desdeParametros("aprobada", null, null, null).getEstado());
        assertEquals("PENDIENTE", FiltroSolicitudes.desdeParametros("  Pendiente ", null, null, null).getEstado());
    }

    @Test
    public void losCuatroEstadosConocidosSeAceptan() throws Exception {
        for (String e : new String[]{"PENDIENTE", "APROBADA", "RECHAZADA", "CANCELADA"}) {
            assertEquals(e, FiltroSolicitudes.desdeParametros(e, null, null, null).getEstado());
        }
    }

    @Test
    public void estadoDesconocidoSeRechazaYListaLosPermitidos() {
        List<String> e = errores("ARCHIVADA", null, null, null);

        assertEquals(1, e.size());
        assertTrue(e.get(0), e.get(0).startsWith("estado: debe ser uno de"));
        assertTrue(e.get(0), e.get(0).contains("PENDIENTE"));
    }

    @Test
    public void intentoDeInyeccionEnEstadoSeRechaza() {
        assertEquals(1, errores("PENDIENTE' OR '1'='1", null, null, null).size());
    }

    @Test
    public void tipoValidoSeConvierteAEntero() throws Exception {
        assertEquals(Integer.valueOf(2),
                FiltroSolicitudes.desdeParametros(null, " 2 ", null, null).getIdTipo());
    }

    @Test
    public void tipoNoNumericoCeroONegativoSeRechaza() {
        for (String malo : new String[]{"abc", "0", "-1", "1.5"}) {
            List<String> e = errores(null, malo, null, null);
            assertEquals(malo, "tipoSolicitudId: debe ser un entero mayor que cero", e.get(0));
        }
    }

    @Test
    public void fechasIsoSeParsean() throws Exception {
        FiltroSolicitudes f = FiltroSolicitudes.desdeParametros(null, null, "2026-10-01", "2026-10-31");

        assertEquals(LocalDate.of(2026, 10, 1), f.getDesde());
        assertEquals(LocalDate.of(2026, 10, 31), f.getHasta());
    }

    @Test
    public void soloDesdeOSoloHastaEsValido() throws Exception {
        assertNull(FiltroSolicitudes.desdeParametros(null, null, "2026-10-01", null).getHasta());
        assertNull(FiltroSolicitudes.desdeParametros(null, null, null, "2026-10-31").getDesde());
    }

    @Test
    public void fechaConFormatoNoIsoSeRechaza() {
        assertEquals("desde: debe tener el formato yyyy-MM-dd",
                errores(null, null, "15/10/2026", null).get(0));
    }

    @Test
    public void fechaInexistenteSeRechaza() {
        assertEquals("hasta: debe tener el formato yyyy-MM-dd",
                errores(null, null, null, "2026-02-30").get(0));
    }

    @Test
    public void desdePosteriorAHastaSeRechaza() {
        assertEquals("desde: no puede ser posterior a hasta",
                errores(null, null, "2026-10-31", "2026-10-01").get(0));
    }

    @Test
    public void desdeIgualAHastaEsValido() throws Exception {
        FiltroSolicitudes f = FiltroSolicitudes.desdeParametros(null, null, "2026-10-15", "2026-10-15");

        assertEquals(f.getDesde(), f.getHasta());
    }

    @Test
    public void variosParametrosInvalidosSeReportanTodosJuntos() {
        List<String> e = errores("X", "abc", "mal", "peor");

        assertEquals(4, e.size());
    }
}
