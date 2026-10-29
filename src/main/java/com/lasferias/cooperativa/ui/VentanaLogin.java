package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.AutenticacionService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class VentanaLogin extends JFrame {

    private static final Logger LOGGER =
        Logger.getLogger(VentanaLogin.class.getName());

    private final AutenticacionService service;
    private final JPasswordField contrasena = new JPasswordField();
    private final JLabel mensajeError = new JLabel(" ");
    private final JButton ingresar = new JButton("Ingresar");

    public VentanaLogin(AutenticacionService service) {
        super("Cooperativa Las Ferias - Iniciar sesión");
        this.service = service;
        configurarVentana();
        construirInterfaz();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(440, 360);
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 18));
        tarjeta.setBackground(Estilo.SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE),
            BorderFactory.createEmptyBorder(24, 26, 22, 26)));

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Cooperativa Las Ferias");
        titulo.setFont(Estilo.fuente(Font.BOLD, 20));
        titulo.setForeground(Estilo.TEXTO);
        JLabel subtitulo = new JLabel("Ingrese la contraseña del administrador.");
        subtitulo.setFont(Estilo.fuente(Font.PLAIN, 12));
        subtitulo.setForeground(Estilo.TEXTO_SECUNDARIO);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(subtitulo, BorderLayout.SOUTH);
        tarjeta.add(encabezado, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = 0;
        restricciones.weightx = 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.anchor = GridBagConstraints.WEST;

        restricciones.gridy = 0;
        restricciones.insets = new Insets(0, 0, 6, 0);
        JLabel etiqueta = new JLabel("Contraseña");
        etiqueta.setFont(Estilo.fuente(Font.BOLD, 13));
        etiqueta.setForeground(Estilo.TEXTO_SECUNDARIO);
        etiqueta.setLabelFor(contrasena);
        formulario.add(etiqueta, restricciones);

        restricciones.gridy = 1;
        restricciones.insets = new Insets(0, 0, 0, 0);
        Estilo.campoPassword(contrasena);
        formulario.add(contrasena, restricciones);
        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        mensajeError.setFont(Estilo.fuente(Font.PLAIN, 12));
        mensajeError.setForeground(Estilo.ERROR);
        pie.add(mensajeError, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        Estilo.botonPrimario(ingresar);
        ingresar.addActionListener(e -> ingresar());
        acciones.add(ingresar);
        pie.add(acciones, BorderLayout.SOUTH);
        tarjeta.add(pie, BorderLayout.SOUTH);

        raiz.add(tarjeta, BorderLayout.CENTER);
        setContentPane(raiz);
        getRootPane().setDefaultButton(ingresar);
    }

    private void ingresar() {
        mensajeError.setText(" ");
        habilitarAcciones(false);
        char[] clave = contrasena.getPassword();

        try {
            if (clave.length == 0) {
                mostrarError("Ingrese la contraseña.");
                return;
            }
            if (!service.autenticar(new String(clave))) {
                mostrarError("Contraseña incorrecta.");
                contrasena.selectAll();
                return;
            }

            VentanaAsociados asociados = new VentanaAsociados(new AsociadoService());
            asociados.setVisible(true);
            dispose();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo verificar la contraseña.", e);
            mostrarError("No se pudo verificar la contraseña. Intente nuevamente.");
        } finally {
            Arrays.fill(clave, '\0');
            habilitarAcciones(true);
            contrasena.requestFocusInWindow();
        }
    }

    private void habilitarAcciones(boolean habilitado) {
        ingresar.setEnabled(habilitado);
        contrasena.setEnabled(habilitado);
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setToolTipText(mensaje);
    }
}
