package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.AsociadoDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.model.Prestamo;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PrestamoService {

    private final PrestamoDAO prestamoDAO;
    private final AsociadoDAO asociadoDAO;

    public PrestamoService() {
        this(new PrestamoDAO(), new AsociadoDAO());
    }

    public PrestamoService(PrestamoDAO prestamoDAO, AsociadoDAO asociadoDAO) {
        this.prestamoDAO = Objects.requireNonNull(prestamoDAO,
            "El DAO de préstamos es obligatorio");
        this.asociadoDAO = Objects.requireNonNull(asociadoDAO,
            "El DAO de asociados es obligatorio");
    }

    public Prestamo registrar(Prestamo prestamo) throws SQLException {
        if (prestamo == null) {
            throw new IllegalArgumentException(
                "Los datos del préstamo son obligatorios.");
        }
        prestamo.setId(0);
        validar(prestamo);
        prestamoDAO.crear(prestamo);
        return prestamo;
    }

    public void actualizar(Prestamo prestamo) throws SQLException {
        if (prestamo == null) {
            throw new IllegalArgumentException(
                "Los datos del préstamo son obligatorios.");
        }
        if (prestamo.getId() <= 0) {
            throw new IllegalArgumentException(
                "Seleccione un préstamo válido para editar.");
        }

        Prestamo persistido = prestamoDAO.buscarPorId(prestamo.getId())
            .orElseThrow(() -> new SQLException(
                "No se encontró el préstamo que se desea actualizar."));

        if (persistido.getAsociadoId() != prestamo.getAsociadoId()) {
            throw new IllegalArgumentException(
                "No se puede cambiar el asociado del préstamo.");
        }

        validar(prestamo);
        if (!prestamoDAO.actualizar(prestamo)) {
            throw new SQLException(
                "No se encontró el préstamo que se desea actualizar.");
        }
    }

    public Optional<Prestamo> buscarPorId(long id) throws SQLException {
        return prestamoDAO.buscarPorId(id);
    }

    public List<Prestamo> listar() throws SQLException {
        return prestamoDAO.listar();
    }

    private void validar(Prestamo prestamo) throws SQLException {
        validarCampos(prestamo);
        if (asociadoDAO.buscarPorId(prestamo.getAsociadoId()).isEmpty()) {
            throw new IllegalArgumentException("Seleccione un asociado válido.");
        }
    }

    private static void validarCampos(Prestamo prestamo) {
        if (prestamo.getAsociadoId() <= 0) {
            throw new IllegalArgumentException("Seleccione un asociado válido.");
        }
        if (prestamo.getMonto() <= 0) {
            throw new IllegalArgumentException(
                "El monto debe ser mayor que cero.");
        }
        if (prestamo.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        String plazo = prestamo.getPlazo() == null
            ? "" : prestamo.getPlazo().trim();
        if (plazo.isEmpty()) {
            throw new IllegalArgumentException("El plazo es obligatorio.");
        }
        prestamo.setPlazo(plazo);

        if (prestamo.getNumeroCuotas() <= 0) {
            throw new IllegalArgumentException(
                "El número de cuotas debe ser mayor que cero.");
        }
        if (prestamo.getValorCuota() <= 0) {
            throw new IllegalArgumentException(
                "El valor de cuota debe ser mayor que cero.");
        }

        String estado = prestamo.getEstado() == null
            ? "" : prestamo.getEstado().trim();
        if (!Prestamo.ESTADO_VIGENTE.equals(estado)
                && !Prestamo.ESTADO_FINALIZADO.equals(estado)) {
            throw new IllegalArgumentException(
                "El estado debe ser Vigente o Finalizado.");
        }
        prestamo.setEstado(estado);
    }
}
