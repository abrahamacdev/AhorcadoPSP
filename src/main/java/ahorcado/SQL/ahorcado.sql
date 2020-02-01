
DROP DATABASE IF EXISTS ahorcado;

CREATE DATABASE IF NOT EXISTS ahorcado;

USE ahorcado;

CREATE TABLE usuario(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    contrasenia VARCHAR(255) NOT NULL,
    idRol INT NOT NULL
);

CREATE TABLE rol(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL
);

INSERT INTO rol (id, nombre) VALUES (1,"admin"), (2,"normal");

ALTER TABLE usuario ADD CONSTRAINT us_nombre_uk UNIQUE usuario(nombre);
ALTER TABLE usuario ADD CONSTRAINT us_idRol_fk FOREIGN KEY usuario(idRol) REFERENCES rol(id);