package com.permisos.service;

/**
 * Reglas del panel Admin TI que no dependen de la base de datos
 * (se prueban con JUnit sin levantar nada).
 */
public final class ReglasAdmin {

    public static final String ROL_ADMIN = "ADMIN";

    private ReglasAdmin() {
    }

    /**
     * Un ADMIN no puede desactivarse a si mismo y siempre debe quedar al menos
     * un ADMIN activo.
     */
    public static void validarDesactivar(boolean esUnoMismo, boolean objetivoEsAdminActivo,
                                         long adminsActivos) throws ConflictoException {
        if (esUnoMismo) {
            throw new ConflictoException("No puede desactivar su propia cuenta.");
        }
        if (objetivoEsAdminActivo && adminsActivos <= 1) {
            throw new ConflictoException("Debe quedar al menos un ADMIN activo.");
        }
    }

    /**
     * Al quitar el rol ADMIN: nadie se lo quita a si mismo y siempre debe quedar
     * al menos un ADMIN activo. Si el rol nuevo tambien es ADMIN no hay nada que validar.
     */
    public static void validarCambioRol(boolean esUnoMismo, boolean objetivoEsAdminActivo,
                                        boolean nuevoEsAdmin, long adminsActivos)
            throws ConflictoException {
        if (nuevoEsAdmin) {
            return;
        }
        if (esUnoMismo && objetivoEsAdminActivo) {
            throw new ConflictoException("No puede quitarse su propio rol de ADMIN.");
        }
        if (objetivoEsAdminActivo && adminsActivos <= 1) {
            throw new ConflictoException("Debe quedar al menos un ADMIN activo.");
        }
    }
}
