package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.service.AsociadoService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

public class FormularioAsociado extends JDialog {

    private static final Logger LOGGER =
        Logger.getLogger(FormularioAsociado.class.getName());

    private final AsociadoService service;
    private final Asociado asociadoExistente;
    private final JTextField documento = new JTextField();
    private final JTextField nombres = new JTextField();
    private final JTextField apellidos = new JTextField();
    private final JTextField telefono = new JTextField();
    private final JLabel fechaRegistro = new JLabel();
    private final JLabel mensajeError = new JLabel(" ");
    private final JButton guardar = new JButton("Guardar");
    private Asociado resultado;

    public FormularioAsociado(JFrame propietario, AsociadoService service,
                              Asociado asociadoExistente) {
        super(propietario, asociadoExistente == null
            ? "Nuevo asociado" : "Editar asociado", true);
        this.service = service;
        this.asociadoExistente = asociadoExistente;

        configurarVentana();
        construirInterfaz();
        cargarDatos();
        pack();
    }

    public static Asociado mostrar(JFrame propietario, AsociadoService service,
                                   Asociado asociadoExistente) {
        FormularioAsociado formulario =
            new FormularioAsociado(propietario, service, asociadoExistente);
        formulario.setLocationRelativeTo(propietario);
        formulario.setVisible(true);
        return formulario.resultado;
    }

    private void configurarVentana() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(22, 22, 20, 22));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 18));
        tarjeta.setBackground(Estilo.SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE),
            BorderFactory.createEmptyBorder(20, 22, 18, 22)));

        JLabel titulo = new JLabel(asociadoExistente == null
            ? "Registrar asociado" : "Actualizar datos del asociado");
        titulo.setFont(Estilo.fuente(Font.BOLD, 18));
        titulo.setForeground(Estilo.TEXTO);
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridy = 0;
        restricciones.insets = new Insets(0, 0, 12, 0);
        restricciones.anchor = GridBagConstraints.WEST;

        agregarCampo(campos, restricciones, "Documento *", documento, null);
        agregarCampo(campos, restricciones, "Nombres *", nombres, null);
        agregarCampo(campos, restricciones, "Apellidos *", apellidos, null);
        agregarCampo(campos, restricciones, "Teléfono", telefono,
            "Opcional, solo dígitos entre 7 y 10 caracteres");

        fechaRegistro.setFont(Estilo.fuente(Font.PLAIN, 13));
        fechaRegistro.setForeground(Estilo.TEXTO);
        fechaRegistro.setOpaque(true);
        fechaRegistro.setBackground(Estilo.FONDO);
        fechaRegistro.setBorder(Estilo.bordeCampo());
        fechaRegistro.setPreferredSize(new Dimension(300, 34));
        agregarCampo(campos, restricciones, "Fecha de registro", fechaRegistro,
            "Se asigna automáticamente y no es editable");

        tarjeta.add(campos, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        mensajeError.setFont(Estilo.fuente(Font.PLAIN, 12));
        mensajeError.setForeground(Estilo.ERROR);
        pie.add(mensajeError, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton cancelar = new JButton("Cancelar");
        Estilo.botonSecundario(cancelar);
        Estilo.botonPrimario(guardar);
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar());
        acciones.add(cancelar);
        acciones.add(guardar);
        pie.add(acciones, BorderLayout.SOUTH);

        tarjeta.add(pie, BorderLayout.SOUTH);
        raiz.add(tarjeta, BorderLayout.CENTER);
        setContentPane(raiz);

        getRootPane().setDefaultButton(guardar);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        getRootPane().getActionMap().put("cerrar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void agregarCampo(JPanel panel, GridBagConstraints restricciones,
                              String etiqueta, JComponent campo, String ayuda) {
        GridBagConstraints etiquetaRestricciones = (GridBagConstraints) restricciones.clone();
        etiquetaRestricciones.gridx = 0;
        etiquetaRestricciones.weightx = 0;
        etiquetaRestricciones.fill = GridBagConstraints.NONE;
        JLabel textoEtiqueta = new JLabel(etiqueta);
        textoEtiqueta.setFont(Estilo.fuente(Font.PLAIN, 13));
        textoEtiqueta.setForeground(Estilo.TEXTO);
        textoEtiqueta.setPreferredSize(new Dimension(125, 34));
        textoEtiqueta.setLabelFor(campo);
        panel.add(textoEtiqueta, etiquetaRestricciones);

        JPanel contenedor = new JPanel(new BorderLayout(0, 3));
        contenedor.setOpaque(false);
        configurarCampo(campo);
        contenedor.add(campo, BorderLayout.CENTER);
        if (ayuda != null) {
            JLabel ayudaLabel = new JLabel(ayuda);
            ayudaLabel.setFont(Estilo.fuente(Font.PLAIN, 11));
            ayudaLabel.setForeground(Estilo.TEXTO_AYUDA);
            contenedor.add(ayudaLabel, BorderLayout.SOUTH);
        }

        GridBagConstraints campoRestricciones = (GridBagConstraints) restricciones.clone();
        campoRestricciones.gridx = 1;
        campoRestricciones.weightx = 1;
        campoRestricciones.fill = GridBagConstraints.HORIZONTAL;
        panel.add(contenedor, campoRestricciones);
        restricciones.gridy++;
    }

    private void configurarCampo(JComponent campo) {
        campo.setFont(Estilo.fuente(Font.PLAIN, 13));
        campo.setForeground(Estilo.TEXTO);
        campo.setBackground(Estilo.SUPERFICIE);
        campo.setPreferredSize(new Dimension(300, 34));
        if (campo instanceof JTextField texto) {
            texto.setBorder(Estilo.bordeCampo());
            texto.setCaretColor(Estilo.TEXTO);
        }
    }

    private void cargarDatos() {
        if (asociadoExistente == null) {
            fechaRegistro.setText(LocalDate.now().toString());
            fechaRegistro.setHorizontalAlignment(SwingConstants.LEFT);
            return;
        }

        documento.setText(asociadoExistente.getDocumento());
        nombres.setText(asociadoExistente.getNombres());
        apellidos.setText(asociadoExistente.getApellidos());
        telefono.setText(asociadoExistente.getTelefono());
        LocalDate fecha = asociadoExistente.getFechaRegistro() == null
            ? LocalDate.now() : asociadoExistente.getFechaRegistro();
        fechaRegistro.setText(fecha.toString());
    }

    private void guardar() {
        mensajeError.setText(" ");
        habilitarAcciones(false);

        Asociado candidato = new Asociado(
            asociadoExistente == null ? 0 : asociadoExistente.getId(),
            documento.getText(),
            nombres.getText(),
            apellidos.getText(),
            telefono.getText(),
            asociadoExistente == null
                ? LocalDate.now() : asociadoExistente.getFechaRegistro()
        );

        try {
            if (asociadoExistente == null) {
                resultado = service.registrar(candidato);
            } else {
                service.actualizar(candidato);
                resultado = candidato;
            }
            dispose();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo guardar el asociado.", e);
            mostrarError("No se pudo guardar el asociado. Intente nuevamente.");
        } finally {
            habilitarAcciones(true);
        }
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setToolTipText(mensaje);
    }

    private void habilitarAcciones(boolean habilitado) {
        guardar.setEnabled(habilitado);
        documento.setEnabled(habilitado);
        nombres.setEnabled(habilitado);
        apellidos.setEnabled(habilitado);
        telefono.setEnabled(habilitado);
    }
}
