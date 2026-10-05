package com.permisos.dao;

import com.permisos.model.Departamento;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class DepartamentoDAO {

    public List<Departamento> listarActivos() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT d FROM Departamento d " +
                            "WHERE d.activo = true " +
                            "ORDER BY d.nombre",
                    Departamento.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public Departamento buscarPorId(int idDepartamento) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Departamento.class, idDepartamento);

        } finally {
            em.close();
        }
    }
}