package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Prestamo;
import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.CuotaService;
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

public class PanelCuotas extends JPanel {

    private static final Logger LOGGER =
        Logger.getLogger(PanelCuotas.class.getName());

    private final CuotaService cuotaService;
    private final PrestamoService prestamoService;
    private final AsociadoService asociadoService;
    private final CuotaTableModel modelo = new CuotaTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JLabel tituloTabla = new JLabel();
    private final JLabel estado = new JLabel();
    private final JButton editar = new JButton("Editar");

    private final Map<Long, Prestamo> prestamos = new HashMap<>();
    private final Map<Long, Asociado> asociados = new HashMap<>();
    private List<Prestamo> prestamosDisponibles = new ArrayList<>();
    private List<Asociado> asociadosDisponibles = new ArrayList<>();

    public PanelCuotas(CuotaService cuotaService,
                       PrestamoService prestamoService,
                       AsociadoService asociadoService) {
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

            List<Cuota> cuotas = cuotaService.listar();
            modelo.setDatos(cuotas, prestamos, asociados);
            tituloTabla.setText("Cuotas registradas (" + cuotas.size() + ")");
            estado.setText("Estado: " + cuotas.size()
                + (cuotas.size() == 1 ? " cuota" : " cuotas"));
            editar.setEnabled(false);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo consultar la lista de cuotas.", e);
            estado.setText("No se pudo consultar la lista de cuotas.");
            JOptionPane.showMessageDialog(this,
                "No se pudo consultar la lista de cuotas. Intente nuevamente.",
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
        JButton nuevo = new JButton("Nueva cuota");
        JButton actualizar = new JButton("Actualizar lista");
        editar.setEnabled(false);
        Estilo.botonPrimario(nuevo);
        Estilo.botonSecundario(editar);
        Estilo.botonSecundario(actualizar);
        nuevo.addActionListener(e -> nuevaCuota());
        editar.addActionListener(e -> editarCuotaSeleccionada());
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
                    editarCuotaSeleccionada();
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

        tabla.getColumnModel().getColumn(0).setPreferredWidth(340);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110);
    }

    private void nuevaCuota() {
        if (prestamosDisponibles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero registre un préstamo para crear una cuota.",
                "Nueva cuota", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Cuota creada = FormularioCuota.mostrar(
                propietario(), cuotaService,
                new ArrayList<>(prestamosDisponibles),
                new ArrayList<>(asociadosDisponibles), null);
            if (creada != null) {
                refrescar();
                seleccionarPorId(creada.getId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo abrir el formulario de cuota.", e);
            JOptionPane.showMessageDialog(this,
                "No se pudo abrir el formulario de cuota. Intente nuevamente.",
                "Nueva cuota", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarCuotaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione una cuota de la tabla para editarla.",
                "Editar cuota", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Cuota seleccionada = modelo.getCuotaEn(fila);
        try {
            Cuota actualizada = FormularioCuota.mostrar(
                propietario(), cuotaService,
                new ArrayList<>(prestamosDisponibles),
                new ArrayList<>(asociadosDisponibles), seleccionada);
            if (actualizada != null) {
                refrescar();
                seleccionarPorId(actualizada.getId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo abrir el formulario de cuota.", e);
            JOptionPane.showMessageDialog(this,
                "No se pudo abrir el formulario de cuota. Intente nuevamente.",
                "Editar cuota", JOptionPane.ERROR_MESSAGE);
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

    private static final class CuotaTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS = {
            "Préstamo", "Número", "Valor", "Vencimiento"
        };

        private List<Cuota> cuotas = new ArrayList<>();
        private Map<Long, Prestamo> prestamos = new HashMap<>();
        private Map<Long, Asociado> asociados = new HashMap<>();

        void setDatos(List<Cuota> cuotas, Map<Long, Prestamo> prestamos,
                      Map<Long, Asociado> asociados) {
            this.cuotas = new ArrayList<>(cuotas);
            this.prestamos = new HashMap<>(prestamos);
            this.asociados = new HashMap<>(asociados);
            fireTableDataChanged();
        }

        Cuota getCuotaEn(int fila) {
            return cuotas.get(fila);
        }

        int buscarFilaPorId(long id) {
            for (int i = 0; i < cuotas.size(); i++) {
                if (cuotas.get(i).getId() == id) {
                    return i;
                }
            }
            return -1;
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
                case 2 -> String.valueOf(cuota.getValor());
                case 3 -> cuota.getFechaVencimiento().toString();
                default -> "";
            };
        }

        private String textoPrestamo(long prestamoId) {
            Prestamo prestamo = prestamos.get(prestamoId);
            if (prestamo == null) {
                return "(préstamo no encontrado)";
            }
            Asociado asociado = asociados.get(prestamo.getAsociadoId());
            if (asociado == null) {
                return "(asociado no encontrado)";
            }
            return asociado.getDocumento() + " - "
                + asociado.getNombres() + " " + asociado.getApellidos()
                + " - " + prestamo.getFecha() + " - " + prestamo.getMonto();
        }
    }
}
