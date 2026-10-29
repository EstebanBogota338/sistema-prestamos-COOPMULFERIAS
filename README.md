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
| 3 | Inicio de sesión y creación del administrador (BCrypt) | Pendiente |
| 4+ | Préstamos, cuotas, pagos, historial, copia de seguridad, datos de demostración | Pendiente |

**Implementado hoy:** alta, búsqueda, consulta y edición de asociados, con validaciones y persistencia en SQLite. **33 pruebas automatizadas.**

**Aún no implementado** (no debe presentarse como operativo): inicio de sesión, pantallas de préstamos, cuotas y pagos, historial, copia de seguridad y datos de demostración.

## Requisitos

- **JDK 17 o superior** (desarrollado con JDK 25, compilando a `release 17`).
- **Apache Maven 3.9+**.
- **Windows** (la ruta de datos usa el perfil del usuario en `AppData`).

## Compilar, probar y ejecutar

Desde la carpeta del proyecto, en la terminal de VS Code (PowerShell):

```
mvn clean compile   # compila
mvn test            # pruebas JUnit 5 (esquema y módulo de asociados: 33 en total)
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
| `usuario` | Credenciales del administrador (la contraseña se guardará siempre con hash BCrypt). |
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
└── diseno/home-mockup.html
src/main/java/com/lasferias/cooperativa/
├── App.java
├── database/  (ConexionBD, InicializadorBD)
├── model/     (Asociado)
├── dao/       (AsociadoDAO)
├── service/   (AsociadoService)
└── ui/        (Estilo, VentanaAsociados, FormularioAsociado)
src/test/java/com/lasferias/cooperativa/
├── database/InicializadorBDTest.java
├── dao/AsociadoDAOTest.java
├── service/AsociadoServiceTest.java
└── ui/        (FormularioAsociadoTest, VentanaAsociadosTest)
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
- Hay un único administrador, sin permisos ni operación simultánea desde varios equipos.