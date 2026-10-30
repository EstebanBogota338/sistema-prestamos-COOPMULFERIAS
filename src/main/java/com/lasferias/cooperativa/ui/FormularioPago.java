package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.PagoService;
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

public class FormularioPago extends JDialog {

    private static final Logger LOGGER =
        Logger.getLogger(FormularioPago.class.getName());

    private static final Pattern MONTO_ENTERO = Pattern.compile("^\\d+$");
    private static final Pattern MONTO_SEPARADO =
        Pattern.compile("^\\d{1,3}([. ]\\d{3})+$");

    private final PagoService service;
    private final List<Cuota> cuotas;
    private final List<Prestamo> prestamos;
    private final List<Asociado> asociados;
    private final Pago existente;

    private final JComboBox<Cuota> comboCuotas = new JComboBox<>();
    private final JLabel cuotaFija = new JLabel();
    private final JTextField fechaPago = new JTextField();
    private final JTextField valor = new JTextField();
    private final JTextField numeroRecibo = new JTextField();
    private final JLabel mensajeError = new JLabel(" ");
    private final JButton guardar = new JButton("Guardar");

    private Pago resultado;

    public FormularioPago(JFrame propietario, PagoService service,
                          List<Cuota> cuotas, List<Prestamo> prestamos,
                          List<Asociado> asociados, Pago existente) {
        super(propietario, existente == null ? "Nuevo pago" : "Editar pago", true);
        this.service = service;
        this.cuotas = new ArrayList<>(cuotas);
        this.prestamos = new ArrayList<>(prestamos);
        this.asociados = new ArrayList<>(asociados);
        this.existente = existente;

        configurarVentana();
        construirInterfaz();
        cargarDatos();
        pack();
    }

    public static Pago mostrar(JFrame padre, PagoService service,
                               List<Cuota> cuotas, List<Prestamo> prestamos,
                               List<Asociado> asociados, Pago existente) {
        FormularioPago formulario = new FormularioPago(
            padre, service, cuotas, prestamos, asociados, existente);
        formulario.setLocationRelativeTo(padre);
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

        JPanel tarjeta = new JPanel(new BorderLayout(0, 16));
        tarjeta.setBackground(Estilo.SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE),
            BorderFactory.createEmptyBorder(20, 22, 18, 22)));

        JLabel titulo = new JLabel(existente == null
            ? "Registrar pago" : "Actualizar datos del pago");
        titulo.setFont(Estilo.fuente(Font.BOLD, 18));
        titulo.setForeground(Estilo.TEXTO);
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridy = 0;
        restricciones.insets = new Insets(0, 0, 10, 0);
        restricciones.anchor = GridBagConstraints.WEST;

        JComponent campoCuota;
        if (existente == null) {
            configurarCombo();
            campoCuota = comboCuotas;
        } else {
            configurarCuotaFija();
            campoCuota = cuotaFija;
        }
        agregarCampo(campos, restricciones, "Cuota *", campoCuota,
            "La cuota no podrá cambiarse después.");

        Estilo.campo(fechaPago);
        agregarCampo(campos, restricciones, "Fecha *", fechaPago,
            "Formato: AAAA-MM-DD");

        Estilo.campo(valor);
        agregarCampo(campos, restricciones, "Valor *", valor,
            "Solo números; puede usar puntos como separador de miles.");

        Estilo.campo(numeroRecibo);
        agregarCampo(campos, restricciones, "N.º de recibo *", numeroRecibo, null);

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
        textoEtiqueta.setPreferredSize(new Dimension(160, 34));
        textoEtiqueta.setLabelFor(campo);
        panel.add(textoEtiqueta, etiquetaRestricciones);

        JPanel contenedor = new JPanel(new BorderLayout(0, 3));
        contenedor.setOpaque(false);
        contenedor.add(campo, BorderLayout.CENTER);
        if (ayuda != null) {
            JLabel ayudaLabel = new JLabel(ayuda);
            ayudaLabel.setFont(Estilo.fuente(Font.PLAIN, 11));
            ayudaLabel.setForeground(Estilo.TEXTO_SECUNDARIO);
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
        comboCuotas.setFont(Estilo.fuente(Font.PLAIN, 13));
        comboCuotas.setForeground(Estilo.TEXTO);
        comboCuotas.setBackground(Estilo.SUPERFICIE);
        comboCuotas.setPreferredSize(new Dimension(340, 34));
        comboCuotas.setRenderer(new CuotaRenderer());
    }

    private void configurarCuotaFija() {
        cuotaFija.setFont(Estilo.fuente(Font.PLAIN, 13));
        cuotaFija.setForeground(Estilo.TEXTO);
        cuotaFija.setOpaque(true);
        cuotaFija.setBackground(Estilo.FONDO);
        cuotaFija.setBorder(Estilo.bordeCampo());
        cuotaFija.setPreferredSize(new Dimension(340, 34));
    }

    private void cargarDatos() {
        if (existente == null) {
            DefaultComboBoxModel<Cuota> modelo = new DefaultComboBoxModel<>();
            modelo.addElement(null);
            for (Cuota cuota : cuotas) {
                modelo.addElement(cuota);
            }
            comboCuotas.setModel(modelo);
            comboCuotas.setSelectedIndex(0);
            habilitarAcciones(true);
            return;
        }

        cuotaFija.setText(formatearCuota(buscarCuota(existente.getCuotaId())));
        fechaPago.setText(existente.getFechaPago().toString());
        valor.setText(String.valueOf(existente.getValor()));
        numeroRecibo.setText(existente.getNumeroRecibo());
        habilitarAcciones(true);
    }

    private void guardar() {
        mensajeError.setText(" ");
        habilitarAcciones(false);
        try {
            Pago candidato = construirPago();
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
            LOGGER.log(Level.SEVERE, "No se pudo guardar el pago.", e);
            mostrarError("No se pudo guardar el pago. Intente nuevamente.");
        } finally {
            habilitarAcciones(true);
        }
    }

    private Pago construirPago() {
        long cuotaId;
        if (existente == null) {
            Cuota seleccionada = (Cuota) comboCuotas.getSelectedItem();
            if (seleccionada == null) {
                throw new IllegalArgumentException("Seleccione una cuota válida.");
            }
            cuotaId = seleccionada.getId();
        } else {
            cuotaId = existente.getCuotaId();
        }

        LocalDate fecha = parsearFecha(fechaPago.getText());
        long valorPago = parsearMonto(valor.getText(), "valor");
        String recibo = numeroRecibo.getText();

        if (existente == null) {
            return new Pago(cuotaId, fecha, valorPago, recibo);
        }
        return new Pago(existente.getId(), cuotaId, fecha, valorPago, recibo);
    }

    private void habilitarAcciones(boolean habilitado) {
        guardar.setEnabled(habilitado);
        fechaPago.setEnabled(habilitado);
        valor.setEnabled(habilitado);
        numeroRecibo.setEnabled(habilitado);
        comboCuotas.setEnabled(habilitado);
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setToolTipText(mensaje);
    }

    private Cuota buscarCuota(long cuotaId) {
        for (Cuota cuota : cuotas) {
            if (cuota.getId() == cuotaId) {
                return cuota;
            }
        }
        return null;
    }

    private Prestamo buscarPrestamo(long prestamoId) {
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getId() == prestamoId) {
                return prestamo;
            }
        }
        return null;
    }

    private Asociado buscarAsociado(long asociadoId) {
        for (Asociado asociado : asociados) {
            if (asociado.getId() == asociadoId) {
                return asociado;
            }
        }
        return null;
    }

    private String formatearCuota(Cuota cuota) {
        if (cuota == null) {
            return "(cuota no encontrada)";
        }
        Prestamo prestamo = buscarPrestamo(cuota.getPrestamoId());
        if (prestamo == null) {
            return "(préstamo no encontrado)";
        }
        Asociado asociado = buscarAsociado(prestamo.getAsociadoId());
        if (asociado == null) {
            return "(asociado no encontrado)";
        }
        return asociado.getDocumento() + " - "
            + asociado.getNombres() + " " + asociado.getApellidos()
            + " - " + prestamo.getFecha() + " - " + prestamo.getMonto()
            + " - Cuota " + cuota.getNumeroCuota();
    }

    // Copiado de FormularioPrestamo/FormularioCuota para conservar el parser.
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

    // Copiado de FormularioPrestamo/FormularioCuota para conservar el parser.
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

    private final class CuotaRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista,
                Object valor, int indice, boolean seleccionado, boolean conFoco) {
            super.getListCellRendererComponent(lista, valor, indice,
                seleccionado, conFoco);
            if (valor == null) {
                setText("Seleccione una cuota…");
            } else {
                setText(formatearCuota((Cuota) valor));
            }
            return this;
        }
    }
}
