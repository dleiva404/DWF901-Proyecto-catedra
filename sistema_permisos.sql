DROP DATABASE IF EXISTS sistema_permisos;

CREATE DATABASE sistema_permisos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_permisos;

CREATE TABLE sucursales_areas (
    id_sucursal_area INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE departamentos (
    id_departamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE empleados (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dui VARCHAR(10) UNIQUE,
    correo VARCHAR(150) UNIQUE,
    telefono VARCHAR(20),
    id_sucursal_area INT NOT NULL,
    empresa VARCHAR(100) NULL,
    id_departamento INT NOT NULL,
    cargo VARCHAR(100),
    fecha_ingreso DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_sucursal_area) REFERENCES sucursales_areas(id_sucursal_area),
    FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento)
);

CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    id_empleado INT NOT NULL,
    id_rol INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME NULL,
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

CREATE TABLE jefaturas (
    id_jefatura INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    id_departamento INT,
    id_sucursal_area INT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento),
    FOREIGN KEY (id_sucursal_area) REFERENCES sucursales_areas(id_sucursal_area)
);

CREATE TABLE tipos_solicitud (
    id_tipo_solicitud INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE solicitudes (
    id_solicitud INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    id_tipo_solicitud INT NOT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    dias_solicitados INT NOT NULL,
    motivo TEXT NOT NULL,
    estado ENUM('PENDIENTE', 'APROBADA', 'RECHAZADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo TEXT,
    fecha_respuesta DATETIME NULL,
    id_jefatura_respuesta INT NULL,
    observaciones_rrhh TEXT,
    fecha_recepcion_rrhh DATETIME NULL,
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    FOREIGN KEY (id_tipo_solicitud) REFERENCES tipos_solicitud(id_tipo_solicitud),
    FOREIGN KEY (id_jefatura_respuesta) REFERENCES empleados(id_empleado),
    CHECK (dias_solicitados > 0),
    CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE vacaciones_empleado (
    id_vacaciones INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    anio YEAR NOT NULL,
    dias_asignados INT NOT NULL DEFAULT 15,
    dias_utilizados INT NOT NULL DEFAULT 0,
    dias_disponibles INT NOT NULL DEFAULT 15,
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    UNIQUE (id_empleado, anio),
    CHECK (dias_asignados >= 0),
    CHECK (dias_utilizados >= 0),
    CHECK (dias_disponibles >= 0)
);

CREATE TABLE incapacidades (
    id_incapacidad INT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud INT NOT NULL UNIQUE,
    numero_documento VARCHAR(100),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    documento_nombre VARCHAR(255),
    documento_ruta VARCHAR(500),
    observaciones TEXT,
    FOREIGN KEY (id_solicitud) REFERENCES solicitudes(id_solicitud),
    CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE historial_solicitud (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_solicitud INT NOT NULL,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    comentario TEXT,
    id_usuario INT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_solicitud) REFERENCES solicitudes(id_solicitud),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE constancias (
    id_constancia INT AUTO_INCREMENT PRIMARY KEY,
    id_empleado INT NOT NULL,
    tipo VARCHAR(50) NULL,
    institucion_destino VARCHAR(150) NOT NULL,
    motivo TEXT,
    salario_referencia DECIMAL(10,2) NOT NULL,
    empresa_emisora VARCHAR(100) NOT NULL,
    token_verificacion VARCHAR(50) NOT NULL UNIQUE,
    estado VARCHAR(20) NULL,
    fecha_solicitud TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
);

INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMIN', 'Administrador del sistema'),
    ('EMPLEADO', 'Empleado que puede realizar solicitudes'),
    ('JEFATURA', 'Jefatura encargada de aprobar o rechazar solicitudes'),
    ('RRHH', 'Personal de Recursos Humanos');

INSERT INTO tipos_solicitud (nombre, descripcion) VALUES
    ('VACACIONES', 'Solicitud de días de vacaciones'),
    ('INCAPACIDAD', 'Solicitud por incapacidad médica'),
    ('AUSENCIA', 'Solicitud de ausencia laboral');

INSERT INTO sucursales_areas (nombre, descripcion) VALUES
    ('Didelco Santa Ana', 'Sucursal Santa Ana'),
    ('Didelco Apopa', 'Sucursal Apopa'),
    ('Didelco Metapán', 'Sucursal Metapán');

INSERT INTO departamentos (nombre, descripcion) VALUES
    ('Recursos Humanos', 'Departamento de Recursos Humanos'),
    ('Administración', 'Departamento Administrativo'),
    ('Ventas', 'Departamento de Ventas'),
    ('Informática', 'Departamento de Informática');

INSERT INTO empleados
    (nombre, apellido, dui, correo, telefono, id_sucursal_area, empresa, id_departamento, cargo, fecha_ingreso)
VALUES
    ('Juan', 'Pérez', '01234567-8', 'juan.perez@empresa.com', '70000000', 1, NULL, 4, 'Analista de Sistemas', '2024-01-15'),
    ('Carlos', 'Gómez', '12345678-9', 'carlos.gomez@empresa.com', '71111111', 1, NULL, 4, 'Jefe de Informática', '2020-03-10'),
    ('Ana', 'Martínez', '23456789-0', 'ana.martinez@empresa.com', '72222222', 1, NULL, 1, 'Analista de Recursos Humanos', '2021-06-01'),
    ('Carlos', 'Cornejo', '34567890-1', 'carlos.cornejo@empresa.com', '73333333', 1, NULL, 4, 'Jefe de Informática', '2021-02-01'),
    ('David', 'Leiva', '45678901-2', 'david.leiva@empresa.com', '74444444', 1, NULL, 4, 'Analista de Sistemas', '2024-02-01'),
    ('Moisés', 'García', '56789012-3', 'moises.garcia@empresa.com', '75555555', 1, NULL, 4, 'Analista de Sistemas', '2024-02-01'),
    ('Alcyr', 'Figueroa', '67890123-4', 'alcyr.figueroa@empresa.com', '76666666', 1, NULL, 4, 'Analista de Sistemas', '2024-02-01'),
    ('Nelson', 'Solano', '78901234-5', 'nelson.solano@empresa.com', '77777777', 1, NULL, 4, 'Analista de Sistemas', '2024-02-01');

INSERT INTO jefaturas (id_empleado, id_departamento, id_sucursal_area) VALUES
    (2, 4, 1),
    (4, 4, 1);

INSERT INTO usuarios (username, password, id_empleado, id_rol) VALUES
    ('juan', '123456', 1, 2),
    ('carlos', '123456', 2, 3),
    ('ana', '123456', 3, 4),
    ('carlos.cornejo', '123456', 4, 3),
    ('david.leiva', '123456', 5, 2),
    ('moises.garcia', '123456', 6, 2),
    ('alcyr.figueroa', '123456', 7, 2),
    ('nelson.solano', '123456', 8, 2);

INSERT INTO vacaciones_empleado (id_empleado, anio, dias_asignados, dias_utilizados, dias_disponibles) VALUES
    (1, YEAR(CURDATE()), 15, 0, 15),
    (4, YEAR(CURDATE()), 15, 0, 15),
    (5, YEAR(CURDATE()), 15, 0, 15),
    (6, YEAR(CURDATE()), 15, 0, 15),
    (7, YEAR(CURDATE()), 15, 0, 15),
    (8, YEAR(CURDATE()), 15, 0, 15);
