-- =============================================
-- STORED PROCEDURES
-- =============================================

DELIMITER $$

-- Ver todos los experimentos
CREATE PROCEDURE SP_VerExperimentos()
BEGIN
    SELECT 
        e.IdExperimento,
        e.Nombre,
        e.Descripcion,
        e.FechaInicio,
        e.FechaFinal,
        e.Estado,
        CONCAT(c.Nombre, ' ', c.Apellido) AS Responsable
    FROM Experimento e
    JOIN Cientifico c ON e.IdResponsable = c.IdCientifico;
END$$

-- Ver todos los resultados de un experimento
CREATE PROCEDURE SP_VerResultadosPorExperimento(IN p_IdExperimento INT)
BEGIN
    SELECT 
        r.IdResultado,
        r.Fecha,
        r.Descripcion,
        r.Prueba,
        p.TipoDePrueba,
        e.Nombre AS Experimento
    FROM Resultado r
    JOIN Prueba p      ON r.IdPrueba      = p.IdPrueba
    JOIN Experimento e ON r.IdExperimento = e.IdExperimento
    WHERE r.IdExperimento = p_IdExperimento;
END$$

-- Ver todos los científicos
CREATE PROCEDURE SP_VerCientificos()
BEGIN
    SELECT 
        IdCientifico,
        Nombre,
        Apellido,
        Nacimiento
    FROM Cientifico
    ORDER BY Apellido, Nombre;
END$$

-- Borrar científico por ID
CREATE PROCEDURE SP_BorrarCientifico(IN p_IdCientifico INT)
BEGIN
    DELETE FROM Cientifico
    WHERE IdCientifico = p_IdCientifico;
    -- Si tiene filas en Realiza o es responsable de algún
    -- experimento, el RESTRICT de las FK va a rechazar
    -- el delete automáticamente con un error.
END$$

-- Borrar experimento por ID
CREATE PROCEDURE SP_BorrarExperimento(IN p_IdExperimento INT)
BEGIN
    DELETE FROM Experimento
    WHERE IdExperimento = p_IdExperimento;
    -- Las filas de Realiza relacionadas se borran solas por CASCADE.
END$$

-- Actualizar científico (todos los datos excepto ID)
CREATE PROCEDURE SP_ActualizarCientifico(
    IN p_IdCientifico INT,
    IN p_Nombre       VARCHAR(100),
    IN p_Apellido     VARCHAR(100),
    IN p_Nacimiento   DATE
)
BEGIN
    UPDATE Cientifico
    SET 
        Nombre     = p_Nombre,
        Apellido   = p_Apellido,
        Nacimiento = p_Nacimiento
    WHERE IdCientifico = p_IdCientifico;
END$$

-- Actualizar experimento (todos los datos excepto ID)
CREATE PROCEDURE SP_ActualizarExperimento(
    IN p_IdExperimento INT,
    IN p_FechaInicio   DATE,
    IN p_FechaFinal    DATE,
    IN p_Nombre        VARCHAR(200),
    IN p_Descripcion   TEXT,
    IN p_Estado        ENUM('Sin comenzar','En proceso','Exitoso','Fallido'),
    IN p_IdResponsable INT
)
BEGIN
    UPDATE Experimento
    SET
        FechaInicio   = p_FechaInicio,
        FechaFinal    = p_FechaFinal,
        Nombre        = p_Nombre,
        Descripcion   = p_Descripcion,
        Estado        = p_Estado,
        IdResponsable = p_IdResponsable
    WHERE IdExperimento = p_IdExperimento;
END$$

-- Verificar contraseña admin
CREATE PROCEDURE SP_VerificarContraseña(OUT p_Contraseña VARCHAR(255))
BEGIN
    SELECT Contraseña INTO p_Contraseña
    FROM ContraseñaAdministrador
    LIMIT 1;
END$$

-- Agregar experimento
CREATE PROCEDURE SP_AgregarExperimento(
    IN p_FechaInicio   DATE,
    IN p_FechaFinal    DATE,
    IN p_Nombre        VARCHAR(200),
    IN p_Descripcion   TEXT,
    IN p_Estado        ENUM('Sin comenzar','En proceso','Exitoso','Fallido'),
    IN p_IdResponsable INT
)
BEGIN
    INSERT INTO Experimento (FechaInicio, FechaFinal, Nombre, Descripcion, Estado, IdResponsable)
    VALUES (p_FechaInicio, p_FechaFinal, p_Nombre, p_Descripcion, p_Estado, p_IdResponsable);

    -- Devuelve el ID generado para que el backend lo pueda usar
    SELECT LAST_INSERT_ID() AS IdExperimento;
END$$

-- Agregar científico
CREATE PROCEDURE SP_AgregarCientifico(
    IN p_Nombre     VARCHAR(100),
    IN p_Apellido   VARCHAR(100),
    IN p_Nacimiento DATE
)
BEGIN
    INSERT INTO Cientifico (Nombre, Apellido, Nacimiento)
    VALUES (p_Nombre, p_Apellido, p_Nacimiento);

    SELECT LAST_INSERT_ID() AS IdCientifico;
END$$

DELIMITER ;


-- =============================================
-- EJEMPLOS DE USO
-- =============================================

CALL SP_VerExperimentos();
CALL SP_VerCientificos();
CALL SP_VerResultadosPorExperimento(1);
CALL SP_BorrarCientifico(10);
CALL SP_BorrarExperimento(2);
CALL SP_ActualizarCientifico(1, 'Lautaro', 'Pawlowicz', '1995-03-14');
CALL SP_ActualizarExperimento(1, '2026-01-10', '2026-06-30', 'Peptides on Rats', 'Descripcion nueva', 'En proceso', 1);
CALL SP_AgregarCientifico('Juan', 'Perez', '2000-01-01');
CALL SP_AgregarExperimento('2026-04-01', '2026-12-01', 'Nuevo Experimento', 'Descripcion', 'Sin comenzar', 1);

-- Para la contraseña se usa OUT, entonces se llama así:
CALL SP_VerificarContraseña(@hash);
SELECT @hash;


-- Agregar resultado parcial (RF02)
CREATE PROCEDURE SP_AgregarResultado(
    IN p_Fecha        DATE,
    IN p_Descripcion  TEXT,
    IN p_Prueba       VARCHAR(200),
    IN p_IdExperimento INT,
    IN p_IdPrueba     INT
)
BEGIN
    INSERT INTO Resultado (Fecha, Descripcion, Prueba, IdExperimento, IdPrueba)
    VALUES (p_Fecha, p_Descripcion, p_Prueba, p_IdExperimento, p_IdPrueba);

    SELECT LAST_INSERT_ID() AS IdResultado;
END$$

-- Asignar científico a experimento
CREATE PROCEDURE SP_AgregarRealiza(
    IN p_IdCientifico  INT,
    IN p_IdExperimento INT
)
BEGIN
    INSERT INTO Realiza (IdCientifico, IdExperimento)
    VALUES (p_IdCientifico, p_IdExperimento);
END$$

-- Desasignar científico de experimento
CREATE PROCEDURE SP_QuitarRealiza(
    IN p_IdCientifico  INT,
    IN p_IdExperimento INT