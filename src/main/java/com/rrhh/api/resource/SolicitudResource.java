package com.rrhh.api.resource;

import com.permisos.model.Solicitud;
import com.permisos.service.FiltroSolicitudes;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.SolicitudEmpleadoService;
import com.rrhh.api.dto.NuevaSolicitudDTO;
import com.rrhh.api.dto.SolicitudDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.TraductorErroresServicio;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.SolicitudMapper;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.validation.ValidadorApi;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Solicitudes del EMPLEADO autenticado sobre SUS PROPIAS solicitudes
 * (endpoints 8 a 11 del contrato): crear, listar, consultar y cancelar.
 *
 * Todo el trabajo lo hace SolicitudEmpleadoService; aquí solo se valida la
 * forma del JSON, se obtiene al usuario del token y se traducen las
 * excepciones a respuestas HTTP.
 *
 * PARA LA PARTE 4 (jefatura / RRHH): este recurso comparte ruta con
 * aprobar, rechazar e historial. Si agregan esos métodos aquí mismo, listar()
 * y obtener() son el lugar donde jefatura (su departamento) y RRHH (todas)
 * deben ampliarse; hoy devuelven solo lo propio para cualquier rol.
 */
@Path("/solicitudes")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "JEFATURA", "RRHH"})
public class SolicitudResource {

    private final SolicitudEmpleadoService servicio = new SolicitudEmpleadoService();

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    // ---------------------------------------------------------------- POST

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(NuevaSolicitudDTO datos) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        if (datos == null) {
            throw new ValidacionApiException(Collections.singletonList("cuerpo: es obligatorio"));
        }

        // Forma de los datos (obligatorios, formatos, largos): todo junto, como en el login.
        ValidadorApi validador = new ValidadorApi();
        validador.positivo("tipoSolicitudId", datos.getTipoSolicitudId());
        validador.obligatorio("motivo", datos.getMotivo());
        validador.longitud("motivo", datos.getMotivo(), 5, 250);
        LocalDate inicio = validador.fecha("fechaInicio", datos.getFechaInicio());
        LocalDate fin = validador.fecha("fechaFin", datos.getFechaFin());
        validador.lanzarSiHayErrores();

        try {
            Solicitud creada = servicio.crear(
                    yo.getEmpleadoId(), datos.getTipoSolicitudId(), datos.getMotivo(), inicio, fin);

            URI ubicacion = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(creada.getIdSolicitud()))
                    .build();

            return Response.created(ubicacion)
                    .entity(SolicitudMapper.toDTO(creada))
                    .build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    // ----------------------------------------------------------------- GET

    @GET
    public Response listar(@QueryParam("estado") String estado,
                           @QueryParam("tipoSolicitudId") String tipoSolicitudId,
                           @QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        try {
            FiltroSolicitudes filtro =
                    FiltroSolicitudes.desdeParametros(estado, tipoSolicitudId, desde, hasta);

            List<SolicitudDTO> respuesta = new ArrayList<>();
            for (Solicitud s : servicio.listarMias(yo.getEmpleadoId(), filtro)) {
                respuesta.add(SolicitudMapper.toDTO(s));
            }
            return Response.ok(respuesta).build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") int id) throws SQLException {

        validarId(id);
        ApiPrincipal yo = usuarioActual();

        try {
            // No existe o es de otra persona: el mismo 404 (el contrato no revela ids ajenos).
            return Response.ok(SolicitudMapper.toDTO(servicio.detalle(yo.getEmpleadoId(), id))).build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    // -------------------------------------------------------------- DELETE

    @DELETE
    @Path("/{id}")
    public Response cancelar(@PathParam("id") int id) throws SQLException {

        validarId(id);
        ApiPrincipal yo = usuarioActual();

        try {
            servicio.cancelar(id, yo.getEmpleadoId(), yo.getUsuarioId());
            return Response.noContent().build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    // ------------------------------------------------------------ utilidades

    /** El usuario autenticado, tomado del SecurityContext (nunca de la sesión JSF). */
    protected ApiPrincipal usuarioActual() {
        ApiPrincipal yo = ApiPrincipal.desde(securityContext);
        if (yo == null) {
            throw new ApiException(401, "Autenticación requerida");
        }
        return yo;
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new BadRequestException("El identificador debe ser mayor que cero");
        }
    }
}
