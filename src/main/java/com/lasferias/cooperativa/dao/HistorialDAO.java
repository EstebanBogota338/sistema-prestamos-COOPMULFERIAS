package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistorialDAO {

    private final Path rutaBaseDatos;

    public HistorialDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public HistorialDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public List<Prestamo> listarPrestamos(long asociadoId) throws SQLException {
        String sql = """
            SELECT id, asociado_id, monto, fecha, plazo, numero_cuotas,
                   valor_cuota, estado
            FROM prestamo
            WHERE asociado_id = ?
            ORDER BY fecha, id""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, asociadoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Prestamo> prestamos = new ArrayList<>();
                while (rs.next()) {
                    prestamos.add(mapearPrestamo(rs));
                }
                return prestamos;
            }
        }
    }

    public List<Cuota> listarCuotas(long asociadoId) throws SQLException {
        String sql = """
            SELECT c.id, c.prestamo_id, c.numero_cuota, c.valor,
                   c.fecha_vencimiento
            FROM cuota c
            JOIN prestamo p ON p.id = c.prestamo_id
            WHERE p.asociado_id = ?
            ORDER BY c.prestamo_id, c.numero_cuota""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, asociadoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Cuota> cuotas = new ArrayList<>();
                while (rs.next()) {
                    cuotas.add(mapearCuota(rs));
                }
                return cuotas;
            }
        }
    }

    public List<Pago> listarPagos(long asociadoId) throws SQLException {
        String sql = """
            SELECT g.id, g.cuota_id, g.fecha_pago, g.valor, g.numero_recibo
            FROM pago g
            JOIN cuota c ON c.id = g.cuota_id
            JOIN prestamo p ON p.id = c.prestamo_id
            WHERE p.asociado_id = ?
            ORDER BY g.fecha_pago, g.id""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, asociadoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pago> pagos = new ArrayList<>();
                while (rs.next()) {
                    pagos.add(mapearPago(rs));
                }
                return pagos;
            }
        }
    }

    private static Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
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

    private static Cuota mapearCuota(ResultSet rs) throws SQLException {
        return new Cuota(
            rs.getLong("id"),
            rs.getLong("prestamo_id"),
            rs.getInt("numero_cuota"),
            rs.getLong("valor"),
            LocalDate.parse(rs.getString("fecha_vencimiento")));
    }

    private static Pago mapearPago(ResultSet rs) throws SQLException {
        return new Pago(
            rs.getLong("id"),
            rs.getLong("cuota_id"),
            LocalDate.parse(rs.getString("fecha_pago")),
            rs.getLong("valor"),
            rs.getString("numero_recibo"));
    }
}
