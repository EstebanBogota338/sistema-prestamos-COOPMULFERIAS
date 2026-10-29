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

## 2. Estado actual: Fases 1, 2, 2.5 y 3 terminadas

**No las rehagas.** Commits: Fase 1 `ade0cf2`; Fase 2 base `3e045fb`; ronda de calidad `7b45498`.

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
| [ui/VentanaAsociados.java](src/main/java/com/lasferias/cooperativa/ui/VentanaAsociados.java) | Barra superior, búsqueda, `JTable` no editable, conteo y acciones |
| [ui/FormularioAsociado.java](src/main/java/com/lasferias/cooperativa/ui/FormularioAsociado.java) | `JDialog` modal compartido para alta y edición |
| [ui/Estilo.java](src/main/java/com/lasferias/cooperativa/ui/Estilo.java) | Paleta, fuente, botón primario/secundario y borde de campo compartidos (Fase 2.5) |
| [App.java](src/main/java/com/lasferias/cooperativa/App.java) | Inicializa la BD, aplica el look and feel del sistema y abre la ventana en el EDT; `System.exit(1)` si falla la BD |

**Pruebas: 49** (JUnit 5 + `@TempDir`) — 33 de las fases 1–2.5 (`AsociadoDAOTest` 15, `AsociadoServiceTest` 11, `InicializadorBDTest` 4, `FormularioAsociadoTest` 2, `VentanaAsociadosTest` 1) y 16 de la Fase 3 (`AutenticacionServiceTest` 12, `UsuarioDAOTest` 4). Los reportes de surefire de la última ejecución muestran 0 fallos.

**Decisiones de la ronda de calidad (no están en el documento; documéntalas, no las amplíes):** documento guardado en mayúsculas con unicidad insensible a mayúsculas en servicio y DAO (`COLLATE NOCASE`); búsqueda por documento, nombres y apellidos ignorando acentos y comodines (filtrado en Java); orden alfabético con `Collator` es-CO; diagnóstico con `java.util.logging`.

### Fase 3 — login y administrador inicial

| Archivo | Contenido |
| --- | --- |
| [model/Usuario.java](src/main/java/com/lasferias/cooperativa/model/Usuario.java) | `id` (long), `nombreUsuario`, `contrasenaHash`; nunca transporta la contraseña en claro |
| [dao/UsuarioDAO.java](src/main/java/com/lasferias/cooperativa/dao/UsuarioDAO.java) | JDBC con `Path`; `crear`, `obtenerAdministrador` → `Optional<Usuario>`, `existeAlgunUsuario`. Sin actualizar ni eliminar |
| [service/AutenticacionService.java](src/main/java/com/lasferias/cooperativa/service/AutenticacionService.java) | `NOMBRE_ADMINISTRADOR = "admin"`; `debeCrearAdministrador`, `crearAdministrador(contrasena, confirmacion)` con BCrypt y `autenticar(contrasena)` |
| [ui/VentanaLogin.java](src/main/java/com/lasferias/cooperativa/ui/VentanaLogin.java) | Login con `JPasswordField`, botón **Ingresar** por defecto; cerrar la ventana cierra la app |
| [ui/VentanaCreacionAdministrador.java](src/main/java/com/lasferias/cooperativa/ui/VentanaCreacionAdministrador.java) | Contraseña y confirmación; al crear el administrador abre el login |
| [ui/Estilo.java](src/main/java/com/lasferias/cooperativa/ui/Estilo.java) | `Estilo.campoPassword(JPasswordField)` (solo acepta `JPasswordField`) |

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
mvn test            → Failures: 0, Errors: 0 (33 pruebas previas + las que agregue cada fase)
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
| **Fases 1 y 2** | Terminadas. |
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
| P1 | **Triggers de SQLite** para "no eliminar" (en `asociado`, `prestamo`, `cuota`, `pago`) y para el bloqueo de edición de cuotas con pagos. Cambio aditivo e idempotente (`CREATE TRIGGER IF NOT EXISTS`), verificado en pruebas manuales con el DDL real. | No agregar triggers ni tocar el DDL. La regla se protege en el servicio. |
| P2 | **¿Se pueden cambiar vínculos al editar?** (`pago.cuota_id`, `cuota.prestamo_id`, `prestamo.asociado_id`). Sin eliminación, un vínculo mal puesto sería irreparable. Recomendación del arquitecto: no permitirlo y documentarlo como limitación. | *Default provisional:* los vínculos se fijan al crear y los formularios de edición no los modifican. Confirmar con el usuario **antes de implementar Pagos**. |
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

**Pendiente para la Fase 3:** si hacen falta campos de contraseña, agregar un helper en `Estilo` sin modificar `VentanaAsociados` ni `FormularioAsociado`.

---

## 7. Fase 3 — Login y administrador inicial (terminada)

- Login: pide solo la contraseña.
- Usuario: siempre `admin` (constante del servicio).
- Al crear el administrador se abre el login.

Siguiente tarea: Préstamos (ver sección 12).

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

---

## 10. Cómo trabajar (obligatorio)

1. `git -c safe.directory="D:/Documentos/programming/sistema-prestamos-COOPMULFERIAS" status` y revisa el árbol antes de editar. Conserva cambios ajenos a tu tarea.
2. Incrementos: modelo → DAO → pruebas DAO → servicio → pruebas servicio → UI → integración con `App`.
3. Ejecuta `mvn clean compile` y `mvn test` tras cada incremento. **No afirmes que algo funciona sin haberlo ejecutado.**
4. Al terminar, `mvn exec:java` y comprueba el flujo real. Si no pudiste ver la UI, dilo.
5. Reporta: archivos creados o modificados, dependencias, comandos con su **salida real**, cuántas pruebas pasan, decisiones tomadas, limitaciones y cualquier diferencia respecto a este archivo.
6. Si algo contradice el documento o este archivo, o falta un dato imprescindible, detente y pregunta con una pregunta concreta.

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
3. **Triggers de SQLite** — solo si el usuario autoriza P1.
4. **Préstamos:** introduce `VentanaPrincipal` (barra superior y navegación por paneles) y convierte `VentanaAsociados` en `PanelAsociados`, manteniendo las pruebas en verde. Préstamo vinculado a un asociado existente; `plazo` texto libre; `estado` Vigente/Finalizado manual; `valor_cuota` manual.
5. **Cuotas:** antes de empezar, crear un helper `database/ErroresBD` (p. ej. `esViolacionUnica(SQLException)`) con su prueba y usarlo también en `AsociadoService`, en vez de copiar la comparación por texto del mensaje de error. Bloqueo de edición con pagos en el servicio.
6. **Pagos:** primero resolver P2 con el usuario. Edición directa en transacción, abonos parciales, sin historial de versiones.
7. **Historial por asociado:** consultas con `JOIN`, sin duplicar registros, estado actual de cada pago, sin totales ni saldos.
8. **Copia de seguridad desde la UI:** `JFileChooser` + `VACUUM INTO` (copia consistente; **falla si el archivo destino ya existe**, así que hay que confirmar la sobrescritura o usar un nombre nuevo). Procedimiento de recuperación documentado: con la aplicación cerrada, reemplazar el archivo de la BD.
9. **Datos de demostración** (`demo/`, script SQL manual) y **documentación final** (README, procedimiento de respaldo, limitaciones).

Recortes si el tiempo aprieta (en este orden): cambio de contraseña, Home con tarjetas de acceso, extras de búsqueda. Lo demás es indispensable por los criterios de aceptación del documento.

### Limitaciones conocidas (documentar en el README final)

- La edición de pagos **no conserva versiones anteriores** ni implementa auditoría (decisión cerrada del documento).
- No hay cambio de contraseña (si no llega a implementarse) ni bloqueo por intentos fallidos.
- jBCrypt 0.4 no tiene mantenimiento activo y solo considera los primeros 72 bytes de la contraseña; es adecuado para este alcance.
- `plazo` es texto libre, sin unidad definida.
- Si se adopta P2 como se recomienda, un registro creado bajo un padre equivocado no puede moverse ni eliminarse.
- La unicidad del documento en SQLite distingue mayúsculas; el servicio normaliza a mayúsculas antes de guardar.
- La ruta de la BD se arma con `user.home\AppData\Roaming`, no con la variable `%APPDATA%`; pueden diferir si el perfil está redirigido.
- No hay cambio ni recuperación de contraseña: si se olvida, solo se recupera restaurando una copia de seguridad anterior o recreando la base de datos.
- `Estilo.campoPassword` solo acepta `JPasswordField`: al llegar Préstamos, generalizar a `Estilo.campo(JTextField)`.