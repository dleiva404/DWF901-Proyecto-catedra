package com.permisos.bean;

import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.ReglasAdmin;
import com.permisos.service.UsuarioAdminService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Bean del panel Admin TI (admin-usuarios.xhtml): crear usuarios, cambiar su
 * rol y activarlos o desactivarlos. Solo entra el rol ADMIN.
 */
@ManagedBean(name = "adminUsuariosBean")
@ViewScoped
public class AdminUsuariosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_ACCESO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idAdmin;

    private final List<Fila> filas = new ArrayList<>();
    private final List<SelectItem> roles = new ArrayList<>();
    private final List<SelectItem> empleadosSinUsuario = new ArrayList<>();
    private final Map<Integer, String> rolPorUsuario = new HashMap<>();

    private String nuevoEmpleadoId;
    private String nuevoUsername;
    private String nuevoRolId;
    private String nuevaContrasena;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        Usuario usuario = (Usuario) ec.getSessionMap().get("usuario");
        Rol rol = (Rol) ec.getSessionMap().get("rol");

        if (usuario == null || rol == null) {
            redirigir(fc, "/login");
            return;
        }
        if (!ReglasAdmin.ROL_ADMIN.equalsIgnoreCase(rol.getNombre())) {
            redirigir(fc, "/solicitudes.xhtml");
            return;
        }

        idAdmin = usuario.getIdUsuario();
        cargar();
    }

    private void cargar() {
        filas.clear();
        roles.clear();
        empleadosSinUsuario.clear();
        rolPorUsuario.clear();

        try {
            UsuarioAdminService servicio = new UsuarioAdminService();

            for (Rol r : servicio.listarRoles()) {
                roles.add(new SelectItem(String.valueOf(r.getIdRol()), r.getNombre()));
            }

            Set<Integer> conUsuario = new HashSet<>();
            for (Usuario u : servicio.listar()) {
                conUsuario.add(u.getIdEmpleado());
                filas.add(new Fila(u));
                rolPorUsuario.put(u.getIdUsuario(), String.valueOf(u.getIdRol()));
            }

            for (Empleado e : new com.permisos.dao.EmpleadoDAO().listarActivos()) {
                if (!conUsuario.contains(e.getIdEmpleado())) {
                    empleadosSinUsuario.add(new SelectItem(String.valueOf(e.getIdEmpleado()),
                            (e.getNombre() + " " + e.getApellido()).trim()));
                }
            }
        } catch (SQLException | RuntimeException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible consultar los usuarios.");
        }
    }

    private void redirigir(FacesContext fc, String ruta) {
        try {
            ExternalContext ec = fc.getExternalContext();
            ec.redirect(ec.getRequestContextPath() + ruta);
            fc.responseComplete();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo redirigir a " + ruta, e);
        }
    }

    private static void mensaje(FacesMessage.Severity tipo, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(tipo, texto, null));
    }

    private static Integer entero(String valor) {
        try {
            return valor == null || valor.trim().isEmpty() ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------- acciones ----------

    public void crear() {
        Integer idEmpleado = entero(nuevoEmpleadoId);
        Integer idRol = entero(nuevoRolId);
        String username = nuevoUsername == null ? "" : nuevoUsername.trim();
        String contrasena = nuevaContrasena == null ? "" : nuevaContrasena;

        List<String> errores = new ArrayList<>();
        if (idEmpleado == null) {
            errores.add("Seleccione el empleado.");
        }
        if (username.length() < 3 || username.length() > 50) {
            errores.add("El usuario debe tener entre 3 y 50 caracteres.");
        }
        if (idRol == null) {
            errores.add("Seleccione el rol.");
        }
        if (contrasena.length() < 8 || contrasena.length() > 128) {
            errores.add("La contraseña temporal debe tener entre 8 y 128 caracteres.");
        }
        if (!errores.isEmpty()) {
            for (String e : errores) {
                mensaje(FacesMessage.SEVERITY_ERROR, e);
            }
            return;
        }

        try {
            new UsuarioAdminService().crear(idEmpleado, username, idRol, contrasena);
            mensaje(FacesMessage.SEVERITY_INFO, "Usuario " + username + " creado.");
            nuevoEmpleadoId = null;
            nuevoUsername = null;
            nuevoRolId = null;
            nuevaContrasena = null;
        } catch (ReglaNegocioException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (SQLException | RuntimeException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible crear el usuario.");
        }
        cargar();
    }

    public void cambiarRol(int idUsuario) {
        Integer idRol = entero(rolPorUsuario.get(idUsuario));
        if (idRol == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Seleccione el rol.");
            return;
        }
        try {
            new UsuarioAdminService().cambiarRol(idAdmin, idUsuario, idRol);
            mensaje(FacesMessage.SEVERITY_INFO, "Rol actualizado.");
        } catch (ReglaNegocioException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (SQLException | RuntimeException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible cambiar el rol.");
        }
        cargar();
    }

    public void activar(int idUsuario) {
        try {
            new UsuarioAdminService().activar(idUsuario);
            mensaje(FacesMessage.SEVERITY_INFO, "Usuario activado.");
        } catch (ReglaNegocioException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (SQLException | RuntimeException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible activar el usuario.");
        }
        cargar();
    }

    public void desactivar(int idUsuario) {
        try {
            new UsuarioAdminService().desactivar(idAdmin, idUsuario);
            mensaje(FacesMessage.SEVERITY_INFO, "Usuario desactivado.");
        } catch (ReglaNegocioException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (SQLException | RuntimeException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible desactivar el usuario.");
        }
        cargar();
    }

    // ---------- datos para la vista ----------

    public List<Fila> getFilas() {
        return filas;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public List<SelectItem> getEmpleadosSinUsuario() {
        return empleadosSinUsuario;
    }

    public boolean isHayEmpleadosSinUsuario() {
        return !empleadosSinUsuario.isEmpty();
    }

    public Map<Integer, String> getRolPorUsuario() {
        return rolPorUsuario;
    }

    public int getTotal() {
        return filas.size();
    }

    public int getActivos() {
        int n = 0;
        for (Fila f : filas) {
            if (f.activo) {
                n++;
            }
        }
        return n;
    }

    public int getInactivos() {
        return filas.size() - getActivos();
    }

    public String getNuevoEmpleadoId() {
        return nuevoEmpleadoId;
    }

    public void setNuevoEmpleadoId(String nuevoEmpleadoId) {
        this.nuevoEmpleadoId = nuevoEmpleadoId;
    }

    public String getNuevoUsername() {
        return nuevoUsername;
    }

    public void setNuevoUsername(String nuevoUsername) {
        this.nuevoUsername = nuevoUsername;
    }

    public String getNuevoRolId() {
        return nuevoRolId;
    }

    public void setNuevoRolId(String nuevoRolId) {
        this.nuevoRolId = nuevoRolId;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    /** Fila de la tabla. Nunca lleva la contraseña ni su hash. */
    public static class Fila implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int id;
        private final String username;
        private final String empleado;
        private final String rol;
        private final boolean activo;
        private final String ultimoAcceso;

        Fila(Usuario u) {
            this.id = u.getIdUsuario();
            this.username = u.getUsername();
            Empleado e = u.getEmpleado();
            this.empleado = e == null ? "" : (e.getNombre() + " " + e.getApellido()).trim();
            this.rol = u.getRol() == null ? "" : u.getRol().getNombre();
            this.activo = u.isActivo();
            this.ultimoAcceso = u.getUltimoAcceso() == null ? "Nunca" : u.getUltimoAcceso().format(FORMATO_ACCESO);
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmpleado() {
            return empleado;
        }

        public String getRol() {
            return rol;
        }

        public boolean isActivo() {
            return activo;
        }

        public String getEstado() {
            return activo ? "Activo" : "Inactivo";
        }

        public String getClaseEstado() {
            return activo ? "aprobada" : "cancelada";
        }

        public String getUltimoAcceso() {
            return ultimoAcceso;
        }

        public String getRolMinusculas() {
            return rol.toLowerCase(Locale.ROOT);
        }
    }
}
