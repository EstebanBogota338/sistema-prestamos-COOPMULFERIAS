package com.lasferias.cooperativa;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.service.AutenticacionService;
import com.lasferias.cooperativa.ui.VentanaCreacionAdministrador;
import com.lasferias.cooperativa.ui.VentanaLogin;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {
        AutenticacionService autenticacion;
        boolean debeCrearAdministrador;

        try {
            InicializadorBD.inicializar();
            System.out.println("Base de datos inicializada correctamente en: "
                + ConexionBD.rutaBaseDatos());
            autenticacion = new AutenticacionService();
            debeCrearAdministrador = autenticacion.debeCrearAdministrador();
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: "
                + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "No se pudo inicializar la base de datos. La aplicación se cerrará.",
                "Error de inicio", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el Look and Feel del sistema.");
        }

        AutenticacionService servicioAutenticacion = autenticacion;
        boolean crearAdministrador = debeCrearAdministrador;
        SwingUtilities.invokeLater(() -> {
            if (crearAdministrador) {
                new VentanaCreacionAdministrador(servicioAutenticacion).setVisible(true);
            } else {
                new VentanaLogin(servicioAutenticacion).setVisible(true);
            }
        });
    }
}
