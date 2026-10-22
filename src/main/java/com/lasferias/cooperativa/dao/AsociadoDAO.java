package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AsociadoDAO {

    private static final String COLUMNAS =
        "id, documento, nombres, apellidos, telefono, fecha_registro";

    private final Path rutaBaseDatos;

    public AsociadoDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public AsociadoDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public long crear(Asociado asociado) throws SQLException {
        String sql = """
            INSERT INTO asociado(documento, nombres, apellidos, telefono, fecha_registro)
            VALUES (?, ?, ?, ?, ?)""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, asociado);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el identificador del asociado creado");
                }
                long id = claves.getLong(1);
                asociado.setId(id);
                return id;
            }
        }
    }

    public void actualizar(Asociado asociado) throws SQLException {
        String sql = """
            UPDATE asociado
            SET documento = ?, nombres = ?, apellidos = ?, telefono = ?
            WHERE id = ?""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, asociado.getDocumento());
            ps.setString(2, asociado.getNombres());
            ps.setString(3, asociado.getApellidos());
            ps.setString(4, asociado.getTelefono());
            ps.setLong(5, asociado.getId());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se encontró el asociado que se desea actualizar");
            }
        }
    }

    public Optional<Asociado> buscarPorDocumento(String documento) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM asociado WHERE documento = ?";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Asociado> buscarPorTexto(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return listar();
        }

        String sql = """
            SELECT %s
            FROM asociado
            WHERE documento LIKE ?
               OR nombres LIKE ?
               OR apellidos LIKE ?
            ORDER BY apellidos COLLATE NOCASE, nombres COLLATE NOCASE, documento
            """.formatted(COLUMNAS);
        String patron = "%" + texto.trim() + "%";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            return consultarLista(ps);
        }
    }

    public List<Asociado> listar() throws SQLException {
        String sql = """
            SELECT %s
            FROM asociado
            ORDER BY apellidos COLLATE NOCASE, nombres COLLATE NOCASE, documento
            """.formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            return consultarLista(ps);
        }
    }

    public boolean existeDocumento(String documento, long idExcluir) throws SQLException {
        String sql = "SELECT 1 FROM asociado WHERE documento = ? AND id <> ? LIMIT 1";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.setLong(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static List<Asociado> consultarLista(PreparedStatement ps) throws SQLException {
        List<Asociado> asociados = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                asociados.add(mapear(rs));
            }
        }
        return asociados;
    }

    private static void asignarDatos(PreparedStatement ps, Asociado asociado)
            throws SQLException {
        ps.setString(1, asociado.getDocumento());
        ps.setString(2, asociado.getNombres());
        ps.setString(3, asociado.getApellidos());
        ps.setString(4, asociado.getTelefono());
        ps.setString(5, asociado.getFechaRegistro().toString());
    }

    private static Asociado mapear(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_registro");
        return new Asociado(
            rs.getLong("id"),
            rs.getString("documento"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("telefono"),
            fecha == null ? null : LocalDate.parse(fecha)
        );
    }
}
