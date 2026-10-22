package com.lasferias.cooperativa.dao;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.model.Asociado;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class AsociadoDAO {

    private static final String COLUMNAS =
        "id, documento, nombres, apellidos, telefono, fecha_registro";
    private static final Locale LOCALE_ESPANOL = Locale.forLanguageTag("es-CO");
    private static final Pattern MARCAS_DIACRITICAS =
        Pattern.compile("\\p{M}+");

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
        String sql = "SELECT " + COLUMNAS
            + " FROM asociado WHERE documento COLLATE NOCASE = ?";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Asociado> buscarPorId(long id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM asociado WHERE id = ?";

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Asociado> buscarPorTexto(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return listar();
        }

        // Se filtra en Java para ignorar diacríticos sin cambiar el esquema SQLite.
        // contains conserva el significado literal de %, _ y \.
        String termino = normalizarTexto(texto.trim());

        return listar().stream()
            .filter(asociado -> contiene(asociado.getDocumento(), termino)
                || contiene(asociado.getNombres(), termino)
                || contiene(asociado.getApellidos(), termino))
            .toList();
    }

    public List<Asociado> listar() throws SQLException {
        String sql = """
            SELECT %s
            FROM asociado
            """.formatted(COLUMNAS);

        try (Connection cn = ConexionBD.obtenerConexion(rutaBaseDatos);
             PreparedStatement ps = cn.prepareStatement(sql)) {
            return ordenar(consultarLista(ps));
        }
    }

    public boolean existeDocumento(String documento, long idExcluir) throws SQLException {
        String sql = """
            SELECT 1
            FROM asociado
            WHERE documento COLLATE NOCASE = ? AND id <> ?
            LIMIT 1""";

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
        if (asociado.getFechaRegistro() == null) {
            throw new SQLException(
                "La fecha de registro del asociado es obligatoria.", "22004");
        }
        ps.setString(1, asociado.getDocumento());
        ps.setString(2, asociado.getNombres());
        ps.setString(3, asociado.getApellidos());
        ps.setString(4, asociado.getTelefono());
        ps.setString(5, asociado.getFechaRegistro().toString());
    }

    private static Asociado mapear(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha_registro");
        if (fecha == null) {
            throw new SQLException(
                "La fecha de registro almacenada no puede ser nula.", "22004");
        }

        LocalDate fechaRegistro;
        try {
            fechaRegistro = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new SQLException(
                "La fecha de registro almacenada no tiene formato ISO válido.",
                "22007", e);
        }

        return new Asociado(
            rs.getLong("id"),
            rs.getString("documento"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("telefono"),
            fechaRegistro
        );
    }

    private static boolean contiene(String valor, String terminoNormalizado) {
        return valor != null
            && normalizarTexto(valor).contains(terminoNormalizado);
    }

    private static String normalizarTexto(String valor) {
        String sinDiacriticos = Normalizer.normalize(valor, Normalizer.Form.NFD);
        return MARCAS_DIACRITICAS.matcher(sinDiacriticos)
            .replaceAll("")
            .toLowerCase(Locale.ROOT);
    }

    private static List<Asociado> ordenar(List<Asociado> asociados) {
        Collator collator = Collator.getInstance(LOCALE_ESPANOL);
        Comparator<String> comparadorTexto =
            (primero, segundo) -> collator.compare(primero, segundo);
        Comparator<Asociado> comparador = Comparator
            .comparing(Asociado::getApellidos, comparadorTexto)
            .thenComparing(Asociado::getNombres, comparadorTexto)
            .thenComparing(Asociado::getDocumento, Comparator.naturalOrder());
        asociados.sort(comparador);
        return asociados;
    }
}
