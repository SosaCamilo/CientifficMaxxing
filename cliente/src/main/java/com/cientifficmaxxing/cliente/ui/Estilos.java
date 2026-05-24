package com.cientifficmaxxing.cliente.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Estilos {

    // Teniamos problemas con los colores de background y foreground en linux. Esto lo soluciona.
    static {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception e) {
            // fallback silencioso
        }
    }

    // ── Paleta ──────────────────────────────────────────────────────────────
    public static final Color NAVBAR_BG      = new Color(30, 55, 100);
    public static final Color FONDO          = new Color(240, 244, 252);
    public static final Color CARD_EXP       = new Color(56, 168, 158);
    public static final Color CARD_SCI       = new Color(66, 120, 200);
    public static final Color CARD_RES       = new Color(66, 120, 200);
    public static final Color BTN_CONFIRM    = new Color(56, 168, 158);
    public static final Color BTN_CANCEL     = new Color(220, 90, 90);
    public static final Color BTN_EDIT       = new Color(55, 110, 220);
    public static final Color BTN_DELETE     = new Color(220, 90, 90);
    public static final Color BTN_NEW        = new Color(60, 165, 75);
    public static final Color BTN_ACTION     = new Color(56, 168, 158);
    public static final Color TEXT_WHITE     = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(22, 32, 54);
    public static final Color TEXT_MUTED     = new Color(110, 125, 155);
    public static final Color SEPARADOR      = new Color(210, 218, 235);
    public static final Color FONDO_FORM     = new Color(252, 253, 255);

    // ── Fuentes ─────────────────────────────────────────────────────────────
    public static final Font FUENTE_TITULO      = new Font("SansSerif", Font.BOLD,  24);
    public static final Font FUENTE_CARD_H      = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FUENTE_CARD_SUB    = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_BTN         = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FUENTE_LABEL       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_LABEL_BOLD  = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FUENTE_SMALL       = new Font("SansSerif", Font.PLAIN, 11);

    private Estilos() {}

    // ── Fábrica de botones redondeados ──────────────────────────────────────

    public static JButton boton(String texto, Color bg) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()  ? bg.darker()   :
                          getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bg);
        btn.setFont(FUENTE_BTN);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton botonIcono(String simbolo, Color bg) {
        JButton btn = boton(simbolo, bg);
        btn.setPreferredSize(new Dimension(36, 30));
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bg);
        return btn;
    }

    public static JButton botonCard(String texto, Color bg) {
        JButton btn = boton(texto, bg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(120, 28));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(bg);
        return btn;
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    // ── Utilidades de fecha ─────────────────────────────────────────────────

    public static String fechaDisplay(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isEmpty()) return "";
        String[] p = yyyyMmDd.split("-");
        if (p.length < 3) return yyyyMmDd;
        return p[2] + "/" + p[1] + "/" + p[0];
    }

    public static String ensamblarFecha(JTextField dd, JTextField mm, JTextField yyyy) {
        String d = dd.getText().trim();
        String m = mm.getText().trim();
        String y = yyyy.getText().trim();
        if (d.isEmpty() || m.isEmpty() || y.isEmpty()) return null;
        try {
            int day   = Integer.parseInt(d);
            int month = Integer.parseInt(m);
            int year  = Integer.parseInt(y);
            if (day < 1 || day > 31)        return null;
            if (month < 1 || month > 12)    return null;
            if (year < 1900 || year > 2100) return null;
        } catch (NumberFormatException e) {
            return null;
        }
        if (d.length() == 1) d = "0" + d;
        if (m.length() == 1) m = "0" + m;
        return y + "-" + m + "-" + d;
    }

    public static String[] descomponerFecha(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isEmpty()) return new String[]{"", "", ""};
        String[] p = yyyyMmDd.split("-");
        if (p.length < 3) return new String[]{"", "", ""};
        return new String[]{p[2], p[1], p[0]};
    }

    public static String extraerError(String respuesta) {
        if (respuesta == null) return "Sin respuesta del servidor";
        String[] p = respuesta.split("\\|", -1);
        if (p.length >= 3) return p[2].replace("<PIPE>", "|").replace("<NL>", "\n");
        return respuesta;
    }

    public static JTextField campo(int columnas) {
        JTextField tf = new JTextField(columnas);
        tf.setFont(FUENTE_LABEL);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(FONDO_FORM);
        tf.setCaretColor(TEXT_DARK);
        tf.setOpaque(true);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SEPARADOR, 1),
            new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    public static JTextArea area(int filas, int columnas) {
        JTextArea ta = new JTextArea(filas, columnas);
        ta.setFont(FUENTE_LABEL);
        ta.setForeground(TEXT_DARK);
        ta.setBackground(FONDO_FORM);
        ta.setCaretColor(TEXT_DARK);
        ta.setOpaque(true);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SEPARADOR, 1),
            new EmptyBorder(5, 10, 5, 10)));
        return ta;
    }

    /** Crea un badge de color para el estado de un experimento. */
    public static JLabel badgeEstado(String estado) {
        Color bg;
        if ("Exitoso".equals(estado))         bg = new Color(60, 165, 75);
        else if ("Fallido".equals(estado))    bg = new Color(220, 90, 90);
        else if ("En proceso".equals(estado)) bg = new Color(230, 155, 30);
        else                                   bg = new Color(130, 140, 165);

        JLabel badge = new JLabel("  " + estado + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBackground(bg);
        badge.setOpaque(false);
        return badge;
    }
}