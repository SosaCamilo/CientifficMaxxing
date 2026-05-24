package com.cientifficmaxxing.cliente.ui;

import com.cientifficmaxxing.cliente.protocolo.Protocolo;

/**
 * Parsea una respuesta DATOS del servidor en encabezados + filas.
 *
 * FORMATO ESPERADO:
 *   DATOS|numCols|numFilas|h1|h2|...|r1v1|r1v2|...|rNvN
 *
 *   Ejemplo con 3 columnas y 2 filas:
 *   DATOS|3|2|IdExp|Nombre|Estado|1|Experimento A|En proceso|2|Experimento B|Exitoso
 *
 * ACCESO A LOS DATOS:
 *   TablaRespuesta t = TablaRespuesta.parsear(respuestaDelServidor);
 *   t.encabezados[0]   → "IdExp"
 *   t.filas[0][1]      → "Experimento A"
 *   t.isEmpty()        → true si no hay filas de datos
 *
 * El método parsear() nunca devuelve null y nunca lanza excepciones — ante cualquier
 * respuesta malformada devuelve una TablaRespuesta vacía.
 */
public class TablaRespuesta {

    public final String[]   encabezados; // nombres de columna (primera fila del resultado)
    public final String[][] filas;       // datos [filaIndex][colIndex]

    private TablaRespuesta(String[] encabezados, String[][] filas) {
        this.encabezados = encabezados;
        this.filas       = filas;
    }

    /** True si el servidor devolvió 0 filas de datos. */
    public boolean isEmpty() { return filas.length == 0; }
    /** Cantidad de filas de datos (sin contar encabezados). */
    public int count()       { return filas.length; }

    /** Parsea la respuesta cruda del servidor. Nunca devuelve null. */
    public static TablaRespuesta parsear(String respuesta) {
        if (respuesta == null) return vacia();
        String[] p = Protocolo.parsear(respuesta);
        if (p.length == 0 || !Protocolo.DATOS.equals(p[0])) return vacia();

        int numCols  = p.length > 1 ? enteroSeguro(p[1]) : 0;
        int numFilas = p.length > 2 ? enteroSeguro(p[2]) : 0;
        if (numCols == 0) return vacia();

        String[] headers = new String[numCols];
        for (int i = 0; i < numCols; i++) {
            int idx = 3 + i;
            headers[i] = idx < p.length ? p[idx] : "";
        }

        String[][] filas = new String[numFilas][numCols];
        int inicio = 3 + numCols;
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numCols; j++) {
                int idx = inicio + i * numCols + j;
                filas[i][j] = idx < p.length ? p[idx] : "";
            }
        }
        return new TablaRespuesta(headers, filas);
    }

    private static TablaRespuesta vacia() {
        return new TablaRespuesta(new String[0], new String[0][0]);
    }

    private static int enteroSeguro(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
