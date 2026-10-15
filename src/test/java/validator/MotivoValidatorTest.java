package validator;

import com.permisos.validator.MotivoValidator;
import org.junit.Test;

import javax.faces.component.UIInput;
import javax.faces.validator.ValidatorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class MotivoValidatorTest {

    private final MotivoValidator validador = new MotivoValidator();

    private void esperaError(UIInput campo, Object valor, String mensajeEsperado) {
        try {
            validador.validate(null, campo, valor);
            fail("Se esperaba ValidatorException para: " + valor);
        } catch (ValidatorException e) {
            assertEquals(mensajeEsperado, e.getFacesMessage().getSummary());
        }
    }

    @Test
    public void motivoValidoNoLanzaError() {
        validador.validate(null, new UIInput(), "Cita médica familiar");
    }

    @Test
    public void nuloSeRechazaComoVacio() {
        esperaError(new UIInput(), null, "Debe indicar el motivo.");
    }

    @Test
    public void soloEspaciosSeRechazaComoVacio() {
        esperaError(new UIInput(), "     ", "Debe indicar el motivo.");
    }

    @Test
    public void muyCortoPorDebajoDelMinimoPorDefecto() {
        esperaError(new UIInput(), "abc", "El motivo es muy corto");
    }

    @Test
    public void exactamenteElMinimoEsValido() {
        validador.validate(null, new UIInput(), "12345");
    }

    @Test
    public void losEspaciosDeLosExtremosNoCuentan() {
        // "  abc  " tiene 3 caracteres útiles, menos que el mínimo (5)
        esperaError(new UIInput(), "  abc  ", "El motivo es muy corto");
    }

    @Test
    public void exactamenteElMaximoPorDefectoEsValido() {
        validador.validate(null, new UIInput(), repetir('a', 250));
    }

    @Test
    public void masDelMaximoPorDefectoSeRechaza() {
        esperaError(new UIInput(), repetir('a', 251), "El motivo es muy largo: máximo 250");
    }

    @Test
    public void respetaMinYMaxPersonalizados() {
        UIInput campo = new UIInput();
        campo.getAttributes().put("min", "5");
        campo.getAttributes().put("max", "500");
        // 300 caracteres: inválido con el máximo por defecto (250), válido con 500
        validador.validate(null, campo, repetir('a', 300));
        esperaError(campo, repetir('a', 501), "El motivo es muy largo: máximo 500");
    }

    @Test
    public void usaLaEtiquetaDelComponenteEnElMensaje() {
        UIInput campo = new UIInput();
        campo.getAttributes().put("etiqueta", "motivo del rechazo");
        esperaError(campo, "", "Debe indicar el motivo del rechazo.");
        esperaError(campo, "ab", "El motivo del rechazo es muy corto");
    }

    @Test
    public void atributoNumericoInvalidoVuelveAlValorPorDefecto() {
        UIInput campo = new UIInput();
        campo.getAttributes().put("min", "no-es-numero");
        esperaError(campo, "abc", "El motivo es muy corto");
    }

    private static String repetir(char c, int veces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}