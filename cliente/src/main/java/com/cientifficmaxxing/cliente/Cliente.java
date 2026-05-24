package com.cientifficmaxxing.cliente;

import com.cientifficmaxxing.cliente.ui.VentanaPrincipal;

import javax.swing.*;

/**
 * Punto de entrada del cliente gráfico de CientifficMaxxing.
 *
 * SECUENCIA DE INICIO:
 *   1. Se aplica el look&feel nativo (Windows/Mac/Linux).
 *   2. Se intenta abrir un Socket TCP a localhost:9000.
 *      Si el servidor NO está corriendo, se muestra un mensaje y se sale.
 *   3. Si la conexión es exitosa, se muestra la VentanaPrincipal.
 *
 * INICIO DE LA UI:
 *   SwingUtilities.invokeLater() encola la creación de la ventana en el
 *   hilo de eventos de Swing, tal como requiere el framework.
 *
 * REQUISITO PREVIO: el servidor debe estar corriendo antes de iniciar el cliente.
 *   Ejecutar primero: mvn exec:java -pl servidor  (o doble clic en iniciar_servidor.cmd)
 */
public class Cliente {
    public static void main(String[] args) {
        // Look & feel nativo: la UI se ve como una app de Windows/Mac en vez de Swing genérico
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // invokeLater garantiza que la construcción de Swing ocurra en el EDT
        SwingUtilities.invokeLater(() -> {
            ConexionServidor conexion;
            try {
                // Abre el socket TCP. Si el servidor no está escuchando, lanza IOException.
                conexion = new ConexionServidor();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "No se pudo conectar al servidor en localhost:9000.\n\n" +
                    "Verificá que el servidor esté corriendo antes de iniciar el cliente.\n" +
                    "Ejecutá primero: mvn exec:java -pl servidor",
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            new VentanaPrincipal(conexion).setVisible(true);
        });
    }
}
