package com.permisos.dao;

import com.permisos.model.HistorialSolicitud;
import com.permisos.model.Solicitud;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitudDAO {

    public int insertar(Solicitud s) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            /*
             * La fecha de solicitud se genera aquí si todavía
             * no viene establecida.
             */
            if (s.getFechaSolicitud() == null) {
                s.setFechaSolicitud(LocalDateTime.now());
            }

            /*
             * Las solicitudes nuevas siempre comienzan PENDIENTES,
             * igual que en el INSERT JDBC original.
             */
            s.setEstado(Solicitud.ESTADO_PENDIENTE);

            em.persist(s);

            em.getTransaction().commit();

            return s.getIdSolicitud();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public List<Solicitud> listarPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT s FROM Solicitud s " +
                                    "WHERE s.empleado.idEmpleado = :idEmpleado " +
                                    "ORDER BY s.fechaSolicitud DESC",
                            Solicitud.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Solicitud> listarPendientesPorJefatura(int idEmpleadoJefe)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            /*
             * La consulta JDBC original hacía:
             *
             * solicitudes -> empleados -> jefaturas
             *
             * En JPA podemos navegar por las relaciones.
             */
            return em.createQuery(
                            "SELECT s FROM Solicitud s " +
                                    "JOIN s.empleado e " +
                                    "JOIN Jefatura j ON j.departamento.idDepartamento = " +
                                    "e.departamento.idDepartamento " +
                                    "WHERE j.empleado.idEmpleado = :idEmpleadoJefe " +
                                    "AND j.activo = true " +
                                    "AND s.estado = :estado " +
                                    "ORDER BY s.fechaSolicitud ASC",
                            Solicitud.class
                    )
                    .setParameter("idEmpleadoJefe", idEmpleadoJefe)
                    .setParameter("estado", Solicitud.ESTADO_PENDIENTE)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Solicitud> listarParaRRHH() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT s FROM Solicitud s " +
                                    "WHERE s.estado IN (:aprobada, :rechazada) " +
                                    "ORDER BY s.fechaRespuesta DESC",
                            Solicitud.class
                    )
                    .setParameter("aprobada", Solicitud.ESTADO_APROBADA)
                    .setParameter("rechazada", Solicitud.ESTADO_RECHAZADA)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public Solicitud buscarPorId(int idSolicitud)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Solicitud.class, idSolicitud);

        } finally {
            em.close();
        }
    }

    /**
     * Aprueba una solicitud y registra el historial
     * dentro de la misma transacción JPA.
     */
    public void aprobar(
            int idSolicitud,
            int idEmpleadoJefe,
            int idUsuarioJefatura) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud == null) {
                throw new SQLException(
                        "No se encontró la solicitud."
                );
            }

            solicitud.setEstado(Solicitud.ESTADO_APROBADA);
            solicitud.setFechaRespuesta(LocalDateTime.now());
            solicitud.setIdJefaturaRespuesta(idEmpleadoJefe);

            HistorialSolicitud h = new HistorialSolicitud();

            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_APROBADA);
            h.setIdUsuario(idUsuarioJefatura);
            h.setFecha(LocalDateTime.now());

            em.persist(h);

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
                    "No se pudo aprobar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    /**
     * Rechaza una solicitud y registra el historial
     * dentro de la misma transacción JPA.
     */
    public void rechazar(
            int idSolicitud,
            int idEmpleadoJefe,
            String motivoRechazo,
            int idUsuarioJefatura) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud == null) {
                throw new SQLException(
                        "No se encontró la solicitud."
                );
            }

            solicitud.setEstado(Solicitud.ESTADO_RECHAZADA);
            solicitud.setFechaRespuesta(LocalDateTime.now());
            solicitud.setIdJefaturaRespuesta(idEmpleadoJefe);
            solicitud.setMotivoRechazo(motivoRechazo);

            HistorialSolicitud h = new HistorialSolicitud();

            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_RECHAZADA);
            h.setComentario(motivoRechazo);
            h.setIdUsuario(idUsuarioJefatura);
            h.setFecha(LocalDateTime.now());

            em.persist(h);

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
                    "No se pudo rechazar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    /**
     * Cancela una solicitud solamente si está PENDIENTE.
     * El cambio y el historial se guardan en la misma transacción.
     */
    public boolean cancelar(
            int idSolicitud,
            int idUsuario) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud == null ||
                    !Solicitud.ESTADO_PENDIENTE.equals(
                            solicitud.getEstado())) {

                em.getTransaction().rollback();
                return false;
            }

            solicitud.setEstado(Solicitud.ESTADO_CANCELADA);

            HistorialSolicitud h = new HistorialSolicitud();

            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_CANCELADA);
            h.setIdUsuario(idUsuario);
            h.setFecha(LocalDateTime.now());

            em.persist(h);

            em.getTransaction().commit();

            return true;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo cancelar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    /**
     * RRHH marca una solicitud ya resuelta como recibida/procesada.
     */
    public void marcarRecibidaPorRRHH(
            int idSolicitud,
            String observaciones) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud != null) {
                solicitud.setObservacionesRrhh(observaciones);
                solicitud.setFechaRecepcionRrhh(
                        LocalDateTime.now()
                );
            }

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo marcar la solicitud como recibida por RRHH.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}
