package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.AsociadoService;
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

class PanelPrestamosTest {

    @TempDir
    Path carpetaTemporal;

    private PanelPrestamos panel;

    @Test
    void muestraElPrestamoConElAsociadoCorrecto() throws Exception {
        Path rutaBd = carpetaTemporal.resolve("panel-prestamos.db");
        InicializadorBD.inicializar(rutaBd);
        AsociadoDAO asociadoDAO = new AsociadoDAO(rutaBd);
        PrestamoDAO prestamoDAO = new PrestamoDAO(rutaBd);
        AsociadoService asociadoService = new AsociadoService(asociadoDAO);
        PrestamoService prestamoService = new PrestamoService(prestamoDAO, asociadoDAO);

        Asociado asociado = asociadoService.registrar(
            new Asociado("11111111", "Ana", "Pérez", null, null));
        prestamoService.registrar(new Prestamo(
            asociado.getId(), 1_000_000L, LocalDate.of(2026, 10, 29),
            "12 meses", 12, 90_000L, Prestamo.ESTADO_VIGENTE));

        SwingUtilities.invokeAndWait(() ->
            panel = new PanelPrestamos(prestamoService, asociadoService));

        JTable tabla = componente(panel, JTable.class);
        assertEquals(1, tabla.getRowCount());
        assertEquals(7, tabla.getColumnCount());
        assertEquals("11111111 - Ana Pérez", tabla.getValueAt(0, 0));
        assertEquals("1.000.000", tabla.getValueAt(0, 1));
        assertEquals("2026-10-29", tabla.getValueAt(0, 2));
        assertEquals("12 meses", tabla.getValueAt(0, 3));
        assertEquals("12", tabla.getValueAt(0, 4));
        assertEquals("90.000", tabla.getValueAt(0, 5));
        assertEquals("Vigente", tabla.getValueAt(0, 6));

        JLabel titulo = componentes(panel, JLabel.class).stream()
            .filter(etiqueta -> "Préstamos registrados (1)".equals(etiqueta.getText()))
            .findFirst()
            .orElseThrow();
        assertEquals("Préstamos registrados (1)", titulo.getText());
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
