package com.rrhh.api.validation;

import com.rrhh.api.exception.ValidacionApiException;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ValidadorApiTest {

    @Test
    public void datosCorrectosNoGeneranErrores() {
        ValidadorApi v = new ValidadorApi();

        v.obligatorio("motivo", "Vacaciones familiares");
        v.longitud("motivo", "Vacaciones familiares", 5, 250);
        LocalDate fecha = v.fecha("fechaInicio", "2026-11-09");
        v.positivo("tipoSolicitudId", 1);

        assertFalse(v.hayErrores());
        assertEquals(LocalDate.of(2026, 11, 9), fecha);
        v.lanzarSiHayErrores();
    }

    @Test
    public void campoVacioONuloEsObligatorio() {
        ValidadorApi v = new ValidadorApi();

        v.obligatorio("username", null);
        v.obligatorio("password", "   ");

        assertEquals(2, v.getErrores().size());
        assertEquals("username: es obligatorio", v.getErrores().get(0));
    }

    @Test
    public void longitudFueraDeRangoSeRechaza() {
        ValidadorApi v = new ValidadorApi();

        v.longitud("motivo", "abc", 5, 250);

        assertEquals("motivo: debe tener entre 5 y 250 caracteres",
                v.getErrores().get(0));
    }

    @Test
    public void unValorVacioNoDuplicaElErrorDeLongitud() {
        ValidadorApi v = new ValidadorApi();

        v.obligatorio("motivo", "");
        v.longitud("motivo", "", 5, 250);

        assertEquals(1, v.getErrores().size());
    }

    @Test
    public void fechaConFormatoIncorrectoSeRechaza() {
        ValidadorApi v = new ValidadorApi();

        assertNull(v.fecha("fechaInicio", "09/11/2026"));
        assertNull(v.fecha("fechaFin", ""));

        assertEquals("fechaInicio: debe tener el formato yyyy-MM-dd",
                v.getErrores().get(0));
        assertEquals("fechaFin: es obligatoria", v.getErrores().get(1));
    }

    @Test
    public void identificadorDebeSerPositivo() {
        ValidadorApi v = new ValidadorApi();

        v.positivo("tipoSolicitudId", 0);
        v.positivo("empleadoId", null);

        assertEquals(2, v.getErrores().size());
    }

    @Test
    public void lanzarSiHayErroresDevuelveLaListaDeCampos() {
        ValidadorApi v = new ValidadorApi();
        v.obligatorio("username", "");
        v.error("fechaFin", "no puede ser anterior a fechaInicio");

        try {
            v.lanzarSiHayErrores();
            fail("Debía lanzar ValidacionApiException");
        } catch (ValidacionApiException ex) {
            assertEquals(400, ex.getEstado());
            assertEquals(2, ex.getCampos().size());
            assertTrue(ex.getCampos().get(1).startsWith("fechaFin:"));
        }
    }
}