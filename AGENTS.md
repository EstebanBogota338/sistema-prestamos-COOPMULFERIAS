# AGENTS.md — Contexto y reglas para el agente coder

> **Lee este archivo completo y el documento de planeación antes de tocar código.**
> Aquí están el estado real del proyecto, las decisiones ya tomadas por el usuario, las que siguen pendientes y el alcance de la tarea actual. No repitas decisiones cerradas y no resuelvas por tu cuenta las pendientes (sección 4): pregunta.

---

## 1. Qué es este proyecto

Herramienta de escritorio **offline** para la cooperativa vinculada a la Plaza de Mercado de Las Ferias (Bogotá): administrar asociados, préstamos, cuotas y pagos. Es una práctica social universitaria; el usuario debe poder **entender y explicar** el código en su sustentación.

- **Stack:** Java (release 17), Swing, SQLite, JDBC puro, Maven.
- **Fuente de verdad funcional:** [docs/MVP-Cooperativa-Las-Ferias-v3.md](docs/MVP-Cooperativa-Las-Ferias-v3.md). Léelo entero. Si algo de este archivo lo contradice, prevalece el documento; avísalo.
- **Boceto visual:** [docs/diseno/home-mockup.html](docs/diseno/home-mockup.html) — solo referencia de lenguaje visual, no se porta.
- **Directorio de trabajo:** `D:\Documentos\programming\sistema-prestamos-COOPMULFERIAS`
- **No** reemplaza Helisa. **No** hace cálculos financieros. **No** borra registros.

---

## 2. Estado actual: Fases 1, 2, 2.5, 3, 4, 5 y 6 terminadas

**No las rehagas.** Commits: Fase 1 `ade0cf2`; Fase 2 base `3e045fb`; ronda de calidad `7b45498`; Fase 2.5 `66c39f1` y `73d4100`; Fase 3 `2f5aca0`, `de1cce5` y `f35961f`; Fase 4 `d4dd14a` (backend), `277390e` (UI), `33b86ac` (correcciones tras la prueba manual) y `71788f9` (cierre de documentación); Fase 5 `7a845ce` (modelo y DAO), `ed77dfb` (servicio) y `89e634b` (UI); Fase 6 `a6dd9cc` (modelo y DAO), `55dcc8e` (servicio) y `8700305` (UI).

### Fase 1 — estructura, esquema y arranque

| Archivo | Contenido |
| --- | --- |
| [pom.xml](pom.xml) | `jar`, `com.lasferias:cooperativa-las-ferias:1.0-SNAPSHOT`, `maven.compiler.release=17`, UTF-8 |
| [ConexionBD.java](src/main/java/com/lasferias/cooperativa/database/ConexionBD.java) | Conexión nueva por operación (sin conexión global); `PRAGMA foreign_keys = ON` en **cada** conexión; sobrecarga con `Path` para pruebas |
| [InicializadorBD.java](src/main/java/com/lasferias/cooperativa/database/InicializadorBD.java) | DDL idempotente (`CREATE TABLE/INDEX IF NOT EXISTS`) |

### Fase 2 — módulo Asociados

| Archivo | Contenido |
| --- | --- |
| [model/Asociado.java](src/main/java/com/lasferias/cooperativa/model/Asociado.java) | `id`, `documento`, `nombres`, `apellidos`, `telefono`, `fechaRegistro` (`LocalDate`) |
| [dao/AsociadoDAO.java](src/main/java/com/lasferias/cooperativa/dao/AsociadoDAO.java) | JDBC parametrizado, constructor con `Path`; crear / actualizar / buscarPorDocumento / buscarPorId / buscarPorTexto / listar / existeDocumento. **Sin eliminar** |
| [service/AsociadoService.java](src/main/java/com/lasferias/cooperativa/service/AsociadoService.java) | Normalización, validaciones, unicidad, fecha automática, traducción de `UNIQUE` |
| [ui/PanelAsociados.java](src/main/java/com/lasferias/cooperativa/ui/PanelAsociados.java) | Contenido de Asociados: búsqueda, `JTable` no editable, conteo y acciones, sin barra superior; nació de `VentanaAsociados` en la Fase 4 |
| [ui/FormularioAsociado.java](src/main/java/com/lasferias/cooperativa/ui/FormularioAsociado.java) | `JDialog` modal compartido para alta y edición |
| [ui/Estilo.java](src/main/java/com/lasferias/cooperativa/ui/Estilo.java) | Paleta, fuente, botón primario/secundario y borde de campo compartidos (Fase 2.5) |
| [App.java](src/main/java/com/lasferias/cooperativa/App.java) | Inicializa la BD, aplica el look and feel y abre `VentanaCreacionAdministrador` si `usuario` está vacía, o `VentanaLogin` si no; `System.exit(1)` si falla la BD |

**Pruebas actuales: 125** (JUnit 5 + `@TempDir`) — 49 de las fases 1–3, 29 de la Fase 4, 21 de la Fase 5, 21 de la Fase 6 (`PagoDAOTest` 5, `PagoServiceTest` 10, `FormularioPagoTest` 5, `PanelPagosTest` 1) y 5 de P1 (triggers, en `InicializadorBDTest`). `PanelAsociadosTest` es la prueba renombrada de Asociados. `mvn clean test` da **125 pruebas y 0 fallos**.

**Decisiones de la ronda de calidad (no están en el documento; documéntalas, no las amplíes):** documento guardado en mayúsculas con unicidad insensible a mayúsculas en servicio y DAO (`COLLATE NOCASE`); búsqueda por documento, nombres y apellidos ignorando acentos y comodines (filtrado en Java); orden alfabético con `Collator` es-CO; diagnóstico con `java.util.logging`.

### Fase 3 — login y administrador inicial

| Archivo | Contenido |
| --- | --- |
| [model/Usuario.java](src/main/java/com/lasferias/cooperativa/model/Usuario.java) | `id` (long), `nombreUsuario`, `contrasenaHash`; nunca transporta la contraseña en claro |
| [dao/UsuarioDAO.java](src/main/java/com/lasferias/cooperativa/dao/UsuarioDAO.java) | JDBC con `Path`; `crear`, `obtenerAdministrador` → `Optional<Usuario>`, `existeAlgunUsuario`. Sin actualizar ni eliminar |
| [service/AutenticacionService.java](src/main/java/com/lasferias/cooperativa/service/AutenticacionService.java) | `NOMBRE_ADMINISTRADOR = "admin"`; `debeCrearAdministrador`, `crearAdministrador(contrasena, confirmacion)` con BCrypt y `autenticar(contrasena)` |
| [ui/VentanaLogin.java](src/main/java/com/lasferias/cooperativa/ui/VentanaLogin.java) | Login con `JPasswordField`, botón **Ingresar** por defecto; cerrar la ventana cierra la app |
| [ui/VentanaCreacionAdministrador.java](src/main/java/com/lasferias/cooperativa/ui/VentanaCreacionAdministrador.java) | Contraseña y confirmación; al crear el administrador abre el login |
| [ui/Estilo.java](src/main/java/com/lasferias/cooperativa/ui/Estilo.java) | `Estilo.campo(JTextField)` y `campoPassword(JPasswordField)` que delega en él |

### Fase 4 — Préstamos (terminada)

| Archivo | Contenido |
| --- | --- |
| [model/Prestamo.java](src/main/java/com/lasferias/cooperativa/model/Prestamo.java) | `id`, `asociadoId`, `monto`, `fecha`, `plazo`, `numeroCuotas`, `valorCuota`, `estado` |
| [dao/PrestamoDAO.java](src/main/java/com/lasferias/cooperativa/dao/PrestamoDAO.java) | JDBC con `Path`; crear, actualizar (devuelve `boolean`), buscarPorId y listar; sin eliminar |
| [service/PrestamoService.java](src/main/java/com/lasferias/cooperativa/service/PrestamoService.java) | Validaciones, asociado existente, protección del vínculo y actualización controlada |
| [ui/VentanaPrincipal.java](src/main/java/com/lasferias/cooperativa/ui/VentanaPrincipal.java) | Barra superior, pestañas Asociados/Préstamos y `CardLayout` |
| [ui/PanelPrestamos.java](src/main/java/com/lasferias/cooperativa/ui/PanelPrestamos.java) | Tabla no editable, acciones Nuevo/Editar/Actualizar y `Map` de asociados |
| [ui/FormularioPrestamo.java](src/main/java/com/lasferias/cooperativa/ui/FormularioPrestamo.java) | Alta/edición modal, asociado fijo en edición, montos normalizados y fecha ISO |
| [ui/VentanaLogin.java](src/main/java/com/lasferias/cooperativa/ui/VentanaLogin.java) | Tras autenticar abre `VentanaPrincipal` antes de `dispose()` |
| Pruebas | `PrestamoDAOTest` 6, `PrestamoServiceTest` 12, `FormularioPrestamoTest` 10, `PanelPrestamosTest` 1; total 29 (suite completa: 78) |

**Prueba manual (Fase 4):** recorrido completo sobre una BD temporal; la real no se tocó (mismo SHA-256 antes y después). Se verificó login, pestañas, alta y edición de asociados, alta de préstamos, montos, fechas y navegación. En `33b86ac` se corrigieron los cuatro defectos visuales detectados: avatar "AD" elidido a `...`, botones primarios con texto blanco sobre blanco, diálogo de asociado con tamaño fijo que recortaba el campo Documento, y concordancia "1 registro / 1 resultado". Queda pendiente de pulir, sin bloquear: el foco no viaja al campo con error.

### Fase 5 — Cuotas (terminada)

| Archivo | Contenido |
| --- | --- |
| [model/Cuota.java](src/main/java/com/lasferias/cooperativa/model/Cuota.java) | `id`, `prestamoId`, `numeroCuota`, `valor`, `fechaVencimiento`; sin campos calculados |
| [dao/CuotaDAO.java](src/main/java/com/lasferias/cooperativa/dao/CuotaDAO.java) | JDBC con `Path`; crear, actualizar condicionado, buscarPorId, listar, listarPorPrestamo, existeNumero y tienePagos; sin eliminar |
| [service/CuotaService.java](src/main/java/com/lasferias/cooperativa/service/CuotaService.java) | Validaciones, préstamo existente, vínculo fijo, unicidad y bloqueo por pagos |
| [ui/VentanaPrincipal.java](src/main/java/com/lasferias/cooperativa/ui/VentanaPrincipal.java) | Barra superior y pestañas Asociados/Préstamos/Cuotas |
| [ui/PanelCuotas.java](src/main/java/com/lasferias/cooperativa/ui/PanelCuotas.java) | Tabla no editable, acciones Nueva cuota/Editar/Actualizar y Map de préstamos y asociados |
| [ui/FormularioCuota.java](src/main/java/com/lasferias/cooperativa/ui/FormularioCuota.java) | Diálogo modal; préstamo fijo en edición; aviso y bloqueo con pagos |
| [test/dao/CuotaDAOTest.java](src/test/java/com/lasferias/cooperativa/dao/CuotaDAOTest.java) | Pruebas del DAO |
| [test/service/CuotaServiceTest.java](src/test/java/com/lasferias/cooperativa/service/CuotaServiceTest.java) | Pruebas de reglas del servicio |
| [test/ui/FormularioCuotaTest.java](src/test/java/com/lasferias/cooperativa/ui/FormularioCuotaTest.java) | Pruebas del formulario |
| [test/ui/PanelCuotasTest.java](src/test/java/com/lasferias/cooperativa/ui/PanelCuotasTest.java) | Prueba del panel |

**Prueba manual (Fase 5):** validación manual por el tester sobre una BD aislada (`-Duser.home` a un temporal); la BD real conservó su SHA-256. Trece comprobaciones en PASA: arranque, login, tres pestañas, alta de cuota con `1.500.000` → `1500000`, unicidad por préstamo, nueve mensajes de validación, edición sin pagos, edición bloqueada con pagos, persistencia y huella de la BD real. Notas: el rechazo de cambio de préstamo no es reproducible desde la interfaz y solo lo cubre `CuotaServiceTest.rechazaCambioDePrestamoAlEditar`; el mensaje "Seleccione una cuota de la tabla para editarla." es defensivo porque Editar está deshabilitado sin selección (`PanelCuotas.java:87`, `:119`, `:162`); la carrera entre la comprobación de pagos y el `UPDATE` condicionado no se verifica manualmente, la protege el `UPDATE` atómico y su traducción en el servicio. Nada nuevo pendiente de pulir: rigen las limitaciones de §12.

### Fase 6 — Pagos (terminada)

| Archivo | Contenido |
| --- | --- |
| [model/Pago.java](src/main/java/com/lasferias/cooperativa/model/Pago.java) | `id`, `cuotaId`, `fechaPago`, `valor`, `numeroRecibo` |
| [dao/PagoDAO.java](src/main/java/com/lasferias/cooperativa/dao/PagoDAO.java) | JDBC con `Path`; crear, actualizar en transacción, buscarPorId, listar y listarPorCuota; sin eliminar |
| [service/PagoService.java](src/main/java/com/lasferias/cooperativa/service/PagoService.java) | Validaciones, cuota existente, vínculo fijo y edición controlada |
| [ui/VentanaPrincipal.java](src/main/java/com/lasferias/cooperativa/ui/VentanaPrincipal.java) | Barra superior y pestañas Asociados/Préstamos/Cuotas/Pagos |
| [ui/PanelPagos.java](src/main/java/com/lasferias/cooperativa/ui/PanelPagos.java) | Tabla no editable con todos los pagos y acciones Nuevo/Editar/Actualizar |
| [ui/FormularioPago.java](src/main/java/com/lasferias/cooperativa/ui/FormularioPago.java) | Alta/edición modal; cuota fija en edición y parser de montos/fecha |
| [test/dao/PagoDAOTest.java](src/test/java/com/lasferias/cooperativa/dao/PagoDAOTest.java) | Pruebas del DAO |
| [test/service/PagoServiceTest.java](src/test/java/com/lasferias/cooperativa/service/PagoServiceTest.java) | Pruebas de reglas del servicio |
| [test/ui/FormularioPagoTest.java](src/test/java/com/lasferias/cooperativa/ui/FormularioPagoTest.java) | Pruebas del formulario |
| [test/ui/PanelPagosTest.java](src/test/java/com/lasferias/cooperativa/ui/PanelPagosTest.java) | Prueba del panel |

**Prueba manual (Fase 6):** validación manual por el tester sobre una BD aislada (`-Duser.home` a un temporal); la BD real conservó su SHA-256. Catorce comprobaciones en PASA: arranque, login, cuatro pestañas, aviso con cero cuotas, alta con abono parcial, recibo con `trim`, validaciones, edición conservando la cuota, bloqueo de la cuota en Cuotas, pago aún editable, mismo recibo en dos pagos, persistencia y huella de la BD real. Notas: el bloqueo de cuotas se validó por interfaz por primera vez en esta fase (aviso literal, tres campos y Guardar deshabilitados; el pago sigue editable y Cancelar no altera la cuota); `construirPago` valida cuota → fecha → valor → recibo, por lo que con varios campos vacíos sale primero la fecha, a diferencia de Préstamos y Cuotas; el combo de cuota trunca la etiqueta cerrada (`... - Cuo…`) y exige dos gestos para elegir; no se verificaron el `rollback` ante un fallo real de BD ni el mensaje de cambio de cuota desde la UI, porque el vínculo es texto fijo.

### Dependencias (versiones finales)

- `org.xerial:sqlite-jdbc:3.49.1.0`
- `org.junit.jupiter:junit-jupiter:5.11.4` (test)
- `maven-surefire-plugin:3.5.4`
- `org.codehaus.mojo:exec-maven-plugin:3.1.0` (`mainClass` = `com.lasferias.cooperativa.App`)
- `org.mindrot:jbcrypt:0.4` (instalada en la Fase 3; hash de contraseñas con BCrypt)

### Entorno verificado

- `java -version` → openjdk 25.0.2 LTS (Temurin); `mvn -version` → Apache Maven 3.9.16; Windows 11; UTF-8.
- `maven.compiler.release` = 17 (se compila con JDK 25, bytecode 17).
- `JAVA_HOME` vacío: Maven usa el Java del `PATH`. Caché local en `C:\Users\Casa\.m2`.

### Comandos que deben seguir funcionando

```
mvn clean compile   → BUILD SUCCESS
mvn test            → Failures: 0, Errors: 0 (125 pruebas actuales + las que agregue cada fase)
mvn exec:java       → abre la aplicación sin excepciones
```

**Ruta de la BD:** `C:\Users\Casa\AppData\Roaming\CooperativaLasFerias\cooperativa.db` (`user.home\AppData\Roaming\CooperativaLasFerias\cooperativa.db`).

### Esquema (no lo cambies sin autorización)

- `usuario(id, nombre_usuario UNIQUE, contrasena_hash)`
- `asociado(id, documento UNIQUE, nombres, apellidos, telefono, fecha_registro)`
- `prestamo(id, asociado_id FK, monto>0, fecha, plazo TEXT, numero_cuotas>0, valor_cuota>0, estado IN ('Vigente','Finalizado'))`
- `cuota(id, prestamo_id FK, numero_cuota, valor>0, fecha_vencimiento, UNIQUE(prestamo_id, numero_cuota))`
- `pago(id, cuota_id FK, fecha_pago, valor>0, numero_recibo)`
- Índices: `idx_prestamo_asociado`, `idx_cuota_prestamo`, `idx_pago_cuota`. Sin `ON DELETE CASCADE`.

Fechas `TEXT` ISO `YYYY-MM-DD`; montos `INTEGER` en pesos colombianos enteros (sin centavos). `plazo` es `TEXT` libre: no asumas meses ni ninguna otra unidad.

---

## 3. Decisiones cerradas por el usuario (no re-preguntar)

| Tema | Decisión |
| --- | --- |
| **Fases 1, 2, 2.5, 3, 4, 5 y 6** | Terminadas. |
| **Triggers de SQLite (P1)** | **Cerrada.** Seis triggers idempotentes en `InicializadorBD`: no se permite borrar en `asociado`, `prestamo`, `cuota`, `pago` ni `usuario`, y no se puede actualizar una cuota que ya tenga pagos. Commits `9e67a53` y `5addd77`. |
| **Vínculos al editar (P2)** | Se fijan al crear y no se modifican; en edición se muestran como solo lectura. Aplica a `prestamo.asociado_id`, `cuota.prestamo_id` y `pago.cuota_id`. |
| **Administrador inicial** | Si `usuario` está vacía, la app pide **contraseña y confirmación** para crearlo. El nombre de usuario **no se pide**: se guarda siempre como `admin` (constante). |
| **Acceso** | El login pide **solo la contraseña**. Hay un único administrador. |
| **Hash de contraseña** | `org.mindrot:jbcrypt:0.4` (BCrypt). Obligatorio; nunca texto plano ni en logs. |
| **Política de contraseña** | *Supuesto mínimo:* obligatoria, 6 caracteres o más, con confirmación al crear. Si el usuario define otra regla, prevalece la suya. |
| **Sesión** | Sin "cerrar sesión", sin multiusuario ni permisos. Cambio de contraseña: opcional (ver sección 12). |
| **Segundo administrador** | Imposible: lo impide el servicio y, además, `UNIQUE(nombre_usuario)` en SQLite. |
| **Copia de seguridad** | Botón con `JFileChooser` + procedimiento de recuperación documentado. Fase posterior. |
| **Datos ficticios** | Script SQL en `demo/` ejecutado manualmente; **nunca** al arrancar. |
| **Apariencia** | Look and feel del sistema. **No** FlatLaf. Paleta del documento aplicada a mano. |
| **Git** | Commits pequeños en español. **No commitear sin que se pida.** |
| **Plazo del proyecto** | 8 jornadas; prioriza flujo funcional sobre detalles visuales. |
| **fecha_registro** | Automática al crear, no editable, se conserva al editar. |
| **Teléfono** | Opcional; solo dígitos, entre 7 y 10 caracteres. |
| **UI / boceto** | Barra superior + paleta del mockup. **No** se muestran módulos inexistentes, saldos ni estados no documentados. |

---

## 4. Decisiones pendientes (no las resuelvas tú; pregunta)

| # | Tema | Qué hacer mientras tanto |
| --- | --- | --- |
| P3 | **Validaciones cruzadas cuota ↔ préstamo** (p. ej. `numero_cuota` ≤ `numero_cuotas` del préstamo; `valor` de cuota distinto de `valor_cuota`). No están en el documento. | No imponerlas. Registrar los valores tal como se ingresan. |
| P4 | **Unicidad de `numero_recibo`.** Un mismo recibo puede cubrir varias cuotas. | No agregar `UNIQUE`. Solo obligatorio. |

---

## 5. Patrón de módulo y reglas de diseño

Cada módulo sigue: `model` (datos) → `dao` (JDBC con `Path` inyectable) → `service` (validación + mensajes en español + traducción de errores) → `ui` (pantalla + diálogo compartido).

- **DAO:** `try-with-resources`, `PreparedStatement` siempre, sin SQL concatenado con datos del usuario, **sin métodos de borrado**, cada operación abre y cierra su conexión. Operaciones con varias escrituras relacionadas, y la edición de un pago, van en transacción (`setAutoCommit(false)` + `commit`/`rollback`). **No crees un `BaseDAO` genérico ni interfaces DAO:** la repetición del `try-with-resources` es aceptable.
- **Service:** normaliza (`trim`, vacío → `null` en opcionales), lanza `IllegalArgumentException` con texto legible en español y asigna las reglas del sistema. Los errores de BD se traducen o se propagan como `SQLException`; nunca se muestran como éxito.
- **UI:** estilos solo desde `ui/Estilo` (ver sección 6). Tabla no editable, formulario modal para alta y edición, mensaje de error junto a los botones, estado en la franja inferior.
- **Tests:** JUnit 5 con `@TempDir`, BD sembrada con `InicializadorBD.inicializar(ruta)`, DAO y servicio construidos con esa ruta. **Nunca** se usa la BD real ni `%APPDATA%`.
- **Integridad en dos capas:** toda regla importante se valida en el servicio (mensaje claro) **y** la respalda un constraint de SQLite cuando corresponda (`UNIQUE`, `FOREIGN KEY`, `CHECK`).
- **Presupuesto de complejidad:** cada módulo nuevo se entrega en su versión mínima con las pruebas esenciales de sus reglas de negocio. **No hagas "rondas de calidad" ni agregues extras** (normalización de acentos, ordenamientos especiales, etc.) que no se pidan. El código debe poder explicarse en una sustentación.

### Paleta y medidas del boceto

| Elemento | Valor |
| --- | --- |
| Fondo / superficie | `#F5F7F6` / `#FFFFFF` |
| Texto principal / secundario | `#212529` / `#6B7280` |
| Bordes | `#E2E5E3` |
| Verde principal / hover | `#2F8F57` / `#256F45` |
| Barra superior | 52 px, blanca, borde inferior |
| Botón primario | fondo `#2F8F57`, texto blanco |

No portar HTML, CSS, CDN ni iconos: la UI es Swing.

---

## 6. Fase 2.5 — extracción de `ui/Estilo` (terminada)

Los colores, `fuente()`, los botones primario y secundario y el borde de campo se extrajeron a `ui/Estilo`. Los márgenes de botón quedaron unificados en 8,15. Verificado: 33 pruebas, 0 fallos, BUILD SUCCESS.

---

## 7. Fase 3 — Login y administrador inicial (terminada)

- Login: pide solo la contraseña.
- Usuario: siempre `admin` (constante del servicio).
- Al crear el administrador se abre el login.

Siguiente tarea: Historial por asociado (ver §12.7).

---

## 8. Reglas de negocio que NO se pueden romper (todo el MVP)

- **Nada de cálculos financieros:** sin intereses, amortización, saldo pendiente, valor de cuota calculado ni agregaciones de montos.
- **Nada de borrar registros:** ni usuarios, ni asociados, ni préstamos, ni cuotas, ni pagos. Solo editar.
- **Cuotas con pagos asociados:** no se puede editar número, valor ni fecha de vencimiento. Sin pagos, sí. La cuota y sus pagos siempre se pueden **consultar**.
- **Pagos:** se editan directamente sobre el registro; **sin historial de versiones ni auditoría**. El historial muestra el estado actual del pago.
- **Abonos parciales permitidos**; valores nulos, cero o negativos rechazados; no se asocia un pago a una cuota inexistente; número de recibo obligatorio.
- **Contraseña siempre con BCrypt** (nunca texto plano, nunca en logs).
- **FK activas** en cada conexión, consultas parametrizadas y transacciones cuando haya varias escrituras relacionadas (incluida la edición de un pago).
- **No inventar** campos, tablas, estados ni validaciones fuera del documento.
- La BD vive en `%APPDATA%\CooperativaLasFerias`, nunca en `target/` ni en temporales. No la subas al repositorio.
- Una funcionalidad no implementada no debe presentarse como operativa.

---

## 9. Convenciones del proyecto

- Paquete base `com.lasferias.cooperativa`; identificadores, comentarios y mensajes en español.
- JDBC directo con `try-with-resources`; nada de frameworks, ORM, inyección de dependencias ni capas extra.
- Dependencias nuevas: solo con justificación y autorización.
- No cambies el DDL existente ni los nombres de columnas; mapeo `snake_case` → `camelCase`.
- Commits en español, pequeños y claros (`Fase 3: ...`); no commitear sin pedido explícito.
- Una rama por fase (fase-N), fusionada a main al terminar. El coder nunca hace push.

---

## 10. Cómo trabajar (obligatorio)

1. `git -c safe.directory="D:/Documentos/programming/sistema-prestamos-COOPMULFERIAS" status` y revisa el árbol antes de editar. Conserva cambios ajenos a tu tarea.
2. Incrementos: modelo → DAO → pruebas DAO → servicio → pruebas servicio → UI → integración con `App`.
3. Ejecuta `mvn clean compile` y `mvn test` tras cada incremento. **No afirmes que algo funciona sin haberlo ejecutado.**
4. Al terminar, `mvn exec:java` y comprueba el flujo real. Si no pudiste ver la UI, dilo.
5. Reporta: archivos creados o modificados, dependencias, comandos con su **salida real**, cuántas pruebas pasan, decisiones tomadas, limitaciones y cualquier diferencia respecto a este archivo.
6. Si algo contradice el documento o este archivo, o falta un dato imprescindible, detente y pregunta con una pregunta concreta.
7. El coder no crea el administrador ni modifica la BD real; comprueba el arranque por el título de la ventana y el SHA-256 de la BD antes y después. La prueba manual con login la hace el usuario.

---

## 11. Trampas conocidas del entorno

- **Propiedad del repo:** el árbol es de `BUILTIN\Administradores`; `git` sin `-c safe.directory=…` falla con *dubious ownership*. No cambies la configuración global sin permiso.
- **JDK 25:** al cargar la DLL de SQLite aparece `WARNING: A restricted method in java.lang.System has been called`. Es inocuo.
- **PowerShell envuelve stderr** de `java`/`mvn` como `NativeCommandError`; verifica con `$LASTEXITCODE`.
- **`JAVA_HOME` vacío:** Maven usa el Java del `PATH`. No configures variables de sistema.
- `.github/modernize/` es basura de una extensión de VS Code, excluida vía `.git/info/exclude`. No la toques.
- No instales herramientas globales ni ejecutes comandos destructivos.

---

## 12. Roadmap y limitaciones conocidas

### Roadmap (orden)

1. **Fase 2.5:** extraer `ui/Estilo` — terminada.
2. **Fase 3:** login y administrador inicial — terminada.
3. **Triggers de SQLite** — **terminada (P1)**: seis triggers idempotentes en `InicializadorBD`; el esquema real los recibe en el siguiente arranque, una sola vez.
4. **Préstamos:** introduce `VentanaPrincipal` (barra superior y navegación por paneles) y convierte `VentanaAsociados` en `PanelAsociados`, manteniendo las pruebas en verde. Préstamo vinculado a un asociado existente; el asociado se fija al crear y no se puede cambiar al editar (P2 a); `plazo` texto libre; `estado` Vigente/Finalizado manual; `valor_cuota` manual. Generalizar `Estilo.campoPassword` a `Estilo.campo(JTextField)`. **Fase 4: terminada.**
5. **Cuotas:** no se crea database/ErroresBD (un solo llamador no justifica la clase; además getErrorCode() devuelve 19 tanto para UNIQUE como para FK, así que el código no discrimina). La violación de unicidad se traduce en CuotaService con un método privado que use org.sqlite.SQLiteException.getResultCode() == SQLITE_CONSTRAINT_UNIQUE, nunca el texto del mensaje. Bloqueo de edición con pagos: comprobación en el servicio para el mensaje + UPDATE ... WHERE id = ? AND NOT EXISTS (SELECT 1 FROM pago WHERE cuota_id = ?) en el DAO para la atomicidad. AsociadoService.esDuplicadoDeDocumento no se toca en esta fase. Decisiones de Cuotas: numero_cuota >= 1 se valida solo en el servicio (el documento v3 no lo pide; no hay CHECK en el DDL porque la tabla ya existe); un préstamo Finalizado puede recibir cuotas (no hay regla documentada); P1 (triggers) quedó cerrada (ver §3); la edición de un pago exigirá transacción (v3 §5.7), con sobrecargas de DAO que reciban una Connection. **Fase 5: terminada.**
6. **Pagos:** P2 cerrada (a); edición directa en transacción, abonos parciales, sin historial de versiones. **Fase 6: terminada.**
7. **Historial por asociado:** consultas con `JOIN`, sin duplicar registros, estado actual de cada pago, sin totales ni saldos.
8. **Copia de seguridad desde la UI:** `JFileChooser` + `VACUUM INTO` (copia consistente; **falla si el archivo destino ya existe**, así que hay que confirmar la sobrescritura o usar un nombre nuevo). Procedimiento de recuperación documentado: con la aplicación cerrada, reemplazar el archivo de la BD.
9. **Datos de demostración** (`demo/`, script SQL manual) y **documentación final** (README, procedimiento de respaldo, limitaciones).

Recortes si el tiempo aprieta (en este orden): cambio de contraseña, Home con tarjetas de acceso, extras de búsqueda. Lo demás es indispensable por los criterios de aceptación del documento.

### Limitaciones conocidas (documentar en el README final)

- La edición de pagos **no conserva versiones anteriores** ni implementa auditoría (decisión cerrada del documento).
- Sin bloqueo por intentos fallidos.
- jBCrypt 0.4 no tiene mantenimiento activo y solo considera los primeros 72 bytes de la contraseña; es adecuado para este alcance.
- `plazo` es texto libre, sin unidad definida.
- Los vínculos se fijan al crear (P2 a): un registro creado bajo un padre equivocado no puede moverse ni eliminarse.
- La unicidad del documento en SQLite distingue mayúsculas; el servicio normaliza a mayúsculas antes de guardar.
- La ruta de la BD se arma con `user.home\AppData\Roaming`, no con la variable `%APPDATA%`; pueden diferir si el perfil está redirigido.
- No hay cambio ni recuperación de contraseña: si se olvida, solo se recupera restaurando una copia de seguridad anterior o recreando la base de datos.
- Los botones primarios usan `BasicButtonUI` para que el fondo verde se pinte de verdad; como efecto secundario pierden el resaltado de "botón por defecto" del L&F de Windows (Enter sigue funcionando). Es apariencia, no comportamiento.
- Pendiente de pulir, sin bloquear: tras un guardado fallido el foco se queda en el último campo y no viaja al que tiene el error.
- `parsearMonto` acepta ceros a la izquierda sin separadores (`"000800"` → 800) y rechaza `"0.800"` y `"0.001"`; `"0"` lo rechaza el servicio, no el parser. Cuotas usa la misma regla.
- En botones primarios deshabilitados el fondo sigue verde y el texto queda casi ilegible; es cosmético y solo ocurre mientras se guarda.
- `cuota.numero_cuota` no tiene `CHECK` en la base: la validación (`>= 1`) es solo del servicio.
- El sistema no restringe el registro de cuotas según el estado del préstamo; el estado es un dato manual.
- El sistema no verifica que la cantidad de cuotas registradas coincida con `prestamo.numero_cuotas` (decisión P3).
- En el formulario de Pagos, el combo de cuota trunca la etiqueta cuando está cerrado (se lee completa en la lista desplegada y en la tabla). Cosmético.