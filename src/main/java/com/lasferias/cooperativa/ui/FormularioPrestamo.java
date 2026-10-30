package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.PrestamoService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class FormularioPrestamo extends JDialog {

    private static final Logger LOGGER =
        Logger.getLogger(FormularioPrestamo.class.getName());

    private final PrestamoService service;
    private final List<Asociado> asociados;
    private final Prestamo existente;

    private final JComboBox<Asociado> comboAsociados = new JComboBox<>();
    private final JLabel asociadoFijo = new JLabel();
    private final JTextField monto = new JTextField();
    private final JTextField fecha = new JTextField();
    private final JTextField plazo = new JTextField();
    private final JTextField numeroCuotas = new JTextField();
    private final JTextField valorCuota = new JTextField();
    private final JComboBox<String> estado = new JComboBox<>(new String[] {
        Prestamo.ESTADO_VIGENTE, Prestamo.ESTADO_FINALIZADO
    });
    private final JLabel mensajeError = new JLabel(" ");
    private final JButton guardar = new JButton("Guardar");

    private Prestamo resultado;

    public FormularioPrestamo(JFrame propietario, PrestamoService service,
                              List<Asociado> asociados, Prestamo existente) {
        super(propietario, existente == null
            ? "Nuevo préstamo" : "Editar préstamo", true);
        this.service = service;
        this.asociados = new ArrayList<>(asociados);
        this.existente = existente;

        configurarVentana();
        construirInterfaz();
        cargarDatos();
    }

    public static Prestamo mostrar(JFrame propietario, PrestamoService service,
                                   List<Asociado> asociados, Prestamo existente) {
        FormularioPrestamo formulario = new FormularioPrestamo(
            propietario, service, asociados, existente);
        formulario.setLocationRelativeTo(propietario);
        formulario.setVisible(true);
        return formulario.resultado;
    }

    private void configurarVentana() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(620, 600);
        setMinimumSize(new Dimension(620, 600));
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(22, 22, 20, 22));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 16));
        tarjeta.setBackground(Estilo.SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE),
            BorderFactory.createEmptyBorder(20, 22, 18, 22)));

        JLabel titulo = new JLabel(existente == null
            ? "Registrar préstamo" : "Actualizar datos del préstamo");
        titulo.setFont(Estilo.fuente(Font.BOLD, 18));
        titulo.setForeground(Estilo.TEXTO);
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridy = 0;
        restricciones.insets = new Insets(0, 0, 10, 0);
        restricciones.anchor = GridBagConstraints.WEST;

        JComponent campoAsociado;
        if (existente == null) {
            configurarCombo();
            campoAsociado = comboAsociados;
        } else {
            configurarAsociadoFijo();
            campoAsociado = asociadoFijo;
        }
        agregarCampo(campos, restricciones, "Asociado *", campoAsociado,
            "El asociado no podrá cambiarse después.");

        Estilo.campo(monto);
        agregarCampo(campos, restricciones, "Monto *", monto,
            "Solo números; puede usar puntos como separador de miles.");

        Estilo.campo(fecha);
        agregarCampo(campos, restricciones, "Fecha *", fecha,
            "Formato: AAAA-MM-DD");

        Estilo.campo(plazo);
        agregarCampo(campos, restricciones, "Plazo *", plazo, null);

        Estilo.campo(numeroCuotas);
        agregarCampo(campos, restricciones, "Número de cuotas *", numeroCuotas, null);

        Estilo.campo(valorCuota);
        agregarCampo(campos, restricciones, "Valor de cuota *", valorCuota,
            "Valor manual, sin cálculos automáticos.");

        estado.setFont(Estilo.fuente(Font.PLAIN, 13));
        estado.setForeground(Estilo.TEXTO);
        estado.setBackground(Estilo.SUPERFICIE);
        estado.setPreferredSize(new Dimension(300, 34));
        agregarCampo(campos, restricciones, "Estado *", estado, null);

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
        GridBagConstraints etiquetaRestricciones =
            (GridBagConstraints) restricciones.clone();
        etiquetaRestricciones.gridx = 0;
        etiquetaRestricciones.weightx = 0;
        etiquetaRestricciones.fill = GridBagConstraints.NONE;
        JLabel textoEtiqueta = new JLabel(etiqueta);
        textoEtiqueta.setFont(Estilo.fuente(Font.BOLD, 13));
        textoEtiqueta.setForeground(Estilo.TEXTO_SECUNDARIO);
        textoEtiqueta.setPreferredSize(new Dimension(135, 34));
        textoEtiqueta.setLabelFor(campo);
        panel.add(textoEtiqueta, etiquetaRestricciones);

        JPanel contenedor = new JPanel(new BorderLayout(0, 3));
        contenedor.setOpaque(false);
        contenedor.add(campo, BorderLayout.CENTER);
        if (ayuda != null) {
            JLabel ayudaLabel = new JLabel(ayuda);
            ayudaLabel.setFont(Estilo.fuente(Font.PLAIN, 11));
            ayudaLabel.setForeground(Estilo.TEXTO_AYUDA);
            contenedor.add(ayudaLabel, BorderLayout.SOUTH);
        }

        GridBagConstraints campoRestricciones =
            (GridBagConstraints) restricciones.clone();
        campoRestricciones.gridx = 1;
        campoRestricciones.weightx = 1;
        campoRestricciones.fill = GridBagConstraints.HORIZONTAL;
        panel.add(contenedor, campoRestricciones);
        restricciones.gridy++;
    }

    private void configurarCombo() {
        comboAsociados.setFont(Estilo.fuente(Font.PLAIN, 13));
        comboAsociados.setForeground(Estilo.TEXTO);
        comboAsociados.setBackground(Estilo.SUPERFICIE);
        comboAsociados.setPreferredSize(new Dimension(300, 34));
        comboAsociados.setRenderer(new AsociadoRenderer());
    }

    private void configurarAsociadoFijo() {
        asociadoFijo.setFont(Estilo.fuente(Font.PLAIN, 13));
        asociadoFijo.setForeground(Estilo.TEXTO);
        asociadoFijo.setOpaque(true);
        asociadoFijo.setBackground(Estilo.FONDO);
        asociadoFijo.setBorder(Estilo.bordeCampo());
        asociadoFijo.setPreferredSize(new Dimension(300, 34));
    }

    private void cargarDatos() {
        if (existente == null) {
            DefaultComboBoxModel<Asociado> modelo = new DefaultComboBoxModel<>();
            modelo.addElement(null);
            for (Asociado asociado : asociados) {
                modelo.addElement(asociado);
            }
            comboAsociados.setModel(modelo);
            comboAsociados.setSelectedIndex(0);
            estado.setSelectedItem(Prestamo.ESTADO_VIGENTE);
            return;
        }

        Asociado asociado = buscarAsociado(existente.getAsociadoId());
        asociadoFijo.setText(formatearAsociado(asociado));
        monto.setText(Estilo.formatearMoneda(existente.getMonto()));
        fecha.setText(existente.getFecha().toString());
        plazo.setText(existente.getPlazo());
        numeroCuotas.setText(String.valueOf(existente.getNumeroCuotas()));
        valorCuota.setText(Estilo.formatearMoneda(existente.getValorCuota()));
        estado.setSelectedItem(existente.getEstado());
    }

    private void guardar() {
        mensajeError.setText(" ");
        habilitarAcciones(false);

        try {
            Prestamo candidato = construirPrestamo();
            if (existente == null) {
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
                "No se pudo guardar el préstamo.", e);
            mostrarError("No se pudo guardar el préstamo. Intente nuevamente.");
        } finally {
            habilitarAcciones(true);
        }
    }

    private Prestamo construirPrestamo() {
        long asociadoId;
        if (existente == null) {
            Asociado seleccionado = (Asociado) comboAsociados.getSelectedItem();
            if (seleccionado == null) {
                throw new IllegalArgumentException("Seleccione un asociado válido.");
            }
            asociadoId = seleccionado.getId();
        } else {
            asociadoId = existente.getAsociadoId();
        }

        long montoValor = parsearMonto(monto.getText(), "monto");
        LocalDate fechaValor = parsearFecha(fecha.getText());
        int cuotasValor = parsearCuotas(numeroCuotas.getText());
        long valorCuotaValor = parsearMonto(valorCuota.getText(), "valor de cuota");
        String estadoValor = (String) estado.getSelectedItem();

        if (existente == null) {
            return new Prestamo(asociadoId, montoValor, fechaValor,
                plazo.getText(), cuotasValor, valorCuotaValor, estadoValor);
        }
        return new Prestamo(existente.getId(), asociadoId, montoValor,
            fechaValor, plazo.getText(), cuotasValor, valorCuotaValor,
            estadoValor);
    }

    private static final Pattern MONTO_ENTERO = Pattern.compile("^\\d+$");
    private static final Pattern MONTO_SEPARADO =
        Pattern.compile("^\\d{1,3}([. ]\\d{3})+$");

    static long parsearMonto(String texto, String nombreCampo) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(
                "El " + nombreCampo + " es obligatorio.");
        }

        boolean separado = MONTO_SEPARADO.matcher(limpio).matches();
        boolean entero = MONTO_ENTERO.matcher(limpio).matches();
        // 0.001 tiene forma de separador de miles, pero no es un monto válido.
        if (!entero && !separado || separado && limpio.charAt(0) == '0') {
            throw new IllegalArgumentException(
                "El " + nombreCampo + " no es válido. Use solo números "
                    + "(ej. 1500000 o 1.500.000).");
        }

        String sinSeparadores = limpio.replace(".", "").replace(" ", "");
        try {
            return Long.parseLong(sinSeparadores);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "El " + nombreCampo + " es demasiado grande.");
        }
    }

    static int parsearCuotas(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(
                "El número de cuotas es obligatorio.");
        }
        if (!limpio.matches("\\d+")) {
            throw new IllegalArgumentException(
                "El número de cuotas debe ser un entero.");
        }
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "El número de cuotas es demasiado grande.");
        }
    }

    static LocalDate parsearFecha(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        try {
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use el formato AAAA-MM-DD.");
        }
    }

    private Asociado buscarAsociado(long asociadoId) {
        for (Asociado asociado : asociados) {
            if (asociado.getId() == asociadoId) {
                return asociado;
            }
        }
        return null;
    }

    private static String formatearAsociado(Asociado asociado) {
        if (asociado == null) {
            return "(asociado no encontrado)";
        }
        return asociado.getDocumento() + " - "
            + asociado.getNombres() + " " + asociado.getApellidos();
    }

    private void habilitarAcciones(boolean habilitado) {
        guardar.setEnabled(habilitado);
        comboAsociados.setEnabled(habilitado);
        monto.setEnabled(habilitado);
        fecha.setEnabled(habilitado);
        plazo.setEnabled(habilitado);
        numeroCuotas.setEnabled(habilitado);
        valorCuota.setEnabled(habilitado);
        estado.setEnabled(habilitado);
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setToolTipText(mensaje);
    }

    private static final class AsociadoRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor,
                int indice, boolean seleccionado, boolean conFoco) {
            super.getListCellRendererComponent(lista, valor, indice,
                seleccionado, conFoco);
            if (valor == null) {
                setText("Seleccione un asociado…");
            } else {
                Asociado asociado = (Asociado) valor;
                setText(asociado.getDocumento() + " - "
                    + asociado.getNombres() + " " + asociado.getApellidos());
            }
            return this;
        }
    }
}
