package com.permisos.service;

import com.permisos.model.Solicitud;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filtros del listado de solicitudes (GET /api/solicitudes), según el contrato:
 * estado, tipoSolicitudId, empleadoId, departamentoId, desde y hasta. Un valor nulo
 * significa "sin filtro".
 *
 * Los parámetros llegan como texto (query string); aquí se convierten una sola
 * vez y de forma segura. Como dice el contrato, desde y hasta se comparan con la
 * FECHA DE INICIO de la solicitud, con ambos extremos incluidos.
 *
 * El filtro "propias" depende del rol y se resuelve en SolicitudResource.
 * Los filtros empleadoId y departamentoId se combinan (AND) con el alcance del rol:
 * una jefatura que pide un empleado de otro departamento recibe una lista vacía.
 *
 * Es Java puro (sin JPA ni JAX-RS) para poder probarlo sin infraestructura.
 */
public final class FiltroSolicitudes {

    private static final List<String> ESTADOS_VALIDOS = Arrays.asList(
            Solicitud.ESTADO_PENDIENTE,
            Solicitud.ESTADO_APROBADA,
            Solicitud.ESTADO_RECHAZADA,
            Solicitud.ESTADO_CANCELADA);

    private final String estado;
    private final Integer idTipo;
    private final Integer idEmpleado;
    private final Integer idDepartamento;
    private final LocalDate desde;
    private final LocalDate hasta;

    private FiltroSolicitudes(String estado, Integer idTipo, Integer idEmpleado,
                              Integer idDepartamento, LocalDate desde, LocalDate hasta) {
        this.estado = estado;
        this.idTipo = idTipo;
        this.idEmpleado = idEmpleado;
        this.idDepartamento = idDepartamento;
        this.desde = desde;
        this.hasta = hasta;
    }

    /** Filtro sin restricciones: devuelve todo. */
    public static FiltroSolicitudes vacio() {
        return new FiltroSolicitudes(null, null, null, null, null, null);
    }

    /**
     * Construye el filtro desde los parámetros crudos. Un parámetro nulo o en
     * blanco significa "sin filtro" para ese campo. Reúne TODOS los errores.
     *
     * @throws ValidacionException con un texto "campo: problema" por cada valor inválido
     */
    public static FiltroSolicitudes desdeParametros(String estado, String tipoSolicitudId,
                                                    String desde, String hasta)
            throws ValidacionException {
        return desdeParametros(estado, tipoSolicitudId, null, null, desde, hasta);
    }

    public static FiltroSolicitudes desdeParametros(String estado, String tipoSolicitudId,
                                                    String empleadoId, String departamentoId,
                                                    String desde, String hasta)
            throws ValidacionException {

        List<String> errores = new ArrayList<>();

        String estadoLimpio = null;
        if (!esBlanco(estado)) {
            estadoLimpio = estado.trim().toUpperCase();
            if (!ESTADOS_VALIDOS.contains(estadoLimpio)) {
                errores.add("estado: debe ser uno de " + ESTADOS_VALIDOS);
                estadoLimpio = null;
            }
        }

        Integer tipo = null;
        if (!esBlanco(tipoSolicitudId)) {
            try {
                tipo = Integer.valueOf(tipoSolicitudId.trim());
                if (tipo <= 0) {
                    errores.add("tipoSolicitudId: debe ser un entero mayor que cero");
                    tipo = null;
                }
            } catch (NumberFormatException e) {
                errores.add("tipoSolicitudId: debe ser un entero mayor que cero");
            }
        }

        Integer idEmpleado = parsearEntero(empleadoId, "empleadoId", errores);
        Integer idDepartamento = parsearEntero(departamentoId, "departamentoId", errores);

        LocalDate desdeFecha = parsearFecha(desde, "desde", errores);
        LocalDate hastaFecha = parsearFecha(hasta, "hasta", errores);

        if (desdeFecha != null && hastaFecha != null && desdeFecha.isAfter(hastaFecha)) {
            errores.add("desde: no puede ser posterior a hasta");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        return new FiltroSolicitudes(estadoLimpio, tipo, idEmpleado, idDepartamento, desdeFecha, hastaFecha);
    }

    private static Integer parsearEntero(String valor, String campo, List<String> errores) {
        if (esBlanco(valor)) {
            return null;
        }
        Integer numero = null;
        try {
            numero = Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            numero = null;
        }
        if (numero == null || numero <= 0) {
            errores.add(campo + ": debe ser un entero mayor que cero");
            return null;
        }
        return numero;
    }

    private static LocalDate parsearFecha(String valor, String campo, List<String> errores) {
        if (esBlanco(valor)) {
            return null;
        }
        try {
            // Formato ISO del contrato de la API: yyyy-MM-dd
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            errores.add(campo + ": debe tener el formato yyyy-MM-dd");
            return null;
        }
    }

    private static boolean esBlanco(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    public String getEstado() {
        return estado;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public Integer getIdDepartamento() {
        return idDepartamento;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }
}
