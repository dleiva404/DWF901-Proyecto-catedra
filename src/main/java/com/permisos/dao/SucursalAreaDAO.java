package com.permisos.dao;

import com.permisos.model.SucursalArea;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class SucursalAreaDAO {

    public List<SucursalArea> listarActivos() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT s FROM SucursalArea s " +
                            "WHERE s.activo = true " +
                            "ORDER BY s.nombre",
                    SucursalArea.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public SucursalArea buscarPorId(int idSucursalArea) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(SucursalArea.class, idSucursalArea);

        } finally {
            em.close();
        }
    }
}