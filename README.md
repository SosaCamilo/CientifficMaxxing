# CientifficMaxxing

Aplicación de escritorio cliente-servidor para gestionar experimentos de laboratorio. Desarrollada como trabajo práctico de Programación sobre Redes.

## ¿Qué hace?

Permite registrar experimentos científicos, los científicos que participan en ellos y los resultados obtenidos. Tiene un modo administrador para gestionar datos y un modo consulta para ver el estado de los experimentos.

## Tecnologías

- Java con Swing para la interfaz gráfica
- Comunicación cliente-servidor por sockets TCP
- Persistencia en archivos CSV
- Concurrencia manejada con semáforos y threads
- Maven como gestor de dependencias

## Requisitos

- Java JDK 11 o superior
- Apache Maven 3.6 o superior

## Cómo ejecutarlo

Primero el servidor, en una terminal:

mvn compile exec:java -pl servidor


Luego el cliente, en otra terminal:

mvn compile exec:java -pl cliente


La contraseña de administrador por defecto es `admin1234`.

## Estructura


CientifficMaxxing/
├── servidor/   ← lógica de negocio, manejo de archivos, concurrencia
├── cliente/    ← interfaz gráfica Swing
└── resources/  ← archivos CSV con los datos
