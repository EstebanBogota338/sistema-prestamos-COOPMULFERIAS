# AGENTS.md — Contexto y reglas para el agente coder

> **Lee este archivo completo y el documento de planeación antes de tocar código.**
> Aquí están el estado real del proyecto, las decisiones ya tomadas por el usuario y el alcance de la siguiente fase. No repitas decisiones que aquí ya están cerradas.

---

## 1. Qué es este proyecto

Herramienta de escritorio **offline** para la cooperativa vinculada a la Plaza de Mercado de Las Ferias: administrar asociados, préstamos, cuotas y pagos.

- **Stack:** Java (release 17), Swing, SQLite, JDBC puro, Maven.
- **Documento de referencia (fuente de verdad funcional):** [docs/MVP-Cooperativa-Las-Ferias-v3.md](docs/MVP-Cooperativa-Las-Ferias-v3.md) — 372 líneas, léelo entero.
- **Directorio de trabajo:** `D:\Documentos\programming\sistema-prestamos-COOPMULFERIAS`
- **No** reemplaza Helisa. **No** hace cálculos financieros. **No** borra registros.

---

## 2. Estado actual: Fase 1 terminada y verificada

La Fase 1 ya está implementada y comprobada con comandos reales. **No la rehagas.**

| Archivo | Contenido |
| --- | --- |
| [pom.xml](pom.xml) | Empaquetado `jar`, groupId `com.lasferias`, artifactId `cooperativa-las-ferias`, version `1.0-SNAPSHOT`, `maven.compiler.release=17`, UTF-8 |
| [App.java](src/main/java/com/lasferias/cooperativa/App.java) | `main` que inicializa la BD e imprime la ruta |
| [ConexionBD.java](src/main/java/com/lasferias/cooperativa/database/ConexionBD.java) | Conexiones SQLite sin conexión global; `PRAGMA foreign_keys = ON` en **cada** conexión |
| [InicializadorBD.java](src/main/java/com/lasferias/cooperativa/database/InicializadorBD.java) | DDL idempotente (`CREATE TABLE/INDEX IF NOT EXISTS`) |
| [InicializadorBDTest.java](src/test/java/com/lasferias/cooperativa/database/InicializadorBDTest.java) | 2 pruebas JUnit 5 con `@TempDir`: esquema e idempotencia |
| [.gitignore](.gitignore) | `target/`, `.classpath`, `.project`, `.settings/`, `*.class`, `.vscode/` |

### Dependencias (versiones finales, ya resueltas contra Maven Central)

- `org.xerial:sqlite-jdbc:3.49.1.0` (scope default)
- `org.junit.jupiter:junit-jupiter:5.11.4` (scope test)
- `maven-surefire-plugin:3.5.4`
- `org.codehaus.mojo:exec-maven-plugin:3.1.0` con `<mainClass>com.lasferias.cooperativa.App</mainClass>`

### Entorno verificado

- `java -version` → **openjdk 25.0.2 LTS (Temurin)**
- `mvn -version` → **Apache Maven 3.9.16**, runtime JDK 25.0.2, plataforma UTF-8, Windows 11
- `maven.compiler.release` = **17** (se compila con JDK 25 pero bytecode 17)
- `JAVA_HOME` está **vacío**; Maven encuentra Java por `PATH`. No se modificó nada del sistema.
- Hay conexión a Maven Central y caché local en `C:\Users\Casa\.m2`.

### Comandos que deben seguir funcionando

```
mvn clean compile   → BUILD SUCCESS (3 clases)
mvn test            → Tests run: 2, Failures: 0, Errors: 0 (BUILD SUCCESS)
mvn exec:java       → "Base de datos inicializada correctamente en: C:\Users\Casa\AppData\Roaming\CooperativaLasFerias\cooperativa.db"
```

**Ruta efectiva de la BD:** `C:\Users\Casa\AppData\Roaming\CooperativaLasFerias\cooperativa.db` (se calcula como `user.home\AppData\Roaming\CooperativaLasFerias\cooperativa.db`; el archivo ya existe con las 5 tablas y los 3 índices).

### Esquema ya creado (no lo cambies sin autorización)

- `usuario(id, nombre_usuario UNIQUE, contrasena_hash)`
- `asociado(id, documento UNIQUE, nombres, apellidos, telefono, fecha_registro)`
- `prestamo(id, asociado_id FK, monto>0, fecha, plazo, numero_cuotas>0, valor_cuota>0, estado IN ('Vigente','Finalizado'))`
- `cuota(id, prestamo_id FK, numero_cuota, valor>0, fecha_vencimiento, UNIQUE(prestamo_id, numero_cuota))`
- `pago(id, cuota_id FK, fecha_pago, valor>0, numero_recibo)`
- Índices: `idx_prestamo_asociado`, `idx_cuota_prestamo`, `idx_pago_cuota`

Todas las fechas son `TEXT` en ISO `YYYY-MM-DD`. Los montos son `INTEGER` (pesos, sin decimales). El DDL exacto está en `InicializadorBD`; **si necesitas un campo nuevo, pídelo antes de tocarlo**.

---

## 3. Decisiones cerradas por el usuario (no re-preguntar)

| Tema | Decisión |
| --- | --- |
| **Fase 2** | **Módulo Asociados completo**: modelo, DAO, servicio con validaciones y pantalla de registro/búsqueda/edición. **El login todavía NO** (va en una fase posterior). |
| **Administrador inicial** | Pantalla de **creación del administrador en el primer arranque** (si la tabla `usuario` está vacía). Se implementa cuando toque el login, no en Fase 2. |
| **Hash de contraseña** | `org.mindrot:jbcrypt:0.4` (BCrypt). Obligatorio cuando se implemente el acceso. |
| **Copia de seguridad** | Botón en la UI que copie el archivo `.db` con `JFileChooser` **+** procedimiento documentado. Fase posterior. |
| **Datos ficticios** | Script SQL en `demo/` ejecutado manualmente; **nunca** se cargan solos al arrancar. |
| **Apariencia** | **Look and feel del sistema** (`UIManager.getSystemLookAndFeelClassName()`). **No** FlatLaf. Igual se respeta la paleta del documento en los componentes que se estilicen. |
| **Git** | Repositorio git inicializado con commit inicial. Trabaja por incrementos y commitea cambios coherentes. |
| **Plazo** | 8 jornadas completas; hay margen, pero prioriza un flujo funcional y fiable sobre detalles visuales. |

---

## 4. Alcance concreto de la Fase 2 — Asociados

**Objetivo:** un usuario puede registrar, buscar, consultar y editar asociados desde Swing, con validaciones y persistencia real en SQLite.

### Paquetes y clases (nombres obligatorios)

```
com.lasferias.cooperativa
├── App.java                     (modificar: inicializar BD y abrir la ventana de Asociados)
├── model/Asociado.java
├── dao/AsociadoDAO.java
├── service/AsociadoService.java
├── ui/VentanaAsociados.java     (JFrame con búsqueda + JTable)
├── ui/FormularioAsociado.java   (JDialog de alta/edición)
└── database/ (sin cambios)
```

### Modelo `Asociado`

Campos: `id` (long, 0 = nuevo), `documento`, `nombres`, `apellidos`, `telefono` (puede ser nulo o vacío), `fechaRegistro` (`LocalDate`). Constructores y getters/setters. Sin lógica de negocio en el modelo.

### `AsociadoDAO` (JDBC puro, sin ORM)

- `crear(Asociado)`, `actualizar(Asociado)`, `buscarPorDocumento(String)`, `buscarPorTexto(String)` (busca en documento, nombres o apellidos con `LIKE`), `listar()`, `existeDocumento(String, long idExcluir)`.
- **Todas** las consultas con `PreparedStatement` paramétrizado, `try-with-resources`, sin SQL concatenado.
- El DAO debe poder apuntar a **una ruta de BD arbitraria** (por ejemplo, constructor con `Path` que use `ConexionBD.obtenerConexion(ruta)`; por defecto `ConexionBD.rutaBaseDatos()`). Las pruebas lo necesitan para usar `@TempDir`.
- **No** implementes `eliminar(...)`: está prohibido por el documento.

### `AsociadoService` (validaciones con mensajes en español)

- Documento: obligatorio, sin espacios sobrantes y **único** (consulta al DAO antes de insertar o actualizar).
- Nombres y apellidos: obligatorios.
- Teléfono: opcional; si viene, sin letras.
- `fechaRegistro`: **se asigna automáticamente** con la fecha del alta (`LocalDate.now()`) y se conserva al editar. *Supuesto documentado: no se edita a mano en la Fase 2; si el usuario pide lo contrario, se ajusta.*
- Los errores se comunican con `IllegalArgumentException` (o una excepción de validación propia) con mensaje claro para mostrar en un `JOptionPane`.
- La unicidad también está respaldada por el `UNIQUE` de la BD.

### UI (Swing, sin FlatLaf)

- Ventana principal: campo de búsqueda por documento o texto, `JTable` no editable con Documento, Nombres, Apellidos, Teléfono y Fecha de registro; botones **Nuevo**, **Editar** y **Actualizar lista**.
- Formulario en `JDialog` modal con etiquetas claras; alta y edición comparten el formulario.
- Mensajes de error comprensibles, en español; un dato inválido nunca debe cerrar la aplicación.
- Nada de eliminar. Nada de animaciones. Legible a 1024×768.
- Al arrancar: `App.main` → `InicializadorBD.inicializar()` → abrir la ventana dentro de `SwingUtilities.invokeLater(...)`. Mantén funcionando `mvn exec:java`.

### Pruebas obligatorias de la Fase 2 (JUnit 5 + `@TempDir`)

1. Registrar un asociado válido y recuperarlo por documento.
2. Intentar registrar un **documento duplicado** → debe fallar con mensaje claro.
3. Buscar por documento existente y por documento inexistente (sin excepción).
4. Editar un asociado y comprobar que el cambio persiste y el `id` no cambia.
5. Validaciones: documento vacío, nombres vacíos, teléfono con letras.
6. Persistencia: cerrar y reabrir la conexión y verificar que el registro sigue ahí.

Nunca escribas en `%APPDATA%` desde las pruebas.

---

## 5. Reglas de negocio que NO se pueden romper (todo el MVP)

- **Nada de cálculos financieros:** sin intereses, amortización, saldo pendiente, valor de cuota calculado ni agregaciones de montos.
- **Nada de borrar registros:** ni asociados, ni préstamos, ni cuotas, ni pagos. Solo editar.
- **Cuotas con pagos asociados:** no se puede editar número, valor ni fecha de vencimiento (se implementa en su fase).
- **Pagos:** se editan directamente sobre el registro; **sin historial de versiones ni auditoría**.
- **Abonos parciales permitidos**; valores nulos, cero o negativos rechazados.
- **Contraseña siempre con hash** (nunca en texto plano) cuando se implemente el login.
- **FK activas** en cada conexión, consultas parametrizadas y transacciones cuando haya varias escrituras relacionadas.
- **No inventar** campos, tablas, estados ni validaciones fuera del documento.
- La BD vive en `%APPDATA%\CooperativaLasFerias`, nunca en `target/` ni en carpetas temporales. No la subas al repositorio.

---

## 6. Convenciones del proyecto

- Paquete base `com.lasferias.cooperativa`; **identificadores, comentarios y mensajes de UI en español** (así está la Fase 1: `ConexionBD`, `rutaBaseDatos`).
- JDBC directo con `try-with-resources`; nada de frameworks ni ORM.
- Dependencias nuevas: solo con justificación y autorización del usuario. (Prevista más adelante: `org.mindrot:jbcrypt:0.4`.)
- No cambies el DDL existente ni los nombres de columnas; el mapeo a Java es `snake_case` → `camelCase`.
- Commits en español, pequeños y con mensaje claro (`Fase 2: ...`).

---

## 7. Cómo trabajar (obligatorio)

1. `git status` y revisa el árbol antes de editar.
2. Implementa en incrementos: modelo → DAO → pruebas DAO → servicio → pruebas servicio → UI.
3. Ejecuta `mvn clean compile` y `mvn test` tras cada incremento relevante; no afirmes que algo funciona sin verlo.
4. Al terminar, ejecuta también `mvn exec:java` y comprueba que la ventana abre y que la BD real no se corrompe.
5. Reporta: archivos creados o modificados, comandos ejecutados con su salida real, cuántas pruebas pasan, decisiones tomadas y pendientes o limitaciones. Si algo no se implementó, dilo explícitamente (el documento lo exige).

### Definición de terminado para la Fase 2

- `mvn clean compile` → BUILD SUCCESS.
- `mvn test` → todas las pruebas verdes (las 2 de Fase 1 + las nuevas de Asociados).
- `mvn exec:java` → abre la ventana de Asociados sin excepciones en consola.
- Flujo manual probado: crear asociado → aparece en la tabla → buscarlo por documento → editarlo → reabrir la app y verlo igual.
- Un documento duplicado se rechaza con mensaje visible.
- Nada de botones ni menús de eliminación.

---

## 8. Trampas conocidas del entorno

- **JDK 25:** al cargar la DLL de SQLite aparece `WARNING: A restricted method in java.lang.System has been called`. Es inocuo (las pruebas pasan). No lo silencies cambiando el pom sin consultar.
- **PowerShell envuelve stderr** de `java` o `mvn` como `NativeCommandError`; es cosmético. Verifica el código real con `$LASTEXITCODE`.
- **`JAVA_HOME` vacío**: Maven usa el Java del `PATH`. No configures variables de sistema.
- `.github/modernize/` es basura de una extensión de VS Code, **no es parte del MVP**; está excluida vía `.git/info/exclude`. No la borres ni la toques.
- No instales herramientas globales ni ejecutes comandos destructivos.

---

## 9. Qué sigue después de la Fase 2 (contexto, no lo implementes todavía)

Login con pantalla de creación del administrador y jBCrypt → Préstamos → Cuotas → Pagos con abonos parciales y edición directa → Historial por asociado → Copia de seguridad desde la UI → Documentación (README, procedimiento de respaldo, limitaciones conocidas).
