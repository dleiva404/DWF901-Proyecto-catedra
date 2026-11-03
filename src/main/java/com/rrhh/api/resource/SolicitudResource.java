package com.rrhh.api.resource;

import com.permisos.model.HistorialSolicitud;
import com.permisos.model.Solicitud;
import com.permisos.service.FiltroSolicitudes;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.SolicitudEmpleadoService;
import com.permisos.service.SolicitudService;
import com.rrhh.api.dto.HistorialSolicitudDTO;
import com.rrhh.api.dto.NuevaSolicitudDTO;
import com.rrhh.api.dto.RechazoSolicitudDTO;
import com.rrhh.api.dto.SolicitudDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.TraductorErroresServicio;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.HistorialSolicitudMapper;
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
 */
@Path("/solicitudes")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "JEFATURA", "RRHH"})
public class SolicitudResource {

    private final SolicitudEmpleadoService servicio = new SolicitudEmpleadoService();
    // Instancia del servicio de negocio de la parte 4 para aprobaciones y validaciones de jefatura
    private final SolicitudService solicitudService = new SolicitudService();

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
                           @QueryParam("propias") String propias,
                           @QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        try {
            // Uso correcto de yo.getRol() adaptado a ApiPrincipal
            String rol = yo.getRol() != null ? yo.getRol() : "";
            boolean esRrhh = "RRHH".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol);
            boolean esJefatura = "JEFATURA".equalsIgnoreCase(rol);

            FiltroSolicitudes filtro =
                    FiltroSolicitudes.desdeParametros(estado, tipoSolicitudId, desde, hasta);

            List<Solicitud> lista;

            if (leerPropias(propias) || !(esRrhh || esJefatura)) {
                lista = servicio.listarMias(yo.getEmpleadoId(), filtro);
            } else if (esRrhh) {
                lista = solicitudService.listarTodas(filtro);
            } else {
                lista = solicitudService.listarPorDepartamento(yo.getEmpleadoId(), filtro);
            }

            List<SolicitudDTO> respuesta = new ArrayList<>();
            for (Solicitud s : lista) {
                respuesta.add(SolicitudMapper.toDTO(s));
            }
            return Response.ok(respuesta).build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") String idTexto) throws SQLException {

        int id = leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        try {
            // Validación adaptada mediante yo.getRol()
            String rol = yo.getRol() != null ? yo.getRol() : "";
            boolean esRrhh = "RRHH".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol);
            boolean esJefatura = "JEFATURA".equalsIgnoreCase(rol);

            if (esRrhh || esJefatura) {
                Solicitud solicitud = solicitudService.obtenerPorId(id);
                if (esRrhh) {
                    return Response.ok(SolicitudMapper.toDTO(solicitud)).build();
                }
                // jefatura: la suya o la de alguien de su departamento
                int idDuenio = solicitud.getEmpleado().getIdEmpleado();
                if (idDuenio == yo.getEmpleadoId()
                        || solicitudService.esJefeDelEmpleado(yo.getEmpleadoId(), idDuenio)) {
                    return Response.ok(SolicitudMapper.toDTO(solicitud)).build();
                }
                // de otro departamento: mismo 404, no se revela que existe
                throw new ApiException(404, "La solicitud indicada no existe.");
            }

            // No existe o es de otra persona: el mismo 404 (el contrato no revela ids ajenos).
            return Response.ok(SolicitudMapper.toDTO(servicio.detalle(yo.getEmpleadoId(), id))).build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    // Endpoints agregados para aprobar, rechazar e historial utilizando SolicitudService
    @POST
    @Path("/{id}/aprobar")
    @RequiresRole({"JEFATURA"})
    public Response aprobar(@PathParam("id") String idTexto) throws SQLException {
        int id = leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        try {
            solicitudService.aprobar(id, yo.getEmpleadoId(), yo.getUsuarioId());
            return Response.noContent().build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @POST
    @Path("/{id}/rechazar")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresRole({"JEFATURA"})
    public Response rechazar(@PathParam("id") String idTexto, RechazoSolicitudDTO datos) throws SQLException {
        int id = leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        try {
            String motivo = (datos != null && datos.getMotivoRechazo() != null) ? datos.getMotivoRechazo() : "";
            if (motivo.trim().isEmpty()) {
                throw new ValidacionApiException(Collections.singletonList("motivoRechazo: es obligatorio"));
            }
            solicitudService.rechazar(id, yo.getEmpleadoId(), motivo, yo.getUsuarioId());
            return Response.noContent().build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @GET
    @Path("/{id}/historial")
    public Response historial(@PathParam("id") String idTexto) throws SQLException {
        int id = leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        try {
            List<HistorialSolicitud> cambios =
                    solicitudService.obtenerHistorial(id, yo.getEmpleadoId(), yo.getRol());

            List<HistorialSolicitudDTO> respuesta = new ArrayList<>();
            for (HistorialSolicitud h : cambios) {
                respuesta.add(HistorialSolicitudMapper.toDTO(h));
            }
            return Response.ok(respuesta).build();
        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }
    // Fin de endpoints agregados

    // -------------------------------------------------------------- DELETE

    @DELETE
    @Path("/{id}")
    public Response cancelar(@PathParam("id") String idTexto) throws SQLException {

        int id = leerId(idTexto);
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

    /**
     * Convierte el identificador de la URL. Se recibe como TEXTO a propósito: si el
     * parámetro fuera un int, Jersey fallaría al convertir "abc" o un número enorme
     * y respondería 404 antes de entrar al método, pero el contrato manda 400 para
     * cualquier identificador inválido.
     */
    static int leerId(String texto) {
        int id;
        try {
            id = Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("El identificador debe ser un número entero mayor que cero");
        }
        if (id <= 0) {
            throw new BadRequestException("El identificador debe ser mayor que cero");
        }
        return id;
    }

    // propias=true devuelve solo lo del usuario, otro valor que no sea true/false es 400
    static boolean leerPropias(String texto) {
        if (texto == null || texto.trim().isEmpty() || "false".equalsIgnoreCase(texto.trim())) {
            return false;
        }
        if ("true".equalsIgnoreCase(texto.trim())) {
            return true;
        }
        throw new BadRequestException("El parámetro propias debe ser true o false");
    }
}