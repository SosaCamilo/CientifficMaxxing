package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.ConexionServidor;
import com.cientifficmaxxing.cliente.protocolo.Protocolo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaPrincipal extends JFrame {

    private final ConexionServidor  conexion;
    private boolean                 adminVerificado = false;

    private final PanelBienvenida   panelBienvenida;
    private final PanelExperimentos panelExperimentos;
    private final PanelCientificos  panelCientificos;
    private final CardLayout        cardLayout = new CardLayout();
    private final JPanel            contenido  = new JPanel(cardLayout);

    private JButton btnExperimentos;
    private JButton btnCientificos;

    public VentanaPrincipal(ConexionServidor conexion) {
        this.conexion          = conexion;
        this.panelBienvenida   = new PanelBienvenida();
        this.panelExperimentos = new PanelExperimentos(conexion);
        this.panelCientificos  = new PanelCientificos(conexion);

        setTitle("CientifficMaxxing");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1060, 680);
        setMinimumSize(new Dimension(820, 550));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                conexion.cerrar();
                System.exit(0);
            }
        });

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildNavbar(), BorderLayout.NORTH);

        contenido.add(panelBienvenida,   "bienvenida");
        contenido.add(panelExperimentos, "experimentos");
        contenido.add(panelCientificos,  "cientificos");
        cardLayout.show(contenido, "bienvenida");
        add(contenido, BorderLayout.CENTER);
    }

    // ── Barra de navegación ──────────────────────────────────────────────────

    private JPanel buildNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Estilos.NAVBAR_BG);
        navbar.setPreferredSize(new Dimension(0, 58));
        navbar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(20, 40, 80)));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 11));
        izq.setOpaque(false);

        JLabel logo = buildLogo();
        JLabel titulo = buildTituloNavbar();

        MouseAdapter irInicio = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                cardLayout.show(contenido, "bienvenida");
                marcarActivo(null);
            }
        };
        logo.addMouseListener(irInicio);
        titulo.addMouseListener(irInicio);
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titulo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.setToolTipText("Ir al inicio");
        titulo.setToolTipText("Ir al inicio");

        izq.add(logo);
        izq.add(titulo);
        navbar.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 11));
        der.setOpaque(false);

        btnExperimentos = buildNavBtn("Experimentos");
        btnCientificos  = buildNavBtn("Científicos");

        btnExperimentos.addActionListener(e -> mostrarExperimentos());
        btnCientificos.addActionListener(e  -> mostrarCientificos());

        der.add(btnExperimentos);
        der.add(btnCientificos);
        navbar.add(der, BorderLayout.EAST);

        return navbar;
    }

    private JLabel buildTituloNavbar() {
        JLabel lbl = new JLabel("<html>"
            + "<span style='color:white;font-size:14pt;font-weight:normal;letter-spacing:1px;'>Cientific&nbsp;</span>"
            + "<span style='color:white;font-size:14pt;font-weight:bold;'>Maxxing</span>"
            + "</html>");
        return lbl;
    }

    private JLabel buildLogo() {
        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 120, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(100, 235, 170));
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] px = {4, 9, 15, 21, 27};
                int[] py = {24, 20, 26, 13, 8};
                for (int i = 1; i < px.length; i++) g2.drawLine(px[i-1], py[i-1], px[i], py[i]);
                g2.setColor(new Color(100, 235, 170));
                for (int i = 0; i < px.length; i++) g2.fillOval(px[i]-3, py[i]-3, 6, 6);
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(36, 36));
        return logo;
    }

    private JButton buildNavBtn(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(15, 35, 70)   :
                           getModel().isRollover() ? new Color(50, 90, 155)  :
                           new Color(40, 75, 135);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Cambio de vistas ─────────────────────────────────────────────────────

    private void mostrarExperimentos() {
        panelExperimentos.cargar();
        cardLayout.show(contenido, "experimentos");
        marcarActivo(btnExperimentos);
    }

    private void mostrarCientificos() {
        if (!adminVerificado) {
            JPasswordField pf = new JPasswordField(16);
            pf.setFont(Estilos.FUENTE_LABEL);
            Object[] msg = {"Ingresá la contraseña de administrador:", pf};
            int op = JOptionPane.showConfirmDialog(this, msg,
                "Acceso Admin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op != JOptionPane.OK_OPTION) return;

            String pass = new String(pf.getPassword());
            try {
                String resp = conexion.enviar(
                    Protocolo.construir(Protocolo.CMD_VERIFICAR_ADMIN, pass));
                if (resp == null || !resp.startsWith(Protocolo.OK)) {
                    JOptionPane.showMessageDialog(this,
                        "Contraseña incorrecta. Acceso denegado.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                adminVerificado = true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        panelCientificos.cargar();
        cardLayout.show(contenido, "cientificos");
        marcarActivo(btnCientificos);
    }

    private void marcarActivo(JButton activo) {
        for (JButton b : new JButton[]{btnExperimentos, btnCientificos}) {
            b.setFont(new Font("SansSerif",
                b == activo ? Font.BOLD : Font.PLAIN, 13));
        }
    }
}
