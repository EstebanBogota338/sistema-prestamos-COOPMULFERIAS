package com.lasferias.cooperativa;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        try {
            InicializadorBD.inicializar();
            System.out.println("Base de datos inicializada correctamente en: " + ConexionBD.rutaBaseDatos());
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
