package com.permisos.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("motivoValidator")
public class MotivoValidator implements Validator {

    private static final int MIN_POR_DEFECTO = 5;
    private static final int MAX_POR_DEFECTO = 250;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        String etiqueta = texto(component, "etiqueta", "motivo");
        int min = numero(component, "min", MIN_POR_DEFECTO);
        int max = numero(component, "max", MAX_POR_DEFECTO);

        String motivo = value == null ? "" : value.toString().trim();

        if (motivo.isEmpty()) {
            throw error("Debe indicar el " + etiqueta + ".");
        }
        if (motivo.length() < min) {
            throw error("El " + etiqueta + " es muy corto");
        }
        if (motivo.length() > max) {
            throw error("El " + etiqueta + " es muy largo: máximo " + max);
        }
    }

    private ValidatorException error(String mensaje) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    private String texto(UIComponent component, String nombre, String porDefecto) {
        Object v = component.getAttributes().get(nombre);
        return v == null ? porDefecto : v.toString();
    }

    private int numero(UIComponent component, String nombre, int porDefecto) {
        Object v = component.getAttributes().get(nombre);
        if (v == null) {
            return porDefecto;
        }
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}