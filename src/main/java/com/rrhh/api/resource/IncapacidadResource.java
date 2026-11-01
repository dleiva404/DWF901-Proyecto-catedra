package com.rrhh.api.resource;

import com.permisos.model.Incapacidad;
import com.permisos.service.IncapacidadEmpleadoService;
import com.permisos.service.ReglaNegocioException;
import com.rrhh.api.dto.NuevaIncapacidadDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.TraductorErroresServicio;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.IncapacidadMapper;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.validation.ValidadorApi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;

/**
 * Registro de una incapacidad del usuario autenticado (endpoint 17 del
 * contrato). Crea de una sola vez la solicitud de tipo INCAPACIDAD y su
 * registro de incapacidad, en una sola transacción.
 *
 * PARA LA PARTE 4: los endpoints GET /incapacidades y GET /incapacidades/{id}
 * (listas según rol) comparten esta ruta; se pueden agregar en esta misma clase.
 */
@Path("/incapacidades")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "JEFATURA", "RRHH"})
public class IncapacidadResource {

    private final IncapacidadEmpleadoService servicio = new IncapacidadEmpleadoService();

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrar(NuevaIncapacidadDTO datos) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        if (datos == null) {
            throw new ValidacionApiException(Collections.singletonList("cuerpo: es obligatorio"));
        }

        ValidadorApi validador = new ValidadorApi();
        validador.obligatorio("numeroDocumento", datos.getNumeroDocumento());
        validador.longitud("numeroDocumento", datos.getNumeroDocumento(), 1, 100);
        if (datos.getObservaciones() != null && datos.getObservaciones().trim().length() > 500) {
            validador.error("observaciones", "debe tener como máximo 500 caracteres");
        }
        LocalDate inicio = validador.fecha("fechaInicio", datos.getFechaInicio());
        LocalDate fin = validador.fecha("fechaFin", datos.getFechaFin());
        validador.lanzarSiHayErrores();

        try {
            Incapacidad registrada = servicio.registrar(
                    yo.getEmpleadoId(), datos.getNumeroDocumento(), inicio, fin, datos.getObservaciones());

            URI ubicacion = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(registrada.getIdIncapacidad()))
                    .build();

            return Response.created(ubicacion)
                    .entity(IncapacidadMapper.toDTO(registrada))
                    .build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    protected ApiPrincipal usuarioActual() {
        ApiPrincipal yo = ApiPrincipal.desde(securityContext);
        if (yo == null) {
            throw new ApiException(401, "Autenticación requerida");
        }
        return yo;
    }
}
