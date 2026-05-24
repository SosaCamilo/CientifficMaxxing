package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de listado de experimentos (RF05).
 * Permite listar, buscar (por ID o nombre), agregar, editar y eliminar experimentos.
 * También da acceso a los resultados (RF02/RF04), cambio de estado (RF03) y equipo (Realiza).
 *
 * Columnas de SP_VerExperimentos (9 columnas, en este orden):
 *  [0]=IdExperimento  [1]=Nombre       [2]=Descripcion  [3]=FechaInicio
 *  [4]=FechaFinal     [5]=Estado       [6]=Responsable  [7]=IdResponsable  [8]=Equipo
 *  [6] = CONCAT(Nombre, ' ', Apellido) del responsable. [8] = GROUP_CONCAT del equipo (Realiza).
 */
public class PanelExperimentos extends JPanel {

    private final ConexionServidor conexion;
    private String[][]             todosLosExp = new String[0][];

    private JPanel     listaPanel;
    private JScrollPane scroll;
    private JTextField  tfBuscar;

    public PanelExperimentos(ConexionServidor conexion) {
        this.conexion = conexion;
        setBackground(Estilos.FONDO);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Barra superior ────────────────────────────────────────────────────
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Estilos.FONDO);
        barra.setBorder(Estilos.padding(12, 16, 12, 16));

        JButton btnNuevo = Estilos.boton("⊕  Nuevo", Estilos.BTN_NEW);
        btnNuevo.setPreferredSize(new Dimension(110, 36));
        btnNuevo.addActionListener(e -> abrirAgregar());
        barra.add(btnNuevo, BorderLayout.WEST);

        JPanel buscarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buscarPanel.setOpaque(false);
        JLabel iconoBuscar = new JLabel("🔍");
        tfBuscar = new JTextField(22);
        tfBuscar.setFont(Estilos.FUENTE_LABEL);
        tfBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilos.SEPARADOR),
            Estilos.padding(4, 8, 4, 8)));
        tfBuscar.setToolTipText("Buscar por ID o Nombre");
        placeholder(tfBuscar, "Buscar (ID / Nombre):");
        tfBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        buscarPanel.add(iconoBuscar);
        buscarPanel.add(tfBuscar);
        barra.add(buscarPanel, BorderLayout.CENTER);

        add(barra, BorderLayout.NORTH);

        // ── Lista de tarjetas ─────────────────────────────────────────────────
        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Estilos.FONDO);
        listaPanel.setBorder(Estilos.padding(4, 12, 12, 12));

        scroll = new JScrollPane(listaPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Estilos.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    /** Carga los experimentos desde el servidor y repinta la lista. */
    public void cargar() {
        listaPanel.removeAll();
        try {
            String resp  = conexion.enviar(Protocolo.CMD_LISTAR_EXPERIMENTOS);
            TablaRespuesta tabla = TablaRespuesta.parsear(resp);
            todosLosExp = tabla.filas;
        } catch (Exception ex) {
            todosLosExp = new String[0][];
            JLabel err = new JLabel("  Error al cargar experimentos: " + ex.getMessage());
            err.setForeground(Estilos.BTN_CANCEL);
            listaPanel.add(err);
        }
        mostrarFilas(todosLosExp);
    }

    private void filtrar() {
        String filtro = tfBuscar.getText().trim().toLowerCase();
        if (filtro.equals("buscar (id / nombre):") || filtro.isEmpty()) {
            mostrarFilas(todosLosExp);
            return;
        }
        List<String[]> resultado = new ArrayList<>();
        for (String[] f : todosLosExp) {
            if (f[0].contains(filtro) || f[1].toLowerCase().contains(filtro)) {
                resultado.add(f);
            }
        }
        mostrarFilas(resultado.toArray(new String[0][]));
    }

    private void mostrarFilas(String[][] filas) {
        listaPanel.removeAll();
        if (filas.length == 0) {
            JLabel vacio = new JLabel("  No se encontraron experimentos.");
            vacio.setFont(Estilos.FUENTE_LABEL);
            vacio.setForeground(new Color(120, 130, 150));
            vacio.setBorder(Estilos.padding(20, 0, 0, 0));
            listaPanel.add(vacio);
        } else {
            for (String[] fila : filas) {
                listaPanel.add(buildTarjeta(fila));
                listaPanel.add(Box.createVerticalStrut(10));
            }
        }
        listaPanel.revalidate();
        listaPanel.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    // ── Construcción de tarjeta ───────────────────────────────────────────────

    private JPanel buildTarjeta(String[] fila) {
        // fila: [0]=Id [1]=Nombre [2]=Descripcion [3]=FechaInicio
        //        [4]=FechaFinal [5]=Estado [6]=IdResponsable [7]=Responsable
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Estilos.CARD_EXP);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(8, 0));
        card.setOpaque(false);
        card.setBorder(Estilos.padding(10, 14, 10, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // ── Contenido izquierda ───────────────────────────────────────────────
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(fila[0] + " - " + fila[1]);
        lblTitulo.setFont(Estilos.FUENTE_CARD_H);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(2));

        // [6]=Responsable(texto), [7]=IdResponsable, [8]=Equipo (GROUP_CONCAT de Realiza)
        String responsable = fila.length > 6 ? fila[6] : "";
        JLabel lblResp = new JLabel("Responsable: " + responsable);
        lblResp.setFont(Estilos.FUENTE_CARD_SUB);
        lblResp.setForeground(new Color(220, 240, 255));
        lblResp.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblResp);

        if (fila.length > 8 && fila[8] != null && !fila[8].isBlank()) {
            JLabel lblEquipo = new JLabel("Equipo: " + fila[8]);
            lblEquipo.setFont(Estilos.FUENTE_CARD_SUB);
            lblEquipo.setForeground(new Color(180, 220, 255));
            lblEquipo.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenido.add(lblEquipo);
        }
        contenido.add(Box.createVerticalStrut(4));

        // Botones de acción
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnResultado  = Estilos.botonCard("+ Resultado",   Estilos.BTN_CONFIRM);
        JButton btnEstado     = Estilos.botonCard("Estado",        Estilos.BTN_CONFIRM);
        JButton btnVerRes     = Estilos.botonCard("Resultados",    Estilos.BTN_CONFIRM);
        JButton btnEquipo     = Estilos.botonCard("Equipo",        Estilos.BTN_EDIT);

        btnResultado.addActionListener(e -> {
            DialogoResultados dlg = new DialogoResultados(
                SwingUtilities.getWindowAncestor(this), conexion,
                fila[0], fila[1], fila[5], true);
            dlg.setVisible(true);
        });
        btnEstado.addActionListener(e -> {
            DialogoEstado dlg = new DialogoEstado(
                SwingUtilities.getWindowAncestor(this), conexion,
                fila[0], fila[1], fila[5]);
            dlg.setVisible(true);
            if (dlg.fueConfirmado()) cargar();
        });
        btnVerRes.addActionListener(e -> {
            DialogoResultados dlg = new DialogoResultados(
                SwingUtilities.getWindowAncestor(this), conexion,
                fila[0], fila[1], fila[5], false);
            dlg.setVisible(true);
        });
        btnEquipo.addActionListener(e -> {
            String idResponsable = fila.length > 7 ? fila[7] : "";
            DialogoRealiza dlg = new DialogoRealiza(
                SwingUtilities.getWindowAncestor(this), conexion, fila[0], fila[1], idResponsable);
            dlg.setVisible(true);
            if (dlg.hubosCambios()) cargar();
        });

        btnRow.add(btnResultado);
        btnRow.add(btnEstado);
        btnRow.add(btnVerRes);
        btnRow.add(btnEquipo);
        contenido.add(btnRow);
        card.add(contenido, BorderLayout.CENTER);

        // ── Derecha: fecha, estado, editar/borrar ─────────────────────────────
        JPanel derecha = new JPanel();
        derecha.setOpaque(false);
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        metaPanel.setOpaque(false);
        JLabel lblFecha = new JLabel(Estilos.fechaDisplay(fila[4]));
        lblFecha.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblFecha.setForeground(Color.WHITE);
        metaPanel.add(lblFecha);
        derecha.add(metaPanel);

        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        estadoPanel.setOpaque(false);
        estadoPanel.add(Estilos.badgeEstado(fila[5]));
        derecha.add(estadoPanel);
        derecha.add(Box.createVerticalStrut(6));

        JPanel iconBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        iconBtns.setOpaque(false);
        JButton btnEdit  = Estilos.botonIcono("✎", Estilos.BTN_EDIT);
        JButton btnDel   = Estilos.botonIcono("🗑", Estilos.BTN_DELETE);
        btnEdit.addActionListener(e -> abrirEditar(fila));
        btnDel.addActionListener(e  -> eliminar(fila));
        iconBtns.add(btnEdit);
        iconBtns.add(btnDel);
        derecha.add(iconBtns);

        card.add(derecha, BorderLayout.EAST);
        return card;
    }

    // ── Acciones ─────────────────────────────────────────────────────────────

    private void abrirAgregar() {
        DialogoExperimento dlg = new DialogoExperimento(
            SwingUtilities.getWindowAncestor(this), conexion, null);
        dlg.setVisible(true);
        if (dlg.fueConfirmado()) cargar();
    }

    private void abrirEditar(String[] fila) {
        DialogoExperimento dlg = new DialogoExperimento(
            SwingUtilities.getWindowAncestor(this), conexion, fila);
        dlg.setVisible(true);
        if (dlg.fueConfirmado()) cargar();
    }

    private void eliminar(String[] fila) {
        int op = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            "¿Eliminar el experimento '" + fila[1] + "'?\nEsta acción no se puede deshacer.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            String resp = conexion.enviar(
                Protocolo.construir(Protocolo.CMD_BORRAR_EXPERIMENTO, fila[0]));
            if (resp != null && resp.startsWith(Protocolo.OK)) {
                cargar();
            } else {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    Estilos.extraerError(resp), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void placeholder(JTextField tf, String hint) {
        tf.setText(hint); tf.setForeground(Color.GRAY);
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
