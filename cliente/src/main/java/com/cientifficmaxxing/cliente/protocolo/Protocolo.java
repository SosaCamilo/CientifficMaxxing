package com.cientifficmaxxing.cliente.protocolo;

import java.util.List;

/**
 * Protocolo de aplicación del cliente — espejo exacto del Protocolo del servidor.
 *
 * FORMATO DE MENSAJE (cliente → servidor):
 *   COMANDO|param1|param2|...\n
 *   Cada parámetro pasa por codificar() antes de enviarse.
 *
 * FORMATO DE RESPUESTA (servidor → cliente):
 *   OK                                → operación exitosa sin datos
 *   OK|nuevoId                        → operación exitosa, devuelve ID generado
 *   DATOS|numCols|numFilas|h1|h2|...|r1v1|r1v2|...|rNvN  → resultado de consulta
 *   ERROR|TIPO|descripcion            → error descriptivo
 *
 * CODIFICACIÓN DE CAMPOS:
 *   '|' dentro de un valor  →  <PIPE>   (para no confundirse con el separador)
 *   '\n' dentro de un valor →  <NL>     (para no romper el delimitador de línea)
 */
public class Protocolo {

    /** Separador de campos en el protocolo. */
    public static final String SEP   = "|";
    /** Prefijo de respuesta exitosa. */
    public static final String OK    = "OK";
    /** Prefijo de respuesta de error. */
    public static final String ERROR = "ERROR";
    /** Prefijo de respuesta con tabla de datos. */
    public static final String DATOS = "DATOS";

    // ── Comandos disponibles (cliente → servidor) ────────────────────────────
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

    // ── Tipos de error que el servidor puede devolver ────────────────────────
    public static final String ERR_VALIDACION   = "VALIDACION";   // parámetros inválidos
    public static final String ERR_RESTRICCION  = "RESTRICCION";  // FK RESTRICT violada
    public static final String ERR_SQL          = "SQL";          // deadlock / lock timeout MySQL
    public static final String ERR_BD           = "BD";           // error genérico de BD
    public static final String ERR_COMANDO      = "COMANDO_DESCONOCIDO"; // cmd no existe
    public static final String ERR_ADMIN        = "ADMIN_INCORRECTO";    // contraseña mala

    private Protocolo() {}

    // ── Codificación / decodificación ────────────────────────────────────────

    /** Escapa '|' y '\n' para que no sean confundidos con separadores de protocolo. */
    public static String codificar(String valor) {
        if (valor == null) return "";
        return valor.replace("|", "<PIPE>")
                    .replace("\r", "")
                    .replace("\n", "<NL>");
    }

    /** Restaura los caracteres originales al recibir un campo ya decodificado. */
    public static String decodificar(String valor) {
        if (valor == null) return "";
        return valor.replace("<PIPE>", "|")
                    .replace("<NL>", "\n");
    }

    // ── Parseo / construcción ────────────────────────────────────────────────

    /**
     * Divide una línea recibida en campos, decodificando cada uno.
     * El campo [0] es siempre el comando o prefijo de respuesta.
     */
    public static String[] parsear(String linea) {
        if (linea == null || linea.isBlank()) return new String[0];
        String[] partes = linea.split("\\|", -1); // -1 para no descartar campos vacíos al final
        for (int i = 0; i < partes.length; i++) {
            partes[i] = decodificar(partes[i]);
        }
        return partes;
    }

    /** Une múltiples campos codificados con SEP. Uso: Protocolo.construir(CMD, p1, p2, ...) */
    public static String construir(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(SEP);
            sb.append(codificar(campos[i] != null ? campos[i] : ""));
        }
        return sb.toString();
    }

    // ── Constructores de respuesta (usados internamente o en tests) ──────────

    /** Respuesta de éxito sin datos extra. */
    public static String ok() { return OK; }

    /** Respuesta de éxito devolviendo un dato (p.ej. nuevo ID). */
    public static String ok(String dato) {
        return OK + SEP + codificar(dato);
    }

    /** Respuesta de error con tipo y descripción legible. */
    public static String error(String tipo, String descripcion) {
        return construir(ERROR, tipo, descripcion);
    }

    /**
     * Construye una respuesta DATOS a partir del List<String[]> del DAO.
     * La lista tiene índice 0 = encabezados, índices 1..N = filas de datos.
     *
     * Resultado: DATOS|numCols|numFilas|h1|h2|...|r1c1|r1c2|...|rNcN
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
