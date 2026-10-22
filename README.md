# Cooperativa Las Ferias — MVP de gestión de asociados y préstamos

Aplicación de escritorio **offline** (Java + Swing + SQLite) para registrar y consultar asociados, préstamos, cuotas y pagos de la cooperativa de la Plaza de Mercado de Las Ferias.

> Documento funcional de referencia: [docs/MVP-Cooperativa-Las-Ferias-v3.md](docs/MVP-Cooperativa-Las-Ferias-v3.md)
> Guía de trabajo para el agente coder: [AGENTS.md](AGENTS.md)

## Estado actual

| Fase | Contenido | Estado |
| --- | --- | --- |
| 1 | Estructura Maven, conexión SQLite, esquema inicial y arranque verificado | Terminada |
| 2 | Módulo Asociados (modelo, DAO, servicio, pantalla, pruebas) | Terminada |
| 3 | Login y creación del administrador (BCrypt) | Pendiente |
| 4+ | Préstamos, cuotas, pagos, historial, copia de seguridad | Pendiente |

**Implementado hoy:** alta, búsqueda, consulta y edición de asociados, con validaciones y persistencia en SQLite (33 pruebas automatizadas).

**Aún no implementado** (no debe presentarse como operativo): inicio de sesión, pantallas de préstamos, cuotas y pagos, historial, copia de seguridad y datos de demostración.

## Requisitos

- **JDK 17 o superior** (desarrollado con JDK 25, compilando a `release 17`).
- **Apache Maven 3.9+**.
- Windows (la ruta de datos usa `%APPDATA%`).

## Compilar, probar y ejecutar

```
mvn clean compile   # compila
mvn test            # pruebas JUnit 5 (esquema y módulo de asociados, 33 en total)
mvn exec:java       # inicializa la BD y ejecuta com.lasferias.cooperativa.App
```

## Base de datos

SQLite en el perfil del usuario:

```
%APPDATA%\CooperativaLasFerias\cooperativa.db
```

Se crea automáticamente en el primer arranque (`CREATE TABLE IF NOT EXISTS`), de modo que volver a iniciar la aplicación no duplica datos. Estructura:

- `usuario` — credenciales del administrador (contraseña siempre con hash).
- `asociado` — documento único, nombres, apellidos, teléfono, fecha de registro.
- `prestamo` — monto, fecha, plazo, número de cuotas, valor de cuota, estado (Vigente/Finalizado), FK a asociado.
- `cuota` — número, valor, fecha de vencimiento, FK a préstamo.
- `pago` — fecha, valor, número de recibo, FK a cuota.

Fechas en texto ISO (`YYYY-MM-DD`), montos en enteros.

## Estructura del proyecto

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
└── ui/        (VentanaAsociados, FormularioAsociado)
src/test/java/com/lasferias/cooperativa/
├── database/InicializadorBDTest.java
├── dao/AsociadoDAOTest.java
└── service/AsociadoServiceTest.java
```

## Reglas del producto (resumen)

- Sin cálculos financieros automáticos: el valor de cuota y los pagos se registran manualmente.
- No se eliminan registros; solo se editan.
- Los pagos se corrigen editando el registro, sin historial de versiones.
- Ninguna funcionalidad pendiente debe darse por terminada.
