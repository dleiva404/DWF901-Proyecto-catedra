package com.rrhh.api.exception;

import com.permisos.service.ConflictoException;
import com.permisos.service.RecursoNoEncontradoException;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.ValidacionException;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Fija el código HTTP que recibe cada tipo de excepción de servicio. Es lo que
 * evita que ApiExceptionMapper convierta TODO en 409.
 */
public class TraductorErroresServicioTest {

    @Test
    public void laValidacionSale400ConLaListaDeCampos() {
        ApiException api = TraductorErroresServicio.traducir(
                new ValidacionException(Arrays.asList("motivo: es obligatorio", "fechaFin: es obligatoria")));

        assertTrue(api instanceof ValidacionApiException);
        assertEquals(400, api.getEstado());
        assertEquals(Arrays.asList("motivo: es obligatorio", "fechaFin: es obligatoria"),
                ((ValidacionApiException) api).getCampos());
    }

    @Test
    public void unRecursoInexistenteSale404() {
        ApiException api = TraductorErroresServicio.traducir(
                new RecursoNoEncontradoException("La solicitud indicada no existe."));

        assertEquals(404, api.getEstado());
        assertEquals("La solicitud indicada no existe.", api.getMessage());
        assertFalse(api instanceof ValidacionApiException);
    }

    @Test
    public void unConflictoSale409() {
        ApiException api = TraductorErroresServicio.traducir(new ConflictoException("La solicitud ya fue aprobada."));

        assertEquals(409, api.getEstado());
        assertEquals("La solicitud ya fue aprobada.", api.getMessage());
    }

    @Test
    public void unaReglaDeNegocioGenericaComoElSaldoInsuficienteSale409() {
        ApiException api = TraductorErroresServicio.traducir(
                new ReglaNegocioException("No tiene suficientes días de vacaciones."));

        assertEquals(409, api.getEstado());
        assertEquals("No tiene suficientes días de vacaciones.", api.getMessage());
    }

    @Test
    public void laValidacionNuncaSale409AunqueSeaReglaDeNegocio() {
        // ValidacionException ES una ReglaNegocioException; debe ganar la rama específica.
        ReglaNegocioException comoGenerica = new ValidacionException("campo: mal");

        assertEquals(400, TraductorErroresServicio.traducir(comoGenerica).getEstado());
    }

    @Test
    public void elRecursoNoEncontradoNuncaSale409AunqueSeaReglaDeNegocio() {
        ReglaNegocioException comoGenerica = new RecursoNoEncontradoException("no hay");

        assertEquals(404, TraductorErroresServicio.traducir(comoGenerica).getEstado());
    }
}
