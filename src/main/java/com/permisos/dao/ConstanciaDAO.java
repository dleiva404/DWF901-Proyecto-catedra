package com.permisos.dao;

import com.permisos.model.Constancia;
import com.permisos.model.Empleado;
import com.permisos.model.SalarioEmpleado;
import com.permisos.model.Usuario;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ConstanciaDAO {

    /*
     * Mantiene la firma que utilizan los Servlets actuales.
     * Internamente utiliza JPA/Hibernate.
     */
    public boolean insertar(Constancia constancia) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            if (constancia.getFechaSolicitud() == null) {
                constancia.setFechaSolicitud(LocalDateTime.now());
            }

            constancia.setEstado(Constancia.ESTADO_PENDIENTE);

            /*
             * Asociamos un Empleado administrado por JPA.
             */
            int idEmpleado = constancia.getIdEmpleado();

            if (idEmpleado <= 0) {
                em.getTransaction().rollback();
                return false;
            }

            constancia.setEmpleado(
                    em.getReference(Empleado.class, idEmpleado)
            );

            /*
             * Si la constancia es salarial, obtiene autom?ticamente
             * el salario activo registrado para el empleado.
             */
            if (constancia.getTipo() != null
                    && constancia.getTipo().toUpperCase().contains("SALAR")) {

                List<SalarioEmpleado> salarios =
                        em.createQuery(
                                        "SELECT s FROM SalarioEmpleado s " +
                                                "WHERE s.empleado.idEmpleado = :idEmpleado " +
                                                "AND s.activo = true " +
                                                "ORDER BY s.fechaActualizacion DESC",
                                        SalarioEmpleado.class
                                )
                                .setParameter("idEmpleado", idEmpleado)
                                .setMaxResults(1)
                                .getResultList();

                if (!salarios.isEmpty()) {
                    constancia.setSalarioReferencia(
                            salarios.get(0).getSalario()
                    );
                }
            }

            em.persist(constancia);

            em.getTransaction().commit();

            return true;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            System.err.println(
                    "Error al insertar constancia: " + ex.getMessage()
            );

            return false;

        } finally {
            em.close();
        }
    }

    /*
     * M?todo utilizado por PDF y otras funcionalidades existentes.
     */
    public Constancia obtenerPorId(int idConstancia) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Constancia.class, idConstancia);

        } finally {
            em.close();
        }
    }

    /*
     * Mantiene compatibilidad con el c?digo anterior.
     */
    public Constancia buscarPorId(int idConstancia)
            throws SQLException {

        return obtenerPorId(idConstancia);
    }

    /*
     * Busca una constancia mediante el token utilizado por QR.
     */
    public Constancia obtenerPorToken(String token) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            List<Constancia> resultado =
                    em.createQuery(
                                    "SELECT c FROM Constancia c " +
                                            "WHERE c.tokenVerificacion = :token",
                                    Constancia.class
                            )
                            .setParameter("token", token)
                            .setMaxResults(1)
                            .getResultList();

            return resultado.isEmpty()
                    ? null
                    : resultado.get(0);

        } finally {
            em.close();
        }
    }

    /*
     * M?todo utilizado por la vista del empleado.
     */
    public List<Constancia> obtenerPorEmpleado(int idEmpleado) {

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

    /*
     * Alias de compatibilidad con la implementaci?n JPA original.
     */
    public List<Constancia> listarPorEmpleado(int idEmpleado)
            throws SQLException {

        return obtenerPorEmpleado(idEmpleado);
    }

    /*
     * Utilizado por la bandeja administrativa de RRHH.
     */
    public List<Constancia> obtenerTodas() {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT c FROM Constancia c " +
                                    "ORDER BY c.fechaSolicitud DESC",
                            Constancia.class
                    )
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
                    .setParameter(
                            "estado",
                            Constancia.ESTADO_PENDIENTE
                    )
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
                    .setParameter(
                            "estado",
                            Constancia.ESTADO_APROBADA
                    )
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /*
     * Mantiene el m?todo utilizado actualmente por
     * AdminConstanciaServlet.
     */
    public boolean actualizarEstado(
            int idConstancia,
            String nuevoEstado) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Constancia constancia =
                    em.find(Constancia.class, idConstancia);

            if (constancia == null) {
                em.getTransaction().rollback();
                return false;
            }

            /*
             * Unificamos los estados utilizados por el c?digo anterior
             * con los estados definidos en la entidad.
             */
            if ("APROBADO".equalsIgnoreCase(nuevoEstado)
                    || "APROBADA".equalsIgnoreCase(nuevoEstado)) {

                constancia.setEstado(
                        Constancia.ESTADO_APROBADA
                );

            } else if ("RECHAZADO".equalsIgnoreCase(nuevoEstado)
                    || "RECHAZADA".equalsIgnoreCase(nuevoEstado)) {

                constancia.setEstado(
                        Constancia.ESTADO_RECHAZADA
                );

            } else {

                constancia.setEstado(nuevoEstado);
            }

            constancia.setFechaRespuesta(
                    LocalDateTime.now()
            );

            em.getTransaction().commit();

            return true;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            System.err.println(
                    "Error al actualizar el estado: " +
                            ex.getMessage()
            );

            return false;

        } finally {
            em.close();
        }
    }

    /*
     * Aprobaci?n completa registrando al usuario de RRHH.
     */
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
                        "No se encontr? la constancia."
                );
            }

            if (!Constancia.ESTADO_PENDIENTE.equals(
                    constancia.getEstado())) {

                throw new SQLException(
                        "La constancia ya fue procesada."
                );
            }

            constancia.setEstado(
                    Constancia.ESTADO_APROBADA
            );

            constancia.setFechaRespuesta(
                    LocalDateTime.now()
            );

            constancia.setUsuarioRrhh(
                    em.getReference(
                            Usuario.class,
                            idUsuarioRrhh
                    )
            );

            constancia.setObservacionesRrhh(
                    observaciones
            );

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

    /*
     * Rechazo completo registrando al usuario de RRHH.
     */
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
                        "No se encontr? la constancia."
                );
            }

            if (!Constancia.ESTADO_PENDIENTE.equals(
                    constancia.getEstado())) {

                throw new SQLException(
                        "La constancia ya fue procesada."
                );
            }

            constancia.setEstado(
                    Constancia.ESTADO_RECHAZADA
            );

            constancia.setFechaRespuesta(
                    LocalDateTime.now()
            );

            constancia.setUsuarioRrhh(
                    em.getReference(
                            Usuario.class,
                            idUsuarioRrhh
                    )
            );

            constancia.setMotivoRechazo(
                    motivoRechazo
            );

            constancia.setObservacionesRrhh(
                    observaciones
            );

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
