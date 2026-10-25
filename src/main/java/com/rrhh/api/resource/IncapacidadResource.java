
package com.rrhh.api.resource;

import com.permisos.dao.IncapacidadDAO;
import com.permisos.model.Incapacidad;
import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.rrhh.api.dto.IncapacidadDTO;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.IncapacidadMapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/incapacidades")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "RRHH"})
public class IncapacidadResource {

    private final IncapacidadDAO incapacidadDAO =
            new IncapacidadDAO();

    @Context
    private HttpServletRequest request;

    @GET
    public Response listarIncapacidades() throws SQLException {
        Usuario usuario = obtenerUsuario();
        String rol = obtenerRol(usuario);

        List<Incapacidad> incapacidades;

        if ("EMPLEADO".equalsIgnoreCase(rol)) {
            incapacidades = incapacidadDAO.listarPorEmpleado(
                    usuario.getIdEmpleado()
            );
        } else if ("RRHH".equalsIgnoreCase(rol)) {
            incapacidades = incapacidadDAO.listarTodas();
        } else {
            throw new ForbiddenException(
                    "No tienes permiso para consultar incapacidades."
            );
        }

        List<IncapacidadDTO> respuesta = new ArrayList<>();

        for (Incapacidad incapacidad : incapacidades) {
            respuesta.add(IncapacidadMapper.toDTO(incapacidad));
        }

        return Response.ok(respuesta).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarIncapacidad(
            @PathParam("id") int id
    ) throws SQLException {

        if (id <= 0) {
            throw new BadRequestException(
                    "El identificador debe ser mayor que cero."
            );
        }

        Usuario usuario = obtenerUsuario();
        String rol = obtenerRol(usuario);

        Incapacidad incapacidad =
                incapacidadDAO.buscarPorId(id);

        if (incapacidad == null) {
            throw new NotFoundException(
                    "No se encontró la incapacidad."
            );
        }

        if ("EMPLEADO".equalsIgnoreCase(rol)) {
            List<Incapacidad> propias =
                    incapacidadDAO.listarPorEmpleado(
                            usuario.getIdEmpleado()
                    );

            boolean perteneceAlEmpleado = false;

            for (Incapacidad propia : propias) {
                if (propia.getIdIncapacidad() == id) {
                    perteneceAlEmpleado = true;
                    break;
                }
            }

            if (!perteneceAlEmpleado) {
                throw new NotFoundException(
                        "No se encontró la incapacidad."
                );
            }

        } else if (!"RRHH".equalsIgnoreCase(rol)) {
            throw new ForbiddenException(
                    "No tienes permiso para consultar esta incapacidad."
            );
        }

        return Response.ok(
                IncapacidadMapper.toDTO(incapacidad)
        ).build();
    }

    private Usuario obtenerUsuario() {
        Object sesionUsuario =
                request.getSession(false) == null
                        ? null
                        : request.getSession(false).getAttribute("usuario");

        if (!(sesionUsuario instanceof Usuario)) {
            throw new javax.ws.rs.NotAuthorizedException(
                    "Debes iniciar sesión."
            );
        }

        return (Usuario) sesionUsuario;
    }

    private String obtenerRol(Usuario usuario) {
        Object rolSesion =
                request.getSession(false).getAttribute("rol");

        if (rolSesion instanceof Rol) {
            return ((Rol) rolSesion).getNombre();
        }

        if (usuario.getRol() != null) {
            return usuario.getRol().getNombre();
        }

        throw new ForbiddenException(
                "No se pudo determinar el rol del usuario."
        );
    }
}
