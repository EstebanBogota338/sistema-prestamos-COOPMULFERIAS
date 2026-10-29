package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Prestamo;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PrestamoDAOTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 29);

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private PrestamoDAO dao;
    private AsociadoDAO asociadoDAO;
    private Asociado asociado;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        rutaBd = carpetaTemporal.resolve("prestamos-dao.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new PrestamoDAO(rutaBd);
        asociadoDAO = new AsociadoDAO(rutaBd);
        asociado = crearAsociado("11111111");
    }

    @Test
    void creaYRecuperaPrestamo() throws SQLException {
        Prestamo prestamo = prestamo(asociado);

        long id = dao.crear(prestamo);
        Prestamo recuperado = new PrestamoDAO(rutaBd)
            .buscarPorId(id).orElseThrow();

        assertEquals(id, prestamo.getId());
        assertEquals(asociado.getId(), recuperado.getAsociadoId());
        assertEquals(1_000_000L, recuperado.getMonto());
        assertEquals(FECHA, recuperado.getFecha());
        assertEquals("12 meses", recuperado.getPlazo());
        assertEquals(12, recuperado.getNumeroCuotas());
        assertEquals(90_000L, recuperado.getValorCuota());
        assertEquals(Prestamo.ESTADO_VIGENTE, recuperado.getEstado());
    }

    @Test
    void actualizaCamposPermitidos() throws SQLException {
        Prestamo creado = prestamo(asociado);
        dao.crear(creado);

        Prestamo actualizado = new Prestamo(
            creado.getId(), asociado.getId(), 2_000_000L,
            LocalDate.of(2026, 11, 1), "18 meses", 18, 120_000L,
            Prestamo.ESTADO_FINALIZADO);

        assertTrue(dao.actualizar(actualizado));

        Prestamo recuperado = dao.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(2_000_000L, recuperado.getMonto());
        assertEquals(LocalDate.of(2026, 11, 1), recuperado.getFecha());
        assertEquals("18 meses", recuperado.getPlazo());
        assertEquals(18, recuperado.getNumeroCuotas());
        assertEquals(120_000L, recuperado.getValorCuota());
        assertEquals(Prestamo.ESTADO_FINALIZADO, recuperado.getEstado());
    }

    @Test
    void actualizarIdInexistenteDevuelveFalse() throws SQLException {
        Prestamo inexistente = new Prestamo(
            999_999L, asociado.getId(), 1_000_000L, FECHA,
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);

        assertFalse(dao.actualizar(inexistente));
    }

    @Test
    void listaPrestamosEnOrdenDeId() throws SQLException {
        Asociado otroAsociado = crearAsociado("22222222");
        Prestamo primero = prestamo(asociado);
        Prestamo segundo = prestamo(otroAsociado);
        segundo.setMonto(2_000_000L);
        dao.crear(primero);
        dao.crear(segundo);

        List<Prestamo> prestamos = dao.listar();

        assertEquals(2, prestamos.size());
        assertEquals(primero.getId(), prestamos.get(0).getId());
        assertEquals(segundo.getId(), prestamos.get(1).getId());
    }

    @Test
    void rechazaAsociadoInexistentePorForeignKey() {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setAsociadoId(999_999L);

        assertThrows(SQLException.class, () -> dao.crear(prestamo));
    }

    @Test
    void actualizarNoCambiaElAsociado() throws SQLException {
        Prestamo creado = prestamo(asociado);
        dao.crear(creado);
        Asociado otroAsociado = crearAsociado("22222222");

        Prestamo intento = new Prestamo(
            creado.getId(), otroAsociado.getId(), 2_000_000L,
            FECHA, "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);

        assertTrue(dao.actualizar(intento));

        Prestamo recuperado = dao.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(asociado.getId(), recuperado.getAsociadoId());
        assertEquals(2_000_000L, recuperado.getMonto());
    }

    private Asociado crearAsociado(String documento) throws SQLException {
        Asociado nuevo = new Asociado(
            documento, "Ana", "Pérez", null, LocalDate.now());
        asociadoDAO.crear(nuevo);
        return nuevo;
    }

    private static Prestamo prestamo(Asociado asociado) {
        return new Prestamo(
            asociado.getId(), 1_000_000L, FECHA,
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);
    }
}
