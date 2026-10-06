package com.permisos.dao;

import com.permisos.model.TipoSolicitud;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;

public class TipoSolicitudDAO {

    public List<TipoSolicitud> listarActivos() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT t FROM TipoSolicitud t " +
                            "WHERE t.activo = true " +
                            "ORDER BY t.nombre",
                    TipoSolicitud.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public TipoSolicitud buscarPorId(int idTipoSolicitud) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(TipoSolicitud.class, idTipoSolicitud);

        } finally {
            em.close();
        }
    }

    public TipoSolicitud buscarPorNombre(String nombre) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT t FROM TipoSolicitud t " +
                                    "WHERE t.nombre = :nombre",
                            TipoSolicitud.class
                    )
                    .setParameter("nombre", nombre)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }
}
