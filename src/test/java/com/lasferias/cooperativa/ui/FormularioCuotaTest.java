package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lasferias.cooperativa.model.Cuota;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FormularioCuotaTest {

    private JDialog formulario;

    @AfterEach
    void cerrarFormulario() throws Exception {
        if (formulario != null) {
            SwingUtilities.invokeAndWait(formulario::dispose);
        }
    }

    @Test
    void aceptaValorValido() {
        assertEquals(1_500_000L,
            FormularioCuota.parsearMonto("1500000", "valor"));
        assertEquals(1_500_000L,
            FormularioCuota.parsearMonto("1.500.000", "valor"));
    }

    @Test
    void rechazaValorInvalido() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioCuota.parsearMonto("abc", "valor"));

        assertTrue(error.getMessage().contains("no es válido"));
    }

    @Test
    void aceptaFechaEnFormatoIso() {
        assertEquals(LocalDate.of(2026, 11, 30),
            FormularioCuota.parsearFecha("2026-11-30"));
    }

    @Test
    void rechazaFechaVacia() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioCuota.parsearFecha(""));

        assertEquals("La fecha es obligatoria.", error.getMessage());
    }

    @Test
    void deshabilitaLosCamposCuandoLaCuotaTienePagos() throws Exception {
        Cuota existente = new Cuota(
            1L, 1L, 1, 90_000L, LocalDate.of(2026, 11, 30));

        SwingUtilities.invokeAndWait(() ->
            formulario = new FormularioCuota(
                null, null, List.of(), List.of(), existente, true));

        List<JTextField> campos = componentes(formulario, JTextField.class);
        assertEquals(3, campos.size());
        for (JTextField campo : campos) {
            assertFalse(campo.isEnabled(), "Campo habilitado: " + campo.getName());
        }
        assertFalse(boton("Guardar").isEnabled());
        assertTrue(componentes(formulario, JLabel.class).stream()
            .map(JLabel::getText)
            .filter(texto -> texto != null)
            .anyMatch(texto -> texto.startsWith(
                "Esta cuota tiene pagos registrados")));
    }

    private JButton boton(String texto) {
        return componentes(formulario, JButton.class).stream()
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
