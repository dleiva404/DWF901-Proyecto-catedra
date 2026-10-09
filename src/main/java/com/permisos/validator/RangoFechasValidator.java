package com.permisos.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


@FacesValidator("rangoFechasValidator")
public class RangoFechasValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        LocalDate fin = parsear(value);
        if (fin == null) {
            return;
        }

        Object idDesde = component.getAttributes().get("desde");
        if (idDesde == null) {
            return;
        }

        UIComponent otro = component.findComponent(idDesde.toString());
        if (!(otro instanceof UIInput)) {
            return;
        }

        UIInput campoDesde = (UIInput) otro;
        // Si "desde" ya se validó en esta petición, su valor está en getValue();
        // si no se procesó (petición AJAX solo de "hasta"), se usa el del bean.
        Object valorDesde = campoDesde.getSubmittedValue();
        if (valorDesde == null) {
            valorDesde = campoDesde.getValue();
        }

        LocalDate inicio = parsear(valorDesde);
        if (inicio == null) {
            return;
        }

        if (fin.isBefore(inicio)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "La fecha final no puede ser anterior a la fecha de inicio.", null));
        }
    }

    private LocalDate parsear(Object valor) {
        if (valor == null || valor.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.toString().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}