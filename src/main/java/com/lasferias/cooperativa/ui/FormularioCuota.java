package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.CuotaService;
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

public class FormularioCuota extends JDialog {

    private static final Logger LOGGER =
        Logger.getLogger(FormularioCuota.class.getName());

    private static final String AVISO_PAGOS =
        "Esta cuota tiene pagos registrados: no se puede modificar su número, "
            + "valor ni fecha de vencimiento.";

    private static final Pattern MONTO_ENTERO = Pattern.compile("^\\d+$");
    private static final Pattern MONTO_SEPARADO =
        Pattern.compile("^\\d{1,3}([. ]\\d{3})+$");

    private final CuotaService service;
    private final List<Prestamo> prestamos;
    private final List<Asociado> asociados;
    private final Cuota existente;
    private final boolean conPagos;

    private final JComboBox<Prestamo> comboPrestamos = new JComboBox<>();
    private final JLabel prestamoFijo = new JLabel();
    private final JTextField numeroCuota = new JTextField();
    private final JTextField valor = new JTextField();
    private final JTextField fechaVencimiento = new JTextField();
    private final JLabel avisoBloqueo = new JLabel(" ");
    private final JLabel mensajeError = new JLabel(" ");
    private final JButton guardar = new JButton("Guardar");

    private Cuota resultado;

    public FormularioCuota(JFrame propietario, CuotaService service,
                           List<Prestamo> prestamos, List<Asociado> asociados,
                           Cuota existente, boolean conPagos) {
        super(propietario, existente == null ? "Nueva cuota" : "Editar cuota", true);
        this.service = service;
        this.prestamos = new ArrayList<>(prestamos);
        this.asociados = new ArrayList<>(asociados);
        this.existente = existente;
        this.conPagos = conPagos;

        configurarVentana();
        construirInterfaz();
        cargarDatos();
        pack();
    }

    public static Cuota mostrar(JFrame padre, CuotaService service,
                                List<Prestamo> prestamos,
                                List<Asociado> asociados, Cuota existente)
            throws SQLException {
        boolean conPagos = existente != null
            && service.tienePagos(existente.getId());
        FormularioCuota formulario = new FormularioCuota(
            padre, service, prestamos, asociados, existente, conPagos);
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
            ? "Registrar cuota" : "Actualizar datos de la cuota");
        titulo.setFont(Estilo.fuente(Font.BOLD, 18));
        titulo.setForeground(Estilo.TEXTO);
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridy = 0;
        restricciones.insets = new Insets(0, 0, 10, 0);
        restricciones.anchor = GridBagConstraints.WEST;

        JComponent campoPrestamo;
        if (existente == null) {
            configurarCombo();
            campoPrestamo = comboPrestamos;
        } else {
            configurarPrestamoFijo();
            campoPrestamo = prestamoFijo;
        }
        agregarCampo(campos, restricciones, "Préstamo *", campoPrestamo,
            "El préstamo no podrá cambiarse después.");

        Estilo.campo(numeroCuota);
        agregarCampo(campos, restricciones, "Número de cuota *", numeroCuota, null);

        Estilo.campo(valor);
        agregarCampo(campos, restricciones, "Valor *", valor,
            "Acepta puntos como separador de miles.");

        Estilo.campo(fechaVencimiento);
        agregarCampo(campos, restricciones, "Fecha de vencimiento *",
            fechaVencimiento, "Formato: AAAA-MM-DD");

        tarjeta.add(campos, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        avisoBloqueo.setFont(Estilo.fuente(Font.PLAIN, 12));
        avisoBloqueo.setForeground(Estilo.AVISO);
        avisoBloqueo.setVisible(false);
        mensajeError.setFont(Estilo.fuente(Font.PLAIN, 12));
        mensajeError.setForeground(Estilo.ERROR);

        JPanel mensajes = new JPanel(new BorderLayout(0, 4));
        mensajes.setOpaque(false);
        mensajes.add(avisoBloqueo, BorderLayout.NORTH);
        mensajes.add(mensajeError, BorderLayout.SOUTH);
        pie.add(mensajes, BorderLayout.NORTH);

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
            ayudaLabel.setFont(Estilo.fuente(Font.PLAIN, 12));
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
        comboPrestamos.setFont(Estilo.fuente(Font.PLAIN, 13));
        comboPrestamos.setForeground(Estilo.TEXTO);
        comboPrestamos.setBackground(Estilo.SUPERFICIE);
        comboPrestamos.setPreferredSize(new Dimension(300, 34));
        comboPrestamos.setRenderer(new PrestamoRenderer());
    }

    private void configurarPrestamoFijo() {
        prestamoFijo.setFont(Estilo.fuente(Font.PLAIN, 13));
        prestamoFijo.setForeground(Estilo.TEXTO);
        prestamoFijo.setOpaque(true);
        prestamoFijo.setBackground(Estilo.FONDO);
        prestamoFijo.setBorder(Estilo.bordeCampo());
        prestamoFijo.setPreferredSize(new Dimension(300, 34));
    }

    private void cargarDatos() {
        if (existente == null) {
            DefaultComboBoxModel<Prestamo> modelo = new DefaultComboBoxModel<>();
            modelo.addElement(null);
            for (Prestamo prestamo : prestamos) {
                modelo.addElement(prestamo);
            }
            comboPrestamos.setModel(modelo);
            comboPrestamos.setSelectedIndex(0);
            habilitarAcciones(true);
            return;
        }

        Prestamo prestamo = buscarPrestamo(existente.getPrestamoId());
        prestamoFijo.setText(formatearPrestamo(prestamo));
        numeroCuota.setText(String.valueOf(existente.getNumeroCuota()));
        valor.setText(Estilo.formatearMoneda(existente.getValor()));
        fechaVencimiento.setText(existente.getFechaVencimiento().toString());

        if (conPagos) {
            avisoBloqueo.setText(AVISO_PAGOS);
            avisoBloqueo.setToolTipText(AVISO_PAGOS);
            avisoBloqueo.setVisible(true);
        }
        habilitarAcciones(true);
    }

    private void guardar() {
        mensajeError.setText(" ");
        habilitarAcciones(false);
        try {
            Cuota candidata = construirCuota();
            if (existente == null) {
                resultado = service.registrar(candidata);
            } else {
                service.actualizar(candidata);
                resultado = candidata;
            }
            dispose();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No se pudo guardar la cuota.", e);
            mostrarError("No se pudo guardar la cuota. Intente nuevamente.");
        } finally {
            habilitarAcciones(true);
        }
    }

    private Cuota construirCuota() {
        long prestamoId;
        if (existente == null) {
            Prestamo seleccionado = (Prestamo) comboPrestamos.getSelectedItem();
            if (seleccionado == null) {
                throw new IllegalArgumentException("Seleccione un préstamo válido.");
            }
            prestamoId = seleccionado.getId();
        } else {
            prestamoId = existente.getPrestamoId();
        }

        int numero = parsearNumeroCuota(numeroCuota.getText());
        long valorCuota = parsearMonto(valor.getText(), "valor");
        LocalDate fecha = parsearFecha(fechaVencimiento.getText());

        if (existente == null) {
            return new Cuota(prestamoId, numero, valorCuota, fecha);
        }
        return new Cuota(existente.getId(), prestamoId, numero, valorCuota, fecha);
    }

    private void habilitarAcciones(boolean habilitado) {
        boolean camposActivos = habilitado && !conPagos;
        guardar.setEnabled(camposActivos);
        numeroCuota.setEnabled(camposActivos);
        valor.setEnabled(camposActivos);
        fechaVencimiento.setEnabled(camposActivos);
        comboPrestamos.setEnabled(habilitado);
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setToolTipText(mensaje);
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

    private String formatearPrestamo(Prestamo prestamo) {
        if (prestamo == null) {
            return "(préstamo no encontrado)";
        }
        Asociado asociado = buscarAsociado(prestamo.getAsociadoId());
        if (asociado == null) {
            return "(asociado no encontrado)";
        }
        return asociado.getDocumento() + " - "
            + asociado.getNombres() + " " + asociado.getApellidos()
            + " - " + prestamo.getFecha() + " - "
            + Estilo.formatearMoneda(prestamo.getMonto());
    }

    // Copiado de FormularioPrestamo para conservar exactamente el mismo parser.
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

    static int parsearNumeroCuota(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(
                "El número de cuota es obligatorio.");
        }
        if (!limpio.matches("\\d+")) {
            throw new IllegalArgumentException(
                "El número de cuota debe ser un entero.");
        }
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "El número de cuota es demasiado grande.");
        }
    }

    // Copiado de FormularioPrestamo para conservar exactamente el mismo parser.
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

    private final class PrestamoRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista,
                Object valor, int indice, boolean seleccionado, boolean conFoco) {
            super.getListCellRendererComponent(lista, valor, indice,
                seleccionado, conFoco);
            if (valor == null) {
                setText("Seleccione un préstamo…");
            } else {
                setText(formatearPrestamo((Prestamo) valor));
            }
            return this;
        }
    }
}
