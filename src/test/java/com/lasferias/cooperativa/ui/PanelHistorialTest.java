package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.CuotaDAO;
import com.lasferias.cooperativa.dao.HistorialDAO;
import com.lasferias.cooperativa.dao.PagoDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.CuotaService;
import com.lasferias.cooperativa.service.HistorialService;
import com.lasferias.cooperativa.service.PagoService;
import com.lasferias.cooperativa.service.PrestamoService;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.time.LocalDate;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PanelHistorialTest {

    @TempDir
    Path carpetaTemporal;

    private PanelHistorial panel;

    @Test
    void muestraLosTresNivelesDelAsociadoSeleccionado() throws Exception {
        Path rutaBd = carpetaTemporal.resolve("panel-historial.db");
        InicializadorBD.inicializar(rutaBd);

        AsociadoDAO asociadoDAO = new AsociadoDAO(rutaBd);
        PrestamoDAO prestamoDAO = new PrestamoDAO(rutaBd);
        CuotaDAO cuotaDAO = new CuotaDAO(rutaBd);
        PagoDAO pagoDAO = new PagoDAO(rutaBd);

        AsociadoService asociadoService = new AsociadoService(asociadoDAO);
        PrestamoService prestamoService =
            new PrestamoService(prestamoDAO, asociadoDAO);
        CuotaService cuotaService = new CuotaService(cuotaDAO, prestamoDAO);
        PagoService pagoService = new PagoService(pagoDAO, cuotaDAO);
        HistorialService historialService =
            new HistorialService(new HistorialDAO(rutaBd));

        Asociado asociado = asociadoService.registrar(
            new Asociado("11111111", "Ana", "Pérez", null, null));
        Prestamo prestamo = prestamoService.registrar(new Prestamo(
            asociado.getId(), 1_000_000L, LocalDate.of(2026, 1, 1),
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE));
        Cuota cuotaUno = cuotaService.registrar(new Cuota(
            prestamo.getId(), 1, 100_000L, LocalDate.of(2026, 2, 1)));
        Cuota cuotaDos = cuotaService.registrar(new Cuota(
            prestamo.getId(), 2, 90_000L, LocalDate.of(2026, 3, 1)));

        pagoService.registrar(new Pago(
            cuotaUno.getId(), LocalDate.of(2026, 2, 2), 30_000L, "REC-001"));
        pagoService.registrar(new Pago(
            cuotaUno.getId(), LocalDate.of(2026, 2, 3), 20_000L, "REC-002"));
        pagoService.registrar(new Pago(
            cuotaDos.getId(), LocalDate.of(2026, 3, 2), 40_000L, "REC-003"));

        SwingUtilities.invokeAndWait(() -> {
            panel = new PanelHistorial(historialService, asociadoService);
            JComboBox<?> combo = buscar(panel, "comboAsociados", JComboBox.class);
            assertNotNull(combo, "No se encontró comboAsociados");
            combo.setSelectedIndex(1);
        });

        JTable tablaPrestamos =
            buscar(panel, "tablaPrestamos", JTable.class);
        JTable tablaCuotas = buscar(panel, "tablaCuotas", JTable.class);
        JTable tablaPagos = buscar(panel, "tablaPagos", JTable.class);
        assertNotNull(tablaPrestamos);
        assertNotNull(tablaCuotas);
        assertNotNull(tablaPagos);

        assertEquals(1, tablaPrestamos.getRowCount());
        assertEquals(2, tablaCuotas.getRowCount());
        assertEquals(3, tablaPagos.getRowCount());

        assertEquals("Préstamos (1)", buscar(panel, "tituloPrestamos",
            JLabel.class).getText());
        assertEquals("Cuotas (2)", buscar(panel, "tituloCuotas",
            JLabel.class).getText());
        assertEquals("Pagos (3)", buscar(panel, "tituloPagos",
            JLabel.class).getText());
        assertEquals("Estado: 11111111 - Ana Pérez",
            buscar(panel, "estadoHistorial", JLabel.class).getText());
    }

    private static <T extends Component> T buscar(
            Container raiz, String nombre, Class<T> tipo) {
        for (Component componente : raiz.getComponents()) {
            if (nombre.equals(componente.getName()) && tipo.isInstance(componente)) {
                return tipo.cast(componente);
            }
            if (componente instanceof Container hijo) {
                T encontrado = buscar(hijo, nombre, tipo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
