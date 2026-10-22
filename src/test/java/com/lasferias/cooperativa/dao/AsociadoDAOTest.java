package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    void interpretaComodinesDeLikeComoTextoLiteral() throws SQLException {
        dao.crear(asociado("DOC%1", "Ana", "Pérez", null));
        dao.crear(asociado("DOC_1", "Bea", "Gómez", null));
        dao.crear(asociado("DOC\\1", "Carla", "Ruiz", null));
        dao.crear(asociado("DOCX1", "Diana", "Torres", null));

        assertEquals(1, dao.buscarPorTexto("%").size());
        assertEquals(1, dao.buscarPorTexto("_").size());
        assertEquals(1, dao.buscarPorTexto("\\").size());
    }

    @Test
    void buscaYDetectaDocumentoSinDistinguirMayusculas() throws SQLException {
        long id = dao.crear(asociado("ABC123", "Ana", "Pérez", null));

        assertTrue(dao.existeDocumento("abc123", 0));
        assertTrue(dao.buscarPorDocumento("abc123").isPresent());
        assertFalse(dao.existeDocumento("abc123", id));
    }

    @Test
    void buscaConTextoVacioONuloListaTodosLosAsociados() throws SQLException {
        dao.crear(asociado("10203040", "Ana", "Pérez", null));

        assertEquals(1, dao.buscarPorTexto(null).size());
        assertEquals(1, dao.buscarPorTexto("").size());
        assertEquals(1, dao.buscarPorTexto("   ").size());
    }

    @Test
    void buscaNombresYApellidosSinDistinguirAcentosNiMayusculas()
            throws SQLException {
        dao.crear(asociado("DOC-JOSE", "José", "Pérez", null));
        dao.crear(asociado("DOC-JOSE2", "Jose", "Ruiz", null));
        dao.crear(asociado("DOC-ALVAREZ", "Ana", "Alvarez", null));

        assertEquals(1, dao.buscarPorTexto("PEREZ").size());
        assertEquals(2, dao.buscarPorTexto("JOSE").size());
        assertEquals(2, dao.buscarPorTexto("JOSÉ").size());
        assertEquals(1, dao.buscarPorTexto("Álvarez").size());
        assertEquals(1, dao.buscarPorTexto("ERE").size());
        assertEquals(1, dao.buscarPorTexto("pÉrEz").size());
    }

    @Test
    void ordenaApellidosConAcentosEnOrdenAlfabeticoEspanol() throws SQLException {
        dao.crear(asociado("DOC-Z", "Ana", "Zapata", null));
        dao.crear(asociado("DOC-AVILA", "Ana", "Ávila", null));
        dao.crear(asociado("DOC-ALVAREZ", "Ana", "Álvarez", null));

        List<String> apellidos = dao.listar().stream()
            .map(Asociado::getApellidos)
            .toList();

        assertEquals(List.of("Álvarez", "Ávila", "Zapata"), apellidos);
    }

    @Test
    void ordenaEmpatesPorNombreYFinalmentePorDocumento() throws SQLException {
        dao.crear(asociado("33333333", "Ana", "Pérez", null));
        dao.crear(asociado("22222222", "Carlos", "Pérez", null));
        dao.crear(asociado("11111111", "Ana", "Pérez", null));

        List<String> documentos = dao.listar().stream()
            .map(Asociado::getDocumento)
            .toList();

        assertEquals(List.of("11111111", "33333333", "22222222"), documentos);
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
    void actualizarIdInexistenteFallaSinAlterarDatos() throws SQLException {
        dao.crear(asociado("10203040", "Ana", "Pérez", null));
        Asociado inexistente = new Asociado(
            999999, "00000000", "Sin", "Registro", null, LocalDate.now());

        SQLException error = assertThrows(SQLException.class,
            () -> dao.actualizar(inexistente));

        assertNotNull(error.getMessage());
        assertEquals(1, dao.listar().size());
        assertTrue(dao.buscarPorDocumento("10203040").isPresent());
    }

    @Test
    void crearSinFechaDeRegistroFallaConSqlExceptionControlada() {
        Asociado sinFecha = new Asociado(
            "SIN-FECHA", "Ana", "Pérez", null, null);

        SQLException error = assertThrows(SQLException.class,
            () -> dao.crear(sinFecha));

        assertEquals("22004", error.getSQLState());
    }

    @Test
    void fechaAlmacenadaMalformadaFallaConSqlExceptionYCausa() throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement("""
                 INSERT INTO asociado(documento, nombres, apellidos, telefono, fecha_registro)
                 VALUES (?, ?, ?, ?, ?)
                 """)) {
            ps.setString(1, "FECHA-MALA");
            ps.setString(2, "Dato");
            ps.setString(3, "Corrupto");
            ps.setString(4, null);
            ps.setString(5, "22/10/2026");
            ps.executeUpdate();
        }

        SQLException error = assertThrows(SQLException.class,
            () -> dao.buscarPorDocumento("FECHA-MALA"));

        assertEquals("22007", error.getSQLState());
        assertInstanceOf(DateTimeParseException.class, error.getCause());
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
