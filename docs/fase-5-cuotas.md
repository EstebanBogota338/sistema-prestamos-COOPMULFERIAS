# Fase 5 — Cuotas: plan e implementación

> **Estado:** Fase 5 **terminada**: backend, UI y validación manual del recorrido completo.
> Commits `7a845ce` (modelo y DAO), `ed77dfb` (servicio) y `89e634b` (UI).
> **Base:** AGENTS.md §12.5; docs/MVP-Cooperativa-Las-Ferias-v3.md §4.3, §4.5, §4.6, §5.3 y §8.
> **Rama:** `fase-5`.

## 0. Estado de implementación

- **Hecho:** `Cuota`, `CuotaDAO`, `CuotaService`, `PanelCuotas`, `FormularioCuota` y la pestaña en `VentanaPrincipal`.
- **Verificado:** `mvn clean test` → **99 pruebas, 0 fallos, BUILD SUCCESS**.
- **Validado manualmente:** recorrido del tester sobre una BD aislada (`-Duser.home` a un temporal); la BD real conservó su SHA-256. Trece comprobaciones en PASA: arranque, login, tres pestañas, alta de cuota con `1.500.000` → `1500000`, unicidad por préstamo, nueve mensajes de validación, edición sin pagos, edición bloqueada con pagos, persistencia y huella de la BD real.
- **Pendiente de pulir:** nada nuevo; rigen las limitaciones ya anotadas en AGENTS §12.

## 1. Objetivo

Incorporar el módulo de **Cuotas** y su navegación, manteniendo el resto del MVP sin cambios:

- Registrar, consultar y editar cuotas vinculadas a préstamos existentes.
- Mantener el vínculo `cuota.prestamo_id` fijo desde la creación.
- Añadir la pestaña **Cuotas** en `VentanaPrincipal`.
- Validar la unicidad de `(prestamo_id, numero_cuota)` y el bloqueo de edición cuando la cuota tiene pagos.
- Mantener la suite anterior en verde y agregar las pruebas del módulo.

## 2. Fuera de alcance

No se implementa en esta fase:

- Triggers de SQLite (P1).
- Validaciones cruzadas cuota ↔ préstamo (P3): no se compara `numero_cuota` con `prestamo.numero_cuotas` ni `valor` con `valor_cuota`.
- Cambios de DDL, columnas, `CHECK` o transacciones.
- Búsqueda o filtros de cuotas.
- Borrado de cuotas.
- Pantallas de Pagos.
- Cálculos financieros, totales, saldos o generación automática de cuotas.

## 3. Decisiones aplicadas

- `numero_cuota >= 1` se valida solo en el servicio; el documento v3 no lo pide y la tabla ya existe sin `CHECK`.
- Un préstamo `Finalizado` puede recibir cuotas, porque no hay una regla documentada que lo impida.
- La unicidad `(prestamo_id, numero_cuota)` se traduce con `getResultCode() == SQLITE_CONSTRAINT_UNIQUE`, nunca comparando el texto del mensaje.
- El bloqueo de edición con pagos tiene dos capas: comprobación en el servicio para el mensaje y `UPDATE ... WHERE id = ? AND NOT EXISTS (SELECT 1 FROM pago WHERE cuota_id = ?)` en el DAO para la atomicidad.
- Sin `database/ErroresBD`: un solo llamador no justifica la clase y `getErrorCode()` devuelve 19 tanto para `UNIQUE` como para `FOREIGN KEY`.
- `cuota.prestamo_id` se fija al crear; `actualizar` no escribe esa columna y el servicio rechaza el cambio.

## 4. Archivos

| Archivo | Acción |
| --- | --- |
| `model/Cuota.java` | Crear |
| `dao/CuotaDAO.java` | Crear |
| `service/CuotaService.java` | Crear |
| `ui/PanelCuotas.java` | Crear |
| `ui/FormularioCuota.java` | Crear |
| `ui/VentanaPrincipal.java` | Modificar (pestaña Cuotas y refresco) |
| `test/dao/CuotaDAOTest.java` | Crear |
| `test/service/CuotaServiceTest.java` | Crear |
| `test/ui/FormularioCuotaTest.java` | Crear |
| `test/ui/PanelCuotasTest.java` | Crear |

## 5. Puertas

| Puerta | Alcance | Pruebas acumuladas |
| --- | --- | --- |
| G1 | Modelo, DAO y pruebas del DAO | 84 |
| G2 | Servicio y pruebas de reglas | 93 |
| G3 | UI y pruebas de formulario/panel | 99 |
| G4 | Documentación de cierre | 99 |

## 6. Reglas de validación

| Mensaje exacto | Cuándo se produce |
| --- | --- |
| `Los datos de la cuota son obligatorios.` | La cuota es `null`. |
| `Seleccione una cuota válida para editar.` | Se intenta actualizar con `id <= 0`. |
| `Seleccione un préstamo válido.` | El préstamo no existe o su id es `<= 0`. |
| `El número de cuota debe ser mayor o igual a 1.` | `numeroCuota < 1`. |
| `El valor de la cuota debe ser mayor que cero.` | `valor <= 0`. |
| `La fecha de vencimiento es obligatoria.` | `fechaVencimiento == null`. |
| `Ya existe una cuota con ese número en este préstamo.` | Duplicado de `(prestamo_id, numero_cuota)` en alta o edición. |
| `Esta cuota tiene pagos registrados: no se puede modificar su número, valor ni fecha de vencimiento.` | La cuota ya tiene al menos un pago. |
| `No se puede cambiar el préstamo de la cuota.` | El `prestamoId` recibido difiere del persistido. |
| `No se encontró la cuota que se desea actualizar.` | El id no existe o el `UPDATE` afecta 0 filas. |

## 7. Pruebas

Se agregaron **21 pruebas**:

| Clase | Pruebas |
| --- | --- |
| `CuotaDAOTest` | 6 |
| `CuotaServiceTest` | 9 |
| `FormularioCuotaTest` | 5 |
| `PanelCuotasTest` | 1 |
| **Total nuevo** | **21** |

La suite queda en **99 pruebas** (49 de las fases 1–3 + 29 de la Fase 4 + 21 de la Fase 5), con **0 fallos**.

## 8. Criterios de aceptación

- Las cuotas pertenecen a préstamos existentes.
- Sin pagos, se pueden editar número, valor y fecha de vencimiento.
- Con pagos, no se pueden modificar número, valor ni fecha de vencimiento.
- Una cuota bloqueada se puede seguir consultando.
- No hay cálculos financieros ni borrado.
- Las cuotas persisten tras reiniciar la aplicación.

## 9. Notas de la validación manual

- El rechazo de cambio de préstamo **no es reproducible desde la interfaz**: el vínculo se muestra como texto fijo y el servicio no lo escribe. Solo lo cubre `CuotaServiceTest.rechazaCambioDePrestamoAlEditar`.
- El mensaje `"Seleccione una cuota de la tabla para editarla."` es defensivo: el botón **Editar** está deshabilitado sin selección (`PanelCuotas.java:87`, `:119`, `:162`), igual que en `PanelPrestamos`.
- La carrera entre la comprobación de pagos y el `UPDATE` condicionado no se verifica manualmente; la protege el `UPDATE` (una sola sentencia atómica) y su traducción el `catch` del servicio.
