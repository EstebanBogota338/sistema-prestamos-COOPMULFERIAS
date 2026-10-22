package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.service.AsociadoService;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FormularioAsociadoTest {

    @TempDir
    Path carpetaTemporal;

    private JDialog formulario;

    @AfterEach
    void cerrarFormulario() throws Exception {
        if (formulario != null) {
            SwingUtilities.invokeAndWait(formulario::dispose);
        }
    }

    @Test
    void asociaLasEtiquetasPrincipalesConSusCampos() throws Exception {
        AsociadoService service = servicioConDaoTemporal();
        SwingUtilities.invokeAndWait(() ->
            formulario = new FormularioAsociado(null, service, null));

        assertEtiquetaAsociada("Documento *");
        assertEtiquetaAsociada("Nombres *");
        assertEtiquetaAsociada("Apellidos *");
        assertEtiquetaAsociada("Teléfono");
        assertEtiquetaAsociada("Fecha de registro");
    }

    @Test
    void registraDiagnosticoTecnicoSinExponerDetalleAlUsuario()
            throws Exception {
        AsociadoService service = new ServicioConFalla(carpetaTemporal);
        Logger logger = Logger.getLogger(FormularioAsociado.class.getName());
        List<LogRecord> registros = new CopyOnWriteArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord registro) {
                registros.add(registro);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        boolean usarPadres = logger.getUseParentHandlers();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        try {
            SwingUtilities.invokeAndWait(() ->
                formulario = new FormularioAsociado(null, service, null));

            SwingUtilities.invokeAndWait(() -> {
                List<JTextField> campos = componentes(formulario, JTextField.class);
                campos.get(0).setText("FALLA-SQL");
                campos.get(1).setText("Ana");
                campos.get(2).setText("Pérez");
                boton(formulario, "Guardar").doClick();
            });

            assertTrue(registros.stream().anyMatch(registro ->
                registro.getLevel() == Level.SEVERE
                    && registro.getThrown() instanceof SQLException));
            String mensajeVisible = componentes(formulario, JLabel.class).stream()
                .map(JLabel::getText)
                .filter(texto -> texto != null)
                .filter(texto -> texto.contains("No se pudo guardar"))
                .findFirst()
                .orElse("");
            assertEquals(
                "No se pudo guardar el asociado. Intente nuevamente.",
                mensajeVisible);
            assertFalse(mensajeVisible.contains("fallo simulado"));
        } finally {
            logger.removeHandler(handler);
            logger.setUseParentHandlers(usarPadres);
        }
    }

    private AsociadoService servicioConDaoTemporal() {
        Path rutaBd = carpetaTemporal.resolve("formulario-asociados.db");
        return new AsociadoService(new AsociadoDAO(rutaBd));
    }

    private void assertEtiquetaAsociada(String texto) {
        JLabel etiqueta = componentes(formulario, JLabel.class).stream()
            .filter(label -> texto.equals(label.getText()))
            .findFirst()
            .orElseThrow();
        assertNotNull(etiqueta.getLabelFor(),
            "La etiqueta " + texto + " no está asociada a un campo");
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

    private static final class ServicioConFalla extends AsociadoService {

        ServicioConFalla(Path carpetaTemporal) {
            super(new AsociadoDAO(carpetaTemporal.resolve("servicio-fallido.db")));
        }

        @Override
        public Asociado registrar(Asociado asociado) throws SQLException {
            throw new SQLException("fallo simulado");
        }
    }
}
