package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CopiaSeguridadDAOTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void creaUnaCopiaConLosDatos() throws Exception {
        Path origen = carpetaTemporal.resolve("origen.db");
        Path destino = carpetaTemporal.resolve("copia.db");
        InicializadorBD.inicializar(origen);

        AsociadoDAO asociadoDAO = new AsociadoDAO(origen);
        asociadoDAO.crear(new Asociado(
            "11111111", "Ana", "Pérez", null, LocalDate.of(2026, 1, 15)));

        new CopiaSeguridadDAO(origen).crearCopia(origen, destino);

        assertTrue(Files.exists(destino));
        assertTrue(Files.size(destino) > 0);
        assertTrue(new AsociadoDAO(destino)
            .buscarPorDocumento("11111111").isPresent());

        Set<String> tablas = tablas(destino);
        assertEquals(5, tablas.size());
        assertTrue(tablas.containsAll(Set.of(
            "usuario", "asociado", "prestamo", "cuota", "pago")));
    }

    @Test
    void fallaSiElDestinoYaExiste() throws Exception {
        Path origen = carpetaTemporal.resolve("origen-existente.db");
        Path destino = carpetaTemporal.resolve("destino-existente.db");
        InicializadorBD.inicializar(origen);
        Files.writeString(destino, "archivo existente");

        assertThrows(SQLException.class,
            () -> new CopiaSeguridadDAO(origen).crearCopia(origen, destino));
    }

    private static Set<String> tablas(Path rutaBd) throws SQLException {
        Set<String> tablas = new LinkedHashSet<>();
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table'";
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString("name");
                if (!nombre.startsWith("sqlite_")) {
                    tablas.add(nombre);
                }
            }
        }
        return tablas;
    }
}
