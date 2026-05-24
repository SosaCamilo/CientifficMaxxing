package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;

public class DialogoEstado extends JDialog {

    private final ConexionServidor conexion;
    private final String           idExperimento;
    private boolean                confirmado = false;

    private static final String[] ESTADOS = {
        "Sin comenzar", "En proceso", "Exitoso", "Fallido"
    };
    private final JComboBox<String> cbEstado = new JComboBox<>(ESTADOS);

    public DialogoEstado(Window owner, ConexionServidor conexion,
                         String idExperimento, String nombreExperimento, String estadoActual) {
        super(owner, "Cambiar Estado", ModalityType.APPLICATION_MODAL);
        this.conexion      = conexion;
        this.idExperimento = idExperimento;
        buildUI(nombreExperimento, estadoActual);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI(String nombreExperimento, String estadoActual) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(440, 380));

        // Header
        JPanel header = new JPanel();
        header.setBackground(Estilos.NAVBAR_BG);
        header.setBorder(Estilos.padding(14, 24, 14, 24));
        JLabel titulo = new JLabel("Cambiar Estado");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(Estilos.padding(22, 32, 14, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx   = 0;

        // Separador
        gc.gridy = 0; gc.insets = new Insets(0, 0, 18, 0);
        form.add(new JSeparator(), gc);

        // Nombre del experimento (destacado)
        gc.gridy = 1; gc.insets = new Insets(0, 0, 4, 0);
        JLabel lblSub = new JLabel("Experimento");
        lblSub.setFont(Estilos.FUENTE_SMALL);
        lblSub.setForeground(Estilos.TEXT_MUTED);
        form.add(lblSub, gc);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 18, 0);
        JLabel lblNombre = new JLabel(idExperimento + "  —  " + nombreExperimento);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNombre.setForeground(Estilos.NAVBAR_BG);
        form.add(lblNombre, gc);

        // Estado actual con badge
        gc.gridy = 3; gc.insets = new Insets(0, 0, 6, 0);
        JLabel lblActualSub = new JLabel("Estado actual");
        lblActualSub.setFont(Estilos.FUENTE_SMALL);
        lblActualSub.setForeground(Estilos.TEXT_MUTED);
        form.add(lblActualSub, gc);

        gc.gridy = 4; gc.insets = new Insets(0, 0, 22, 0);
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setOpaque(false);
        JLabel badge = Estilos.badgeEstado(estadoActual);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badgePanel.add(badge);
        form.add(badgePanel, gc);

        // Nuevo estado
        gc.gridy = 5; gc.insets = new Insets(0, 0, 6, 0);
        JLabel lblNuevoSub = new JLabel("Nuevo estado");
        lblNuevoSub.setFont(Estilos.FUENTE_SMALL);
        lblNuevoSub.setForeground(Estilos.TEXT_MUTED);
        form.add(lblNuevoSub, gc);

        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        cbEstado.setFont(Estilos.FUENTE_LABEL);
        cbEstado.setPreferredSize(new Dimension(360, 36));
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(estadoActual)) { cbEstado.setSelectedIndex(i); break; }
        }
        form.add(cbEstado, gc);

        add(form, BorderLayout.CENTER);

        // Botones lado a lado
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(Estilos.padding(10, 32, 22, 32));

        JButton btnConfirmar = Estilos.boton("Confirmar", Estilos.BTN_CONFIRM);
        btnConfirmar.setPreferredSize(new Dimension(0, 44));
        btnConfirmar.addActionListener(e -> confirmar());

        JButton btnCancelar = Estilos.boton("Cancelar", Estilos.BTN_CANCEL);
        btnCancelar.setPreferredSize(new Dimension(0, 44));
        btnCancelar.addActionListener(e -> dispose());

        btnPanel.add(btnConfirmar);
        btnPanel.add(btnCancelar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void confirmar() {
        String nuevoEstado = (String) cbEstado.getSelectedItem();
        if (nuevoEstado == null) return;
        try {
            String peticion  = Protocolo.construir(Protocolo.CMD_ACTUALIZAR_ESTADO,
                                   idExperimento, nuevoEstado);
            String respuesta = conexion.enviar(peticion);
            if (respuesta != null && respuesta.startsWith(Protocolo.OK)) {
                confirmado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, Estilos.extraerError(respuesta),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean fueConfirmado() { return confirmado; }
}
