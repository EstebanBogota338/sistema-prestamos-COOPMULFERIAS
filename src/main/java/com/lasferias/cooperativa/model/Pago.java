package com.lasferias.cooperativa.model;

import java.time.LocalDate;

public class Pago {

    private long id;
    private long cuotaId;
    private LocalDate fechaPago;
    private long valor;
    private String numeroRecibo;

    public Pago() {
    }

    public Pago(long cuotaId, LocalDate fechaPago, long valor,
                String numeroRecibo) {
        this(0, cuotaId, fechaPago, valor, numeroRecibo);
    }

    public Pago(long id, long cuotaId, LocalDate fechaPago, long valor,
                String numeroRecibo) {
        this.id = id;
        this.cuotaId = cuotaId;
        this.fechaPago = fechaPago;
        this.valor = valor;
        this.numeroRecibo = numeroRecibo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCuotaId() {
        return cuotaId;
    }

    public void setCuotaId(long cuotaId) {
        this.cuotaId = cuotaId;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public long getValor() {
        return valor;
    }

    public void setValor(long valor) {
        this.valor = valor;
    }

    public String getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }
}
