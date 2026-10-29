package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Cuota;
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

public class CuotaDAO {

    private static final String COLUMNAS =
        "id, prestamo_id, numero_cuota, valor, fecha_vencimiento";

    private final Path rutaBaseDatos;

    public CuotaDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public CuotaDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public long crear(Cuota cuota) throws SQLException {
        String sql = """
            INSERT INTO cuota(prestamo_id, numero_cuota, valor, fecha_vencimiento)
            VALUES (?, ?, ?, ?)""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, cuota.getPrestamoId());
            ps.setInt(2, cuota.getNumeroCuota());
            ps.setLong(3, cuota.getValor());
            ps.setString(4, cuota.getFechaVencimiento().toString());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException(
                        "No se pudo obtener el identificador de la cuota creada");
                }
                long id = claves.getLong(1);
                cuota.setId(id);
                return id;
            }
        }
    }

    public Optional<Cuota> buscarPorId(long id) throws SQLException {
        String sql = """
            SELECT %s
            FROM cuota
            WHERE id = ?""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Cuota> listar() throws SQLException {
        String sql = """
            SELECT %s
            FROM cuota
            ORDER BY prestamo_id, numero_cuota""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapearLista(rs);
        }
    }

    public List<Cuota> listarPorPrestamo(long prestamoId) throws SQLException {
        String sql = """
            SELECT %s
            FROM cuota
            WHERE prestamo_id = ?
            ORDER BY numero_cuota""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, prestamoId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        }
    }

    public boolean actualizar(Cuota cuota) throws SQLException {
        String sql = """
            UPDATE cuota
            SET numero_cuota = ?, valor = ?, fecha_vencimiento = ?
            WHERE id = ?
              AND NOT EXISTS (
                  SELECT 1 FROM pago WHERE cuota_id = ?
              )""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cuota.getNumeroCuota());
            ps.setLong(2, cuota.getValor());
            ps.setString(3, cuota.getFechaVencimiento().toString());
            ps.setLong(4, cuota.getId());
            ps.setLong(5, cuota.getId());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean existeNumero(long prestamoId, int numero, long idExcluir)
            throws SQLException {
        String sql = """
            SELECT 1
            FROM cuota
            WHERE prestamo_id = ?
              AND numero_cuota = ?
              AND id <> ?
            LIMIT 1""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, prestamoId);
            ps.setInt(2, numero);
            ps.setLong(3, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean tienePagos(long cuotaId) throws SQLException {
        String sql = "SELECT 1 FROM pago WHERE cuota_id = ? LIMIT 1";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, cuotaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static List<Cuota> mapearLista(ResultSet rs) throws SQLException {
        List<Cuota> cuotas = new ArrayList<>();
        while (rs.next()) {
            cuotas.add(mapear(rs));
        }
        return cuotas;
    }

    private static Cuota mapear(ResultSet rs) throws SQLException {
        return new Cuota(
            rs.getLong("id"),
            rs.getLong("prestamo_id"),
            rs.getInt("numero_cuota"),
            rs.getLong("valor"),
            LocalDate.parse(rs.getString("fecha_vencimiento")));
    }
}
