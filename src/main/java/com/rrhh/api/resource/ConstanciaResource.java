
package com.rrhh.api.resource;

import com.permisos.dao.ConstanciaDAO;
import com.permisos.model.Constancia;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.rrhh.api.dto.ConstanciaDTO;
import com.rrhh.api.dto.NuevaConstanciaDTO;
import com.rrhh.api.dto.DecisionConstanciaDTO;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.ConstanciaMapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/constancias")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "RRHH"})
public class ConstanciaResource {

    private final ConstanciaDAO constanciaDAO =
            new ConstanciaDAO();

    @Context
    private HttpServletRequest request;

    @GET
    public Response listarConstancias() throws SQLException {
        Usuario usuario = obtenerUsuario();
        String rol = obtenerRol(usuario);

        List<Constancia> constancias;

        if ("EMPLEADO".equalsIgnoreCase(rol)) {
            constancias = constanciaDAO.listarPorEmpleado(
                    usuario.getIdEmpleado()
            );
        } else if ("RRHH".equalsIgnoreCase(rol)) {
            constancias = constanciaDAO.obtenerTodas();
        } else {
            throw new ForbiddenException(
                    "No tienes permiso para consultar constancias."
            );
        }

        List<ConstanciaDTO> respuesta = new ArrayList<>();

        for (Constancia constancia : constancias) {
            respuesta.add(ConstanciaMapper.toDTO(constancia));
        }

        return Response.ok(respuesta).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarConstancia(
            @PathParam("id") int id
    ) throws SQLException {

        validarId(id);

        Usuario usuario = obtenerUsuario();
        String rol = obtenerRol(usuario);

        Constancia constancia =
                constanciaDAO.buscarPorId(id);

        if (constancia == null) {
            throw new NotFoundException(
                    "No se encontró la constancia."
            );
        }

        if ("EMPLEADO".equalsIgnoreCase(rol)) {
            if (constancia.getIdEmpleado()
                    != usuario.getIdEmpleado()) {
                throw new NotFoundException(
                        "No se encontró la constancia."
                );
            }
        } else if (!"RRHH".equalsIgnoreCase(rol)) {
            throw new ForbiddenException(
                    "No tienes permiso para consultar esta constancia."
            );
        }

        return Response.ok(
                ConstanciaMapper.toDTO(constancia)
        ).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarConstancia(
            NuevaConstanciaDTO datos
    ) throws SQLException {

        Usuario usuario = obtenerUsuario();
        String rol = obtenerRol(usuario);

        if (!"EMPLEADO".equalsIgnoreCase(rol)) {
            throw new ForbiddenException(
                    "Solo un empleado puede solicitar una constancia."
            );
        }

        if (datos == null
                || datos.getTipo() == null
                || datos.getTipo().trim().isEmpty()) {
            throw new BadRequestException(
                    "El tipo de constancia es obligatorio."
            );
        }

        if (datos.getTipo().trim().length() > 50) {
            throw new BadRequestException(
                    "El tipo no puede superar 50 caracteres."
            );
        }

        if (datos.getInstitucion() != null
                && datos.getInstitucion().length() > 150) {
            throw new BadRequestException(
                    "La institución no puede superar 150 caracteres."
            );
        }

        Constancia nueva = new Constancia();
        nueva.setIdEmpleado(usuario.getIdEmpleado());
        nueva.setTipo(datos.getTipo().trim());

        nueva.setInstitucion(
                limpiar(datos.getInstitucion())
        );

        nueva.setMotivo(
                limpiar(datos.getMotivo())
        );

        boolean registrada = constanciaDAO.insertar(nueva);

        if (!registrada) {
            throw new WebApplicationException(
                    "No fue posible registrar la constancia. " +
                            "Verifica, por ejemplo, que exista un salario " +
                            "activo si solicitas una constancia salarial.",
                    Response.Status.CONFLICT
            );
        }

        Constancia guardada =
                constanciaDAO.buscarPorId(
                        nueva.getIdConstancia()
                );

        if (guardada == null) {
            return Response.status(
                    Response.Status.CREATED
            ).build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(ConstanciaMapper.toDTO(guardada))
                .build();
    }

    @POST
    @Path("/{id}/aprobar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aprobarConstancia(
            @PathParam("id") int id,
            DecisionConstanciaDTO datos
    ) throws SQLException {

        validarId(id);

        Usuario usuario = obtenerUsuario();
        validarRRHH(usuario);

        Constancia existente =
                constanciaDAO.buscarPorId(id);

        if (existente == null) {
            throw new NotFoundException(
                    "No se encontró la constancia."
            );
        }

        if (!Constancia.ESTADO_PENDIENTE.equals(
                existente.getEstado())) {
            throw new WebApplicationException(
                    "La constancia ya fue procesada.",
                    Response.Status.CONFLICT
            );
        }

        String observaciones =
                datos == null ? null
                        : limpiar(datos.getObservaciones());

        try {
            constanciaDAO.aprobar(
                    id,
                    usuario.getIdUsuario(),
                    observaciones
            );
        } catch (SQLException ex) {
            throw new WebApplicationException(
                    "No se pudo aprobar la constancia. " +
                            "Puede que ya haya sido procesada.",
                    Response.Status.CONFLICT
            );
        }

        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/rechazar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rechazarConstancia(
            @PathParam("id") int id,
            DecisionConstanciaDTO datos
    ) throws SQLException {

        validarId(id);

        Usuario usuario = obtenerUsuario();
        validarRRHH(usuario);

        if (datos == null
                || datos.getMotivoRechazo() == null
                || datos.getMotivoRechazo().trim().isEmpty()) {
            throw new BadRequestException(
                    "El motivo del rechazo es obligatorio."
            );
        }

        String motivo = datos.getMotivoRechazo().trim();

        Constancia existente =
                constanciaDAO.buscarPorId(id);

        if (existente == null) {
            throw new NotFoundException(
                    "No se encontró la constancia."
            );
        }

        if (!Constancia.ESTADO_PENDIENTE.equals(
                existente.getEstado())) {
            throw new WebApplicationException(
                    "La constancia ya fue procesada.",
                    Response.Status.CONFLICT
            );
        }

        String observaciones =
                limpiar(datos.getObservaciones());

        try {
            constanciaDAO.rechazar(
                    id,
                    usuario.getIdUsuario(),
                    motivo,
                    observaciones
            );
        } catch (SQLException ex) {
            throw new WebApplicationException(
                    "No se pudo rechazar la constancia. " +
                            "Puede que ya haya sido procesada.",
                    Response.Status.CONFLICT
            );
        }

        return Response.noContent().build();
    }

    private Usuario obtenerUsuario() {
        if (request == null
                || request.getSession(false) == null) {
            throw new NotAuthorizedException(
                    "Debes iniciar sesión."
            );
        }

        Object objeto = request.getSession(false)
                .getAttribute("usuario");

        if (!(objeto instanceof Usuario)) {
            throw new NotAuthorizedException(
                    "Debes iniciar sesión."
            );
        }

        return (Usuario) objeto;
    }

    private String obtenerRol(Usuario usuario) {
        Object objeto = request.getSession(false)
                .getAttribute("rol");

        if (objeto instanceof Rol) {
            return ((Rol) objeto).getNombre();
        }

        if (usuario.getRol() != null) {
            return usuario.getRol().getNombre();
        }

        throw new ForbiddenException(
                "No se pudo determinar el rol del usuario."
        );
    }

    private void validarRRHH(Usuario usuario) {
        if (!"RRHH".equalsIgnoreCase(obtenerRol(usuario))) {
            throw new ForbiddenException(
                    "Esta operación está reservada para RRHH."
            );
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new BadRequestException(
                    "El identificador debe ser mayor que cero."
            );
        }
    }

    private String limpiar(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
