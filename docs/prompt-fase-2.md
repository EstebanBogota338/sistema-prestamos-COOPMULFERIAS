# Fase 2 — Módulo Asociados (prompt para el agente coder)

Ya revisé tu auditoría. Puedes implementar la Fase 2: módulo de Asociados.

## CONTEXTO OBLIGATORIO (antes de tocar código)

Lee completos, sin fragmentos:

- `docs/MVP-Cooperativa-Las-Ferias-v3.md` (requisitos del MVP).
- `AGENTS.md` (estado real del proyecto, decisiones cerradas y convenciones).
- `docs/diseno/home-mockup.html` (boceto visual del usuario; ver sección REFERENCIA VISUAL).

Ejecuta `git status` y conserva cualquier cambio ajeno a esta tarea. Presenta brevemente el orden de implementación y los archivos que modificarás; después procede sin esperar otra aprobación, siempre que no encuentres una contradicción importante o un riesgo inesperado.

---

## REFERENCIA VISUAL — boceto del usuario

El archivo `docs/diseno/home-mockup.html` es el boceto que aprobó el usuario. Es un **mockup HTML exportado de una pantalla Home**; sirve como referencia de lenguaje visual (colores, jerarquía, densidad, bordes, radios y espaciados), **no** como alcance ni como código a portar. La entrega debe ser Swing real y operativa: funcionando, no un mockup.

### Tokens medidos del boceto (úsalos tal cual)

| Elemento | Valor |
| --- | --- |
| Fondo de la app | `#F5F7F6` |
| Superficies / tarjetas | `#FFFFFF` |
| Texto principal | `#212529` |
| Texto secundario | `#6B7280` |
| Bordes | `#E2E5E3`, grosor `0.5px` |
| Verde principal (acento) | `#2F8F57` |
| Verde oscuro (hover/activo) | `#256F45` (dato del documento, no del boceto) |
| Radio de tarjetas | `8px` (contenedor exterior `12px`) |
| Pantalla destino | 1920 × 1080, legible a 1024 × 768 |
| Barra superior | alto `52px`, fondo `#FFFFFF`, borde inferior `#E2E5E3`, padding lateral `20px` |
| Título de la app | `15px`, peso 500, `#212529` ("Cooperativa Las Ferias") |
| Pestaña activa | `13px`, peso 500, `#2F8F57`, subrayado `2px` `#2F8F57` |
| Etiquetas de sección | `13px`, peso 500, `#6B7280` |
| Título de tarjeta | `14px`, peso 500, `#212529` |
| Subtítulo de tarjeta | `12px`, `#6B7280` |
| Cifra destacada | `24px`, peso 500, `#212529` |
| Espaciado entre tarjetas | `14–16px`; padding de tarjeta `14px`; padding de contenido `24px` |
| Avatar de usuario | círculo `24px`, fondo `#E8F5EC`, iniciales `11px` `#2F8F57` |
| Botón primario | fondo `#2F8F57`, texto blanco; hover `#256F45` |

### Estructura del boceto (para imitar el lenguaje, no la pantalla)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Cooperativa Las Ferias        Asociados        [AD] Administrador        │  barra 52px, blanca
├──────────────────────────────────────────────────────────────────────────┤
│  (contenido sobre #F5F7F6, padding 24px)                                 │
│                                                                          │
│  Etiqueta de sección (13px #6B7280)                                      │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │  tarjetas blancas
│  │ icono + título│ │ icono + título│ │ icono + título│ │ icono + título│ │  borde #E2E5E3
│  │ subtítulo     │ │ subtítulo     │ │ subtítulo     │ │ subtítulo     │ │  radio 8px
│  └───────────────┘ └───────────────┘ └───────────────┘ └───────────────┘ │  gap 14px
└──────────────────────────────────────────────────────────────────────────┘
```

### Traducción exigida a la ventana de Asociados (Fase 2)

Reutiliza la barra superior y el lenguaje de tarjetas, con esta composición:

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Cooperativa Las Ferias            [ Asociados ]        [AD] Administrador│
├──────────────────────────────────────────────────────────────────────────┤
│  Buscar asociado                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │ [ documento, nombres o apellidos ______________ ]  [Buscar] [Limpiar]│ │ tarjeta blanca
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  Asociados registrados (N)                    [Nuevo asociado] [Editar]  │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │ Documento │ Nombres │ Apellidos │ Teléfono │ Fecha de registro      │  │ JTable NO editable
│  │ 10203040  │ Ana     │ Pérez     │ 3001234567│ 2026-10-22            │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│  Estado: N registros · mensajes de validación/confirmación aquí          │
└──────────────────────────────────────────────────────────────────────────┘
```

Y el formulario modal (alta y edición comparten el mismo diálogo):

```
┌─ Nuevo asociado ──────────────────────────────┐
│ Documento *   [__________________________]    │
│ Nombres *     [__________________________]    │
│ Apellidos *   [__________________________]    │
│ Teléfono      [__________________________]    │  opcional, 7–10 dígitos
│ Fecha de registro: 2026-10-22 (automática)    │  no editable
│ ⚠ [mensaje de validación legible en rojo]     │
│                          [Cancelar] [Guardar] │
└───────────────────────────────────────────────┘
```

### Qué NO portar del boceto (importante)

- **No** copies HTML, CSS, utilidades de estilo, variables `--*`, la fuente de iconos por CDN, iconos Tabler ni ninguna dependencia web: la UI es Swing puro. Los iconos son opcionales y, si se usan, deben ser recursos locales o texto; nada de red.
- **No** implementes el Home, las 4 tarjetas de accesos rápidos, el bloque "Resumen" ni el dashboard: están fuera de esta fase (y `AGENTS.md` prohíbe mostrarlos como funcionales).
- **No** implementes la tarjeta "Saldo pendiente total" ni ninguna cifra monetaria agregada: el documento (4.5) prohíbe calcular o mostrar saldos pendientes y agregaciones de importes.
- **No** uses los valores del boceto (48, 15, $2.300.000) como datos reales ni los codifiques: son de ejemplo. Está prohibido introducir datos ficticios al arrancar.
- **No** muestres pestañas o enlaces a Home, Préstamos, Cuotas o Pagos: todavía no existen y no deben aparecer como operativos.
- **No** implementes "Asociados activos": no hay estado de asociado (regla explícita). Un conteo neutro tipo "Asociados registrados (N)" sí es válido.
- **No** muestres "AD admin" como si existiera login: usa una etiqueta "Administrador" o el avatar con iniciales, sin autenticación.
- Si algo del boceto contradice al documento o a las reglas de esta tarea, **prevalece el documento**; déjalo anotado en el informe.

---

## ALCANCE

- Modelo `Asociado`.
- DAO JDBC con consultas parametrizadas y conexiones gestionadas correctamente.
- Servicio con las validaciones acordadas.
- Interfaz Swing para listar, buscar, registrar y editar asociados.
- Integración de la ventana con `App.java`.
- Pruebas automatizadas de DAO, servicio y restricciones importantes.

La interfaz debe ser funcional, no un mockup. La tabla no debe permitir edición directa de sus celdas. El alta y la edición deben utilizar un formulario coherente, con mensajes claros de validación y error. Aplica la paleta y el lenguaje visual de la sección REFERENCIA VISUAL.

## REGLAS

- No eliminar registros.
- No añadir estado del asociado.
- Documento único; nombres, apellidos y documento obligatorios.
- Teléfono opcional con la validación acordada: solo dígitos, entre 7 y 10 caracteres.
- `fecha_registro` automática al crear (`LocalDate.now()`) y sin cambios durante la edición.
- La edición conserva el mismo ID.
- No introducir datos ficticios al arrancar.
- No implementar préstamos, cuotas, pagos, login, dashboard ni cálculos financieros.
- No añadir dependencias ni refactorizar componentes existentes sin necesidad demostrable.
- No cambiar el esquema existente si el módulo puede funcionar con él.
- No introducir reglas nuevas no documentadas.
- Entradas y salidas de la UI en español, coherentes con el resto del proyecto (`com.lasferias.cooperativa`).

## PRUEBAS Y PERSISTENCIA

- Usa `@TempDir` en las pruebas. Nunca uses la base real en los tests.
- Prueba alta, búsqueda, documento duplicado, edición, validaciones y persistencia al cerrar y reabrir conexiones.
- Refuerza las pruebas existentes del esquema (`InicializadorBDTest`) para comprobar restricciones relevantes, claves foráneas e índices, sin convertirlo en una refactorización independiente.
- Gestiona correctamente las excepciones de unicidad (constraint `UNIQUE` de `documento`) y no muestres errores SQL como éxitos.
- No introduzcas reglas nuevas no documentadas.

## CRITERIOS DE FINALIZACIÓN

1. `mvn clean compile` termina correctamente.
2. `mvn test` termina correctamente.
3. La aplicación abre la ventana de Asociados y permite realizar las operaciones previstas.
4. Los datos persisten al cerrar y reabrir la aplicación.
5. Los errores de validación y persistencia se comunican claramente.
6. No se modifican archivos fuera del alcance sin justificación.

Ejecuta las verificaciones disponibles y entrega un informe final con:

- archivos modificados y creados;
- decisiones técnicas;
- resultados reales de compilación y pruebas (cópialos, no los resumas de memoria);
- limitaciones conocidas;
- pasos para probar manualmente la interfaz (incluye el comando `mvn exec:java` y el recorrido crear → buscar → editar → reiniciar).

No afirmes que una funcionalidad está verificada si no la has ejecutado. Si no puedes observar la ventana, dilo explícitamente y describe qué comprobaste por otros medios.

**No hagas commits automáticamente.** Deja los cambios listos para mi revisión (`git status` / `git diff`).
