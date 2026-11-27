package com.rrhh.api.resource;

import com.permisos.dao.EmpleadoDAO;
import com.permisos.dao.RolDAO;
import com.permisos.dao.UsuarioDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.rrhh.api.dto.LoginRequestDTO;
import com.rrhh.api.dto.LoginResponseDTO;
import com.rrhh.api.dto.CambioContrasenaDTO;
import com.rrhh.api.dto.SolicitarRecuperacionDTO;
import com.rrhh.api.dto.RestablecerContrasenaDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.security.SesionApi;
import com.rrhh.api.security.TokenService;
import com.rrhh.api.validation.ValidadorApi;
import com.permisos.service.ServicioAutenticacion;
import com.permisos.service.ServicioAutenticacion.ResultadoCambioContrasena;
import com.permisos.service.ServicioAutenticacion.ResultadoRestablecimiento;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private EmpleadoDAO empleadoDAO;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ServicioAutenticacion servicioAutenticacion;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequestDTO datos) throws SQLException {

        ValidadorApi validador = new ValidadorApi();
        validador.obligatorio("username",
                datos == null ? null : datos.getUsername());
        validador.obligatorio("password",
                datos == null ? null : datos.getPassword());
        validador.lanzarSiHayErrores();

        Usuario usuario = usuarioDAO.autenticar(
                datos.getUsername().trim(), datos.getPassword());

        if (usuario == null) {
            throw new ApiException(401, "Usuario o contraseña incorrectos.");
        }

        Rol rol = rolDAO.buscarPorId(usuario.getIdRol());
        if (rol == null || rol.getNombre() == null) {
            throw new ApiException(403, "El usuario no tiene un rol asignado.");
        }

        Empleado empleado = empleadoDAO.buscarPorId(usuario.getIdEmpleado());
        String nombre = empleado == null
                ? usuario.getUsername()
                : empleado.getNombre() + " " + empleado.getApellido();

        usuarioDAO.registrarUltimoAcceso(usuario.getIdUsuario());

        SesionApi sesion = tokenService.crear(
                usuario.getIdUsuario(),
                usuario.getIdEmpleado(),
                usuario.getUsername(),
                nombre,
                rol.getNombre());

        LoginResponseDTO respuesta = new LoginResponseDTO(
                sesion.getToken(),
                tokenService.getDuracionSegundos(),
                new LoginResponseDTO.UsuarioSesionDTO(
                        sesion.getUsuarioId(),
                        sesion.getUsername(),
                        sesion.getEmpleadoId(),
                        sesion.getNombre(),
                        sesion.getRol()));

        return Response.ok(respuesta).build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        ApiPrincipal yo = ApiPrincipal.desde(securityContext);

        if (yo != null) {
            tokenService.invalidar(yo.getToken());
        }

        return Response.noContent().build();
    }

    @POST
    @Path("/cambiar-contrasena")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cambiarContrasena(CambioContrasenaDTO datos)
            throws SQLException {
        ApiPrincipal yo = ApiPrincipal.desde(securityContext);
        if (yo == null) {
            throw new ApiException(401, "Autenticación requerida");
        }

        ValidadorApi validador = new ValidadorApi();
        validador.obligatorio("actual",
                datos == null ? null : datos.getActual());
        validador.obligatorio("nueva",
                datos == null ? null : datos.getNueva());
        validador.longitud("nueva",
                datos == null ? null : datos.getNueva(), 8, 128);
        if (datos != null && datos.getActual() != null
                && datos.getActual().equals(datos.getNueva())) {
            validador.error("nueva",
                    "debe ser distinta de la contraseña actual");
        }
        if (datos != null && datos.getNueva() != null
                && yo.getName().equalsIgnoreCase(datos.getNueva())) {
            validador.error("nueva",
                    "no puede ser igual al nombre de usuario");
        }
        validador.lanzarSiHayErrores();

        ResultadoCambioContrasena resultado =
                servicioAutenticacion.cambiarContrasena(
                        yo.getUsuarioId(), datos.getActual(), datos.getNueva());
        if (resultado == ResultadoCambioContrasena.ACTUAL_INCORRECTA) {
            throw new ApiException(400, "La contraseña actual es incorrecta.");
        }
        if (resultado == ResultadoCambioContrasena.NUEVA_NO_PERMITIDA) {
            throw new ApiException(400,
                    "La nueva contraseña no cumple las reglas.");
        }

        tokenService.invalidar(yo.getToken());
        return Response.noContent().build();
    }

    @POST
    @Path("/recuperar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response solicitarRecuperacion(SolicitarRecuperacionDTO datos)
            throws SQLException {
        ValidadorApi validador = new ValidadorApi();
        validador.obligatorio("username",
                datos == null ? null : datos.getUsername());
        validador.longitud("username",
                datos == null ? null : datos.getUsername(), 1, 50);
        validador.lanzarSiHayErrores();

        servicioAutenticacion.solicitarRecuperacion(
                datos.getUsername().trim());
        return Response.noContent().build();
    }

    @POST
    @Path("/restablecer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restablecerContrasena(RestablecerContrasenaDTO datos)
            throws SQLException {
        ValidadorApi validador = new ValidadorApi();
        validador.obligatorio("token",
                datos == null ? null : datos.getToken());
        validador.longitud("token",
                datos == null ? null : datos.getToken(), 1, 128);
        validador.obligatorio("nueva",
                datos == null ? null : datos.getNueva());
        validador.longitud("nueva",
                datos == null ? null : datos.getNueva(), 8, 128);
        validador.lanzarSiHayErrores();

        ResultadoRestablecimiento resultado =
                servicioAutenticacion.restablecerContrasena(
                        datos.getToken().trim(), datos.getNueva());
        if (resultado == ResultadoRestablecimiento.CODIGO_INVALIDO) {
            throw new ApiException(400,
                    "El código de recuperación no es válido o venció.");
        }
        if (resultado == ResultadoRestablecimiento.NUEVA_NO_PERMITIDA) {
            throw new ApiException(400,
                    "La nueva contraseña no puede ser el nombre de usuario.");
        }
        return Response.noContent().build();
    }
}