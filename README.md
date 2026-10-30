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
| 7+ | Historial, copia de seguridad y datos de demostración | Pendiente |

**Implementado hoy:** alta, búsqueda, consulta y edición de asociados; inicio de sesión y creación del administrador inicial con hash BCrypt; módulo de Préstamos con navegación principal, registro, consulta y edición; módulo de Cuotas con navegación, registro, consulta y edición; y módulo de Pagos con navegación, registro, consulta y edición, sin cálculos financieros ni borrado. **120 pruebas automatizadas.** El recorrido completo ya se validó manualmente por el tester sobre una base de datos temporal.

**Aún no implementado** (no debe presentarse como operativo): historial, copia de seguridad y datos de demostración. Detalle de la Fase 4: [docs/fase-4-prestamos.md](docs/fase-4-prestamos.md). Detalle de la Fase 5: [docs/fase-5-cuotas.md](docs/fase-5-cuotas.md). Detalle de la Fase 6: [docs/fase-6-pagos.md](docs/fase-6-pagos.md).

## Requisitos

- **JDK 17 o superior** (desarrollado con JDK 25, compilando a `release 17`).
- **Apache Maven 3.9+**.
- **Windows** (la ruta de datos usa el perfil del usuario en `AppData`).

## Compilar, probar y ejecutar

Desde la carpeta del proyecto, en la terminal de VS Code (PowerShell):

```
mvn clean compile   # compila
mvn test            # pruebas JUnit 5 (esquema, asociados, autenticación, préstamos, cuotas y pagos: 120 en total)
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
└── diseno/home-mockup.html
src/main/java/com/lasferias/cooperativa/
├── App.java
├── database/  (ConexionBD, InicializadorBD)
├── model/     (Asociado, Prestamo, Cuota, Pago, Usuario)
├── dao/       (AsociadoDAO, PrestamoDAO, CuotaDAO, PagoDAO,
                UsuarioDAO)
├── service/   (AsociadoService, AutenticacionService, PrestamoService,
                CuotaService, PagoService)
└── ui/        (Estilo, VentanaPrincipal, PanelAsociados, PanelPrestamos,
                PanelCuotas, PanelPagos, FormularioAsociado, FormularioPrestamo,
                FormularioCuota, FormularioPago, VentanaLogin,
                VentanaCreacionAdministrador)
src/test/java/com/lasferias/cooperativa/
├── database/InicializadorBDTest.java
├── dao/       (AsociadoDAOTest, PrestamoDAOTest, CuotaDAOTest,
                PagoDAOTest, UsuarioDAOTest)
├── service/   (AsociadoServiceTest, AutenticacionServiceTest,
                PrestamoServiceTest, CuotaServiceTest, PagoServiceTest)
└── ui/        (FormularioAsociadoTest, FormularioPrestamoTest,
                FormularioCuotaTest, FormularioPagoTest, PanelAsociadosTest,
                PanelPrestamosTest, PanelCuotasTest, PanelPagosTest)
```

## Reglas del producto (resumen)

- Sin cálculos financieros automáticos: el valor de cuota y los pagos se registran manualmente; no hay intereses, amortizaciones ni saldos pendientes.
- No se elimina ningún registro (asociados, préstamos, cuotas ni pagos); solo se editan.
- Una cuota con pagos asociados no puede modificar su número, valor ni fecha de vencimiento.
- Los pagos se corrigen editando el registro directamente, **sin historial de versiones ni auditoría**.
- Se admiten abonos parciales; se rechazan pagos con valor nulo, cero o negativo.
- Para pruebas y demostraciones se usan **solo datos ficticios**.
- Ninguna funcionalidad pendiente debe darse por terminada.

## Copia de seguridad y recuperación

*Pendiente.* Se documentará aquí el procedimiento completo cuando se implemente la copia desde la interfaz.

## Limitaciones conocidas

Se completará al terminar el MVP. Ya están definidas por diseño:

- La edición de pagos **no conserva versiones anteriores** ni implementa auditoría avanzada.
- El campo `plazo` es texto libre, sin unidad fija.
- Los vínculos se fijan al crear (P2 a): un préstamo no puede cambiar de asociado al editarse; un vínculo mal puesto no se puede mover ni eliminar.
- Hay un único administrador, sin permisos ni operación simultánea desde varios equipos.
- No hay cambio ni recuperación de contraseña: si se olvida, solo se recupera restaurando una copia de seguridad anterior o recreando la base de datos.
- Los botones primarios usan `BasicButtonUI` para que el fondo verde se pinte; como efecto secundario pierden el resaltado de "botón por defecto" del L&F de Windows (Enter sigue funcionando).
- Pendiente de pulir, sin bloquear: tras un guardado fallido el foco se queda en el último campo y no viaja al que tiene el error.
- `parsearMonto` acepta ceros a la izquierda sin separadores (`"000800"` → 800) y rechaza `"0.800"` y `"0.001"`; `"0"` lo rechaza el servicio, no el parser. Cuotas usa la misma regla.
- En botones primarios deshabilitados el fondo sigue verde y el texto queda casi ilegible; es cosmético y solo ocurre mientras se guarda.
- `cuota.numero_cuota` no tiene `CHECK` en la base: la validación (`>= 1`) es solo del servicio.
- El sistema no restringe el registro de cuotas según el estado del préstamo; el estado es un dato manual.
- El sistema no verifica que la cantidad de cuotas registradas coincida con `prestamo.numero_cuotas` (decisión P3).
- En el formulario de Pagos, el combo de cuota trunca la etiqueta cuando está cerrado (se lee completa en la lista desplegada y en la tabla). Cosmético.