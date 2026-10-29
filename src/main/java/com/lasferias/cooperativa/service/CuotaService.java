package com.lasferias.cooperativa.service;

import com.lasferias.cooperativa.dao.CuotaDAO;
import com.lasferias.cooperativa.dao.PrestamoDAO;
import com.lasferias.cooperativa.model.Cuota;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CuotaService {

    private final CuotaDAO cuotaDAO;
    private final PrestamoDAO prestamoDAO;

    public CuotaService() {
        this(new CuotaDAO(), new PrestamoDAO());
    }

    public CuotaService(CuotaDAO cuotaDAO, PrestamoDAO prestamoDAO) {
        this.cuotaDAO = Objects.requireNonNull(cuotaDAO,
            "El DAO de cuotas es obligatorio");
        this.prestamoDAO = Objects.requireNonNull(prestamoDAO,
            "El DAO de préstamos es obligatorio");
    }

    public Cuota registrar(Cuota cuota) throws SQLException {
        if (cuota == null) {
            throw new IllegalArgumentException(
                "Los datos de la cuota son obligatorios.");
        }
        cuota.setId(0);
        validarCampos(cuota);
        if (prestamoDAO.buscarPorId(cuota.getPrestamoId()).isEmpty()) {
            throw new IllegalArgumentException("Seleccione un préstamo válido.");
        }
        if (cuotaDAO.existeNumero(
                cuota.getPrestamoId(), cuota.getNumeroCuota(), 0)) {
            throw new IllegalArgumentException(
                "Ya existe una cuota con ese número en este préstamo.");
        }

        try {
            cuotaDAO.crear(cuota);
        } catch (SQLException e) {
            if (esNumeroDuplicado(e)) {
                throw new IllegalArgumentException(
                    "Ya existe una cuota con ese número en este préstamo.", e);
            }
            throw e;
        }
        return cuota;
    }

    public void actualizar(Cuota cuota) throws SQLException {
        if (cuota == null) {
            throw new IllegalArgumentException(
                "Los datos de la cuota son obligatorios.");
        }
        if (cuota.getId() <= 0) {
            throw new IllegalArgumentException(
                "Seleccione una cuota válida para editar.");
        }

        Cuota persistida = cuotaDAO.buscarPorId(cuota.getId())
            .orElseThrow(() -> new SQLException(
                "No se encontró la cuota que se desea actualizar."));

        if (persistida.getPrestamoId() != cuota.getPrestamoId()) {
            throw new IllegalArgumentException(
                "No se puede cambiar el préstamo de la cuota.");
        }

        if (cuotaDAO.tienePagos(cuota.getId())) {
            throw new IllegalArgumentException(
                "Esta cuota tiene pagos registrados: no se puede modificar "
                    + "su número, valor ni fecha de vencimiento.");
        }

        validarCampos(cuota);
        if (cuotaDAO.existeNumero(
                cuota.getPrestamoId(), cuota.getNumeroCuota(), cuota.getId())) {
            throw new IllegalArgumentException(
                "Ya existe una cuota con ese número en este préstamo.");
        }

        boolean actualizada;
        try {
            actualizada = cuotaDAO.actualizar(cuota);
        } catch (SQLException e) {
            if (esNumeroDuplicado(e)) {
                throw new IllegalArgumentException(
                    "Ya existe una cuota con ese número en este préstamo.", e);
            }
            throw e;
        }

        if (!actualizada) {
            throw new SQLException(
                "No se encontró la cuota que se desea actualizar.");
        }
    }

    public Optional<Cuota> buscarPorId(long id) throws SQLException {
        return cuotaDAO.buscarPorId(id);
    }

    public List<Cuota> listar() throws SQLException {
        return cuotaDAO.listar();
    }

    public List<Cuota> listarPorPrestamo(long prestamoId) throws SQLException {
        return cuotaDAO.listarPorPrestamo(prestamoId);
    }

    public boolean tienePagos(long cuotaId) throws SQLException {
        return cuotaDAO.tienePagos(cuotaId);
    }

    private static void validarCampos(Cuota cuota) {
        if (cuota.getPrestamoId() <= 0) {
            throw new IllegalArgumentException("Seleccione un préstamo válido.");
        }
        if (cuota.getNumeroCuota() < 1) {
            throw new IllegalArgumentException(
                "El número de cuota debe ser mayor o igual a 1.");
        }
        if (cuota.getValor() <= 0) {
            throw new IllegalArgumentException(
                "El valor de la cuota debe ser mayor que cero.");
        }
        if (cuota.getFechaVencimiento() == null) {
            throw new IllegalArgumentException(
                "La fecha de vencimiento es obligatoria.");
        }
    }

    private static boolean esNumeroDuplicado(SQLException e) {
        return e instanceof org.sqlite.SQLiteException se
            && se.getResultCode()
                == org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE;
    }
}
