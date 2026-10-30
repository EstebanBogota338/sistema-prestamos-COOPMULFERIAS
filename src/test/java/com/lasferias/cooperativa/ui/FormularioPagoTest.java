package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FormularioPagoTest {

    @Test
    void aceptaValorValido() {
        assertEquals(1_500_000L,
            FormularioPago.parsearMonto("1500000", "valor"));
        assertEquals(1_500_000L,
            FormularioPago.parsearMonto("1.500.000", "valor"));
        assertEquals(800_000L,
            FormularioPago.parsearMonto(" 800000 ", "valor"));
    }

    @Test
    void rechazaValorInvalido() {
        for (String texto : new String[]{"1.5", "0.001", "0.800", "abc"}) {
            IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> FormularioPago.parsearMonto(texto, "valor"));
            assertTrue(error.getMessage().contains("no es válido"),
                "Texto aceptado indebidamente: " + texto);
        }
    }

    @Test
    void aceptaFechaEnFormatoIso() {
        assertEquals(LocalDate.of(2026, 12, 1),
            FormularioPago.parsearFecha("2026-12-01"));
    }

    @Test
    void rechazaFechaVacia() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioPago.parsearFecha(""));
        assertEquals("La fecha es obligatoria.", error.getMessage());
    }

    @Test
    void rechazaFechaConFormatoInvalido() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioPago.parsearFecha("01/12/2026"));
        assertEquals("Use el formato AAAA-MM-DD.", error.getMessage());
    }
}
