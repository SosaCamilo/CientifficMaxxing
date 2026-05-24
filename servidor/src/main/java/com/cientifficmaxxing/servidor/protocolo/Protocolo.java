package com.cientifficmaxxing.servidor.protocolo;

import java.util.List;

/**
 * Protocolo de aplicación sobre TCP para CientifficMaxxing.
 *
 * ── FORMATO DE MENSAJE (cliente → servidor) ──────────────────
 *   COMANDO|param1|param2|...\n
 *
 * ── FORMATO DE RESPUESTA (servidor → cliente) ────────────────
 *   OK                              → éxito sin datos
 *   OK|nuevoId                      → éxito, devuelve ID generado
 *   DATOS|numCols|numFilas|h1|h2|...|r1v1|r1v2|...|rNvN
 *                                   → resultado de consulta
 *   ERROR|TIPO|descripcion          → error descriptivo
 *
 * ── CODIFICACIÓN DE CAMPOS ───────────────────────────────────
 *   '|' dentro de un valor  →  <PIPE>
 *   '\n' dentro de un valor →  <NL>
 *   Esto garantiza que el separador '|' siempre es un separador real.
 */
public class Protocolo {

    /** Separador de campos en el protocolo. */
    public static final String SEP = "|";

    // ---- Prefijos de respuesta ---------------------------------
    public static final String OK    = "OK";
    public static final String ERROR = "ERROR";
    public static final String DATOS = "DATOS";

    // ---- Comandos (cliente → servidor) -------------------------
    public static final String CMD_LISTAR_EXPERIMENTOS    = "LISTAR_EXPERIMENTOS";
    public static final String CMD_AGREGAR_EXPERIMENTO    = "AGREGAR_EXPERIMENTO";
    public static final String CMD_ACTUALIZAR_EXPERIMENTO = "ACTUALIZAR_EXPERIMENTO";
    public static final String CMD_ACTUALIZAR_ESTADO      = "ACTUALIZAR_ESTADO";
    public static final String CMD_BORRAR_EXPERIMENTO     = "BORRAR_EXPERIMENTO";

    public static final String CMD_LISTAR_RESULTADOS      = "LISTAR_RESULTADOS";
    public static final String CMD_AGREGAR_RESULTADO      = "AGREGAR_RESULTADO";

    public static final String CMD_LISTAR_CIENTIFICOS     = "LISTAR_CIENTIFICOS";
    public static final String CMD_BUSCAR_CIENTIFICO      = "BUSCAR_CIENTIFICO";
    public static final String CMD_AGREGAR_CIENTIFICO     = "AGREGAR_CIENTIFICO";
    public static final String CMD_ACTUALIZAR_CIENTIFICO  = "ACTUALIZAR_CIENTIFICO";
    public static final String CMD_BORRAR_CIENTIFICO      = "BORRAR_CIENTIFICO";

    public static final String CMD_AGREGAR_REALIZA        = "AGREGAR_REALIZA";
    public static final String CMD_QUITAR_REALIZA         = "QUITAR_REALIZA";

    public static final String CMD_VERIFICAR_ADMIN        = "VERIFICAR_ADMIN";

    // ---- Tipos de error ----------------------------------------
    public static final String ERR_VALIDACION   = "VALIDACION";
    public static final String ERR_RESTRICCION  = "RESTRICCION";
    public static final String ERR_SQL          = "SQL";
    public static final String ERR_BD           = "BD";
    public static final String ERR_COMANDO      = "COMANDO_DESCONOCIDO";
    public static final String ERR_ADMIN        = "ADMIN_INCORRECTO";

    private Protocolo() {}

    // ============================================================
    // CODIFICACIÓN / DECODIFICACIÓN
    // ============================================================

    /**
     * Escapa los caracteres especiales de un campo antes de enviarlo.
     * '|' → "<PIPE>",  '\n'/'\r' → "<NL>"
     */
    public static String codificar(String valor) {
        if (valor == null) return "";
        return valor.replace("|", "<PIPE>")
                    .replace("\r", "")
                    .replace("\n", "<NL>");
    }

    /**
     * Restaura los caracteres originales al recibir un campo.
     */
    public static String decodificar(String valor) {
        if (valor == null) return "";
        return valor.replace("<PIPE>", "|")
                    .replace("<NL>", "\n");
    }

    // ============================================================
    // PARSEO DE MENSAJES ENTRANTES
    // ============================================================

    /**
     * Divide una línea recibida en campos usando '|' como separador.
     * Decodifica cada campo antes de devolverlo.
     * El campo [0] siempre es el comando/prefijo.
     */
    public static String[] parsear(String linea) {
        if (linea == null || linea.isBlank()) return new String[0];
        String[] partes = linea.split("\\|", -1);
        for (int i = 0; i < partes.length; i++) {
            partes[i] = decodificar(partes[i]);
        }
        return partes;
    }
    public static String[] parsearCSV(String linea) {
        if (linea == null || linea.isBlank()) return new String[0];
        String[] partes = linea.split(",", -1);
        return partes;
    }

    // ============================================================
    // CONSTRUCTORES DE RESPUESTA
    // ============================================================

    /** Une campos codificados con SEP. */
    public static String construir(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(SEP);
            sb.append(codificar(campos[i] != null ? campos[i] : ""));
        }
        return sb.toString();
    }

    /** Respuesta de éxito sin datos. */
    public static String ok() {
        return OK;
    }

    /** Respuesta de éxito con un dato (ej: nuevo ID). */
    public static String ok(String dato) {
        return OK + SEP + codificar(dato);
    }

    /** Respuesta de error con tipo y descripción. */
    public static String error(String tipo, String descripcion) {
        return construir(ERROR, tipo, descripcion);
    }

    /**
     * Construye una respuesta DATOS a partir del List<String[]> del DAO.
     *
     * La lista tiene el índice 0 = encabezados, índices 1..N = filas de datos.
     *
     * Formato resultante:
     *   DATOS|numCols|numFilas|h1|h2|...|r1c1|r1c2|...|rNcN
     */
    public static String datos(List<String[]> tabla) {
        if (tabla == null || tabla.isEmpty()) {
            return DATOS + SEP + "0" + SEP + "0";
        }

        String[] encabezados = tabla.get(0);
        int numCols  = encabezados.length;
        int numFilas = tabla.size() - 1;

        StringBuilder sb = new StringBuilder();
        sb.append(DATOS).append(SEP).append(numCols).append(SEP).append(numFilas);

        for (String h : encabezados)
            sb.append(SEP).append(codificar(h));

        for (int i = 1; i < tabla.size(); i++)
            for (String v : tabla.get(i))
                sb.append(SEP).append(codificar(v != null ? v : ""));

        return sb.toString();
    }
}
