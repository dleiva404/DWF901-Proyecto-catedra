package com.permisos.service;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.model.Constancia;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

// constancias: empleado y jefatura ven las suyas, RRHH ve todas
public class ConstanciaService {

    private final ConstanciaDAO constanciaDAO = new ConstanciaDAO();

    public List<Constancia> listar(int idEmpleado, boolean verTodas) throws SQLException {

        String jpql = "SELECT c FROM Constancia c JOIN FETCH c.empleado "
                + (verTodas ? "" : "WHERE c.empleado.idEmpleado = :idEmpleado ")
                + "ORDER BY c.fechaSolicitud DESC, c.idConstancia DESC";

        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<Constancia> consulta = em.createQuery(jpql, Constancia.class);

            if (!verTodas) {
                consulta.setParameter("idEmpleado", idEmpleado);
            }

            return consulta.getResultList();

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudieron consultar las constancias.", ex);

        } finally {
            em.close();
        }
    }

    // si no existe o no es de quien consulta (y no es RRHH) responde igual: no se revela que existe
    public Constancia detalle(int idEmpleado, boolean verTodas, int idConstancia)
            throws RecursoNoEncontradoException, SQLException {

        String jpql = "SELECT c FROM Constancia c JOIN FETCH c.empleado "
                + "WHERE c.idConstancia = :idConstancia"
                + (verTodas ? "" : " AND c.empleado.idEmpleado = :idEmpleado");

        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<Constancia> consulta = em.createQuery(jpql, Constancia.class)
                    .setParameter("idConstancia", idConstancia);

            if (!verTodas) {
                consulta.setParameter("idEmpleado", idEmpleado);
            }

            List<Constancia> encontradas = consulta.getResultList();

            if (encontradas.isEmpty()) {
                throw new RecursoNoEncontradoException("La constancia indicada no existe.");
            }
            return encontradas.get(0);

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo consultar la constancia.", ex);

        } finally {
            em.close();
        }
    }

    public Constancia crear(int idEmpleado, String tipo, String institucion, String motivo)
            throws ReglaNegocioException, SQLException {

        Constancia nueva = new Constancia();
        nueva.setIdEmpleado(idEmpleado);
        nueva.setTipo(tipo);
        nueva.setInstitucion(institucion);
        nueva.setMotivo(motivo);
        nueva.setSalarioReferencia(BigDecimal.ZERO);
        nueva.setEmpresaEmisora(empresaEmisora(idEmpleado));
        nueva.setTokenVerificacion(UUID.randomUUID().toString().substring(0, 15));

        if (!constanciaDAO.insertar(nueva)) {
            throw new ConflictoException("No se pudo registrar la constancia. "
                    + "Si es salarial, verifique que el empleado tenga un salario activo.");
        }

        // se vuelve a leer para devolver la constancia completa
        return detalle(idEmpleado, false, nueva.getIdConstancia());
    }

        // RRHH aprueba una constancia pendiente
    public void aprobar(int idConstancia, int idUsuarioRrhh, String observaciones)
            throws ReglaNegocioException, SQLException {

        validarPendiente(idConstancia);
        constanciaDAO.aprobar(idConstancia, idUsuarioRrhh, observaciones);
    }

    // RRHH rechaza una constancia pendiente
    public void rechazar(int idConstancia, int idUsuarioRrhh, String motivoRechazo, String observaciones)
            throws ReglaNegocioException, SQLException {

        validarPendiente(idConstancia);
        constanciaDAO.rechazar(idConstancia, idUsuarioRrhh, motivoRechazo, observaciones);
    }

    // 404 si no existe, 409 si ya fue aprobada o rechazada
    private void validarPendiente(int idConstancia) throws ReglaNegocioException, SQLException {

        Constancia c = detalle(0, true, idConstancia);

        if (!Constancia.ESTADO_PENDIENTE.equals(c.getEstado())) {
            throw new ConflictoException("La constancia ya fue procesada.");
        }
    }

    // misma regla de ConstanciaServlet: empresa afiliada, o "Didelco " + sucursal
    private String empresaEmisora(int idEmpleado) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            List<Object[]> filas = em.createQuery(
                            "SELECT e.empresa, s.nombre FROM Empleado e "
                                    + "JOIN e.sucursalArea s WHERE e.idEmpleado = :id", Object[].class)
                    .setParameter("id", idEmpleado)
                    .getResultList();

            String empresa = filas.isEmpty() ? null : (String) filas.get(0)[0];
            String sucursal = filas.isEmpty() ? null : (String) filas.get(0)[1];

            if (empresa != null && !empresa.trim().isEmpty()) {
                return empresa;
            }

            String base = (sucursal != null) ? sucursal : "Central";
            return base.toLowerCase().startsWith("didelco") ? base : "Didelco " + base;

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo determinar la empresa emisora.", ex);

        } finally {
            em.close();
        }
    }
}