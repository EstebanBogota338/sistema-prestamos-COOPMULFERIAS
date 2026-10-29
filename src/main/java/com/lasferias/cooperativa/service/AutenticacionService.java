package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.UsuarioDAO;
import com.lasferias.cooperativa.model.Usuario;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AutenticacionService {

    public static final String NOMBRE_ADMINISTRADOR = "admin";
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService() {
        this(new UsuarioDAO());
    }

    public AutenticacionService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = Objects.requireNonNull(usuarioDAO,
            "El DAO de usuarios es obligatorio");
    }

    public boolean debeCrearAdministrador() throws SQLException {
        return !usuarioDAO.existeAlgunUsuario();
    }

    public void crearAdministrador(String contrasena, String confirmacion)
            throws SQLException {
        if (usuarioDAO.existeAlgunUsuario()) {
            throw new IllegalArgumentException("Ya existe un administrador registrado.");
        }
        validarContrasena(contrasena);
        if (!contrasena.equals(confirmacion)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        String hash = BCrypt.hashpw(contrasena, BCrypt.gensalt());
        usuarioDAO.crear(new Usuario(NOMBRE_ADMINISTRADOR, hash));
    }

    public boolean autenticar(String contrasena) throws SQLException {
        if (contrasena == null || contrasena.isEmpty()) {
            return false;
        }

        Optional<Usuario> administrador = usuarioDAO.obtenerAdministrador();
        if (administrador.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(contrasena, administrador.get().getContrasenaHash());
    }

    private static void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos 6 caracteres.");
        }
    }
}
