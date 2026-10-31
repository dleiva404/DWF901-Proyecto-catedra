package com.permisos.service;

import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.Incapacidad;
import com.permisos.model.Solicitud;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * PRUEBA DE INTEGRACIÓN: usa la base de datos REAL.
 *
 * Por su nombre (termina en IT) Maven NO la ejecuta con "mvn package" ni con
 * "mvn test": no debe correr sola porque escribe datos. Se lanza a propósito:
 *
 *     mvn test -Dtest=SolicitudEmpleadoServiceIT
 *
 * Requisitos:
 *  - MySQL encendido y db.properties con tu contraseña.
 *  - sistema_permisos.sql recién importado (Juan = empleado 1, usuario 1, con
 *    15 días de vacaciones del año actual; Carlos Gómez = empleado 2).
 *
 * Si no hay base de datos o faltan esos datos, la prueba se OMITE (no falla).
 *
 * Deja registros nuevos en la base (una solicitud cancelada y una incapacidad
 * pendiente): reimporta el SQL cuando quieras volver a empezar limpio.
 *
 * Importante: esta prueba se escribió sin poder ejecutarla contra MySQL. Si
 * falla, el mensaje dirá en qué paso; ese dato es justo el que hace falta.
 */
public class SolicitudEmpleadoServiceIT {

    private static final int JUAN = 1;
    private static final int USUARIO_JUAN = 1;
    private static final int OTRO_EMPLEADO = 2;

    private final SolicitudEmpleadoService servicio = new SolicitudEmpleadoService();
    private final IncapacidadEmpleadoService incapacidades = new IncapacidadEmpleadoService();

    @BeforeClass
    public static void requiereBaseDeDatosConDatosDePrueba() {
        try {
            new SolicitudEmpleadoService().saldoVacaciones(JUAN, LocalDate.now().getYear());
        } catch (Throwable e) {
            // Throwable y no Exception: si JPA no arranca (sin base de datos), la inicialización
            // de JPAUtil lanza un Error (ExceptionInInitializerError), no una Exception.
            Assume.assumeNoException(
                    "Sin base de datos o sin el saldo de Juan del año actual: se omite la prueba.", e);
        }
    }

    private static LocalDate proximoLunes() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private static int idTipo(String nombre) throws Exception {
        return new TipoSolicitudDAO().buscarPorNombre(nombre).getIdTipoSolicitud();
    }

    @Test
    public void escenarioCompletoDeVacaciones() throws Exception {
        LocalDate lunes = proximoLunes();
        int anio = LocalDate.now().getYear();
        Assume.assumeTrue("El próximo lunes cae en otro año; se omite.", lunes.getYear() == anio);

        ResumenVacaciones antes = servicio.saldoVacaciones(JUAN, anio);

        // 1. Crear: queda PENDIENTE y cuenta 3 días (lunes a miércoles)
        Solicitud creada = servicio.crear(
                JUAN, idTipo("VACACIONES"), "Prueba de integración", lunes, lunes.plusDays(2));
        int id = creada.getIdSolicitud();

        assertTrue("Debe tener id", id > 0);
        assertEquals(3, creada.getDiasSolicitados());
        assertEquals(Solicitud.ESTADO_PENDIENTE, creada.getEstado());
        assertEquals(JUAN, creada.getIdEmpleado());
        assertEquals("VACACIONES", creada.getTipoSolicitud().getNombre());

        // 2. Saldo: suben los pendientes, pero lo disponible NO baja hasta que se apruebe
        ResumenVacaciones conPendiente = servicio.saldoVacaciones(JUAN, anio);
        assertEquals(antes.getDiasPendientes() + 3, conPendiente.getDiasPendientes());
        assertEquals(antes.getDiasDisponibles(), conPendiente.getDiasDisponibles());
        // lo que todavía se puede pedir (disponibles - pendientes) baja en 3
        assertEquals(antes.getDiasDisponibles() - antes.getDiasPendientes() - 3,
                conPendiente.getDiasDisponibles() - conPendiente.getDiasPendientes());

        // 3. Listado propio con filtro por estado: aparece
        List<Solicitud> pendientes = servicio.listarMias(
                JUAN, FiltroSolicitudes.desdeParametros("PENDIENTE", null, null, null));
        boolean aparece = false;
        for (Solicitud s : pendientes) {
            aparece = aparece || s.getIdSolicitud() == id;
        }
        assertTrue("La solicitud nueva debe aparecer en las pendientes", aparece);

        // 4. Detalle propio funciona; el de otra persona responde "no existe"
        assertEquals(id, servicio.detalle(JUAN, id).getIdSolicitud());
        try {
            servicio.detalle(OTRO_EMPLEADO, id);
            fail("Otro empleado no debe poder ver la solicitud");
        } catch (RecursoNoEncontradoException esperado) {
            // 404
        }

        // 5. Pedir más de lo solicitable se rechaza (cuenta los pendientes)
        int solicitable = conPendiente.getDiasDisponibles() - conPendiente.getDiasPendientes();
        LocalDate finDemasiadoLargo = lunes.plusDays(solicitable + 7);
        try {
            servicio.crear(JUAN, idTipo("VACACIONES"), "Prueba de integración", lunes, finDemasiadoLargo);
            fail("Debió rechazar por saldo insuficiente");
        } catch (RecursoNoEncontradoException | ConflictoException | ValidacionException inesperada) {
            // El saldo insuficiente NO es dato inválido (400) ni recurso inexistente (404): el
            // contrato lo manda a 409, que es lo que produce una ReglaNegocioException genérica.
            fail("Debió ser una regla de negocio genérica (409), no " + inesperada.getClass().getSimpleName());
        } catch (ReglaNegocioException esperado) {
            assertTrue(esperado.getMessage(), esperado.getMessage().contains("suficientes días"));
        }

        // 6. Cancelar la propia pendiente
        servicio.cancelar(id, JUAN, USUARIO_JUAN);
        assertEquals(Solicitud.ESTADO_CANCELADA, servicio.detalle(JUAN, id).getEstado());
        assertEquals(antes.getDiasPendientes(), servicio.saldoVacaciones(JUAN, anio).getDiasPendientes());

        // 7. Cancelar otra vez -> conflicto (409); cancelar una ajena -> "no existe" (404)
        try {
            servicio.cancelar(id, JUAN, USUARIO_JUAN);
            fail("Una solicitud ya cancelada no se puede cancelar de nuevo");
        } catch (ConflictoException esperado) {
            // 409
        }
        try {
            servicio.cancelar(id, OTRO_EMPLEADO, USUARIO_JUAN);
            fail("Nadie puede cancelar la solicitud de otra persona");
        } catch (RecursoNoEncontradoException esperado) {
            // 404
        }
    }

    @Test
    public void unaIncapacidadSeRegistraJuntoConSuSolicitud() throws Exception {
        LocalDate lunesPasado = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));

        Incapacidad inc = incapacidades.registrar(
                JUAN, "IT-" + System.currentTimeMillis(), lunesPasado, lunesPasado.plusDays(2), "Reposo");

        assertTrue("Debe tener id de incapacidad", inc.getIdIncapacidad() > 0);
        assertTrue("Debe tener id de solicitud", inc.getIdSolicitud() > 0);

        // La solicitud existe, es de Juan, de tipo INCAPACIDAD y está pendiente
        Solicitud s = servicio.detalle(JUAN, inc.getIdSolicitud());
        assertEquals("INCAPACIDAD", s.getTipoSolicitud().getNombre());
        assertEquals(Solicitud.ESTADO_PENDIENTE, s.getEstado());
        assertEquals(3, s.getDiasSolicitados());
    }

    @Test
    public void unaIncapacidadSinDocumentoNoSeGuardaNada() throws Exception {
        LocalDate lunesPasado = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        int antes = servicio.listarMias(JUAN, FiltroSolicitudes.vacio()).size();

        try {
            incapacidades.registrar(JUAN, "  ", lunesPasado, lunesPasado.plusDays(1), null);
            fail("Debió rechazar la incapacidad sin documento");
        } catch (ValidacionException esperado) {
            assertEquals("numeroDocumento: es obligatorio", esperado.getErrores().get(0));
        }

        assertEquals("No debe haberse creado ninguna solicitud",
                antes, servicio.listarMias(JUAN, FiltroSolicitudes.vacio()).size());
    }

    @Test
    public void crearConElTipoIncapacidadSeRechazaYApuntaAlEndpointCorrecto() throws Exception {
        LocalDate lunes = proximoLunes();

        try {
            servicio.crear(JUAN, idTipo("INCAPACIDAD"), "Prueba de integración", lunes, lunes);
            fail("Una incapacidad no se crea como solicitud normal");
        } catch (ValidacionException esperado) {
            assertTrue(esperado.getMessage(), esperado.getMessage().contains("/api/incapacidades"));
        }
    }
}
