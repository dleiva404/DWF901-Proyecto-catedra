-- =========================================================
-- SISTEMA DE SOLICITUD DE PERMISOS
-- Base de datos MySQL
-- Arquitectura prevista: MVC
-- =========================================================

DROP DATABASE IF EXISTS sistema_permisos;

CREATE DATABASE sistema_permisos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_permisos;

-- =========================================================
-- 1. SUCURSALES / ÁREAS
-- =========================================================
CREATE TABLE sucursales_areas (
    id_sucursal_area INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 2. DEPARTAMENTOS
-- =========================================================
CREATE TABLE departamentos (
    id_departamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 3. EMPLEADOS
-- =========================================================
CREATE TABLE empleados (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dui VARCHAR(10) UNIQUE,
    correo VARCHAR(150) UNIQUE,
    telefono VARCHAR(20),
    id_sucursal_area INT NOT NULL,
    id_departamento INT NOT NULL,
    cargo VARCHAR(100),
    fecha_ingreso DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_sucursal_area) REFERENCES sucursales_areas(id_sucursal_area),
    FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento)
);

-- =========================================================
-- 4. ROLES
-- =========================================================
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 5. USUARIOS
-- =========================================================
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

-- =========================================================
-- 6. JEFATURAS
-- =========================================================
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

-- =========================================================
-- 7. TIPOS DE SOLICITUD
-- =========================================================
CREATE TABLE tipos_solicitud (
    id_tipo_solicitud INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 8. SOLICITUDES
-- =========================================================
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

-- =========================================================
-- 9. VACACIONES DE EMPLEADOS
-- =========================================================
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

-- =========================================================
-- 10. INCAPACIDADES
-- =========================================================
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

-- =========================================================
-- 11. HISTORIAL DE SOLICITUDES
-- =========================================================
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

-- =========================================================
-- DATOS INICIALES
-- =========================================================

-- ROLES
INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMIN', 'Administrador del sistema'),
    ('EMPLEADO', 'Empleado que puede realizar solicitudes'),
    ('JEFATURA', 'Jefatura encargada de aprobar o rechazar solicitudes'),
    ('RRHH', 'Personal de Recursos Humanos');

-- TIPOS DE SOLICITUD
INSERT INTO tipos_solicitud (nombre, descripcion) VALUES
    ('VACACIONES', 'Solicitud de días de vacaciones'),
    ('INCAPACIDAD', 'Solicitud por incapacidad médica'),
    ('AUSENCIA', 'Solicitud de ausencia laboral');

-- SUCURSALES / ÁREAS
INSERT INTO sucursales_areas (nombre, descripcion) VALUES
    ('Sucursal Central', 'Oficina principal'),
    ('Sucursal San Salvador', 'Sucursal de San Salvador'),
    ('Sucursal Santa Ana', 'Sucursal de Santa Ana');

-- DEPARTAMENTOS
INSERT INTO departamentos (nombre, descripcion) VALUES
    ('Recursos Humanos', 'Departamento de Recursos Humanos'),
    ('Administración', 'Departamento Administrativo'),
    ('Ventas', 'Departamento de Ventas'),
    ('Informática', 'Departamento de Informática');

-- EMPLEADOS DE PRUEBA
INSERT INTO empleados
    (nombre, apellido, dui, correo, telefono, id_sucursal_area, id_departamento, cargo, fecha_ingreso)
VALUES
    ('Juan', 'Pérez', '01234567-8', 'juan.perez@empresa.com', '70000000', 1, 4, 'Analista de Sistemas', '2024-01-15'),
    ('Carlos', 'Gómez', '12345678-9', 'carlos.gomez@empresa.com', '71111111', 1, 4, 'Jefe de Informática', '2020-03-10'),
    ('Ana', 'Martínez', '23456789-0', 'ana.martinez@empresa.com', '72222222', 1, 1, 'Analista de Recursos Humanos', '2021-06-01');

-- JEFATURA (Carlos será jefe de Informática)
INSERT INTO jefaturas (id_empleado, id_departamento, id_sucursal_area) VALUES
    (2, 4, 1);

-- USUARIOS DE PRUEBA
INSERT INTO usuarios (username, password, id_empleado, id_rol) VALUES
    ('juan', '123456', 1, 2),
    ('carlos', '123456', 2, 3),
    ('ana', '123456', 3, 4);

-- VACACIONES DEL AÑO ACTUAL
INSERT INTO vacaciones_empleado (id_empleado, anio, dias_asignados, dias_utilizados, dias_disponibles) VALUES
    (1, YEAR(CURDATE()), 15, 0, 15);
