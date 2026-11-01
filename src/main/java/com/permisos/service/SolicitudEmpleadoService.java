package com.permisos.service;

import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.Empleado;
import com.permisos.model.HistorialSolicitud;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;
import com.permisos.model.VacacionesEmpleado;
import com.permisos.service.ReglasVacaciones.Saldo;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Operaciones de un EMPLEADO sobre SUS PROPIAS solicitudes. Pensado para ser
 * usado por la API REST, sin depender de Jersey, de la sesión HTTP ni de JSF:
 * recibe el id del empleado ya identificado y devuelve datos o lanza excepciones.
 *
 * Cómo se traducen las excepciones a HTTP (lo hace la capa REST, no este servicio):
 *   ValidacionException          -> 400 (lleva la lista "campo: problema")
 *   RecursoNoEncontradoException -> 404
 *   ConflictoException           -> 409
 *   ReglaNegocioException        -> 409 (p. ej. saldo insuficiente, como indica el contrato)
 *   SQLException                 -> 500
 *
 * Seguridad: TODAS las consultas incluyen el id del empleado en el WHERE, así
 * nadie puede ver ni tocar solicitudes ajenas aunque conozca el identificador.
 *
 * Las solicitudes que devuelve traen YA CARGADOS el tipo y el empleado (las
 * relaciones son LAZY): se pueden convertir a JSON después de cerrar la sesión
 * sin LazyInitializationException.
 */
public class SolicitudEmpleadoService {

    private static final String TIPO_INCAPACIDAD = "INCAPACIDAD";

    private final TipoSolicitudDAO tipoSolicitudDAO = new TipoSolicitudDAO();

    // ------------------------------------------------------------------
    // Crear
    // ------------------------------------------------------------------

    /**
     * Crea una solicitud PENDIENTE del empleado y la devuelve ya cargada.
     * Aplica las mismas reglas que el formulario web: motivo, fechas, solo
     * lunes a sábado, no fechas pasadas y, si son VACACIONES, saldo suficiente.
     *
     * Las incapacidades no se crean aquí (llevan documento y su propio
     * endpoint): se rechazan con un mensaje claro.
     *
     * Si son vacaciones, la validación del saldo y el guardado ocurren en UNA
     * sola transacción, con la fila del saldo bloqueada: dos peticiones
     * simultáneas del mismo empleado no pueden gastar dos veces los mismos días.
     */
    public Solicitud crear(int idEmpleado, Integer idTipo, String motivo,
                           LocalDate inicio, LocalDate fin)
            throws ReglaNegocioException, SQLException {

        int dias = ReglasSolicitud.validarNuevaSolicitud(idTipo, motivo, inicio, fin, LocalDate.now());

        TipoSolicitud tipo = tipoSolicitudDAO.buscarPorId(idTipo);
        if (tipo == null || !tipo.isActivo()) {
            throw new ValidacionException("tipoSolicitudId: el tipo indicado no existe");
        }
        if (TIPO_INCAPACIDAD.equals(tipo.getNombre())) {
            throw new ValidacionException(
                    "tipoSolicitudId: las incapacidades se registran con POST /api/incapacidades");
        }

        int idSolicitud;
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            if (ReglasVacaciones.TIPO_VACACIONES.equals(tipo.getNombre())) {
                int anio = inicio.getYear();
                VacacionesEmpleado saldo = buscarSaldo(em, idEmpleado, anio, true);
                int pendientes = sumarDiasPendientes(em, idEmpleado, anio);
                ReglasVacaciones.validarSaldoParaSolicitar(aSaldo(saldo), pendientes, dias);
            }

            Solicitud nueva = new Solicitud();
            nueva.setEmpleado(em.getReference(Empleado.class, idEmpleado));
            nueva.setTipoSolicitud(em.getReference(TipoSolicitud.class, tipo.getIdTipoSolicitud()));
            nueva.setFechaSolicitud(LocalDateTime.now());
            nueva.setFechaInicio(inicio);
            nueva.setFechaFin(fin);
            nueva.setDiasSolicitados(dias);
            nueva.setMotivo(motivo.trim());
            nueva.setEstado(Solicitud.ESTADO_PENDIENTE);

            em.persist(nueva);
            em.getTransaction().commit();

            idSolicitud = nueva.getIdSolicitud();

        } catch (ReglaNegocioException ex) {
            revertir(em);
            throw ex;

        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo registrar la solicitud.", ex);

        } finally {
            em.close();
        }

        // Se vuelve a leer con las relaciones cargadas, lista para convertir a JSON.
        return detalle(idEmpleado, idSolicitud);
    }

    // ------------------------------------------------------------------
    // Consultar
    // ------------------------------------------------------------------

    /**
     * Solicitudes del empleado, de la más reciente a la más antigua, con los
     * filtros indicados. Los valores del filtro viajan siempre como
     * PARÁMETROS de la consulta, nunca concatenados.
     */
    public List<Solicitud> listarMias(int idEmpleado, FiltroSolicitudes filtro) throws SQLException {

        StringBuilder jpql = new StringBuilder(
                "SELECT s FROM Solicitud s JOIN FETCH s.tipoSolicitud JOIN FETCH s.empleado "
                        + "WHERE s.empleado.idEmpleado = :idEmpleado");

        if (filtro.getEstado() != null) {
            jpql.append(" AND s.estado = :estado");
        }
        if (filtro.getIdTipo() != null) {
            jpql.append(" AND s.tipoSolicitud.idTipoSolicitud = :idTipo");
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
            TypedQuery<Solicitud> consulta = em.createQuery(jpql.toString(), Solicitud.class);
            consulta.setParameter("idEmpleado", idEmpleado);

            if (filtro.getEstado() != null) {
                consulta.setParameter("estado", filtro.getEstado());
            }
            if (filtro.getIdTipo() != null) {
                consulta.setParameter("idTipo", filtro.getIdTipo());
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

    /**
     * Una solicitud del empleado.
     *
     * @throws RecursoNoEncontradoException si no existe O NO ES SUYA (mismo
     *         mensaje en ambos casos: no se revela que el id existe)
     */
    public Solicitud detalle(int idEmpleado, int idSolicitud)
            throws RecursoNoEncontradoException, SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            List<Solicitud> encontradas = em.createQuery(
                            "SELECT s FROM Solicitud s JOIN FETCH s.tipoSolicitud JOIN FETCH s.empleado "
                                    + "WHERE s.idSolicitud = :idSolicitud "
                                    + "AND s.empleado.idEmpleado = :idEmpleado",
                            Solicitud.class)
                    .setParameter("idSolicitud", idSolicitud)
                    .setParameter("idEmpleado", idEmpleado)
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

    // ------------------------------------------------------------------
    // Cancelar
    // ------------------------------------------------------------------

    /**
     * Cancela una solicitud PENDIENTE del empleado. La fila se lee con
     * PESSIMISTIC_WRITE: si la jefatura la está aprobando justo en ese
     * instante, una de las dos operaciones espera a la otra y la segunda ve el
     * estado ya cambiado.
     *
     * Solo se cancela directamente lo que sigue PENDIENTE. Una solicitud ya
     * resuelta responde con ConflictoException (409).
     *
     * PENDIENTE DE DEFINIR POR EL EQUIPO: "pedir la cancelación" de una
     * solicitud ya APROBADA. Requiere decidir dónde se guarda esa petición y
     * quién la responde (probablemente cambia el SQL). Cuando se defina, el
     * punto de extensión es la rama de ConflictoException de este método.
     *
     * @throws RecursoNoEncontradoException si no existe o no es del empleado (404)
     * @throws ConflictoException           si ya no está pendiente (409)
     */
    public void cancelar(int idSolicitud, int idEmpleado, int idUsuario)
            throws ReglaNegocioException, SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud = em.find(Solicitud.class, idSolicitud, LockModeType.PESSIMISTIC_WRITE);

            if (solicitud == null || solicitud.getEmpleado().getIdEmpleado() != idEmpleado) {
                throw new RecursoNoEncontradoException("La solicitud indicada no existe.");
            }
            if (!Solicitud.ESTADO_PENDIENTE.equals(solicitud.getEstado())) {
                throw new ConflictoException(mensajeNoCancelable(solicitud.getEstado()));
            }

            solicitud.setEstado(Solicitud.ESTADO_CANCELADA);

            HistorialSolicitud h = new HistorialSolicitud();
            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_CANCELADA);
            h.setComentario("Cancelada por el empleado");
            h.setIdUsuario(idUsuario);
            h.setFecha(LocalDateTime.now());
            em.persist(h);

            em.getTransaction().commit();

        } catch (ReglaNegocioException ex) {
            revertir(em);
            throw ex;

        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo cancelar la solicitud.", ex);

        } finally {
            em.close();
        }
    }

    static String mensajeNoCancelable(String estado) {
        if (Solicitud.ESTADO_CANCELADA.equals(estado)) {
            return "La solicitud ya está cancelada.";
        }
        if (Solicitud.ESTADO_RECHAZADA.equals(estado)) {
            return "La solicitud ya fue rechazada y no se puede cancelar.";
        }
        if (Solicitud.ESTADO_APROBADA.equals(estado)) {
            return "La solicitud ya fue aprobada. La cancelación de solicitudes aprobadas "
                    + "todavía no está disponible.";
        }
        return "La solicitud ya no está pendiente.";
    }

    // ------------------------------------------------------------------
    // Saldo de vacaciones
    // ------------------------------------------------------------------

    /**
     * Saldo de vacaciones del empleado en un año, con tomados, pendientes y
     * disponibles.
     *
     * @throws RecursoNoEncontradoException si no hay saldo registrado ese año (404)
     */
    public ResumenVacaciones saldoVacaciones(int idEmpleado, int anio)
            throws RecursoNoEncontradoException, SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            VacacionesEmpleado saldo = buscarSaldo(em, idEmpleado, anio, false);

            if (saldo == null) {
                throw new RecursoNoEncontradoException(
                        "No hay saldo de vacaciones registrado para " + anio + ".");
            }

            int pendientes = sumarDiasPendientes(em, idEmpleado, anio);

            return ResumenVacaciones.calcular(
                    anio,
                    saldo.getDiasAsignados(),
                    saldo.getDiasUtilizados(),
                    saldo.getDiasDisponibles(),
                    pendientes);

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo consultar el saldo de vacaciones.", ex);
        } finally {
            em.close();
        }
    }

    // ------------------------------------------------------------------
    // Utilidades privadas (mismas consultas que usa SolicitudService)
    // ------------------------------------------------------------------

    private VacacionesEmpleado buscarSaldo(EntityManager em, int idEmpleado, int anio, boolean bloquear) {
        TypedQuery<VacacionesEmpleado> consulta = em.createQuery(
                        "SELECT v FROM VacacionesEmpleado v "
                                + "WHERE v.empleado.idEmpleado = :idEmpleado AND v.anio = :anio",
                        VacacionesEmpleado.class)
                .setParameter("idEmpleado", idEmpleado)
                .setParameter("anio", anio);

        if (bloquear) {
            consulta.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }

        List<VacacionesEmpleado> resultado = consulta.getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    /** Suma de días de las solicitudes PENDIENTES de vacaciones que empiezan en ese año. */
    private int sumarDiasPendientes(EntityManager em, int idEmpleado, int anio) {
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

        return (total == null) ? 0 : total.intValue();
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
