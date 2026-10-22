package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.model.Asociado;
import com.lasferias.cooperativa.service.AsociadoService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class VentanaAsociados extends JFrame {

    private static final Color FONDO = new Color(0xF5F7F6);
    private static final Color SUPERFICIE = Color.WHITE;
    private static final Color TEXTO = new Color(0x212529);
    private static final Color TEXTO_SECUNDARIO = new Color(0x6B7280);
    private static final Color BORDE = new Color(0xE2E5E3);
    private static final Color VERDE = new Color(0x2F8F57);
    private static final Color VERDE_OSCURO = new Color(0x256F45);
    private static final Color VERDE_SUAVE = new Color(0xE8F5EC);

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
        raiz.setBackground(FONDO);
        raiz.add(crearBarraSuperior(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));
        contenido.add(crearTarjetaBusqueda(), BorderLayout.NORTH);
        contenido.add(crearTarjetaTabla(), BorderLayout.CENTER);

        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(SUPERFICIE);
        barra.setPreferredSize(new Dimension(0, 52));
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
            BorderFactory.createEmptyBorder(0, 20, 0, 20)));

        JLabel nombreApp = new JLabel("Cooperativa Las Ferias");
        nombreApp.setFont(fuente(Font.BOLD, 15));
        nombreApp.setForeground(TEXTO);
        barra.add(nombreApp, BorderLayout.WEST);

        JLabel pestana = new JLabel("Asociados", SwingConstants.CENTER);
        pestana.setFont(fuente(Font.BOLD, 13));
        pestana.setForeground(VERDE);
        pestana.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 14, 13, 14),
            BorderFactory.createMatteBorder(0, 0, 2, 0, VERDE)));
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.setOpaque(false);
        centro.add(pestana);
        barra.add(centro, BorderLayout.CENTER);

        JLabel administrador = new JLabel("Administrador");
        administrador.setFont(fuente(Font.PLAIN, 13));
        administrador.setForeground(TEXTO_SECUNDARIO);
        JLabel avatar = new JLabel("AD", SwingConstants.CENTER);
        avatar.setFont(fuente(Font.BOLD, 11));
        avatar.setForeground(VERDE);
        avatar.setOpaque(true);
        avatar.setBackground(VERDE_SUAVE);
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setBorder(BorderFactory.createLineBorder(VERDE_SUAVE, 8));
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
            BorderFactory.createLineBorder(BORDE),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel etiqueta = new JLabel("Buscar asociado");
        etiqueta.setFont(fuente(Font.BOLD, 13));
        etiqueta.setForeground(TEXTO_SECUNDARIO);
        tarjeta.add(etiqueta, BorderLayout.NORTH);

        JPanel filtros = new JPanel(new BorderLayout(10, 0));
        filtros.setOpaque(false);
        configurarCampoBusqueda();
        filtros.add(campoBusqueda, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton buscar = new JButton("Buscar");
        JButton limpiar = new JButton("Limpiar");
        configurarBotonPrimario(buscar);
        configurarBotonSecundario(limpiar);
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
        ayuda.setFont(fuente(Font.PLAIN, 11));
        ayuda.setForeground(TEXTO_SECUNDARIO);
        tarjeta.add(ayuda, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaTabla() {
        JPanel tarjeta = tarjetaBlanca();
        tarjeta.setLayout(new BorderLayout(0, 12));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE),
            BorderFactory.createEmptyBorder(14, 16, 12, 16)));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        tituloTabla.setFont(fuente(Font.BOLD, 14));
        tituloTabla.setForeground(TEXTO);
        encabezado.add(tituloTabla, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        JButton nuevo = new JButton("Nuevo asociado");
        JButton actualizar = new JButton("Actualizar lista");
        editar.setEnabled(false);
        configurarBotonPrimario(nuevo);
        configurarBotonSecundario(editar);
        configurarBotonSecundario(actualizar);
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
        desplazamiento.setBorder(BorderFactory.createLineBorder(BORDE));
        desplazamiento.getViewport().setBackground(SUPERFICIE);
        tarjeta.add(desplazamiento, BorderLayout.CENTER);

        estado.setFont(fuente(Font.PLAIN, 12));
        estado.setForeground(TEXTO_SECUNDARIO);
        tarjeta.add(estado, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void configurarCampoBusqueda() {
        campoBusqueda.setFont(fuente(Font.PLAIN, 13));
        campoBusqueda.setForeground(TEXTO);
        campoBusqueda.setCaretColor(TEXTO);
        campoBusqueda.setBackground(SUPERFICIE);
        campoBusqueda.setPreferredSize(new Dimension(300, 34));
        campoBusqueda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE),
            BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        campoBusqueda.setToolTipText("Documento, nombres o apellidos");
        campoBusqueda.addActionListener(e -> buscar());
    }

    private void configurarTabla() {
        tabla.setFont(fuente(Font.PLAIN, 13));
        tabla.setForeground(TEXTO);
        tabla.setBackground(SUPERFICIE);
        tabla.setSelectionBackground(VERDE_SUAVE);
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(BORDE);
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
        encabezado.setFont(fuente(Font.BOLD, 12));
        encabezado.setForeground(TEXTO_SECUNDARIO);
        encabezado.setBackground(SUPERFICIE);
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
            actualizarEstado(asociados.size(), mensaje);
        } catch (SQLException e) {
            estado.setText("No se pudo consultar la lista de asociados.");
            JOptionPane.showMessageDialog(this,
                "No se pudo consultar la lista de asociados. Intente nuevamente.",
                "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstado(int cantidad, String mensaje) {
        tituloTabla.setText("Asociados registrados (" + cantidad + ")");
        String base = "Estado: " + cantidad + " registros";
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
        panel.setBackground(SUPERFICIE);
        return panel;
    }

    private static Font fuente(int estilo, int tamano) {
        return new Font("Segoe UI", estilo, tamano);
    }

    private static void configurarBotonPrimario(JButton boton) {
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(VERDE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VERDE),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(VERDE_OSCURO);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(VERDE);
            }
        });
    }

    private static void configurarBotonSecundario(JButton boton) {
        boton.setFont(fuente(Font.PLAIN, 13));
        boton.setForeground(TEXTO);
        boton.setBackground(SUPERFICIE);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
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
