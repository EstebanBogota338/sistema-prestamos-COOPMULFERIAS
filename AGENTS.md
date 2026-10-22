# AGENTS.md — Contexto y reglas para el agente coder

> **Lee este archivo completo y el documento de planeación antes de tocar código.**
> Aquí están el estado real del proyecto, las decisiones ya tomadas por el usuario y el alcance de la siguiente fase. No repitas decisiones que aquí ya están cerradas.

---

## 1. Qué es este proyecto

Herramienta de escritorio **offline** para la cooperativa vinculada a la Plaza de Mercado de Las Ferias: administrar asociados, préstamos, cuotas y pagos.

- **Stack:** Java (release 17), Swing, SQLite, JDBC puro, Maven.
- **Documento de referencia (fuente de verdad funcional):** [docs/MVP-Cooperativa-Las-Ferias-v3.md](docs/MVP-Cooperativa-Las-Ferias-v3.md) — 372 líneas, léelo entero.
- **Boceto visual del usuario:** [docs/diseno/home-mockup.html](docs/diseno/home-mockup.html) — referencia de navegación, disposición y paleta. La sección 4 explica qué sí y qué no se porta a Swing.
- **Directorio de trabajo:** `D:\Documentos\programming\sistema-prestamos-COOPMULFERIAS`
- **No** reemplaza Helisa. **No** hace cálculos financieros. **No** borra registros.

---

## 2. Estado actual: Fases 1 y 2 terminadas y verificadas

Las dos primeras fases están implementadas, probadas y commiteadas. **No las rehagas.**

### Fase 1 — estructura, esquema y arranque (commit `ade0cf2`)

| Archivo | Contenido |
| --- | --- |
| [pom.xml](pom.xml) | Empaquetado `jar`, `com.lasferias:cooperativa-las-ferias:1.0-SNAPSHOT`, `maven.compiler.release=17`, UTF-8 |
| [ConexionBD.java](src/main/java/com/lasferias/cooperativa/database/ConexionBD.java) | Conexiones SQLite sin conexión global; `PRAGMA foreign_keys = ON` en **cada** conexión |
| [InicializadorBD.java](src/main/java/com/lasferias/cooperativa/database/InicializadorBD.java) | DDL idempotente (`CREATE TABLE/INDEX IF NOT EXISTS`) |
| [.gitignore](.gitignore) | `target/`, `.classpath`, `.project`, `.settings/`, `*.class`, `.vscode/` |

### Fase 2 — módulo Asociados (commit `3e045fb`, +1.557 líneas)

| Archivo | Contenido |
| --- | --- |
| [model/Asociado.java](src/main/java/com/lasferias/cooperativa/model/Asociado.java) | `id`, `documento`, `nombres`, `apellidos`, `telefono`, `fechaRegistro` (`LocalDate`) |
| [dao/AsociadoDAO.java](src/main/java/com/lasferias/cooperativa/dao/AsociadoDAO.java) | JDBC parametrizado; constructor con `Path`; crear/actualizar/buscarPorDocumento/buscarPorTexto/listar/existeDocumento; **sin eliminar** |
| [service/AsociadoService.java](src/main/java/com/lasferias/cooperativa/service/AsociadoService.java) | Normalización, validaciones, unicidad y fecha automática |
| [ui/VentanaAsociados.java](src/main/java/com/lasferias/cooperativa/ui/VentanaAsociados.java) | Barra superior, búsqueda, `JTable` no editable, conteo y acciones |
| [ui/FormularioAsociado.java](src/main/java/com/lasferias/cooperativa/ui/FormularioAsociado.java) | `JDialog` modal compartido para alta y edición |
| [App.java](src/main/java/com/lasferias/cooperativa/App.java) | Inicializa BD, aplica el look and feel del sistema y abre la ventana en el EDT; `System.exit(1)` si falla la BD |
| Tests | `AsociadoDAOTest` (6), `AsociadoServiceTest` (7), `InicializadorBDTest` ampliado (4) → **17 pruebas** |

Verificación ejecutada y repetida: `mvn clean compile` (8 clases, release 17), `mvn test` (**17 pruebas, 0 fallos**) y `mvn exec:java` (abre "Cooperativa Las Ferias - Asociados", 1000×680, sin excepciones). El recorrido manual de clics del usuario confirmó alta, búsqueda, edición conservando ID y fecha, rechazo de duplicado y persistencia al reabrir.

### Dependencias (versiones finales, ya resueltas contra Maven Central)

- `org.xerial:sqlite-jdbc:3.49.1.0` (scope default)
- `org.junit.jupiter:junit-jupiter:5.11.4` (scope test)
- `maven-surefire-plugin:3.5.4`
- `org.codehaus.mojo:exec-maven-plugin:3.1.0` con `<mainClass>com.lasferias.cooperativa.App</mainClass>`
- **Aún no instalada** (autorizada solo para la Fase 3): `org.mindrot:jbcrypt:0.4`

### Entorno verificado

- `java -version` → **openjdk 25.0.2 LTS (Temurin)**
- `mvn -version` → **Apache Maven 3.9.16**, runtime JDK 25.0.2, plataforma UTF-8, Windows 11
- `maven.compiler.release` = **17** (se compila con JDK 25 pero bytecode 17)
- `JAVA_HOME` está **vacío**; Maven encuentra Java por `PATH`.
- Caché local en `C:\Users\Casa\.m2` con todas las dependencias descargadas.
- **El árbol pertenece a `BUILTIN\Administradores`**, así que `git` a secas falla con *dubious ownership*. Usa `git -c safe.directory="D:/Documentos/programming/sistema-prestamos-COOPMULFERIAS" …` o pide permiso para añadir la excepción a la configuración global.

### Comandos que deben seguir funcionando

```
mvn clean compile   → BUILD SUCCESS (8 clases)
mvn test            → Tests run: 17, Failures: 0, Errors: 0
mvn exec:java       → abre la ventana de Asociados
```

**Ruta efectiva de la BD:** `C:\Users\Casa\AppData\Roaming\CooperativaLasFerias\cooperativa.db` (`user.home\AppData\Roaming\CooperativaLasFerias\cooperativa.db`). Verificada con `PRAGMA integrity_check = ok`.

### Esquema (no lo cambies sin autorización)

- `usuario(id, nombre_usuario UNIQUE, contrasena_hash)`
- `asociado(id, documento UNIQUE, nombres, apellidos, telefono, fecha_registro)`
- `prestamo(id, asociado_id FK, monto>0, fecha, plazo, numero_cuotas>0, valor_cuota>0, estado IN ('Vigente','Finalizado'))`
- `cuota(id, prestamo_id FK, numero_cuota, valor>0, fecha_vencimiento, UNIQUE(prestamo_id, numero_cuota))`
- `pago(id, cuota_id FK, fecha_pago, valor>0, numero_recibo)`
- Índices: `idx_prestamo_asociado`, `idx_cuota_prestamo`, `idx_pago_cuota`

Fechas `TEXT` ISO `YYYY-MM-DD`; montos `INTEGER` (pesos). La tabla `usuario` ya existe y está vacía: la Fase 3 la usa tal cual.

---

## 3. Decisiones cerradas por el usuario (no re-preguntar)

| Tema | Decisión |
| --- | --- |
| **Fase 2** | Módulo Asociados completo — **terminada**. |
| **Fase 3 (ahora)** | **Login + pantalla de creación del administrador en el primer arranque.** No se tocan préstamos, cuotas, pagos ni dashboard. |
| **Administrador inicial** | Si la tabla `usuario` está vacía, la app pide crear el administrador antes de dejar entrar. |
| **Formulario de acceso** | Pide **solo la contraseña**; el usuario administrador es único. |
| **Hash de contraseña** | `org.mindrot:jbcrypt:0.4` (BCrypt). Obligatorio; nunca texto plano. |
| **Política de contraseña** | *Supuesto mínimo:* obligatoria y de 6 caracteres o más, con confirmación en la creación. Si el usuario define otra regla, prevalece la suya. |
| **Sesión** | Sin "cerrar sesión", sin multiusuario ni permisos. |
| **Copia de seguridad** | Botón con `JFileChooser` + procedimiento documentado. Fase posterior. |
| **Datos ficticios** | Script SQL en `demo/` ejecutado manualmente; **nunca** al arrancar. |
| **Apariencia** | Look and feel del sistema. **No** FlatLaf. Paleta del documento aplicada a mano. |
| **Git** | Commits pequeños en español. No commitear sin pedirlo. |
| **Plazo** | 8 jornadas completas; prioriza flujo funcional sobre detalles visuales. |
| **fecha_registro** | Automática al crear y **no editable**; se conserva al editar. |
| **Teléfono** | Opcional; solo dígitos, entre 7 y 10 caracteres. |
| **UI / boceto** | Barra superior + paleta del mockup. **No** se muestran módulos inexistentes, saldos ni estados no documentados. |

---

## 4. Patrón establecido en la Fase 2 (reutilízalo)

Cada módulo nuevo sigue esta estructura: `model` (datos), `dao` (JDBC parametrizado con `Path` inyectable), `service` (validación + mensajes en español + traducción de `UNIQUE`), `ui` (pantalla + diálogo compartido).

- **DAO:** `try-with-resources`, `PreparedStatement` siempre, sin SQL concatenado con datos del usuario, sin métodos de borrado, cada operación abre y cierra su conexión.
- **Service:** normaliza (`trim`, teléfono vacío → `null`), lanza `IllegalArgumentException` con texto legible y asigna las reglas del sistema (fecha). Las excepciones de BD se traducen o se propagan como `SQLException`, nunca se muestran como éxito.
- **UI:** constantes de color, barra superior del mockup, tarjetas blancas con borde `#E2E5E3`, tabla no editable, formulario modal para alta y edición, mensajes de error junto a los botones, estado en la barra inferior.
- **Tests:** JUnit 5 con `@TempDir`, BD sembrada con `InicializadorBD.inicializar(ruta)`, DAO/service construidos con esa ruta. **Nunca** se usa la BD real.

### Paleta y medidas del boceto (aplicables a cualquier pantalla)

| Elemento | Valor |
| --- | --- |
| Fondo / superficie | `#F5F7F6` / `#FFFFFF` |
| Texto principal / secundario | `#212529` / `#6B7280` |
| Bordes | `#E2E5E3` (0.5–1 px) |
| Verde principal / hover | `#2F8F57` / `#256F45` |
| Radio | `8px` tarjetas |
| Barra superior | 52 px, blanca, borde inferior, título 15 px 500 |
| Títulos de tarjeta / etiquetas | 14 px 500 / 13 px 500 `#6B7280` |
| Botón primario | fondo `#2F8F57`, texto blanco |

No portar HTML, CSS, CDN ni iconos: la UI es Swing.

---

## 5. Alcance de la Fase 3 — Login y administrador inicial

**Objetivo:** al arrancar, la app exige credenciales. La primera vez guía la creación del administrador; después pide la contraseña. Con la contraseña correcta abre la ventana de Asociados ya existente.

### Flujo

1. `App.main` inicializa la BD (igual que ahora) y aplica el look and feel del sistema.
2. Si `usuario` está vacía → **VentanaCreacionAdministrador** (usuario sugerido `admin`, contraseña y confirmación).
3. Al crear → se guarda el hash y se continúa al login (o se entra directo; documenta lo que elijas).
4. Si ya hay administrador → **VentanaLogin** (solo contraseña).
5. Contraseña correcta → `new VentanaAsociados(new AsociadoService()).setVisible(true)`.
6. Contraseña incorrecta → mensaje claro, sin cerrar la app y sin revelar detalles internos.
7. Falla la BD → mensaje y `System.exit(1)` (comportamiento actual, no lo rompas).

### Clases (nombres obligatorios)

```
com.lasferias.cooperativa
├── App.java                      (modificar el flujo de arranque)
├── model/Usuario.java
├── dao/UsuarioDAO.java
├── service/AutenticacionService.java
├── ui/VentanaLogin.java
└── ui/VentanaCreacionAdministrador.java
```

### Modelo `Usuario`

`id` (long), `nombreUsuario`, `contrasenaHash`. **Nunca** guardes ni transportes la contraseña en claro en el modelo.

### `UsuarioDAO`

- `crear(Usuario)`, `obtenerAdministrador()` → `Optional<Usuario>`, `existeAlgunUsuario()`.
- Columnas exactas: `id, nombre_usuario, contrasena_hash`. Conexión con `ConexionBD.obtenerConexion(Path)` y constructor con ruta para los tests.
- Sin actualizar ni eliminar en esta fase (el cambio de contraseña está fuera de alcance).

### `AutenticacionService`

- `debeCrearAdministrador()` → tabla vacía.
- `crearAdministrador(nombreUsuario, contrasena, confirmacion)`: nombre obligatorio y único, contraseña de 6+ caracteres, confirmación idéntica; genera hash con `BCrypt.hashpw(contrasena, BCrypt.gensalt())`.
- `autenticar(contrasena)` → `boolean` comparando con `BCrypt.checkpw` contra el hash almacenado.
- Errores con `IllegalArgumentException` y mensajes en español. **Nunca** loguees contraseñas ni hashes.

### UI

- Mantén la barra superior y la paleta de la sección 4.
- Login: `JPasswordField`, botón **Ingresar** como botón por defecto, mensaje de error visible; cerrar la ventana cierra la app (no hay sesión que cerrar).
- Creación: usuario (por defecto `admin`), contraseña y repetir, mensajes de validación, botón **Crear administrador**.

### Dependencia autorizada

`org.mindrot:jbcrypt:0.4` (scope default). Es la **única** dependencia nueva permitida en esta fase; no toques las demás.

### Pruebas obligatorias (JUnit 5 + `@TempDir`)

1. Crear administrador → una fila en `usuario` y hash distinto de la contraseña (empieza por `$2a$`).
2. `autenticar` devuelve `true` con la contraseña correcta y `false` con otra.
3. Creación rechaza contraseña vacía, menor de 6, confirmación distinta y nombre vacío.
4. `debeCrearAdministrador` verdadero con BD vacía y falso tras crearlo.
5. Persistencia: otra instancia del DAO (nuevas conexiones) encuentra el administrador.
6. Login incorrecto no autentica y no rompe la BD.
7. Ninguna prueba escribe en `%APPDATA%`.

---

## 6. Reglas de negocio que NO se pueden romper (todo el MVP)

- **Nada de cálculos financieros:** sin intereses, amortización, saldo pendiente, valor de cuota calculado ni agregaciones de montos.
- **Nada de borrar registros:** ni usuarios, ni asociados, ni préstamos, ni cuotas, ni pagos. Solo editar.
- **Cuotas con pagos asociados:** no se puede editar número, valor ni fecha de vencimiento.
- **Pagos:** se editan directamente sobre el registro; **sin historial de versiones ni auditoría**.
- **Abonos parciales permitidos**; valores nulos, cero o negativos rechazados.
- **Contraseña siempre con BCrypt** (nunca texto plano, nunca en logs).
- **FK activas** en cada conexión, consultas parametrizadas y transacciones cuando haya varias escrituras relacionadas.
- **No inventar** campos, tablas, estados ni validaciones fuera del documento.
- La BD vive en `%APPDATA%\CooperativaLasFerias`, nunca en `target/` ni en temporales. No la subas al repositorio.

---

## 7. Convenciones del proyecto

- Paquete base `com.lasferias.cooperativa`; identificadores, comentarios y mensajes en español.
- JDBC directo con `try-with-resources`; nada de frameworks ni ORM.
- Dependencias nuevas: solo con justificación y autorización. En la Fase 3 la única es `jbcrypt`.
- No cambies el DDL existente ni los nombres de columnas; mapeo `snake_case` → `camelCase`.
- Commits en español, pequeños y claros (`Fase 3: ...`); no commitear sin pedido explícito.

---

## 8. Cómo trabajar (obligatorio)

1. `git -c safe.directory=… status` y revisa el árbol antes de editar.
2. Incrementos: modelo → DAO → pruebas DAO → servicio → pruebas servicio → UI → integración con `App`.
3. Ejecuta `mvn clean compile` y `mvn test` tras cada incremento; no afirmes que algo funciona sin verlo.
4. Al terminar, `mvn exec:java` y comprueba el flujo real: primer arranque pide crear administrador, login incorrecto falla, login correcto abre Asociados.
5. Reporta archivos, comandos con su salida real, pruebas (cuántas pasan), decisiones y limitaciones. Si no pudiste ver la UI, dilo.

### Definición de terminado para la Fase 3

- `mvn clean compile` → BUILD SUCCESS y `mvn test` → 17 pruebas previas + las nuevas, todas verdes.
- Con la tabla `usuario` vacía, la app pide crear el administrador y lo guarda con hash BCrypt.
- Login con contraseña incorrecta muestra error y no abre la app.
- Login correcto abre la ventana de Asociados existente.
- Al cerrar y reabrir, el administrador sigue existiendo y el login funciona.
- `System.exit(1)` ante fallo de BD intacto; sin datos ficticios; sin módulos nuevos a la vista.

---

## 9. Trampas conocidas del entorno

- **Propiedad del repo:** todo el árbol es de `BUILTIN\Administradores`; `git` sin `-c safe.directory=…` falla con *dubious ownership*. No cambies la configuración global sin permiso del usuario.
- **JDK 25:** al cargar la DLL de SQLite aparece `WARNING: A restricted method in java.lang.System has been called`. Es inocuo.
- **PowerShell envuelve stderr** de `java`/`mvn` como `NativeCommandError`; verifica con `$LASTEXITCODE`.
- **`JAVA_HOME` vacío:** Maven usa el Java del `PATH`. No configures variables de sistema.
- `.github/modernize/` es basura de una extensión de VS Code, excluida vía `.git/info/exclude`. No la toques.
- No instales herramientas globales ni ejecutes comandos destructivos.

---

## 10. Roadmap

Fase 3 (login + administrador) → Préstamos → Cuotas → Pagos con abonos parciales y edición directa → Historial por asociado → Copia de seguridad desde la UI → Documentación final (README, procedimiento de respaldo, limitaciones conocidas).
