package com.rrhh.api.resource;

import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.UsuarioAdminService;
import com.rrhh.api.dto.CambioRolDTO;
import com.rrhh.api.dto.NuevoUsuarioDTO;
import com.rrhh.api.dto.RolDTO;
import com.rrhh.api.dto.UsuarioAdminDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.TraductorErroresServicio;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.mapper.UsuarioAdminMapper;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.validation.ValidadorApi;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Panel Admin TI (solo rol ADMIN, lo garantiza SecurityConfig): listar usuarios
 * y roles, crear usuarios, cambiar su rol y activarlos o desactivarlos.
 * Las acciones usan POST porque el CORS de la API solo permite GET, POST y DELETE.
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminUsuarioResource {

    @Autowired
    private UsuarioAdminService servicio;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/usuarios")
    public Response listarUsuarios() throws SQLException {
        List<UsuarioAdminDTO> respuesta = new ArrayList<>();
        for (Usuario u : servicio.listar()) {
            respuesta.add(UsuarioAdminMapper.toDTO(u));
        }
        return Response.ok(respuesta).build();
    }

    @GET
    @Path("/roles")
    public Response listarRoles() throws SQLException {
        List<RolDTO> respuesta = new ArrayList<>();
        for (Rol r : servicio.listarRoles()) {
            respuesta.add(UsuarioAdminMapper.toDTO(r));
        }
        return Response.ok(respuesta).build();
    }

    @POST
    @Path("/usuarios")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(NuevoUsuarioDTO datos) throws SQLException {
        if (datos == null) {
            throw new ValidacionApiException(Collections.singletonList("cuerpo: es obligatorio"));
        }

        // misma regla de longitud de contrasena que AuthResource (8 a 128)
        ValidadorApi validador = new ValidadorApi();
        validador.positivo("empleadoId", datos.getEmpleadoId());
        validador.obligatorio("username", datos.getUsername());
        validador.longitud("username", datos.getUsername(), 3, 50);
        validador.positivo("rolId", datos.getRolId());
        validador.obligatorio("contrasena", datos.getContrasena());
        validador.longitud("contrasena", datos.getContrasena(), 8, 128);
        validador.lanzarSiHayErrores();

        try {
            Usuario creado = servicio.crear(datos.getEmpleadoId(), datos.getUsername(),
                    datos.getRolId(), datos.getContrasena());
            URI ubicacion = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(creado.getIdUsuario()))
                    .build();
            return Response.created(ubicacion).entity(UsuarioAdminMapper.toDTO(creado)).build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @POST
    @Path("/usuarios/{id}/rol")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cambiarRol(@PathParam("id") String idTexto, CambioRolDTO datos)
            throws SQLException {
        int id = SolicitudResource.leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        ValidadorApi validador = new ValidadorApi();
        validador.positivo("rolId", datos == null ? null : datos.getRolId());
        validador.lanzarSiHayErrores();

        try {
            servicio.cambiarRol(yo.getUsuarioId(), id, datos.getRolId());
            return Response.noContent().build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @POST
    @Path("/usuarios/{id}/activar")
    public Response activar(@PathParam("id") String idTexto) throws SQLException {
        int id = SolicitudResource.leerId(idTexto);
        try {
            servicio.activar(id);
            return Response.noContent().build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @POST
    @Path("/usuarios/{id}/desactivar")
    public Response desactivar(@PathParam("id") String idTexto) throws SQLException {
        int id = SolicitudResource.leerId(idTexto);
        ApiPrincipal yo = usuarioActual();
        try {
            servicio.desactivar(yo.getUsuarioId(), id);
            return Response.noContent().build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    private ApiPrincipal usuarioActual() {
        ApiPrincipal yo = ApiPrincipal.desde(securityContext);
        if (yo == null) {
            throw new ApiException(401, "Autenticación requerida");
        }
        return yo;
    }
}
