-- Datos de demostración de la Cooperativa Las Ferias.
--
-- IMPORTANTE:
--   * Todos los datos son ficticios.
--   * Se cargan a mano; la aplicación NUNCA ejecuta este script al arrancar.
--   * Debe ejecutarse sobre una base SIN datos: el script fija los identificadores
--     (1, 2, 3...) y una segunda ejecución fallará por clave duplicada; es esperado.
--
-- La base debe tener ya el esquema creado por la aplicación. Ejemplo:
--   sqlite3 "%USERPROFILE%\AppData\Roaming\CooperativaLasFerias\cooperativa.db" ".read demo/demo.sql"

PRAGMA foreign_keys = ON;
BEGIN TRANSACTION;

-- 5 asociados
INSERT INTO asociado (id, documento, nombres, apellidos, telefono, fecha_registro) VALUES
    (1, '10000001', 'Ana María',    'Gómez Rodríguez',  '3001234567', '2026-01-05'),
    (2, '10000002', 'Carlos Andrés', 'Martínez López',   '3102345678', '2026-01-07'),
    (3, '10000003', 'Laura Sofía',   'Ramírez Torres',   '3203456789', '2026-01-10'),
    (4, '10000004', 'José David',    'Herrera Castro',   '3014567890', '2026-01-12'),
    (5, '10000005', 'Diana Carolina','Peña Moreno',       NULL,        '2026-01-15');

-- 6 préstamos: 4 Vigentes y 2 Finalizados
INSERT INTO prestamo (id, asociado_id, monto, fecha, plazo, numero_cuotas, valor_cuota, estado) VALUES
    (1, 1, 1200000, '2026-02-01', '6 meses', 3, 400000, 'Vigente'),
    (2, 2, 1050000, '2026-02-10', '6 meses', 3, 350000, 'Vigente'),
    (3, 3, 1000000, '2026-02-15', '4 meses', 2, 500000, 'Vigente'),
    (4, 1,  900000, '2026-03-01', '6 meses', 3, 300000, 'Finalizado'),
    (5, 4,  900000, '2026-03-05', '4 meses', 2, 450000, 'Vigente'),
    (6, 5, 1200000, '2026-01-20', '4 meses', 2, 600000, 'Finalizado');

-- 15 cuotas, con vencimientos posteriores a la fecha de cada préstamo
INSERT INTO cuota (id, prestamo_id, numero_cuota, valor, fecha_vencimiento) VALUES
    (1,  1, 1, 400000, '2026-03-01'),
    (2,  1, 2, 400000, '2026-04-01'),
    (3,  1, 3, 400000, '2026-05-01'),
    (4,  2, 1, 350000, '2026-03-10'),
    (5,  2, 2, 350000, '2026-04-10'),
    (6,  2, 3, 350000, '2026-05-10'),
    (7,  3, 1, 500000, '2026-03-15'),
    (8,  3, 2, 500000, '2026-04-15'),
    (9,  4, 1, 300000, '2026-04-01'),
    (10, 4, 2, 300000, '2026-05-01'),
    (11, 4, 3, 300000, '2026-06-01'),
    (12, 5, 1, 450000, '2026-04-05'),
    (13, 5, 2, 450000, '2026-05-05'),
    (14, 6, 1, 600000, '2026-02-20'),
    (15, 6, 2, 600000, '2026-03-20');

-- 13 pagos. Incluye:
--   * dos abonos parciales en la cuota 5 (100.000 y 30.000 de 350.000);
--   * el recibo REC-2026-001 repetido en las cuotas 1 y 4 (P4);
--   * pagos en los dos préstamos Finalizados.
INSERT INTO pago (id, cuota_id, fecha_pago, valor, numero_recibo) VALUES
    (1,  1,  '2026-03-02', 400000, 'REC-2026-001'),
    (2,  2,  '2026-04-02', 400000, 'REC-2026-002'),
    (3,  3,  '2026-05-02', 400000, 'REC-2026-003'),
    (4,  4,  '2026-03-11', 350000, 'REC-2026-001'),
    (5,  5,  '2026-04-12', 100000, 'REC-2026-004'),
    (6,  9,  '2026-04-02', 300000, 'REC-2026-005'),
    (7,  10, '2026-05-02', 300000, 'REC-2026-006'),
    (8,  11, '2026-06-02', 300000, 'REC-2026-007'),
    (9,  12, '2026-04-06', 450000, 'REC-2026-008'),
    (10, 13, '2026-05-06', 450000, 'REC-2026-009'),
    (11, 14, '2026-02-21', 600000, 'REC-2026-010'),
    (12, 15, '2026-03-21', 600000, 'REC-2026-011'),
    (13, 5,  '2026-04-20',  30000, 'REC-2026-013');

COMMIT;
