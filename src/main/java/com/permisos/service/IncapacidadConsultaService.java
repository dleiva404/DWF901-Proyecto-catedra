package com.permisos.service;

import com.permisos.model.Incapacidad;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;

// consultas de incapacidades: empleado y jefatura ven las suyas, RRHH ve todas
public class IncapacidadConsultaService {

    public List<Incapacidad> listar(int idEmpleado, boolean verTodas) throws SQLException {

        String jpql = "SELECT i FROM Incapacidad i JOIN FETCH i.solicitud "
                + (verTodas ? "" : "WHERE i.solicitud.empleado.idEmpleado = :idEmpleado ")
                + "ORDER BY i.fechaInicio DESC, i.idIncapacidad DESC";

        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<Incapacidad> consulta = em.createQuery(jpql, Incapacidad.class);

            if (!verTodas) {
                consulta.setParameter("idEmpleado", idEmpleado);
            }

            return consulta.getResultList();

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudieron consultar las incapacidades.", ex);

        } finally {
            em.close();
        }
    }

    // si no existe o no es de quien consulta (y no es RRHH) responde igual: no se revela que existe
    public Incapacidad detalle(int idEmpleado, boolean verTodas, int idIncapacidad)
            throws RecursoNoEncontradoException, SQLException {

        String jpql = "SELECT i FROM Incapacidad i JOIN FETCH i.solicitud "
                + "WHERE i.idIncapacidad = :idIncapacidad"
                + (verTodas ? "" : " AND i.solicitud.empleado.idEmpleado = :idEmpleado");

        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<Incapacidad> consulta = em.createQuery(jpql, Incapacidad.class)
                    .setParameter("idIncapacidad", idIncapacidad);

            if (!verTodas) {
                consulta.setParameter("idEmpleado", idEmpleado);
            }

            List<Incapacidad> encontradas = consulta.getResultList();

            if (encontradas.isEmpty()) {
                throw new RecursoNoEncontradoException("La incapacidad indicada no existe.");
            }
            return encontradas.get(0);

        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo consultar la incapacidad.", ex);

        } finally {
            em.close();
        }
    }
}