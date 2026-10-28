
package com.rrhh.api.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiError {

    private int codigo;
    private String mensaje;
    private String ruta;
    private String fechaHora;


    public ApiError(int codigo, String mensaje, String ruta) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.ruta = normalizarRuta(ruta);
        this.fechaHora = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );
    }

    private static String normalizarRuta(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            return "/api";
        }
        return ruta.startsWith("/api/") ? ruta
                : "/api/" + (ruta.startsWith("/") ? ruta.substring(1) : ruta);
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
