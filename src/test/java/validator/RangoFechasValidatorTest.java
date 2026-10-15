package validator;

import com.permisos.validator.RangoFechasValidator;
import org.junit.Before;
import org.junit.Test;

import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.validator.ValidatorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class RangoFechasValidatorTest {

    private final RangoFechasValidator validador = new RangoFechasValidator();
    private UIForm form;
    private UIInput desde;
    private UIInput hasta;

    @Before
    public void armarFormulario() {
        form = new UIForm();
        form.setId("formSolicitud");
        desde = new UIInput();
        desde.setId("desde");
        hasta = new UIInput();
        hasta.setId("hasta");
        form.getChildren().add(desde);
        form.getChildren().add(hasta);
        hasta.getAttributes().put("desde", "desde");
    }

    private void esperaError(Object valorHasta) {
        try {
            validador.validate(null, hasta, valorHasta);
            fail("Se esperaba ValidatorException para hasta=" + valorHasta);
        } catch (ValidatorException e) {
            assertEquals("La fecha final no puede ser anterior a la fecha de inicio.",
                    e.getFacesMessage().getSummary());
        }
    }

    @Test
    public void finPosteriorAlInicioEsValido() {
        desde.setSubmittedValue("2026-10-20");
        validador.validate(null, hasta, "2026-10-25");
    }

    @Test
    public void finIgualAlInicioEsValido() {
        desde.setSubmittedValue("2026-10-20");
        validador.validate(null, hasta, "2026-10-20");
    }

    @Test
    public void finAnteriorAlInicioSeRechaza() {
        desde.setSubmittedValue("2026-10-20");
        esperaError("2026-10-19");
    }

    @Test
    public void usaElValorDelBeanSiDesdeNoSeEnvio() {
        // Petición AJAX solo de "hasta": "desde" no trae submittedValue
        desde.setValue("2026-10-20");
        esperaError("2026-10-10");
    }

    @Test
    public void elValorEnviadoTienePrioridadSobreElDelBean() {
        desde.setValue("2026-10-01");
        desde.setSubmittedValue("2026-10-20");
        esperaError("2026-10-10");
    }

    @Test
    public void sinFinNoValida() {
        desde.setSubmittedValue("2026-10-20");
        validador.validate(null, hasta, null);
        validador.validate(null, hasta, "");
    }

    @Test
    public void sinInicioNoValida() {
        validador.validate(null, hasta, "2026-10-10");
    }

    @Test
    public void formatosInvalidosSeIgnoran() {
        // Del formato se encarga FechaValidator; este solo compara rangos
        desde.setSubmittedValue("no-es-fecha");
        validador.validate(null, hasta, "2026-10-10");
        desde.setSubmittedValue("2026-10-20");
        validador.validate(null, hasta, "tampoco");
    }

    @Test
    public void sinAtributoDesdeNoValida() {
        hasta.getAttributes().remove("desde");
        desde.setSubmittedValue("2026-10-20");
        validador.validate(null, hasta, "2026-10-10");
    }

    @Test
    public void idDeCampoInexistenteNoValida() {
        hasta.getAttributes().put("desde", "noExiste");
        validador.validate(null, hasta, "2026-10-10");
    }
}