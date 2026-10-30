package com.lasferias.cooperativa.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class InicializadorBD {

    private InicializadorBD() { }

    private static final String SQL_USUARIO = """
        CREATE TABLE IF NOT EXISTS usuario (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre_usuario   TEXT NOT NULL UNIQUE,
            contrasena_hash  TEXT NOT NULL
        )""";

    private static final String SQL_ASOCIADO = """
        CREATE TABLE IF NOT EXISTS asociado (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            documento        TEXT NOT NULL UNIQUE,
            nombres          TEXT NOT NULL,
            apellidos        TEXT NOT NULL,
            telefono         TEXT,
            fecha_registro   TEXT NOT NULL
        )""";

    private static final String SQL_PRESTAMO = """
        CREATE TABLE IF NOT EXISTS prestamo (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            asociado_id      INTEGER NOT NULL,
            monto            INTEGER NOT NULL CHECK (monto > 0),
            fecha            TEXT NOT NULL,
            plazo            TEXT NOT NULL,
            numero_cuotas    INTEGER NOT NULL CHECK (numero_cuotas > 0),
            valor_cuota      INTEGER NOT NULL CHECK (valor_cuota > 0),
            estado           TEXT NOT NULL CHECK (estado IN ('Vigente', 'Finalizado')),
            FOREIGN KEY (asociado_id) REFERENCES asociado(id)
        )""";

    private static final String SQL_CUOTA = """
        CREATE TABLE IF NOT EXISTS cuota (
            id                  INTEGER PRIMARY KEY AUTOINCREMENT,
            prestamo_id         INTEGER NOT NULL,
            numero_cuota        INTEGER NOT NULL,
            valor               INTEGER NOT NULL CHECK (valor > 0),
            fecha_vencimiento   TEXT NOT NULL,
            FOREIGN KEY (prestamo_id) REFERENCES prestamo(id),
            UNIQUE (prestamo_id, numero_cuota)
        )""";

    private static final String SQL_PAGO = """
        CREATE TABLE IF NOT EXISTS pago (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            cuota_id         INTEGER NOT NULL,
            fecha_pago       TEXT NOT NULL,
            valor            INTEGER NOT NULL CHECK (valor > 0),
            numero_recibo    TEXT NOT NULL,
            FOREIGN KEY (cuota_id) REFERENCES cuota(id)
        )""";

    private static final String SQL_IDX_PRESTAMO_ASOCIADO =
        "CREATE INDEX IF NOT EXISTS idx_prestamo_asociado ON prestamo(asociado_id)";
    private static final String SQL_IDX_CUOTA_PRESTAMO =
        "CREATE INDEX IF NOT EXISTS idx_cuota_prestamo ON cuota(prestamo_id)";
    private static final String SQL_IDX_PAGO_CUOTA =
        "CREATE INDEX IF NOT EXISTS idx_pago_cuota ON pago(cuota_id)";

    private static final String SQL_TRG_ASOCIADO_SIN_BORRADO = """
        CREATE TRIGGER IF NOT EXISTS trg_asociado_sin_borrado
        BEFORE DELETE ON asociado
        BEGIN
            SELECT RAISE(ABORT, 'No se permite eliminar registros.');
        END;""";

    private static final String SQL_TRG_PRESTAMO_SIN_BORRADO = """
        CREATE TRIGGER IF NOT EXISTS trg_prestamo_sin_borrado
        BEFORE DELETE ON prestamo
        BEGIN
            SELECT RAISE(ABORT, 'No se permite eliminar registros.');
        END;""";

    private static final String SQL_TRG_CUOTA_SIN_BORRADO = """
        CREATE TRIGGER IF NOT EXISTS trg_cuota_sin_borrado
        BEFORE DELETE ON cuota
        BEGIN
            SELECT RAISE(ABORT, 'No se permite eliminar registros.');
        END;""";

    private static final String SQL_TRG_PAGO_SIN_BORRADO = """
        CREATE TRIGGER IF NOT EXISTS trg_pago_sin_borrado
        BEFORE DELETE ON pago
        BEGIN
            SELECT RAISE(ABORT, 'No se permite eliminar registros.');
        END;""";

    private static final String SQL_TRG_USUARIO_SIN_BORRADO = """
        CREATE TRIGGER IF NOT EXISTS trg_usuario_sin_borrado
        BEFORE DELETE ON usuario
        BEGIN
            SELECT RAISE(ABORT, 'No se permite eliminar registros.');
        END;""";

    private static final String SQL_TRG_CUOTA_CON_PAGOS_SIN_EDICION = """
        CREATE TRIGGER IF NOT EXISTS trg_cuota_con_pagos_sin_edicion
        BEFORE UPDATE ON cuota
        WHEN EXISTS (SELECT 1 FROM pago WHERE cuota_id = OLD.id)
        BEGIN
            SELECT RAISE(ABORT, 'Esta cuota tiene pagos registrados: no se puede modificar su número, valor ni fecha de vencimiento.');
        END;""";

    public static void inicializar() throws SQLException {
        inicializar(ConexionBD.rutaBaseDatos());
    }

    public static void inicializar(Path ruta) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion(ruta);
             Statement st = cn.createStatement()) {
            st.execute(SQL_USUARIO);
            st.execute(SQL_ASOCIADO);
            st.execute(SQL_PRESTAMO);
            st.execute(SQL_CUOTA);
            st.execute(SQL_PAGO);
            st.execute(SQL_IDX_PRESTAMO_ASOCIADO);
            st.execute(SQL_IDX_CUOTA_PRESTAMO);
            st.execute(SQL_IDX_PAGO_CUOTA);
            st.execute(SQL_TRG_ASOCIADO_SIN_BORRADO);
            st.execute(SQL_TRG_PRESTAMO_SIN_BORRADO);
            st.execute(SQL_TRG_CUOTA_SIN_BORRADO);
            st.execute(SQL_TRG_PAGO_SIN_BORRADO);
            st.execute(SQL_TRG_USUARIO_SIN_BORRADO);
            st.execute(SQL_TRG_CUOTA_CON_PAGOS_SIN_EDICION);
        }
    }
}
