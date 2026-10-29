package com.rrhh.api.dto;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private long expiraEnSegundos;
    private UsuarioSesionDTO usuario;

    public LoginResponseDTO(String token, long expiraEnSegundos,
                            UsuarioSesionDTO usuario) {
        this.token = token;
        this.tipo = "Bearer";
        this.expiraEnSegundos = expiraEnSegundos;
        this.usuario = usuario;
    }

    public String getToken() { return token; }

    public String getTipo() { return tipo; }

    public long getExpiraEnSegundos() { return expiraEnSegundos; }

    public UsuarioSesionDTO getUsuario() { return usuario; }

    public static class UsuarioSesionDTO {

        private int id;
        private String username;
        private int empleadoId;
        private String nombre;
        private String rol;

        public UsuarioSesionDTO(int id, String username, int empleadoId,
                                String nombre, String rol) {
            this.id = id;
            this.username = username;
            this.empleadoId = empleadoId;
            this.nombre = nombre;
            this.rol = rol;
        }

        public int getId() { return id; }

        public String getUsername() { return username; }

        public int getEmpleadoId() { return empleadoId; }

        public String getNombre() { return nombre; }

        public String getRol() { return rol; }
    }
}