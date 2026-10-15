package com.permisos.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


public final class ReglasVacaciones {

    public static final String TIPO_VACACIONES = "VACACIONES";

    private ReglasVacaciones() {
        
    }

    public static int contarDiasLaborables(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            return 0;
        }
        long total = ChronoUnit.DAYS.between(inicio, fin) + 1;
        LocalDate primerDomingo = inicio.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long domingos = primerDomingo.isAfter(fin)
                ? 0
                : ChronoUnit.DAYS.between(primerDomingo, fin) / 7 + 1;
        return (int) (total - domingos);
    }

    public static int calcularDias(LocalDate inicio, LocalDate fin) throws ReglaNegocioException {
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Debe indicar fecha de inicio y fin.");
        }
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha fin no puede ser anterior al inicio.");
        }
        int dias = contarDiasLaborables(inicio, fin);
        if (dias == 0) {
            throw new ReglaNegocioException(
                    "El rango seleccionado solo incluye domingos. Elija al menos un día de lunes a sábado.");
        }
        return dias;
    }

    /**
     * Valida que se pueda SOLICITAR esa cantidad de días.
     *
     * @param saldo           saldo del año de la solicitud (null si no existe registro)
     * @param diasPendientes  días ya comprometidos en solicitudes PENDIENTES del mismo año
     * @param diasSolicitados días de la nueva solicitud
     */
    public static void validarSaldoParaSolicitar(Saldo saldo, int diasPendientes, int diasSolicitados)
            throws ReglaNegocioException {

        if (diasSolicitados <= 0) {
            throw new ReglaNegocioException("Debe solicitar al menos 1 día.");
        }
        if (saldo == null) {
            throw new ReglaNegocioException("No tiene saldo de vacaciones registrado para ese año.");
        }

        int libres = saldo.getDiasDisponibles() - diasPendientes;

        if (diasSolicitados > libres) {
            throw new ReglaNegocioException(String.format(
                    "No tiene suficientes días de vacaciones: solicita %d, tiene %d disponibles "
                            + "y %d ya comprometidos en solicitudes pendientes.",
                    diasSolicitados, saldo.getDiasDisponibles(), diasPendientes));
        }
    }

   
    public static Saldo descontar(Saldo saldo, int dias) throws ReglaNegocioException {
        if (saldo == null) {
            throw new ReglaNegocioException("No hay saldo de vacaciones registrado para descontar.");
        }
        if (dias <= 0) {
            throw new ReglaNegocioException("Los días a descontar deben ser al menos 1.");
        }
        if (dias > saldo.getDiasDisponibles()) {
            throw new ReglaNegocioException(String.format(
                    "El saldo disponible (%d días) no alcanza para aprobar %d días.",
                    saldo.getDiasDisponibles(), dias));
        }
        return new Saldo(
                saldo.getDiasAsignados(),
                saldo.getDiasUtilizados() + dias,
                saldo.getDiasDisponibles() - dias);
    }

    
    public static final class Saldo {

        private final int diasAsignados;
        private final int diasUtilizados;
        private final int diasDisponibles;

        public Saldo(int diasAsignados, int diasUtilizados, int diasDisponibles) {
            this.diasAsignados = diasAsignados;
            this.diasUtilizados = diasUtilizados;
            this.diasDisponibles = diasDisponibles;
        }

        public int getDiasAsignados() {
            return diasAsignados;
        }

        public int getDiasUtilizados() {
            return diasUtilizados;
        }

        public int getDiasDisponibles() {
            return diasDisponibles;
        }

        
        public boolean esConsistente() {
            return diasAsignados == diasUtilizados + diasDisponibles;
        }
    }
}
