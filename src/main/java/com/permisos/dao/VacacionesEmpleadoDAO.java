package com.permisos.dao;

import com.permisos.model.VacacionesEmpleado;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;

public class VacacionesEmpleadoDAO {

    public VacacionesEmpleado buscarPorEmpleadoYAnio(
            int idEmpleado,
            int anio) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT v FROM VacacionesEmpleado v " +
                                    "WHERE v.empleado.idEmpleado = :idEmpleado " +
                                    "AND v.anio = :anio",
                            VacacionesEmpleado.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .setParameter("anio", anio)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }

    /**
     * Descuenta días del saldo.
     * Se utiliza cuando una solicitud de vacaciones es APROBADA.
     */
    public void descontarDias(
            int idEmpleado,
            int anio,
            int dias) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            int filasActualizadas = em.createQuery(
                            "UPDATE VacacionesEmpleado v " +
                                    "SET v.diasUtilizados = v.diasUtilizados + :dias, " +
                                    "v.diasDisponibles = v.diasDisponibles - :dias " +
                                    "WHERE v.empleado.idEmpleado = :idEmpleado " +
                                    "AND v.anio = :anio"
                    )
                    .setParameter("dias", dias)
                    .setParameter("idEmpleado", idEmpleado)
                    .setParameter("anio", anio)
                    .executeUpdate();

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudieron descontar los días de vacaciones.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    /**
     * Devuelve días al saldo.
     * Se utiliza si una solicitud ya aprobada se cancela posteriormente.
     */
    public void devolverDias(
            int idEmpleado,
            int anio,
            int dias) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            int filasActualizadas = em.createQuery(
                            "UPDATE VacacionesEmpleado v " +
                                    "SET v.diasUtilizados = v.diasUtilizados - :dias, " +
                                    "v.diasDisponibles = v.diasDisponibles + :dias " +
                                    "WHERE v.empleado.idEmpleado = :idEmpleado " +
                                    "AND v.anio = :anio"
                    )
                    .setParameter("dias", dias)
                    .setParameter("idEmpleado", idEmpleado)
                    .setParameter("anio", anio)
                    .executeUpdate();

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudieron devolver los días de vacaciones.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}
