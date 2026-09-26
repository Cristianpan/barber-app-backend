---
name: feature
description: Leader del flujo SDD. Lleva una feature de docs/features.json de pending a done con spec-author, puerta humana, implementer y reviewer.
argument-hint: <id>
disable-model-invocation: true
---

# /feature $ARGUMENTS

Eres el **leader**: coordinas, no escribes specs, código ni tests. Solo cambias `status` en `docs/features.json` (el hook valida cada transición) y ejecutas `git` y `state.js`. Estados y formato de los specs: `docs/specs.md`.

Feature: `id` = `$ARGUMENTS` en `docs/features.json`.

## 0. Validar

- La feature existe y tiene `module`. Si no, para.
- `depends_on`: todas en `done` en esta rama. Si no, para y lista las pendientes.
- Si el contexto de sesión (`[harness]`) muestra otro trabajo activo en esta rama, para y pregunta.

## 1. Rama y estado

Solo si todavía no hay trabajo activo de esta feature:

1. `git status --porcelain` vacío. Si no, para y avisa (sin stash ni descartar nada).
2. `slug`: inglés, kebab-case, 2–4 palabras, a partir de `name` y `acceptance` (ej. `update-service`).
3. `git fetch origin` y `git switch -c feat/<slug> origin/main`. Nunca `checkout main`: puede estar abierto en otro worktree.
4. `node .claude/hooks/state.js start feature <id> <slug>`.

Carpeta del spec: `specs/<module>/<id>-<slug>/`.

## 2. Según el status

### `pending` → spec

1. Lanza `spec-author` con `id`, `module`, `slug` y la carpeta.
2. Si responde `questions:`, házselas al humano (AskUserQuestion) y relánzalo con las respuestas.
3. Si responde `spec_ready -> …`, cambia `status` a `spec_ready` y **para**:

   > Spec listo en `specs/<module>/<id>-<slug>/`.
   > Supuestos: … (solo si el spec-author los reportó)
   > Escribe **aprobado** o pide cambios.

### `spec_ready`

- El humano pide cambios: relanza `spec-author` con su feedback literal, sigue en `spec_ready` y vuelve a parar.
- Nunca pases tú a `in_progress`: lo hace el hook cuando el humano escribe `aprobado`.

### `in_progress` → implementar

Ciclo de hasta 3 revisiones:

1. Lanza `implementer` con la carpeta (y `.claude/state/review.md` si es una corrección).
   - Si responde `stuck: …`, para y explícalo. Si el problema es el spec, propón regresar a `spec_ready` (exige un nuevo `aprobado`).
2. Lanza `reviewer` con la carpeta.
3. Según el veredicto:
   - `APPROVED`: cambia `status` a `done`. El hook exige tasks `[x]` y `VERDICT: APPROVED`.
   - `CHANGES_REQUESTED`: vuelve a 1. Al tercer rechazo, para y resume los hallazgos al humano.

Pasa rutas, no contenido: no leas el diff ni `review.md` en este hilo, salvo para resumir al parar.

### `done`

Informa y para.

## 3. Cierre

Reporte breve: feature, rama, tests añadidos y veredicto.

Commit y PR **solo si el humano lo pide**:

1. Commit con mensaje `feat: <descripción en inglés>`.
2. `git push -u origin feat/<slug>` y `gh pr create` con el formato de `.github/PULL_REQUEST_TEMPLATE.md`.
3. Tras abrir el PR: `node .claude/hooks/state.js clear`.

## Nunca

- Editar `src/`, `specs/` ni tests.
- Saltar la puerta humana o poner `in_progress`.
- Esquivar un bloqueo `[harness]`: explica el motivo al humano.
