package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AsociadoServiceTest {

    @TempDir
    Path carpetaTemporal;

    private AsociadoDAO dao;
    private AsociadoService service;

    @BeforeEach
    void prepararServicio() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("asociados-service.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new AsociadoDAO(rutaBd);
        service = new AsociadoService(dao);
    }

    @Test
    void registraAsociadoConFechaAutomaticaYNormalizaDatos() throws SQLException {
        Asociado asociado = new Asociado(
            "  10203040  ", "  Ana  ", "  Pérez  ", "  3001234567  ", null);

        service.registrar(asociado);
        Asociado recuperado = dao.buscarPorDocumento("10203040").orElseThrow();

        assertTrue(asociado.getId() > 0);
        assertEquals("10203040", recuperado.getDocumento());
        assertEquals("Ana", recuperado.getNombres());
        assertEquals("Pérez", recuperado.getApellidos());
        assertEquals("3001234567", recuperado.getTelefono());
        assertEquals(LocalDate.now(), recuperado.getFechaRegistro());
    }

    @Test
    void rechazaDocumentoDuplicadoConMensajeClaro() throws SQLException {
        service.registrar(asociado("10203040", "Ana", "Pérez", null));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(asociado(" 10203040 ", "Otro", "Asociado", null)));

        assertTrue(error.getMessage().contains("documento"));
    }

    @Test
    void rechazaDocumentoDuplicadoSinDistinguirMayusculas() throws SQLException {
        Asociado primero = service.registrar(
            asociado("AbC123", "Ana", "Pérez", null));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(asociado("abc123", "Otra", "Persona", null)));

        assertEquals("ABC123",
            dao.buscarPorId(primero.getId()).orElseThrow().getDocumento());
        assertTrue(error.getMessage().contains("documento"));
        assertEquals(1, dao.listar().size());
    }

    @Test
    void validaDocumentoNombresYTelefono() {
        IllegalArgumentException documento = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(asociado("   ", "Ana", "Pérez", null)));
        IllegalArgumentException nombres = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(asociado("10203040", " ", "Pérez", null)));
        IllegalArgumentException apellidos = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(asociado("10203040", "Ana", " ", null)));
        IllegalArgumentException telefono = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(asociado("10203040", "Ana", "Pérez", "300ABC123")));

        assertEquals("El documento es obligatorio.", documento.getMessage());
        assertEquals("Los nombres son obligatorios.", nombres.getMessage());
        assertEquals("Los apellidos son obligatorios.", apellidos.getMessage());
        assertTrue(telefono.getMessage().contains("solo dígitos"));
    }

    @Test
    void validaFronterasDeLongitudDelTelefono() throws SQLException {
        assertDoesNotThrow(() -> service.registrar(
            asociado("TEL-7", "Ana", "Siete", "1234567")));
        assertDoesNotThrow(() -> service.registrar(
            asociado("TEL-10", "Ana", "Diez", "1234567890")));

        assertThrows(IllegalArgumentException.class, () -> service.registrar(
            asociado("TEL-6", "Ana", "Seis", "123456")));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(
            asociado("TEL-11", "Ana", "Once", "12345678901")));
    }

    @Test
    void rechazaRegistroNuloConMensajeFuncional() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.registrar(null));

        assertEquals("Los datos del asociado son obligatorios.", error.getMessage());
    }

    @Test
    void aceptaTelefonoVacioComoDatoOpcional() throws SQLException {
        Asociado asociado = asociado("10203040", "Ana", "Pérez", "   ");

        assertDoesNotThrow(() -> service.registrar(asociado));

        assertNull(dao.buscarPorDocumento("10203040").orElseThrow().getTelefono());
    }

    @Test
    void editaConservandoIdYFechaDeRegistro() throws SQLException {
        Asociado asociado = service.registrar(
            asociado("10203040", "Ana", "Pérez", null));
        long id = asociado.getId();
        LocalDate fechaOriginal = asociado.getFechaRegistro();

        asociado.setNombres("Ana María");
        asociado.setApellidos("Pérez Díaz");
        asociado.setTelefono("3109876543");
        asociado.setFechaRegistro(LocalDate.of(2030, 1, 1));
        service.actualizar(asociado);

        Asociado recuperado = dao.buscarPorDocumento("10203040").orElseThrow();
        assertEquals(id, recuperado.getId());
        assertEquals(fechaOriginal, asociado.getFechaRegistro());
        assertEquals("Ana María", recuperado.getNombres());
        assertEquals("Pérez Díaz", recuperado.getApellidos());
        assertEquals(fechaOriginal, recuperado.getFechaRegistro());
    }

    @Test
    void rechazaCambiarDocumentoAOtroYaRegistradoDuranteLaEdicion()
            throws SQLException {
        Asociado primero = service.registrar(
            asociado("ABC123", "Ana", "Pérez", null));
        service.registrar(asociado("XYZ789", "Carlos", "Gómez", null));

        primero.setDocumento("xyz789");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> service.actualizar(primero));

        assertTrue(error.getMessage().contains("documento"));
        assertEquals("ABC123", dao.buscarPorId(primero.getId())
            .orElseThrow().getDocumento());
    }

    @Test
    void actualizarIdInexistenteFallaConSqlException() {
        Asociado inexistente = new Asociado(
            999999, "NO-EXISTE", "Sin", "Registro", null, LocalDate.now());

        assertThrows(SQLException.class, () -> service.actualizar(inexistente));
    }

    @Test
    void buscaAsociadosPorTextoYDevuelveListaVaciaSiNoHayCoincidencias()
            throws SQLException {
        service.registrar(asociado("10203040", "Ana", "Pérez", null));

        List<Asociado> encontrados = service.buscar("Pérez");
        List<Asociado> sinResultados = service.buscar("NoExiste");

        assertEquals(1, encontrados.size());
        assertTrue(sinResultados.isEmpty());
    }

    private static Asociado asociado(String documento, String nombres,
                                     String apellidos, String telefono) {
        return new Asociado(documento, nombres, apellidos, telefono, null);
    }
}
