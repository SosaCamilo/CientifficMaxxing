package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo de resultados de un experimento con dos modos exclusivos:
 *
 *   soloAgregar = true  → modo AGREGAR: muestra solo el formulario de carga.
 *                          No muestra el historial de resultados anteriores.
 *   soloAgregar = false → modo VER: muestra el historial de resultados.
 *                          No permite agregar nuevos resultados.
 *
 * COLUMNAS DE SP_VerResultadosPorExperimento:
 *   [0]=IdResultado  [1]=Fecha  [2]=Descripcion  [3]=Prueba  [4]=TipoDePrueba  [5]=Experimento
 *
 * REGLA RF02: No se pueden agregar resultados a experimentos "Exitoso" o "Fallido".
 *
 * TIPOS DE PRUEBA: Hardcodeados según la tabla TipoPrueba en BD (IDs 1–10).
 */
public class DialogoResultados extends JDialog {

    private static final String[][] TIPOS_PRUEBA = {
        {"1",  "Hemograma completo"},
        {"2",  "Panel metabólico"},
        {"3",  "Electroencefalograma"},
        {"4",  "Prueba de cortisol"},
        {"5",  "Análisis de testosterona"},
        {"6",  "Prueba de resistencia física"},
        {"7",  "Escala de somnolencia de Epworth"},
        {"8",  "Polisomniografía"},
        {"9",  "Densitometría capilar"},
        {"10", "Biopsia de folículo piloso"}
    };

    private final ConexionServidor conexion;
    private final String           idExperimento;
    private final String           estadoExperimento;
    private final boolean          soloAgregar;

    private JPanel      listaPanel;
    private JScrollPane scroll;

    public DialogoResultados(Window owner, ConexionServidor conexion,
                             String idExperimento, String nombreExperimento,
                             String estadoExperimento, boolean soloAgregar) {
        super(owner,
              soloAgregar ? "Agregar Resultado — " + nombreExperimento
                          : "Resultados — " + nombreExperimento,
              ModalityType.APPLICATION_MODAL);
        this.conexion          = conexion;
        this.idExperimento     = idExperimento;
        this.estadoExperimento = estadoExperimento;
        this.soloAgregar       = soloAgregar;

        if (soloAgregar) {
            buildUIAgregar(nombreExperimento);
        } else {
            buildUIVer(nombreExperimento);
            cargarResultados();
        }

        pack();
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    // ═══════════════════════════════════════════════════════════
    // MODO VER — solo historial, sin botón agregar
    // ═══════════════════════════════════════════════════════════

    private void buildUIVer(String nombreExperimento) {
        setPreferredSize(new Dimension(700, 520));
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Estilos.NAVBAR_BG);
        header.setBorder(Estilos.padding(10, 20, 10, 20));
        JLabel titulo = new JLabel("Resultados del Experimento");
        titulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        titulo.setForeground(new Color(180, 200, 230));
        header.add(titulo, BorderLayout.WEST);

        JLabel lblEstado = new JLabel("Estado: " + estadoExperimento);
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstado.setForeground(new Color(180, 200, 230));
        header.add(lblEstado, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Estilos.FONDO);
        listaPanel.setBorder(Estilos.padding(8, 12, 8, 12));

        scroll = new JScrollPane(listaPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Estilos.FONDO);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Estilos.FONDO);
        footer.setBorder(Estilos.padding(4, 16, 8, 16));
        JButton btnCerrar = Estilos.boton("Cerrar", new Color(120, 140, 170));
        btnCerrar.setPreferredSize(new Dimension(100, 34));
        btnCerrar.addActionListener(e -> dispose());
        footer.add(btnCerrar);
        add(footer, BorderLayout.SOUTH);
    }

    private void cargarResultados() {
        listaPanel.removeAll();
        try {
            String resp = conexion.enviar(
                Protocolo.construir(Protocolo.CMD_LISTAR_RESULTADOS, idExperimento));
            TablaRespuesta tabla = TablaRespuesta.parsear(resp);

            if (tabla.isEmpty()) {
                JLabel empty = new JLabel("  No hay resultados registrados para este experimento.");
                empty.setFont(Estilos.FUENTE_LABEL);
                empty.setForeground(new Color(120, 130, 150));
                listaPanel.add(empty);
            } else {
                for (String[] fila : tabla.filas) {
                    listaPanel.add(buildTarjetaResultado(fila));
                    listaPanel.add(Box.createVerticalStrut(8));
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("  Error al cargar: " + ex.getMessage());
            err.setForeground(Estilos.BTN_CANCEL);
            listaPanel.add(err);
        }
        listaPanel.revalidate();
        listaPanel.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private JPanel buildTarjetaResultado(String[] fila) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Estilos.CARD_RES);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(8, 0));
        card.setOpaque(false);
        card.setBorder(Estilos.padding(10, 14, 10, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(fila[0] + " - " + fila[4]);
        lblTitulo.setFont(Estilos.FUENTE_CARD_H);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblTitulo);

        JLabel lblSub = new JLabel("Fecha: " + Estilos.fechaDisplay(fila[1]) +
                                   "   Resultado: " + truncar(fila[3], 60));
        lblSub.setFont(Estilos.FUENTE_CARD_SUB);
        lblSub.setForeground(new Color(220, 235, 255));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblSub);

        if (!fila[2].isEmpty()) {
            JLabel lblDesc = new JLabel(truncar(fila[2], 80));
            lblDesc.setFont(new Font("SansSerif", Font.ITALIC, 11));
            lblDesc.setForeground(new Color(200, 220, 250));
            lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenido.add(lblDesc);
        }
        card.add(contenido, BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════════════════════════════════════
    // MODO AGREGAR — solo el formulario, sin historial
    // ═══════════════════════════════════════════════════════════

    private void buildUIAgregar(String nombreExperimento) {
        setPreferredSize(new Dimension(460, 490));
        setLayout(new BorderLayout());

        // Header
        JPanel hdr = new JPanel();
        hdr.setBackground(Estilos.NAVBAR_BG);
        hdr.setBorder(Estilos.padding(12, 20, 12, 20));
        JLabel th = new JLabel("Agregar Resultado Parcial");
        th.setFont(new Font("SansSerif", Font.PLAIN, 17));
        th.setForeground(new Color(180, 200, 230));
        hdr.add(th);
        add(hdr, BorderLayout.NORTH);

        // RF02: bloqueo si el experimento está finalizado
        if ("Exitoso".equals(estadoExperimento) || "Fallido".equals(estadoExperimento) || "Sin Comenzar".equals(estadoExperimento)) {
            JPanel bloqueado = new JPanel(new BorderLayout());
            bloqueado.setBackground(Color.WHITE);
            bloqueado.setBorder(Estilos.padding(30, 30, 30, 30));
            JLabel msg = new JLabel("<html><b>Operación no permitida</b><br><br>"
                + "No se pueden agregar resultados a un experimento<br>"
                + "en estado <b>" + estadoExperimento + "</b>.</html>");
            msg.setFont(Estilos.FUENTE_LABEL);
            msg.setForeground(Estilos.TEXT_DARK);
            bloqueado.add(msg, BorderLayout.CENTER);

            JPanel fp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            fp.setBackground(Color.WHITE);
            JButton btnC = Estilos.boton("Cerrar", new Color(120, 140, 170));
            btnC.setPreferredSize(new Dimension(100, 36));
            btnC.addActionListener(e -> dispose());
            fp.add(btnC);
            bloqueado.add(fp, BorderLayout.SOUTH);
            add(bloqueado, BorderLayout.CENTER);
            return;
        }

        // Campos del formulario
        JTextField tfDd    = Estilos.campo(3);
        JTextField tfMm    = Estilos.campo(3);
        JTextField tfYyyy  = Estilos.campo(5);
        JTextArea  taDesc  = Estilos.area(3, 30);
        JTextField tfPrueba = Estilos.campo(30);
        JComboBox<String> cbTipo = new JComboBox<>();
        for (String[] t : TIPOS_PRUEBA) cbTipo.addItem(t[0] + " - " + t[1]);
        cbTipo.setFont(Estilos.FUENTE_LABEL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(Estilos.padding(14, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;

        gc.gridy = 0; gc.insets = new Insets(0, 0, 12, 0);
        form.add(new JSeparator(), gc);

        gc.gridy = 1; gc.insets = new Insets(4, 0, 2, 0);
        form.add(fl("Fecha"), gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 8, 0);
        JPanel fechaP = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        fechaP.setOpaque(false);
        tfDd.setPreferredSize(new Dimension(48, 32));
        tfDd.setHorizontalAlignment(SwingConstants.CENTER);
        tfMm.setPreferredSize(new Dimension(48, 32));
        tfMm.setHorizontalAlignment(SwingConstants.CENTER);
        tfYyyy.setPreferredSize(new Dimension(66, 32));
        tfYyyy.setHorizontalAlignment(SwingConstants.CENTER);
        ph(tfDd, "DD"); ph(tfMm, "MM"); ph(tfYyyy, "YYYY");
        fechaP.add(tfDd); fechaP.add(tfMm); fechaP.add(tfYyyy);
        form.add(fechaP, gc);

        gc.gridy = 3; gc.insets = new Insets(4, 0, 2, 0);
        form.add(fl("Descripción"), gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 8, 0);
        form.add(new JScrollPane(taDesc), gc);

        gc.gridy = 5; gc.insets = new Insets(4, 0, 2, 0);
        form.add(fl("Texto de Prueba (resultado observado)"), gc);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 8, 0);
        form.add(tfPrueba, gc);

        gc.gridy = 7; gc.insets = new Insets(4, 0, 2, 0);
        form.add(fl("Tipo de Prueba"), gc);
        gc.gridy = 8; gc.insets = new Insets(0, 0, 0, 0);
        cbTipo.setPreferredSize(new Dimension(0, 34));
        form.add(cbTipo, gc);

        add(form, BorderLayout.CENTER);

        // Botones
        JPanel bp = new JPanel(new GridLayout(2, 1, 0, 8));
        bp.setBackground(Color.WHITE);
        bp.setBorder(Estilos.padding(8, 24, 16, 24));

        JButton btnConfirmar = Estilos.boton("Confirmar", Estilos.BTN_CONFIRM);
        btnConfirmar.setPreferredSize(new Dimension(0, 42));
        JButton btnCancelar = Estilos.boton("Cancelar", Estilos.BTN_CANCEL);
        btnCancelar.setPreferredSize(new Dimension(0, 42));
        btnCancelar.addActionListener(e -> dispose());
        bp.add(btnConfirmar);
        bp.add(btnCancelar);
        add(bp, BorderLayout.SOUTH);

        btnConfirmar.addActionListener(e -> {
            String fecha  = Estilos.ensamblarFecha(tfDd, tfMm, tfYyyy);
            String desc   = taDesc.getText().trim();
            String prueba = tfPrueba.getText().trim();
            if (fecha == null) {
                warn(this, "Fecha inválida. Verificá que el día (1-31), mes (1-12) y año (1900-2100) sean correctos.");
                return;
            }
            if (prueba.isEmpty()) {
                warn(this, "El texto de prueba es obligatorio.");
                return;
            }
            int selIdx = cbTipo.getSelectedIndex();
            if (selIdx < 0) {
                warn(this, "Seleccioná un tipo de prueba.");
                return;
            }
            String idPrueba = TIPOS_PRUEBA[selIdx][0];
            try {
                String peticion = Protocolo.construir(Protocolo.CMD_AGREGAR_RESULTADO,
                                      fecha, desc, prueba, idExperimento, idPrueba);
                String resp = conexion.enviar(peticion);
                if (resp != null && resp.startsWith(Protocolo.OK)) {
                    JOptionPane.showMessageDialog(this,
                        "Resultado registrado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        Estilos.extraerError(resp), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private static String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private static JLabel fl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Estilos.FUENTE_LABEL);
        l.setForeground(Estilos.TEXT_DARK);
        return l;
    }

    private static void ph(JTextField tf, String hint) {
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

    private static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }
}
