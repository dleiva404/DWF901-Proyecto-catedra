package com.rrhh.api.resource;

import com.permisos.dao.EmpleadoDAO;
import com.permisos.dao.RolDAO;
import com.permisos.dao.UsuarioDAO;
import com.permisos.model.Empleado;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.rrhh.api.dto.LoginRequestDTO;
import com.rrhh.api.dto.LoginResponseDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.filter.Publico;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.security.SesionApi;
import com.rrhh.api.security.TokenService;
import com.rrhh.api.validation.ValidadorApi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.SQLException;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    @Publico
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

        SesionApi sesion = TokenService.getInstance().crear(
                usuario.getIdUsuario(),
                usuario.getIdEmpleado(),
                usuario.getUsername(),
                nombre,
                rol.getNombre());

        LoginResponseDTO respuesta = new LoginResponseDTO(
                sesion.getToken(),
                TokenService.getInstance().getDuracionSegundos(),
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
            TokenService.getInstance().invalidar(yo.getToken());
        }

        return Response.noContent().build();
    }
}