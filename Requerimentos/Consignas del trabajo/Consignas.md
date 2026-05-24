Actividad 02: “Conectar No Es Comunicar: Protocolos o Caos”

CONSIGNA GENERAL
En este proyecto deberán diseñar e implementar un sistema Cliente-Servidor que:
● Se comunique mediante sockets
● Intercambie información con un protocolo propio
● Registre eventos en una base de datos MySQL
El objetivo no es solo conectar, sino lograr una comunicación ordenada, válida y verificable.

PROBLEMÁTICA
El sistema deberá recibir solicitudes, procesarlas correctamente y registrar los resultados.
El foco está en la consistencia:
● Lo que llega debe poder interpretarse
● Lo que se procesa debe ser válido
● Lo que se registra debe poder consultarse

REQUERIMIENTOS TÉCNICOS
Comunicación:
● Uso de sockets TCP
● Implementación del modelo cliente-servidor
● Establecimiento, intercambio y cierre correcto de la conexión
● Manejo de errores básicos (servidor no disponible, conexión interrumpida)
● Envío y recepción de mensajes sin interrupciones del sistema.
Protocolo de la Aplicación:
● Definición de estructura de mensajes
● Delimitación clara
● Identificación de operaciones
● Validación de contenido
Persistencia (MySQL):
● Solo el servidor accede a la base de datos.
● Registro de eventos mediante stored procedures.
● Gestión básica de excepciones y concurrencia.
● Registro de conexiones, solicitudes y resultados (log del sistema).
Robustez:
● Validación de datos de entrada y manejo de errores sin interrumpir el sistema.
● Respuestas claras al cliente.

Actividad del Plan de Aprendizaje - diseñada y desarrollada por Pablo Andrés Linares - PSR - 6to 1ra/2da CSC - Ciclo Lectivo 2026

Plan de Aprendizaje Bimestral: Conectar No Es Comunicar: Arquitectura para que No Se Hagan los Sordos

FORMA DE TRABAJO
● Equipos de 3 a 4 integrantes.
● Desafío asignado por sorteo.
● Organización en etapas: Diseño, Prototipo, Construcción y Pruebas.
● Uso obligatorio de tablero Kanban.
Se evaluará:
● Sistema funcional, con interfaces gráficas.
● Persistencia correcta de datos.
● Consistencia entre diseño y código.
● Defensa técnica del sistema.
● Manejo de errores y validación de datos.

ESPECIFICACIONES DEL SISTEMA
Arquitectura:
● Cliente-Servidor estricto.
● Uso de sockets en Java.
● Gestión de conexión y excepciones.
Base de Datos:
● MySQL con stored procedures que cuenten con manejo de excepciones y transaccionalidad.
● Acceso exclusivo desde el servidor.
● Seguridad básica (no datos sensibles en texto plano).
Diseño:
● Diagramas UML (Casos de Uso, Secuencia y Clases).
● Modelo Conceptual y Lógico de base de datos (Peter Chen / Edgard Codd).
Calidad
● Validación de entrada.
● Logs completos.
● Código claro y ordenado.