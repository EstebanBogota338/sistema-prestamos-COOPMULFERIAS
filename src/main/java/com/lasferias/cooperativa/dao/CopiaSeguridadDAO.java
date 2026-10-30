package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class CopiaSeguridadDAO {

    private final Path rutaBaseDatos;

    public CopiaSeguridadDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public CopiaSeguridadDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public Path getRutaBaseDatos() {
        return rutaBaseDatos;
    }

    public void crearCopia(Path rutaBaseDatos, Path destino) throws SQLException {
        String sql = "VACUUM INTO ?";
        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, destino.toAbsolutePath().toString());
            ps.execute();
        }
    }
}
