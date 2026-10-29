package com.lasferias.cooperativa.model;

import java.time.LocalDate;

public class Prestamo {

    public static final String ESTADO_VIGENTE = "Vigente";
    public static final String ESTADO_FINALIZADO = "Finalizado";

    private long id;
    private long asociadoId;
    private long monto;
    private LocalDate fecha;
    private String plazo;
    private int numeroCuotas;
    private long valorCuota;
    private String estado;

    public Prestamo() {
    }

    public Prestamo(long asociadoId, long monto, LocalDate fecha, String plazo,
                    int numeroCuotas, long valorCuota, String estado) {
        this(0, asociadoId, monto, fecha, plazo, numeroCuotas, valorCuota, estado);
    }

    public Prestamo(long id, long asociadoId, long monto, LocalDate fecha,
                    String plazo, int numeroCuotas, long valorCuota, String estado) {
        this.id = id;
        this.asociadoId = asociadoId;
        this.monto = monto;
        this.fecha = fecha;
        this.plazo = plazo;
        this.numeroCuotas = numeroCuotas;
        this.valorCuota = valorCuota;
        this.estado = estado;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAsociadoId() {
        return asociadoId;
    }

    public void setAsociadoId(long asociadoId) {
        this.asociadoId = asociadoId;
    }

    public long getMonto() {
        return monto;
    }

    public void setMonto(long monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getPlazo() {
        return plazo;
    }

    public void setPlazo(String plazo) {
        this.plazo = plazo;
    }

    public int getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(int numeroCuotas) {
        this.numeroCuotas = numeroCuotas;
    }

    public long getValorCuota() {
        return valorCuota;
    }

    public void setValorCuota(long valorCuota) {
        this.valorCuota = valorCuota;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
