package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.service.AsociadoService;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PanelAsociadosTest {

    @TempDir
    Path carpetaTemporal;

    private PanelAsociados panel;

    @Test
    void diferenciaTotalRegistradoDeResultadosFiltrados() throws Exception {
        AsociadoService service = crearServicioConDosAsociados();

        SwingUtilities.invokeAndWait(() -> panel = new PanelAsociados(service));

        assertEquals("Asociados registrados (2)", buscarTitulo().getText());

        SwingUtilities.invokeAndWait(() -> {
            componente(panel, JTextField.class).setText("Ana");
            boton(panel, "Buscar").doClick();
        });

        assertEquals(1, componente(panel, JTable.class).getRowCount());
        assertEquals("Resultados de búsqueda (1)", buscarTitulo().getText());

        SwingUtilities.invokeAndWait(() -> boton(panel, "Limpiar").doClick());

        assertEquals(2, componente(panel, JTable.class).getRowCount());
        assertEquals("Asociados registrados (2)", buscarTitulo().getText());
    }

    private AsociadoService crearServicioConDosAsociados() throws SQLException {
        Path rutaBd = carpetaTemporal.resolve("panel-asociados.db");
        InicializadorBD.inicializar(rutaBd);
        AsociadoService service = new AsociadoService(new AsociadoDAO(rutaBd));
        service.registrar(new Asociado(
            "11111111", "Ana", "Pérez", null, null));
        service.registrar(new Asociado(
            "22222222", "Carlos", "Gómez", null, null));
        return service;
    }

    private JLabel buscarTitulo() {
        return componentes(panel, JLabel.class).stream()
            .filter(etiqueta -> etiqueta.getText() != null)
            .filter(etiqueta -> etiqueta.getText().startsWith(
                "Asociados registrados")
                || etiqueta.getText().startsWith("Resultados de búsqueda"))
            .findFirst()
            .orElseThrow();
    }

    private static <T extends Component> T componente(Container raiz, Class<T> tipo) {
        List<T> encontrados = componentes(raiz, tipo);
        assertFalse(encontrados.isEmpty(), "No se encontró el componente " + tipo);
        return encontrados.get(0);
    }

    private static JButton boton(Container raiz, String texto) {
        return componentes(raiz, JButton.class).stream()
            .filter(boton -> texto.equals(boton.getText()))
            .findFirst()
            .orElseThrow();
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
