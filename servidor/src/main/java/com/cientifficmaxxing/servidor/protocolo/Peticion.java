package com.cientifficmaxxing.servidor.protocolo;

//import com.cientifficmaxxing.servidor.dao.CientifficDAO;
import com.cientifficmaxxing.servidor.dao.NCientifficDAO;
import com.cientifficmaxxing.servidor.util.Logs;

//import java.sql.SQLIntegrityConstraintViolationException;
//import java.sql.SQLTransactionRollbackException;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Capa de despacho de comandos del servidor.
 *
 * RESPONSABILIDADES:
 *   - Parsear la línea cruda del socket usando Protocolo.parsear().
 *   - Validar que los parámetros mínimos estén presentes (p.ej. IDs numéricos).
 *   - Despachar el comando al método correcto del DAO.
 *   - Capturar TODAS las excepciones y transformarlas en respuestas ERROR del protocolo,
 *     garantizando que procesar() NUNCA lanza excepciones.
 *
 * MANEJO DE ERRORES EN CASCADA:
 *   - Antes se hacian aca, ahora de a poco los estoy pasando a la clase NCientifficDAO, que tiene variables estaticas en las que se va a verificar la integridad.

 * Esta clase no sabe nada de Sockets: solo transforma String → String.
 */
public class Peticion {

    //private final CientifficDAO dao;
    private final NCientifficDAO ndao;

    public Peticion(/*CientifficDAO dao*/) {
        //this.dao = dao;
        this.ndao = new NCientifficDAO();
    }

    /**
     * Punto de entrada único.
     * Siempre devuelve una String no nula con la respuesta para el cliente.
     */
    public String procesar(String lineaRaw) {
        if (lineaRaw == null || lineaRaw.isBlank()) {
            return Protocolo.error(Protocolo.ERR_VALIDACION, "Mensaje vacío");
        }

        String[] p   = Protocolo.parsear(lineaRaw);
        String   cmd = p[0];

        try {
            return switch (cmd) {

                // ── Experimentos ──────────────────────────────────────
                case Protocolo.CMD_LISTAR_EXPERIMENTOS    -> listarExperimentos();
                case Protocolo.CMD_AGREGAR_EXPERIMENTO    -> agregarExperimento(p);
                case Protocolo.CMD_ACTUALIZAR_EXPERIMENTO -> actualizarExperimento(p);
                //case Protocolo.CMD_ACTUALIZAR_ESTADO      -> actualizarEstado(p);
                //case Protocolo.CMD_BORRAR_EXPERIMENTO     -> borrarExperimento(p);

                // ── Resultados ────────────────────────────────────────
                case Protocolo.CMD_LISTAR_RESULTADOS      -> listarResultados(p);
                case Protocolo.CMD_AGREGAR_RESULTADO      -> agregarResultado(p);

                // ── Científicos ───────────────────────────────────────
                case Protocolo.CMD_LISTAR_CIENTIFICOS     -> listarCientificos();
                case Protocolo.CMD_BUSCAR_CIENTIFICO      -> buscarCientifico(p);
                //case Protocolo.CMD_AGREGAR_CIENTIFICO     -> agregarCientifico(p);
                //case Protocolo.CMD_ACTUALIZAR_CIENTIFICO  -> actualizarCientifico(p);
                //case Protocolo.CMD_BORRAR_CIENTIFICO      -> borrarCientifico(p);

                // ── Relación Realiza ──────────────────────────────────
                case Protocolo.CMD_AGREGAR_REALIZA        -> agregarRealiza(p);
                //case Protocolo.CMD_QUITAR_REALIZA         -> quitarRealiza(p);

                // ── Administrador ─────────────────────────────────────
                case Protocolo.CMD_VERIFICAR_ADMIN        -> verificarAdmin(p);

                default -> Protocolo.error(Protocolo.ERR_COMANDO,
                               "Comando desconocido: " + cmd);
            };
// Falta cachear que el cientifico ya forme parte del equipo
// Falta cachear que el cientifico pertenece a algo (no se puede borrar)
// Falta chequear formato de fecha invalido
        } /*catch (SQLTransactionRollbackException e) {
            // Deadlock o lock timeout de MySQL: la transacción fue revertida en el SP
            Logs.error("Error SQL procesando '" + cmd + "': " + e.getMessage());
            return Protocolo.error(Protocolo.ERR_SQL,
                       "Error de transacción en la base de datos, reintentá la operación");

        } catch (SQLIntegrityConstraintViolationException e) {
            Logs.advertencia("Restricción FK en '" + cmd + "': " + e.getMessage());
            String mensaje;
            if (e.getErrorCode() == 1062) {
                // Duplicate entry: el registro ya existe (ej: científico ya en el equipo)
                if (Protocolo.CMD_AGREGAR_REALIZA.equals(cmd)) {
                    mensaje = "El científico ya forma parte del equipo de este experimento.";
                } else {
                    mensaje = "El registro ya existe.";
                }
            } else {
                // 1451: FK RESTRICT en DELETE — científico en equipo de experimento(s)
                // 1452: FK RESTRICT en INSERT — clave foránea no existe
                if (Protocolo.CMD_BORRAR_CIENTIFICO.equals(cmd)) {
                    mensaje = "El científico pertenece al equipo de uno o más experimentos. " +
                              "Primero quítelo de todos los equipos y luego intente eliminarlo.";
                } else {
                    mensaje = "No se puede eliminar: tiene registros asociados.";
                }
            }
            return Protocolo.error(Protocolo.ERR_RESTRICCION, mensaje);

        } catch (SQLException e) {
            Logs.error("Error de BD en '" + cmd + "': " + e.getMessage(), e);
            return Protocolo.error(Protocolo.ERR_BD, e.getMessage());

        } catch (RuntimeException e) {
            // Captura IllegalArgumentException de fechas mal formadas (Date.valueOf),
            // NullPointerException u otros errores de programación.
            // Sin este catch, la sesión terminaría sin responder,
            // dejando al cliente bloqueado esperando una respuesta que nunca llega.
            Logs.error("Error inesperado procesando '" + cmd + "': " + e.getMessage(), e);
            String msgError = e.getMessage();
            if (msgError == null || msgError.isBlank()) {
                msgError = "Formato de fecha inválido. Usá DD/MM/YYYY con valores correctos";
            }
            return Protocolo.error(Protocolo.ERR_VALIDACION, msgError);
        }*/
        catch (IOException e) {
            System.err.println("Error al ejecutar el Switch case");
            return Protocolo.error(Protocolo.ERR_SQL, e.getMessage());
        }
    }

    // ============================================================
    // EXPERIMENTOS
    // ============================================================

    private String listarExperimentos() throws IOException {
        return Protocolo.datos(ndao.listarExperimentos());
    }

    private String agregarExperimento(String[] p) throws IOException {
        // AGREGAR_EXPERIMENTO|fechaInicio|fechaFinal|nombre|descripcion|estado|idResponsable
        if (p.length < 7)
            return faltanParametros(p[0], "fechaInicio|fechaFinal|nombre|descripcion|estado|idResponsable");
        int idResp = parsearId(p[6]);
        if (idResp < 0) return idInvalido(p[6]);

        //int nuevoId = dao.agregarExperimento(p[1], p[2], p[3], p[4], p[5], idResp);
        int nnuevoId = -1;
        try {
            nnuevoId = ndao.agregarExperimento(p[1], p[2], p[3], p[4], p[5], idResp);
            if (nnuevoId <= 0)
            return Protocolo.error(Protocolo.ERR_BD, "No se pudo agregar el experimento");
        } catch(IOException e){
            System.err.println("Error al agregar al txt");
        }
        //if (nuevoId <= 0 || nnuevoId <=0)
        //    return Protocolo.error(Protocolo.ERR_BD, "No se pudo agregar el experimento");
            
        
        //int[] valores = {nuevoId, nnuevoId};
        agregarResponsableAlEquipo(idResp, nnuevoId);
        return Protocolo.ok(String.valueOf(nnuevoId));
        
        
    }

    private String actualizarExperimento(String[] p) throws IOException {
        // ACTUALIZAR_EXPERIMENTO|id|fechaInicio|fechaFinal|nombre|descripcion|estado|idResponsable
        if (p.length < 8)
            return faltanParametros(p[0], "id|fechaInicio|fechaFinal|nombre|descripcion|estado|idResponsable");
        int id     = parsearId(p[1]);
        int idResp = parsearId(p[7]);
        if (id < 0)     return idInvalido(p[1]);
        if (idResp < 0) return idInvalido(p[7]);

        try{
            ndao.actualizarExperimento(id, p[2], p[3], p[4], p[5], p[6], idResp);
        } catch (IOException e){
            Logs.error("Error al actualizar experimento " + id + " en CSV: " + e.getMessage(), e);
            
        }
        agregarResponsableAlEquipo(idResp, id);
        return Protocolo.ok();
    }

    /** Agrega el responsable al equipo del experimento si todavía no está. Ignora duplicados. */
    private void agregarResponsableAlEquipo(int idResp, int idExperimento) {
        /*try {
            dao.agregarRealiza(idResp, idExperimento);
            Logs.info("Responsable " + idResp + " agregado automáticamente al equipo del experimento " + idExperimento);
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // Ya estaba en el equipo — no es un error, se ignora silenciosamente
        } catch (SQLException e) {
            Logs.advertencia("No se pudo agregar al responsable " + idResp
                + " al equipo del experimento " + idExperimento + ": " + e.getMessage());
        }*/
        try {
            int agregado = ndao.agregarRealiza(idResp, idExperimento);
            if (agregado>0){
                Logs.info("Responsable " + idResp + " agregado automáticamente al equipo del experimento " + idExperimento);
            } else {
                Logs.info("Responsable " + idResp + " ya estaba en el equipo del experimento " + idExperimento);    
            }
        }catch (IOException e) {
            Logs.advertencia("No se pudo agregar al responsable " + idResp
                + " al equipo del experimento " + idExperimento + ": " + e.getMessage());
        }
    }

    //Hay que agregar esto !!!
    /*private String actualizarEstado(String[] p) throws IOException {
        // ACTUALIZAR_ESTADO|id|nuevoEstado
        if (p.length < 3) return faltanParametros(p[0], "id|estado");
        int id = parsearId(p[1]);
        if (id < 0) return idInvalido(p[1]);

        ndao.actualizarEstadoExperimento(id, p[2]);
        return Protocolo.ok();
    } */

    // Hay que agregar esto !
    /*private String borrarExperimento(String[] p) throws IOException {
        // BORRAR_EXPERIMENTO|id
        if (p.length < 2) return faltanParametros(p[0], "id");
        int id = parsearId(p[1]);
        if (id < 0) return idInvalido(p[1]);

        dao.borrarExperimento(id);
        return Protocolo.ok();
    }*/

    // ============================================================
    // RESULTADOS
    // ============================================================

    // EN PROCESO
    private String listarResultados(String[] p) throws IOException  {
        // LISTAR_RESULTADOS|idExperimento
        if (p.length < 2) return faltanParametros(p[0], "idExperimento");
        int id = parsearId(p[1]);
        if (id < 0) 
            return idInvalido(p[1]);

        return Protocolo.datos(ndao.listarResultadosPorExperimento(id));
    }

    private String agregarResultado(String[] p) throws IOException  {
        // AGREGAR_RESULTADO|fecha|descripcion|prueba|idExperimento|idPrueba
        if (p.length < 6)
            return faltanParametros(p[0], "fecha|descripcion|prueba|idExperimento|idPrueba");
        int idExp    = parsearId(p[4]);
        int idPrueba = parsearId(p[5]);
        if (idExp    < 0) return idInvalido(p[4]);
        if (idPrueba < 0) return idInvalido(p[5]);

        int nnuevoId=-1;
        try {
            nnuevoId = ndao.agregarResultado(p[1], p[2], p[3], idExp, idPrueba);
            if (nnuevoId<0){
                System.err.println("El experimento no existe o no esta en proceso");
            }
        } catch (IOException e){
            Logs.advertencia("No se pudo agregar al resultado" + nnuevoId
                + " al experimento " + idExp + ": " + e.getMessage());
        }
        return nnuevoId > 0
            ? Protocolo.ok(String.valueOf(nnuevoId))
            : Protocolo.error(Protocolo.ERR_BD, "No se pudo agregar el resultado");
    }

    // ============================================================
    // CIENTÍFICOS
    // ============================================================

    private String listarCientificos() throws IOException  {
        return Protocolo.datos(ndao.listarCientificos());
    }

    // Hay que hacer !
    private String buscarCientifico(String[] p) throws IOException  {
        // BUSCAR_CIENTIFICO|filtro  (filtro puede ser ID numérico o nombre/apellido parcial)
        if (p.length < 2) return faltanParametros(p[0], "filtro");
        String filtro = p[1].toLowerCase().trim();

        List<String[]> todos = ndao.listarCientificos();
        if (todos.isEmpty()) return Protocolo.datos(todos);

        List<String[]> resultado = new ArrayList<>();
        resultado.add(todos.get(0)); // encabezados siempre presentes

        for (int i = 1; i < todos.size(); i++) {
            String[] fila = todos.get(i);
            // fila[0]=IdCientifico  fila[1]=Nombre  fila[2]=Apellido  fila[3]=Nacimiento
            if (fila[0].equals(filtro)
                    || fila[1].toLowerCase().contains(filtro)
                    || fila[2].toLowerCase().contains(filtro)) {
                resultado.add(fila);
            }
        }
        return Protocolo.datos(resultado);
    } 
    
    // Hay que hacer !
    /*private String agregarCientifico(String[] p) throws IOException  {
        // AGREGAR_CIENTIFICO|nombre|apellido|nacimiento
        if (p.length < 4) return faltanParametros(p[0], "nombre|apellido|nacimiento");

        int nuevoId = dao.agregarCientifico(p[1], p[2], p[3]);
        return nuevoId > 0
            ? Protocolo.ok(String.valueOf(nuevoId))
            : Protocolo.error(Protocolo.ERR_BD, "No se pudo agregar el científico");
    }

    private String actualizarCientifico(String[] p) throws IOException  {
        // ACTUALIZAR_CIENTIFICO|id|nombre|apellido|nacimiento
        if (p.length < 5) return faltanParametros(p[0], "id|nombre|apellido|nacimiento");
        int id = parsearId(p[1]);
        if (id < 0) return idInvalido(p[1]);

        dao.actualizarCientifico(id, p[2], p[3], p[4]);
        return Protocolo.ok();
    } */

        
    // Hay que hacer !
    /*private String borrarCientifico(String[] p) throws IOException  {
        // BORRAR_CIENTIFICO|id
        if (p.length < 2) return faltanParametros(p[0], "id");
        int id = parsearId(p[1]);
        if (id < 0) return idInvalido(p[1]);

        if (dao.esResponsable(id)) {
            return Protocolo.error(Protocolo.ERR_RESTRICCION,
                "El científico es responsable de uno o más experimentos. " +
                "Primero cambie el responsable de esos experimentos y luego intente eliminarlo.");
        }
        dao.borrarCientifico(id);
        return Protocolo.ok();
    } */

    // ============================================================
    // RELACIÓN REALIZA
    // ============================================================

    private String agregarRealiza(String[] p) throws IOException  {
        // AGREGAR_REALIZA|idCientifico|idExperimento
        if (p.length < 3) return faltanParametros(p[0], "idCientifico|idExperimento");
        int idC = parsearId(p[1]);
        int idE = parsearId(p[2]);
        if (idC < 0) return idInvalido(p[1]);
        if (idE < 0) return idInvalido(p[2]);

        ndao.agregarRealiza(idC, idE);
        return Protocolo.ok();
    }

    // Hay que hacer !
    /*private String quitarRealiza(String[] p) throws IOException  {
        // QUITAR_REALIZA|idCientifico|idExperimento
        if (p.length < 3) return faltanParametros(p[0], "idCientifico|idExperimento");
        int idC = parsearId(p[1]);
        int idE = parsearId(p[2]);
        if (idC < 0) return idInvalido(p[1]);
        if (idE < 0) return idInvalido(p[2]);

        int filasAfectadas = ndao.quitarRealiza(idC, idE);
        if (filasAfectadas == 0) {
            return Protocolo.error(Protocolo.ERR_VALIDACION,
                "El científico no forma parte del equipo de este experimento.");
        }
        return Protocolo.ok();
    }*/

    // ============================================================
    // ADMINISTRADOR
    // ============================================================
    
    // Hay que hacer !
    private boolean validarContrasenia(String contraseniaIngresada, String contraseniaAlmacenada) {
        // Si la almacenada es null, no podemos comparar (evita NullPointerException)
        if (contraseniaAlmacenada == null) {
            return false;
        }
        // Compara ambas contraseñas y devuelve true si son exactamente iguales
        return contraseniaAlmacenada.equals(contraseniaIngresada);
    }

    private String verificarAdmin(String[] p) throws IOException {
        // VERIFICAR_ADMIN|contrasenia
        if (p.length < 2) {
            return faltanParametros(p[0], "contrasenia");
        }

        String almacenada = NCientifficDAO.obtenerContraseniaAdmin();
        String ingresada = p[1];

        // Usamos la nueva función booleana para decidir el retorno
        if (validarContrasenia(ingresada, almacenada)) {
            return Protocolo.ok();
        } else {
            return Protocolo.error(Protocolo.ERR_ADMIN, "Contraseña incorrecta");
        }
    }

    // ============================================================
    // UTILIDADES PRIVADAS
    // ============================================================

    /** Parsea un ID entero positivo. Devuelve -1 si el valor es inválido o ≤ 0. */
    private static int parsearId(String valor) {
        try {
            int v = Integer.parseInt(valor.trim());
            return (v > 0) ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String faltanParametros(String cmd, String esperados) {
        return Protocolo.error(Protocolo.ERR_VALIDACION,
            "Formato incorrecto. Esperado: " + cmd + "|" + esperados);
    }

    private static String idInvalido(String valor) {
        return Protocolo.error(Protocolo.ERR_VALIDACION,
            "ID inválido: '" + valor + "'. Debe ser un número entero positivo");
    }
}
