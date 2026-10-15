package com.permisos.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


@FacesValidator("fechaValidator")
public class FechaValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(value.toString().trim());
        } catch (DateTimeParseException e) {
            throw error("La fecha no tiene un formato válido.");
        }

        boolean permitirPasado = "true".equalsIgnoreCase(
                String.valueOf(component.getAttributes().get("permitirPasado")));

        if (!permitirPasado && fecha.isBefore(LocalDate.now())) {
            throw error("No se pueden solicitar fechas pasadas.");
        }
    }

    private ValidatorException error(String mensaje) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }
}