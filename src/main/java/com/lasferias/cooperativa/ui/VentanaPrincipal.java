package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.CopiaSeguridadService;
import com.lasferias.cooperativa.service.CuotaService;
import com.lasferias.cooperativa.service.HistorialService;
import com.lasferias.cooperativa.service.PagoService;
import com.lasferias.cooperativa.service.PrestamoService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VentanaPrincipal extends JFrame {

    private static final Logger LOGGER =
        Logger.getLogger(VentanaPrincipal.class.getName());

    private final CopiaSeguridadService copiaSeguridadService =
        new CopiaSeguridadService();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PanelAsociados panelAsociados;
    private final PanelPrestamos panelPrestamos;
    private final PanelCuotas panelCuotas;
    private final PanelPagos panelPagos;
    private final PanelHistorial panelHistorial;
    private final JButton pestanaAsociados = new JButton("Asociados");
    private final JButton pestanaPrestamos = new JButton("Préstamos");
    private final JButton pestanaCuotas = new JButton("Cuotas");
    private final JButton pestanaPagos = new JButton("Pagos");
    private final JButton pestanaHistorial = new JButton("Historial");

    public VentanaPrincipal() {
        this(new AsociadoService(), new PrestamoService(), new CuotaService(),
            new PagoService(), new HistorialService());
    }

    public VentanaPrincipal(AsociadoService asociadoService,
                            PrestamoService prestamoService,
                            CuotaService cuotaService,
                            PagoService pagoService,
                            HistorialService historialService) {
        super("Cooperativa Las Ferias - Principal");
        this.panelAsociados = new PanelAsociados(asociadoService);
        this.panelPrestamos = new PanelPrestamos(prestamoService, asociadoService);
        this.panelCuotas = new PanelCuotas(cuotaService, prestamoService, asociadoService);
        this.panelPagos = new PanelPagos(pagoService, cuotaService, prestamoService, asociadoService);
        this.panelHistorial = new PanelHistorial(historialService, asociadoService);
        configurarVentana();
        construirInterfaz();
        mostrarAsociados();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(crearBarraSuperior(), BorderLayout.NORTH);

        contenido.setBackground(Estilo.FONDO);
        contenido.add(panelAsociados, "asociados");
        contenido.add(panelPrestamos, "prestamos");
        contenido.add(panelCuotas, "cuotas");
        contenido.add(panelPagos, "pagos");
        contenido.add(panelHistorial, "historial");
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

        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        centro.setOpaque(false);
        configurarPestana(pestanaAsociados, true);
        configurarPestana(pestanaPrestamos, false);
        configurarPestana(pestanaCuotas, false);
        configurarPestana(pestanaPagos, false);
        configurarPestana(pestanaHistorial, false);
        pestanaAsociados.addActionListener(e -> mostrarAsociados());
        pestanaPrestamos.addActionListener(e -> mostrarPrestamos());
        pestanaCuotas.addActionListener(e -> mostrarCuotas());
        pestanaPagos.addActionListener(e -> mostrarPagos());
        pestanaHistorial.addActionListener(e -> mostrarHistorial());
        centro.add(pestanaAsociados);
        centro.add(pestanaPrestamos);
        centro.add(pestanaCuotas);
        centro.add(pestanaPagos);
        centro.add(pestanaHistorial);
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
        avatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel usuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        usuario.setOpaque(false);
        JButton copia = new JButton("Copia de seguridad");
        Estilo.botonSecundario(copia);
        copia.addActionListener(e -> crearCopiaSeguridad(copia));
        usuario.add(copia);
        usuario.add(avatar);
        usuario.add(administrador);
        barra.add(usuario, BorderLayout.EAST);
        return barra;
    }

    private void configurarPestana(JButton boton, boolean activa) {
        boton.setFont(Estilo.fuente(Font.BOLD, 13));
        boton.setForeground(activa ? Estilo.VERDE : Estilo.TEXTO_SECUNDARIO);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 14, 13, 14),
            BorderFactory.createMatteBorder(0, 0, 2, 0,
                activa ? Estilo.VERDE : Estilo.SUPERFICIE)));
    }

    private void mostrarAsociados() {
        tarjetas.show(contenido, "asociados");
        configurarPestana(pestanaAsociados, true);
        configurarPestana(pestanaPrestamos, false);
        configurarPestana(pestanaCuotas, false);
        configurarPestana(pestanaPagos, false);
        configurarPestana(pestanaHistorial, false);
        panelAsociados.refrescar();
    }

    private void mostrarPrestamos() {
        tarjetas.show(contenido, "prestamos");
        configurarPestana(pestanaAsociados, false);
        configurarPestana(pestanaPrestamos, true);
        configurarPestana(pestanaCuotas, false);
        configurarPestana(pestanaPagos, false);
        configurarPestana(pestanaHistorial, false);
        panelPrestamos.refrescar();
    }

    private void mostrarCuotas() {
        tarjetas.show(contenido, "cuotas");
        configurarPestana(pestanaAsociados, false);
        configurarPestana(pestanaPrestamos, false);
        configurarPestana(pestanaCuotas, true);
        configurarPestana(pestanaPagos, false);
        configurarPestana(pestanaHistorial, false);
        panelCuotas.refrescar();
    }

    private void mostrarPagos() {
        tarjetas.show(contenido, "pagos");
        configurarPestana(pestanaAsociados, false);
        configurarPestana(pestanaPrestamos, false);
        configurarPestana(pestanaCuotas, false);
        configurarPestana(pestanaPagos, true);
        configurarPestana(pestanaHistorial, false);
        panelPagos.refrescar();
    }

    private void mostrarHistorial() {
        tarjetas.show(contenido, "historial");
        configurarPestana(pestanaAsociados, false);
        configurarPestana(pestanaPrestamos, false);
        configurarPestana(pestanaCuotas, false);
        configurarPestana(pestanaPagos, false);
        configurarPestana(pestanaHistorial, true);
        panelHistorial.refrescar();
    }

    private void crearCopiaSeguridad(JButton boton) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar copia de seguridad");
        selector.setSelectedFile(new File(
            "cooperativa-" + LocalDate.now() + ".db"));
        selector.setFileFilter(new FileNameExtensionFilter(
            "Bases de datos SQLite (*.db)", "db"));
        File documentos = new File(
            System.getProperty("user.home"), "Documents");
        selector.setCurrentDirectory(documentos.isDirectory()
            ? documentos : new File(System.getProperty("user.home")));

        while (true) {
            int opcion = selector.showSaveDialog(this);
            if (opcion != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File archivo = selector.getSelectedFile();
            if (archivo == null) {
                return;
            }

            boolean sobrescribir = false;
            if (archivo.exists()) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe. ¿Desea sobrescribirlo?",
                    "Confirmar sobrescritura", JOptionPane.YES_NO_OPTION);
                if (confirmacion != JOptionPane.YES_OPTION) {
                    continue;
                }
                sobrescribir = true;
            }

            boton.setEnabled(false);
            try {
                Path ruta = copiaSeguridadService.crearCopia(
                    archivo.toPath(), sobrescribir);
                JOptionPane.showMessageDialog(this,
                    "Copia de seguridad creada en:\n" + ruta,
                    "Copia de seguridad", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Copia de seguridad", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE,
                    "No se pudo crear la copia de seguridad.", e);
                JOptionPane.showMessageDialog(this,
                    "No se pudo crear la copia de seguridad. "
                        + "Intente nuevamente.",
                    "Copia de seguridad", JOptionPane.ERROR_MESSAGE);
            } finally {
                boton.setEnabled(true);
            }
            return;
        }
    }
}
