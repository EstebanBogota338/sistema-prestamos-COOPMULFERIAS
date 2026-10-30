# Fase 9 — datos de demostración y cierre

> **Estado:** MVP **terminado y validado de punta a punta**.
> Commits `2e96695` (demo y cierre), `580150c` (metadatos) y el de este cierre.
> **Base:** AGENTS.md §12.9; docs/MVP-Cooperativa-Las-Ferias-v3.md §8 y §9.
> **Rama:** `cierre`.

## 0. Estado final

- **Código:** cinco módulos (Asociados, Préstamos, Cuotas, Pagos, Historial), copia de seguridad y seis *triggers*. **139 pruebas, 0 fallos**.
- **Validación final:** recorrido completo por el tester sobre una BD aislada con `demo/demo.sql` cargado; **trece comprobaciones en PASA**, incluida la restauración de una copia. La BD real conservó su SHA-256.
- **Hallazgos de esa validación:** dos frases del README sobre la recarga de la demo (corregidas aquí) y la ausencia de una cuota con dos pagos en la demo (corregida aquí).
- **Fuera del alcance, por decisión:** cambio de contraseña y pantalla Home con tarjetas.

## 1. Criterios de aceptación

| Grupo | Evidencia |
| --- | --- |
| Asociados | Alta, búsqueda, unicidad de documento y persistencia |
| Préstamos | Registro vinculado a un asociado, valor de cuota manual, edición sin cambiar el asociado |
| Cuotas y pagos | Cuota ligada a préstamo, pagos con abono parcial, cuota con pagos bloqueada, pago editable |
| Historial | Consulta por asociado sin duplicar registros y con el estado actual de cada pago |
| Acceso y persistencia | Login, credenciales rechazadas, contraseña con BCrypt, datos tras reiniciar |
| Copia de seguridad | Creación desde la interfaz y recuperación siguiendo el procedimiento del README |

## 2. Cómo se demuestra

La demostración sigue la sección **«Guion de demostración»** del README y usa `demo/demo.sql` cargado a mano. El guion se siguió tal como está escrito y fue ejecutable.

## 3. Limitaciones

La lista canónica es **«Limitaciones conocidas»** del README. No se repite aquí.

## 4. Qué queda fuera

- Cambio de contraseña (documentado como limitación).
- Pantalla Home con tarjetas (recorte deliberado).
- P3 y P4 (decisiones de no imponer validaciones).
- Mejoras de pulido ya anotadas: foco tras guardado fallido, combo de cuota truncado y altura del Historial.
