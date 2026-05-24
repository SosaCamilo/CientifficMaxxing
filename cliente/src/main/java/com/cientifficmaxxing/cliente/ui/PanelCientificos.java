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
 * Panel de gestión de científicos (RF06–RF09). Solo accesible con contraseña admin (RF10).
 *
 * FUNCIONES:
 *   - Listar todos los científicos (RF06) — llama LISTAR_CIENTIFICOS al abrir.
 *   - Buscar científico por ID, nombre o apellido (RF06 filtro) — búsqueda local.
 *   - Agregar científico (RF07) — abre DialogoCientifico en modo AGREGAR.
 *   - Modificar científico (RF08) — abre DialogoCientifico en modo MODIFICAR.
 *   - Eliminar científico (RF09) — pide confirmación y envía BORRAR_CIENTIFICO.
 *     Si el científico tiene experimentos asociados (FK RESTRICT), el servidor
 *     devuelve ERR_RESTRICCION y se muestra el mensaje de error.
 *
 * COLUMNAS DE SP_VerCientificos (en orden):
 *   [0]=IdCientifico  [1]=Nombre  [2]=Apellido  [3]=Nacimiento (formato "yyyy-MM-dd")
 *
 * BÚSQUEDA LOCAL:
 *   La lista completa se carga una vez desde el servidor y se guarda en todosLosCientificos.
 *   El filtro actúa sobre este array en memoria (sin ir al servidor por cada keystroke).
 */
public class PanelCientificos extends JPanel {

    private final ConexionServidor conexion;
    private String[][]             todosLosCientificos = new String[0][];

    private JPanel      listaPanel;
    private JScrollPane scroll;
    private JTextField  tfBuscar;

    public PanelCientificos(ConexionServidor conexion) {
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
        buscarPanel.add(new JLabel("🔍"));
        tfBuscar = new JTextField(22);
        tfBuscar.setFont(Estilos.FUENTE_LABEL);
        tfBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilos.SEPARADOR),
            Estilos.padding(4, 8, 4, 8)));
        tfBuscar.setToolTipText("Buscar por ID, Nombre o Apellido");
        placeholder(tfBuscar, "Buscar (ID / Nombre):");
        tfBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
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

    /** Carga científicos desde el servidor. */
    public void cargar() {
        listaPanel.removeAll();
        try {
            String resp = conexion.enviar(Protocolo.CMD_LISTAR_CIENTIFICOS);
            TablaRespuesta tabla = TablaRespuesta.parsear(resp);
            todosLosCientificos = tabla.filas;
        } catch (Exception ex) {
            todosLosCientificos = new String[0][];
            JLabel err = new JLabel("  Error al cargar científicos: " + ex.getMessage());
            err.setForeground(Estilos.BTN_CANCEL);
            listaPanel.add(err);
        }
        mostrarFilas(todosLosCientificos);
    }

    private void filtrar() {
        String filtro = tfBuscar.getText().trim().toLowerCase();
        if (filtro.equals("buscar (id / nombre):") || filtro.isEmpty()) {
            mostrarFilas(todosLosCientificos);
            return;
        }
        List<String[]> res = new ArrayList<>();
        for (String[] f : todosLosCientificos) {
            if (f[0].contains(filtro)
                    || f[1].toLowerCase().contains(filtro)
                    || f[2].toLowerCase().contains(filtro)) {
                res.add(f);
            }
        }
        mostrarFilas(res.toArray(new String[0][]));
    }

    private void mostrarFilas(String[][] filas) {
        listaPanel.removeAll();
        if (filas.length == 0) {
            JLabel vacio = new JLabel("  No se encontraron científicos.");
            vacio.setFont(Estilos.FUENTE_LABEL);
            vacio.setForeground(new Color(120, 130, 150));
            vacio.setBorder(Estilos.padding(20, 0, 0, 0));
            listaPanel.add(vacio);
        } else {
            for (String[] fila : filas) {
                listaPanel.add(buildTarjeta(fila));
                listaPanel.add(Box.createVerticalStrut(8));
            }
        }
        listaPanel.revalidate();
        listaPanel.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private JPanel buildTarjeta(String[] fila) {
        // fila: [0]=IdCientifico [1]=Nombre [2]=Apellido [3]=Nacimiento
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Estilos.CARD_SCI);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(8, 0));
        card.setOpaque(false);
        card.setBorder(Estilos.padding(10, 14, 10, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        // Contenido
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        // "Id - Apellido, Nombre"
        JLabel lblTitulo = new JLabel(fila[0] + " - " + fila[2] + ", " + fila[1]);
        lblTitulo.setFont(Estilos.FUENTE_CARD_H);
        lblTitulo.setForeground(Color.WHITE);
        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(3));

        JLabel lblNac = new JLabel("Nacimiento: " + Estilos.fechaDisplay(fila[3]));
        lblNac.setFont(Estilos.FUENTE_CARD_SUB);
        lblNac.setForeground(new Color(220, 235, 255));
        contenido.add(lblNac);

        card.add(contenido, BorderLayout.CENTER);

        // Botones editar / eliminar
        JPanel iconBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        iconBtns.setOpaque(false);

        JButton btnEdit = Estilos.botonIcono("✎", Estilos.BTN_EDIT);
        JButton btnDel  = Estilos.botonIcono("🗑", Estilos.BTN_DELETE);

        btnEdit.addActionListener(e -> abrirEditar(fila));
        btnDel.addActionListener(e  -> eliminar(fila));

        iconBtns.add(btnEdit);
        iconBtns.add(btnDel);
        card.add(iconBtns, BorderLayout.EAST);

        return card;
    }

    // ── Acciones ─────────────────────────────────────────────────────────────

    private void abrirAgregar() {
        DialogoCientifico dlg = new DialogoCientifico(
            SwingUtilities.getWindowAncestor(this), conexion,
            null, null, null, null);
        dlg.setVisible(true);
        if (dlg.fueConfirmado()) cargar();
    }

    private void abrirEditar(String[] fila) {
        DialogoCientifico dlg = new DialogoCientifico(
            SwingUtilities.getWindowAncestor(this), conexion,
            fila[0], fila[1], fila[2], fila[3]);
        dlg.setVisible(true);
        if (dlg.fueConfirmado()) cargar();
    }

    private void eliminar(String[] fila) {
        int op = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            "¿Eliminar al científico " + fila[2] + ", " + fila[1] + "?\n" +
            "No se puede eliminar si tiene experimentos asociados.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            String resp = conexion.enviar(
                Protocolo.construir(Protocolo.CMD_BORRAR_CIENTIFICO, fila[0]));
            if (resp != null && resp.startsWith(Protocolo.OK)) {
                cargar();
            } else {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    Estilos.extraerError(resp), "Error al eliminar", JOptionPane.ERROR_MESSAGE);
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
