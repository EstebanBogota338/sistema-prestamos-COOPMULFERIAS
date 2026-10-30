package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.CuotaDAO;
import com.lasferias.cooperativa.dao.PagoDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PagoServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 12, 1);

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private PagoDAO pagoDAO;
    private PagoService service;
    private Cuota primeraCuota;
    private Cuota segundaCuota;

    @BeforeEach
    void prepararServicio() throws SQLException {
        rutaBd = carpetaTemporal.resolve("pagos-service.db");
        InicializadorBD.inicializar(rutaBd);

        AsociadoDAO asociadoDAO = new AsociadoDAO(rutaBd);
        PrestamoDAO prestamoDAO = new PrestamoDAO(rutaBd);
        CuotaDAO cuotaDAO = new CuotaDAO(rutaBd);
        pagoDAO = new PagoDAO(rutaBd);
        service = new PagoService(pagoDAO, cuotaDAO);

        Asociado asociado = new Asociado(
            "11111111", "Ana", "Pérez", null, LocalDate.now());
        asociadoDAO.crear(asociado);

        Prestamo prestamo = new Prestamo(
            asociado.getId(), 1_000_000L, LocalDate.of(2026, 10, 29),
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);
        prestamoDAO.crear(prestamo);

        primeraCuota = new Cuota(
            prestamo.getId(), 1, 100_000L, LocalDate.of(2026, 11, 30));
        segundaCuota = new Cuota(
            prestamo.getId(), 2, 90_000L, LocalDate.of(2026, 12, 30));
        cuotaDAO.crear(primeraCuota);
        cuotaDAO.crear(segundaCuota);
    }

    @Test
    void registraPagoValido() throws SQLException {
        Pago pago = pago(primeraCuota, FECHA, 40_000L, "REC-001");

        Pago creado = service.registrar(pago);

        assertTrue(creado.getId() > 0);
        Pago recuperado = pagoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(primeraCuota.getId(), recuperado.getCuotaId());
        assertEquals(FECHA, recuperado.getFechaPago());
        assertEquals(40_000L, recuperado.getValor());
        assertEquals("REC-001", recuperado.getNumeroRecibo());
    }

    @Test
    void aceptaAbonoParcialMenorQueLaCuota() throws SQLException {
        Pago pago = pago(primeraCuota, FECHA, 30_000L, "REC-002");

        Pago creado = service.registrar(pago);

        Pago recuperado = pagoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(30_000L, recuperado.getValor());
        assertTrue(recuperado.getValor() < primeraCuota.getValor());
    }

    @Test
    void rechazaValorCero() throws SQLException {
        Pago pago = pago(primeraCuota, FECHA, 0L, "REC-003");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(pago));
        assertEquals("El valor del pago debe ser mayor que cero.",
            error.getMessage());
        assertTrue(pagoDAO.listar().isEmpty());
    }

    @Test
    void rechazaValorNegativo() throws SQLException {
        Pago pago = pago(primeraCuota, FECHA, -1L, "REC-004");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(pago));
        assertEquals("El valor del pago debe ser mayor que cero.",
            error.getMessage());
        assertTrue(pagoDAO.listar().isEmpty());
    }

    @Test
    void rechazaFechaNula() throws SQLException {
        Pago pago = pago(primeraCuota, null, 40_000L, "REC-005");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(pago));
        assertEquals("La fecha del pago es obligatoria.", error.getMessage());
        assertTrue(pagoDAO.listar().isEmpty());
    }

    @Test
    void rechazaReciboVacio() throws SQLException {
        Pago pago = pago(primeraCuota, FECHA, 40_000L, "   ");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(pago));
        assertEquals("El número de recibo es obligatorio.",
            error.getMessage());
        assertTrue(pagoDAO.listar().isEmpty());
    }

    @Test
    void rechazaCuotaInexistente() throws SQLException {
        Pago cuotaCero = pago(primeraCuota, FECHA, 40_000L, "REC-006");
        cuotaCero.setCuotaId(0L);
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuotaCero));
        assertEquals("Seleccione una cuota válida.", error.getMessage());

        Pago cuotaFantasma = pago(primeraCuota, FECHA, 40_000L, "REC-007");
        cuotaFantasma.setCuotaId(999_999L);
        error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuotaFantasma));
        assertEquals("Seleccione una cuota válida.", error.getMessage());

        assertTrue(pagoDAO.listar().isEmpty());
    }

    @Test
    void actualizaFechaValorYReciboConservandoLaCuota() throws SQLException {
        Pago creado = service.registrar(
            pago(primeraCuota, FECHA, 40_000L, "REC-008"));

        Pago cambio = new Pago(
            creado.getId(), primeraCuota.getId(), FECHA.plusDays(3),
            45_000L, " REC-009 ");

        service.actualizar(cambio);

        Pago recuperado = pagoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(primeraCuota.getId(), recuperado.getCuotaId());
        assertEquals(FECHA.plusDays(3), recuperado.getFechaPago());
        assertEquals(45_000L, recuperado.getValor());
        assertEquals("REC-009", recuperado.getNumeroRecibo());
    }

    @Test
    void rechazaCambioDeCuotaAlEditar() throws SQLException {
        Pago creado = service.registrar(
            pago(primeraCuota, FECHA, 40_000L, "REC-010"));

        Pago cambio = new Pago(
            creado.getId(), segundaCuota.getId(), FECHA, 45_000L, "REC-011");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.actualizar(cambio));
        assertEquals("No se puede cambiar la cuota del pago.",
            error.getMessage());

        Pago recuperado = pagoDAO.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(primeraCuota.getId(), recuperado.getCuotaId());
        assertEquals(40_000L, recuperado.getValor());
    }

    @Test
    void rechazaActualizarUnPagoInexistente() throws SQLException {
        Pago inexistente = new Pago(
            999_999L, primeraCuota.getId(), FECHA, 40_000L, "REC-012");

        SQLException error = assertThrows(
            SQLException.class, () -> service.actualizar(inexistente));
        assertEquals("No se encontró el pago que se desea actualizar.",
            error.getMessage());
    }

    private static Pago pago(Cuota cuota, LocalDate fecha, long valor,
                             String recibo) {
        return new Pago(cuota.getId(), fecha, valor, recibo);
    }
}
