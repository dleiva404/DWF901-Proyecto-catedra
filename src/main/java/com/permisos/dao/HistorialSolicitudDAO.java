package com.permisos.dao;

import com.permisos.model.HistorialSolicitud;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class HistorialSolicitudDAO {

    public int insertar(HistorialSolicitud historial)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(historial);

            em.getTransaction().commit();

            return historial.getIdHistorial();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar el historial.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public List<HistorialSolicitud> listarPorSolicitud(
            int idSolicitud) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT h FROM HistorialSolicitud h " +
                                    "WHERE h.solicitud.idSolicitud = :idSolicitud " +
                                    "ORDER BY h.fecha DESC",
                            HistorialSolicitud.class
                    )
                    .setParameter("idSolicitud", idSolicitud)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<HistorialSolicitud> listarPorEmpleado(
            int idEmpleado) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT h FROM HistorialSolicitud h " +
                                    "WHERE h.solicitud.empleado.idEmpleado = :idEmpleado " +
                                    "ORDER BY h.fecha DESC",
                            HistorialSolicitud.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public HistorialSolicitud buscarPorId(
            int idHistorial) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(
                    HistorialSolicitud.class,
                    idHistorial
            );

        } finally {
            em.close();
        }
    }
}