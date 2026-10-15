package validator;

import com.permisos.validator.FechaValidator;
import org.junit.Test;

import javax.faces.component.UIInput;
import javax.faces.validator.ValidatorException;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class FechaValidatorTest {

    private final FechaValidator validador = new FechaValidator();

    private void esperaError(UIInput campo, Object valor, String mensajeEsperado) {
        try {
            validador.validate(null, campo, valor);
            fail("Se esperaba ValidatorException para: " + valor);
        } catch (ValidatorException e) {
            assertEquals(mensajeEsperado, e.getFacesMessage().getSummary());
        }
    }

    @Test
    public void vacioONuloSeIgnora() {
        // La obligatoriedad la maneja required="true", no este validador
        validador.validate(null, new UIInput(), null);
        validador.validate(null, new UIInput(), "");
        validador.validate(null, new UIInput(), "   ");
    }

    @Test
    public void fechaDeHoyEsValida() {
        validador.validate(null, new UIInput(), LocalDate.now().toString());
    }

    @Test
    public void fechaFuturaEsValida() {
        validador.validate(null, new UIInput(), LocalDate.now().plusDays(30).toString());
    }

    @Test
    public void fechaPasadaSeRechaza() {
        esperaError(new UIInput(), LocalDate.now().minusDays(1).toString(),
                "No se pueden solicitar fechas pasadas.");
    }

    @Test
    public void fechaPasadaSePermiteConAtributo() {
        UIInput campo = new UIInput();
        campo.getAttributes().put("permitirPasado", "true");
        validador.validate(null, campo, LocalDate.now().minusDays(10).toString());
    }

    @Test
    public void formatoInvalidoSeRechaza() {
        esperaError(new UIInput(), "15/10/2026", "La fecha no tiene un formato válido.");
        esperaError(new UIInput(), "hola", "La fecha no tiene un formato válido.");
    }

    @Test
    public void fechaInexistenteSeRechaza() {
        // 30 de febrero no existe
        esperaError(new UIInput(), "2027-02-30", "La fecha no tiene un formato válido.");
    }

    @Test
    public void ignoraEspaciosAlrededor() {
        validador.validate(null, new UIInput(), "  " + LocalDate.now().plusDays(1) + "  ");
    }
}