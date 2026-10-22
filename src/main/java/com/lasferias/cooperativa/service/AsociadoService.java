package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.model.Asociado;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AsociadoService {

    private final AsociadoDAO asociadoDAO;

    public AsociadoService() {
        this(new AsociadoDAO());
    }

    public AsociadoService(AsociadoDAO asociadoDAO) {
        this.asociadoDAO = Objects.requireNonNull(asociadoDAO,
            "El DAO de asociados es obligatorio");
    }

    public Asociado registrar(Asociado asociado) throws SQLException {
        prepararYValidar(asociado);
        asociado.setId(0);
        asociado.setFechaRegistro(LocalDate.now());
        validarDocumentoDisponible(asociado.getDocumento(), 0);

        try {
            asociadoDAO.crear(asociado);
            return asociado;
        } catch (SQLException e) {
            throw traducirDuplicadoSiCorresponde(e);
        }
    }

    public void actualizar(Asociado asociado) throws SQLException {
        prepararYValidar(asociado);
        if (asociado.getId() <= 0) {
            throw new IllegalArgumentException("Seleccione un asociado válido para editar.");
        }
        validarDocumentoDisponible(asociado.getDocumento(), asociado.getId());

        try {
            asociadoDAO.actualizar(asociado);
        } catch (SQLException e) {
            throw traducirDuplicadoSiCorresponde(e);
        }
    }

    public List<Asociado> buscar(String texto) throws SQLException {
        return asociadoDAO.buscarPorTexto(texto);
    }

    private void validarDocumentoDisponible(String documento, long idExcluir)
            throws SQLException {
        if (asociadoDAO.existeDocumento(documento, idExcluir)) {
            throw duplicado(null);
        }
    }

    private static void prepararYValidar(Asociado asociado) {
        if (asociado == null) {
            throw new IllegalArgumentException("Los datos del asociado son obligatorios.");
        }

        asociado.setDocumento(normalizarObligatorio(
            asociado.getDocumento(), "El documento es obligatorio."));
        asociado.setNombres(normalizarObligatorio(
            asociado.getNombres(), "Los nombres son obligatorios."));
        asociado.setApellidos(normalizarObligatorio(
            asociado.getApellidos(), "Los apellidos son obligatorios."));

        String telefono = normalizarOpcional(asociado.getTelefono());
        if (telefono != null && !telefono.matches("\\d{7,10}")) {
            throw new IllegalArgumentException(
                "El teléfono debe contener solo dígitos y tener entre 7 y 10 caracteres.");
        }
        asociado.setTelefono(telefono);
    }

    private static String normalizarObligatorio(String valor, String mensaje) {
        String normalizado = valor == null ? "" : valor.trim();
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return normalizado;
    }

    private static String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private static IllegalArgumentException duplicado(Throwable causa) {
        IllegalArgumentException error = new IllegalArgumentException(
            "Ya existe un asociado con el documento indicado.");
        if (causa != null) {
            error.initCause(causa);
        }
        return error;
    }

    private static SQLException traducirDuplicadoSiCorresponde(SQLException error) {
        if (esDuplicadoDeDocumento(error)) {
            throw duplicado(error);
        }
        return error;
    }

    private static boolean esDuplicadoDeDocumento(SQLException error) {
        Throwable actual = error;
        while (actual != null) {
            String mensaje = actual.getMessage();
            if (mensaje != null && mensaje.toLowerCase(Locale.ROOT)
                    .contains("unique constraint failed: asociado.documento")) {
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }
}
