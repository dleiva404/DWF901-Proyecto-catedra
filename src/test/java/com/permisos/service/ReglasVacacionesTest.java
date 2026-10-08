package com.permisos.service;

import com.permisos.service.ReglasVacaciones.Saldo;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReglasVacacionesTest {

    // ---------- calcularDias ----------

    @Test
    public void unSoloDiaCuentaComoUno() throws Exception {
        LocalDate d = LocalDate.of(2026, 10, 15);
        assertEquals(1, ReglasVacaciones.calcularDias(d, d));
    }

    @Test
    public void extremosIncluidos() throws Exception {
        assertEquals(3, ReglasVacaciones.calcularDias(
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12)));
    }

    @Test
    public void cruzaFinDeMes() throws Exception {
        assertEquals(4, ReglasVacaciones.calcularDias(
                LocalDate.of(2026, 1, 30), LocalDate.of(2026, 2, 2)));
    }

    @Test
    public void anioBisiesto() throws Exception {
        // 28 feb, 29 feb, 1 mar de 2028 (bisiesto)
        assertEquals(3, ReglasVacaciones.calcularDias(
                LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1)));
    }

    @Test
    public void finAntesDeInicioEsInvalido() {
        try {
            ReglasVacaciones.calcularDias(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 10));
            fail("Debió lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            assertEquals("La fecha fin no puede ser anterior al inicio.", e.getMessage());
        }
    }

    @Test(expected = ReglaNegocioException.class)
    public void fechasNulasSonInvalidas() throws Exception {
        ReglasVacaciones.calcularDias(null, LocalDate.of(2026, 10, 10));
    }

    // ---------- validarSaldoParaSolicitar ----------

    @Test
    public void puedeSolicitarExactamenteElSaldo() throws Exception {
        ReglasVacaciones.validarSaldoParaSolicitar(new Saldo(15, 10, 5), 0, 5);
    }

    @Test(expected = ReglaNegocioException.class)
    public void noPuedeSolicitarUnDiaMasQueElSaldo() throws Exception {
        ReglasVacaciones.validarSaldoParaSolicitar(new Saldo(15, 10, 5), 0, 6);
    }

    @Test
    public void lasSolicitudesPendientesCuentanContraElSaldo() throws Exception {
        Saldo saldo = new Saldo(15, 10, 5);

        // 5 disponibles - 4 ya comprometidos = 1 libre
        ReglasVacaciones.validarSaldoParaSolicitar(saldo, 4, 1);   // ok

        try {
            ReglasVacaciones.validarSaldoParaSolicitar(saldo, 4, 2);
            fail("Debió rechazar: solo queda 1 día libre");
        } catch (ReglaNegocioException e) {
            assertTrue(e.getMessage().contains("comprometidos"));
        }
    }

    
    @Test
    public void dobleGastoDelMismoSaldoSeBloquea() throws Exception {
        Saldo saldo = new Saldo(15, 10, 5);

        ReglasVacaciones.validarSaldoParaSolicitar(saldo, 0, 4);   // primera: ok

        try {
            ReglasVacaciones.validarSaldoParaSolicitar(saldo, 4, 4); // segunda: no
            fail("La segunda solicitud no debió pasar");
        } catch (ReglaNegocioException e) {
            // esperado
        }
    }

    @Test(expected = ReglaNegocioException.class)
    public void sinRegistroDeSaldoNoSePuedeSolicitar() throws Exception {
        ReglasVacaciones.validarSaldoParaSolicitar(null, 0, 1);
    }

    @Test(expected = ReglaNegocioException.class)
    public void ceroDiasNoEsValido() throws Exception {
        ReglasVacaciones.validarSaldoParaSolicitar(new Saldo(15, 0, 15), 0, 0);
    }

    @Test(expected = ReglaNegocioException.class)
    public void diasNegativosNoSonValidos() throws Exception {
        ReglasVacaciones.validarSaldoParaSolicitar(new Saldo(15, 0, 15), 0, -3);
    }

    // ---------- descontar ----------

    @Test
    public void descontarActualizaUtilizadosYDisponibles() throws Exception {
        Saldo nuevo = ReglasVacaciones.descontar(new Saldo(15, 0, 15), 4);

        assertEquals(15, nuevo.getDiasAsignados());
        assertEquals(4, nuevo.getDiasUtilizados());
        assertEquals(11, nuevo.getDiasDisponibles());
        assertTrue(nuevo.esConsistente());
    }

    @Test
    public void sePuedeGastarElSaldoHastaCero() throws Exception {
        Saldo nuevo = ReglasVacaciones.descontar(new Saldo(15, 10, 5), 5);

        assertEquals(15, nuevo.getDiasUtilizados());
        assertEquals(0, nuevo.getDiasDisponibles());
        assertTrue(nuevo.esConsistente());
    }

    @Test
    public void noSePuedeDescontarMasDeLoDisponible() {
        try {
            ReglasVacaciones.descontar(new Saldo(15, 10, 5), 6);
            fail("Debió lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            assertTrue(e.getMessage().contains("no alcanza"));
        }
    }

    @Test
    public void descontarNoMutaElSaldoOriginal() throws Exception {
        Saldo original = new Saldo(15, 0, 15);
        ReglasVacaciones.descontar(original, 4);

        assertEquals(0, original.getDiasUtilizados());
        assertEquals(15, original.getDiasDisponibles());
    }

    @Test(expected = ReglaNegocioException.class)
    public void descontarSinSaldoFalla() throws Exception {
        ReglasVacaciones.descontar(null, 1);
    }

    @Test(expected = ReglaNegocioException.class)
    public void descontarCeroDiasFalla() throws Exception {
        ReglasVacaciones.descontar(new Saldo(15, 0, 15), 0);
    }

    @Test
    public void variasAprobacionesSeguidasMantienenLaConsistencia() throws Exception {
        Saldo s = new Saldo(15, 0, 15);
        s = ReglasVacaciones.descontar(s, 3);
        s = ReglasVacaciones.descontar(s, 5);
        s = ReglasVacaciones.descontar(s, 7);

        assertEquals(15, s.getDiasUtilizados());
        assertEquals(0, s.getDiasDisponibles());
        assertTrue(s.esConsistente());

        try {
            ReglasVacaciones.descontar(s, 1);
            fail("Con saldo 0 no se puede aprobar nada más");
        } catch (ReglaNegocioException e) {
            // esperado
        }
    }

    @Test
    public void detectaUnSaldoInconsistente() {
        assertFalse(new Saldo(15, 4, 10).esConsistente()); // 4 + 10 != 15
    }
}
