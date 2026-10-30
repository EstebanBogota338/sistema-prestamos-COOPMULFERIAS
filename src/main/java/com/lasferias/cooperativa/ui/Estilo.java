package com.lasferias.cooperativa.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Estilos compartidos de la interfaz.
 * Centraliza la paleta, la tipografía, los botones y los bordes de campo.
 */
public final class Estilo {

    public static final Color FONDO = new Color(0xF5F7F6);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = new Color(0x212529);
    public static final Color TEXTO_SECUNDARIO = new Color(0x6B7280);
    /** Texto de ayuda bajo los campos (11 px). Más oscuro que TEXTO_SECUNDARIO para ganar contraste. */
    public static final Color TEXTO_AYUDA = new Color(0x4B5563);
    public static final Color BORDE = new Color(0xE2E5E3);
    public static final Color VERDE = new Color(0x2F8F57);
    public static final Color VERDE_OSCURO = new Color(0x256F45);
    public static final Color VERDE_SUAVE = new Color(0xE8F5EC);
    public static final Color ERROR = new Color(0xB42318);

    private static final int MARGEN_VERTICAL_BOTON = 8;
    private static final int MARGEN_HORIZONTAL_BOTON = 15;

    private Estilo() {
    }

    public static Font fuente(int estilo, int tamano) {
        return new Font("Segoe UI", estilo, tamano);
    }

    public static void botonPrimario(JButton boton) {
        boton.setUI(new BasicButtonUI());
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(VERDE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VERDE),
            margenBoton()));
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

    public static void botonSecundario(JButton boton) {
        boton.setFont(fuente(Font.PLAIN, 13));
        boton.setForeground(TEXTO);
        boton.setBackground(SUPERFICIE);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE),
            margenBoton()));
    }

    public static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE),
            BorderFactory.createEmptyBorder(7, 9, 7, 9));
    }

    public static void campo(JTextField campo) {
        campo.setFont(fuente(Font.PLAIN, 13));
        campo.setForeground(TEXTO);
        campo.setBackground(SUPERFICIE);
        campo.setCaretColor(TEXTO);
        campo.setBorder(bordeCampo());
        campo.setPreferredSize(new Dimension(300, 34));
    }

    public static void campoPassword(JPasswordField campo) {
        campo(campo);
    }

    private static Border margenBoton() {
        return BorderFactory.createEmptyBorder(
            MARGEN_VERTICAL_BOTON,
            MARGEN_HORIZONTAL_BOTON,
            MARGEN_VERTICAL_BOTON,
            MARGEN_HORIZONTAL_BOTON);
    }
}
