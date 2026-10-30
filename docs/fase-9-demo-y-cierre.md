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

## Pulido de legibilidad posterior al cierre

Seis commits posteriores al cierre del MVP:

- `e02599c`: tono propio para los textos de ayuda de los formularios.
- `75f3213`: color de aviso para el bloqueo de cuota.
- `e4c7a33`: separador de miles en la presentación de montos y `EstiloTest`.
- `804f123`: ancho mínimo de ventana de 1000 px para que quepan las cinco pestañas.
- `d38960f`: textos de ayuda a 12 px y acortamiento del texto más largo.
- `8ad1fe8`: formato de moneda en las etiquetas que concatenan el monto del préstamo.

**Corrección de un dato:** el mínimo de la ventana ya no es 900×600 sino **1000×600**. La barra superior necesita unos 952 px desde que la Fase 8 le añadió el botón «Copia de seguridad»; a 900 la pestaña Historial se quedaba fuera. La comprobación de la Fase 7 («cinco pestañas en una línea a 900×600») quedó desmentida por este hallazgo.

**El fallo que encontró el tester:** la columna «Préstamo» del Historial —y dos etiquetas más— seguía mostrando el monto crudo. Lo cazó mirando la pantalla, no la suite. Se arregló con `Estilo.formatearMoneda` en los tres sitios y con una aserción en `PanelHistorialTest` que ahora fija el formato.

El pre-relleno de los formularios en edición usa el mismo formato y el valor **no cambia** al guardar sin tocar el campo, porque `parsearMonto` acepta los puntos y `FormularioPrestamoTest` ya cubría ese análisis.
