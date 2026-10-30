package com.rrhh.api.resource;

import com.rrhh.api.dto.NuevaIncapacidadDTO;
import com.rrhh.api.dto.NuevaSolicitudDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.security.SesionApi;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Pruebas de los caminos de ERROR 400 y 401 de los recursos del empleado.
 * Todos fallan ANTES de tocar la base de datos, así que no la necesitan.
 *
 * Qué NO cubren (necesitan servidor y base de datos): las respuestas
 * correctas 200/201/204 y los errores 404/409, que dependen de datos reales;
 * y los de identificador <= 0, que construyen una BadRequestException y
 * requieren Jersey en tiempo de ejecución.
 */
public class RecursosEmpleadoTest {

    private static final ApiPrincipal YO = new ApiPrincipal(
            new SesionApi("token-de-prueba", 1, 1, "juan", "Juan", "EMPLEADO",
                    Instant.now().plusSeconds(600)));

    private static SolicitudResource solicitudes() {
        return new SolicitudResource() {
            @Override
            protected ApiPrincipal usuarioActual() {
                return YO;
            }
        };
    }

    private static VacacionesResource vacaciones() {
        return new VacacionesResource() {
            @Override
            protected ApiPrincipal usuarioActual() {
                return YO;
            }
        };
    }

    private static IncapacidadResource incapacidades() {
        return new IncapacidadResource() {
            @Override
            protected ApiPrincipal usuarioActual() {
                return YO;
            }
        };
    }

    private static List<String> camposDe(ValidacionApiException e) {
        assertEquals(400, e.getEstado());
        return e.getCampos();
    }

    // ---------------------------------------------------- POST /solicitudes

    @Test
    public void crearSolicitudSinCuerpoResponde400() throws Exception {
        try {
            solicitudes().crear(null);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[cuerpo: es obligatorio]", camposDe(e).toString());
        }
    }

    @Test
    public void crearSolicitudVaciaListaTodosLosCamposFaltantes() throws Exception {
        try {
            solicitudes().crear(new NuevaSolicitudDTO());
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            List<String> campos = camposDe(e);
            assertEquals(4, campos.size());
            assertTrue(campos.contains("tipoSolicitudId: debe ser un entero mayor que cero"));
            assertTrue(campos.contains("motivo: es obligatorio"));
            assertTrue(campos.contains("fechaInicio: es obligatoria"));
            assertTrue(campos.contains("fechaFin: es obligatoria"));
        }
    }

    @Test
    public void crearSolicitudConFechaMalFormadaYMotivoCortoListaAmbos() throws Exception {
        NuevaSolicitudDTO datos = new NuevaSolicitudDTO();
        datos.setTipoSolicitudId(1);
        datos.setMotivo("abc");
        datos.setFechaInicio("15/11/2026");
        datos.setFechaFin("2026-11-20");

        try {
            solicitudes().crear(datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            List<String> campos = camposDe(e);
            assertEquals(2, campos.size());
            assertTrue(campos.contains("motivo: debe tener entre 5 y 250 caracteres"));
            assertTrue(campos.contains("fechaInicio: debe tener el formato yyyy-MM-dd"));
        }
    }

    @Test
    public void crearSolicitudSinAutenticarResponde401() throws Exception {
        try {
            new SolicitudResource().crear(new NuevaSolicitudDTO());   // sin SecurityContext
            fail("Debió responder 401");
        } catch (ApiException e) {
            assertEquals(401, e.getEstado());
        }
    }

    // ----------------------------------------------------- GET /solicitudes

    @Test
    public void listarConEstadoInvalidoResponde400() throws Exception {
        try {
            solicitudes().listar("ARCHIVADA", null, null, null);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertTrue(camposDe(e).get(0).startsWith("estado: debe ser uno de"));
        }
    }

    @Test
    public void listarConVariosFiltrosInvalidosLosReportaTodos() throws Exception {
        try {
            solicitudes().listar("X", "abc", "mal", "2026-10-01");
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals(3, camposDe(e).size());
        }
    }

    @Test
    public void listarConDesdePosteriorAHastaResponde400() throws Exception {
        try {
            solicitudes().listar(null, null, "2026-10-31", "2026-10-01");
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[desde: no puede ser posterior a hasta]", camposDe(e).toString());
        }
    }

    // ----------------------------------------------- GET /vacaciones/saldo

    @Test
    public void saldoConAnioNoNumericoOCortoResponde400() throws Exception {
        for (String malo : new String[]{"20x6", "26", "20266", "dos mil"}) {
            try {
                vacaciones().saldo(malo);
                fail("Debió responder 400 para: " + malo);
            } catch (ValidacionApiException e) {
                assertEquals(malo, "[anio: debe ser un número de cuatro dígitos]", camposDe(e).toString());
            }
        }
    }

    // ------------------------------------------------- POST /incapacidades

    @Test
    public void registrarIncapacidadSinCuerpoResponde400() throws Exception {
        try {
            incapacidades().registrar(null);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[cuerpo: es obligatorio]", camposDe(e).toString());
        }
    }

    @Test
    public void registrarIncapacidadVaciaListaLosCamposObligatorios() throws Exception {
        try {
            incapacidades().registrar(new NuevaIncapacidadDTO());
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            List<String> campos = camposDe(e);
            assertEquals(3, campos.size());
            assertTrue(campos.contains("numeroDocumento: es obligatorio"));
            assertTrue(campos.contains("fechaInicio: es obligatoria"));
            assertTrue(campos.contains("fechaFin: es obligatoria"));
        }
    }

    @Test
    public void registrarIncapacidadConObservacionesMuyLargasResponde400() throws Exception {
        StringBuilder largas = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            largas.append('x');
        }
        NuevaIncapacidadDTO datos = new NuevaIncapacidadDTO();
        datos.setNumeroDocumento("ISSS-1");
        datos.setFechaInicio("2026-10-26");
        datos.setFechaFin("2026-10-28");
        datos.setObservaciones(largas.toString());

        try {
            incapacidades().registrar(datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[observaciones: debe tener como máximo 500 caracteres]", camposDe(e).toString());
        }
    }
}
