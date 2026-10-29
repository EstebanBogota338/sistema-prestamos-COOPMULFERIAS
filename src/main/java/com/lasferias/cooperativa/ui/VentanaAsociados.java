package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.service.AsociadoService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class VentanaAsociados extends JFrame {

    private static final Logger LOGGER =
        Logger.getLogger(VentanaAsociados.class.getName());

    private final AsociadoService service;
    private final AsociadoTableModel modelo = new AsociadoTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JTextField campoBusqueda = new JTextField();
    private final JLabel tituloTabla = new JLabel();
    private final JLabel estado = new JLabel();
    private final JButton editar = new JButton("Editar");

    public VentanaAsociados(AsociadoService service) {
        super("Cooperativa Las Ferias - Asociados");
        this.service = service;

        configurarVentana();
        construirInterfaz();
        cargarAsociados("", null);
    }

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(crearBarraSuperior(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBackground(Estilo.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));
        contenido.add(crearTarjetaBusqueda(), BorderLayout.NORTH);
        contenido.add(crearTarjetaTabla(), BorderLayout.CENTER);

        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.SUPERFICIE);
        barra.setPreferredSize(new Dimension(0, 52));
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
            BorderFactory.createEmptyBorder(0, 20, 0, 20)));

        JLabel nombreApp = new JLabel("Cooperativa Las Ferias");
        nombreApp.setFont(Estilo.fuente(Font.BOLD, 15));
        nombreApp.setForeground(Estilo.TEXTO);
        barra.add(nombreApp, BorderLayout.WEST);

        JLabel pestana = new JLabel("Asociados", SwingConstants.CENTER);
        pestana.setFont(Estilo.fuente(Font.BOLD, 13));
        pestana.setForeground(Estilo.VERDE);
        pestana.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 14, 13, 14),
            BorderFactory.createMatteBorder(0, 0, 2, 0, Estilo.VERDE)));
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.setOpaque(false);
        centro.add(pestana);
        barra.add(centro, BorderLayout.CENTER);

        JLabel administrador = new JLabel("Administrador");
        administrador.setFont(Estilo.fuente(Font.PLAIN, 13));
        administrador.setForeground(Estilo.TEXTO_SECUNDARIO);
        JLabel avatar = new JLabel("AD", SwingConstants.CENTER);
        avatar.setFont(Estilo.fuente(Font.BOLD, 11));
        avatar.setForeground(Estilo.VERDE);
        avatar.setOpaque(true);
        avatar.setBackground(Estilo.VERDE_SUAVE);
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setBorder(BorderFactory.createLineBorder(Estilo.VERDE_SUAVE, 8));
        JPanel usuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        usuario.setOpaque(false);
        usuario.add(avatar);
        usuario.add(administrador);
        barra.add(usuario, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearTarjetaBusqueda() {
        JPanel tarjeta = tarjetaBlanca();
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDE),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel etiqueta = new JLabel("Buscar asociado");
        etiqueta.setFont(Estilo.fuente(Font.BOLD, 13));
        etiqueta.setForeground(Estilo.TEXTO_SECUNDARIO);
        tarjeta.add(etiqueta, BorderLayout.NORTH);

        JPanel filtros = new JPanel(new BorderLayout(10, 0));
        filtros.setOpaque(false);
        configurarCampoBusqueda();
        filtros.add(campoBusqueda, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton buscar = new JButton("Buscar");
        JButton limpiar = new JButton("Limpiar");
        Estilo.botonPrimario(buscar);
        Estilo.botonSecundario(limpiar);
        buscar.addActionListener(e -> buscar());
        limpiar.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarAsociados("", "Lista actualizada.");
        });
        acciones.add(buscar);
        acciones.add(limpiar);
        filtros.add(acciones, BorderLayout.EAST);
        tarjeta.add(filtros, BorderLayout.CENTER);

        JLabel ayuda = new JLabel(
            "Ingrese documento, nombres o apellidos y presione Buscar.");
        ayuda.setFont(Estilo.fuente(Font.PLAIN, 11));
        ayuda.setForeground(Estilo.TEXTO_SECUNDARIO);
        tarjeta.add(ayuda, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaTabla() {
        JPanel tarjeta = tarjetaBlanca();
        tarjeta.setLayout(new BorderLayout(0, 12));
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
        JButton nuevo = new JButton("Nuevo asociado");
        JButton actualizar = new JButton("Actualizar lista");
        editar.setEnabled(false);
        Estilo.botonPrimario(nuevo);
        Estilo.botonSecundario(editar);
        Estilo.botonSecundario(actualizar);
        nuevo.addActionListener(e -> nuevoAsociado());
        editar.addActionListener(e -> editarAsociadoSeleccionado());
        actualizar.addActionListener(e -> cargarAsociados("", "Lista actualizada."));
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
        return tarjeta;
    }

    private void configurarCampoBusqueda() {
        campoBusqueda.setFont(Estilo.fuente(Font.PLAIN, 13));
        campoBusqueda.setForeground(Estilo.TEXTO);
        campoBusqueda.setCaretColor(Estilo.TEXTO);
        campoBusqueda.setBackground(Estilo.SUPERFICIE);
        campoBusqueda.setPreferredSize(new Dimension(300, 34));
        campoBusqueda.setBorder(Estilo.bordeCampo());
        campoBusqueda.setToolTipText("Documento, nombres o apellidos");
        campoBusqueda.addActionListener(e -> buscar());
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
                    editarAsociadoSeleccionado();
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

        tabla.getColumnModel().getColumn(0).setPreferredWidth(125);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(125);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(145);
    }

    private void buscar() {
        cargarAsociados(campoBusqueda.getText(), null);
    }

    private void nuevoAsociado() {
        Asociado creado = FormularioAsociado.mostrar(this, service, null);
        if (creado != null) {
            campoBusqueda.setText("");
            cargarAsociados("", "Asociado registrado correctamente.");
            seleccionarPorId(creado.getId());
        }
    }

    private void editarAsociadoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un asociado de la tabla para editarlo.",
                "Editar asociado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Asociado seleccionado = modelo.getAsociadoEn(fila);
        Asociado actualizado = FormularioAsociado.mostrar(this, service, seleccionado);
        if (actualizado != null) {
            campoBusqueda.setText("");
            cargarAsociados("", "Asociado actualizado correctamente.");
            seleccionarPorId(actualizado.getId());
        }
    }

    private void cargarAsociados(String texto, String mensaje) {
        try {
            List<Asociado> asociados = service.buscar(texto);
            modelo.setAsociados(asociados);
            actualizarEstado(asociados.size(), texto, mensaje);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                "No se pudo consultar la lista de asociados.", e);
            estado.setText("No se pudo consultar la lista de asociados.");
            JOptionPane.showMessageDialog(this,
                "No se pudo consultar la lista de asociados. Intente nuevamente.",
                "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstado(int cantidad, String texto, String mensaje) {
        boolean filtrada = texto != null && !texto.isBlank();
        tituloTabla.setText(filtrada
            ? "Resultados de búsqueda (" + cantidad + ")"
            : "Asociados registrados (" + cantidad + ")");
        String base = filtrada
            ? "Estado: " + cantidad + " resultados"
            : "Estado: " + cantidad + " registros";
        estado.setText(mensaje == null ? base : base + " · " + mensaje);
    }

    private void seleccionarPorId(long id) {
        int fila = modelo.buscarFilaPorId(id);
        if (fila >= 0) {
            tabla.setRowSelectionInterval(fila, fila);
            tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
        }
    }

    private static JPanel tarjetaBlanca() {
        JPanel panel = new JPanel();
        panel.setBackground(Estilo.SUPERFICIE);
        return panel;
    }

    private static final class AsociadoTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS = {
            "Documento", "Nombres", "Apellidos", "Teléfono", "Fecha de registro"
        };

        private List<Asociado> asociados = new ArrayList<>();

        void setAsociados(List<Asociado> asociados) {
            this.asociados = new ArrayList<>(asociados);
            fireTableDataChanged();
        }

        Asociado getAsociadoEn(int fila) {
            return asociados.get(fila);
        }

        int buscarFilaPorId(long id) {
            for (int i = 0; i < asociados.size(); i++) {
                if (asociados.get(i).getId() == id) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getRowCount() {
            return asociados.size();
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
            Asociado asociado = asociados.get(fila);
            return switch (columna) {
                case 0 -> asociado.getDocumento();
                case 1 -> asociado.getNombres();
                case 2 -> asociado.getApellidos();
                case 3 -> asociado.getTelefono();
                case 4 -> {
                    LocalDate fecha = asociado.getFechaRegistro();
                    yield fecha == null ? "" : fecha.toString();
                }
                default -> "";
            };
        }
    }
}
