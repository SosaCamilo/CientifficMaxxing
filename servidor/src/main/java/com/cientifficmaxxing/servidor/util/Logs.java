package com.cientifficmaxxing.servidor.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Sistema de logs del servidor.
 *
 * Registra todos los eventos en dos destinos simultáneamente:
 *   1. Consola (stdout) — visible en la terminal mientras el servidor corre.
 *   2. Archivo "servidor.log" — persiste entre reinicios del servidor (modo append).
 *
 * Niveles usados:
 *   info()       → INFO  — conexiones, peticiones recibidas, respuestas enviadas
 *   advertencia()→ WARNING — situaciones no fatales (FK violation, duplicado)
 *   error()      → SEVERE  — errores que requieren atención (BD caída, bug, etc.)
 *
 * Formato de salida: [2026-04-24 10:30:00] [INFO   ] mensaje
 */
public class Logs {

    private static final Logger logger;

    static {
        // Formato: [2026-04-24 10:30:00] [INFO   ] mensaje
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT] [%4$-7s] %5$s%n"
        );

        logger = Logger.getLogger("CientifficMaxxing");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        ConsoleHandler consola = new ConsoleHandler();
        consola.setLevel(Level.ALL);
        consola.setFormatter(new SimpleFormatter());
        logger.addHandler(consola);

        try {
            FileHandler archivo = new FileHandler("servidor.log", true);
            archivo.setLevel(Level.ALL);
            archivo.setFormatter(new SimpleFormatter());
            logger.addHandler(archivo);
        } catch (IOException e) {
            logger.warning("No se pudo crear servidor.log: " + e.getMessage());
        }
    }

    private Logs() {}

    public static void info(String mensaje) {
        logger.info(mensaje);
    }

    public static void advertencia(String mensaje) {
        logger.warning(mensaje);
    }

    public static void error(String mensaje) {
        logger.severe(mensaje);
    }

    public static void error(String mensaje, Throwable causa) {
        logger.log(Level.SEVERE, mensaje, causa);
    }
}
