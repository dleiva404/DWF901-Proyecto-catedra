package com.permisos.dao;

import com.permisos.model.Usuario;
import com.permisos.util.JPAUtil;
import com.permisos.util.PasswordUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import java.sql.SQLException;

public class UsuarioDAO {

    private static final Logger LOG =
            Logger.getLogger(UsuarioDAO.class.getName());

    public Usuario autenticar(String username, String password)
            throws SQLException {

        if (username == null || password == null) {
            return null;
        }

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Usuario usuario = em.createQuery(
                            "SELECT u FROM Usuario u " +
                                    "WHERE u.username = :username " +
                                    "AND u.activo = true",
                            Usuario.class
                    )
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (usuario == null) {
                // Mismo tiempo de respuesta que con un usuario real.
                PasswordUtil.simularVerificacion(password);
                return null;
            }

            String guardado = usuario.getPassword();

            boolean correcta = PasswordUtil.esHash(guardado)
                    ? PasswordUtil.verificar(password, guardado)
                    : PasswordUtil.coincideTextoPlano(password, guardado);

            if (!correcta) {
                return null;
            }

            if (PasswordUtil.necesitaActualizar(guardado)) {
                actualizarHash(em, usuario, password);
            }

            return usuario;

        } finally {
            em.close();
        }
    }

    private void actualizarHash(EntityManager em, Usuario usuario,
                                String password) {
        try {
            em.getTransaction().begin();
            usuario.setPassword(PasswordUtil.hash(password));
            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            LOG.log(Level.WARNING,
                    "No se pudo actualizar el hash del usuario "
                            + usuario.getIdUsuario(), ex);
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