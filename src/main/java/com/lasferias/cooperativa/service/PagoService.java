package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.CuotaDAO;
import com.lasferias.cooperativa.dao.PagoDAO;
import com.lasferias.cooperativa.model.Pago;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PagoService {

    private final PagoDAO pagoDAO;
    private final CuotaDAO cuotaDAO;

    public PagoService() {
        this(new PagoDAO(), new CuotaDAO());
    }

    public PagoService(PagoDAO pagoDAO, CuotaDAO cuotaDAO) {
        this.pagoDAO = Objects.requireNonNull(pagoDAO,
            "El DAO de pagos es obligatorio");
        this.cuotaDAO = Objects.requireNonNull(cuotaDAO,
            "El DAO de cuotas es obligatorio");
    }

    public Pago registrar(Pago pago) throws SQLException {
        if (pago == null) {
            throw new IllegalArgumentException(
                "Los datos del pago son obligatorios.");
        }
        pago.setId(0);
        validarCampos(pago);
        if (cuotaDAO.buscarPorId(pago.getCuotaId()).isEmpty()) {
            throw new IllegalArgumentException("Seleccione una cuota válida.");
        }
        pagoDAO.crear(pago);
        return pago;
    }

    public void actualizar(Pago pago) throws SQLException {
        if (pago == null) {
            throw new IllegalArgumentException(
                "Los datos del pago son obligatorios.");
        }
        if (pago.getId() <= 0) {
            throw new IllegalArgumentException(
                "Seleccione un pago válido para editar.");
        }

        Pago persistido = pagoDAO.buscarPorId(pago.getId())
            .orElseThrow(() -> new SQLException(
                "No se encontró el pago que se desea actualizar."));

        if (persistido.getCuotaId() != pago.getCuotaId()) {
            throw new IllegalArgumentException(
                "No se puede cambiar la cuota del pago.");
        }

        validarCampos(pago);
        if (!pagoDAO.actualizar(pago)) {
            throw new SQLException(
                "No se encontró el pago que se desea actualizar.");
        }
    }

    public Optional<Pago> buscarPorId(long id) throws SQLException {
        return pagoDAO.buscarPorId(id);
    }

    public List<Pago> listar() throws SQLException {
        return pagoDAO.listar();
    }

    public List<Pago> listarPorCuota(long cuotaId) throws SQLException {
        return pagoDAO.listarPorCuota(cuotaId);
    }

    private static void validarCampos(Pago pago) {
        if (pago.getCuotaId() <= 0) {
            throw new IllegalArgumentException("Seleccione una cuota válida.");
        }
        if (pago.getFechaPago() == null) {
            throw new IllegalArgumentException(
                "La fecha del pago es obligatoria.");
        }
        if (pago.getValor() <= 0) {
            throw new IllegalArgumentException(
                "El valor del pago debe ser mayor que cero.");
        }

        String recibo = pago.getNumeroRecibo() == null
            ? "" : pago.getNumeroRecibo().trim();
        if (recibo.isEmpty()) {
            throw new IllegalArgumentException(
                "El número de recibo es obligatorio.");
        }
        pago.setNumeroRecibo(recibo);
    }
}
