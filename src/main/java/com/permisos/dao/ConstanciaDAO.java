package com.permisos.dao;

import com.permisos.model.Constancia;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ConstanciaDAO {

    public int insertar(Constancia constancia) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            if (constancia.getFechaSolicitud() == null) {
                constancia.setFechaSolicitud(LocalDateTime.now());
            }

            constancia.setEstado(Constancia.ESTADO_PENDIENTE);

            em.persist(constancia);

            em.getTransaction().commit();

            return constancia.getIdConstancia();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar la solicitud de constancia.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public List<Constancia> listarPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT c FROM Constancia c " +
                                    "WHERE c.empleado.idEmpleado = :idEmpleado " +
                                    "ORDER BY c.fechaSolicitud DESC",
                            Constancia.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Constancia> listarPendientes()
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT c FROM Constancia c " +
                                    "WHERE c.estado = :estado " +
                                    "ORDER BY c.fechaSolicitud ASC",
                            Constancia.class
                    )
                    .setParameter("estado", Constancia.ESTADO_PENDIENTE)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Constancia> listarAprobadas()
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT c FROM Constancia c " +
                                    "WHERE c.estado = :estado " +
                                    "ORDER BY c.fechaRespuesta DESC",
                            Constancia.class
                    )
                    .setParameter("estado", Constancia.ESTADO_APROBADA)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public Constancia buscarPorId(int idConstancia)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Constancia.class, idConstancia);

        } finally {
            em.close();
        }
    }

    public void aprobar(
            int idConstancia,
            int idUsuarioRrhh,
            String observaciones) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Constancia constancia =
                    em.find(Constancia.class, idConstancia);

            if (constancia == null) {
                throw new SQLException(
                        "No se encontró la constancia."
                );
            }

            if (!Constancia.ESTADO_PENDIENTE.equals(
                    constancia.getEstado())) {

                throw new SQLException(
                        "La constancia ya fue procesada."
                );
            }

            constancia.setEstado(Constancia.ESTADO_APROBADA);
            constancia.setFechaRespuesta(LocalDateTime.now());
            constancia.setIdUsuarioRrhh(idUsuarioRrhh);
            constancia.setObservacionesRrhh(observaciones);
            constancia.setMotivoRechazo(null);

            em.getTransaction().commit();

        } catch (SQLException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw ex;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo aprobar la constancia.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public void rechazar(
            int idConstancia,
            int idUsuarioRrhh,
            String motivoRechazo,
            String observaciones) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Constancia constancia =
                    em.find(Constancia.class, idConstancia);

            if (constancia == null) {
                throw new SQLException(
                        "No se encontró la constancia."
                );
            }

            if (!Constancia.ESTADO_PENDIENTE.equals(
                    constancia.getEstado())) {

                throw new SQLException(
                        "La constancia ya fue procesada."
                );
            }

            constancia.setEstado(Constancia.ESTADO_RECHAZADA);
            constancia.setFechaRespuesta(LocalDateTime.now());
            constancia.setIdUsuarioRrhh(idUsuarioRrhh);
            constancia.setMotivoRechazo(motivoRechazo);
            constancia.setObservacionesRrhh(observaciones);

            em.getTransaction().commit();

        } catch (SQLException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw ex;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo rechazar la constancia.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}