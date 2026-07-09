package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;

/**
 * Visor del log del servidor (servidor.log).
 * Pide el contenido al servidor vía LISTAR_LOGS y lo muestra en un área de solo lectura.
 */
public class DialogoLogs extends JDialog {

    private final ConexionServidor conexion;
    private final JTextArea taActual = crearArea();

    public DialogoLogs(Window owner, ConexionServidor conexion) {
        super(owner, "Logs del Servidor", ModalityType.APPLICATION_MODAL);
        this.conexion = conexion;
        buildUI();
        cargar();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setPreferredSize(new Dimension(860, 580));
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Estilos.NAVBAR_BG);
        header.setBorder(Estilos.padding(12, 20, 12, 20));
        JLabel titulo = new JLabel("Logs del Servidor");
        titulo.setFont(new Font("SansSerif", Font.PLAIN, 17));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        add(envolver(taActual), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Estilos.FONDO);
        JButton btnActualizar = Estilos.boton("Actualizar", Estilos.BTN_EDIT);
        btnActualizar.setPreferredSize(new Dimension(120, 34));
        btnActualizar.addActionListener(e -> cargar());
        JButton btnCerrar = Estilos.boton("Cerrar", new Color(120, 140, 170));
        btnCerrar.setPreferredSize(new Dimension(100, 34));
        btnCerrar.addActionListener(e -> dispose());
        footer.add(btnActualizar);
        footer.add(btnCerrar);
        add(footer, BorderLayout.SOUTH);
    }

    private void cargar() {
        cargarEn(taActual, Protocolo.CMD_LISTAR_LOGS);
    }

    private void cargarEn(JTextArea area, String comando) {
        try {
            String resp = conexion.enviar(comando);
            if (resp != null && resp.startsWith(Protocolo.OK)) {
                String[] partes = Protocolo.parsear(resp);
                String contenido = partes.length > 1 ? partes[1] : "";
                area.setText(contenido.isEmpty() ? "(Sin contenido)" : contenido);
            } else {
                area.setText("Error al obtener el log: " + Estilos.extraerError(resp));
            }
        } catch (Exception ex) {
            area.setText("Error de conexión: " + ex.getMessage());
        }
        area.setCaretPosition(area.getDocument().getLength());
    }

    private static JScrollPane envolver(JTextArea area) {
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        return sp;
    }

    private static JTextArea crearArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setForeground(Estilos.TEXT_DARK);
        ta.setBackground(Color.WHITE);
        ta.setLineWrap(false);
        ta.setTabSize(2);
        return ta;
    }
}