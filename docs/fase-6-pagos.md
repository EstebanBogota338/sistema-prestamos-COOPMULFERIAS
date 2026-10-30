# Fase 6 — Pagos: plan e implementación

> **Estado:** Fase 6 **terminada**: backend, UI y validación manual del recorrido completo.
> Commits `a6dd9cc` (modelo y DAO), `55dcc8e` (servicio) y `8700305` (UI).
> **Base:** AGENTS.md §12.6; docs/MVP-Cooperativa-Las-Ferias-v3.md §4.4, §4.5, §4.6, §5.4 y §8.
> **Rama:** `fase-6`.

## 0. Estado de implementación

- **Hecho:** `Pago`, `PagoDAO`, `PagoService`, `PanelPagos`, `FormularioPago` y la pestaña en `VentanaPrincipal`.
- **Verificado:** `mvn clean test` → **120 pruebas, 0 fallos, BUILD SUCCESS**.
- **Validado manualmente por el tester:** recorrido sobre una BD aislada (`-Duser.home` a un temporal); la BD real conservó su SHA-256. Catorce comprobaciones en PASA: arranque, login, cuatro pestañas, aviso con cero cuotas, alta con abono parcial, recibo con `trim`, validaciones, edición conservando la cuota, bloqueo de la cuota en Cuotas, pago aún editable, mismo recibo en dos pagos, persistencia y huella de la BD real.
- **Pendiente de pulir:** solo lo cosmético anotado en §9.

## 1. Objetivo

Incorporar el módulo de **Pagos** y su navegación, manteniendo el resto del MVP sin cambios:

- Registrar, consultar y editar pagos vinculados a cuotas existentes.
- Mantener el vínculo `pago.cuota_id` fijo desde la creación.
- Añadir la pestaña **Pagos** en `VentanaPrincipal`.
- Permitir abonos parciales y edición directa del registro, sin historial de versiones.
- Mantener la suite anterior en verde y agregar las pruebas del módulo.

## 2. Fuera de alcance

No se implementa en esta fase:

- Triggers de SQLite (P1).
- Validaciones cruzadas adicionales (P3) ni cambios en la decisión P4 sobre `numero_recibo`.
- Cambios de DDL, columnas, `CHECK` o transacciones de otras operaciones.
- Búsqueda o filtros de pagos.
- Borrado o anulación de pagos.
- Historial de versiones o auditoría.
- Pantallas de Historial y copia de seguridad.
- Cálculos financieros, totales, saldos o agregaciones de importes.

## 3. Decisiones aplicadas

- `pago.cuota_id` se fija al crear: el `UPDATE` no escribe `cuota_id` y el servicio rechaza el cambio.
- La edición se ejecuta **en transacción** (`setAutoCommit(false)` + `commit`/`rollback`), con la comprobación de existencia y el `UPDATE` en la misma conexión; un `false` se traduce a `SQLException`.
- **Sobre la transacción:** la edición es un único `UPDATE`; la transacción se incluye porque v3 §5.7 la exige expresamente para la edición de pagos, y en ella va la lectura de existencia junto con la escritura. La coherencia del vínculo no depende de la transacción: el `UPDATE` nunca escribe `cuota_id`. No debe presentarse como "atomicidad del vínculo", porque no lo es.
- Abonos parciales permitidos: el valor no se compara con el de la cuota y no se calcula saldo.
- Sin `UNIQUE` de `numero_recibo` (decisión P4: un recibo puede cubrir varios pagos).
- Sin borrado ni anulación.
- `numero_recibo` obligatorio y normalizado con `trim`; el vacío se rechaza, no se convierte en nulo.
- El estado del préstamo no se comprueba: se puede pagar una cuota de un préstamo `Finalizado`.

## 4. Archivos

| Archivo | Acción |
| --- | --- |
| `model/Pago.java` | Crear |
| `dao/PagoDAO.java` | Crear |
| `service/PagoService.java` | Crear |
| `ui/PanelPagos.java` | Crear |
| `ui/FormularioPago.java` | Crear |
| `ui/VentanaPrincipal.java` | Modificar (pestaña Pagos y refresco) |
| `test/dao/PagoDAOTest.java` | Crear |
| `test/service/PagoServiceTest.java` | Crear |
| `test/ui/FormularioPagoTest.java` | Crear |
| `test/ui/PanelPagosTest.java` | Crear |

## 5. Puertas

| Puerta | Alcance | Pruebas acumuladas |
| --- | --- | --- |
| G1 | Modelo, DAO y pruebas del DAO | 104 |
| G2 | Servicio y pruebas de reglas | 114 |
| G3 | UI y pruebas de formulario/panel | 120 |
| G4 | Documentación de cierre | 120 |

## 6. Reglas de validación

| Regla | Capa | Mensaje |
| --- | --- | --- |
| Pago nulo | Servicio | `Los datos del pago son obligatorios.` |
| `id <= 0` al editar | Servicio | `Seleccione un pago válido para editar.` |
| Cuota sin elegir (alta) | Formulario | `Seleccione una cuota válida.` |
| Cuota inexistente o `id <= 0` | Servicio | `Seleccione una cuota válida.` |
| Fecha vacía o con formato inválido | Formulario | `La fecha es obligatoria.` / `Use el formato AAAA-MM-DD.` |
| Fecha nula | Servicio | `La fecha del pago es obligatoria.` |
| Valor vacío, no numérico o desbordado | Formulario | `El valor es obligatorio.` / `El valor no es válido. Use solo números (ej. 1500000 o 1.500.000).` / `El valor es demasiado grande.` |
| `valor <= 0` | Servicio | `El valor del pago debe ser mayor que cero.` |
| Recibo vacío tras `trim` | Servicio | `El número de recibo es obligatorio.` |
| Cambio de cuota | Servicio | `No se puede cambiar la cuota del pago.` |
| Pago inexistente o `UPDATE` sin efecto | Servicio | `No se encontró el pago que se desea actualizar.` |

## 7. Pruebas

Se agregaron **21 pruebas**:

| Clase | Pruebas |
| --- | --- |
| `PagoDAOTest` | 5 |
| `PagoServiceTest` | 10 |
| `FormularioPagoTest` | 5 |
| `PanelPagosTest` | 1 |
| **Total nuevo** | **21** |

La suite queda en **120 pruebas** (49 de las fases 1–3 + 29 de la Fase 4 + 21 de la Fase 5 + 21 de la Fase 6), con **0 fallos**.

## 8. Criterios de aceptación

- El pago pertenece a una cuota existente.
- Se admiten abonos parciales.
- Se rechazan importes nulos, cero o negativos.
- El número de recibo es obligatorio.
- Se puede editar un pago y el registro actualizado reemplaza al anterior sin conservar versiones.
- No se borra ni se anula.
- La cuota con pagos se consulta aunque su edición esté bloqueada.
- Los datos persisten tras reiniciar.

## 9. Notas de la validación manual

- **El bloqueo de cuotas se validó por interfaz por primera vez en esta fase.** Hasta la Fase 5 solo lo cubría `CuotaServiceTest` y una inserción manual por SQL. En este recorrido se registró un pago por la UI y se comprobó en Cuotas que la cuota queda bloqueada (aviso literal, tres campos y Guardar deshabilitados), que el pago sigue siendo editable y que Cancelar no altera la cuota.
- **Orden de validación del formulario:** `construirPago` valida cuota → fecha → valor → recibo, así que con varios campos vacíos sale primero el mensaje de la fecha. `FormularioPrestamo` y `FormularioCuota` hacen lo contrario (valor antes que fecha). Es una diferencia conocida de orden, no de comportamiento: en ambos casos se muestra un mensaje claro por vez.
- **El combo de cuota trunca la etiqueta cuando está cerrado** (`... - Cuo…`); el texto completo se lee en la lista desplegada y en la tabla. Cosmético y aceptado.
- **El combo exige dos gestos:** al abrirlo queda resaltado «Seleccione una cuota…», así que Enter solo confirma el marcador. Es comportamiento estándar del componente.
- **No verificado:** el `rollback` ante un fallo real de BD y la atomicidad de la transacción (haría falta inyección de fallos o dos procesos); y el mensaje «No se puede cambiar la cuota del pago.», irrealizable desde la UI porque en edición la cuota es texto fijo.
