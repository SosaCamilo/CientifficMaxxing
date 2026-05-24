package com.cientifficmaxxing.cliente.ui;

import javax.swing.*;
import java.awt.*;

public class PanelBienvenida extends JPanel {

    public PanelBienvenida() {
        setBackground(Estilos.FONDO);
        setLayout(new BorderLayout());
        add(buildContenido(), BorderLayout.CENTER);
    }

    private JScrollPane buildContenido() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(Estilos.padding(44, 60, 44, 60));

        // ── Encabezado ────────────────────────────────────────────────────────
        JLabel titulo = new JLabel("Bienvenido a CientifficMaxxing");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 32));
        titulo.setForeground(Estilos.NAVBAR_BG);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(8));

        JLabel version = new JLabel("v1.1  ·  Lanzamiento: 20/04/2026");
        version.setFont(new Font("SansSerif", Font.PLAIN, 13));
        version.setForeground(Estilos.TEXT_MUTED);
        version.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(version);
        panel.add(Box.createVerticalStrut(28));

        panel.add(separador());
        panel.add(Box.createVerticalStrut(28));

        // ── Descripción ───────────────────────────────────────────────────────
        JLabel descTitulo = new JLabel("¿Qué es CientifficMaxxing?");
        descTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        descTitulo.setForeground(Estilos.TEXT_DARK);
        descTitulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descTitulo);
        panel.add(Box.createVerticalStrut(10));

        String[] descripcion = {
            "CientifficMaxxing es un software de gestión de laboratorio científico que le permite",
            "organizar experimentos, registrar resultados parciales y administrar los equipos",
            "de investigadores de manera eficiente y centralizada."
        };
        for (String linea : descripcion) {
            JLabel lbl = new JLabel(linea);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lbl.setForeground(Estilos.TEXT_DARK);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(lbl);
            panel.add(Box.createVerticalStrut(3));
        }

        panel.add(Box.createVerticalStrut(28));
        panel.add(separador());
        panel.add(Box.createVerticalStrut(28));

        // ── Alcance ───────────────────────────────────────────────────────────
        JLabel alcanceTitulo = new JLabel("Alcance de la aplicación");
        alcanceTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        alcanceTitulo.setForeground(Estilos.TEXT_DARK);
        alcanceTitulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(alcanceTitulo);
        panel.add(Box.createVerticalStrut(12));

        String[][] alcance = {
            {"Experimentos",  "Cargue, modifique, elimine y consulte experimentos con fechas, estado y responsable."},
            {"Resultados",    "Registre resultados parciales con fecha, descripción y tipo de prueba asociada."},
            {"Equipo",        "Asigne o quite científicos del equipo de trabajo de cada experimento."},
            {"Estado",        "Actualice el estado de un experimento: Sin comenzar, En proceso, Exitoso o Fallido."},
            {"Científicos",   "Administre el padrón de científicos disponibles (requiere contraseña de administrador)."}
        };
        for (String[] item : alcance) {
            panel.add(buildItemAlcance(item[0], item[1]));
            panel.add(Box.createVerticalStrut(8));
        }

        panel.add(Box.createVerticalStrut(28));
        panel.add(separador());
        panel.add(Box.createVerticalStrut(28));

        // ── Cómo navegar ──────────────────────────────────────────────────────
        JLabel navTitulo = new JLabel("Cómo moverse en la aplicación");
        navTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        navTitulo.setForeground(Estilos.TEXT_DARK);
        navTitulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(navTitulo);
        panel.add(Box.createVerticalStrut(12));

        String[][] pasos = {
            {"Barra superior",  "Use los botones Inicio, Experimentos y Científicos para cambiar de sección."},
            {"Tarjetas",        "Cada experimento y científico se muestra como una tarjeta con sus datos principales."},
            {"+ Resultado",     "Abre el formulario para agregar un resultado parcial a ese experimento."},
            {"Resultados",      "Muestra el historial de resultados registrados para ese experimento."},
            {"Equipo",          "Permite agregar o quitar científicos del equipo de un experimento."},
            {"Estado",          "Cambia el estado actual del experimento."},
            {"✎ / 🗑",          "Los íconos azul y rojo en cada tarjeta permiten editar o eliminar el registro."},
            {"Buscar",          "Use la barra de búsqueda (por ID o nombre) para filtrar la lista en tiempo real."},
            {"Científicos",     "El acceso a esta sección requiere la contraseña de administrador."}
        };
        for (String[] paso : pasos) {
            panel.add(buildItemGuia(paso[0], paso[1]));
            panel.add(Box.createVerticalStrut(6));
        }

        panel.add(Box.createVerticalStrut(32));

        // ── Pie ───────────────────────────────────────────────────────────────
        JLabel pie = new JLabel("CientifficMaxxing v1.1 — Todos los derechos reservados © 2026");
        pie.setFont(new Font("SansSerif", Font.ITALIC, 11));
        pie.setForeground(Estilos.TEXT_MUTED);
        pie.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(pie);

        JScrollPane scroll = new JScrollPane(panel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Estilos.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildItemAlcance(String etiqueta, String descripcion) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel badge = new JLabel("  " + etiqueta + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Estilos.CARD_EXP);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(100, 22));

        JLabel desc = new JLabel("  " + descripcion);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(Estilos.TEXT_DARK);

        fila.add(badge);
        fila.add(desc);
        return fila;
    }

    private JPanel buildItemGuia(String etiqueta, String descripcion) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(etiqueta + ": ");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(Estilos.NAVBAR_BG);
        lbl.setPreferredSize(new Dimension(130, 20));

        JLabel desc = new JLabel(descripcion);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(Estilos.TEXT_DARK);

        fila.add(lbl);
        fila.add(desc);
        return fila;
    }

    private JSeparator separador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Estilos.SEPARADOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }
}
