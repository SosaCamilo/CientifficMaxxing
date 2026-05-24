-- =============================================
-- INSERTS - DATOS INICIALES
-- =============================================

-- Científicos (fechas inventadas)
INSERT INTO Cientifico (Nombre, Apellido, Nacimiento) VALUES
    ('Lautaro',   'Pawlowicz',  '1995-03-14'),
    ('Camilo',    'Sosa',       '1998-07-22'),
    ('Gael',      'Lapeyre',    '2000-11-05'),
    ('Lucas',     'Caballer',   '1997-04-18'),
    ('Ilya',      'Ferenz',     '1999-09-30'),
    ('Bruno',     'Palomeque',  '2001-02-11'),
    ('Miguel',    'Ortiz',      '1996-06-25'),
    ('Benicio',   'Analiz',     '2002-08-08'),
    ('Clavicular','Tonete',     '1994-12-01'),
    ('Lautaro',   'Geli',       '2000-05-17');

-- Pruebas
INSERT INTO Prueba (TipoDePrueba) VALUES
    ('Hemograma completo'),
    ('Panel metabólico'),
    ('Electroencefalograma'),
    ('Prueba de cortisol'),
    ('Análisis de testosterona'),
    ('Prueba de resistencia física'),
    ('Escala de somnolencia de Epworth'),
    ('Polisomniografía'),
    ('Densitometría capilar'),
    ('Biopsia de folículo piloso');

-- Contraseña admin (hasheada con bcrypt - el texto plano es "admin1234")
INSERT INTO ContraseñaAdministrador (Contraseña) VALUES ('admin1234');

-- Experimentos
INSERT INTO Experimento (FechaInicio, FechaFinal, Nombre, Descripcion, Estado, IdResponsable) VALUES
    ('2026-01-10', '2026-06-30',
     'Peptides on Rats',
     'Estudio del efecto de péptidos sintéticos sobre el metabolismo y comportamiento en ratas de laboratorio.',
     'En proceso', 1),

    ('2026-02-01', '2026-05-31',
     'Sleep Quality on ASU FRAT',
     'Análisis de la calidad del sueño en estudiantes universitarios de fraternidades bajo distintas condiciones de descanso.',
     'En proceso', 2),

    ('2026-03-15', '2026-09-15',
     'Androgenic hair behaviour',
     'Investigación sobre el comportamiento del cabello bajo influencia de andrógenos en distintos grupos etarios.',
     'Sin comenzar', 4);

-- Relaciones Realiza
INSERT INTO Realiza (IdCientifico, IdExperimento) VALUES
    (1, 1), (3, 1), (5, 1),   -- Peptides on Rats
    (2, 2), (6, 2), (7, 2),   -- Sleep Quality
    (4, 3), (8, 3), (9, 3);   -- Androgenic hair

-- Resultados parciales
INSERT INTO Resultado (Fecha, Descripcion, Prueba, IdExperimento, IdPrueba) VALUES
    ('2026-02-15',
     'Los péptidos mostraron reducción del 12% en glucosa en sangre tras 4 semanas.',
     'Reducción glucémica moderada observada en grupo experimental.',
     1, 2),

    ('2026-03-20',
     'Se registró aumento de cortisol en ratas sometidas a dosis altas.',
     'Respuesta de estrés elevada en dosis alta.',
     1, 4),

    ('2026-03-10',
     'El grupo con restricción de sueño mostró somnolencia diurna significativa.',
     'Escala de Epworth promedio: 14/24 en grupo privado de sueño.',
     2, 7);