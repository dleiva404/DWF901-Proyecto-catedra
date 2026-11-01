package com.rrhh.api.resource;

import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.TipoSolicitud;
import com.rrhh.api.dto.TipoSolicitudDTO;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.TipoSolicitudMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de tipos de solicitud activos (endpoint 4 del contrato), para
 * llenar listas desplegables en los clientes.
 */
@Path("/tipos-solicitud")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "JEFATURA", "RRHH"})
public class TipoSolicitudResource {

    private final TipoSolicitudDAO tipoSolicitudDAO = new TipoSolicitudDAO();

    @GET
    public Response listar() throws SQLException {
        List<TipoSolicitudDTO> respuesta = new ArrayList<>();
        for (TipoSolicitud tipo : tipoSolicitudDAO.listarActivos()) {
            respuesta.add(TipoSolicitudMapper.toDTO(tipo));
        }
        return Response.ok(respuesta).build();
    }
}
