package com.permisos.dao;

import com.permisos.model.Departamento;
import com.permisos.model.Empleado;
import com.permisos.model.SucursalArea;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class EmpleadoDAO {

    public int insertar(Empleado e) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(e);

            em.getTransaction().commit();

            return e.getIdEmpleado();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar el empleado.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public List<Empleado> listarActivos() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Empleado e " +
                            "WHERE e.activo = true " +
                            "ORDER BY e.nombre, e.apellido",
                    Empleado.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public Empleado buscarPorId(int idEmpleado) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Empleado.class, idEmpleado);

        } finally {
            em.close();
        }
    }

    public List<Empleado> listarPorDepartamento(int idDepartamento)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM Empleado e " +
                                    "WHERE e.departamento.idDepartamento = :idDepartamento " +
                                    "AND e.activo = true " +
                                    "ORDER BY e.nombre, e.apellido",
                            Empleado.class
                    )
                    .setParameter("idDepartamento", idDepartamento)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public void actualizar(Empleado e) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.merge(e);

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo actualizar el empleado.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}