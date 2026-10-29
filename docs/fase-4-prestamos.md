# Fase 4 — Préstamos: plan e implementación

> **Estado:** Fase 4 **terminada**: backend, UI y validación manual del recorrido completo. Commits `d4dd14a` (backend), `277390e` (UI), `33b86ac` (correcciones tras la prueba manual) y `71788f9` (cierre de documentación).
> **Base:** AGENTS.md §12.4; docs/MVP-Cooperativa-Las-Ferias-v3.md §4.2 y §5.2; P2 cerrada en la opción (a).
> **Rama:** `fase-4`, fusionada a `main` al cerrar la fase.

## 0. Estado de implementación

- **Hecho:** backend (`Prestamo`, `PrestamoDAO`, `PrestamoService`), UI (`VentanaPrincipal`, `PanelAsociados`, `PanelPrestamos`, `FormularioPrestamo`, `Estilo.campo`) y pruebas.
- **Verificado:** `mvn clean test` → **78 pruebas, 0 fallos, BUILD SUCCESS**, ejecutado antes y después de las correcciones.
- **Validado manualmente:** recorrido login → Principal → Asociados → Préstamos sobre una BD temporal; la BD real no se tocó (mismo SHA-256 antes y después). Se comprobaron montos (`1.5` rechazado; `1.500.000` guardado como `1500000`), fechas, conteos y navegación.
- **Corregido tras esa prueba (`33b86ac`):** avatar "AD" elidido a `...`, botones primarios con texto blanco sobre blanco, diálogo de asociado recortado por tamaño fijo, y concordancia "1 registro / 1 resultado".
- **Decisiones aplicadas:** los montos aceptan puntos y espacios como separadores de miles; se validan y luego se eliminan antes de parsear. La fecha inicia vacía con la pista `AAAA-MM-DD`.
- **Pendiente de pulir (no bloqueante):** el foco no viaja al campo con error tras un guardado fallido.

## 1. Objetivo

Incorporar el módulo de **Préstamos** y la navegación principal, manteniendo el resto del MVP sin cambios:

- Registrar, consultar y editar préstamos.
- Vincular cada préstamo a un asociado existente.
- Introducir `VentanaPrincipal` con barra superior y navegación por paneles.
- Convertir `VentanaAsociados` en `PanelAsociados`.
- Mantener las 49 pruebas actuales en verde y agregar las pruebas del módulo.

## 2. Fuera de alcance

No se implementará en esta fase:

- Cuotas, pagos, historial, copia de seguridad ni datos de demostración.
- Cálculos de intereses, amortización, saldos o valor de cuota.
- Eliminación de préstamos.
- Cambio de asociado en un préstamo ya creado (P2 a).
- Triggers de SQLite (P1), validaciones cruzadas cuota↔préstamo (P3) ni unicidad de `numero_recibo` (P4).
- Cambios de esquema, columnas o DDL.

## 3. Decisiones aplicables

- **P2 (a):** `prestamo.asociado_id` se fija al crear. En edición se muestra el asociado como solo lectura y el servicio rechaza cualquier intento de cambiarlo.
- **Sin cálculos financieros:** `monto`, `numero_cuotas` y `valor_cuota` se guardan tal como se ingresan. No se deriva el valor de la cuota ni se valida contra el monto.
- **Sin borrado:** el DAO no tendrá métodos de eliminación y la UI no ofrecerá esa acción.
- **Sin cambios de esquema:** la tabla `prestamo` ya existe con las columnas y restricciones necesarias.
- **Fechas:** `LocalDate` en el modelo, `TEXT` ISO `YYYY-MM-DD` en SQLite.
- **Montos:** pesos colombianos enteros (`long`/`INTEGER`), sin centavos. La entrada acepta puntos y espacios como separadores de miles; se valida el formato y luego se eliminan antes de `Long.parseLong`.
- **Fecha:** campo vacío con la pista `AAAA-MM-DD`; no se precarga la fecha actual.
- **Plazo:** texto libre, obligatorio, sin asumir unidad.
- **Estado:** `Vigente` o `Finalizado`, seleccionado manualmente.

## 4. Archivos previstos

| Archivo | Acción | Responsabilidad |
| --- | --- | --- |
| `model/Prestamo.java` | Crear | Datos del préstamo y constantes de estado |
| `dao/PrestamoDAO.java` | Crear | JDBC parametrizado con `Path`; crear, actualizar, buscar y listar |
| `service/PrestamoService.java` | Crear | Validaciones, existencia del asociado y protección del vínculo |
| `ui/VentanaPrincipal.java` | Crear | Barra superior, navegación Asociados/Préstamos y `CardLayout` |
| `ui/PanelAsociados.java` | Renombrar desde `VentanaAsociados.java` | Contenido actual de Asociados, sin barra superior ni `JFrame` |
| `ui/PanelPrestamos.java` | Crear | Tabla de préstamos y acciones Nuevo/Editar/Actualizar |
| `ui/FormularioPrestamo.java` | Crear | Diálogo modal de alta y edición |
| `ui/Estilo.java` | Modificar | Agregar `campo(JTextField)`; `campoPassword` delega en él |
| `ui/VentanaLogin.java` | Modificar | Abrir `VentanaPrincipal` en vez de `VentanaAsociados` |
| `test/.../dao/PrestamoDAOTest.java` | Crear | Pruebas del DAO |
| `test/.../service/PrestamoServiceTest.java` | Crear | Pruebas de reglas del servicio |
| `test/.../ui/PanelAsociadosTest.java` | Renombrada/adaptada | Apunta a `PanelAsociados`; conserva las aserciones de búsqueda y conteo |

La conversión implica renombrar el archivo y la clase `VentanaAsociados` a `PanelAsociados`; no se mantiene una ventana duplicada.

No se modificarán `AsociadoDAO`, `AsociadoService`, `InicializadorBD`, `ConexionBD` ni el esquema. La única prueba existente que se renombró y adaptó fue `PanelAsociadosTest`.

## 5. Pasos detallados

### Paso 0 — Preparación y línea base

1. Crear la rama `fase-4` desde `main`.
2. Confirmar que `mvn test` pasa con 49 pruebas y 0 fallos.
3. Releer AGENTS.md §12.4 y el documento funcional §4.2.
4. No tocar la base de datos real ni crear datos de prueba en ella.

### Paso 1 — Modelo `Prestamo`

1. Crear `model/Prestamo.java` con:
   - `long id`
   - `long asociadoId`
   - `long monto`
   - `LocalDate fecha`
   - `String plazo`
   - `int numeroCuotas`
   - `long valorCuota`
   - `String estado`
2. Agregar constantes `ESTADO_VIGENTE = "Vigente"` y `ESTADO_FINALIZADO = "Finalizado"`.
3. Constructores vacío, sin id y con id; getters y setters.
4. No incluir campos calculados ni nombres de asociado en el modelo.

### Paso 2 — DAO `PrestamoDAO`

1. Constructor sin argumentos que use `ConexionBD.rutaBaseDatos()` y constructor con `Path`.
2. Implementar, con `PreparedStatement` y `try-with-resources`:
   - `crear(Prestamo)` → devuelve `long` y asigna el id generado.
   - `actualizar(Prestamo)` → devuelve `boolean`; actualiza monto, fecha, plazo, número de cuotas, valor de cuota y estado; **no actualiza `asociado_id`**; devuelve `false` si no afecta ninguna fila.
   - `buscarPorId(long)` → `Optional<Prestamo>`.
   - `listar()` → `List<Prestamo>`, orden estable por `id`.
3. Mapear `fecha` con `LocalDate.parse`/`toString` y las columnas `snake_case` a camelCase.
4. No agregar métodos de borrado ni SQL concatenado con datos.

### Paso 3 — Pruebas del DAO

Crear `PrestamoDAOTest` con `@TempDir`, `InicializadorBD.inicializar(ruta)` y `new PrestamoDAO(ruta)`:

1. Crear y recuperar un préstamo usando una instancia nueva del DAO.
2. Actualizar monto, fecha, plazo, cuotas, valor de cuota y estado.
3. Actualizar un id inexistente → `false`.
4. Listar varios préstamos en orden estable.
5. Rechazar un `asociado_id` inexistente por la FK.
6. Comprobar que `actualizar` no cambia `asociado_id`.

### Paso 4 — Servicio `PrestamoService`

1. Constructores: sin argumentos y con `PrestamoDAO` + `AsociadoDAO` (sin frameworks).
2. `registrar(Prestamo)`:
   - id se ignora y se asigna por la BD;
   - valida asociado existente con `AsociadoDAO.buscarPorId`;
   - valida monto > 0, fecha no nula, plazo no vacío, número de cuotas > 0, valor de cuota > 0 y estado permitido;
   - guarda los valores tal como se ingresan, sin cálculos.
3. `actualizar(Prestamo)`:
   - exige id > 0 y préstamo existente;
   - recupera el préstamo persistido;
   - si `asociadoId` cambia, lanza `IllegalArgumentException("No se puede cambiar el asociado del préstamo.")`;
   - valida el resto de campos y actualiza;
   - si `PrestamoDAO.actualizar` devuelve `false`, lanza `SQLException`; nunca lo trata como éxito.
4. `buscarPorId(long)` y `listar()`.
5. Mensajes de error en español, sin mostrar detalles de SQL.
6. Las `SQLException` se propagan; nunca se convierten en éxito.

### Paso 5 — Pruebas del servicio

Crear `PrestamoServiceTest` con `@TempDir`:

1. Registrar un préstamo válido.
2. Rechazar asociado inexistente.
3. Rechazar monto cero o negativo.
4. Rechazar fecha nula.
5. Rechazar número de cuotas cero o negativo.
6. Rechazar valor de cuota cero o negativo.
7. Rechazar plazo vacío.
8. Rechazar estado distinto de `Vigente`/`Finalizado`.
9. Actualizar los campos permitidos.
10. Rechazar cambio de asociado al editar.
11. Actualizar un id inexistente → `SQLException`.
12. Conservar el valor de cuota ingresado sin calcularlo.

### Paso 6 — Base de la UI

1. En `Estilo`, agregar `public static void campo(JTextField campo)` con fuente, colores, borde y tamaño preferido.
2. Hacer que `campoPassword(JPasswordField)` delegue en `campo`.
3. Crear `VentanaPrincipal`:
   - `JFrame` de 1000×680, mínimo 900×600, centrada;
   - barra superior de 52 px con título, pestañas **Asociados** y **Préstamos**, y el avatar de administrador;
   - `CardLayout` para el contenido;
   - cierre con `EXIT_ON_CLOSE`;
   - no mostrar Cuotas, Pagos, Home ni módulos inexistentes.
4. Convertir `VentanaAsociados` en `PanelAsociados`:
   - extiende `JPanel`;
   - conserva búsqueda, tabla, conteo y acciones;
   - elimina la barra superior y la configuración de `JFrame`;
   - para abrir `FormularioAsociado`, obtiene el `JFrame` ancestro con `SwingUtilities.getWindowAncestor` (o `null` en pruebas).
5. Usar `git mv` para renombrar `VentanaAsociados.java` a `PanelAsociados.java` y `VentanaAsociadosTest.java` a `PanelAsociadosTest.java`, conservando el historial. Adaptar la prueba para construir `PanelAsociados` directamente, manteniendo las aserciones actuales.

### Paso 7 — `PanelPrestamos`

1. Tabla no editable con columnas:
   - Asociado (documento y nombre),
   - Monto,
   - Fecha,
   - Plazo,
   - Cuotas,
   - Valor de cuota,
   - Estado.
2. Acciones:
   - **Nuevo préstamo** (primario),
   - **Editar** (secundario, deshabilitado sin selección),
   - **Actualizar lista** (secundario).
3. Estado inferior: “Préstamos registrados (n)”.
4. Cargar los préstamos con `PrestamoService.listar()` y los asociados con `AsociadoService.buscar("")`; verificado que `buscar("")` devuelve la lista completa (`buscarPorTexto` vacío → `listar()`). Usar un `Map<Long, Asociado>` para mostrar el asociado sin consultas N+1.
5. Exponer un método explícito `refrescar()` que recargue préstamos y asociados; `VentanaPrincipal` lo llama al cambiar a la pestaña, sin listeners de componentes.
6. No agregar búsqueda ni filtros en esta fase, para no ampliar el alcance.
7. Doble clic sobre una fila abre la edición.
8. Agregar una prueba mínima obligatoria de `PanelPrestamos`: con dos asociados y un préstamo, comprobar que la tabla muestra el asociado correcto; es la prueba que cubre el `Map`.

### Paso 8 — `FormularioPrestamo`

1. `JDialog` modal para alta y edición, con tarjeta blanca y estilos de `Estilo`.
2. Alta:
   - `JComboBox<Asociado>` no editable, con un `ListCellRenderer` que muestre documento y nombre (no sobrescribir `toString()` en `Asociado`);
   - primer ítem “Seleccione un asociado…” sin asociado seleccionado; si se guarda así, el formulario responde “Seleccione un asociado válido.” antes de llamar al servicio;
   - si no hay asociados, mostrar un mensaje y no abrir el formulario.
3. Edición:
   - mostrar el asociado en un `JLabel` de solo lectura;
   - conservar el `asociadoId` original;
   - mostrar el aviso “El asociado no podrá cambiarse después.”.
4. Campos:
   - Monto (entero, > 0): acepta puntos y espacios como separadores de miles; se valida el formato y luego se eliminan antes de `Long.parseLong`. Si hay letras o desborde, se muestra un mensaje claro.
   - Fecha: inicia vacía con la pista `AAAA-MM-DD`; si `LocalDate.parse` falla, se muestra “Use el formato AAAA-MM-DD.”.
   - Plazo (texto libre obligatorio).
   - Número de cuotas (entero, > 0).
   - Valor de cuota (entero, > 0, manual).
   - Estado (`JComboBox` con Vigente/Finalizado).
   La UI rechaza campos vacíos o no numéricos antes de llamar al servicio; el servicio valida los rangos.
5. Botones **Guardar** (primario y por defecto) y **Cancelar** (secundario); Escape cierra.
6. Deshabilitar campos y botones mientras se guarda; mensaje de error junto a los botones.
7. No calcular ni rellenar automáticamente el valor de cuota a partir del monto y las cuotas.

### Paso 9 — Integración

1. `VentanaLogin`, tras autenticar, abre `VentanaPrincipal` antes de hacer `dispose()`.
2. `VentanaPrincipal` crea `AsociadoService` y `PrestamoService` con las rutas por defecto; ofrecer un constructor con servicios para pruebas.
3. Mantener `App` igual: creación de administrador o login según la tabla `usuario`.
4. No alterar el esquema ni la base de datos real durante las pruebas.

### Paso 10 — Pruebas de UI y verificación

1. Adaptar/renombrar `VentanaAsociadosTest` a `PanelAsociadosTest`.
2. Agregar la prueba obligatoria de `PanelPrestamos` (título, columnas y asociado mostrado desde el `Map`).
3. Ejecutar:
   - `mvn clean compile`
   - `mvn clean test` (78 pruebas, 0 fallos)
   - `mvn exec:java`: solo comprobar que arranca sin excepciones y que el SHA-256 no cambia. Abre login o creación, **no** `VentanaPrincipal`; la UI principal y los paneles los valida el tester manual.
   - `git diff --check`
4. Verificar el SHA-256 de la BD real antes y después de `exec:java`.
5. Reportar archivos, pruebas, decisiones, limitaciones y lo que no se pudo comprobar.

### Paso 11 — Documentación y cierre

1. Actualizar README y AGENTS con el estado de la Fase 4, el número real de pruebas y las clases nuevas.
2. Documentar P2 (a) como limitación: un préstamo creado con un asociado equivocado no se puede reasignar ni eliminar.
3. No hacer commit ni push sin autorización explícita.

## 6. Reglas de validación del servicio

| Regla | Mensaje propuesto |
| --- | --- |
| Asociado obligatorio/existente | “Seleccione un asociado válido.” |
| Monto > 0 | “El monto debe ser mayor que cero.” |
| Fecha obligatoria | “La fecha es obligatoria.” |
| Plazo no vacío | “El plazo es obligatorio.” |
| Cuotas > 0 | “El número de cuotas debe ser mayor que cero.” |
| Valor de cuota > 0 | “El valor de cuota debe ser mayor que cero.” |
| Estado permitido | “El estado debe ser Vigente o Finalizado.” |
| Cambio de asociado | “No se puede cambiar el asociado del préstamo.” |

No se agregan validaciones cruzadas entre cuotas y préstamo (P3) ni unicidad de recibos (P4).

## 7. Pruebas previstas

- `PrestamoDAOTest` 6.
- `PrestamoServiceTest` 12.
- `FormularioPrestamoTest` 10.
- `PanelPrestamosTest` 1.
- Total nuevo: **29 pruebas** (la previsión era 23; el formulario acabó con 10 en vez de 4).
- `PanelAsociadosTest` es la prueba renombrada de Asociados; no suma.
- Total de la suite: **49 + 29 = 78 pruebas**, verificado con `mvn clean test`.

## 8. Criterios de aceptación

- Se puede registrar un préstamo vinculado a un asociado existente.
- Se guardan monto, fecha, plazo, número de cuotas, valor de cuota y estado sin cálculos.
- Se puede editar un préstamo sin cambiar su asociado.
- Se rechaza un intento de cambiar el asociado desde el servicio.
- No existe eliminación de préstamos.
- La navegación Asociados/Préstamos funciona y no muestra módulos inexistentes.
- Las 78 pruebas pasan y no se modificó el esquema.
- No hay cambios de esquema ni de la base de datos real.

## 9. Riesgos y decisiones aplicadas

1. **Convertir `VentanaAsociados` en `PanelAsociados`** obligó a renombrar y adaptar su prueba a `PanelAsociadosTest`; fue la única prueba existente que se tocó.
2. **Columnas y acciones de la tabla**: la propuesta es la mínima; no incluye búsqueda de préstamos.
3. **Asociado en edición**: se muestra como solo lectura; el servicio rechaza cambios.
4. **Fecha**: se usará texto ISO y `LocalDate`; no se agrega selector de calendario.
5. **UI tests**: se adaptó `PanelAsociadosTest`, se agregó `PanelPrestamosTest` y 10 pruebas de entrada en `FormularioPrestamoTest`.
6. **Rama y commits**: se trabajó en `fase-4`, se fusionó a `main` con `--ff-only` al cerrar y el resultado quedó publicado en `origin/main`.
