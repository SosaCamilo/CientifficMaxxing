DROP PROCEDURE IF EXISTS SP_VerExperimentos;

DELIMITER $$

CREATE PROCEDURE SP_VerExperimentos()
BEGIN
    SELECT
        e.IdExperimento,
        e.Nombre,
        e.Descripcion,
        e.FechaInicio,
        e.FechaFinal,
        e.Estado,
        CONCAT(c.Nombre, ' ', c.Apellido) AS Responsable,
        e.IdResponsable,
        IFNULL(
            GROUP_CONCAT(
                CONCAT(ce.Nombre, ' ', ce.Apellido)
                ORDER BY ce.Apellido, ce.Nombre
                SEPARATOR ', '
            ),
        '') AS Equipo
    FROM Experimento e
    JOIN Cientifico c  ON e.IdResponsable  = c.IdCientifico
    LEFT JOIN Realiza r     ON r.IdExperimento  = e.IdExperimento
    LEFT JOIN Cientifico ce ON ce.IdCientifico  = r.IdCientifico
    GROUP BY e.IdExperimento, e.Nombre, e.Descripcion,
             e.FechaInicio, e.FechaFinal, e.Estado,
             e.IdResponsable, c.Nombre, c.Apellido;
END$$

DELIMITER ;
