package com.permisos.service;

import com.permisos.dao.EmpleadoDAO;
import com.permisos.dao.RolDAO;
import com.permisos.dao.UsuarioAdminDAO;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.permisos.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Reglas del panel Admin TI: crear usuarios, cambiar su rol y activarlos o
 * desactivarlos. Nunca devuelve ni registra contrasenas.
 */
@Service
public class UsuarioAdminService {

    private final UsuarioAdminDAO usuarioAdminDAO;
    private final RolDAO rolDAO;
    private final EmpleadoDAO empleadoDAO;

    @Autowired
    public UsuarioAdminService(UsuarioAdminDAO usuarioAdminDAO, RolDAO rolDAO, EmpleadoDAO empleadoDAO) {
        this.usuarioAdminDAO = usuarioAdminDAO;
        this.rolDAO = rolDAO;
        this.empleadoDAO = empleadoDAO;
    }

    /** Para pruebas o pantallas que no levantan Spring. */
    public UsuarioAdminService() {
        this(new UsuarioAdminDAO(), new RolDAO(), new EmpleadoDAO());
    }

    public List<Usuario> listar() throws SQLException {
        return usuarioAdminDAO.listar();
    }

    public List<Rol> listarRoles() throws SQLException {
        return rolDAO.listarActivos();
    }

    /** Crea el usuario de un empleado que aun no tiene. La contrasena se guarda con hash. */
    public Usuario crear(int idEmpleado, String username, int idRol, String contrasena)
            throws ReglaNegocioException, SQLException {

        String nombre = username.trim();

        if (empleadoDAO.buscarPorId(idEmpleado) == null) {
            throw new RecursoNoEncontradoException("El empleado indicado no existe.");
        }
        Rol rol = rolDAO.buscarPorId(idRol);
        if (rol == null || !rol.isActivo()) {
            throw new ValidacionException("rolId: el rol indicado no existe");
        }
        if (usuarioAdminDAO.existeUsername(nombre)) {
            throw new ConflictoException("El nombre de usuario ya existe.");
        }
        if (usuarioAdminDAO.empleadoTieneUsuario(idEmpleado)) {
            throw new ConflictoException("El empleado ya tiene un usuario.");
        }

        int id = usuarioAdminDAO.insertar(idEmpleado, idRol, nombre, PasswordUtil.hash(contrasena));
        return usuarioAdminDAO.buscarPorId(id);
    }

    public void cambiarRol(int idAdmin, int idUsuario, int idRol)
            throws ReglaNegocioException, SQLException {

        Usuario usuario = buscarExistente(idUsuario);
        Rol rol = rolDAO.buscarPorId(idRol);
        if (rol == null || !rol.isActivo()) {
            throw new ValidacionException("rolId: el rol indicado no existe");
        }

        ReglasAdmin.validarCambioRol(
                usuario.getIdUsuario() == idAdmin,
                esAdminActivo(usuario),
                ReglasAdmin.ROL_ADMIN.equals(rol.getNombre()),
                usuarioAdminDAO.contarAdminsActivos());

        usuarioAdminDAO.cambiarRol(idUsuario, idRol);
    }

    public void activar(int idUsuario) throws ReglaNegocioException, SQLException {
        buscarExistente(idUsuario);
        usuarioAdminDAO.cambiarEstado(idUsuario, true);
    }

    public void desactivar(int idAdmin, int idUsuario) throws ReglaNegocioException, SQLException {
        Usuario usuario = buscarExistente(idUsuario);

        ReglasAdmin.validarDesactivar(
                usuario.getIdUsuario() == idAdmin,
                esAdminActivo(usuario),
                usuarioAdminDAO.contarAdminsActivos());

        usuarioAdminDAO.cambiarEstado(idUsuario, false);
    }

    private Usuario buscarExistente(int idUsuario) throws ReglaNegocioException, SQLException {
        Usuario usuario = usuarioAdminDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("El usuario indicado no existe.");
        }
        return usuario;
    }

    private static boolean esAdminActivo(Usuario usuario) {
        return usuario.isActivo() && usuario.getRol() != null
                && ReglasAdmin.ROL_ADMIN.equals(usuario.getRol().getNombre());
    }
}
