package com.permisos.dao;

import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.permisos.util.JPAUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;

/**
 * Consultas y escrituras de usuarios para el panel Admin TI.
 * Es una clase aparte de UsuarioDAO para no chocar con los cambios de login.
 */
@Repository
public class UsuarioAdminDAO {

    private static final String CONSULTA_CON_DETALLE =
            "SELECT u FROM Usuario u JOIN FETCH u.empleado JOIN FETCH u.rol ";

    public List<Usuario> listar() throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(CONSULTA_CON_DETALLE + "ORDER BY u.username", Usuario.class)
                    .getResultList();
        } catch (RuntimeException ex) {
            throw new SQLException("No se pudieron consultar los usuarios.", ex);
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(CONSULTA_CON_DETALLE + "WHERE u.idUsuario = :id", Usuario.class)
                    .setParameter("id", idUsuario)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo consultar el usuario.", ex);
        } finally {
            em.close();
        }
    }

    public boolean existeUsername(String username) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return total != null && total > 0;
        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo verificar el nombre de usuario.", ex);
        } finally {
            em.close();
        }
    }

    public boolean empleadoTieneUsuario(int idEmpleado) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u WHERE u.empleado.idEmpleado = :id", Long.class)
                    .setParameter("id", idEmpleado)
                    .getSingleResult();
            return total != null && total > 0;
        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo verificar el usuario del empleado.", ex);
        } finally {
            em.close();
        }
    }

    public long contarAdminsActivos() throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u "
                                    + "WHERE u.rol.nombre = :rol AND u.activo = true", Long.class)
                    .setParameter("rol", "ADMIN")
                    .getSingleResult();
            return total != null ? total : 0;
        } catch (RuntimeException ex) {
            throw new SQLException("No se pudo contar los administradores.", ex);
        } finally {
            em.close();
        }
    }

    /** Guarda un usuario nuevo y devuelve su id. La contrasena ya debe venir con hash. */
    public int insertar(int idEmpleado, int idRol, String username, String hashContrasena)
            throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Usuario nuevo = new Usuario();
            nuevo.setUsername(username);
            nuevo.setPassword(hashContrasena);
            nuevo.setEmpleado(em.getReference(Empleado.class, idEmpleado));
            nuevo.setRol(em.getReference(Rol.class, idRol));
            nuevo.setActivo(true);
            nuevo.setFechaCreacion(LocalDateTime.now());

            em.persist(nuevo);
            em.getTransaction().commit();
            return nuevo.getIdUsuario();

        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo registrar el usuario.", ex);
        } finally {
            em.close();
        }
    }

    public void cambiarRol(int idUsuario, int idRol) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, idUsuario);
            if (usuario != null) {
                usuario.setRol(em.getReference(Rol.class, idRol));
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo cambiar el rol.", ex);
        } finally {
            em.close();
        }
    }

    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, idUsuario);
            if (usuario != null) {
                usuario.setActivo(activo);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            revertir(em);
            throw new SQLException("No se pudo cambiar el estado del usuario.", ex);
        } finally {
            em.close();
        }
    }

    private void revertir(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
