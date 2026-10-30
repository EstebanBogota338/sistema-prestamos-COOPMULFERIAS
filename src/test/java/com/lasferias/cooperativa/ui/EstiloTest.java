package com.lasferias.cooperativa.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstiloTest {

    @Test
    void formateaMontosConSeparadorDeMilesColombiano() {
        assertEquals("1.500.000", Estilo.formatearMoneda(1_500_000L));
        assertEquals("1.000.000", Estilo.formatearMoneda(1_000_000L));
        assertEquals("999", Estilo.formatearMoneda(999L));
        assertEquals("0", Estilo.formatearMoneda(0L));
    }
}
