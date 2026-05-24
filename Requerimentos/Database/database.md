-- Tabla catálogo de tipos de prueba
CREATE TABLE Prueba (
    IdPrueba    INT PRIMARY KEY AUTO_INCREMENT,
    TipoDePrueba VARCHAR(100) NOT NULL
);

-- Datos iniciales del catálogo
INSERT INTO Prueba (TipoDePrueba) VALUES
    ('Hemograma'),
    ('Prueba de resistencia'),
    ('Análisis bioquímico');

-- Científicos
CREATE TABLE Cientifico (
    IdCientifico    INT PRIMARY KEY AUTO_INCREMENT,
    Nombre          VARCHAR(100) NOT NULL,
    Apellido        VARCHAR(100) NOT NULL,
    Nacimiento      DATE         NOT NULL
);

-- Tabla de contraseña del administrador (singleton)
CREATE TABLE ContraseñaAdministrador (
    IdContraseña    INT PRIMARY KEY AUTO_INCREMENT,
    Contraseña      VARCHAR(255) NOT NULL  -- guardar hasheada
);

-- Experimentos
CREATE TABLE Experimento (
    IdExperimento   INT PRIMARY KEY AUTO_INCREMENT,
    FechaInicio     DATE         NOT NULL,
    FechaFinal      DATE,
    Nombre          VARCHAR(200) NOT NULL,
    Descripcion     TEXT,
    Estado          ENUM('Sin comenzar','En proceso','Exitoso','Fallido')
                    NOT NULL DEFAULT 'Sin comenzar',
    IdResponsable   INT NOT NULL,
    FOREIGN KEY (IdResponsable) REFERENCES Cientifico(IdCientifico)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

-- Relación N:N entre Cientifico y Experimento
CREATE TABLE Realiza (
    IdCientifico    INT NOT NULL,
    IdExperimento   INT NOT NULL,
    PRIMARY KEY (IdCientifico, IdExperimento),
    FOREIGN KEY (IdCientifico) REFERENCES Cientifico(IdCientifico)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    FOREIGN KEY (IdExperimento) REFERENCES Experimento(IdExperimento)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

-- Resultados parciales (1 experimento → 0..N resultados)
CREATE TABLE Resultado (
    IdResultado     INT PRIMARY KEY AUTO_INCREMENT,
    Fecha           DATE         NOT NULL,
    Descripcion     TEXT,
    Prueba          VARCHAR(200),          -- texto libre del resultado
    IdExperimento   INT NOT NULL,
    IdPrueba        INT NOT NULL,          -- FK al catálogo
    FOREIGN KEY (IdExperimento) REFERENCES Experimento(IdExperimento)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    FOREIGN KEY (IdPrueba) REFERENCES Prueba(IdPrueba)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);