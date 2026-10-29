package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Prestamo;
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

public class PrestamoDAO {

    private static final String COLUMNAS =
        "id, asociado_id, monto, fecha, plazo, numero_cuotas, valor_cuota, estado";

    private final Path rutaBaseDatos;

    public PrestamoDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public PrestamoDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public long crear(Prestamo prestamo) throws SQLException {
        String sql = """
            INSERT INTO prestamo(asociado_id, monto, fecha, plazo,
                                 numero_cuotas, valor_cuota, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, prestamo);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el identificador del préstamo creado");
                }
                long id = claves.getLong(1);
                prestamo.setId(id);
                return id;
            }
        }
    }

    public boolean actualizar(Prestamo prestamo) throws SQLException {
        String sql = """
            UPDATE prestamo
            SET monto = ?, fecha = ?, plazo = ?, numero_cuotas = ?,
                valor_cuota = ?, estado = ?
            WHERE id = ?""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, prestamo.getMonto());
            ps.setString(2, prestamo.getFecha().toString());
            ps.setString(3, prestamo.getPlazo());
            ps.setInt(4, prestamo.getNumeroCuotas());
            ps.setLong(5, prestamo.getValorCuota());
            ps.setString(6, prestamo.getEstado());
            ps.setLong(7, prestamo.getId());
            return ps.executeUpdate() == 1;
        }
    }

    public Optional<Prestamo> buscarPorId(long id) throws SQLException {
        String sql = """
            SELECT %s
            FROM prestamo
            WHERE id = ?""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Prestamo> listar() throws SQLException {
        String sql = """
            SELECT %s
            FROM prestamo
            ORDER BY id""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Prestamo> prestamos = new ArrayList<>();
            while (rs.next()) {
                prestamos.add(mapear(rs));
            }
            return prestamos;
        }
    }

    private static void asignarDatos(PreparedStatement ps, Prestamo prestamo)
            throws SQLException {
        ps.setLong(1, prestamo.getAsociadoId());
        ps.setLong(2, prestamo.getMonto());
        ps.setString(3, prestamo.getFecha().toString());
        ps.setString(4, prestamo.getPlazo());
        ps.setInt(5, prestamo.getNumeroCuotas());
        ps.setLong(6, prestamo.getValorCuota());
        ps.setString(7, prestamo.getEstado());
    }

    private static Prestamo mapear(ResultSet rs) throws SQLException {
        return new Prestamo(
            rs.getLong("id"),
            rs.getLong("asociado_id"),
            rs.getLong("monto"),
            LocalDate.parse(rs.getString("fecha")),
            rs.getString("plazo"),
            rs.getInt("numero_cuotas"),
            rs.getLong("valor_cuota"),
            rs.getString("estado"));
    }
}
