package com.permisos.bean;

import com.permisos.dao.EmpleadoDAO;
import com.permisos.dao.RolDAO;
import com.permisos.dao.UsuarioDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.sql.SQLException;
import java.util.Map;

/**
 * Bean de la pantalla de inicio de sesión (login.xhtml).
 * Valida las credenciales con UsuarioDAO y guarda en sesión "usuario", "rol" y "empleado",
 * igual que lo hacía LoginServlet.
 */
@ManagedBean(name = "loginBean")
@RequestScoped
public class LoginBean {

    private String username;
    private String password;
    private String error;

    public String ingresar() {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            error = "Debe ingresar usuario y contraseña.";
            return null;
        }

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.autenticar(username.trim(), password);

            if (usuario == null) {
                error = "Usuario o contraseña incorrectos.";
                return null;
            }

            Rol rol = new RolDAO().buscarPorId(usuario.getIdRol());
            Empleado empleado = new EmpleadoDAO().buscarPorId(usuario.getIdEmpleado());

            usuarioDAO.registrarUltimoAcceso(usuario.getIdUsuario());

            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, Object> sesion = ec.getSessionMap();
            sesion.put("usuario", usuario);
            sesion.put("rol", rol);
            sesion.put("empleado", empleado);

            if (rol != null && "JEFATURA".equalsIgnoreCase(rol.getNombre())) {
                return "jefatura?faces-redirect=true";
            }
            return "solicitudes?faces-redirect=true";

        } catch (SQLException e) {
            error = "Error de conexión con la base de datos.";
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getError() {
        return error;
    }

    public boolean isHayError() {
        return error != null;
    }
}