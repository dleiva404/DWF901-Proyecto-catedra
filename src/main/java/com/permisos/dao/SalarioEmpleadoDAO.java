package com.permisos.dao;

import com.permisos.model.SalarioEmpleado;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SalarioEmpleadoDAO {

    public int insertar(SalarioEmpleado salarioEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            if (salarioEmpleado.getFechaActualizacion() == null) {
                salarioEmpleado.setFechaActualizacion(LocalDateTime.now());
            }

            salarioEmpleado.setActivo(true);

            em.persist(salarioEmpleado);

            em.getTransaction().commit();

            return salarioEmpleado.getIdSalario();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar el salario del empleado.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public SalarioEmpleado buscarActivoPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT s FROM SalarioEmpleado s " +
                                    "WHERE s.empleado.idEmpleado = :idEmpleado " +
                                    "AND s.activo = true " +
                                    "ORDER BY s.fechaActualizacion DESC",
                            SalarioEmpleado.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .setMaxResults(1)
                    .getSingleResult();

        } catch (NoResultException ex) {
            return null;

        } finally {
            em.close();
        }
    }

    public List<SalarioEmpleado> listarPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT s FROM SalarioEmpleado s " +
                                    "WHERE s.empleado.idEmpleado = :idEmpleado " +
                                    "ORDER BY s.fechaActualizacion DESC",
                            SalarioEmpleado.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public int registrarNuevoSalario(SalarioEmpleado nuevoSalario)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            int idEmpleado = nuevoSalario.getIdEmpleado();

            em.createQuery(
                            "UPDATE SalarioEmpleado s " +
                                    "SET s.activo = false " +
                                    "WHERE s.empleado.idEmpleado = :idEmpleado " +
                                    "AND s.activo = true"
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .executeUpdate();

            nuevoSalario.setActivo(true);
            nuevoSalario.setFechaActualizacion(LocalDateTime.now());

            em.persist(nuevoSalario);

            em.getTransaction().commit();

            return nuevoSalario.getIdSalario();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo actualizar el salario del empleado.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public SalarioEmpleado buscarPorId(int idSalario)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(SalarioEmpleado.class, idSalario);

        } finally {
            em.close();
        }
    }
}