package com.permisos.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResumenVacacionesTest {

    @Test
    public void sinNadaTomadoNiPendienteQuedaTodoDisponible() {
        ResumenVacaciones r = ResumenVacaciones.calcular(2026, 15, 0, 15, 0);

        assertEquals(2026, r.getAnio());
        assertEquals(15, r.getDiasAsignados());
        assertEquals(0, r.getDiasTomados());
        assertEquals(0, r.getDiasPendientes());
        assertEquals(15, r.getDiasDisponibles());
        assertEquals(15, r.getDiasSolicitables());
    }

    @Test
    public void losPendientesBajanLoSolicitablePeroNoLoDisponible() {
        // 5 tomados, 10 disponibles, 4 pendientes de respuesta
        ResumenVacaciones r = ResumenVacaciones.calcular(2026, 15, 5, 10, 4);

        assertEquals(10, r.getDiasDisponibles());   // igual que el anillo de la pantalla web
        assertEquals(4, r.getDiasPendientes());
        assertEquals(6, r.getDiasSolicitables());   // 10 - 4
    }

    @Test
    public void losSolicitablesNuncaSonNegativos() {
        // dato raro: más pendientes que disponibles
        ResumenVacaciones r = ResumenVacaciones.calcular(2026, 15, 10, 5, 8);

        assertEquals(0, r.getDiasSolicitables());
    }

    @Test
    public void conTodoElSaldoGastadoNoQuedaNadaSolicitable() {
        ResumenVacaciones r = ResumenVacaciones.calcular(2026, 15, 15, 0, 0);

        assertEquals(0, r.getDiasDisponibles());
        assertEquals(0, r.getDiasSolicitables());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unAsignadoNegativoEsUnDatoCorrupto() {
        ResumenVacaciones.calcular(2026, -1, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unPendienteNegativoEsUnDatoCorrupto() {
        ResumenVacaciones.calcular(2026, 15, 0, 15, -2);
    }
}
