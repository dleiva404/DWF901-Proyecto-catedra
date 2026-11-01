package com.permisos.service;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Fechas fijas para que los resultados no dependan del día en que se corran:
 *   lunes 26-oct-2026 ... sábado 31-oct, domingo 1-nov, lunes 2-nov.
 */
public class ReglasSolicitudTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 26);          // lunes
    private static final LocalDate LUNES = LocalDate.of(2026, 10, 26);
    private static final LocalDate MIERCOLES = LocalDate.of(2026, 10, 28);
    private static final LocalDate SABADO = LocalDate.of(2026, 10, 31);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 11, 1);
    private static final LocalDate LUNES_SIGUIENTE = LocalDate.of(2026, 11, 2);

    private static final String MOTIVO_OK = "Vacaciones familiares";

    private static String texto(int longitud) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    private static List<String> erroresSolicitud(Integer tipo, String motivo, LocalDate ini, LocalDate fin) {
        try {
            ReglasSolicitud.validarNuevaSolicitud(tipo, motivo, ini, fin, HOY);
            fail("Debió lanzar ValidacionException");
            return null;
        } catch (ValidacionException e) {
            return e.getErrores();
        }
    }

    private static List<String> erroresIncapacidad(String doc, String obs, LocalDate ini, LocalDate fin) {
        try {
            ReglasSolicitud.validarIncapacidad(doc, obs, ini, fin);
            fail("Debió lanzar ValidacionException");
            return null;
        } catch (ValidacionException e) {
            return e.getErrores();
        }
    }

    // ================= solicitud nueva =================

    @Test
    public void solicitudCorrectaDevuelveLosDias() throws Exception {
        assertEquals(3, ReglasSolicitud.validarNuevaSolicitud(1, MOTIVO_OK, LUNES, MIERCOLES, HOY));
    }

    @Test
    public void iniciarHoyEstaPermitido() throws Exception {
        assertEquals(1, ReglasSolicitud.validarNuevaSolicitud(1, MOTIVO_OK, HOY, HOY, HOY));
    }

    @Test
    public void lunesASabadoSonSeisDias() throws Exception {
        assertEquals(6, ReglasSolicitud.validarNuevaSolicitud(1, MOTIVO_OK, LUNES, SABADO, HOY));
    }

    @Test
    public void unaSemanaCompletaDescuentaElDomingo() throws Exception {
        assertEquals(6, ReglasSolicitud.validarNuevaSolicitud(1, MOTIVO_OK, LUNES, DOMINGO, HOY));
    }

    @Test
    public void sabadoALunesDescuentaElDomingo() throws Exception {
        // sábado, domingo, lunes = 3 días de calendario, 2 contables
        assertEquals(2, ReglasSolicitud.validarNuevaSolicitud(1, MOTIVO_OK, SABADO, LUNES_SIGUIENTE, HOY));
    }

    @Test
    public void unRangoSoloDeDomingoSeRechaza() {
        List<String> e = erroresSolicitud(1, MOTIVO_OK, DOMINGO, DOMINGO);
        assertEquals(1, e.size());
        assertTrue(e.get(0), e.get(0).startsWith("fechaFin: ") && e.get(0).contains("solo incluye domingos"));
    }

    @Test
    public void finAnteriorAlInicioSeRechaza() {
        assertEquals("fechaFin: no puede ser anterior a fechaInicio",
                erroresSolicitud(1, MOTIVO_OK, MIERCOLES, LUNES).get(0));
    }

    @Test
    public void fechaPasadaSeRechaza() {
        assertEquals("fechaInicio: no puede ser una fecha pasada",
                erroresSolicitud(1, MOTIVO_OK, HOY.minusDays(1), HOY.plusDays(2)).get(0));
    }

    @Test
    public void fechasNulasSeReportanPorSeparado() {
        assertEquals("fechaInicio: es obligatoria", erroresSolicitud(1, MOTIVO_OK, null, MIERCOLES).get(0));
        assertEquals("fechaFin: es obligatoria", erroresSolicitud(1, MOTIVO_OK, LUNES, null).get(0));
    }

    @Test
    public void tipoNuloOInvalidoSeRechaza() {
        assertEquals("tipoSolicitudId: es obligatorio",
                erroresSolicitud(null, MOTIVO_OK, LUNES, MIERCOLES).get(0));
        assertEquals("tipoSolicitudId: debe ser un entero mayor que cero",
                erroresSolicitud(0, MOTIVO_OK, LUNES, MIERCOLES).get(0));
    }

    @Test
    public void motivoVacioONuloSeRechaza() {
        assertEquals("motivo: es obligatorio", erroresSolicitud(1, null, LUNES, MIERCOLES).get(0));
        assertEquals("motivo: es obligatorio", erroresSolicitud(1, "   ", LUNES, MIERCOLES).get(0));
    }

    @Test
    public void motivoDeCuatroCaracteresEsMuyCorto() {
        assertEquals("motivo: debe tener entre 5 y 250 caracteres",
                erroresSolicitud(1, texto(4), LUNES, MIERCOLES).get(0));
    }

    @Test
    public void motivoDeCincoCaracteresEsElMinimoValido() throws Exception {
        ReglasSolicitud.validarNuevaSolicitud(1, texto(5), LUNES, MIERCOLES, HOY);
    }

    @Test
    public void losEspaciosAlrededorDelMotivoNoCuentan() {
        // "  abcd  " tiene 4 caracteres útiles
        assertEquals("motivo: debe tener entre 5 y 250 caracteres",
                erroresSolicitud(1, "  abcd  ", LUNES, MIERCOLES).get(0));
    }

    @Test
    public void motivoDeDoscientosCincuentaEsElMaximoDelContrato() throws Exception {
        ReglasSolicitud.validarNuevaSolicitud(1, texto(250), LUNES, MIERCOLES, HOY);
    }

    @Test
    public void motivoDeDoscientosCincuentaYUnoSeRechaza() {
        // El formulario web admite 500, pero la API sigue el contrato: 250.
        assertEquals("motivo: debe tener entre 5 y 250 caracteres",
                erroresSolicitud(1, texto(251), LUNES, MIERCOLES).get(0));
    }

    @Test
    public void variosErroresSeReunenEnUnaLista() {
        List<String> e = erroresSolicitud(null, "", null, null);

        assertEquals(4, e.size());
        assertTrue(e.contains("tipoSolicitudId: es obligatorio"));
        assertTrue(e.contains("motivo: es obligatorio"));
        assertTrue(e.contains("fechaInicio: es obligatoria"));
        assertTrue(e.contains("fechaFin: es obligatoria"));
    }

    @Test
    public void todosLosErroresTienenElFormatoCampoDosPuntosProblema() {
        for (String e : erroresSolicitud(null, "", null, null)) {
            assertTrue(e, e.matches("^[a-zA-Z]+: .+"));
        }
    }

    // ================= incapacidad =================

    @Test
    public void incapacidadCorrectaDevuelveLosDias() throws Exception {
        assertEquals(3, ReglasSolicitud.validarIncapacidad("123456", "Reposo", LUNES, MIERCOLES));
    }

    @Test
    public void unaIncapacidadPuedeEmpezarEnElPasado() throws Exception {
        // Se registra después de incapacitarse: no hay regla de fechas pasadas.
        LocalDate inicioPasado = LocalDate.of(2026, 10, 12);   // lunes, 2 semanas antes de HOY
        LocalDate finPasado = LocalDate.of(2026, 10, 14);      // miércoles
        assertEquals(3, ReglasSolicitud.validarIncapacidad("123456", null, inicioPasado, finPasado));
    }

    @Test
    public void incapacidadSinObservacionesEsValida() throws Exception {
        ReglasSolicitud.validarIncapacidad("123456", null, LUNES, MIERCOLES);
    }

    @Test
    public void incapacidadSinDocumentoSeRechaza() {
        assertEquals("numeroDocumento: es obligatorio",
                erroresIncapacidad(null, null, LUNES, MIERCOLES).get(0));
        assertEquals("numeroDocumento: es obligatorio",
                erroresIncapacidad("  ", null, LUNES, MIERCOLES).get(0));
    }

    @Test
    public void documentoDeCienCaracteresEsElMaximoValido() throws Exception {
        ReglasSolicitud.validarIncapacidad(texto(100), null, LUNES, MIERCOLES);
    }

    @Test
    public void documentoMuyLargoSeRechaza() {
        assertEquals("numeroDocumento: debe tener como máximo 100 caracteres",
                erroresIncapacidad(texto(101), null, LUNES, MIERCOLES).get(0));
    }

    @Test
    public void observacionesMuyLargasSeRechazan() {
        assertEquals("observaciones: debe tener como máximo 500 caracteres",
                erroresIncapacidad("123456", texto(501), LUNES, MIERCOLES).get(0));
    }

    @Test
    public void incapacidadConFinAnteriorSeRechaza() {
        assertEquals("fechaFin: no puede ser anterior a fechaInicio",
                erroresIncapacidad("123456", null, MIERCOLES, LUNES).get(0));
    }

    @Test
    public void incapacidadSoloDeDomingoSeRechaza() {
        String e = erroresIncapacidad("123456", null, DOMINGO, DOMINGO).get(0);
        assertTrue(e, e.contains("solo incluye domingos"));
    }

    @Test
    public void incapacidadSinFechasSeRechaza() {
        List<String> e = erroresIncapacidad("123456", null, null, null);
        assertTrue(e.contains("fechaInicio: es obligatoria"));
        assertTrue(e.contains("fechaFin: es obligatoria"));
    }

    @Test
    public void incapacidadReuneVariosErrores() {
        List<String> e = erroresIncapacidad("", texto(501), null, null);

        assertEquals(4, e.size());
        assertTrue(e.contains("numeroDocumento: es obligatorio"));
        assertTrue(e.contains("observaciones: debe tener como máximo 500 caracteres"));
    }
}
