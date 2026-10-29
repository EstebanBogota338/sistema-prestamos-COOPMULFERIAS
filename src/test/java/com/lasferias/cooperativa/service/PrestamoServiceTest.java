package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Prestamo;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PrestamoServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 29);

    @TempDir
    Path carpetaTemporal;

    private PrestamoDAO prestamoDAO;
    private AsociadoDAO asociadoDAO;
    private PrestamoService service;
    private Asociado asociado;

    @BeforeEach
    void prepararServicio() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("prestamos-service.db");
        InicializadorBD.inicializar(rutaBd);
        asociadoDAO = new AsociadoDAO(rutaBd);
        prestamoDAO = new PrestamoDAO(rutaBd);
        service = new PrestamoService(prestamoDAO, asociadoDAO);
        asociado = crearAsociado("11111111");
    }

    @Test
    void registraPrestamoValido() throws SQLException {
        Prestamo creado = service.registrar(prestamo(asociado));

        assertTrue(creado.getId() > 0);
        Prestamo recuperado = prestamoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(asociado.getId(), recuperado.getAsociadoId());
        assertEquals(1_000_000L, recuperado.getMonto());
        assertEquals(FECHA, recuperado.getFecha());
        assertEquals("12 meses", recuperado.getPlazo());
        assertEquals(12, recuperado.getNumeroCuotas());
        assertEquals(90_000L, recuperado.getValorCuota());
        assertEquals(Prestamo.ESTADO_VIGENTE, recuperado.getEstado());
    }

    @Test
    void rechazaAsociadoInexistente() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setAsociadoId(999_999L);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(prestamo));

        assertTrue(error.getMessage().toLowerCase().contains("asociado"));
        assertTrue(prestamoDAO.listar().isEmpty());
    }

    @Test
    void rechazaMontoNoPositivo() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setMonto(0L);
        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));

        prestamo.setMonto(-1L);
        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));
    }

    @Test
    void rechazaFechaNula() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setFecha(null);

        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));
    }

    @Test
    void rechazaNumeroDeCuotasNoPositivo() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setNumeroCuotas(0);
        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));

        prestamo.setNumeroCuotas(-1);
        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));
    }

    @Test
    void rechazaValorDeCuotaNoPositivo() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setValorCuota(0L);
        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));

        prestamo.setValorCuota(-1L);
        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));
    }

    @Test
    void rechazaPlazoVacio() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setPlazo("   ");

        assertThrows(IllegalArgumentException.class,
            () -> service.registrar(prestamo));
    }

    @Test
    void rechazaEstadoInvalido() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setEstado("Cancelado");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(prestamo));

        assertTrue(error.getMessage().contains("Vigente"));
    }

    @Test
    void actualizaCamposPermitidos() throws SQLException {
        Prestamo creado = service.registrar(prestamo(asociado));
        Prestamo actualizado = new Prestamo(
            creado.getId(), asociado.getId(), 2_000_000L,
            LocalDate.of(2026, 11, 1), "18 meses", 18, 120_000L,
            Prestamo.ESTADO_FINALIZADO);

        service.actualizar(actualizado);

        Prestamo recuperado = prestamoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(2_000_000L, recuperado.getMonto());
        assertEquals(LocalDate.of(2026, 11, 1), recuperado.getFecha());
        assertEquals("18 meses", recuperado.getPlazo());
        assertEquals(18, recuperado.getNumeroCuotas());
        assertEquals(120_000L, recuperado.getValorCuota());
        assertEquals(Prestamo.ESTADO_FINALIZADO, recuperado.getEstado());
    }

    @Test
    void rechazaCambioDeAsociado() throws SQLException {
        Prestamo creado = service.registrar(prestamo(asociado));
        Asociado otro = crearAsociado("22222222");
        Prestamo intento = new Prestamo(
            creado.getId(), otro.getId(), 2_000_000L,
            FECHA, "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.actualizar(intento));

        assertEquals("No se puede cambiar el asociado del préstamo.",
            error.getMessage());
        Prestamo recuperado = prestamoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(asociado.getId(), recuperado.getAsociadoId());
        assertEquals(1_000_000L, recuperado.getMonto());
    }

    @Test
    void actualizarIdInexistenteLanzaSqlException() {
        Prestamo inexistente = new Prestamo(
            999_999L, asociado.getId(), 1_000_000L, FECHA,
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);

        assertThrows(SQLException.class, () -> service.actualizar(inexistente));
    }

    @Test
    void conservaValorCuotaSinCalcularlo() throws SQLException {
        Prestamo prestamo = prestamo(asociado);
        prestamo.setMonto(1_000_000L);
        prestamo.setNumeroCuotas(10);
        prestamo.setValorCuota(123_456L);

        Prestamo creado = service.registrar(prestamo);
        Prestamo recuperado = prestamoDAO.buscarPorId(creado.getId()).orElseThrow();

        assertEquals(123_456L, recuperado.getValorCuota());
        assertNotEquals(100_000L, recuperado.getValorCuota());
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
