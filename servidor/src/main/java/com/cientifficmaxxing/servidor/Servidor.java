package com.cientifficmaxxing.servidor;

import com.cientifficmaxxing.servidor.util.Logs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import com.cientifficmaxxing.servidor.dao.NCientifficDAO;


/**
 * Punto de entrada del servidor TCP de CientifficMaxxing.
 *
 * El servidor atiende clientes de forma secuencial: acepta una conexión,
 * la atiende completamente y luego acepta la siguiente.
 * La integridad de los datos ante accesos simultáneos la gestiona MySQL (InnoDB)
 * mediante transacciones y row-level locking en los Stored Procedures.
 */
public class Servidor {

    private static final int PUERTO = 9000;

    public static void main(String[] args) {
        Logs.info("=== CientifficMaxxing Servidor ===");
        Logs.info("Iniciando en puerto " + PUERTO + "...");
        
        try {
            NCientifficDAO.cargar();
            
        } catch (IOException e) {
            System.err.println("Error crítico al cargar datos: " + e.getMessage());
            System.exit(1);
        }
        
        // try-with-resources garantiza que el ServerSocket se cierre si ocurre un error.
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            Logs.info("Servidor listo. Esperando clientes...");

            // Bucle infinito: el servidor atiende clientes hasta que el proceso es detenido.
            while (true) {
                // accept() bloquea hasta recibir una conexión TCP entrante.
                Socket clienteSocket = serverSocket.accept();
                String ip = clienteSocket.getInetAddress().getHostAddress();
                
                Logs.info("Conexión entrante desde: " + ip);

                // Atender al cliente de forma secuencial antes de aceptar el siguiente.
                
                new ManejadorCliente(clienteSocket).start();
            }

        } catch (IOException e) {
            Logs.error("Error fatal en el servidor", e);
        }
    }
}
