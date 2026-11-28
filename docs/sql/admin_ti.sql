USE sistema_permisos;

-- carlos.cornejo pasa a ser el ADMIN TI (rol 1).
-- Se puede correr varias veces: solo actualiza su rol.
UPDATE usuarios SET id_rol = 1 WHERE username = 'carlos.cornejo';
