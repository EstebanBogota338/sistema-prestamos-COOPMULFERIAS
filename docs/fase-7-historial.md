# Fase 7 — Historial por asociado: plan e implementación

> **Estado:** Fase 7 **terminada**: backend, UI y validación manual de la pestaña.
> Commits `fc9199b` (DAO), `40e9dcc` (servicio) y `cdfd41f` (UI).
> **Base:** AGENTS.md §12.7; docs/MVP-Cooperativa-Las-Ferias-v3.md §3.1, §5.6 y §8 «Historial».
> **Rama:** `fase-7`.

## 0. Estado de implementación

- **Hecho:** `HistorialDAO`, `HistorialService`, `PanelHistorial` y la quinta pestaña en `VentanaPrincipal`. Sin modelo nuevo: reutiliza `Prestamo`, `Cuota`, `Pago` y `Asociado`.
- **Verificado:** `mvn clean test` → **132 pruebas, 0 fallos, BUILD SUCCESS**.
- **Validado por el tester:** once comprobaciones en PASA sobre una BD aislada **sembrada por script** (dos asociados con historiales de distinto tamaño) y **acotada a la pestaña Historial**; los cuatro módulos anteriores ya estaban validados en sus fases. La BD real conservó su SHA-256.
- **Pendiente de pulir:** el reparto de altura entre las tres tablas (§9).

## 1. Objetivo

Incorporar una pestaña de **consulta del historial por asociado**, manteniendo el resto del MVP sin cambios:

- Elegir un asociado y ver sus préstamos, cuotas y pagos.
- Presentar cada fila una sola vez, sin duplicar cuotas por sus pagos.
- Reutilizar los modelos y servicios existentes.
- No añadir edición, exportación, filtros, totales ni cálculos.

## 2. Fuera de alcance

No se implementa en esta fase:

- Edición desde el historial.
- Exportar a CSV, PDF u otro formato.
- Filtros por fecha u otros criterios.
- Totales, saldos, pendientes, pagos agregados o comparaciones entre pagos y cuotas.
- Borrado.
- Modelo nuevo.
- Cambios de DDL.
- Modificar `PanelPrestamos`, `PanelCuotas` o `PanelPagos`.

## 3. Decisiones aplicadas

- **Forma A:** tres tablas encadenadas (Préstamos → Cuotas → Pagos) y un combo de asociado, en una pestaña nueva. Cada fila aparece una sola vez.
- **Por qué no un `JOIN` único de cuatro tablas:** repetiría cada cuota por cada pago, que es exactamente lo que el documento prohíbe con «sin duplicar registros».
- **Tres consultas:** `listarPrestamos` (filtro directo por `asociado_id`), `listarCuotas` (`cuota ⋈ prestamo`) y `listarPagos` (`pago ⋈ cuota ⋈ prestamo`), todas filtrando por `asociado_id`.
- **Solo consulta:** sin alta, sin edición y sin borrado.
- `HistorialService` solo valida `asociadoId > 0` («Seleccione un asociado válido.»); no comprueba que el asociado exista, porque desde la interfaz solo se pueden elegir asociados existentes y un asociado sin datos devuelve listas vacías.

## 4. Archivos

| Archivo | Acción |
| --- | --- |
| `dao/HistorialDAO.java` | Crear |
| `service/HistorialService.java` | Crear |
| `ui/PanelHistorial.java` | Crear |
| `ui/VentanaPrincipal.java` | Modificar (quinta pestaña y refresco) |
| `test/dao/HistorialDAOTest.java` | Crear |
| `test/service/HistorialServiceTest.java` | Crear |
| `test/ui/PanelHistorialTest.java` | Crear |

## 5. Puertas

| Puerta | Alcance | Pruebas acumuladas |
| --- | --- | --- |
| G1 | DAO y servicio | 131 |
| G2 | UI | 132 |
| G3 | Documentación | 132 |

## 6. Reglas de validación

Una sola validación en el servicio:

| Regla | Capa | Mensaje |
| --- | --- | --- |
| `asociadoId <= 0` | Servicio | `Seleccione un asociado válido.` |

El resto de la pantalla son estados, no validaciones: `"Seleccione un asociado para ver su historial."` cuando no hay selección.

## 7. Pruebas

Se agregaron **7 pruebas**:

| Clase | Pruebas |
| --- | --- |
| `HistorialDAOTest` | 4 |
| `HistorialServiceTest` | 2 |
| `PanelHistorialTest` | 1 |
| **Total nuevo** | **7** |

La suite queda en **132 pruebas** (49 + 29 + 21 + 21 + 5 + 7), con **0 fallos**.

## 8. Criterios de aceptación

- Se consulta la información de un asociado.
- Los préstamos, cuotas y pagos se presentan sin duplicar registros.
- La información es la almacenada, reflejando el estado actual de cada pago.
- No hay cálculos financieros.

## 9. Notas de la validación manual

- **La comprobación central es que una cuota con dos pagos se lista una sola vez.** Se consigue porque `listarCuotas` une `cuota` con `prestamo` y **nunca** con `pago`; si se uniera, la cuota aparecería dos veces. En la prueba se comprueba con dos pagos sobre una cuota y se exige una fila; en la pantalla, con `REC-001` y `REC-002` sobre la cuota 1 y la tabla mostrando solo las cuotas 1 y 2.
- **El combo ordena por apellidos**, no por documento: es el `Collator` es-CO que ya ordena todas las listas de asociados de la aplicación. No es un defecto.
- **Las tres tablas reparten la altura a partes iguales**, así que a 1000×680 la tabla de pagos muestra unas dos filas y media y hay que desplazarse con tres pagos. Funciona, pero se anota como cosmético.
- La validación se hizo sobre una BD sembrada por script y **sin repetir la regresión** de los cuatro módulos anteriores, ya validados en sus fases.
