package com.rrhh.api.resource;

import com.permisos.service.ReglaNegocioException;
import com.permisos.service.SolicitudEmpleadoService;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.TraductorErroresServicio;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.SaldoVacacionesMapper;
import com.rrhh.api.security.ApiPrincipal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;

/**
 * Saldo de vacaciones del usuario autenticado (endpoint 5 del contrato).
 * Siempre es el SUYO: el empleado se toma del token, no de la URL.
 */
@Path("/vacaciones")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "JEFATURA", "RRHH"})
public class VacacionesResource {

    private final SolicitudEmpleadoService servicio = new SolicitudEmpleadoService();

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/saldo")
    public Response saldo(@QueryParam("anio") String anioTexto) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        int anio = LocalDate.now().getYear();

        if (anioTexto != null && !anioTexto.trim().isEmpty()) {
            if (!anioTexto.trim().matches("\\d{4}")) {
                throw new ValidacionApiException(
                        Collections.singletonList("anio: debe ser un número de cuatro dígitos"));
            }
            anio = Integer.parseInt(anioTexto.trim());
        }

        try {
            return Response.ok(
                    SaldoVacacionesMapper.toDTO(yo.getEmpleadoId(),
                            servicio.saldoVacaciones(yo.getEmpleadoId(), anio))
            ).build();

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
