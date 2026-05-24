package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para Agregar (RF07) o Modificar (RF08) un científico.
 *
 * MODOS:
 *   id == null → AGREGAR: envía AGREGAR_CIENTIFICO|nombre|apellido|nacimiento
 *   id != null → MODIFICAR: envía ACTUALIZAR_CIENTIFICO|id|nombre|apellido|nacimiento
 *
 * La fecha de nacimiento usa tres campos separados (DD MM YYYY) con placeholders.
 * Estilos.ensamblarFecha() los valida y une en "yyyy-MM-dd" antes de enviar al servidor.
 *
 * Uso:
 *   new DialogoCientifico(owner, conexion, null, null, null, null)      // AGREGAR
 *   new DialogoCientifico(owner, conexion, "5", "María", "García", "1990-05-12") // MODIFICAR
 *   if (dialogo.fueConfirmado()) { panelCientificos.cargar(); }
 */
public class DialogoCientifico extends JDialog {

    private final ConexionServidor conexion;
    private final String           idExistente;   // null = modo AGREGAR
    private boolean                confirmado = false;

    private final JTextField tfNombre    = Estilos.campo(20);
    private final JTextField tfApellido  = Estilos.campo(20);
    private final JTextField tfDia       = Estilos.campo(3);
    private final JTextField tfMes       = Estilos.campo(3);
    private final JTextField tfAnio      = Estilos.campo(5);

    public DialogoCientifico(Window owner, ConexionServidor conexion,
                              String id, String nombre, String apellido, String nacimiento) {
        super(owner, id == null ? "Cargar Científico" : "Modificar Científico",
              ModalityType.APPLICATION_MODAL);
        this.conexion    = conexion;
        this.idExistente = id;
        buildUI();
        if (id != null) precargar(nombre, apellido, nacimiento);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Encabezado azul ──────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(Estilos.NAVBAR_BG);
        header.setBorder(Estilos.padding(16, 24, 16, 24));
        JLabel titulo = new JLabel(idExistente == null ? "Nuevo Científico" : "Modificar Científico");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // ── Formulario ────────────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new GridBagLayout());
        form.setBorder(Estilos.padding(20, 30, 10, 30));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(8, 0, 4, 0);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx   = 0;

        // Separador
        gc.gridy = 0;
        JSeparator sep = new JSeparator();
        sep.setForeground(Estilos.SEPARADOR);
        form.add(sep, gc);

        // Nombre
        gc.gridy = 1; gc.insets = new Insets(14, 0, 2, 0);
        form.add(label("Nombre"), gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 8, 0);
        tfNombre.setPreferredSize(new Dimension(260, 34));
        form.add(tfNombre, gc);

        // Apellido
        gc.gridy = 3; gc.insets = new Insets(8, 0, 2, 0);
        form.add(label("Apellido"), gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 8, 0);
        tfApellido.setPreferredSize(new Dimension(260, 34));
        form.add(tfApellido, gc);

        // Nacimiento
        gc.gridy = 5; gc.insets = new Insets(8, 0, 2, 0);
        form.add(label("Nacimiento"), gc);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 8, 0);
        form.add(buildFechaPanel(), gc);

        add(form, BorderLayout.CENTER);

        // ── Botones ───────────────────────────────────────────────────────────
        JPanel botones = new JPanel(new GridLayout(1, 2, 12, 0));
        botones.setBackground(Color.WHITE);
        botones.setBorder(Estilos.padding(10, 30, 22, 30));

        JButton btnConfirmar = Estilos.boton("Confirmar", Estilos.BTN_EDIT);
        btnConfirmar.setPreferredSize(new Dimension(0, 44));
        btnConfirmar.addActionListener(e -> confirmar());

        JButton btnCancelar = Estilos.boton("Cancelar", Estilos.BTN_CANCEL);
        btnCancelar.setPreferredSize(new Dimension(0, 44));
        btnCancelar.addActionListener(e -> dispose());

        botones.add(btnConfirmar);
        botones.add(btnCancelar);
        add(botones, BorderLayout.SOUTH);
    }

    private JPanel buildFechaPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        tfDia.setPreferredSize(new Dimension(50, 34));
        tfMes.setPreferredSize(new Dimension(50, 34));
        tfAnio.setPreferredSize(new Dimension(70, 34));
        tfDia.setHorizontalAlignment(SwingConstants.CENTER);
        tfMes.setHorizontalAlignment(SwingConstants.CENTER);
        tfAnio.setHorizontalAlignment(SwingConstants.CENTER);
        // Placeholders
        ponerPlaceholder(tfDia,  "DD");
        ponerPlaceholder(tfMes,  "MM");
        ponerPlaceholder(tfAnio, "YYYY");
        p.add(tfDia); p.add(tfMes); p.add(tfAnio);
        return p;
    }

    private void precargar(String nombre, String apellido, String nacimiento) {
        tfNombre.setText(nombre);
        tfApellido.setText(apellido);
        String[] partes = Estilos.descomponerFecha(nacimiento);
        tfDia.setText(partes[0]);
        tfMes.setText(partes[1]);
        tfAnio.setText(partes[2]);
    }

    private void confirmar() {
        String nombre   = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String fecha    = Estilos.ensamblarFecha(tfDia, tfMes, tfAnio);

        if (nombre.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Apellido son obligatorios.",
                "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fecha == null) {
            JOptionPane.showMessageDialog(this, "Completá los tres campos de Nacimiento (DD MM YYYY).",
                "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String peticion;
            if (idExistente == null) {
                peticion = Protocolo.construir(Protocolo.CMD_AGREGAR_CIENTIFICO, nombre, apellido, fecha);
            } else {
                peticion = Protocolo.construir(Protocolo.CMD_ACTUALIZAR_CIENTIFICO,
                               idExistente, nombre, apellido, fecha);
            }
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

    private static JLabel label(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(Estilos.FUENTE_LABEL);
        lbl.setForeground(Estilos.TEXT_DARK);
        return lbl;
    }

    private static void ponerPlaceholder(JTextField tf, String hint) {
        tf.setText(hint);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(hint)) { tf.setText(""); tf.setForeground(Estilos.TEXT_DARK); }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(hint); tf.setForeground(Color.GRAY); }
            }
        });
    }
}
