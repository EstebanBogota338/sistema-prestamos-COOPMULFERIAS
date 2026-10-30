package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Pago;
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

public class PagoDAO {

    private static final String COLUMNAS =
        "id, cuota_id, fecha_pago, valor, numero_recibo";

    private final Path rutaBaseDatos;

    public PagoDAO() {
        this(ConexionBD.rutaBaseDatos());
    }

    public PagoDAO(Path rutaBaseDatos) {
        this.rutaBaseDatos = Objects.requireNonNull(rutaBaseDatos,
            "La ruta de la base de datos es obligatoria");
    }

    public long crear(Pago pago) throws SQLException {
        String sql = """
            INSERT INTO pago(cuota_id, fecha_pago, valor, numero_recibo)
            VALUES (?, ?, ?, ?)""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql,
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pago.getCuotaId());
            ps.setString(2, pago.getFechaPago().toString());
            ps.setLong(3, pago.getValor());
            ps.setString(4, pago.getNumeroRecibo());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException(
                        "No se pudo obtener el identificador del pago creado");
                }
                long id = claves.getLong(1);
                pago.setId(id);
                return id;
            }
        }
    }

    public Optional<Pago> buscarPorId(long id) throws SQLException {
        String sql = """
            SELECT %s
            FROM pago
            WHERE id = ?""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Pago> listar() throws SQLException {
        String sql = """
            SELECT %s
            FROM pago
            ORDER BY id""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapearLista(rs);
        }
    }

    public List<Pago> listarPorCuota(long cuotaId) throws SQLException {
        String sql = """
            SELECT %s
            FROM pago
            WHERE cuota_id = ?
            ORDER BY fecha_pago, id""".formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, cuotaId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        }
    }

    public boolean actualizar(Pago pago) throws SQLException {
        String sql = """
            UPDATE pago
            SET fecha_pago = ?, valor = ?, numero_recibo = ?
            WHERE id = ?""";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos)) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement comprobar = cn.prepareStatement(
                        "SELECT 1 FROM pago WHERE id = ?")) {
                    comprobar.setLong(1, pago.getId());
                    try (ResultSet rs = comprobar.executeQuery()) {
                        if (!rs.next()) {
                            cn.rollback();
                            return false;
                        }
                    }
                }

                int filas;
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, pago.getFechaPago().toString());
                    ps.setLong(2, pago.getValor());
                    ps.setString(3, pago.getNumeroRecibo());
                    ps.setLong(4, pago.getId());
                    filas = ps.executeUpdate();
                }

                if (filas != 1) {
                    cn.rollback();
                    return false;
                }
                cn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                throw e;
            }
        }
    }

    private static List<Pago> mapearLista(ResultSet rs) throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        while (rs.next()) {
            pagos.add(mapear(rs));
        }
        return pagos;
    }

    private static Pago mapear(ResultSet rs) throws SQLException {
        return new Pago(
            rs.getLong("id"),
            rs.getLong("cuota_id"),
            LocalDate.parse(rs.getString("fecha_pago")),
            rs.getLong("valor"),
            rs.getString("numero_recibo"));
    }
}
