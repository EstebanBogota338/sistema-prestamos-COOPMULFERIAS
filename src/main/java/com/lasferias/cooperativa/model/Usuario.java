package com.lasferias.cooperativa.model;

public class Usuario {

    private long id;
    private String nombreUsuario;
    private String contrasenaHash;

    public Usuario() {
    }

    public Usuario(String nombreUsuario, String contrasenaHash) {
        this(0, nombreUsuario, contrasenaHash);
    }

    public Usuario(long id, String nombreUsuario, String contrasenaHash) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = contrasenaHash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }
}
