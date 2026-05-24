package com.cientifficmaxxing.cliente;

import java.io.*;
import java.net.Socket;

/**
 * Gestiona la conexión TCP con el servidor en localhost:9000.
 *
 * DISEÑO:
 *   Un único socket TCP persiste durante toda la sesión del cliente.
 *   La comunicación es siempre 1 request → 1 response (sin pipelining).
 *
 * PROTOCOLO DE TEXTO:
 *   - salida.println(peticion) envía la petición + \n al servidor.
 *   - entrada.readLine() lee la respuesta hasta el siguiente \n.
 *   - Si el servidor cierra la conexión, readLine() devuelve null → IOException.
 *
 * CHARSET:
 *   Tanto el cliente como el servidor usan UTF-8 explícitamente para evitar
 *   problemas con tildes y caracteres especiales en Windows (default: Windows-1252).
 */
public class ConexionServidor {

    private static final String HOST   = "localhost";
    private static final int    PUERTO = 9000;

    private final Socket         socket;
    private final BufferedReader entrada;
    private final PrintWriter    salida;

    /** Abre el socket TCP y los streams. Lanza IOException si el servidor no responde. */
    public ConexionServidor() throws IOException {
        socket  = new Socket(HOST, PUERTO);
        entrada = new BufferedReader(
                      new InputStreamReader(socket.getInputStream(), "UTF-8"));
        salida  = new PrintWriter(
                      new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
    }

    /**
     * Envía una petición al servidor y espera (bloquea) la respuesta.
     * La llamada bloquea hasta que el servidor responde.
     *
     * @throws IOException si el socket fue cerrado o el servidor desconectó.
     */
    public String enviar(String peticion) throws IOException {
        if (socket.isClosed()) throw new IOException("Conexión cerrada");
        salida.println(peticion);        // envía línea + flush automático (autoFlush=true)
        String respuesta = entrada.readLine(); // bloquea hasta recibir \n del servidor
        if (respuesta == null) throw new IOException("El servidor cerró la conexión");
        return respuesta;
    }

    /** Retorna true si el socket sigue abierto y conectado. */
    public boolean isConectado() {
        return !socket.isClosed() && socket.isConnected();
    }

    /** Cierra el socket al cerrar la ventana principal. */
    public void cerrar() {
        try { socket.close(); } catch (IOException ignored) {}
    }
}
