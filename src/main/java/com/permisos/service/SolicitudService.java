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

    /
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
                throw new ReglaNegocioException("La solicitud indicada no existe.");
            }
            if (!Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado())) {
                throw new ReglaNegocioException("Solo se pueden aprobar solicitudes pendientes.");
            }

            validarQueEsJefeDelEmpleado(em, idEmpleadoJefe, solicitud);

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

   
    private void validarQueEsJefeDelEmpleado(EntityManager em, int idEmpleadoJefe, Solicitud solicitud)
            throws ReglaNegocioException {

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
            throw new ReglaNegocioException("No tiene permiso para aprobar solicitudes de este empleado.");
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
