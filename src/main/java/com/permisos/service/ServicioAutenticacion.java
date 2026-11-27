package com.permisos.service;

import com.permisos.model.RecuperacionContrasena;
import com.permisos.model.Usuario;
import com.permisos.util.JPAUtil;
import com.permisos.util.PasswordUtil;
import com.permisos.util.TokenRecuperacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ServicioAutenticacion {

    private static final Logger LOG =
            Logger.getLogger(ServicioAutenticacion.class.getName());
    private static final long VIGENCIA_RECUPERACION_MINUTOS = 30;

    private final NotificacionService notificacionService;

    @Autowired
    public ServicioAutenticacion(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    public ResultadoCambioContrasena cambiarContrasena(
            int idUsuario, String actual, String nueva) throws SQLException {
        if (vacio(nueva)) {
            return ResultadoCambioContrasena.NUEVA_NO_PERMITIDA;
        }
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, idUsuario,
                    LockModeType.PESSIMISTIC_WRITE);

            if (usuario == null || !usuario.isActivo()
                    || !coincide(actual, usuario.getPassword())) {
                em.getTransaction().rollback();
                return ResultadoCambioContrasena.ACTUAL_INCORRECTA;
            }
            if (actual.equals(nueva)
                    || usuario.getUsername().equalsIgnoreCase(nueva)) {
                em.getTransaction().rollback();
                return ResultadoCambioContrasena.NUEVA_NO_PERMITIDA;
            }

            usuario.setPassword(PasswordUtil.hash(nueva));
            em.getTransaction().commit();
            return ResultadoCambioContrasena.CAMBIADA;
        } catch (RuntimeException ex) {
            revertir(em);
            throw falloSeguro("No se pudo cambiar la contraseña.", ex);
        } finally {
            em.close();
        }
    }

    public void solicitarRecuperacion(String username) throws SQLException {
        DatosRecuperacion entrega = null;
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();
            List<Usuario> encontrados = em.createQuery(
                            "SELECT u FROM Usuario u JOIN FETCH u.empleado "
                                    + "WHERE u.username = :username AND u.activo = true",
                            Usuario.class)
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList();

            if (!encontrados.isEmpty()) {
                Usuario usuario = em.find(Usuario.class,
                        encontrados.get(0).getIdUsuario(),
                        LockModeType.PESSIMISTIC_WRITE);
                if (usuario == null || !usuario.isActivo()) {
                    em.getTransaction().commit();
                    return;
                }
                em.refresh(usuario, LockModeType.PESSIMISTIC_WRITE);
                if (!usuario.isActivo()) {
                    em.getTransaction().commit();
                    return;
                }
                String correo = usuario.getEmpleado().getCorreo();
                if (!vacio(correo)) {
                    LocalDateTime ahora = LocalDateTime.now();
                    String token = TokenRecuperacion.generar();

                    em.createQuery(
                                    "UPDATE RecuperacionContrasena r "
                                            + "SET r.usado = true "
                                            + "WHERE r.usuario.idUsuario = :idUsuario "
                                            + "AND r.usado = false")
                            .setParameter("idUsuario", usuario.getIdUsuario())
                            .executeUpdate();

                    RecuperacionContrasena recuperacion =
                            new RecuperacionContrasena();
                    recuperacion.setUsuario(usuario);
                    recuperacion.setTokenHash(TokenRecuperacion.hash(token));
                    recuperacion.setFechaCreacion(ahora);
                    recuperacion.setFechaExpiracion(
                            ahora.plusMinutes(VIGENCIA_RECUPERACION_MINUTOS));
                    recuperacion.setUsado(false);
                    em.persist(recuperacion);

                    String nombre = usuario.getEmpleado().getNombre() + " "
                            + usuario.getEmpleado().getApellido();
                    entrega = new DatosRecuperacion(correo, nombre, token);
                }
            }

            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            revertir(em);
            throw falloSeguro("No se pudo solicitar la recuperación.", ex);
        } finally {
            em.close();
        }

        if (entrega != null) {
            notificacionService.codigoRecuperacion(
                    entrega.correo, entrega.nombre, entrega.token);
        }
    }

    public ResultadoRestablecimiento restablecerContrasena(
            String token, String nueva) throws SQLException {
        if (vacio(token)) {
            return ResultadoRestablecimiento.CODIGO_INVALIDO;
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            String hash = TokenRecuperacion.hash(token);
            List<Integer> usuarios = em.createQuery(
                            "SELECT r.usuario.idUsuario FROM RecuperacionContrasena r "
                                    + "WHERE r.tokenHash = :hash",
                            Integer.class)
                    .setParameter("hash", hash)
                    .setMaxResults(1)
                    .getResultList();

            if (usuarios.isEmpty()) {
                em.getTransaction().rollback();
                return ResultadoRestablecimiento.CODIGO_INVALIDO;
            }

            Usuario usuario = em.find(Usuario.class, usuarios.get(0),
                    LockModeType.PESSIMISTIC_WRITE);
            if (usuario == null || !usuario.isActivo()) {
                em.getTransaction().rollback();
                return ResultadoRestablecimiento.CODIGO_INVALIDO;
            }

            List<RecuperacionContrasena> encontradas = em.createQuery(
                            "SELECT r FROM RecuperacionContrasena r "
                                    + "WHERE r.tokenHash = :hash "
                                    + "AND r.usado = false "
                                    + "AND r.fechaExpiracion > :ahora",
                            RecuperacionContrasena.class)
                    .setParameter("hash", hash)
                    .setParameter("ahora", LocalDateTime.now())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();

            if (encontradas.isEmpty()) {
                em.getTransaction().rollback();
                return ResultadoRestablecimiento.CODIGO_INVALIDO;
            }

            RecuperacionContrasena recuperacion = encontradas.get(0);
            em.refresh(recuperacion, LockModeType.PESSIMISTIC_WRITE);
            if (recuperacion.isUsado()
                    || !recuperacion.getFechaExpiracion().isAfter(LocalDateTime.now())
                    || !TokenRecuperacion.coincide(
                            token, recuperacion.getTokenHash())) {
                em.getTransaction().rollback();
                return ResultadoRestablecimiento.CODIGO_INVALIDO;
            }
            if (usuario.getUsername().equalsIgnoreCase(nueva)) {
                em.getTransaction().rollback();
                return ResultadoRestablecimiento.NUEVA_NO_PERMITIDA;
            }

            usuario.setPassword(PasswordUtil.hash(nueva));
            em.createQuery(
                            "UPDATE RecuperacionContrasena r "
                                    + "SET r.usado = true "
                                    + "WHERE r.usuario.idUsuario = :idUsuario")
                    .setParameter("idUsuario", usuario.getIdUsuario())
                    .executeUpdate();
            em.getTransaction().commit();
            return ResultadoRestablecimiento.RESTABLECIDA;
        } catch (RuntimeException ex) {
            revertir(em);
            throw falloSeguro("No se pudo restablecer la contraseña.", ex);
        } finally {
            em.close();
        }
    }

    private static boolean coincide(String clave, String guardada) {
        return PasswordUtil.esHash(guardada)
                ? PasswordUtil.verificar(clave, guardada)
                : PasswordUtil.coincideTextoPlano(clave, guardada);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static void revertir(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private static SQLException falloSeguro(String mensaje, RuntimeException ex) {
        LOG.log(Level.SEVERE, mensaje + " (" + ex.getClass().getSimpleName() + ").");
        return new SQLException(mensaje);
    }

    public enum ResultadoCambioContrasena {
        CAMBIADA,
        ACTUAL_INCORRECTA,
        NUEVA_NO_PERMITIDA
    }

    public enum ResultadoRestablecimiento {
        RESTABLECIDA,
        CODIGO_INVALIDO,
        NUEVA_NO_PERMITIDA
    }

    private static final class DatosRecuperacion {
        private final String correo;
        private final String nombre;
        private final String token;

        private DatosRecuperacion(String correo, String nombre, String token) {
            this.correo = correo;
            this.nombre = nombre;
            this.token = token;
        }
    }
}
