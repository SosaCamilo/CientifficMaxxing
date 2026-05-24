package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para gestionar el equipo de un experimento (tabla Realiza).
 *
 * La tabla Realiza es una relación N:M entre Cientifico y Experimento:
 *   - AGREGAR_REALIZA → SP_AgregarRealiza(idCientifico, idExperimento)
 *   - QUITAR_REALIZA  → SP_QuitarRealiza(idCientifico, idExperimento)
 *
 * El combo muestra todos los científicos. El usuario elige uno y presiona
 * "+ Agregar al equipo" o "- Quitar del equipo". La respuesta del servidor
 * indica éxito o error (p.ej. si intenta agregar a alguien ya en el equipo,
 * la FK UNIQUE lanza ERR_RESTRICCION).
 */
public class DialogoRealiza extends JDialog {

    private final ConexionServidor conexion;
    private final String           idExperimento;
    private final String           idResponsable;
    private boolean                hubosCambios = false;

    private JComboBox<String[]> cbCientificos;
    private String[][]          datosCientificos;

    public DialogoRealiza(Window owner, ConexionServidor conexion,
                          String idExperimento, String nombreExperimento,
                          String idResponsable) {
        super(owner, "Gestionar Equipo del Experimento", ModalityType.APPLICATION_MODAL);
        this.conexion      = conexion;
        this.idExperimento = idExperimento;
        this.idResponsable = idResponsable == null ? "" : idResponsable;
        buildUI(nombreExperimento);
        cargarCientificos();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI(String nombreExperimento) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(430, 340));

        // Header
        JPanel header = new JPanel();
        header.setBackground(Estilos.NAVBAR_BG);
        header.setBorder(Estilos.padding(14, 24, 14, 24));
        JLabel titulo = new JLabel("Gestionar Equipo");
        titulo.setFont(new Font("SansSerif", Font.PLAIN, 18));
        titulo.setForeground(new Color(180, 200, 230));
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // Cuerpo
        JPanel cuerpo = new JPanel();
        cuerpo.setBackground(Color.WHITE);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBorder(Estilos.padding(20, 30, 10, 30));

        JLabel lblExp = new JLabel("Experimento: " + nombreExperimento);
        lblExp.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblExp.setForeground(Estilos.TEXT_DARK);
        lblExp.setAlignmentX(LEFT_ALIGNMENT);
        cuerpo.add(lblExp);
        cuerpo.add(Box.createVerticalStrut(16));

        JLabel lblSel = new JLabel("Seleccionar Científico:");
        lblSel.setFont(Estilos.FUENTE_LABEL);
        lblSel.setForeground(Estilos.TEXT_DARK);
        lblSel.setAlignmentX(LEFT_ALIGNMENT);
        cuerpo.add(lblSel);
        cuerpo.add(Box.createVerticalStrut(6));

        cbCientificos = new JComboBox<>();
        cbCientificos.setFont(Estilos.FUENTE_LABEL);
        cbCientificos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbCientificos.setAlignmentX(LEFT_ALIGNMENT);
        cuerpo.add(cbCientificos);
        cuerpo.add(Box.createVerticalStrut(20));

        // Botones de acción
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnPanel.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnAgregar = Estilos.boton("+ Agregar al equipo", Estilos.BTN_CONFIRM);
        JButton btnQuitar  = Estilos.boton("- Quitar del equipo",  Estilos.BTN_CANCEL);

        btnAgregar.addActionListener(e -> enviarRealiza(Protocolo.CMD_AGREGAR_REALIZA));
        btnQuitar.addActionListener(e  -> enviarRealiza(Protocolo.CMD_QUITAR_REALIZA));

        btnPanel.add(btnAgregar);
        btnPanel.add(btnQuitar);
        cuerpo.add(btnPanel);

        add(cuerpo, BorderLayout.CENTER);

        // Botón cerrar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(Estilos.padding(0, 20, 12, 20));
        JButton btnCerrar = Estilos.boton("Cerrar", new Color(120, 140, 170));
        btnCerrar.setPreferredSize(new Dimension(100, 36));
        btnCerrar.addActionListener(e -> dispose());
        footer.add(btnCerrar);
        add(footer, BorderLayout.SOUTH);
    }

    private void cargarCientificos() {
        try {
            String resp = conexion.enviar(Protocolo.CMD_LISTAR_CIENTIFICOS);
            TablaRespuesta tabla = TablaRespuesta.parsear(resp);
            datosCientificos = tabla.filas;
            java.util.Arrays.sort(datosCientificos, (a, b) -> {
                try { return Integer.compare(Integer.parseInt(a[0]), Integer.parseInt(b[0])); }
                catch (NumberFormatException e) { return a[0].compareTo(b[0]); }
            });
            cbCientificos.removeAllItems();
            for (String[] fila : datosCientificos) {
                // fila: [IdCientifico, Nombre, Apellido, Nacimiento]
                cbCientificos.addItem(fila);
            }
            cbCientificos.setRenderer((list, value, index, isSelected, hasFocus) -> {
                JLabel lbl = new JLabel();
                if (value != null) {
                    lbl.setText(value[0] + " - " + value[2] + ", " + value[1]);
                }
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? Estilos.CARD_SCI : Color.WHITE);
                lbl.setForeground(isSelected ? Color.WHITE : Estilos.TEXT_DARK);
                lbl.setBorder(Estilos.padding(4, 8, 4, 8));
                return lbl;
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar científicos: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarRealiza(String comando) {
        int sel = cbCientificos.getSelectedIndex();
        if (sel < 0 || datosCientificos == null || sel >= datosCientificos.length) {
            JOptionPane.showMessageDialog(this, "Seleccioná un científico primero.",
                "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idCientifico = datosCientificos[sel][0];
        if (Protocolo.CMD_QUITAR_REALIZA.equals(comando)
                && !idResponsable.isEmpty()
                && idCientifico.equals(idResponsable)) {
            JOptionPane.showMessageDialog(this,
                "No se puede quitar al responsable del equipo.\n" +
                "Primero asigná otro responsable al experimento y luego quitá a este científico.",
                "Operación no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String peticion  = Protocolo.construir(comando, idCientifico, idExperimento);
            String respuesta = conexion.enviar(peticion);
            if (respuesta != null && respuesta.startsWith(Protocolo.OK)) {
                hubosCambios = true;
                String accion = comando.equals(Protocolo.CMD_AGREGAR_REALIZA) ? "agregado" : "quitado";
                JOptionPane.showMessageDialog(this,
                    "Científico " + accion + " correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, Estilos.extraerError(respuesta),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean hubosCambios() { return hubosCambios; }
}
