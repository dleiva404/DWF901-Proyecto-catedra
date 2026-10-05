package com.permisos.dao;

import com.permisos.model.Incapacidad;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class IncapacidadDAO {

    public int insertar(Incapacidad incapacidad)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(incapacidad);

            em.getTransaction().commit();

            return incapacidad.getIdIncapacidad();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar la incapacidad.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public Incapacidad buscarPorId(int idIncapacidad)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(
                    Incapacidad.class,
                    idIncapacidad
            );

        } finally {
            em.close();
        }
    }

    public List<Incapacidad> listarPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT i FROM Incapacidad i " +
                                    "WHERE i.empleado.idEmpleado = :idEmpleado " +
                                    "ORDER BY i.fechaInicio DESC",
                            Incapacidad.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Incapacidad> listarTodas()
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT i FROM Incapacidad i " +
                                    "ORDER BY i.fechaInicio DESC",
                            Incapacidad.class
                    )
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public void actualizar(Incapacidad incapacidad)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.merge(incapacidad);

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo actualizar la incapacidad.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}
