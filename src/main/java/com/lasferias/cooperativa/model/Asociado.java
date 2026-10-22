package com.lasferias.cooperativa.model;

import java.time.LocalDate;

public class Asociado {

    private long id;
    private String documento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private LocalDate fechaRegistro;

    public Asociado() {
    }

    public Asociado(String documento, String nombres, String apellidos,
                    String telefono, LocalDate fechaRegistro) {
        this(0, documento, nombres, apellidos, telefono, fechaRegistro);
    }

    public Asociado(long id, String documento, String nombres, String apellidos,
                    String telefono, LocalDate fechaRegistro) {
        this.id = id;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
