package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.CuotaDAO;
import com.lasferias.cooperativa.dao.PagoDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.CuotaService;
import com.lasferias.cooperativa.service.PagoService;
import com.lasferias.cooperativa.service.PrestamoService;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PanelPagosTest {

    @TempDir
    Path carpetaTemporal;

    private PanelPagos panel;

    @Test
    void muestraElPagoConLaCuotaCorrecta() throws Exception {
        Path rutaBd = carpetaTemporal.resolve("panel-pagos.db");
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

        Asociado asociado = asociadoService.registrar(
            new Asociado("11111111", "Ana", "Pérez", null, null));
        Prestamo prestamo = prestamoService.registrar(new Prestamo(
            asociado.getId(), 1_000_000L, LocalDate.of(2026, 10, 29),
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE));
        Cuota cuota = cuotaService.registrar(new Cuota(
            prestamo.getId(), 1, 100_000L, LocalDate.of(2026, 11, 30)));
        pagoService.registrar(new Pago(
            cuota.getId(), LocalDate.of(2026, 12, 1), 40_000L, "REC-001"));

        SwingUtilities.invokeAndWait(() -> panel = new PanelPagos(
            pagoService, cuotaService, prestamoService, asociadoService));

        JTable tabla = componente(panel, JTable.class);
        assertEquals(1, tabla.getRowCount());
        assertEquals(4, tabla.getColumnCount());
        assertEquals(
            "11111111 - Ana Pérez - 2026-10-29 - 1000000 - Cuota 1",
            tabla.getValueAt(0, 0));
        assertEquals("2026-12-01", tabla.getValueAt(0, 1));
        assertEquals("40000", tabla.getValueAt(0, 2));
        assertEquals("REC-001", tabla.getValueAt(0, 3));

        JLabel estado = componentes(panel, JLabel.class).stream()
            .filter(etiqueta -> "Estado: 1 pago".equals(etiqueta.getText()))
            .findFirst()
            .orElseThrow();
        assertEquals("Estado: 1 pago", estado.getText());

        JLabel titulo = componentes(panel, JLabel.class).stream()
            .filter(etiqueta -> "Pagos registrados (1)".equals(etiqueta.getText()))
            .findFirst()
            .orElseThrow();
        assertEquals("Pagos registrados (1)", titulo.getText());
    }

    private static <T extends Component> T componente(Container raiz, Class<T> tipo) {
        List<T> encontrados = componentes(raiz, tipo);
        assertFalse(encontrados.isEmpty(), "No se encontró el componente " + tipo);
        return encontrados.get(0);
    }

    private static <T extends Component> List<T> componentes(
            Container raiz, Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        recolectar(raiz, tipo, encontrados);
        return encontrados;
    }

    private static <T extends Component> void recolectar(
            Container contenedor, Class<T> tipo, List<T> encontrados) {
        for (Component componente : contenedor.getComponents()) {
            if (tipo.isInstance(componente)) {
                encontrados.add(tipo.cast(componente));
            }
            if (componente instanceof Container hijo) {
                recolectar(hijo, tipo, encontrados);
            }
        }
    }
}
