package com.lasferias.cooperativa.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConexionBD {

    private static final String CARPETA_DATOS = "CooperativaLasFerias";
    private static final String NOMBRE_ARCHIVO_BD = "cooperativa.db";

    private ConexionBD() { }

    public static Path rutaBaseDatos() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "AppData", "Roaming", CARPETA_DATOS, NOMBRE_ARCHIVO_BD);
    }

    public static boolean baseDatosExiste() {
        return Files.exists(rutaBaseDatos());
    }

    public static Connection obtenerConexion() throws SQLException {
        return obtenerConexion(rutaBaseDatos());
    }

    public static Connection obtenerConexion(Path ruta) throws SQLException {
        try {
            if (ruta.getParent() != null) {
                Files.createDirectories(ruta.getParent());
            }
            Connection cn = DriverManager.getConnection("jdbc:sqlite:" + ruta.toAbsolutePath());
            try (Statement st = cn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            return cn;
        } catch (IOException e) {
            throw new SQLException("No se pudo preparar la carpeta de datos en " + ruta, e);
        }
    }
}
