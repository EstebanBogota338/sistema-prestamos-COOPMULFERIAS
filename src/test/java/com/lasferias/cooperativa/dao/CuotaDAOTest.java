package com.lasferias.cooperativa.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Prestamo;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CuotaDAOTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 30);

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private CuotaDAO dao;
    private PrestamoDAO prestamoDAO;
    private AsociadoDAO asociadoDAO;
    private Prestamo prestamo;
    private Prestamo otroPrestamo;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException {
        rutaBd = carpetaTemporal.resolve("cuotas-dao.db");
        InicializadorBD.inicializar(rutaBd);
        dao = new CuotaDAO(rutaBd);
        prestamoDAO = new PrestamoDAO(rutaBd);
        asociadoDAO = new AsociadoDAO(rutaBd);
        Asociado asociado = crearAsociado("11111111");
        prestamo = crearPrestamo(asociado);
        otroPrestamo = crearPrestamo(asociado);
    }

    @Test
    void creaYRecuperaCuota() throws SQLException {
        Cuota cuota = cuota(prestamo, 1, 90_000L, FECHA);

        long id = dao.crear(cuota);
        Cuota recuperada = dao.buscarPorId(id).orElseThrow();

        assertEquals(id, cuota.getId());
        assertEquals(prestamo.getId(), recuperada.getPrestamoId());
        assertEquals(1, recuperada.getNumeroCuota());
        assertEquals(90_000L, recuperada.getValor());
        assertEquals(FECHA, recuperada.getFechaVencimiento());
    }

    @Test
    void listaCuotasDeUnPrestamoOrdenadasPorNumero() throws SQLException {
        Cuota segunda = cuota(prestamo, 2, 80_000L, FECHA.plusDays(30));
        Cuota primera = cuota(prestamo, 1, 90_000L, FECHA);
        Cuota deOtro = cuota(otroPrestamo, 1, 70_000L, FECHA);
        dao.crear(segunda);
        dao.crear(primera);
        dao.crear(deOtro);

        List<Cuota> delPrestamo = dao.listarPorPrestamo(prestamo.getId());
        assertEquals(2, delPrestamo.size());
        assertEquals(List.of(1, 2),
            delPrestamo.stream().map(Cuota::getNumeroCuota).toList());

        List<Cuota> todas = dao.listar();
        assertEquals(List.of(1, 2, 1),
            todas.stream().map(Cuota::getNumeroCuota).toList());
    }

    @Test
    void rechazaPrestamoInexistentePorForeignKey() {
        Cuota cuota = cuota(prestamo, 1, 90_000L, FECHA);
        cuota.setPrestamoId(999_999L);

        assertThrows(SQLException.class, () -> dao.crear(cuota));
    }

    @Test
    void rechazaNumeroDeCuotaRepetidoEnElMismoPrestamo() throws SQLException {
        dao.crear(cuota(prestamo, 1, 90_000L, FECHA));

        Cuota duplicada = cuota(prestamo, 1, 80_000L, FECHA.plusDays(1));

        assertThrows(SQLException.class, () -> dao.crear(duplicada));
    }

    @Test
    void actualizarNoCambiaElPrestamo() throws SQLException {
        Cuota creada = cuota(prestamo, 1, 90_000L, FECHA);
        dao.crear(creada);

        Cuota cambio = new Cuota(
            creada.getId(), otroPrestamo.getId(), 2, 80_000L,
            FECHA.plusDays(5));

        assertTrue(dao.actualizar(cambio));

        Cuota recuperada = dao.buscarPorId(creada.getId()).orElseThrow();
        assertEquals(prestamo.getId(), recuperada.getPrestamoId());
        assertEquals(2, recuperada.getNumeroCuota());
        assertEquals(80_000L, recuperada.getValor());
        assertEquals(FECHA.plusDays(5), recuperada.getFechaVencimiento());
    }

    @Test
    void actualizarNoModificaUnaCuotaConPagos() throws SQLException {
        Cuota creada = cuota(prestamo, 1, 90_000L, FECHA);
        dao.crear(creada);
        insertarPago(creada.getId());

        assertTrue(dao.tienePagos(creada.getId()));
        Cuota intento = new Cuota(
            creada.getId(), prestamo.getId(), 2, 80_000L,
            FECHA.plusDays(9));

        assertFalse(dao.actualizar(intento));

        Cuota recuperada = dao.buscarPorId(creada.getId()).orElseThrow();
        assertEquals(1, recuperada.getNumeroCuota());
        assertEquals(90_000L, recuperada.getValor());
        assertEquals(FECHA, recuperada.getFechaVencimiento());
    }

    private Asociado crearAsociado(String documento) throws SQLException {
        Asociado nuevo = new Asociado(
            documento, "Ana", "Pérez", null, LocalDate.now());
        asociadoDAO.crear(nuevo);
        return nuevo;
    }

    private Prestamo crearPrestamo(Asociado asociado) throws SQLException {
        Prestamo nuevo = new Prestamo(
            asociado.getId(), 1_000_000L, LocalDate.of(2026, 10, 29),
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE);
        prestamoDAO.crear(nuevo);
        return nuevo;
    }

    private static Cuota cuota(Prestamo prestamo, int numero, long valor,
                               LocalDate fechaVencimiento) {
        return new Cuota(prestamo.getId(), numero, valor, fechaVencimiento);
    }

    private void insertarPago(long cuotaId) throws SQLException {
        String sql = """
            INSERT INTO pago(cuota_id, fecha_pago, valor, numero_recibo)
            VALUES (?, ?, ?, ?)""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBd);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, cuotaId);
            ps.setString(2, "2026-11-01");
            ps.setLong(3, 50_000L);
            ps.setString(4, "REC-001");
            ps.executeUpdate();
        }
    }
}
