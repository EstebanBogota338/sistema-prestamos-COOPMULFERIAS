package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class HistorialDAOTest {

    @TempDir
    Path carpetaTemporal;

    private HistorialDAO historial;
    private AsociadoDAO asociadoDAO;
    private PrestamoDAO prestamoDAO;
    private CuotaDAO cuotaDAO;
    private PagoDAO pagoDAO;
    private Asociado ana;
    private Asociado luis;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("historial-dao.db");
        InicializadorBD.inicializar(rutaBd);
        historial = new HistorialDAO(rutaBd);
        asociadoDAO = new AsociadoDAO(rutaBd);
        prestamoDAO = new PrestamoDAO(rutaBd);
        cuotaDAO = new CuotaDAO(rutaBd);
        pagoDAO = new PagoDAO(rutaBd);

        ana = new Asociado("11111111", "Ana", "Pérez", null,
            LocalDate.of(2026, 1, 15));
        luis = new Asociado("22222222", "Luis", "Gómez", null,
            LocalDate.of(2026, 1, 16));
        asociadoDAO.crear(ana);
        asociadoDAO.crear(luis);
    }

    @Test
    void listaPrestamosDeUnAsociado() throws SQLException {
        Prestamo febrero = crearPrestamo(ana, LocalDate.of(2026, 2, 1), 200_000L);
        Prestamo enero = crearPrestamo(ana, LocalDate.of(2026, 1, 1), 100_000L);
        Prestamo deLuis = crearPrestamo(luis, LocalDate.of(2026, 3, 1), 300_000L);

        List<Prestamo> prestamos = historial.listarPrestamos(ana.getId());

        assertEquals(2, prestamos.size());
        assertEquals(enero.getId(), prestamos.get(0).getId());
        assertEquals(febrero.getId(), prestamos.get(1).getId());
        assertFalse(prestamos.stream().anyMatch(p -> p.getId() == deLuis.getId()));
    }

    @Test
    void listaCuotasDeUnAsociadoSinDuplicar() throws SQLException {
        Prestamo prestamo = crearPrestamo(ana, LocalDate.of(2026, 1, 1), 100_000L);
        Cuota cuota = crearCuota(prestamo, 1, 50_000L,
            LocalDate.of(2026, 2, 1));
        crearPago(cuota, LocalDate.of(2026, 2, 2), 20_000L, "REC-001");
        crearPago(cuota, LocalDate.of(2026, 2, 3), 10_000L, "REC-002");

        List<Cuota> cuotas = historial.listarCuotas(ana.getId());

        assertEquals(1, cuotas.size());
        assertEquals(cuota.getId(), cuotas.get(0).getId());
    }

    @Test
    void listaPagosDeUnAsociado() throws SQLException {
        Prestamo prestamoAna = crearPrestamo(ana,
            LocalDate.of(2026, 1, 1), 100_000L);
        Cuota cuotaAna = crearCuota(prestamoAna, 1, 50_000L,
            LocalDate.of(2026, 2, 1));
        Pago primerPago = crearPago(cuotaAna,
            LocalDate.of(2026, 2, 2), 20_000L, "REC-001");
        Pago segundoPago = crearPago(cuotaAna,
            LocalDate.of(2026, 2, 3), 10_000L, "REC-002");

        Prestamo prestamoLuis = crearPrestamo(luis,
            LocalDate.of(2026, 1, 1), 80_000L);
        Cuota cuotaLuis = crearCuota(prestamoLuis, 1, 40_000L,
            LocalDate.of(2026, 2, 1));
        Pago pagoLuis = crearPago(cuotaLuis,
            LocalDate.of(2026, 2, 4), 30_000L, "REC-003");

        List<Pago> pagos = historial.listarPagos(ana.getId());

        assertEquals(2, pagos.size());
        assertEquals(primerPago.getId(), pagos.get(0).getId());
        assertEquals(segundoPago.getId(), pagos.get(1).getId());
        assertFalse(pagos.stream().anyMatch(p -> p.getId() == pagoLuis.getId()));
    }

    @Test
    void noDevuelveNadaDeOtroAsociado() throws SQLException {
        Prestamo prestamoAna = crearPrestamo(ana,
            LocalDate.of(2026, 1, 1), 100_000L);
        Cuota cuotaAna = crearCuota(prestamoAna, 1, 50_000L,
            LocalDate.of(2026, 2, 1));
        Pago pagoAna = crearPago(cuotaAna,
            LocalDate.of(2026, 2, 2), 20_000L, "REC-001");

        Prestamo prestamoLuis = crearPrestamo(luis,
            LocalDate.of(2026, 1, 1), 100_000L);
        Cuota cuotaLuis = crearCuota(prestamoLuis, 1, 50_000L,
            LocalDate.of(2026, 2, 1));
        Pago pagoLuis = crearPago(cuotaLuis,
            LocalDate.of(2026, 2, 2), 20_000L, "REC-001");

        List<Prestamo> prestamos = historial.listarPrestamos(ana.getId());
        List<Cuota> cuotas = historial.listarCuotas(ana.getId());
        List<Pago> pagos = historial.listarPagos(ana.getId());

        assertEquals(List.of(prestamoAna.getId()),
            prestamos.stream().map(Prestamo::getId).toList());
        assertEquals(List.of(cuotaAna.getId()),
            cuotas.stream().map(Cuota::getId).toList());
        assertEquals(List.of(pagoAna.getId()),
            pagos.stream().map(Pago::getId).toList());

        assertFalse(prestamos.stream().anyMatch(p -> p.getId() == prestamoLuis.getId()));
        assertFalse(cuotas.stream().anyMatch(c -> c.getId() == cuotaLuis.getId()));
        assertFalse(pagos.stream().anyMatch(p -> p.getId() == pagoLuis.getId()));
    }

    private Prestamo crearPrestamo(Asociado asociado, LocalDate fecha,
                                   long monto) throws SQLException {
        Prestamo prestamo = new Prestamo(
            asociado.getId(), monto, fecha, "12 meses", 12, 50_000L,
            Prestamo.ESTADO_VIGENTE);
        prestamoDAO.crear(prestamo);
        return prestamo;
    }

    private Cuota crearCuota(Prestamo prestamo, int numero, long valor,
                             LocalDate vencimiento) throws SQLException {
        Cuota cuota = new Cuota(
            prestamo.getId(), numero, valor, vencimiento);
        cuotaDAO.crear(cuota);
        return cuota;
    }

    private Pago crearPago(Cuota cuota, LocalDate fecha, long valor,
                           String recibo) throws SQLException {
        Pago pago = new Pago(cuota.getId(), fecha, valor, recibo);
        pagoDAO.crear(pago);
        return pago;
    }
}
