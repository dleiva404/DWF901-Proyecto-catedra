
package com.rrhh.api.resource;

import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Empleado;
import com.rrhh.api.dto.EmpleadoDTO;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.EmpleadoMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

@Path("/empleados")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"RRHH"})
public class EmpleadoResource {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @GET
    public Response listarEmpleados() throws SQLException {

        List<Empleado> empleados = empleadoDAO.listarActivos();
        List<EmpleadoDTO> respuesta = new ArrayList<>();

        for (Empleado empleado : empleados) {
            respuesta.add(EmpleadoMapper.toDTO(empleado));
        }

        return Response.ok(respuesta).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarEmpleado(@PathParam("id") int id)
            throws SQLException {

        if (id <= 0) {
            throw new javax.ws.rs.BadRequestException(
                    "El identificador debe ser mayor que cero"
            );
        }

        Empleado empleado = empleadoDAO.buscarPorId(id);

        if (empleado == null) {
            throw new NotFoundException(
                    "No se encontró el empleado"
            );
        }

        return Response.ok(
                EmpleadoMapper.toDTO(empleado)
        ).build();
    }
}
