package com.permisos.service;

import com.permisos.model.HistorialSolicitud;
import com.permisos.model.Solicitud;
import com.permisos.model.VacacionesEmpleado;
import com.permisos.service.ReglasVacaciones.Saldo;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class SolicitudService {

  /**
 * @throws ReglaNegocioException si el rango es inválido o no alcanza el saldo
 * @throws SQLException          si falla la consulta
 */
    public void validarVacaciones(int idEmpleado, LocalDate inicio, LocalDate fin)
            throws ReglaNegocioException, SQLException {

        int diasSolicitados = ReglasVacaciones.calcularDias(inicio, fin);
        int anio = inicio.getYear();

        EntityManager em = JPAUtil.getEntityManager();

        try {
            VacacionesEmpleado v = buscarSaldo(em, idEmpleado, anio, false);

            // SUM de un int devuelve Long en JPA; es null si no hay filas.
            Long total = em.createQuery(
                            "SELECT SUM(s.diasSolicitados) FROM Solicitud s "
                                    + "WHERE s.empleado.idEmpleado = :idEmpleado "
                                    + "AND s.estado = :estado "
                                    + "AND s.tipoSolicitud.nombre = :tipo "
                                    + "AND s.fechaInicio >= :desde "
                                    + "AND s.fechaInicio <= :hasta",
                            Long.class)
                    .setParameter("idEmpleado", idEmpleado)
                    .setParameter("estado", Solicitud.ESTADO_PENDIENTE)
                    .setParameter("tipo", ReglasVacaciones.TIPO_VACACIONES)
                    .setParameter("desde", LocalDate.of(anio, 1, 1))
                    .setParameter("hasta", LocalDate.of(anio, 12, 31))
                    .getSingleResult();

            int diasPendientes = (total == null) ? 0 : total.intValue();

            ReglasVacaciones.validarSaldoParaSolicitar(aSaldo(v), diasPendientes, diasSolicitados);

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo validar el saldo de vacaciones.", ex);
        } finally {
            em.close();
        }
    }

    public void aprobar(int idSolicitud, int idEmpleadoJefe, int idUsuarioJefatura)
            throws ReglaNegocioException, SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud = em.find(Solicitud.class, idSolicitud, LockModeType.PESSIMISTIC_WRITE);

            if (solicitud == null) {
                throw new RecursoNoEncontradoException("La solicitud indicada no existe.");
            }

            validarQueEsJefeDelEmpleado(em, idEmpleadoJefe, solicitud);

            if (!Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado())) {
                throw new ReglaNegocioException("Solo se pueden aprobar solicitudes pendientes.");
            }

            String comentario = null;

            if (ReglasVacaciones.TIPO_VACACIONES.equals(solicitud.getTipoSolicitud().getNombre())) {

                int anio = solicitud.getFechaInicio().getYear();
                int idEmpleado = solicitud.getEmpleado().getIdEmpleado();

                VacacionesEmpleado v = buscarSaldo(em, idEmpleado, anio, true);

                if (v == null) {
                    throw new ReglaNegocioException(
                            "El empleado no tiene saldo de vacaciones registrado para " + anio + ".");
                }

                Saldo nuevo = ReglasVacaciones.descontar(aSaldo(v), solicitud.getDiasSolicitados());

                v.setDiasUtilizados(nuevo.getDiasUtilizados());
                v.setDiasDisponibles(nuevo.getDiasDisponibles());

                comentario = "Se descontaron " + solicitud.getDiasSolicitados()
                        + " día(s) del saldo de vacaciones " + anio + ".";
            }

            LocalDateTime ahora = LocalDateTime.now();

            solicitud.setEstado(Solicitud.ESTADO_APROBADA);
            solicitud.setFechaRespuesta(ahora);
            solicitud.setIdJefaturaRespuesta(idEmpleadoJefe);

            HistorialSolicitud h = new HistorialSolicitud();
            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_APROBADA);
            h.setComentario(comentario);
            h.setIdUsuario(idUsuarioJefatura);
            h.setFecha(ahora);

            em.persist(h);

            em.getTransaction().commit();

        } catch (ReglaNegocioException ex) {
            revertir(em);
            throw ex;

        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo aprobar la solicitud.", ex);

        } finally {
            em.close();
        }
    }

    public void rechazar(int idSolicitud, int idEmpleadoJefe, String motivoRechazo, int idUsuarioJefatura)
            throws ReglaNegocioException, SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud = em.find(Solicitud.class, idSolicitud, LockModeType.PESSIMISTIC_WRITE);

            if (solicitud == null) {
                throw new RecursoNoEncontradoException("La solicitud indicada no existe.");
            }

            validarQueEsJefeDelEmpleado(em, idEmpleadoJefe, solicitud);

            if (!Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado())) {
                throw new ReglaNegocioException("Solo se pueden rechazar solicitudes pendientes.");
            }

            LocalDateTime ahora = LocalDateTime.now();

            solicitud.setEstado(Solicitud.ESTADO_RECHAZADA);
            solicitud.setFechaRespuesta(ahora);
            solicitud.setIdJefaturaRespuesta(idEmpleadoJefe);
            solicitud.setMotivoRechazo(motivoRechazo);

            HistorialSolicitud h = new HistorialSolicitud();
            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_RECHAZADA);
            h.setComentario(motivoRechazo);
            h.setIdUsuario(idUsuarioJefatura);
            h.setFecha(ahora);

            em.persist(h);

            em.getTransaction().commit();

        } catch (ReglaNegocioException ex) {
            revertir(em);
            throw ex;

        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo rechazar la solicitud.", ex);

        } finally {
            em.close();
        }
    }

    public boolean esJefeDelEmpleado(int idEmpleadoJefe, int idEmpleado) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Long coincidencias = em.createQuery(
                            "SELECT COUNT(j) FROM Jefatura j, Empleado e "
                                    + "WHERE e.idEmpleado = :idEmpleado "
                                    + "AND j.empleado.idEmpleado = :idJefe "
                                    + "AND j.departamento.idDepartamento = e.departamento.idDepartamento "
                                    + "AND j.activo = true",
                            Long.class)
                    .setParameter("idEmpleado", idEmpleado)
                    .setParameter("idJefe", idEmpleadoJefe)
                    .getSingleResult();

            return coincidencias != null && coincidencias > 0;

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo verificar la jefatura del empleado.", ex);

        } finally {
            em.close();
        }
    }

    public Solicitud obtenerPorId(int idSolicitud) throws RecursoNoEncontradoException, SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            java.util.List<Solicitud> encontradas = em.createQuery(
                            "SELECT s FROM Solicitud s JOIN FETCH s.tipoSolicitud JOIN FETCH s.empleado "
                                    + "WHERE s.idSolicitud = :idSolicitud",
                            Solicitud.class)
                    .setParameter("idSolicitud", idSolicitud)
                    .getResultList();

            if (encontradas.isEmpty()) {
                throw new RecursoNoEncontradoException("La solicitud indicada no existe.");
            }
            return encontradas.get(0);

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo consultar la solicitud.", ex);

        } finally {
            em.close();
        }
    }

    public java.util.List<Solicitud> listarTodas(FiltroSolicitudes filtro) throws SQLException {
        return listarConFiltro(null, filtro);
    }

    public java.util.List<Solicitud> listarPorDepartamento(int idEmpleadoJefe, FiltroSolicitudes filtro)
            throws SQLException {
        return listarConFiltro(idEmpleadoJefe, filtro);
    }

    private java.util.List<Solicitud> listarConFiltro(Integer idJefe, FiltroSolicitudes filtro)
            throws SQLException {

        StringBuilder jpql = new StringBuilder(
                "SELECT s FROM Solicitud s JOIN FETCH s.tipoSolicitud JOIN FETCH s.empleado WHERE 1 = 1");

        if (idJefe != null) {
            jpql.append(" AND s.empleado.departamento.idDepartamento IN "
                    + "(SELECT j.departamento.idDepartamento FROM Jefatura j "
                    + "WHERE j.empleado.idEmpleado = :idJefe AND j.activo = true)");
        }
        if (filtro.getEstado() != null) {
            jpql.append(" AND s.estado = :estado");
        }
        if (filtro.getIdTipo() != null) {
            jpql.append(" AND s.tipoSolicitud.idTipoSolicitud = :idTipo");
        }
        if (filtro.getIdEmpleado() != null) {
            jpql.append(" AND s.empleado.idEmpleado = :idEmpleadoFiltro");
        }
        if (filtro.getIdDepartamento() != null) {
            jpql.append(" AND s.empleado.departamento.idDepartamento = :idDepartamentoFiltro");
        }
        if (filtro.getDesde() != null) {
            jpql.append(" AND s.fechaInicio >= :desde");
        }
        if (filtro.getHasta() != null) {
            jpql.append(" AND s.fechaInicio <= :hasta");
        }
        jpql.append(" ORDER BY s.fechaSolicitud DESC, s.idSolicitud DESC");

        EntityManager em = JPAUtil.getEntityManager();

        try {
            javax.persistence.TypedQuery<Solicitud> consulta = em.createQuery(jpql.toString(), Solicitud.class);

            if (idJefe != null) {
                consulta.setParameter("idJefe", idJefe);
            }
            if (filtro.getEstado() != null) {
                consulta.setParameter("estado", filtro.getEstado());
            }
            if (filtro.getIdTipo() != null) {
                consulta.setParameter("idTipo", filtro.getIdTipo());
            }
            if (filtro.getIdEmpleado() != null) {
                consulta.setParameter("idEmpleadoFiltro", filtro.getIdEmpleado());
            }
            if (filtro.getIdDepartamento() != null) {
                consulta.setParameter("idDepartamentoFiltro", filtro.getIdDepartamento());
            }
            if (filtro.getDesde() != null) {
                consulta.setParameter("desde", filtro.getDesde());
            }
            if (filtro.getHasta() != null) {
                consulta.setParameter("hasta", filtro.getHasta());
            }

            return consulta.getResultList();

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudieron consultar las solicitudes.", ex);

        } finally {
            em.close();
        }
    }

    public java.util.List<HistorialSolicitud> obtenerHistorial(int idSolicitud, int idEmpleado, String rol)
            throws RecursoNoEncontradoException, SQLException {

        Solicitud solicitud = obtenerPorId(idSolicitud);
        int idDuenio = solicitud.getEmpleado().getIdEmpleado();

        boolean puedeVer = "RRHH".equalsIgnoreCase(rol)
                || idDuenio == idEmpleado
                || ("JEFATURA".equalsIgnoreCase(rol) && esJefeDelEmpleado(idEmpleado, idDuenio));

        if (!puedeVer) {
            throw new RecursoNoEncontradoException("La solicitud indicada no existe.");
        }

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT h FROM HistorialSolicitud h JOIN FETCH h.solicitud LEFT JOIN FETCH h.usuario "
                                    + "WHERE h.solicitud.idSolicitud = :idSolicitud "
                                    + "ORDER BY h.fecha ASC, h.idHistorial ASC",
                            HistorialSolicitud.class)
                    .setParameter("idSolicitud", idSolicitud)
                    .getResultList();

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo consultar el historial.", ex);

        } finally {
            em.close();
        }
    }

    private void validarQueEsJefeDelEmpleado(EntityManager em, int idEmpleadoJefe, Solicitud solicitud)
            throws ReglaNegocioException {

        if (solicitud.getEmpleado().getIdEmpleado() == idEmpleadoJefe) {
            throw new AccesoDenegadoException("No puede aprobar ni rechazar sus propias solicitudes.");
        }

        int idDepartamento = solicitud.getEmpleado().getDepartamento().getIdDepartamento();

        Long coincidencias = em.createQuery(
                        "SELECT COUNT(j) FROM Jefatura j "
                                + "WHERE j.empleado.idEmpleado = :idJefe "
                                + "AND j.departamento.idDepartamento = :idDepartamento "
                                + "AND j.activo = true",
                        Long.class)
                .setParameter("idJefe", idEmpleadoJefe)
                .setParameter("idDepartamento", idDepartamento)
                .getSingleResult();

        if (coincidencias == null || coincidencias == 0) {
            throw new AccesoDenegadoException("No tiene permiso para aprobar solicitudes de este empleado.");
        }
    }

    private VacacionesEmpleado buscarSaldo(EntityManager em, int idEmpleado, int anio, boolean bloquear) {
        javax.persistence.TypedQuery<VacacionesEmpleado> q = em.createQuery(
                        "SELECT v FROM VacacionesEmpleado v "
                                + "WHERE v.empleado.idEmpleado = :idEmpleado AND v.anio = :anio",
                        VacacionesEmpleado.class)
                .setParameter("idEmpleado", idEmpleado)
                .setParameter("anio", anio);

        if (bloquear) {
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }

        java.util.List<VacacionesEmpleado> resultado = q.getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    private Saldo aSaldo(VacacionesEmpleado v) {
        if (v == null) {
            return null;
        }
        return new Saldo(v.getDiasAsignados(), v.getDiasUtilizados(), v.getDiasDisponibles());
    }

    private void revertir(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
