package com.lasferias.cooperativa.ui;

import com.lasferias.cooperativa.service.AsociadoService;
import com.lasferias.cooperativa.service.PrestamoService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class VentanaPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PanelAsociados panelAsociados;
    private final PanelPrestamos panelPrestamos;
    private final JButton pestanaAsociados = new JButton("Asociados");
    private final JButton pestanaPrestamos = new JButton("Préstamos");

    public VentanaPrincipal() {
        this(new AsociadoService(), new PrestamoService());
    }

    public VentanaPrincipal(AsociadoService asociadoService,
                            PrestamoService prestamoService) {
        super("Cooperativa Las Ferias - Principal");
        this.panelAsociados = new PanelAsociados(asociadoService);
        this.panelPrestamos = new PanelPrestamos(prestamoService, asociadoService);
        configurarVentana();
        construirInterfaz();
        mostrarAsociados();
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

        contenido.setBackground(Estilo.FONDO);
        contenido.add(panelAsociados, "asociados");
        contenido.add(panelPrestamos, "prestamos");
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
        pestanaAsociados.addActionListener(e -> mostrarAsociados());
        pestanaPrestamos.addActionListener(e -> mostrarPrestamos());
        centro.add(pestanaAsociados);
        centro.add(pestanaPrestamos);
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
        panelAsociados.refrescar();
    }

    private void mostrarPrestamos() {
        tarjetas.show(contenido, "prestamos");
        configurarPestana(pestanaAsociados, false);
        configurarPestana(pestanaPrestamos, true);
        panelPrestamos.refrescar();
    }
}
