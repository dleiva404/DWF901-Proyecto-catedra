package com.permisos.dao;

import com.permisos.model.Usuario;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario autenticar(String username, String password)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT u FROM Usuario u " +
                                    "WHERE u.username = :username " +
                                    "AND u.password = :password " +
                                    "AND u.activo = true",
                            Usuario.class
                    )
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Usuario.class, idUsuario);

        } finally {
            em.close();
        }
    }

    public Usuario buscarPorEmpleado(int idEmpleado) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT u FROM Usuario u " +
                                    "WHERE u.empleado.idEmpleado = :idEmpleado",
                            Usuario.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }

    public void registrarUltimoAcceso(int idUsuario)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Usuario usuario = em.find(Usuario.class, idUsuario);

            if (usuario != null) {
                usuario.setUltimoAcceso(
                        java.time.LocalDateTime.now()
                );
            }

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar el último acceso.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}