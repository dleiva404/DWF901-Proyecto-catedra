package com.rrhh.api.dto;

public class RestablecerContrasenaDTO {

    private String token;
    private String nueva;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }
}
