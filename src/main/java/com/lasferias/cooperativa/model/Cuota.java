package com.lasferias.cooperativa.model;

import java.time.LocalDate;

public class Cuota {

    private long id;
    private long prestamoId;
    private int numeroCuota;
    private long valor;
    private LocalDate fechaVencimiento;

    public Cuota() {
    }

    public Cuota(long prestamoId, int numeroCuota, long valor,
                 LocalDate fechaVencimiento) {
        this(0, prestamoId, numeroCuota, valor, fechaVencimiento);
    }

    public Cuota(long id, long prestamoId, int numeroCuota, long valor,
                 LocalDate fechaVencimiento) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.numeroCuota = numeroCuota;
        this.valor = valor;
        this.fechaVencimiento = fechaVencimiento;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(long prestamoId) {
        this.prestamoId = prestamoId;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public long getValor() {
        return valor;
    }

    public void setValor(long valor) {
        this.valor = valor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
