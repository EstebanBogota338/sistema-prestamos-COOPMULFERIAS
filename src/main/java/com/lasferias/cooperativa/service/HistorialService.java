package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.HistorialDAO;
import com.lasferias.cooperativa.model.Cuota;
import com.lasferias.cooperativa.model.Pago;
import com.lasferias.cooperativa.model.Prestamo;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class HistorialService {

    private final HistorialDAO historialDAO;

    public HistorialService() {
        this(new HistorialDAO());
    }

    public HistorialService(HistorialDAO historialDAO) {
        this.historialDAO = Objects.requireNonNull(historialDAO,
            "El DAO de historial es obligatorio");
    }

    public List<Prestamo> listarPrestamos(long asociadoId) throws SQLException {
        validarAsociado(asociadoId);
        return historialDAO.listarPrestamos(asociadoId);
    }

    public List<Cuota> listarCuotas(long asociadoId) throws SQLException {
        validarAsociado(asociadoId);
        return historialDAO.listarCuotas(asociadoId);
    }

    public List<Pago> listarPagos(long asociadoId) throws SQLException {
        validarAsociado(asociadoId);
        return historialDAO.listarPagos(asociadoId);
    }

    private static void validarAsociado(long asociadoId) {
        if (asociadoId <= 0) {
            throw new IllegalArgumentException("Seleccione un asociado válido.");
        }
    }
}
