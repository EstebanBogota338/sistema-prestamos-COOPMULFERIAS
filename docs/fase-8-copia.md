# Fase 8 — Copia de seguridad desde la interfaz: plan e implementación

> **Estado:** Fase 8 **terminada**: backend, botón y validación manual de copia **y recuperación**.
> Commits `51c4d45` (DAO), `60389b0` (servicio) y `f397f2a` (UI).
> **Base:** AGENTS.md §12.8; docs/MVP-Cooperativa-Las-Ferias-v3.md §3.1, §8 «Acceso y persistencia» y §9.11.
> **Rama:** `fase-8`.

## 0. Estado de implementación

- **Hecho:** `CopiaSeguridadDAO` (`VACUUM INTO ?`), `CopiaSeguridadService` y el botón en la barra superior.
- **Verificado:** `mvn clean test` → **139 pruebas, 0 fallos, BUILD SUCCESS**.
- **Validado por el tester:** diez comprobaciones en PASA sobre una BD aislada sembrada por script. Se creó la copia, se comprobó con `integrity_check = ok`, se probó la confirmación de sobrescritura (NO deja el archivo intacto por `LastWriteTime`, SÍ lo reescribe), el rechazo de copiar sobre la BD en uso, y —lo central— la **recuperación**: restaurada una copia anterior, desaparecieron los datos posteriores y la aplicación siguió funcionando. La BD real conservó su SHA-256.
- **Pendiente de pulir:** el diálogo de sobrescritura aparece antes que el aviso de «no se puede sobrescribir la base de datos en uso», y el selector no añade `.db` automáticamente.

## 1. Objetivo

Permitir una copia consistente de la base con la aplicación abierta y documentar su recuperación manual:

- Botón **«Copia de seguridad»** en la barra superior.
- Selección del archivo destino con `JFileChooser`.
- Confirmación antes de sobrescribir.
- Copia con `VACUUM INTO ?`.
- Recuperación manual con la aplicación cerrada.

## 2. Fuera de alcance

No se implementa en esta fase:

- Restaurar desde la interfaz.
- Comprimir o empaquetar la copia.
- Copias automáticas o programadas.
- Historial de copias.
- Exportar a CSV.

## 3. Decisiones aplicadas

- Botón **«Copia de seguridad»** en la barra superior, junto al avatar; sin barra de menú nueva.
- `JFileChooser` con nombre sugerido `cooperativa-AAAA-MM-DD.db`, filtro `.db` y carpeta Documentos.
- Copia con **`VACUUM INTO ?`** (parámetro, no ruta concatenada): es consistente aunque haya datos recién guardados. Verificado que sqlite-jdbc 3.49.1.0 acepta el marcador en esa sentencia.
- Si el destino existe: confirmación; si se acepta, se borra y se regenera; si se rechaza, se vuelve al selector.
- No se puede copiar sobre la base en uso (comparación de rutas absolutas normalizadas).
- La recuperación es **manual y con la aplicación cerrada**; no hay restauración desde la UI.

## 4. Archivos

| Archivo | Acción |
| --- | --- |
| `dao/CopiaSeguridadDAO.java` | Crear |
| `service/CopiaSeguridadService.java` | Crear |
| `ui/VentanaPrincipal.java` | Modificar (botón y flujo del selector) |
| `test/dao/CopiaSeguridadDAOTest.java` | Crear |
| `test/service/CopiaSeguridadServiceTest.java` | Crear |

## 5. Puertas

| Puerta | Alcance | Pruebas acumuladas |
| --- | --- | --- |
| G1 | DAO | 134 |
| G2 | Servicio | 139 |
| G3 | UI, sin pruebas nuevas | 139 |
| G4 | Documentación | 139 |

## 6. Reglas y mensajes

| Caso | Mensaje |
| --- | --- |
| Destino nulo | `Seleccione un archivo de destino.` |
| Destino igual a la BD en uso | `No se puede sobrescribir la base de datos en uso.` |
| Destino existente sin confirmación | `El archivo de destino ya existe.` |

## 7. Pruebas

Se agregaron **7 pruebas**:

| Clase | Pruebas |
| --- | --- |
| `CopiaSeguridadDAOTest` | 2 |
| `CopiaSeguridadServiceTest` | 5 |
| **Total nuevo** | **7** |

La suite queda en **139 pruebas** (49 + 29 + 21 + 21 + 5 + 7 + 7), con **0 fallos**.

## 8. Criterio de aceptación

- v3 §8: «La copia de seguridad puede recuperarse mediante el procedimiento documentado».
- v3 §9.11: crear una copia y comprobar su recuperación.

## 9. Notas de la validación manual

- **La copia es una foto, no una vista de la base viva.** Se comprobó creando una copia, añadiendo un tercer asociado y restaurando la copia: el tercer asociado desapareció.
- La copia resultante abre correctamente y `PRAGMA integrity_check` devuelve `ok`.
- Al elegir como destino la propia base, **el diálogo de sobrescritura aparece antes** que el aviso de «no se puede sobrescribir la base de datos en uso». Es cosmético y está pendiente de pulir: la comprobación debería ir antes de preguntar.
- El selector **no añade la extensión `.db`** si el usuario omite escribirla; el archivo sigue siendo una base SQLite válida.
- La aplicación no añade la extensión ni valida el nombre: se guarda lo que el usuario escriba.
