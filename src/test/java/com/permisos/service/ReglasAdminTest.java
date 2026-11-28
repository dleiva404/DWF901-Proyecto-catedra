package com.permisos.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReglasAdminTest {

    // ---------- desactivar ----------

    @Test
    public void unAdminNoPuedeDesactivarseASiMismo() {
        try {
            ReglasAdmin.validarDesactivar(true, true, 3);
            fail("debia lanzar ConflictoException");
        } catch (ConflictoException e) {
            assertEquals("No puede desactivar su propia cuenta.", e.getMessage());
        }
    }

    @Test
    public void noSeDesactivaElUltimoAdminActivo() {
        try {
            ReglasAdmin.validarDesactivar(false, true, 1);
            fail("debia lanzar ConflictoException");
        } catch (ConflictoException e) {
            assertEquals("Debe quedar al menos un ADMIN activo.", e.getMessage());
        }
    }

    @Test
    public void seDesactivaUnAdminSiQuedanOtros() throws Exception {
        ReglasAdmin.validarDesactivar(false, true, 2);
    }

    @Test
    public void seDesactivaUnUsuarioQueNoEsAdmin() throws Exception {
        ReglasAdmin.validarDesactivar(false, false, 1);
    }

    // ---------- cambio de rol ----------

    @Test
    public void unAdminNoPuedeQuitarseSuPropioRol() {
        try {
            ReglasAdmin.validarCambioRol(true, true, false, 3);
            fail("debia lanzar ConflictoException");
        } catch (ConflictoException e) {
            assertEquals("No puede quitarse su propio rol de ADMIN.", e.getMessage());
        }
    }

    @Test
    public void noSeLeQuitaElRolAlUltimoAdmin() {
        try {
            ReglasAdmin.validarCambioRol(false, true, false, 1);
            fail("debia lanzar ConflictoException");
        } catch (ConflictoException e) {
            assertEquals("Debe quedar al menos un ADMIN activo.", e.getMessage());
        }
    }

    @Test
    public void seLeQuitaElRolAUnAdminSiQuedanOtros() throws Exception {
        ReglasAdmin.validarCambioRol(false, true, false, 2);
    }

    @Test
    public void darRolAdminSiempreSePuede() throws Exception {
        ReglasAdmin.validarCambioRol(true, true, true, 1);
        ReglasAdmin.validarCambioRol(false, false, true, 0);
    }

    @Test
    public void cambiarRolAUnUsuarioQueNoEsAdmin() throws Exception {
        ReglasAdmin.validarCambioRol(false, false, false, 1);
    }
}
