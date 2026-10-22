# Documento de planeación — MVP de gestión de asociados y préstamos

**Proyecto:** Herramienta administrativa para la cooperativa vinculada a la Plaza de Mercado de Las Ferias. **Tecnologías:** Java Swing, SQLite, JDBC y Maven. **Modalidad:** Aplicación de escritorio offline. **Estado:** Especificación funcional actualizada — incorpora la decisión de edición simple de pagos, sin auditoría avanzada.

## 1. Objetivo

Desarrollar una aplicación de escritorio que permita centralizar y consultar la información de asociados, préstamos, cuotas y pagos de la cooperativa, mejorando la organización y la trazabilidad de sus registros.

La aplicación complementará los procesos actuales y no reemplazará Helisa ni realizará cálculos financieros automáticos.

El MVP debe ser sencillo, estable, comprensible para sus usuarios y viable dentro del tiempo disponible para la práctica social (8 jornadas de trabajo).

## 2. Contexto y usuarios

La cooperativa utiliza herramientas contables, registros físicos y hojas de cálculo. La información se encuentra distribuida y puede ser difícil reconstruir el historial de un asociado rápidamente.

La primera versión se ejecutará localmente en un computador, sin depender de internet ni de servicios externos.

Se contempla un usuario administrador para acceder al sistema. No se implementarán permisos avanzados ni operación simultánea desde varios equipos.

Para las demostraciones y pruebas se utilizarán datos ficticios, salvo que exista autorización expresa para utilizar datos reales. Dado que se trata de registros financieros y que la edición de pagos no conservará versiones anteriores (ver 4.4), esto es especialmente importante: durante la demostración se usarán exclusivamente datos ficticios.

## 3. Alcance funcional

### 3.1 Funcionalidades incluidas

- Inicio de sesión de administrador.
- Registro, consulta, búsqueda y edición de asociados.
- Registro, consulta y edición de préstamos.
- Registro y consulta de cuotas.
- Registro, consulta y **edición** de pagos, incluidos los abonos parciales.
- Consulta del historial relacionado con un asociado y sus préstamos.
- Consulta de información registrada y de los valores de cuota y pagos.
- Persistencia de datos en SQLite.
- Validación de entradas y relaciones entre registros.
- Copia de seguridad manual de la base de datos y procedimiento de recuperación documentado.
- Interfaz de escritorio sencilla y consistente.

### 3.2 Funcionalidades excluidas

- Cálculo automático de intereses corrientes o de mora.
- Cálculo automático de amortización o generación financiera de cuotas.
- Liquidaciones anticipadas o certificaciones contables.
- Reemplazo de Helisa.
- Sincronización o integración automática con Helisa.
- Migración masiva de información histórica.
- Aprobación automática de préstamos.
- Múltiples usuarios concurrentes y permisos avanzados.
- Reportes analíticos complejos.
- **Historial de versiones o auditoría avanzada de ediciones de pagos** (ver 4.4).

Una funcionalidad no implementada no debe presentarse como si estuviera operativa.

## 4. Reglas de negocio definitivas

### 4.1 Asociados

| Campo | Descripción |
| --- | --- |
| documento | Identificador único del asociado. |
| nombres | Nombres del asociado. |
| apellidos | Apellidos del asociado. |
| telefono | Número de contacto. |
| fecha\_registro | Fecha en que se registra el asociado. |

Reglas:

- El documento debe ser único.
- Los campos obligatorios deben validarse.
- No se permitirá registrar dos asociados con el mismo documento.
- Se permitirá buscar y consultar asociados.
- Los cambios deben conservarse después de cerrar y volver a abrir la aplicación.
- No se permitirá eliminar asociados; solo se podrán editar sus datos (ver 4.6).

### 4.2 Préstamos

| Campo | Descripción |
| --- | --- |
| monto | Valor registrado del préstamo. |
| fecha | Fecha del préstamo: la fecha consignada en el registro o soporte disponible, sin asumir que siempre es la de desembolso. |
| plazo | Plazo registrado. |
| numero\_cuotas | Cantidad de cuotas acordada. |
| valor\_cuota | Valor de la cuota ingresado manualmente. |
| estado | Estado del préstamo: Vigente o Finalizado. |

El préstamo debe estar vinculado a un asociado existente.

Reglas:

- El monto y el plazo deben ser válidos.
- El número de cuotas debe ser un entero positivo.
- El valor de cuota se registra manualmente.
- El sistema no calculará el valor de cuota a partir del monto y del plazo.
- No se calcularán intereses ni se modificarán automáticamente condiciones financieras.
- No se permitirá eliminar préstamos; solo se podrán editar sus datos (ver 4.6).
- La aplicación conservará el valor registrado, sin sustituirlo por uno calculado.

La cantidad de cuotas y el valor de cuota deben corresponder a lo acordado por la cooperativa. La aplicación no asumirá que el monto dividido entre las cuotas representa una liquidación financiera completa.

### 4.3 Cuotas

Las cuotas se registrarán de acuerdo con la información suministrada para cada préstamo.

Cada cuota debe identificar el préstamo al que pertenece y conservar el valor que se haya registrado manualmente.

No se generará automáticamente un plan de amortización ni se calcularán cuotas con intereses.

Cuando sea necesario consultar el estado de una cuota, la información presentada deberá corresponder a los registros efectivamente almacenados. No se inventarán valores faltantes.

Los campos de la cuota son: número de cuota, valor y fecha de vencimiento, todos ingresados manualmente.

**Regla de edición de cuotas:** si una cuota tiene uno o más pagos asociados, el sistema bloqueará la modificación de su número, valor y fecha de vencimiento. Si no tiene pagos asociados, estos campos podrán editarse. Esta restricción busca preservar la coherencia de los registros existentes. No se permitirá eliminar cuotas.

### 4.4 Pagos, abonos parciales y corrección de pagos *(actualizado)*

El sistema permitirá registrar pagos vinculados a las cuotas correspondientes.

Los pagos podrán registrarse como abonos parciales; no se exige que el valor recibido coincida con el valor total de la cuota, y no se calculará un saldo pendiente para validarlo.

Cada pago debe conservar, como mínimo, la información necesaria para identificar:

- La cuota a la que corresponde.
- La fecha del pago.
- El valor recibido.
- El número de recibo, de carácter obligatorio.

Reglas de registro:

- No se admitirán pagos con valores nulos, cero o negativos.
- No se permitirá asociar un pago a una cuota inexistente.
- El número de recibo se conservará para facilitar la consulta y la trazabilidad.
- No se implementará la eliminación ni la anulación de pagos; solo la edición directa del registro, según se describe a continuación.

**Corrección de pagos (decisión cerrada):** el sistema permitirá editar los datos de un pago registrado. Se actualizará el registro existente, sin implementar un historial de versiones ni una auditoría avanzada. El historial general mostrará los datos actuales del pago, no las versiones previas a la edición.

### 4.5 Cálculos y valores financieros

El sistema no calculará automáticamente valores financieros.

No implementará:

- Intereses.
- Amortizaciones.
- Liquidaciones.
- Generación automática del valor de las cuotas.
- Reajustes financieros.

La aplicación podrá presentar los valores que hayan sido registrados y la información que se derive directamente de los pagos almacenados, siempre que la operación se limite a la consulta administrativa acordada.

La aplicación no calculará ni mostrará saldos pendientes derivados de comparar cuotas y pagos. Los importes se presentan tal como fueron registrados, sin operaciones de agregación adicionales.

## 4.6 Eliminación de registros (decisión cerrada)

El MVP no permitirá eliminar ningún registro: ni asociados, ni préstamos, ni cuotas, ni pagos. Todos los registros podrán editarse, pero no eliminarse físicamente. Esta decisión reduce el riesgo de pérdida de información y simplifica la implementación dentro del tiempo disponible.

## 5. Modelo de datos

La base de datos será SQLite, con acceso mediante JDBC.

### 5.1 Asociado

Campos funcionales: documento, nombres, apellidos, telefono, fecha\_registro. Se podrá utilizar una clave primaria interna para establecer relaciones entre tablas. Esta clave técnica no sustituye el documento como identificador único del asociado.

### 5.2 Préstamo

Campos funcionales: monto, fecha, plazo, numero\_cuotas, valor\_cuota, estado. Se añadirá una clave primaria interna y una referencia al asociado al que pertenece el préstamo.

### 5.3 Cuota

La cuota deberá vincularse con un préstamo existente y conservar el valor registrado manualmente. Se podrá utilizar un identificador interno y un número de cuota para distinguir sus registros.

### 5.4 Pago

El pago deberá vincularse con la cuota correspondiente y conservar la fecha, el valor pagado y el número de recibo cuando aplique. Sus campos podrán actualizarse mediante edición directa del registro (ver 4.4); no se requiere una tabla de historial de versiones para el MVP.

### 5.5 Usuario

Se mantendrá una entidad para las credenciales de acceso del administrador.

**La contraseña del administrador se almacenará siempre mediante un hash seguro (ej. BCrypt o equivalente con sal), nunca en texto plano.** Esta regla es de cumplimiento obligatorio, independientemente de la simplificación aplicada a otras áreas del MVP.

El usuario administrador se creará en el primer arranque de la aplicación. La función de cambio de contraseña es opcional para esta versión del MVP; si no se implementa por falta de tiempo, debe documentarse expresamente como limitación conocida.

### 5.6 Relaciones

- Un asociado puede tener varios préstamos.
- Cada préstamo pertenece a un asociado.
- Un préstamo puede tener varias cuotas.
- Cada cuota pertenece a un préstamo.
- Una cuota puede tener varios pagos.
- Cada pago pertenece a una cuota.

### 5.7 Integridad de la base de datos

- Cada entidad —asociado, préstamo, cuota y pago— tendrá una clave primaria técnica interna generada automáticamente por SQLite.
- El documento será el identificador único funcional del asociado y se utilizará para buscarlo desde la interfaz.
- Las relaciones entre asociados, préstamos, cuotas y pagos se establecerán mediante claves foráneas.
- Activar las claves foráneas de SQLite en cada conexión.
- Utilizar consultas SQL parametrizadas.
- Impedir documentos duplicados.
- Validar los datos tanto en la interfaz como en la capa de negocio.
- Conservar la base de datos en una ubicación local persistente dentro del perfil del usuario de Windows, fuera de carpetas temporales o de compilación; por ejemplo, `AppData/CooperativaLasFerias`.
- No cargar datos ficticios repetidamente en cada inicio.
- Utilizar transacciones cuando una operación requiera varias escrituras relacionadas, incluida la edición de un pago existente.
- Impedir la eliminación física de registros en todos los módulos.

## 6. Arquitectura técnica

La estructura debe ser sencilla y permitir que el código se comprenda y mantenga con facilidad.

Paquetes sugeridos:

- **ui**: ventanas, formularios y navegación.
- **model**: entidades del sistema.
- **dao**: consultas y operaciones de persistencia.
- **service**: validaciones y reglas de negocio.
- **database**: conexión e inicialización de SQLite.

Se utilizarán Java Swing para la interfaz, SQLite para el almacenamiento, JDBC para el acceso a los datos y Maven para gestionar las dependencias y ejecutar las pruebas.

Se podrá evaluar FlatLaf para mejorar la apariencia visual, siempre que su integración no complique ni retrase el desarrollo.

## 7. Diseño visual

La interfaz utilizará una apariencia administrativa sencilla, clara y consistente (estilo "fintech simple").

Paleta:

| Elemento | Color |
| --- | --- |
| Fondo principal | #FFFFFF |
| Fondo secundario | #F5F7F6 |
| Texto principal | #212529 |
| Texto secundario | #6B7280 |
| Bordes | #E2E5E3 |
| Verde principal | #2F8F57 |
| Verde oscuro (hover) | #256F45 |

La navegación (barra superior híbrida + tarjetas de acceso en el Home) debe facilitar el acceso a los módulos de **Asociados, Préstamos, Cuotas y Pagos**. Los formularios tendrán etiquetas claras y mensajes de validación comprensibles.

No se priorizarán animaciones ni efectos visuales complejos. La legibilidad y la facilidad de uso estarán por encima de los adornos.

Resolución objetivo: 1920 × 1080, con legibilidad mínima en 1024 × 768.

## 8. Criterios de aceptación

**Asociados**

- Se puede registrar y consultar un asociado.
- Se puede buscar por documento.
- No se permiten documentos duplicados.
- Los campos obligatorios se validan.
- Los cambios persisten en SQLite.

**Préstamos**

- Se puede registrar un préstamo asociado a un registro existente.
- Se almacenan correctamente el monto, la fecha, el plazo, el número de cuotas, el valor de cuota y el estado.
- El valor de cuota no se calcula automáticamente.
- Se conservan las relaciones con el asociado.

**Cuotas y pagos**

- Las cuotas pertenecen a préstamos existentes.
- Los pagos pertenecen a cuotas existentes.
- Se admiten abonos parciales.
- Se rechazan importes inválidos (nulos, cero o negativos).
- Se puede editar un pago existente; el registro actualizado reemplaza al anterior sin conservar versiones previas.
- Una cuota sin pagos asociados permite editar su número, valor y fecha de vencimiento.
- Una cuota con pagos asociados no permite modificar su número, valor ni fecha de vencimiento.
- El sistema permite consultar la cuota y sus pagos aunque la edición de esos campos esté bloqueada.
- Las relaciones entre asociados, préstamos, cuotas y pagos se mantienen correctamente después de cerrar y volver a abrir la aplicación.
- La información persiste después de reiniciar la aplicación.

**Historial**

- Se puede consultar la información relacionada con un asociado.
- Los préstamos, cuotas y pagos se presentan sin duplicar registros.
- La información mostrada corresponde a la almacenada en SQLite, reflejando siempre el estado actual de cada pago (no versiones anteriores).
- No se presentan cálculos financieros automáticos que estén fuera del alcance.

**Acceso y persistencia**

- El sistema solicita credenciales.
- Las credenciales incorrectas se rechazan.
- La contraseña se almacena mediante hash seguro, nunca en texto plano.
- La base de datos permanece disponible después de cerrar la aplicación.
- La copia de seguridad puede recuperarse mediante el procedimiento documentado.

## 9. Estrategia de pruebas *(actualizada)*

Se utilizarán pruebas unitarias cuando sea razonable, además de pruebas manuales de los flujos completos.

Casos mínimos:

1. Registrar un asociado válido.
2. Intentar registrar un documento duplicado.
3. Buscar un asociado por documento.
4. Registrar un préstamo vinculado a un asociado.
5. Comprobar que el valor de cuota se conserva tal como fue ingresado.
6. Registrar una cuota para un préstamo existente.
7. Registrar un pago válido.
8. Registrar un abono parcial y comprobar que se conserva el importe.
9. Editar un pago existente y comprobar que el registro se actualiza correctamente, sin generar una versión adicional.
10. Cerrar y volver a abrir la aplicación para verificar la persistencia.
11. Crear una copia de seguridad y comprobar su recuperación.

> ~~Rechazar un pago superior al valor pendiente~~ — caso retirado por decisión explícita: exigiría calcular el saldo pendiente, lo cual se consideró innecesario para el MVP de 8 días.

Se utilizarán datos ficticios en las pruebas, sin exponer información personal real de los asociados.

## 10. Plan de trabajo

| Etapa | Actividad |
| --- | --- |
| 1 | Revisar el proyecto y configurar Maven, Swing, SQLite y JDBC. |
| 2 | Definir el esquema de la base de datos y el acceso del administrador. |
| 3 | Implementar asociados y sus validaciones. |
| 4 | Implementar préstamos y sus relaciones. |
| 5 | Implementar cuotas y pagos manuales, incluida la edición simple de pagos. |
| 6 | Implementar consultas e historial. |
| 7 | Ejecutar pruebas de persistencia, validación y relaciones. |
| 8 | Documentar la ejecución, las copias de seguridad y la demostración final. |

La prioridad será contar con un flujo funcional y fiable antes de dedicar tiempo a detalles visuales secundarios.

## 11. Entregables

- Proyecto Maven con código fuente organizado.
- Base de datos SQLite.
- Instrucciones para compilar y ejecutar.
- Procedimiento de configuración del administrador.
- Documentación de copia de seguridad y recuperación.
- Pruebas realizadas y resultados comprobados.
- Datos ficticios de demostración.
- Registro de funcionalidades implementadas y limitaciones conocidas, incluyendo explícitamente: **la edición de pagos no conserva versiones anteriores ni implementa auditoría avanzada.**

## 12. Instrucciones para el agente de programación

Antes de modificar el código:

1. Inspeccionar el proyecto existente.
2. Identificar las funcionalidades ya implementadas.
3. Conservar el código que funcione correctamente.
4. Verificar que el modelo de datos respete esta especificación.
5. Presentar un plan breve de implementación.

Durante el desarrollo:

- Trabajar en incrementos pequeños.
- Ejecutar las pruebas después de los cambios relevantes.
- No afirmar que una función está terminada sin comprobarla.
- No agregar dependencias innecesarias.
- No implementar cálculos financieros automáticos.
- No sustituir Helisa.
- No introducir campos ni estados nuevos sin justificación y autorización.
- No utilizar datos ficticios como si fueran registros reales.
- No implementar historial de versiones ni auditoría avanzada para pagos: la edición debe actualizar el registro existente de forma directa.
- Almacenar siempre la contraseña del administrador mediante hash seguro.

Al finalizar, documentar los archivos creados o modificados, los comandos de compilación y prueba ejecutados, los resultados obtenidos y las funcionalidades que aún estén pendientes.

## 13. Decisiones pendientes

No quedan decisiones funcionales pendientes para el alcance definido del MVP.

El agente de programación podrá definir los detalles técnicos internos necesarios para implementar la solución, siempre que:

- Respete los campos funcionales y las relaciones establecidos en este documento.
- Utilice claves primarias técnicas automáticas en SQLite.
- No incorpore funcionalidades fuera del alcance sin autorización.
- No altere las decisiones cerradas sobre cálculos financieros, edición de pagos, eliminación de registros ni restricciones de edición de cuotas.
