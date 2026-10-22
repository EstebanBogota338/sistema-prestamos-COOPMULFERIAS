package com.lasferias.cooperativa.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InicializadorBDTest {

    /** Tablas que el esquema de la fase 1 debe crear. */
    private static final List<String> TABLAS_ESPERADAS =
        List.of("usuario", "asociado", "prestamo", "cuota", "pago");

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
}
