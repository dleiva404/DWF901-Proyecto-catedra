package com.permisos.service;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * La capa REST decide el código HTTP según el TIPO de excepción, así que estas
 * pruebas fijan ese contrato: qué es subclase de qué y qué mensajes se envían
 * al cancelar una solicitud que ya no está pendiente.
 */
public class ExcepcionesServicioTest {

    @Test
    public void lasExcepcionesNuevasSonReglaNegocio() {
        // Así el código que ya captura ReglaNegocioException (la web) no se rompe.
        assertTrue(new ConflictoException("x") instanceof ReglaNegocioException);
        assertTrue(new RecursoNoEncontradoException("x") instanceof ReglaNegocioException);
        assertTrue(new ValidacionException("x") instanceof ReglaNegocioException);
    }

    @Test
    public void elMensajeSeConserva() {
        assertEquals("choque", new ConflictoException("choque").getMessage());
        assertEquals("no hay", new RecursoNoEncontradoException("no hay").getMessage());
    }

    @Test
    public void validacionLlevaLaListaCompletaYEsInmutable() {
        ValidacionException e = new ValidacionException(Arrays.asList("a: mal", "b: mal"));

        assertEquals(Arrays.asList("a: mal", "b: mal"), e.getErrores());
        assertEquals("a: mal b: mal", e.getMessage());
        try {
            e.getErrores().add("c: mal");
            org.junit.Assert.fail("La lista no debe poder modificarse");
        } catch (UnsupportedOperationException esperado) {
            // inmutable
        }
    }

    @Test
    public void validacionConUnSoloErrorTambienFunciona() {
        ValidacionException e = new ValidacionException("campo: problema");

        assertEquals(1, e.getErrores().size());
        assertEquals("campo: problema", e.getMessage());
    }

    @Test
    public void mensajesAlCancelarUnaSolicitudQueYaNoEstaPendiente() {
        assertEquals("La solicitud ya está cancelada.",
                SolicitudEmpleadoService.mensajeNoCancelable("CANCELADA"));
        assertEquals("La solicitud ya fue rechazada y no se puede cancelar.",
                SolicitudEmpleadoService.mensajeNoCancelable("RECHAZADA"));
        assertTrue(SolicitudEmpleadoService.mensajeNoCancelable("APROBADA").contains("todavía no está disponible"));
        assertEquals("La solicitud ya no está pendiente.",
                SolicitudEmpleadoService.mensajeNoCancelable("OTRO"));
    }
}
