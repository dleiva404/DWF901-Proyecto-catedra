USE sistema_permisos;

-- Empresas de ejemplo para ver un logo distinto en cada constancia.
-- Se puede correr varias veces: solo actualiza la columna empresa.
UPDATE empleados SET empresa = NULL         WHERE id_empleado = 1;  -- Juan: Didelco
UPDATE empleados SET empresa = 'Invercalma' WHERE id_empleado = 2;  -- Carlos G.: logo GC
UPDATE empleados SET empresa = 'Propultran' WHERE id_empleado = 3;  -- Ana
UPDATE empleados SET empresa = 'Copro'      WHERE id_empleado = 5;  -- David
UPDATE empleados SET empresa = 'Steel'      WHERE id_empleado = 6;  -- Moises
UPDATE empleados SET empresa = 'DCA'        WHERE id_empleado = 7;  -- Alcyr
UPDATE empleados SET empresa = 'Elf'        WHERE id_empleado = 8;  -- Nelson
