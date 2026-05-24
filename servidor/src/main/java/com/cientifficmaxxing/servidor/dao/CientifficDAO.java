package com.cientifficmaxxing.servidor.dao;

import com.cientifficmaxxing.servidor.util.Logs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Capa de acceso a datos del servidor.
 *
 * DISEÑO:
 *   - Todos los métodos llaman a Stored Procedures con CallableStatement.
 *   - Cada instancia recibe una Connection propia del cliente → sin conflictos entre sesiones.
 *   - Las transacciones (START TRANSACTION / COMMIT / ROLLBACK) están DENTRO de cada SP
 *     en la BD, no en código Java → MySQL garantiza la atomicidad.
 *
 * INTEGRIDAD CON MYSQL InnoDB:
 *   - InnoDB usa row-level locking: si dos operaciones intentan modificar la misma fila,
 *     la segunda espera al COMMIT de la primera (no hay lecturas sucias).
 *   - Si el tiempo de espera supera innodb_lock_wait_timeout, MySQL lanza
 *     SQLTransactionRollbackException (deadlock/timeout), que ManejadorPeticiones captura.
 *
 * FORMATO DE RETORNO para consultas (SELECT):
 *   List<String[]> donde índice 0 = encabezados de columna,
 *                           índices 1..N = filas de datos (todo String).
 */
public class CientifficDAO {

    private final Connection conexion;

    public CientifficDAO(Connection conexion) {
        this.conexion = conexion;
    }

    // =========================================================
    // CONSULTAS (solo lectura, sin transacción explícita)
    // =========================================================

    public List<String[]> listarExperimentos() throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_VerExperimentos()}")) {
            cs.execute();
            return toLista(cs.getResultSet());
        }
    }

    public List<String[]> listarResultadosPorExperimento(int idExperimento) throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_VerResultadosPorExperimento(?)}")) {
            cs.setInt(1, idExperimento);
            cs.execute();
            return toLista(cs.getResultSet());
        }
    }

    public List<String[]> listarCientificos() throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_VerCientificos()}")) {
            cs.execute();
            return toLista(cs.getResultSet());
        }
    }

    /** Devuelve la contraseña del administrador almacenada en la BD. */
    public String obtenerContraseniaAdmin() throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_VerificarContrasenia(?)}")) {
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.execute();
            return cs.getString(1);
        }
    }

    // =========================================================
    // MUTACIONES (transacción manejada dentro de cada SP)
    // =========================================================

    /** @return ID del nuevo científico, o -1 si el SP no devolvió resultado. */
    public int agregarCientifico(String nombre, String apellido, String nacimiento)
            throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_AgregarCientifico(?,?,?)}")) {
            cs.setString(1, nombre);
            cs.setString(2, apellido);
            cs.setDate(3, parseFecha(nacimiento));
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs != null && rs.next()) return rs.getInt("IdCientifico");
            return -1;
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —agregar científico: " + e.getMessage());
            throw e;
        }
    }

    public void actualizarCientifico(int id, String nombre, String apellido, String nacimiento)
            throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_ActualizarCientifico(?,?,?,?)}")) {
            cs.setInt(1, id);
            cs.setString(2, nombre);
            cs.setString(3, apellido);
            cs.setDate(4, parseFecha(nacimiento));
            cs.execute();
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —actualizar científico: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Elimina un científico.
     * Lanza SQLIntegrityConstraintViolationException si tiene experimentos asociados
     * (la FK RESTRICT lo bloquea automáticamente).
     */
    public void borrarCientifico(int id) throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_BorrarCientifico(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            Logs.advertencia("No se puede borrar científico " + id + ": tiene experimentos asociados.");
            throw e;
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —borrar científico: " + e.getMessage());
            throw e;
        }
    }

    /** @return ID del nuevo experimento. */
    public int agregarExperimento(String fechaInicio, String fechaFinal, String nombre,
                                   String descripcion, String estado, int idResponsable)
            throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_AgregarExperimento(?,?,?,?,?,?)}")) {
            cs.setDate(1, parseFecha(fechaInicio));
            cs.setDate(2, parseFecha(fechaFinal));
            cs.setString(3, nombre);
            cs.setString(4, descripcion);
            cs.setString(5, estado);
            cs.setInt(6, idResponsable);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs != null && rs.next()) return rs.getInt("IdExperimento");
            return -1;
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —agregar experimento: " + e.getMessage());
            throw e;
        }
    }

    public void actualizarExperimento(int id, String fechaInicio, String fechaFinal, String nombre,
                                       String descripcion, String estado, int idResponsable)
            throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_ActualizarExperimento(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, id);
            cs.setDate(2, parseFecha(fechaInicio));
            cs.setDate(3, parseFecha(fechaFinal));
            cs.setString(4, nombre);
            cs.setString(5, descripcion);
            cs.setString(6, estado);
            cs.setInt(7, idResponsable);
            cs.execute();
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —actualizar experimento: " + e.getMessage());
            throw e;
        }
    }

    public void borrarExperimento(int id) throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_BorrarExperimento(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —borrar experimento: " + e.getMessage());
            throw e;
        }
    }

    public void actualizarEstadoExperimento(int id, String estado) throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_ActualizarEstadoExperimento(?,?)}")) {
            cs.setInt(1, id);
            cs.setString(2, estado);
            cs.execute();
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —actualizar estado: " + e.getMessage());
            throw e;
        }
    }

    /** @return ID del nuevo resultado. */
    public int agregarResultado(String fecha, String descripcion, String prueba,
                                 int idExperimento, int idPrueba) throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_AgregarResultado(?,?,?,?,?)}")) {
            cs.setDate(1, parseFecha(fecha));
            cs.setString(2, descripcion);
            cs.setString(3, prueba);
            cs.setInt(4, idExperimento);
            cs.setInt(5, idPrueba);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs != null && rs.next()) return rs.getInt("IdResultado");
            return -1;
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —agregar resultado: " + e.getMessage());
            throw e;
        }
    }

    public void agregarRealiza(int idCientifico, int idExperimento) throws SQLException {
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_AgregarRealiza(?,?)}")) {
            cs.setInt(1, idCientifico);
            cs.setInt(2, idExperimento);
            cs.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            Logs.advertencia("Relación Realiza ya existe: científico=" + idCientifico
                    + " experimento=" + idExperimento);
            throw e;
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —agregar realiza: " + e.getMessage());
            throw e;
        }
    }

    public int quitarRealiza(int idCientifico, int idExperimento) throws SQLException {
        // Pre-check: verify the scientist is actually in the team before deleting.
        // getUpdateCount() from stored procedure calls is unreliable in MySQL JDBC.
        String sqlCheck = "SELECT COUNT(*) FROM Realiza WHERE IdCientifico = ? AND IdExperimento = ?";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(sqlCheck)) {
            ps.setInt(1, idCientifico);
            ps.setInt(2, idExperimento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) return 0;
            }
        }
        try (CallableStatement cs = conexion.prepareCall("{CALL SP_QuitarRealiza(?,?)}")) {
            cs.setInt(1, idCientifico);
            cs.setInt(2, idExperimento);
            cs.execute();
        } catch (SQLTransactionRollbackException e) {
            Logs.error("Error SQL —quitar realiza: " + e.getMessage());
            throw e;
        }
        return 1;
    }

    /** @return true si el científico es responsable de al menos un experimento. */
    public boolean esResponsable(int idCientifico) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Experimento WHERE IdResponsable = ?";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCientifico);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =========================================================
    // UTILIDADES INTERNAS
    // =========================================================

    /**
     * Convierte un ResultSet en una lista de arrays de String.
     * El primer elemento de la lista son los nombres de columna (encabezados).
     * Si el ResultSet es null o está vacío, devuelve lista con solo encabezados.
     */
    private List<String[]> toLista(ResultSet rs) throws SQLException {
        List<String[]> resultado = new ArrayList<>();
        if (rs == null) return resultado;

        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        String[] encabezados = new String[cols];
        for (int i = 1; i <= cols; i++) {
            encabezados[i - 1] = meta.getColumnLabel(i);
        }
        resultado.add(encabezados);

        while (rs.next()) {
            String[] fila = new String[cols];
            for (int i = 1; i <= cols; i++) {
                String val = rs.getString(i);
                fila[i - 1] = (val == null) ? "" : val;
            }
            resultado.add(fila);
        }
        return resultado;
    }

    /**
     * Parsea una fecha "yyyy-MM-dd" a java.sql.Date.
     * Si la cadena es null o vacía, devuelve null (columna nullable en la BD).
     *
     * IMPORTANTE: Date.valueOf() lanza IllegalArgumentException si el formato no es
     * exactamente "yyyy-MM-dd". ManejadorPeticiones captura RuntimeException para este caso,
     * devolviendo ERR_VALIDACION al cliente en vez de cortar la sesión silenciosamente.
     */
    private static java.sql.Date parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) return null;
        return java.sql.Date.valueOf(fecha); // lanza IllegalArgumentException si formato inválido
    }
}
