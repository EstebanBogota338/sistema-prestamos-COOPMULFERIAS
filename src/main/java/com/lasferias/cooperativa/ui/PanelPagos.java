package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.CuotaService;
import com.lasferias.cooperativa.service.PagoService;
import com.lasferias.cooperativa.service.PrestamoService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class PanelPagos extends JPanel {

    private static final Logger LOGGER =
        Logger.getLogger(PanelPagos.class.getName());

    private final PagoService pagoService;
    private final CuotaService cuotaService;
    private final PrestamoService prestamoService;
    private final AsociadoService asociadoService;
    private final PagoTableModel modelo = new PagoTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JLabel tituloTabla = new JLabel();
    private final JLabel estado = new JLabel();
    private final JButton editar = new JButton("Editar");

    private final Map<Long, Cuota> cuotas = new HashMap<>();
    private final Map<Long, Prestamo> prestamos = new HashMap<>();
    private final Map<Long, Asociado> asociados = new HashMap<>();
    private List<Cuota> cuotasDisponibles = new ArrayList<>();
    private List<Prestamo> prestamosDisponibles = new ArrayList<>();
    private List<Asociado> asociadosDisponibles = new ArrayList<>();

    public PanelPagos(PagoService pagoService, CuotaService cuotaService,
                      PrestamoService prestamoService,
                      AsociadoService asociadoService) {
        this.pagoService = pagoService;
        this.cuotaService = cuotaService;
        this.prestamoService = prestamoService;
        this.asociadoService = asociadoService;
        construirInterfaz();
        refrescar();
    }

    public void refrescar() {
        try {
            List<Asociado> listaAsociados = asociadoService.buscar("");
            asociados.clear();
            for (Asociado asociado : listaAsociados) {
                asociados.put(asociado.getId(), asociado);
            }
            asociadosDisponibles = new ArrayList<>(listaAsociados);

            List<Prestamo> listaPrestamos = prestamoService.listar();
            prestamos.clear();
            for (Prestamo prestamo : listaPrestamos) {
                prestamos.put(prestamo.getId(), prestamo);
            }
            prestamosDisponibles = new ArrayList<>(listaPrestamos);

            List<Cuota> listaCuotas = cuotaService.listar();
            cuotas.clear();
            for (Cuota cuota : listaCuotas) {
                cuotas.put(cuota.getId(), cuota);
            }
            cuotasDisponibles = new ArrayList<>(listaCuotas);

            List<Pago> pagos = pagoService.listar();
            modelo.setDatos(pagos, cuotas, prestamos, asociados);
            tituloTabla.setText("Pagos registrados (" + pagos.size() + ")");
            estado.setText("Estado: " + pagos.size()
                + (pagos.size() == 1 ? " pago" : " pagos"));
            editar.setEnabled(false);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo consultar la lista de pagos.", e);
            estado.setText("No se pudo consultar la lista de pagos.");
            JOptionPane.showMessageDialog(this,
                "No se pudo consultar la lista de pagos. Intente nuevamente.",
                "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
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

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        tituloTabla.setFont(Estilo.fuente(Font.BOLD, 14));
        tituloTabla.setForeground(Estilo.TEXTO);
        encabezado.add(tituloTabla, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton nuevo = new JButton("Nuevo pago");
        JButton actualizar = new JButton("Actualizar lista");
        editar.setEnabled(false);
        Estilo.botonPrimario(nuevo);
        Estilo.botonSecundario(editar);
        Estilo.botonSecundario(actualizar);
        nuevo.addActionListener(e -> nuevoPago());
        editar.addActionListener(e -> editarPagoSeleccionado());
        actualizar.addActionListener(e -> refrescar());
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(actualizar);
        encabezado.add(acciones, BorderLayout.EAST);
        tarjeta.add(encabezado, BorderLayout.NORTH);

        configurarTabla();
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        desplazamiento.getViewport().setBackground(Estilo.SUPERFICIE);
        tarjeta.add(desplazamiento, BorderLayout.CENTER);

        estado.setFont(Estilo.fuente(Font.PLAIN, 12));
        estado.setForeground(Estilo.TEXTO_SECUNDARIO);
        tarjeta.add(estado, BorderLayout.SOUTH);

        add(tarjeta, BorderLayout.CENTER);
    }

    private void configurarTabla() {
        tabla.setFont(Estilo.fuente(Font.PLAIN, 13));
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
        tabla.getTableHeader().setResizingAllowed(true);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                editar.setEnabled(tabla.getSelectedRow() >= 0);
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    editarPagoSeleccionado();
                }
            }
        });

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(Estilo.fuente(Font.BOLD, 12));
        encabezado.setForeground(Estilo.TEXTO_SECUNDARIO);
        encabezado.setBackground(Estilo.SUPERFICIE);
        encabezado.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer celda = new DefaultTableCellRenderer();
        celda.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
        for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(celda);
        }

        tabla.getColumnModel().getColumn(0).setPreferredWidth(430);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(140);
    }

    private void nuevoPago() {
        if (cuotasDisponibles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero registre una cuota para crear un pago.",
                "Nuevo pago", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Pago creado = FormularioPago.mostrar(
            propietario(), pagoService,
            new ArrayList<>(cuotasDisponibles),
            new ArrayList<>(prestamosDisponibles),
            new ArrayList<>(asociadosDisponibles), null);
        if (creado != null) {
            refrescar();
            seleccionarPorId(creado.getId());
        }
    }

    private void editarPagoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un pago de la tabla para editarlo.",
                "Editar pago", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Pago seleccionado = modelo.getPagoEn(fila);
        Pago actualizado = FormularioPago.mostrar(
            propietario(), pagoService,
            new ArrayList<>(cuotasDisponibles),
            new ArrayList<>(prestamosDisponibles),
            new ArrayList<>(asociadosDisponibles), seleccionado);
        if (actualizado != null) {
            refrescar();
            seleccionarPorId(actualizado.getId());
        }
    }

    private void seleccionarPorId(long id) {
        int fila = modelo.buscarFilaPorId(id);
        if (fila >= 0) {
            tabla.setRowSelectionInterval(fila, fila);
            tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
        }
    }

    private JFrame propietario() {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        return ventana instanceof JFrame frame ? frame : null;
    }

    private static final class PagoTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS = {
            "Cuota", "Fecha", "Valor", "N.º de recibo"
        };

        private List<Pago> pagos = new ArrayList<>();
        private Map<Long, Cuota> cuotas = new HashMap<>();
        private Map<Long, Prestamo> prestamos = new HashMap<>();
        private Map<Long, Asociado> asociados = new HashMap<>();

        void setDatos(List<Pago> pagos, Map<Long, Cuota> cuotas,
                      Map<Long, Prestamo> prestamos,
                      Map<Long, Asociado> asociados) {
            this.pagos = new ArrayList<>(pagos);
            this.cuotas = new HashMap<>(cuotas);
            this.prestamos = new HashMap<>(prestamos);
            this.asociados = new HashMap<>(asociados);
            fireTableDataChanged();
        }

        Pago getPagoEn(int fila) {
            return pagos.get(fila);
        }

        int buscarFilaPorId(long id) {
            for (int i = 0; i < pagos.size(); i++) {
                if (pagos.get(i).getId() == id) {
                    return i;
                }
            }
            return -1;
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
                case 2 -> String.valueOf(pago.getValor());
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
            Asociado asociado = asociados.get(prestamo.getAsociadoId());
            if (asociado == null) {
                return "(asociado no encontrado)";
            }
            return asociado.getDocumento() + " - "
                + asociado.getNombres() + " " + asociado.getApellidos()
                + " - " + prestamo.getFecha() + " - " + prestamo.getMonto()
                + " - Cuota " + cuota.getNumeroCuota();
        }
    }
}
