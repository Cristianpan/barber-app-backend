# Spec Driven Development (SDD)

> requirements → design → tasks → code. El código de una feature no se escribe hasta que un humano aprueba su spec.

Reglas de código, errores y tests: [`conventions.md`](conventions.md). Capas: [`architecture.md`](architecture.md). Agentes, skills y hooks: [`AGENTS.md`](../AGENTS.md).

## Tipos de trabajo

| Tipo | Qué es | Flujo | Rama |
|---|---|---|---|
| Feature | Capacidad nueva para el usuario (endpoint o caso de uso nuevo) | `/feature <id>`: SDD completo | `feat/<slug>` |
| Fix | Algo existente no cumple su spec, o su spec cambia | `/fix <descripción>`: plan → aprobación → cambio | `fix/<slug>` |
| Chore / refactor | Schema base, config, dependencias; reestructura sin cambio observable | `/fix <descripción>` | `chore/<slug>`, `refactor/<slug>` |

Solo las features viven en `features.json`. Toda rama sale de `origin/main`; `<slug>` en inglés, kebab-case, lo elige el agente a partir del contenido.

## Estructura

```
specs/<module>/<id>-<slug>/   # module e id de features.json; slug = el de la rama
├── requirements.md           # QUÉ necesita el usuario (EARS); se comparte con el frontend
├── design.md                 # CÓMO: decisiones técnicas
└── tasks.md                  # PASOS ejecutables
```

## Flujo de feature y estados

```
pending → [spec-author] → spec_ready → ⏸ "aprobado" → in_progress → [implementer ⇄ reviewer] → done
```

| Estado | Significado | Lo pone |
|---|---|---|
| `pending` | Sin spec (la feature no tiene campo `status`) | — |
| `spec_ready` | Spec listo, esperando aprobación. No se toca código | leader |
| `in_progress` | Spec aprobado; se implementa | solo el hook, cuando el humano escribe `aprobado` |
| `done` | Tasks `[x]`, build verde y reviewer aprobó | leader |

- Si en `spec_ready` el humano pide cambios, el spec-author los aplica y la feature sigue en `spec_ready`.
- Si en `in_progress` el spec resulta inviable, el leader lo regresa a `spec_ready`, y se necesita un nuevo `aprobado`.

## Cambios a features existentes

Van por `/fix`. El agente presenta un plan (problema, cómo reproducir, causa, solución, impacto, tests) y espera `aprobado`.

| Caso | Qué se documenta |
|---|---|
| Bug: `R<n>` y `D<n>` siguen válidos | Nada. Se agrega el test del `R<n>` que el bug rompía |
| Cambia el **cómo** | Se edita la `D<n>` |
| Cambia el **qué** | Se editan el `R<n>` y las `D<n>` afectadas, se ajustan tests y el front vuelve a copiar `requirements.md` |
| Capacidad nueva | No es fix: feature nueva en `features.json` |

Se edita en sitio, sin historial (Git la guarda). `tasks.md` no se toca: es el plan original.

## requirements.md — EARS

Cabecera fija, para poder copiarlo tal cual al frontend:

```markdown
# 7 — Editar servicio
Módulo: `services`
```

Cada requirement es un párrafo con id estable (`R1`, `R2`, …), un único `DEBE`/`NO DEBE` y uno de estos patrones:

| Patrón | Plantilla |
|---|---|
| Ubicuo | `El sistema DEBE <acción>.` |
| Evento | `CUANDO <disparador>, el sistema DEBE <acción>.` |
| Estado | `MIENTRAS <estado>, el sistema DEBE <acción>.` |
| Opcional | `DONDE <feature opcional>, el sistema DEBE <acción>.` |
| No deseado | `SI <evento no deseado> ENTONCES el sistema DEBE <acción>.` |

- **Nivel usuario, no técnico**: qué puede hacer, qué obtiene y qué se le impide. Sin clases, tokens, códigos HTTP, config, logs ni frameworks: eso es una `D<n>`. Prueba rápida: si solo lo entiende un desarrollador, es `D<n>`. Además vale igual para backend y frontend.
- **Un requirement por comportamiento observable**: las causas distintas con el mismo resultado se agrupan (✅ "SI el correo o la contraseña son incorrectos ENTONCES … rechazar sin revelar qué dato falló"). Se parte solo si cambia lo que el usuario hace u obtiene.
- **Alto nivel → acotado**: el usuario suele dar el happy path y algún edge. El `spec-author` deja el `R<n>` en lenguaje de usuario y baja lo técnico (excepción, mensaje, edges razonables) a `D<n>`. Si añade comportamiento observable que el `acceptance` no pide, lo reporta como supuesto, y el humano lo ve antes de aprobar.

```markdown
## R1
CUANDO el administrador consulta los usuarios recientes, el sistema DEBE
devolver hasta 5 usuarios ordenados por fecha de creación descendente.

## R2
SI el administrador pide una cantidad de usuarios menor o igual a 0 ENTONCES el sistema DEBE
rechazar la petición como inválida.
```

## design.md — decisiones técnicas

Lista **concisa** de decisiones técnicas, tomadas **antes** de codificar. No es un borrador del código ni una explicación.

**Formato: una decisión = una línea, directa.** Se permiten hasta dos líneas solo si la decisión es compleja o afecta una regla de negocio (el "porqué" va en esa segunda línea). Cada decisión aparece una sola vez, con id `D1`, `D2`, … en orden. No llevan test propio.

```markdown
- **D1** `POST /auth/sign-in` en `AuthController` → `AuthService`; público (`permitAll`), stateless, CSRF desactivado.
- **D2** `findByEmail` en `UserRepository`; el correo se busca en minúsculas.
- **D3** Credenciales inválidas → `UnauthorizedException` con `INVALID_CREDENTIALS_MESSAGE`. Mismo mensaje si el correo no existe o la contraseña falla, para no revelar cuál.
```

Solo se documenta lo que **no** está ya dictado por `architecture.md` o `conventions.md` (no repetir "advice global", "OpenAPI", naming, formato de error, etc.). Cada decisión trae la **excepción/status** que usa cuando aplica.

Secciones (todas breves, en viñetas de una línea):

1. **Decisiones** (`D<n>`).
2. **Alternativas descartadas**: solo si son relevantes; una línea con el motivo.
3. **Limitaciones conocidas**: solo si existen.

Prohibido:
- código Java o snippets de config;
- lista de archivos (la dan las tasks);
- sección de tests o de excepciones aparte;
- historial ("cambio pedido…", "sustituye a…", "antes hacía…") o preguntas abiertas: Git guarda la historia, se edita la `D<n>` y listo;
- recap del stack;
- repetir `D<n>` o `R<n>`.

**Alcance**: solo decisiones de los `R<n>` de esta feature. Lo que un `R<n>` no pide (ej. validación de JWT en un spec de sign-in) va en su propia feature.

## tasks.md — checklist ejecutable

Pasos discretos en orden, con checkbox. Cada task referencia al menos un `R<n>` o `D<n>`.
- Sin tasks "escribir test para `D<n>`".
- Todo endpoint nuevo incluye "documentar en OpenAPI dentro del grupo de su módulo".
- El `implementer` marca `[x]` al completar cada task.

## Tests

Los tests nacen de los `R<n>`. Todas las reglas de testing: [`conventions.md`](conventions.md#testing) (fuente única).

## features.json

Lista de features de producto. Se comparte con el frontend: se copia y se quitan los `status`, que son propios de cada repo. El humano añade features; los agentes solo cambian `status`, y el hook valida cada cambio.

```json
{ "id": 7, "module": "services", "name": "Editar servicio", "status": "spec_ready",
  "depends_on": [6], "acceptance": ["El administrador puede actualizar los datos de un servicio", "..."] }
```

| Campo | Significado |
|---|---|
| `id` | Entero, estable, no se reutiliza. |
| `module` | Módulo en kebab-case (`auth`, `services`); agrupa los specs. |
| `name` | Nombre legible en español ("Editar servicio"). |
| `status` | Opcional: `spec_ready` \| `in_progress` \| `done`. Si falta, es `pending`. |
| `depends_on` | Ids sin cuyo código esta feature no se puede implementar (bloqueo técnico, no orden lógico: editar no depende de crear si el schema ya existe). Deben estar `done` en la rama actual. `[]` si no aplica. |
| `acceptance` | Criterios del humano; entrada del `spec-author`. Solo se editan mientras la feature está `pending`; después manda `requirements.md` y los cambios van por `/fix`. |
