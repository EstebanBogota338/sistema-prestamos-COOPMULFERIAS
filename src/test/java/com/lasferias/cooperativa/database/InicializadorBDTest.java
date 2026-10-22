package com.lasferias.cooperativa.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InicializadorBDTest {

    /** Tablas que el esquema de la fase 1 debe crear. */
    private static final List<String> TABLAS_ESPERADAS =
        List.of("usuario", "asociado", "prestamo", "cuota", "pago");
    private static final List<String> INDICES_ESPERADOS =
        List.of("idx_prestamo_asociado", "idx_cuota_prestamo", "idx_pago_cuota");

    private static final String SQL_TABLAS =
        "SELECT name FROM sqlite_master WHERE type='table'";

    @TempDir
    Path carpetaTemporal;

    @Test
    void inicializarCreaLasCincoTablas() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("cooperativa-test.db");

        InicializadorBD.inicializar(rutaBd);

        assertTrue(Files.exists(rutaBd), "No se creo el archivo de base de datos en " + rutaBd);

        Set<String> tablas = nombresDeTablas(rutaBd);
        for (String tabla : TABLAS_ESPERADAS) {
            assertTrue(tablas.contains(tabla),
                "Falta la tabla requerida: " + tabla + ". Tablas encontradas: " + tablas);
        }

        assertEquals(List.of("id", "nombre_usuario", "contrasena_hash"),
            nombresDeColumnas(rutaBd, "usuario"));
        assertEquals(List.of("id", "documento", "nombres", "apellidos",
                "telefono", "fecha_registro"),
            nombresDeColumnas(rutaBd, "asociado"));
        assertEquals(List.of("id", "asociado_id", "monto", "fecha", "plazo",
                "numero_cuotas", "valor_cuota", "estado"),
            nombresDeColumnas(rutaBd, "prestamo"));
        assertEquals(List.of("id", "prestamo_id", "numero_cuota", "valor",
                "fecha_vencimiento"),
            nombresDeColumnas(rutaBd, "cuota"));
        assertEquals(List.of("id", "cuota_id", "fecha_pago", "valor", "numero_recibo"),
            nombresDeColumnas(rutaBd, "pago"));
        List<String> indices = nombresDeIndices(rutaBd);
        assertEquals(INDICES_ESPERADOS.size(), indices.size());
        assertTrue(indices.containsAll(INDICES_ESPERADOS));
    }

    @Test
    void inicializarDosVecesSobreLaMismaRutaEsIdempotente() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("cooperativa-idempotente.db");

        assertDoesNotThrow(() -> InicializadorBD.inicializar(rutaBd),
            "La primera inicializacion no debe fallar");
        assertDoesNotThrow(() -> InicializadorBD.inicializar(rutaBd),
            "La segunda inicializacion no debe fallar");

        Set<String> tablas = nombresDeTablas(rutaBd);
        assertTrue(tablas.containsAll(TABLAS_ESPERADAS),
            "Tras inicializar dos veces faltan tablas. Tablas encontradas: " + tablas);
    }

    @Test
    void activaLasClavesForaneasEnCadaConexionYRechazaRelacionesInexistentes()
            throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("cooperativa-claves-foraneas.db");
        InicializadorBD.inicializar(rutaBd);

        assertEquals(1, consultarPragmaClavesForaneas(rutaBd));
        assertEquals(1, consultarPragmaClavesForaneas(rutaBd));

        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO prestamo(asociado_id, monto, fecha, plazo, numero_cuotas,
                                 valor_cuota, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, 999999L, 1000, "2026-10-22", "1 mes", 1, 1000, "Vigente"));
    }

    @Test
    void rechazaDocumentosDuplicadosEImportesEstadosYValoresInvalidos()
            throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("cooperativa-restricciones.db");
        InicializadorBD.inicializar(rutaBd);

        long asociadoId = insertarYObtenerId(rutaBd, """
            INSERT INTO asociado(documento, nombres, apellidos, telefono, fecha_registro)
            VALUES (?, ?, ?, ?, ?)
            """, "10203040", "Ana", "Pérez", null, "2026-10-22");
        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO asociado(documento, nombres, apellidos, telefono, fecha_registro)
            VALUES (?, ?, ?, ?, ?)
            """, "10203040", "Otro", "Asociado", null, "2026-10-22"));

        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO prestamo(asociado_id, monto, fecha, plazo, numero_cuotas,
                                 valor_cuota, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, asociadoId, 0, "2026-10-22", "1 mes", 1, 1000, "Vigente"));
        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO prestamo(asociado_id, monto, fecha, plazo, numero_cuotas,
                                 valor_cuota, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, asociadoId, 1000, "2026-10-22", "1 mes", 0, 1000, "Vigente"));
        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO prestamo(asociado_id, monto, fecha, plazo, numero_cuotas,
                                 valor_cuota, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, asociadoId, 1000, "2026-10-22", "1 mes", 1, 1000, "Cancelado"));

        long prestamoId = insertarYObtenerId(rutaBd, """
            INSERT INTO prestamo(asociado_id, monto, fecha, plazo, numero_cuotas,
                                 valor_cuota, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, asociadoId, 1000, "2026-10-22", "1 mes", 1, 1000, "Vigente");
        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO cuota(prestamo_id, numero_cuota, valor, fecha_vencimiento)
            VALUES (?, ?, ?, ?)
            """, prestamoId, 1, 0, "2026-11-22"));

        long cuotaId = insertarYObtenerId(rutaBd, """
            INSERT INTO cuota(prestamo_id, numero_cuota, valor, fecha_vencimiento)
            VALUES (?, ?, ?, ?)
            """, prestamoId, 1, 1000, "2026-11-22");
        assertThrows(SQLException.class, () -> ejecutar(rutaBd, """
            INSERT INTO pago(cuota_id, fecha_pago, valor, numero_recibo)
            VALUES (?, ?, ?, ?)
            """, cuotaId, "2026-10-22", 0, "REC-001"));
    }

    private static Set<String> nombresDeTablas(Path rutaBd) throws SQLException {
        Set<String> tablas = new LinkedHashSet<>();
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement(SQL_TABLAS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tablas.add(rs.getString("name"));
            }
        }
        return tablas;
    }

    private static List<String> nombresDeColumnas(Path rutaBd, String tabla)
            throws SQLException {
        List<String> columnas = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")")) {
            while (rs.next()) {
                columnas.add(rs.getString("name"));
            }
        }
        return columnas;
    }

    private static List<String> nombresDeIndices(Path rutaBd) throws SQLException {
        List<String> indices = new ArrayList<>();
        String sql = """
            SELECT name
            FROM sqlite_master
            WHERE type = 'index' AND name IN (?, ?, ?)
            ORDER BY name
            """;
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, INDICES_ESPERADOS.get(0));
            ps.setString(2, INDICES_ESPERADOS.get(1));
            ps.setString(3, INDICES_ESPERADOS.get(2));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    indices.add(rs.getString("name"));
                }
            }
        }
        return indices;
    }

    private static int consultarPragmaClavesForaneas(Path rutaBd) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA foreign_keys")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void ejecutar(Path rutaBd, String sql, Object... parametros)
            throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarParametros(ps, parametros);
            ps.executeUpdate();
        }
    }

    private static long insertarYObtenerId(Path rutaBd, String sql,
                                           Object... parametros) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, parametros);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                assertTrue(claves.next(), "SQLite no devolvió la clave generada");
                return claves.getLong(1);
            }
        }
    }

    private static void asignarParametros(PreparedStatement ps, Object... parametros)
            throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }
}
