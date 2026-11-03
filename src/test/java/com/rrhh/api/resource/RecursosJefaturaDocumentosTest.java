package com.rrhh.api.resource;

import com.rrhh.api.dto.NuevaConstanciaDTO;
import com.rrhh.api.dto.RechazoSolicitudDTO;
import com.rrhh.api.dto.ResolverConstanciaDTO;
import com.rrhh.api.exception.ApiException;
import com.rrhh.api.exception.ValidacionApiException;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.security.SesionApi;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Pruebas de los caminos de ERROR 400 y 401 de constancias, aprobar/rechazar,
 * historial e incapacidades. Todos fallan ANTES de tocar la base de datos.
 *
 * Qué NO cubren (se probaron con docs/prueba-constancias.ps1 y prueba-api.ps1
 * contra Tomcat): los 200/201/204, los 403 por rol y los 404/409.
 */
public class RecursosJefaturaDocumentosTest {

    private static final ApiPrincipal YO = new ApiPrincipal(
            new SesionApi("token-de-prueba", 1, 1, "ana", "Ana", "RRHH",
                    Instant.now().plusSeconds(600)));

    private static ConstanciaResource constancias() {
        return new ConstanciaResource() {
            @Override
            protected ApiPrincipal usuarioActual() {
                return YO;
            }
        };
    }

    private static SolicitudResource solicitudes() {
        return new SolicitudResource() {
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

    private static String texto(int largo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < largo; i++) {
            sb.append('x');
        }
        return sb.toString();
    }

    // --------------------------------------------------- POST /constancias

    @Test
    public void solicitarConstanciaSinCuerpoResponde400() throws Exception {
        try {
            constancias().solicitar(null);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[cuerpo: es obligatorio]", camposDe(e).toString());
        }
    }

    @Test
    public void solicitarConstanciaVaciaListaTipoEInstitucion() throws Exception {
        try {
            constancias().solicitar(new NuevaConstanciaDTO());
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            List<String> campos = camposDe(e);
            assertEquals(2, campos.size());
            assertTrue(campos.contains("tipo: debe ser Salarial o Laboral"));
            assertTrue(campos.contains("institucion: es obligatorio"));
        }
    }

    @Test
    public void solicitarConstanciaConTipoInvalidoResponde400() throws Exception {
        NuevaConstanciaDTO datos = new NuevaConstanciaDTO();
        datos.setTipo("Otro");
        datos.setInstitucion("Banco");

        try {
            constancias().solicitar(datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[tipo: debe ser Salarial o Laboral]", camposDe(e).toString());
        }
    }

    @Test
    public void solicitarConstanciaConInstitucionMuyLargaResponde400() throws Exception {
        NuevaConstanciaDTO datos = new NuevaConstanciaDTO();
        datos.setTipo("Laboral");
        datos.setInstitucion(texto(151));

        try {
            constancias().solicitar(datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[institucion: debe tener entre 1 y 150 caracteres]", camposDe(e).toString());
        }
    }

    @Test
    public void solicitarConstanciaConMotivoMuyLargoResponde400() throws Exception {
        NuevaConstanciaDTO datos = new NuevaConstanciaDTO();
        datos.setTipo("Salarial");
        datos.setInstitucion("Banco");
        datos.setMotivo(texto(501));

        try {
            constancias().solicitar(datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[motivo: debe tener como máximo 500 caracteres]", camposDe(e).toString());
        }
    }

    @Test
    public void solicitarConstanciaSinAutenticarResponde401() throws Exception {
        try {
            new ConstanciaResource().solicitar(new NuevaConstanciaDTO());   // sin SecurityContext
            fail("Debió responder 401");
        } catch (ApiException e) {
            assertEquals(401, e.getEstado());
        }
    }

    // ---------------------------------------------------- GET /constancias

    @Test
    public void listarConstanciasConPropiasInvalidoResponde400() throws Exception {
        try {
            constancias().listar("abc");
            fail("Debió responder 400");
        } catch (BadRequestException e) {
            assertEquals("El parámetro propias debe ser true o false", e.getMessage());
        }
    }

    @Test
    public void unIdInvalidoResponde400EnTodosLosEndpointsDeConstancias() throws Exception {
        for (String malo : new String[]{"abc", "0", "-5", "99999999999"}) {
            try {
                constancias().obtener(malo);
                fail("GET debió responder 400 para: " + malo);
            } catch (BadRequestException e) {
                // esperado
            }
            try {
                constancias().aprobar(malo, null);
                fail("aprobar debió responder 400 para: " + malo);
            } catch (BadRequestException e) {
                // esperado
            }
            try {
                constancias().rechazar(malo, new ResolverConstanciaDTO());
                fail("rechazar debió responder 400 para: " + malo);
            } catch (BadRequestException e) {
                // esperado
            }
            try {
                constancias().pdf(malo, null);
                fail("pdf debió responder 400 para: " + malo);
            } catch (BadRequestException e) {
                // esperado
            }
        }
    }

    // ------------------------------------ aprobar y rechazar constancias

    @Test
    public void rechazarConstanciaSinCuerpoResponde400() throws Exception {
        try {
            constancias().rechazar("1", null);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[motivoRechazo: es obligatorio]", camposDe(e).toString());
        }
    }

    @Test
    public void rechazarConstanciaSinMotivoOConMotivoEnBlancoResponde400() throws Exception {
        ResolverConstanciaDTO vacio = new ResolverConstanciaDTO();
        ResolverConstanciaDTO enBlanco = new ResolverConstanciaDTO();
        enBlanco.setMotivoRechazo("   ");

        for (ResolverConstanciaDTO datos : new ResolverConstanciaDTO[]{vacio, enBlanco}) {
            try {
                constancias().rechazar("1", datos);
                fail("Debió responder 400");
            } catch (ValidacionApiException e) {
                assertEquals("[motivoRechazo: es obligatorio]", camposDe(e).toString());
            }
        }
    }

    @Test
    public void rechazarConstanciaConObservacionesMuyLargasResponde400() throws Exception {
        ResolverConstanciaDTO datos = new ResolverConstanciaDTO();
        datos.setMotivoRechazo("Falta información");
        datos.setObservaciones(texto(1001));

        try {
            constancias().rechazar("1", datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[observaciones: debe tener como máximo 1000 caracteres]", camposDe(e).toString());
        }
    }

    @Test
    public void aprobarConstanciaConObservacionesMuyLargasResponde400() throws Exception {
        ResolverConstanciaDTO datos = new ResolverConstanciaDTO();
        datos.setObservaciones(texto(1001));

        try {
            constancias().aprobar("1", datos);
            fail("Debió responder 400");
        } catch (ValidacionApiException e) {
            assertEquals("[observaciones: debe tener como máximo 1000 caracteres]", camposDe(e).toString());
        }
    }

    // ---------------------------------- rechazar solicitud (jefatura)

    @Test
    public void rechazarSolicitudSinMotivoResponde400() throws Exception {
        RechazoSolicitudDTO enBlanco = new RechazoSolicitudDTO();
        enBlanco.setMotivoRechazo("  ");

        for (RechazoSolicitudDTO datos : new RechazoSolicitudDTO[]{null, new RechazoSolicitudDTO(), enBlanco}) {
            try {
                solicitudes().rechazar("1", datos);
                fail("Debió responder 400");
            } catch (ValidacionApiException e) {
                assertEquals("[motivoRechazo: es obligatorio]", camposDe(e).toString());
            }
        }
    }

    // ------------------------------------------- historial e incapacidades

    @Test
    public void historialConIdInvalidoResponde400() throws Exception {
        for (String malo : new String[]{"abc", "0", "-1"}) {
            try {
                solicitudes().historial(malo);
                fail("Debió responder 400 para: " + malo);
            } catch (BadRequestException e) {
                // esperado
            }
        }
    }

    @Test
    public void obtenerIncapacidadConIdInvalidoResponde400() throws Exception {
        for (String malo : new String[]{"abc", "0", "99999999999"}) {
            try {
                incapacidades().obtener(malo);
                fail("Debió responder 400 para: " + malo);
            } catch (BadRequestException e) {
                // esperado
            }
        }
    }

    // ----------------------------------------------------------- propias

    @Test
    public void listarSolicitudesConPropiasInvalidoResponde400() throws Exception {
        try {
            solicitudes().listar(null, null, "abc", null, null);
            fail("Debió responder 400");
        } catch (BadRequestException e) {
            assertEquals("El parámetro propias debe ser true o false", e.getMessage());
        }
    }

    @Test
    public void leerPropiasAceptaTrueFalseOVacio() {
        assertFalse(SolicitudResource.leerPropias(null));
        assertFalse(SolicitudResource.leerPropias(""));
        assertFalse(SolicitudResource.leerPropias("false"));
        assertTrue(SolicitudResource.leerPropias("true"));
        assertTrue(SolicitudResource.leerPropias(" TRUE "));
    }
}