package com.permisos.service;

import com.permisos.model.Solicitud;
import com.permisos.util.JPAUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Regla de traslape de fechas: un empleado no puede tener dos solicitudes
 * (vacaciones, ausencia o incapacidad) cuyas fechas compartan al menos un dia.
 * Solo cuentan las solicitudes PENDIENTE o APROBADA; las RECHAZADA y CANCELADA
 * no bloquean.
 *
 * La usan la API (dentro de su transaccion) y la web JSF/servlets (con su
 * propio EntityManager), para que la regla sea la misma en los dos canales.
 */
public final class ValidacionTraslape {

    private ValidacionTraslape() {
    }

    /** Verdadero si los dos rangos comparten al menos un dia (extremos incluidos). */
    public static boolean seTraslapan(LocalDate ini1, LocalDate fin1,
                                      LocalDate ini2, LocalDate fin2) {
        return !fin1.isBefore(ini2) && !fin2.isBefore(ini1);
    }

    /** Version con EntityManager propio, para la web (JSF y servlets). */
    public static void validar(int idEmpleado, LocalDate inicio, LocalDate fin)
            throws ReglaNegocioException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            validar(em, idEmpleado, inicio, fin);
        } finally {
            em.close();
        }
    }

    /** Version para usar dentro de una transaccion que ya esta abierta. */
    public static void validar(EntityManager em, int idEmpleado,
                               LocalDate inicio, LocalDate fin)
            throws ReglaNegocioException {

        List<Solicitud> choques = em.createQuery(
                        "SELECT s FROM Solicitud s "
                                + "WHERE s.empleado.idEmpleado = :id "
                                + "AND s.estado IN (:estados) "
                                + "AND s.fechaInicio <= :fin AND s.fechaFin >= :inicio "
                                + "ORDER BY s.fechaInicio", Solicitud.class)
                .setParameter("id", idEmpleado)
                .setParameter("estados", Arrays.asList(
                        Solicitud.ESTADO_PENDIENTE, Solicitud.ESTADO_APROBADA))
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .setMaxResults(1)
                .getResultList();

        if (!choques.isEmpty()) {
            Solicitud s = choques.get(0);
            throw new ConflictoException(
                    "Las fechas se traslapan con otra solicitud "
                            + s.getEstado().toLowerCase() + " del "
                            + s.getFechaInicio() + " al " + s.getFechaFin() + ".");
        }
    }
}
