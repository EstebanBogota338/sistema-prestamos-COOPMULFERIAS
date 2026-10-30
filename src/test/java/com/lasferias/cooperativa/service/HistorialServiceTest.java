package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.HistorialDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HistorialServiceTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void rechazaAsociadoIdNoPositivo() {
        Path rutaBd = carpetaTemporal.resolve("historial-service-validacion.db");
        HistorialService service =
            new HistorialService(new HistorialDAO(rutaBd));

        for (long id : new long[]{0L, -1L}) {
            IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.listarPrestamos(id));
            assertEquals("Seleccione un asociado válido.", error.getMessage());

            error = assertThrows(IllegalArgumentException.class,
                () -> service.listarCuotas(id));
            assertEquals("Seleccione un asociado válido.", error.getMessage());

            error = assertThrows(IllegalArgumentException.class,
                () -> service.listarPagos(id));
            assertEquals("Seleccione un asociado válido.", error.getMessage());
        }
    }

    @Test
    void devuelveListasVaciasSiElAsociadoNoTieneDatos() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("historial-service-vacio.db");
        InicializadorBD.inicializar(rutaBd);
        AsociadoDAO asociadoDAO = new AsociadoDAO(rutaBd);
        Asociado asociado = new Asociado(
            "11111111", "Ana", "Pérez", null, LocalDate.of(2026, 1, 15));
        asociadoDAO.crear(asociado);

        HistorialService service =
            new HistorialService(new HistorialDAO(rutaBd));

        assertTrue(service.listarPrestamos(asociado.getId()).isEmpty());
        assertTrue(service.listarCuotas(asociado.getId()).isEmpty());
        assertTrue(service.listarPagos(asociado.getId()).isEmpty());
    }
}
