package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AsociadoDAOTest {

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private AsociadoDAO dao;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        rutaBd = carpetaTemporal.resolve("asociados-dao.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new AsociadoDAO(rutaBd);
    }

    @Test
    void creaYRecuperaAsociadoPorDocumento() throws SQLException {
        Asociado asociado = asociado("10203040", "Ana", "Pérez", "3001234567");

        long id = dao.crear(asociado);
        Optional<Asociado> recuperado = dao.buscarPorDocumento("10203040");

        assertTrue(id > 0);
        assertEquals(id, asociado.getId());
        assertTrue(recuperado.isPresent());
        assertEquals("Ana", recuperado.get().getNombres());
        assertEquals(LocalDate.of(2026, 10, 22), recuperado.get().getFechaRegistro());
    }

    @Test
    void rechazaDocumentoDuplicadoEnLaBaseDeDatos() throws SQLException {
        dao.crear(asociado("10203040", "Ana", "Pérez", null));

        assertThrows(SQLException.class,
            () -> dao.crear(asociado("10203040", "Otro", "Asociado", null)));
    }

    @Test
    void buscaPorTextoExistenteEInexistenteSinExcepcion() throws SQLException {
        dao.crear(asociado("10203040", "Ana", "Pérez", null));
        dao.crear(asociado("99887766", "Carlos", "Gómez", null));

        List<Asociado> porDocumento = dao.buscarPorTexto("1020");
        List<Asociado> porNombre = dao.buscarPorTexto("Carlos");
        List<Asociado> inexistentes = dao.buscarPorTexto("NoExiste");

        assertEquals(1, porDocumento.size());
        assertEquals("10203040", porDocumento.get(0).getDocumento());
        assertEquals(1, porNombre.size());
        assertTrue(inexistentes.isEmpty());
    }

    @Test
    void actualizaConservaIdYFechaDeRegistro() throws SQLException {
        Asociado asociado = asociado("10203040", "Ana", "Pérez", null);
        long id = dao.crear(asociado);

        Asociado cambios = new Asociado(
            id, "10203040", "Ana María", "Pérez Díaz", "3109876543",
            LocalDate.of(2030, 1, 1)
        );
        dao.actualizar(cambios);

        Asociado recuperado = dao.buscarPorDocumento("10203040").orElseThrow();
        assertEquals(id, recuperado.getId());
        assertEquals("Ana María", recuperado.getNombres());
        assertEquals("Pérez Díaz", recuperado.getApellidos());
        assertEquals("3109876543", recuperado.getTelefono());
        assertEquals(LocalDate.of(2026, 10, 22), recuperado.getFechaRegistro());
    }

    @Test
    void detectaDocumentoExistenteExcluyendoElMismoId() throws SQLException {
        long id = dao.crear(asociado("10203040", "Ana", "Pérez", null));

        assertTrue(dao.existeDocumento("10203040", 0));
        assertFalse(dao.existeDocumento("10203040", id));
        assertFalse(dao.existeDocumento("00000000", 0));
    }

    @Test
    void elRegistroPersisteAlUsarUnaNuevaInstanciaDelDao() throws SQLException {
        dao.crear(asociado("10203040", "Ana", "Pérez", null));

        AsociadoDAO otroDao = new AsociadoDAO(rutaBd);
        Optional<Asociado> recuperado = otroDao.buscarPorDocumento("10203040");

        assertTrue(recuperado.isPresent());
        assertNotNull(recuperado.get().getFechaRegistro());
    }

    private static Asociado asociado(String documento, String nombres,
                                     String apellidos, String telefono) {
        return new Asociado(documento, nombres, apellidos, telefono,
            LocalDate.of(2026, 10, 22));
    }
}
