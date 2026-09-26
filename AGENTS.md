# AGENTS.md — mapa para agentes de IA

> Punto de entrada. Es un **mapa**, no un reglamento: cada regla vive en un solo doc y aquí solo se apunta a él. Lee lo que necesites cuando lo necesites.

## Proyecto

Backend de una app web para gestionar el personal, los servicios y las citas de una barbería. El usuario principal es el **administrador**, que puede ver y modificar todo:

- **Tienda**: ubicación, historia y horarios.
- **Personal**: empleados y un reporte de lo que ha hecho cada uno.
- **Servicios**: catálogo de lo que ofrece la barbería.
- **Ventas**: reporte semanal; registro de servicios hechos sin cita, asociados a un empleado.
- **Landing**: página pública para clientes, que muestra la información de la tienda, el personal y los servicios.
- **Citas**: disponibilidad casi automática. A partir del horario del empleado y la duración del servicio, el sistema calcula los espacios libres de la persona con quien se quiere la cita.

Es la visión general, no el alcance actual: lo construido y lo pendiente está en `docs/features.json`.

## 1. Al empezar

- El hook `SessionStart` inyecta un mensaje `[harness]` con la rama y el trabajo activo. Síguelo.
- Antes de un spec: `docs/specs.md`. Antes de escribir o revisar código: `docs/conventions.md` y `docs/architecture.md`.

## 2. Mapa

| Ruta | Qué es | Cuándo leerla |
|---|---|---|
| `docs/features.json` | Features de producto y su `status` (vacío = `pending`). Se comparte con el frontend | Al trabajar una feature |
| `docs/specs.md` | Flujo SDD, estados, formato de requirements/design/tasks, `features.json`, cambios a features existentes | Antes de redactar o leer un spec |
| `docs/conventions.md` | Código, excepciones, errores, OpenAPI, testing. Fuente única | Antes de escribir o revisar código |
| `docs/architecture.md` | Capas y regla de dependencia | Antes de implementar |
| `specs/<module>/<id>-<slug>/` | `requirements.md` + `design.md` + `tasks.md` de una feature | Al implementar, revisar o corregir esa feature |
| `.claude/skills/` | `/feature` (leader) y `/fix` | Los invoca el humano |
| `.claude/agents/` | `spec-author`, `implementer`, `reviewer` | Los lanzan las skills |
| `.claude/hooks/` | El harness: gates, aprobación, estado | Si un `[harness]` te bloquea |
| `.claude/state/` | Estado local del worktree (gitignored): `current.json`, `review.md` | Solo lo tocan los hooks, `state.js` y el reviewer |

## 3. Flujos

| Trabajo | Comando | Rama |
|---|---|---|
| Feature nueva (en `features.json`) | `/feature <id>`: spec → "aprobado" → implementer ⇄ reviewer → done | `feat/<slug>` |
| Fix, chore o refactor | `/fix <descripción>`: plan → "aprobado" → cambio | `fix/`, `chore/`, `refactor/<slug>` |

- Toda rama sale de `origin/main` (`git fetch` + `git switch -c <rama> origin/main`). Nunca `checkout main`: puede estar abierto en otro worktree.
- `<slug>` en inglés, kebab-case, lo elige el agente a partir del contenido.
- Detalle de cada flujo: `docs/specs.md` y el `SKILL.md` de cada skill.

## 4. Reglas duras

- **Nada de código sin aprobación humana.** Solo el humano aprueba, escribiendo exactamente `aprobado`.
- **Un trabajo activo por worktree.** Varios trabajos en paralelo = varios worktrees.
- **Build verde antes de cerrar**: `./mvnw clean install` → `BUILD SUCCESS`.
- **Test de otra feature rojo**: no se edita para que pase; se busca la causa en el cambio actual.
- **Commit, push y PR solo cuando el humano lo pide.** El harness pide confirmación.
- **Si no sabes algo, búscalo en `docs/`** antes de inventarlo. Si no está, pregunta.
- **Nunca esquives un bloqueo `[harness]`** (otra ruta, Bash, editar el hook): explica el motivo al humano.

## 5. Harness

Los hooks garantizan el **proceso**; la calidad la cuidan los agentes y el reviewer.

| Hook | Qué garantiza |
|---|---|
| `check-spec-gate.js` (Edit/Write) | `src/` y `pom.xml`: nunca en `main`; en una feature solo con `in_progress`, spec completo y `depends_on` en `done`; en un fix solo con plan aprobado. En `specs/`: solo el spec de la feature activa y según su status. En `features.json`: solo transiciones válidas y nunca `→ in_progress`. `.claude/state/` y el harness quedan protegidos |
| `check-bash.js` (Bash/PowerShell) | Las mismas reglas cuando la shell escribe (`>`, `sed -i`, `cp`, …). Es heurístico. Pide confirmación para commit, push y PR |
| `approve.js` (prompt) | `aprobado` → feature `spec_ready → in_progress`, o fix aprobado. Es la única vía de aprobación |
| `session-start.js` | Inyecta el trabajo activo y copia `.env` desde el worktree principal si falta |

**Estado** (`.claude/state/current.json`, uno por worktree):
- Lo crea `node .claude/hooks/state.js start …` al abrir la rama y lo borra `state.js clear` después del PR.
- Guarda qué trabajo está activo, en qué rama y si el fix está aprobado.
- Si la rama actual no coincide con la de `current.json`, el hook bloquea.

## 6. Si te bloqueas

- Relee la sección relevante de `docs/`.
- Si una herramienta no hace lo esperado o el spec no es viable: **para y explica**, sin workarounds. El humano decide.
