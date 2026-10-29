package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormularioPrestamoTest {

    @Test
    void aceptaMontosValidos() {
        assertEquals(1_500_000L,
            FormularioPrestamo.parsearMonto("1500000", "monto"));
        assertEquals(1_500_000L,
            FormularioPrestamo.parsearMonto("1.500.000", "monto"));
        assertEquals(1_500_000L,
            FormularioPrestamo.parsearMonto("1 500 000", "monto"));
        assertEquals(800_000L,
            FormularioPrestamo.parsearMonto(" 800000 ", "monto"));
    }

    @Test
    void rechazaMontosInvalidos() {
        for (String texto : List.of(
                "abc", "1.5", "1,5", "-3", "1.50.000", "1 5", "0.001", ".")) {
            IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> FormularioPrestamo.parsearMonto(texto, "monto"),
                "Caso: " + texto);
            assertTrue(error.getMessage().contains("no es válido"),
                "Caso: " + texto);
        }
    }

    @Test
    void rechazaMontoVacio() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioPrestamo.parsearMonto("", "monto"));

        assertTrue(error.getMessage().contains("obligatorio"));
    }

    @Test
    void rechazaMontoDesbordado() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioPrestamo.parsearMonto(
                "99999999999999999999", "monto"));

        assertTrue(error.getMessage().contains("demasiado grande"));
    }

    @Test
    void aceptaCuotasValidas() {
        assertEquals(12, FormularioPrestamo.parsearCuotas("12"));
    }

    @Test
    void rechazaCuotasInvalidas() {
        for (String texto : List.of("", "abc", "-1", "99999999999")) {
            assertThrows(IllegalArgumentException.class,
                () -> FormularioPrestamo.parsearCuotas(texto),
                "Caso: " + texto);
        }
    }

    @Test
    void aceptaFechaEnFormatoIso() {
        assertEquals(LocalDate.of(2026, 10, 29),
            FormularioPrestamo.parsearFecha("2026-10-29"));
    }

    @Test
    void rechazaFechasInvalidas() {
        for (String texto : List.of("2025-02-30", "30/01/2025")) {
            IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> FormularioPrestamo.parsearFecha(texto),
                "Caso: " + texto);
            assertEquals("Use el formato AAAA-MM-DD.", error.getMessage(),
                "Caso: " + texto);
        }
    }

    @Test
    void rechazaFechaVacia() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioPrestamo.parsearFecha(""));

        assertEquals("La fecha es obligatoria.", error.getMessage());
    }

    @Test
    void rechazaMontoConLetras() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> FormularioPrestamo.parsearMonto("1.5M", "monto"));

        assertTrue(error.getMessage().contains("no es válido"));
    }
}
