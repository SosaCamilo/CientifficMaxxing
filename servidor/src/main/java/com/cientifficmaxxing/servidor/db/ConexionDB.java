package com.cientifficmaxxing.servidor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones JDBC a MySQL.
 *
 * DISEÑO INTENCIONAL — Una conexión por cliente:
 * Esta clase NO usa pool de conexiones. Cada llamada a obtener() abre
 * una conexión nueva. ManejadorCliente llama a obtener() una sola vez
 * (al inicio de la sesión del cliente) y la cierra con try-with-resources
 * al terminar. Así cada sesión tiene su propio canal aislado a MySQL.
 *
 * Parámetros de la URL:
 *   useSSL=false               → deshabilita SSL (desarrollo local)
 *   serverTimezone=UTC         → evita errores de zona horaria en JDK moderno
 *   allowPublicKeyRetrieval=true → necesario para autenticación caching_sha2_password
 *                                  en MySQL 8+
 */
public class ConexionDB {

    private static final String URL =
        "jdbc:mysql://localhost:3306/cientifficmaxxing" +
        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    // *** IMPORTANTE: si tu MySQL tiene contraseña, cambialá acá ***
    private static final String USUARIO    = "adminis";
    private static final String CONTRASENIA = "12341234";

    private ConexionDB() {}

    /**
     * Devuelve una conexión nueva a MySQL.
     * Cada sesión de cliente debe llamar a este método una vez y cerrar
     * la conexión cuando termina (try-with-resources en ManejadorCliente).
     *
     * @throws SQLException si MySQL no está disponible o la contraseña es incorrecta.
     */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENIA);
    }
}
