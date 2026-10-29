package com.lasferias.cooperativa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.CuotaDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CuotaServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 30);

    @TempDir
    Path carpetaTemporal;

    private Path rutaBd;
    private AsociadoDAO asociadoDAO;
    private PrestamoDAO prestamoDAO;
    private CuotaDAO cuotaDAO;
    private CuotaService service;
    private Asociado asociado;
    private Prestamo prestamo;
    private Prestamo otroPrestamo;

    @BeforeEach
    void prepararServicio() throws SQLException {
        rutaBd = carpetaTemporal.resolve("cuotas-service.db");
        InicializadorBD.inicializar(rutaBd);
        asociadoDAO = new AsociadoDAO(rutaBd);
        prestamoDAO = new PrestamoDAO(rutaBd);
        cuotaDAO = new CuotaDAO(rutaBd);
        service = new CuotaService(cuotaDAO, prestamoDAO);
        asociado = crearAsociado("11111111");
        prestamo = crearPrestamo(asociado);
        otroPrestamo = crearPrestamo(asociado);
    }

    @Test
    void registraCuotaValida() throws SQLException {
        Cuota cuota = cuota(prestamo, 1, 90_000L, FECHA);
        cuota.setId(77L);

        Cuota creada = service.registrar(cuota);

        assertTrue(creada.getId() > 0);
        assertNotEquals(77L, creada.getId());
        Cuota recuperada = cuotaDAO.buscarPorId(creada.getId()).orElseThrow();
        assertEquals(prestamo.getId(), recuperada.getPrestamoId());
        assertEquals(1, recuperada.getNumeroCuota());
        assertEquals(90_000L, recuperada.getValor());
        assertEquals(FECHA, recuperada.getFechaVencimiento());
    }

    @Test
    void rechazaPrestamoInexistente() throws SQLException {
        Cuota sinPrestamo = cuota(prestamo, 1, 90_000L, FECHA);
        sinPrestamo.setPrestamoId(0L);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(sinPrestamo));
        assertEquals("Seleccione un préstamo válido.", error.getMessage());

        Cuota inexistente = cuota(prestamo, 1, 90_000L, FECHA);
        inexistente.setPrestamoId(999_999L);
        error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(inexistente));
        assertEquals("Seleccione un préstamo válido.", error.getMessage());

        assertTrue(cuotaDAO.listar().isEmpty());
    }

    @Test
    void rechazaNumeroDeCuotaNoPositivo() throws SQLException {
        Cuota cuota = cuota(prestamo, 0, 90_000L, FECHA);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuota));
        assertEquals("El número de cuota debe ser mayor o igual a 1.",
            error.getMessage());

        cuota.setNumeroCuota(-1);
        error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuota));
        assertEquals("El número de cuota debe ser mayor o igual a 1.",
            error.getMessage());
    }

    @Test
    void rechazaValorNoPositivo() throws SQLException {
        Cuota cuota = cuota(prestamo, 1, 0L, FECHA);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuota));
        assertEquals("El valor de la cuota debe ser mayor que cero.",
            error.getMessage());

        cuota.setValor(-1L);
        error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuota));
        assertEquals("El valor de la cuota debe ser mayor que cero.",
            error.getMessage());
    }

    @Test
    void rechazaFechaNula() throws SQLException {
        Cuota cuota = cuota(prestamo, 1, 90_000L, null);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.registrar(cuota));
        assertEquals("La fecha de vencimiento es obligatoria.",
            error.getMessage());
    }

    @Test
    void rechazaNumeroDeCuotaDuplicadoConMensajeClaro() throws SQLException {
        Cuota primera = service.registrar(cuota(prestamo, 1, 90_000L, FECHA));
        service.registrar(cuota(prestamo, 2, 80_000L, FECHA.plusDays(30)));

        Cuota duplicadaAlta = cuota(prestamo, 1, 70_000L, FECHA.plusDays(1));
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrar(duplicadaAlta));
        assertEquals("Ya existe una cuota con ese número en este préstamo.",
            error.getMessage());
        assertEquals(2, cuotaDAO.listarPorPrestamo(prestamo.getId()).size());

        Cuota edicionMismoNumero = new Cuota(
            primera.getId(), prestamo.getId(), 1, 95_000L, FECHA.plusDays(2));
        service.actualizar(edicionMismoNumero);

        Cuota recuperada = cuotaDAO.buscarPorId(primera.getId()).orElseThrow();
        assertEquals(1, recuperada.getNumeroCuota());
        assertEquals(95_000L, recuperada.getValor());

        Cuota edicionNumeroAjeno = new Cuota(
            primera.getId(), prestamo.getId(), 2, 96_000L, FECHA.plusDays(3));
        error = assertThrows(
            IllegalArgumentException.class,
            () -> service.actualizar(edicionNumeroAjeno));
        assertEquals("Ya existe una cuota con ese número en este préstamo.",
            error.getMessage());

        recuperada = cuotaDAO.buscarPorId(primera.getId()).orElseThrow();
        assertEquals(1, recuperada.getNumeroCuota());
        assertEquals(95_000L, recuperada.getValor());
    }

    @Test
    void permiteEditarCuotaSinPagos() throws SQLException {
        Cuota creada = service.registrar(cuota(prestamo, 1, 90_000L, FECHA));
        Cuota cambio = new Cuota(
            creada.getId(), prestamo.getId(), 3, 95_000L, FECHA.plusDays(5));

        service.actualizar(cambio);

        Cuota recuperada = cuotaDAO.buscarPorId(creada.getId()).orElseThrow();
        assertEquals(3, recuperada.getNumeroCuota());
        assertEquals(95_000L, recuperada.getValor());
        assertEquals(FECHA.plusDays(5), recuperada.getFechaVencimiento());
    }

    @Test
    void rechazaEditarCuotaConPagosYNoAlteraLaFila() throws SQLException {
        Cuota creada = service.registrar(cuota(prestamo, 1, 90_000L, FECHA));
        insertarPago(creada.getId());

        Cuota intento = new Cuota(
            creada.getId(), prestamo.getId(), 2, 80_000L, FECHA.plusDays(9));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.actualizar(intento));
        assertEquals(
            "Esta cuota tiene pagos registrados: no se puede modificar su "
                + "número, valor ni fecha de vencimiento.",
            error.getMessage());

        Cuota recuperada = cuotaDAO.buscarPorId(creada.getId()).orElseThrow();
        assertEquals(1, recuperada.getNumeroCuota());
        assertEquals(90_000L, recuperada.getValor());
        assertEquals(FECHA, recuperada.getFechaVencimiento());
    }

    @Test
    void rechazaCambioDePrestamoAlEditar() throws SQLException {
        Cuota creada = service.registrar(cuota(prestamo, 1, 90_000L, FECHA));
        insertarPago(creada.getId());

        Cuota intento = new Cuota(
            creada.getId(), otroPrestamo.getId(), 1, 90_000L, FECHA);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class, () -> service.actualizar(intento));
        assertEquals("No se puede cambiar el préstamo de la cuota.",
            error.getMessage());

        Cuota recuperada = cuotaDAO.buscarPorId(creada.getId()).orElseThrow();
        assertEquals(prestamo.getId(), recuperada.getPrestamoId());
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
