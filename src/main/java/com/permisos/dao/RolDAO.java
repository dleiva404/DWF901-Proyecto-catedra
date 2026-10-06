package com.permisos.dao;

import com.permisos.model.Rol;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class RolDAO {

    public List<Rol> listarActivos() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM Rol r " +
                            "WHERE r.activo = true " +
                            "ORDER BY r.nombre",
                    Rol.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public Rol buscarPorId(int idRol) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Rol.class, idRol);

        } finally {
            em.close();
        }
    }
}