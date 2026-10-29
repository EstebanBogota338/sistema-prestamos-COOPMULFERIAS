package com.lasferias.cooperativa.ui;

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

public class VentanaCreacionAdministrador extends JFrame {

    private static final Logger LOGGER =
        Logger.getLogger(VentanaCreacionAdministrador.class.getName());

    private final AutenticacionService service;
    private final JPasswordField contrasena = new JPasswordField();
    private final JPasswordField confirmacion = new JPasswordField();
    private final JLabel mensajeError = new JLabel(" ");
    private final JButton crear = new JButton("Crear administrador");

    public VentanaCreacionAdministrador(AutenticacionService service) {
        super("Cooperativa Las Ferias - Crear administrador");
        this.service = service;
        configurarVentana();
        construirInterfaz();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(480, 440);
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
        JLabel titulo = new JLabel("Crear administrador");
        titulo.setFont(Estilo.fuente(Font.BOLD, 20));
        titulo.setForeground(Estilo.TEXTO);
        JLabel subtitulo = new JLabel(
            "Defina la contraseña del administrador.");
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
        JLabel etiquetaContrasena = new JLabel("Contraseña");
        etiquetaContrasena.setFont(Estilo.fuente(Font.BOLD, 13));
        etiquetaContrasena.setForeground(Estilo.TEXTO_SECUNDARIO);
        etiquetaContrasena.setLabelFor(contrasena);
        formulario.add(etiquetaContrasena, restricciones);

        restricciones.gridy = 1;
        restricciones.insets = new Insets(0, 0, 14, 0);
        Estilo.campoPassword(contrasena);
        formulario.add(contrasena, restricciones);

        restricciones.gridy = 2;
        restricciones.insets = new Insets(0, 0, 6, 0);
        JLabel etiquetaConfirmacion = new JLabel("Confirmar contraseña");
        etiquetaConfirmacion.setFont(Estilo.fuente(Font.BOLD, 13));
        etiquetaConfirmacion.setForeground(Estilo.TEXTO_SECUNDARIO);
        etiquetaConfirmacion.setLabelFor(confirmacion);
        formulario.add(etiquetaConfirmacion, restricciones);

        restricciones.gridy = 3;
        restricciones.insets = new Insets(0, 0, 4, 0);
        Estilo.campoPassword(confirmacion);
        formulario.add(confirmacion, restricciones);

        restricciones.gridy = 4;
        restricciones.insets = new Insets(0, 0, 0, 0);
        JLabel ayuda = new JLabel("Mínimo 6 caracteres.");
        ayuda.setFont(Estilo.fuente(Font.PLAIN, 11));
        ayuda.setForeground(Estilo.TEXTO_SECUNDARIO);
        formulario.add(ayuda, restricciones);
        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        mensajeError.setFont(Estilo.fuente(Font.PLAIN, 12));
        mensajeError.setForeground(Estilo.ERROR);
        pie.add(mensajeError, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        Estilo.botonPrimario(crear);
        crear.addActionListener(e -> crearAdministrador());
        acciones.add(crear);
        pie.add(acciones, BorderLayout.SOUTH);
        tarjeta.add(pie, BorderLayout.SOUTH);

        raiz.add(tarjeta, BorderLayout.CENTER);
        setContentPane(raiz);
        getRootPane().setDefaultButton(crear);
    }

    private void crearAdministrador() {
        mensajeError.setText(" ");
        habilitarAcciones(false);
        char[] clave = contrasena.getPassword();
        char[] confirmar = confirmacion.getPassword();

        try {
            service.crearAdministrador(new String(clave), new String(confirmar));
            VentanaLogin login = new VentanaLogin(service);
            login.setVisible(true);
            dispose();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo crear el administrador.", e);
            mostrarError("No se pudo crear el administrador. Intente nuevamente.");
        } finally {
            Arrays.fill(clave, '\0');
            Arrays.fill(confirmar, '\0');
            habilitarAcciones(true);
            contrasena.requestFocusInWindow();
        }
    }

    private void habilitarAcciones(boolean habilitado) {
        crear.setEnabled(habilitado);
        contrasena.setEnabled(habilitado);
        confirmacion.setEnabled(habilitado);
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setToolTipText(mensaje);
    }
}
