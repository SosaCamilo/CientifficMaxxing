package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para Agregar (RF01) o Modificar un experimento existente.
 *
 * MODOS:
 *   fila == null → modo AGREGAR: envía AGREGAR_EXPERIMENTO al servidor.
 *   fila != null → modo MODIFICAR: envía ACTUALIZAR_EXPERIMENTO al servidor.
 *
 * DATOS DEL SP_VerExperimentos (columnas en orden):
 *   [0]=IdExperimento  [1]=Nombre       [2]=Descripcion  [3]=FechaInicio
 *   [4]=FechaFinal     [5]=Estado       [6]=Responsable  [7]=IdResponsable
 *   Nota: [6] es el CONCAT("Apellido, Nombre") para mostrar,
 *         [7] es el IdCientifico numérico que se usa para pre-seleccionar en el combo.
 *
 * FECHAS:
 *   Se usan tres JTextField separados (DD, MM, YYYY) porque es más claro para el usuario.
 *   Estilos.ensamblarFecha() los une en "yyyy-MM-dd" validando que sean numéricos.
 *   Estilos.descomponerFecha() hace el proceso inverso al pre-cargar un experimento existente.
 *
 * RESPONSABLE:
 *   Al abrir el diálogo, se carga la lista de científicos con LISTAR_CIENTIFICOS.
 *   Si estamos en modo MODIFICAR, se pre-selecciona el científico con fila[7] (IdResponsable).
 */
public class DialogoExperimento extends JDialog {

    private final ConexionServidor conexion;
    private final String           idExistente;
    private boolean                confirmado = false;

    private final JTextField tfNombre     = Estilos.campo(25);
    private final JTextArea  taDesc       = Estilos.area(4, 25);
    private final JTextField tfInicioDd   = Estilos.campo(3);
    private final JTextField tfInicioMm   = Estilos.campo(3);
    private final JTextField tfInicioYyyy = Estilos.campo(5);
    private final JTextField tfFinalDd    = Estilos.campo(3);
    private final JTextField tfFinalMm    = Estilos.campo(3);
    private final JTextField tfFinalYyyy  = Estilos.campo(5);

    private static final String[] ESTADOS = {
        "Sin comenzar", "En proceso", "Exitoso", "Fallido"
    };
    private final JComboBox<String>   cbEstado       = new JComboBox<>(ESTADOS);
    private       JComboBox<String[]> cbResponsable;
    private       String[][]          datosCientificos;

    public DialogoExperimento(Window owner, ConexionServidor conexion, String[] fila) {
        super(owner, fila == null ? "Cargar Experimento" : "Modificar Experimento",
              ModalityType.APPLICATION_MODAL);
        this.conexion    = conexion;
        this.idExistente = fila == null ? null : fila[0];
        buildUI();
        cargarResponsables(fila);
        if (fila != null) precargar(fila);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(520, 590));

        // Header
        JPanel header = new JPanel();
        header.setBackground(Estilos.NAVBAR_BG);
        header.setBorder(Estilos.padding(14, 24, 14, 24));
        JLabel titulo = new JLabel(idExistente == null ? "Nuevo Experimento" : "Modificar Experimento");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // Formulario — columna única, fechas lado a lado
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(Estilos.padding(16, 28, 8, 28));

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor   = GridBagConstraints.NORTHWEST;
        gc.fill     = GridBagConstraints.HORIZONTAL;
        gc.gridx    = 0;
        gc.gridwidth = 2;
        gc.weightx  = 1;

        // Separador
        gc.gridy = 0; gc.insets = new Insets(0, 0, 10, 0);
        form.add(new JSeparator(), gc);

        // Nombre
        gc.gridy = 1; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Nombre"), gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 10, 0);
        tfNombre.setPreferredSize(new Dimension(440, 34));
        form.add(tfNombre, gc);

        // Descripción
        gc.gridy = 3; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Descripción"), gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 10, 0);
        JScrollPane scrollDesc = new JScrollPane(taDesc);
        scrollDesc.setPreferredSize(new Dimension(440, 78));
        form.add(scrollDesc, gc);

        // Fechas lado a lado
        gc.gridy = 5; gc.gridwidth = 1; gc.weightx = 0.5;
        gc.gridx = 0; gc.insets = new Insets(0, 0, 4, 12);
        form.add(lbl("Fecha Inicial"), gc);
        gc.gridx = 1; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Fecha Final"), gc);

        gc.gridy = 6;
        gc.gridx = 0; gc.insets = new Insets(0, 0, 10, 12);
        form.add(buildFechaPanel(tfInicioDd, tfInicioMm, tfInicioYyyy), gc);
        gc.gridx = 1; gc.insets = new Insets(0, 0, 10, 0);
        form.add(buildFechaPanel(tfFinalDd, tfFinalMm, tfFinalYyyy), gc);

        // Estado
        gc.gridy = 7; gc.gridwidth = 2; gc.weightx = 1; gc.gridx = 0;
        gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Estado"), gc);
        gc.gridy = 8; gc.insets = new Insets(0, 0, 10, 0);
        cbEstado.setFont(Estilos.FUENTE_LABEL);
        cbEstado.setPreferredSize(new Dimension(440, 34));
        form.add(cbEstado, gc);

        // Responsable
        gc.gridy = 9; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Responsable"), gc);
        gc.gridy = 10; gc.insets = new Insets(0, 0, 6, 0);
        cbResponsable = new JComboBox<>();
        cbResponsable.setFont(Estilos.FUENTE_LABEL);
        cbResponsable.setPreferredSize(new Dimension(440, 34));
        cbResponsable.setRenderer((list, value, index, sel, focus) -> {
            JLabel l = new JLabel(value == null ? "" : value[0] + " - " + value[2] + ", " + value[1]);
            l.setOpaque(true);
            l.setBackground(sel ? Estilos.CARD_SCI : Color.WHITE);
            l.setForeground(sel ? Color.WHITE : Estilos.TEXT_DARK);
            l.setBorder(Estilos.padding(4, 8, 4, 8));
            return l;
        });
        form.add(cbResponsable, gc);

        add(form, BorderLayout.CENTER);

        // Botones lado a lado
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(Estilos.padding(10, 28, 20, 28));

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

    private JPanel buildFechaPanel(JTextField dd, JTextField mm, JTextField yyyy) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        dd.setPreferredSize(new Dimension(48, 32));
        mm.setPreferredSize(new Dimension(48, 32));
        yyyy.setPreferredSize(new Dimension(66, 32));
        dd.setHorizontalAlignment(SwingConstants.CENTER);
        mm.setHorizontalAlignment(SwingConstants.CENTER);
        yyyy.setHorizontalAlignment(SwingConstants.CENTER);
        placeholder(dd, "DD"); placeholder(mm, "MM"); placeholder(yyyy, "YYYY");
        p.add(dd); p.add(mm); p.add(yyyy);
        return p;
    }

    private void cargarResponsables(String[] filaActual) {
        try {
            String resp  = conexion.enviar(Protocolo.CMD_LISTAR_CIENTIFICOS);
            TablaRespuesta tabla = TablaRespuesta.parsear(resp);
            datosCientificos = tabla.filas;
            java.util.Arrays.sort(datosCientificos, (a, b) -> {
                try { return Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])); }
                catch (NumberFormatException e) { return a[0].compareTo(b[0]); }
            });
            cbResponsable.removeAllItems();
            for (String[] c : datosCientificos) cbResponsable.addItem(c);

            // Pre-seleccionar responsable actual si estamos editando
            if (filaActual != null) {
                String idResp = filaActual.length > 7 ? filaActual[7] : ""; // IdResponsable
                for (int i = 0; i < datosCientificos.length; i++) {
                    if (datosCientificos[i][0].equals(idResp)) {
                        cbResponsable.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar científicos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void precargar(String[] fila) {
        // fila: [0]=Id [1]=Nombre [2]=Descripcion [3]=FechaInicio [4]=FechaFinal [5]=Estado [6]=Responsable(texto) [7]=IdResp
        tfNombre.setText(fila[1]);
        taDesc.setText(fila[2]);

        String[] ini = Estilos.descomponerFecha(fila[3]);
        tfInicioDd.setText(ini[0]); tfInicioMm.setText(ini[1]); tfInicioYyyy.setText(ini[2]);

        String[] fin = Estilos.descomponerFecha(fila[4]);
        tfFinalDd.setText(fin[0]); tfFinalMm.setText(fin[1]); tfFinalYyyy.setText(fin[2]);

        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(fila[5])) { cbEstado.setSelectedIndex(i); break; }
        }
    }

    private void confirmar() {
        String nombre  = tfNombre.getText().trim();
        String desc    = taDesc.getText().trim();
        String fInicio = Estilos.ensamblarFecha(tfInicioDd, tfInicioMm, tfInicioYyyy);
        String fFinal  = Estilos.ensamblarFecha(tfFinalDd,  tfFinalMm,  tfFinalYyyy);
        String estado  = (String) cbEstado.getSelectedItem();

        if (nombre.isEmpty()) {
            error("El Nombre es obligatorio."); return;
        }
        if (fInicio == null || fFinal == null) {
            error("Fecha inválida. Verificá que el día (1-31), mes (1-12) y año (1900-2100) sean correctos."); return;
        }
        try {
            java.time.LocalDate inicio = java.time.LocalDate.parse(fInicio);
            java.time.LocalDate fin    = java.time.LocalDate.parse(fFinal);
            if (fin.isBefore(inicio)) {
                error("La fecha final no puede ser anterior a la fecha inicial."); return;
            }
        } catch (Exception e) {
            error("Fecha inválida. Verificá que el día (1-31), mes (1-12) y año (1900-2100) sean correctos."); return;
        }
        if (cbResponsable.getSelectedIndex() < 0 || datosCientificos == null) {
            error("Seleccioná un responsable."); return;
        }

        String idResp = datosCientificos[cbResponsable.getSelectedIndex()][0];

        try {
            String peticion;
            if (idExistente == null) {
                peticion = Protocolo.construir(Protocolo.CMD_AGREGAR_EXPERIMENTO,
                               fInicio, fFinal, nombre, desc, estado, idResp);
            } else {
                peticion = Protocolo.construir(Protocolo.CMD_ACTUALIZAR_EXPERIMENTO,
                               idExistente, fInicio, fFinal, nombre, desc, estado, idResp);
            }
            String resp = conexion.enviar(peticion);
            if (resp != null && resp.startsWith(Protocolo.OK)) {
                // Determinar el ID del experimento (nuevo o existente)
                String[] partes = Protocolo.parsear(resp);
                String idExp = idExistente != null ? idExistente
                                                   : (partes.length >= 2 ? partes[1] : null);
                // Agregar al responsable al equipo automáticamente (ignorar si ya está)
                if (idExp != null && !idExp.isEmpty()) {
                    try {
                        conexion.enviar(Protocolo.construir(
                            Protocolo.CMD_AGREGAR_REALIZA, idResp, idExp));
                    } catch (Exception ignored) {}
                }
                confirmado = true;
                dispose();
            } else {
                error(Estilos.extraerError(resp));
            }
        } catch (Exception ex) {
            error("Error de conexión: " + ex.getMessage());
        }
    }

    public boolean fueConfirmado() { return confirmado; }

    private static JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Estilos.FUENTE_LABEL);
        l.setForeground(Estilos.TEXT_DARK);
        return l;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.WARNING_MESSAGE);
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
