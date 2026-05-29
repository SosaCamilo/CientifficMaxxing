package com.cientifficmaxxing.servidor;

import com.cientifficmaxxing.servidor.dao.CientifficDAO;
//import com.cientifficmaxxing.servidor.db.ConexionDB;
import com.cientifficmaxxing.servidor.protocolo.ManejadorPeticiones;
import com.cientifficmaxxing.servidor.util.Logs;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Atiende a un cliente conectado durante toda su sesión.
 *
 * CICLO DE VIDA:
 *   1. start() es llamado directamente desde Servidor.java (ejecuta el metodo run() )
 *   2. Se abren BufferedReader / PrintWriter sobre el socket (charset UTF-8).
 *   3. Se abre UNA conexión JDBC para toda la sesión del cliente.
 *      (Proximamente la reemplazo toda con archivos)
 *   4. Se entra al bucle procesarMensajes() — bloquea leyendo líneas del socket.
 *   5. Cuando el cliente cierra la conexión, readLine() devuelve null y el bucle termina.
 *   6. El try-with-resources cierra automáticamente el socket y la conexión JDBC.
 *
 * MANEJO DE ERRORES:
 *   - Si MySQL no está disponible (SQLException en ConexionDB.obtener()), se informa
 *     al cliente y se cierra la sesión.
 *   - Si hay un error de I/O en el socket, se registra y termina la atención.
 *   - Si procesar() lanza RuntimeException (bug / parámetro inválido), ManejadorPeticiones
 *     la captura y devuelve un ERROR de validación.
 */
public class ManejadorCliente extends Thread {

    private final Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        String ip = socket.getInetAddress().getHostAddress();
        Logs.info("[" + ip + "] Cliente conectado");

        // try-with-resources: cierra el socket al salir del bloque (ya sea por error o normalmente)
        try (socket) {
            // PrintWriter con autoFlush=true: cada println() envía los datos inmediatamente
            PrintWriter salida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            
            
            /*
            // Una conexión JDBC por cliente — si falla, se avisa y se cierra la sesión
            try (Connection conexion = ConexionDB.obtener()) {
                CientifficDAO dao = new CientifficDAO(conexion);
                procesarMensajes(entrada, salida, dao, ip);

            } catch (SQLException e) {
                // La BD no está disponible: informar al cliente y cerrar la sesión
                Logs.error("[" + ip + "] No se pudo conectar a la BD: " + e.getMessage(), e);
                salida.println("ERROR:BASE_DE_DATOS_NO_DISPONIBLE");
            }*/

        } catch (IOException e) {
            // Error leyendo/escribiendo en el socket (cliente desconectado abruptamente, etc.)
            Logs.error("[" + ip + "] Error de E/S: " + e.getMessage());
        } finally {
            // Este bloque siempre se ejecuta — registra desconexión en el log
            Logs.info("[" + ip + "] Cliente desconectado");
        }
    }
    
    public void Start(){};

    
    
    /**
     * Bucle principal: lee mensajes del cliente, los procesa y responde.
     *
     * El protocolo usa líneas de texto: cada mensaje del cliente es una línea (\n)
     * y la respuesta del servidor también es una línea (\n).
     * La secuencia es estrictamente request-response: el cliente envía UNA línea
     * y espera UNA línea de respuesta antes de enviar la siguiente.
     *
     * readLine() devuelve null cuando el cliente cierra la conexión (EOF en el socket).
     */
    private void procesarMensajes(BufferedReader entrada, PrintWriter salida,
                                   CientifficDAO dao, String ip) throws IOException {
        ManejadorPeticiones handler = new ManejadorPeticiones(/*dao*/);
        String linea;
        while ((linea = entrada.readLine()) != null) {
            Logs.info("[" + ip + "] >> " + linea);
            // procesar() SIEMPRE devuelve una String no nula — nunca lanza excepciones
            String respuesta = handler.procesar(linea);
            salida.println(respuesta);
            Logs.info("[" + ip + "] << " + respuesta);
        }
    }
}
