package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Usuario;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

public class UsuarioDAO {

    private final Path rutaBaseDatos;

    public UsuarioDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public UsuarioDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public long crear(Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuario(nombre_usuario, contrasena_hash)
            VALUES (?, ?)""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getContrasenaHash());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el identificador del usuario creado");
                }
                long id = claves.getLong(1);
                usuario.setId(id);
                return id;
            }
        }
    }

    public Optional<Usuario> obtenerAdministrador() throws SQLException {
        String sql = """
            SELECT id, nombre_usuario, contrasena_hash
            FROM usuario
            ORDER BY id
            LIMIT 1""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(new Usuario(
                rs.getLong("id"),
                rs.getString("nombre_usuario"),
                rs.getString("contrasena_hash")));
        }
    }

    public boolean existeAlgunUsuario() throws SQLException {
        String sql = "SELECT 1 FROM usuario LIMIT 1";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}
