package com.permisos.dao;

import com.permisos.model.Jefatura;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class JefaturaDAO {

    public Jefatura buscarPorId(int idJefatura) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Jefatura.class, idJefatura);

        } finally {
            em.close();
        }
    }

    public Jefatura buscarPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT j FROM Jefatura j " +
                                    "WHERE j.empleado.idEmpleado = :idEmpleado",
                            Jefatura.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }

    public List<Jefatura> listarActivas() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT j FROM Jefatura j " +
                            "WHERE j.activo = true " +
                            "ORDER BY j.idJefatura",
                    Jefatura.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public List<Jefatura> listarPorDepartamento(int idDepartamento)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT j FROM Jefatura j " +
                                    "WHERE j.departamento.idDepartamento = :idDepartamento " +
                                    "AND j.activo = true " +
                                    "ORDER BY j.idJefatura",
                            Jefatura.class
                    )
                    .setParameter("idDepartamento", idDepartamento)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Jefatura> listarPorSucursalArea(int idSucursalArea)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT j FROM Jefatura j " +
                                    "WHERE j.sucursalArea.idSucursalArea = :idSucursalArea " +
                                    "AND j.activo = true " +
                                    "ORDER BY j.idJefatura",
                            Jefatura.class
                    )
                    .setParameter("idSucursalArea", idSucursalArea)
                    .getResultList();

        } finally {
            em.close();
        }
    }
}
