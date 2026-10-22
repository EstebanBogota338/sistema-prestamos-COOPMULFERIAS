package com.lasferias.cooperativa;

import com.lasferias.cooperativa.database.ConexionBD;
import com.lasferias.cooperativa.database.InicializadorBD;
import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.ui.VentanaAsociados;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        try {
            InicializadorBD.inicializar();
            System.out.println("Base de datos inicializada correctamente en: " + ConexionBD.rutaBaseDatos());
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "No se pudo inicializar la base de datos. La aplicación se cerrará.",
                "Error de inicio", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el Look and Feel del sistema.");
        }

        SwingUtilities.invokeLater(() ->
            new VentanaAsociados(new AsociadoService()).setVisible(true));
    }
}
