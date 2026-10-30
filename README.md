# Cooperativa Las Ferias — MVP de gestión de asociados y préstamos

Aplicación de escritorio **offline** (Java + Swing + SQLite) para registrar y consultar asociados, préstamos, cuotas y pagos de la cooperativa vinculada a la Plaza de Mercado de Las Ferias, en Bogotá. Se desarrolla como parte de una práctica social universitaria.

La aplicación **complementa** a Helisa; no lo reemplaza ni se integra con él, y **no realiza cálculos financieros**: todos los valores (monto, valor de cuota, pagos) se registran manualmente.

> - Documento funcional de referencia: [docs/MVP-Cooperativa-Las-Ferias-v3.md](docs/MVP-Cooperativa-Las-Ferias-v3.md)
> - Guía de trabajo para el agente de programación: [AGENTS.md](AGENTS.md)

## Estado actual

| Fase | Contenido | Estado |
| --- | --- | --- |
| 1 | Estructura Maven, conexión SQLite, esquema inicial y arranque verificado | Terminada |
| 2 | Módulo Asociados (modelo, DAO, servicio, pantalla, pruebas) | Terminada |
| 2.5 | Extracción de estilos compartidos de la interfaz (`ui/Estilo`) | Terminada |
| 3 | Inicio de sesión y creación del administrador (BCrypt) | Terminada |
| 4 | Préstamos (modelo, DAO, servicio, pantalla, formulario y navegación) | Terminada |
| 5 | Cuotas (modelo, DAO, servicio, pantalla y formulario) | Terminada |
| 6 | Pagos (modelo, DAO, servicio, pantalla y formulario) | Terminada |
| 7 | Historial por asociado (consulta) | Terminada |
| 8 | Copia de seguridad desde la interfaz | Terminada |
| 9 | Datos de demostración y documentación final | Terminada |

**Implementado hoy:** alta, búsqueda, consulta y edición de asociados; inicio de sesión y creación del administrador inicial con hash BCrypt; módulo de Préstamos con navegación principal, registro, consulta y edición; módulo de Cuotas con navegación, registro, consulta y edición; módulo de Pagos con navegación, registro, consulta y edición; historial por asociado con tres tablas encadenadas; y copia de seguridad desde la barra superior, sin cálculos financieros ni borrado. **139 pruebas automatizadas.** El recorrido completo ya se validó manualmente por el tester sobre una base de datos temporal.

**Todo lo previsto está implementado.** Las limitaciones asumidas se detallan más abajo. Detalle de la Fase 4: [docs/fase-4-prestamos.md](docs/fase-4-prestamos.md). Detalle de la Fase 5: [docs/fase-5-cuotas.md](docs/fase-5-cuotas.md). Detalle de la Fase 6: [docs/fase-6-pagos.md](docs/fase-6-pagos.md). Detalle de la Fase 7: [docs/fase-7-historial.md](docs/fase-7-historial.md). Detalle de la Fase 8: [docs/fase-8-copia.md](docs/fase-8-copia.md).

## Requisitos

- **JDK 17 o superior** (desarrollado con JDK 25, compilando a `release 17`).
- **Apache Maven 3.9+**.
- **Windows** (la ruta de datos usa el perfil del usuario en `AppData`).

## Compilar, probar y ejecutar

Desde la carpeta del proyecto, en la terminal de VS Code (PowerShell):

```
mvn clean compile   # compila
mvn test            # pruebas JUnit 5 (esquema, asociados, autenticación, préstamos, cuotas, pagos, historial y copia: 139 en total)
mvn exec:java       # inicializa la base de datos y abre la aplicación
```

En la primera compilación Maven descarga las dependencias, por lo que necesita internet **solo ese momento**; después la aplicación funciona sin conexión. Al usar JDK 25 puede aparecer la advertencia `A restricted method in java.lang.System has been called` al cargar SQLite: es inocua.

Las pruebas usan bases de datos temporales; **nunca** tocan la base de datos real.

## Base de datos

SQLite, guardada en el perfil del usuario de Windows:

```
%USERPROFILE%\AppData\Roaming\CooperativaLasFerias\cooperativa.db
```

Se crea automáticamente en el primer arranque (`CREATE TABLE IF NOT EXISTS`), de modo que volver a iniciar la aplicación no duplica estructura ni datos. No se cargan datos ficticios al arrancar.

| Tabla | Contenido |
| --- | --- |
| `usuario` | Credenciales del administrador (contraseña almacenada como hash BCrypt). |
| `asociado` | Documento único, nombres, apellidos, teléfono, fecha de registro. |
| `prestamo` | Monto, fecha, plazo, número de cuotas, valor de cuota, estado (Vigente/Finalizado) y FK a asociado. |
| `cuota` | Número (único dentro de su préstamo), valor, fecha de vencimiento y FK a préstamo. |
| `pago` | Fecha, valor, número de recibo y FK a cuota. |

Fechas en texto ISO (`YYYY-MM-DD`); montos en pesos colombianos enteros (`INTEGER`). Las claves foráneas se activan en cada conexión y las restricciones (`UNIQUE`, `CHECK`, `FOREIGN KEY`) protegen la integridad además de las validaciones del código.

> El esquema incluye además seis *triggers* que impiden borrar registros desde fuera de la
> aplicación (`asociado`, `prestamo`, `cuota`, `pago` y `usuario`) y que bloquean la actualización de
> una cuota que ya tiene pagos. Una base creada antes de esta versión los recibe en el siguiente
> arranque, una sola vez: el archivo cambia en ese arranque y a partir de ahí queda estable.

## Configuración del administrador

En el primer arranque, si la tabla `usuario` está vacía, la aplicación pide **contraseña y confirmación**. El nombre de usuario no se pregunta: se guarda siempre como `admin`. La contraseña debe tener **6 caracteres o más** y se almacena con **BCrypt**; nunca en texto plano. Solo existe un administrador. **No hay cambio ni recuperación de contraseña**: si se olvida, hay que restaurar una copia de seguridad o recrear la base de datos.

## Datos de demostración

`demo/demo.sql` contiene datos **ficticios** para una demostración: 5 asociados, 6 préstamos (4 Vigentes y 2 Finalizados), 15 cuotas y 13 pagos, con dos abonos parciales en la cuota 5 y un recibo repetido en cuotas distintas. Se carga **a mano** y **nunca** al arrancar; no inserta el administrador.

Con la aplicación cerrada, desde la carpeta del proyecto y sobre una base ya creada y **sin datos** (el script no toca la tabla `usuario`):

```
sqlite3 "%USERPROFILE%\AppData\Roaming\CooperativaLasFerias\cooperativa.db" ".read demo/demo.sql"
```

En PowerShell, usa `$env:USERPROFILE` en lugar de `%USERPROFILE%`. También sirve cualquier cliente SQLite que ejecute el archivo. Si la base es nueva, ábrela una vez para crear el esquema y el administrador, ciérrala y luego carga la demo. Debe ejecutarse sobre una base **sin datos**: el script fija los identificadores (1, 2, 3…) y una segunda ejecución fallará por clave duplicada; eso es esperado.

Para empezar de cero: cierra la aplicación, borra `cooperativa.db` y vuelve a abrirla; se recrea vacía y pide crear el administrador.

## Arquitectura

Capas simples, sin frameworks: `ui` → `service` → `dao` → `database`, con `model` compartido. Cada operación de base de datos abre y cierra su propia conexión; no existe una conexión global.

```
pom.xml
AGENTS.md
README.md
docs/
├── MVP-Cooperativa-Las-Ferias-v3.md
├── prompt-fase-2.md
├── fase-4-prestamos.md
├── fase-5-cuotas.md
├── fase-6-pagos.md
├── fase-7-historial.md
├── fase-8-copia.md
└── diseno/home-mockup.html
demo/
└── demo.sql
src/main/java/com/lasferias/cooperativa/
├── App.java
├── database/  (ConexionBD, InicializadorBD)
├── model/     (Asociado, Prestamo, Cuota, Pago, Usuario)
├── dao/       (AsociadoDAO, PrestamoDAO, CuotaDAO, PagoDAO,
                HistorialDAO, CopiaSeguridadDAO, UsuarioDAO)
├── service/   (AsociadoService, AutenticacionService, PrestamoService,
                CuotaService, PagoService, HistorialService,
                CopiaSeguridadService)
└── ui/        (Estilo, VentanaPrincipal, PanelAsociados, PanelPrestamos,
                PanelCuotas, PanelPagos, PanelHistorial, FormularioAsociado,
                FormularioPrestamo, FormularioCuota, FormularioPago, VentanaLogin,
                VentanaCreacionAdministrador)
src/test/java/com/lasferias/cooperativa/
├── database/InicializadorBDTest.java
├── dao/       (AsociadoDAOTest, PrestamoDAOTest, CuotaDAOTest,
                PagoDAOTest, HistorialDAOTest, CopiaSeguridadDAOTest,
                UsuarioDAOTest)
├── service/   (AsociadoServiceTest, AutenticacionServiceTest,
                PrestamoServiceTest, CuotaServiceTest, PagoServiceTest,
                HistorialServiceTest, CopiaSeguridadServiceTest)
└── ui/        (FormularioAsociadoTest, FormularioPrestamoTest,
                FormularioCuotaTest, FormularioPagoTest, PanelAsociadosTest,
                PanelPrestamosTest, PanelCuotasTest, PanelPagosTest,
                PanelHistorialTest)
```

## Reglas del producto (resumen)

- Sin cálculos financieros automáticos: el valor de cuota y los pagos se registran manualmente; no hay intereses, amortizaciones ni saldos pendientes.
- No se elimina ningún registro (asociados, préstamos, cuotas ni pagos); solo se editan.
- Una cuota con pagos asociados no puede modificar su número, valor ni fecha de vencimiento.
- Los pagos se corrigen editando el registro directamente, **sin historial de versiones ni auditoría**.
- Se admiten abonos parciales; se rechazan pagos con valor nulo, cero o negativo.
- Para pruebas y demostraciones se usan **solo datos ficticios**.
- Todo lo previsto está implementado; las limitaciones conocidas se detallan al final.

## Guion de demostración

1. **Login** y recorrido por **Asociados**.
2. **Préstamos:** consulta y edición de un préstamo.
3. **Cuotas:** editar una cuota sin pagos e intentar editar una con pagos para mostrar el bloqueo.
4. **Pagos:** registrar un abono parcial y editar un pago.
5. **Historial:** elegir un asociado y comprobar las tres tablas encadenadas.
6. **Copia de seguridad:** crear una copia y mostrar el procedimiento de restauración.

## Copia de seguridad y recuperación

> **Crear la copia.** Con la aplicación abierta, pulsa **«Copia de seguridad»** en la barra superior y
> elige dónde guardarla; el nombre sugerido incluye la fecha (`cooperativa-AAAA-MM-DD.db`). Si eliges
> un archivo que ya existe, la aplicación pide confirmación antes de reemplazarlo. La copia se genera
> con `VACUUM INTO`, de modo que es consistente aunque haya datos recién guardados.
>
> **Restaurar la copia.** La recuperación es manual y **con la aplicación cerrada**:
>
> 1. Cierra la aplicación.
> 2. Abre `%USERPROFILE%\AppData\Roaming\CooperativaLasFerias\`.
> 3. Renombra `cooperativa.db` a `cooperativa-antes.db` (no lo borres todavía).
> 4. Copia tu copia de seguridad en esa carpeta y renómbrala a `cooperativa.db`.
> 5. Abre la aplicación y verifica los datos. Si algo no cuadra, cierra y devuelve el nombre al archivo
>    `cooperativa-antes.db`.
>
> La copia contiene la base completa: administrador, asociados, préstamos, cuotas, pagos e historial.
> Restaurar una copia **reemplaza todo lo registrado después de esa copia**: no hay fusión de datos.

## Limitaciones conocidas

Las siguientes limitaciones están asumidas por diseño:

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
- En la pestaña Historial, las tres tablas reparten la altura a partes iguales, así que con varios pagos hay que desplazarse para verlos todos. Cosmético.
- Al sobrescribir una copia de seguridad, el archivo anterior se borra antes de generar el nuevo; si la generación fallara, hay que repetirla. La base en uso no se ve afectada.
- No hay pantalla de inicio (Home) con tarjetas de acceso, como describe el documento en §7: la navegación se resuelve con la barra superior de pestañas y cubre los cinco módulos. Fue un recorte deliberado para priorizar el flujo funcional.