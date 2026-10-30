package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.CopiaSeguridadDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CopiaSeguridadServiceTest {

    @TempDir
    Path carpetaTemporal;

    private Path origen;
    private CopiaSeguridadService service;

    @BeforeEach
    void prepararBaseDeDatos() throws Exception {
        origen = carpetaTemporal.resolve("origen.db");
        InicializadorBD.inicializar(origen);
        new AsociadoDAO(origen).crear(new Asociado(
            "11111111", "Ana", "Pérez", null, LocalDate.of(2026, 1, 15)));
        service = new CopiaSeguridadService(new CopiaSeguridadDAO(origen));
    }

    @Test
    void creaCopiaEnUnArchivoNuevo() throws Exception {
        Path destino = carpetaTemporal.resolve("copia-nueva.db");

        Path resultado = service.crearCopia(destino, false);

        assertTrue(Files.exists(resultado));
        assertTrue(Files.size(resultado) > 0);
        assertEquals(destino.toAbsolutePath().normalize(), resultado);
        assertTrue(new AsociadoDAO(resultado)
            .buscarPorDocumento("11111111").isPresent());
    }

    @Test
    void rechazaDestinoNulo() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.crearCopia(null, false));
        assertEquals("Seleccione un archivo de destino.", error.getMessage());
    }

    @Test
    void rechazaSobrescribirSinConfirmacion() throws Exception {
        Path destino = carpetaTemporal.resolve("copia-existente.db");
        Files.writeString(destino, "contenido previo");
        byte[] antes = Files.readAllBytes(destino);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.crearCopia(destino, false));

        assertEquals("El archivo de destino ya existe.", error.getMessage());
        assertArrayEquals(antes, Files.readAllBytes(destino));
    }

    @Test
    void sobrescribeConConfirmacion() throws Exception {
        Path destino = carpetaTemporal.resolve("copia-reemplazada.db");
        Files.writeString(destino, "contenido previo");
        long tamanoAnterior = Files.size(destino);

        Path resultado = service.crearCopia(destino, true);

        assertTrue(Files.exists(resultado));
        assertTrue(Files.size(resultado) > tamanoAnterior);
        assertTrue(new AsociadoDAO(resultado)
            .buscarPorDocumento("11111111").isPresent());
    }

    @Test
    void rechazaCopiarSobreLaBaseEnUso() throws Exception {
        byte[] antes = Files.readAllBytes(origen);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.crearCopia(origen, true));

        assertEquals("No se puede sobrescribir la base de datos en uso.",
            error.getMessage());
        assertArrayEquals(antes, Files.readAllBytes(origen));
        assertTrue(new AsociadoDAO(origen)
            .buscarPorDocumento("11111111").isPresent());
    }
}
