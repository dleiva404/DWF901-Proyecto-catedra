package com.rrhh.api.resource;

import com.permisos.model.Constancia;
import com.google.zxing.WriterException;
import com.permisos.dao.EmpleadoDAO;
import com.permisos.model.Empleado;
import com.permisos.service.ConstanciaPdfGenerador;
import com.permisos.service.ConstanciaService;
import com.permisos.service.ReglaNegocioException;
import com.rrhh.api.dto.ConstanciaDTO;
import com.rrhh.api.dto.NuevaConstanciaDTO;
import com.rrhh.api.dto.ResolverConstanciaDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.TraductorErroresServicio;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.filter.RequiresRole;
import com.rrhh.api.mapper.ConstanciaMapper;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.validation.ValidadorApi;

import javax.ws.rs.Consumes;
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
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constancias del usuario autenticado (endpoints 18, 19 y 20 del contrato).
 * Empleado y jefatura ven las suyas; RRHH ve todas.
 */
@Path("/constancias")
@Produces(MediaType.APPLICATION_JSON)
@RequiresRole({"EMPLEADO", "JEFATURA", "RRHH"})
public class ConstanciaResource {

    private final ConstanciaService servicio = new ConstanciaService();

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    public Response listar(@QueryParam("propias") String propias) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        // RRHH ve todas, salvo que pida solo las suyas
        boolean soloPropias = SolicitudResource.leerPropias(propias);
        boolean verTodas = esRrhh(yo) && !soloPropias;

        List<ConstanciaDTO> respuesta = new ArrayList<>();
        for (Constancia c : servicio.listar(yo.getEmpleadoId(), verTodas)) {
            respuesta.add(ConstanciaMapper.toDTO(c));
        }
        return Response.ok(respuesta).build();
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") String idTexto) throws SQLException {

        int id = SolicitudResource.leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        try {
            Constancia encontrada = servicio.detalle(yo.getEmpleadoId(), esRrhh(yo), id);
            return Response.ok(ConstanciaMapper.toDTO(encontrada)).build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response solicitar(NuevaConstanciaDTO datos) throws SQLException {

        ApiPrincipal yo = usuarioActual();

        if (datos == null) {
            throw new ValidacionApiException(Collections.singletonList("cuerpo: es obligatorio"));
        }

        ValidadorApi validador = new ValidadorApi();

        // solo Salarial o Laboral, como en la aplicación web
        String tipo = null;
        if (datos.getTipo() != null && "salarial".equalsIgnoreCase(datos.getTipo().trim())) {
            tipo = "Salarial";
        } else if (datos.getTipo() != null && "laboral".equalsIgnoreCase(datos.getTipo().trim())) {
            tipo = "Laboral";
        } else {
            validador.error("tipo", "debe ser Salarial o Laboral");
        }

        validador.obligatorio("institucion", datos.getInstitucion());
        validador.longitud("institucion", datos.getInstitucion(), 1, 150);
        if (datos.getMotivo() != null && datos.getMotivo().trim().length() > 500) {
            validador.error("motivo", "debe tener como máximo 500 caracteres");
        }
        validador.lanzarSiHayErrores();

        String motivo = (datos.getMotivo() == null || datos.getMotivo().trim().isEmpty())
                ? null : datos.getMotivo().trim();

        try {
            Constancia creada = servicio.crear(
                    yo.getEmpleadoId(), tipo, datos.getInstitucion().trim(), motivo);

            URI ubicacion = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(creada.getIdConstancia()))
                    .build();

            return Response.created(ubicacion)
                    .entity(ConstanciaMapper.toDTO(creada))
                    .build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

        @POST
    @Path("/{id}/aprobar")
    @RequiresRole({"RRHH"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aprobar(@PathParam("id") String idTexto, ResolverConstanciaDTO datos)
            throws SQLException {

        int id = SolicitudResource.leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        // el cuerpo es opcional
        String observaciones = (datos == null) ? null : datos.getObservaciones();

        ValidadorApi validador = new ValidadorApi();
        if (observaciones != null && observaciones.trim().length() > 1000) {
            validador.error("observaciones", "debe tener como máximo 1000 caracteres");
        }
        validador.lanzarSiHayErrores();

        try {
            servicio.aprobar(id, yo.getUsuarioId(), limpiar(observaciones));
            return Response.noContent().build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    @POST
    @Path("/{id}/rechazar")
    @RequiresRole({"RRHH"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rechazar(@PathParam("id") String idTexto, ResolverConstanciaDTO datos)
            throws SQLException {

        int id = SolicitudResource.leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        if (datos == null) {
            throw new ValidacionApiException(
                    Collections.singletonList("motivoRechazo: es obligatorio"));
        }

        ValidadorApi validador = new ValidadorApi();
        validador.obligatorio("motivoRechazo", datos.getMotivoRechazo());
        validador.longitud("motivoRechazo", datos.getMotivoRechazo(), 1, 1000);
        if (datos.getObservaciones() != null && datos.getObservaciones().trim().length() > 1000) {
            validador.error("observaciones", "debe tener como máximo 1000 caracteres");
        }
        validador.lanzarSiHayErrores();

        try {
            servicio.rechazar(id, yo.getUsuarioId(),
                    datos.getMotivoRechazo().trim(), limpiar(datos.getObservaciones()));
            return Response.noContent().build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        }
    }

    // texto vacío se guarda como null
    private String limpiar(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? null : texto.trim();
    }

    @GET
    @Path("/{id}/pdf")
    @Produces({"application/pdf", MediaType.APPLICATION_JSON})
    public Response pdf(@PathParam("id") String idTexto, @Context HttpServletRequest request)
            throws SQLException {

        int id = SolicitudResource.leerId(idTexto);
        ApiPrincipal yo = usuarioActual();

        try {
            // 404 si no existe o es de otro empleado (RRHH ve todas)
            Constancia constancia = servicio.detalle(yo.getEmpleadoId(), esRrhh(yo), id);

            if (!Constancia.ESTADO_APROBADA.equals(constancia.getEstado())) {
                throw new ApiException(409, "La constancia aún no está aprobada.");
            }

            Empleado empleado = new EmpleadoDAO().buscarPorId(constancia.getIdEmpleado());

            String carpetaLogos = request.getServletContext().getRealPath("/img/logos");
            String urlVerificacion = request.getRequestURL().toString().replace(request.getRequestURI(), "")
                    + request.getContextPath() + "/verificar?token=" + constancia.getTokenVerificacion();

            byte[] pdf = new ConstanciaPdfGenerador()
                    .generar(constancia, empleado, carpetaLogos, urlVerificacion);

            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "inline; filename=constancia_" + id + ".pdf")
                    .build();

        } catch (ReglaNegocioException e) {
            throw TraductorErroresServicio.traducir(e);
        } catch (IOException | WriterException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la constancia.", e);
        }
    }

    private boolean esRrhh(ApiPrincipal yo) {
        return "RRHH".equalsIgnoreCase(yo.getRol());
    }

    protected ApiPrincipal usuarioActual() {
        ApiPrincipal yo = ApiPrincipal.desde(securityContext);
        if (yo == null) {
            throw new ApiException(401, "Autenticación requerida");
        }
        return yo;
    }
}