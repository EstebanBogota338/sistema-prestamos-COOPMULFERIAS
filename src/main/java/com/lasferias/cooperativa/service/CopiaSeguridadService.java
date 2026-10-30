package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.CopiaSeguridadDAO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;

public class CopiaSeguridadService {

    private final CopiaSeguridadDAO copiaSeguridadDAO;
    private final Path rutaBaseDatos;

    public CopiaSeguridadService() {
        this(new CopiaSeguridadDAO());
    }

    public CopiaSeguridadService(CopiaSeguridadDAO copiaSeguridadDAO) {
        this.copiaSeguridadDAO = Objects.requireNonNull(copiaSeguridadDAO,
            "El DAO de copia de seguridad es obligatorio");
        this.rutaBaseDatos = copiaSeguridadDAO.getRutaBaseDatos();
    }

    public Path crearCopia(Path destino, boolean sobrescribir)
            throws SQLException {
        if (destino == null) {
            throw new IllegalArgumentException(
                "Seleccione un archivo de destino.");
        }

        Path destinoAbsoluto = destino.toAbsolutePath().normalize();
        Path baseAbsoluta = rutaBaseDatos.toAbsolutePath().normalize();
        if (destinoAbsoluto.equals(baseAbsoluta)) {
            throw new IllegalArgumentException(
                "No se puede sobrescribir la base de datos en uso.");
        }

        if (Files.exists(destinoAbsoluto) && !sobrescribir) {
            throw new IllegalArgumentException(
                "El archivo de destino ya existe.");
        }

        try {
            if (Files.exists(destinoAbsoluto)) {
                Files.delete(destinoAbsoluto);
            }
            copiaSeguridadDAO.crearCopia(rutaBaseDatos, destinoAbsoluto);
        } catch (IOException e) {
            throw new SQLException(
                "No se pudo crear la copia de seguridad.", e);
        }
        return destinoAbsoluto;
    }
}
