package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.HistorialService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class PanelHistorial extends JPanel {

    private static final Logger LOGGER =
        Logger.getLogger(PanelHistorial.class.getName());

    private final HistorialService historialService;
    private final AsociadoService asociadoService;

    private final JComboBox<Asociado> comboAsociados = new JComboBox<>();
    private final PrestamoTableModel modeloPrestamos = new PrestamoTableModel();
    private final CuotaTableModel modeloCuotas = new CuotaTableModel();
    private final PagoTableModel modeloPagos = new PagoTableModel();
    private final JTable tablaPrestamos = new JTable(modeloPrestamos);
    private final JTable tablaCuotas = new JTable(modeloCuotas);
    private final JTable tablaPagos = new JTable(modeloPagos);
    private final JLabel tituloPrestamos = new JLabel("Préstamos (0)");
    private final JLabel tituloCuotas = new JLabel("Cuotas (0)");
    private final JLabel tituloPagos = new JLabel("Pagos (0)");
    private final JLabel estado = new JLabel();
    private boolean actualizandoCombo;

    public PanelHistorial(HistorialService historialService,
                          AsociadoService asociadoService) {
        this.historialService = historialService;
        this.asociadoService = asociadoService;
        tablaPrestamos.setName("tablaPrestamos");
        tablaCuotas.setName("tablaCuotas");
        tablaPagos.setName("tablaPagos");
        construirInterfaz();
        refrescar();
    }

    public void refrescar() {
        Long idSeleccionado = idAsociadoSeleccionado();
        try {
            List<Asociado> asociados = asociadoService.buscar("");
            actualizandoCombo = true;
            DefaultComboBoxModel<Asociado> modelo = new DefaultComboBoxModel<>();
            modelo.addElement(null);
            for (Asociado asociado : asociados) {
                modelo.addElement(asociado);
            }
            comboAsociados.setModel(modelo);

            int indice = 0;
            if (idSeleccionado != null) {
                for (int i = 0; i < asociados.size(); i++) {
                    if (asociados.get(i).getId() == idSeleccionado) {
                        indice = i + 1;
                        break;
                    }
                }
            }
            comboAsociados.setSelectedIndex(indice);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo consultar la lista de asociados.", e);
            limpiar();
            estado.setText("No se pudo consultar la lista de asociados.");
            JOptionPane.showMessageDialog(this,
                "No se pudo consultar la lista de asociados. Intente nuevamente.",
                "Error de consulta", JOptionPane.ERROR_MESSAGE);
        } finally {
            actualizandoCombo = false;
        }
        cargarHistorial();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(Estilo.SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE),
            BorderFactory.createEmptyBorder(14, 16, 12, 16)));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtro.setOpaque(false);
        JLabel etiqueta = new JLabel("Asociado:");
        etiqueta.setFont(Estilo.fuente(Font.BOLD, 13));
        etiqueta.setForeground(Estilo.TEXTO_SECUNDARIO);
        comboAsociados.setName("comboAsociados");
        comboAsociados.setFont(Estilo.fuente(Font.PLAIN, 13));
        comboAsociados.setForeground(Estilo.TEXTO);
        comboAsociados.setBackground(Estilo.SUPERFICIE);
        comboAsociados.setPreferredSize(new Dimension(380, 34));
        comboAsociados.setRenderer(new AsociadoRenderer());
        comboAsociados.addActionListener(e -> {
            if (!actualizandoCombo) {
                cargarHistorial();
            }
        });
        JButton actualizar = new JButton("Actualizar lista");
        Estilo.botonSecundario(actualizar);
        actualizar.addActionListener(e -> refrescar());

        filtro.add(etiqueta);
        filtro.add(comboAsociados);
        filtro.add(actualizar);
        tarjeta.add(filtro, BorderLayout.NORTH);

        JPanel bloques = new JPanel(new GridBagLayout());
        bloques.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = 0;
        restricciones.weightx = 1;
        restricciones.fill = GridBagConstraints.BOTH;

        agregarBloque(bloques, tituloPrestamos, tablaPrestamos,
            restricciones, 0);
        agregarBloque(bloques, tituloCuotas, tablaCuotas,
            restricciones, 1);
        agregarBloque(bloques, tituloPagos, tablaPagos,
            restricciones, 2);
        tarjeta.add(bloques, BorderLayout.CENTER);

        estado.setName("estadoHistorial");
        estado.setFont(Estilo.fuente(Font.PLAIN, 12));
        estado.setForeground(Estilo.TEXTO_SECUNDARIO);
        estado.setText("Seleccione un asociado para ver su historial.");
        tarjeta.add(estado, BorderLayout.SOUTH);

        add(tarjeta, BorderLayout.CENTER);
    }

    private void agregarBloque(JPanel contenedor, JLabel titulo,
                               JTable tabla, GridBagConstraints base,
                               int fila) {
        titulo.setFont(Estilo.fuente(Font.BOLD, 13));
        titulo.setForeground(Estilo.TEXTO);
        titulo.setName("titulo" + tabla.getName().substring(5));
        configurarTabla(tabla);

        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        desplazamiento.getViewport().setBackground(Estilo.SUPERFICIE);

        JPanel bloque = new JPanel(new BorderLayout(0, 6));
        bloque.setOpaque(false);
        bloque.add(titulo, BorderLayout.NORTH);
        bloque.add(desplazamiento, BorderLayout.CENTER);

        GridBagConstraints restricciones =
            (GridBagConstraints) base.clone();
        restricciones.gridy = fila;
        restricciones.weighty = 1;
        restricciones.insets = new Insets(0, 0, 10, 0);
        contenedor.add(bloque, restricciones);
    }

    private void configurarTabla(JTable tabla) {
        tabla.setFont(Estilo.fuente(Font.PLAIN, 12));
        tabla.setForeground(Estilo.TEXTO);
        tabla.setBackground(Estilo.SUPERFICIE);
        tabla.setSelectionBackground(Estilo.VERDE_SUAVE);
        tabla.setSelectionForeground(Estilo.TEXTO);
        tabla.setGridColor(Estilo.BORDE);
        tabla.setRowHeight(34);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setDefaultEditor(Object.class, null);
        tabla.getTableHeader().setReorderingAllowed(false);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(Estilo.fuente(Font.BOLD, 11));
        encabezado.setForeground(Estilo.TEXTO_SECUNDARIO);
        encabezado.setBackground(Estilo.SUPERFICIE);
        encabezado.setPreferredSize(new Dimension(0, 30));

        DefaultTableCellRenderer celda = new DefaultTableCellRenderer();
        celda.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(celda);
        }
    }

    private void cargarHistorial() {
        Asociado seleccionado = (Asociado) comboAsociados.getSelectedItem();
        if (seleccionado == null) {
            limpiar();
            return;
        }

        try {
            List<Prestamo> prestamos =
                historialService.listarPrestamos(seleccionado.getId());
            List<Cuota> cuotas =
                historialService.listarCuotas(seleccionado.getId());
            List<Pago> pagos =
                historialService.listarPagos(seleccionado.getId());

            Map<Long, Prestamo> prestamosPorId = new HashMap<>();
            for (Prestamo prestamo : prestamos) {
                prestamosPorId.put(prestamo.getId(), prestamo);
            }
            Map<Long, Cuota> cuotasPorId = new HashMap<>();
            for (Cuota cuota : cuotas) {
                cuotasPorId.put(cuota.getId(), cuota);
            }

            modeloPrestamos.setDatos(prestamos);
            modeloCuotas.setDatos(cuotas, prestamosPorId);
            modeloPagos.setDatos(pagos, cuotasPorId, prestamosPorId);
            tituloPrestamos.setText("Préstamos (" + prestamos.size() + ")");
            tituloCuotas.setText("Cuotas (" + cuotas.size() + ")");
            tituloPagos.setText("Pagos (" + pagos.size() + ")");
            estado.setText("Estado: " + seleccionado.getDocumento() + " - "
                + seleccionado.getNombres() + " "
                + seleccionado.getApellidos());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo consultar el historial del asociado.", e);
            limpiar();
            estado.setText("No se pudo consultar el historial del asociado.");
            JOptionPane.showMessageDialog(this,
                "No se pudo consultar el historial del asociado. "
                    + "Intente nuevamente.",
                "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        modeloPrestamos.setDatos(List.of());
        modeloCuotas.setDatos(List.of(), Map.of());
        modeloPagos.setDatos(List.of(), Map.of(), Map.of());
        tituloPrestamos.setText("Préstamos (0)");
        tituloCuotas.setText("Cuotas (0)");
        tituloPagos.setText("Pagos (0)");
        estado.setText("Seleccione un asociado para ver su historial.");
    }

    private Long idAsociadoSeleccionado() {
        Asociado seleccionado = (Asociado) comboAsociados.getSelectedItem();
        return seleccionado == null ? null : seleccionado.getId();
    }

    private static final class AsociadoRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista,
                Object valor, int indice, boolean seleccionado, boolean conFoco) {
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

    private static final class PrestamoTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS = {
            "Fecha", "Monto", "Plazo", "Cuotas", "Valor cuota", "Estado"
        };

        private List<Prestamo> prestamos = new ArrayList<>();

        void setDatos(List<Prestamo> prestamos) {
            this.prestamos = new ArrayList<>(prestamos);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return prestamos.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int columna) {
            return COLUMNAS[columna];
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Prestamo prestamo = prestamos.get(fila);
            return switch (columna) {
                case 0 -> prestamo.getFecha().toString();
                case 1 -> Estilo.formatearMoneda(prestamo.getMonto());
                case 2 -> prestamo.getPlazo();
                case 3 -> String.valueOf(prestamo.getNumeroCuotas());
                case 4 -> Estilo.formatearMoneda(prestamo.getValorCuota());
                case 5 -> prestamo.getEstado();
                default -> "";
            };
        }
    }

    private static final class CuotaTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS = {
            "Préstamo", "Número", "Valor", "Vencimiento"
        };

        private List<Cuota> cuotas = new ArrayList<>();
        private Map<Long, Prestamo> prestamos = new HashMap<>();

        void setDatos(List<Cuota> cuotas, Map<Long, Prestamo> prestamos) {
            this.cuotas = new ArrayList<>(cuotas);
            this.prestamos = new HashMap<>(prestamos);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return cuotas.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int columna) {
            return COLUMNAS[columna];
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Cuota cuota = cuotas.get(fila);
            return switch (columna) {
                case 0 -> textoPrestamo(cuota.getPrestamoId());
                case 1 -> String.valueOf(cuota.getNumeroCuota());
                case 2 -> Estilo.formatearMoneda(cuota.getValor());
                case 3 -> cuota.getFechaVencimiento().toString();
                default -> "";
            };
        }

        private String textoPrestamo(long prestamoId) {
            Prestamo prestamo = prestamos.get(prestamoId);
            if (prestamo == null) {
                return "(préstamo no encontrado)";
            }
            return prestamo.getFecha() + " - " + prestamo.getMonto();
        }
    }

    private static final class PagoTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS = {
            "Cuota", "Fecha", "Valor", "N.º de recibo"
        };

        private List<Pago> pagos = new ArrayList<>();
        private Map<Long, Cuota> cuotas = new HashMap<>();
        private Map<Long, Prestamo> prestamos = new HashMap<>();

        void setDatos(List<Pago> pagos, Map<Long, Cuota> cuotas,
                      Map<Long, Prestamo> prestamos) {
            this.pagos = new ArrayList<>(pagos);
            this.cuotas = new HashMap<>(cuotas);
            this.prestamos = new HashMap<>(prestamos);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return pagos.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int columna) {
            return COLUMNAS[columna];
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Pago pago = pagos.get(fila);
            return switch (columna) {
                case 0 -> textoCuota(pago.getCuotaId());
                case 1 -> pago.getFechaPago().toString();
                case 2 -> Estilo.formatearMoneda(pago.getValor());
                case 3 -> pago.getNumeroRecibo();
                default -> "";
            };
        }

        private String textoCuota(long cuotaId) {
            Cuota cuota = cuotas.get(cuotaId);
            if (cuota == null) {
                return "(cuota no encontrada)";
            }
            Prestamo prestamo = prestamos.get(cuota.getPrestamoId());
            if (prestamo == null) {
                return "(préstamo no encontrado)";
            }
            return prestamo.getFecha() + " - Cuota " + cuota.getNumeroCuota();
        }
    }
}
