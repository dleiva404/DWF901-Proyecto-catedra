
package com.rrhh.api.resource;

import com.permisos.dao.SolicitudDAO;
import com.permisos.model.Rol;
import com.permisos.model.Solicitud;
import com.permisos.model.Usuario;
import com.rrhh.api.dto.SolicitudDTO;
import com.rrhh.api.mapper.SolicitudMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.DELETE;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Path("/solicitudes")
@Produces(MediaType.APPLICATION_JSON)
public class SolicitudResource {

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();

    @Context
    private HttpServletRequest request;

    @GET
    public Response listarSolicitudes() throws SQLException {

        // 1. Comprobar que exista una sesión iniciada.
        HttpSession session = request.getSession(false);

        if (session == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Debe iniciar sesión para consultar solicitudes.")
                    .build();
        }

        // 2. Recuperar el usuario autenticado.
        Object usuarioSesion = session.getAttribute("usuario");

        if (!(usuarioSesion instanceof Usuario)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("La sesión no contiene un usuario válido.")
                    .build();
        }

        Usuario usuario = (Usuario) usuarioSesion;

        // 3. Obtener el rol de la sesión.
        Object rolSesion = session.getAttribute("rol");
        Rol rol;

        if (rolSesion instanceof Rol) {
            rol = (Rol) rolSesion;
        } else {
            rol = usuario.getRol();
        }

        if (rol == null || rol.getNombre() == null) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("No se pudo determinar el rol del usuario.")
                    .build();
        }

        String nombreRol = rol.getNombre().trim().toUpperCase();

        // 4. Consultar únicamente las solicitudes permitidas
        // para el rol del usuario.
        List<Solicitud> solicitudes;

        switch (nombreRol) {

            case "EMPLEADO":
                solicitudes = solicitudDAO.listarPorEmpleado(
                        usuario.getIdEmpleado()
                );
                break;

            case "JEFATURA":
                solicitudes =
                        solicitudDAO.listarPendientesPorJefatura(
                                usuario.getIdEmpleado()
                        );
                break;

            case "RRHH":
                solicitudes = solicitudDAO.listarParaRRHH();
                break;

            default:
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("El rol no tiene permiso para consultar solicitudes.")
                        .build();
        }

        // 5. Convertir las entidades JPA a DTO.
        List<SolicitudDTO> respuesta = new ArrayList<>();

        for (Solicitud solicitud : solicitudes) {
            respuesta.add(SolicitudMapper.toDTO(solicitud));
        }

        // 6. Responder con JSON y HTTP 200.
        return Response.ok(respuesta).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarSolicitud(@PathParam("id") int id)
            throws SQLException {

        // 1. Validar el identificador.
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El identificador debe ser mayor que cero.")
                    .build();
        }

        // 2. Verificar la sesión.
        HttpSession session = request.getSession(false);

        if (session == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Debe iniciar sesión.")
                    .build();
        }

        Object usuarioSesion = session.getAttribute("usuario");

        if (!(usuarioSesion instanceof Usuario)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("La sesión no contiene un usuario válido.")
                    .build();
        }

        Usuario usuario = (Usuario) usuarioSesion;

        Object rolSesion = session.getAttribute("rol");
        Rol rol = rolSesion instanceof Rol
                ? (Rol) rolSesion
                : usuario.getRol();

        if (rol == null || rol.getNombre() == null) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("No se pudo determinar el rol.")
                    .build();
        }

        String nombreRol = rol.getNombre().trim().toUpperCase();

        // 3. Buscar la solicitud.
        Solicitud solicitud = solicitudDAO.buscarPorId(id);

        if (solicitud == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No se encontró la solicitud.")
                    .build();
        }

        // 4. Verificar los permisos de consulta.
        boolean autorizado = false;

        switch (nombreRol) {

            case "EMPLEADO":
                autorizado =
                        solicitud.getIdEmpleado() == usuario.getIdEmpleado();
                break;

            case "JEFATURA":
                List<Solicitud> pendientes =
                        solicitudDAO.listarPendientesPorJefatura(
                                usuario.getIdEmpleado()
                        );

                for (Solicitud pendiente : pendientes) {
                    if (pendiente.getIdSolicitud()
                            == solicitud.getIdSolicitud()) {
                        autorizado = true;
                        break;
                    }
                }
                break;

            case "RRHH":
                List<Solicitud> solicitudesRRHH =
                        solicitudDAO.listarParaRRHH();

                for (Solicitud s : solicitudesRRHH) {
                    if (s.getIdSolicitud() == solicitud.getIdSolicitud()) {
                        autorizado = true;
                        break;
                    }
                }
                break;

            default:
                autorizado = false;
        }

        if (!autorizado) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No se encontró la solicitud.")
                    .build();
        }

        // 5. Devolver el DTO, nunca la entidad directamente.
        return Response.ok(
                SolicitudMapper.toDTO(solicitud)
        ).build();
    }

    @DELETE
    @Path("/{id}")
    public Response cancelarSolicitud(@PathParam("id") int id)
            throws SQLException {

        // 1. Validar el identificador.
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El identificador debe ser mayor que cero.")
                    .build();
        }

        // 2. Comprobar que exista una sesión activa.
        HttpSession session = request.getSession(false);

        if (session == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Debe iniciar sesión.")
                    .build();
        }

        // 3. Recuperar el usuario autenticado.
        Object usuarioSesion = session.getAttribute("usuario");

        if (!(usuarioSesion instanceof Usuario)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("La sesión no contiene un usuario válido.")
                    .build();
        }

        Usuario usuario = (Usuario) usuarioSesion;

        // 4. Determinar el rol del usuario.
        Object rolSesion = session.getAttribute("rol");

        Rol rol = rolSesion instanceof Rol
                ? (Rol) rolSesion
                : usuario.getRol();

        if (rol == null || rol.getNombre() == null) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("No se pudo determinar el rol del usuario.")
                    .build();
        }

        String nombreRol = rol.getNombre().trim().toUpperCase();

        // 5. Solo un empleado puede cancelar su propia solicitud.
        if (!"EMPLEADO".equals(nombreRol)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Solo el empleado puede cancelar su solicitud.")
                    .build();
        }

        // 6. Ejecutar la cancelación usando la lógica existente del DAO.
        boolean cancelada = solicitudDAO.cancelar(
                id,
                usuario.getIdEmpleado(),
                usuario.getIdUsuario()
        );

        // 7. Si el DAO no pudo cancelar, no revelar información ajena.
        if (!cancelada) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No se encontró una solicitud pendiente que pueda cancelar.")
                    .build();
        }

        // 8. HTTP 204: operación exitosa, sin cuerpo de respuesta.
        return Response.noContent().build();
    }

}
