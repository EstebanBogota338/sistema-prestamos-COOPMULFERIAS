package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PagoDAOTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 12, 1);

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private PagoDAO dao;
    private Cuota primeraCuota;
    private Cuota segundaCuota;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        rutaBd = carpetaTemporal.resolve("pagos-dao.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new PagoDAO(rutaBd);

        AsociadoDAO asociadoDAO = new AsociadoDAO(rutaBd);
        PrestamoDAO prestamoDAO = new PrestamoDAO(rutaBd);
        CuotaDAO cuotaDAO = new CuotaDAO(rutaBd);

        Asociado asociado = new Asociado(
            "11111111", "Ana", "Pérez", null, LocalDate.now());
        asociadoDAO.crear(asociado);

        Prestamo prestamo = new Prestamo(
            asociado.getId(), 1_000_000L, LocalDate.of(2026, 10, 29),
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);
        prestamoDAO.crear(prestamo);

        primeraCuota = new Cuota(
            prestamo.getId(), 1, 90_000L, LocalDate.of(2026, 11, 30));
        segundaCuota = new Cuota(
            prestamo.getId(), 2, 80_000L, LocalDate.of(2026, 12, 30));
        cuotaDAO.crear(primeraCuota);
        cuotaDAO.crear(segundaCuota);
    }

    @Test
    void creaYRecuperaPagoConNuevaInstanciaDelDao() throws SQLException {
        Pago pago = new Pago(
            primeraCuota.getId(), FECHA, 40_000L, "REC-001");

        long id = dao.crear(pago);

        PagoDAO otroDao = new PagoDAO(rutaBd);
        Pago recuperado = otroDao.buscarPorId(id).orElseThrow();
        assertEquals(id, pago.getId());
        assertEquals(primeraCuota.getId(), recuperado.getCuotaId());
        assertEquals(FECHA, recuperado.getFechaPago());
        assertEquals(40_000L, recuperado.getValor());
        assertEquals("REC-001", recuperado.getNumeroRecibo());
    }

    @Test
    void listaPagosDeUnaCuotaYOrdenaPorFecha() throws SQLException {
        Pago posterior = new Pago(
            primeraCuota.getId(), FECHA, 30_000L, "REC-002");
        Pago anterior = new Pago(
            primeraCuota.getId(), FECHA.minusDays(5), 20_000L, "REC-003");
        Pago deOtraCuota = new Pago(
            segundaCuota.getId(), FECHA, 10_000L, "REC-004");
        dao.crear(posterior);
        dao.crear(anterior);
        dao.crear(deOtraCuota);

        List<Pago> dePrimera = dao.listarPorCuota(primeraCuota.getId());
        assertEquals(2, dePrimera.size());
        assertEquals(anterior.getId(), dePrimera.get(0).getId());
        assertEquals(posterior.getId(), dePrimera.get(1).getId());
        assertEquals(3, dao.listar().size());
    }

    @Test
    void rechazaCuotaInexistentePorForeignKey() {
        Pago pago = new Pago(999_999L, FECHA, 40_000L, "REC-005");

        assertThrows(SQLException.class, () -> dao.crear(pago));
    }

    @Test
    void actualizarConservaLaCuota() throws SQLException {
        Pago pago = new Pago(
            primeraCuota.getId(), FECHA, 40_000L, "REC-006");
        dao.crear(pago);

        Pago cambio = new Pago(
            pago.getId(), segundaCuota.getId(), FECHA.plusDays(1),
            45_000L, "REC-007");

        assertTrue(dao.actualizar(cambio));

        Pago recuperado = dao.buscarPorId(pago.getId()).orElseThrow();
        assertEquals(primeraCuota.getId(), recuperado.getCuotaId());
        assertEquals(FECHA.plusDays(1), recuperado.getFechaPago());
        assertEquals(45_000L, recuperado.getValor());
        assertEquals("REC-007", recuperado.getNumeroRecibo());
    }

    @Test
    void actualizarDevuelveFalseSiElPagoNoExiste() throws SQLException {
        Pago inexistente = new Pago(
            999_999L, primeraCuota.getId(), FECHA, 40_000L, "REC-008");

        assertFalse(dao.actualizar(inexistente));
    }
}
