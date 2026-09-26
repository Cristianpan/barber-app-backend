---
name: fix
description: Flujo corto para fix, chore o refactor. Plan → "aprobado" → cambio, sin SDD completo.
argument-hint: <descripción del issue o tarea>
disable-model-invocation: true
---

# /fix $ARGUMENTS

Issue o tarea: **$ARGUMENTS**

Investigas e implementas tú, en este hilo. Reglas de código y tests: `docs/conventions.md` y `docs/architecture.md`. Qué se documenta según el impacto: `docs/specs.md#cambios-a-features-existentes`.

## 1. Clasificar

| Tipo | Cuándo |
|---|---|
| `fix` | Algo existente no cumple su spec, o su spec debe cambiar |
| `chore` | Schema base, config, dependencias, tooling |
| `refactor` | Reestructura sin cambio observable |

Si es una capacidad nueva (endpoint o caso de uso nuevo), no es un fix: para y propón añadirla a `docs/features.json` y usar `/feature`.

## 2. Rama y estado

1. `git status --porcelain` vacío. Si no, para y avisa.
2. `slug`: inglés, kebab-case, 2–4 palabras (ej. `duplicate-user-email`).
3. `git fetch origin` y `git switch -c <type>/<slug> origin/main`.
4. `node .claude/hooks/state.js start fix <slug>`.

## 3. Investigar (solo lectura)

Localiza el código, la feature afectada (`specs/<module>/<id>-<slug>/`) y los `R<n>`/`D<n>` implicados. Hasta que el humano apruebe, el hook bloquea la escritura.

## 4. Plan y parar

Presenta en el chat, sin crear archivo:

```markdown
## Problema          (chore/refactor: Objetivo)
## Cómo reproducir   (solo fix)
## Causa
## Solución
## Impacto en spec   ninguno | D<n> | R<n> + D<n>
## Tests             test que se agrega o ajusta, ligado a su R<n>
```

- Impacto `ninguno`: el bug viola un `R<n>` vigente.
- Impacto `D<n>`: cambia el cómo.
- Impacto `R<n>`: cambia el qué. Al aprobar, el humano aprueba también el nuevo requisito; recuérdale que el front debe volver a copiar `requirements.md`.
- Si ningún `R<n>` cubre el caso del bug, falta un requisito: el impacto es `R<n>`.

Termina con: "Escribe **aprobado** o pide cambios." y **para**.

## 5. Implementar

Cuando el hook confirme la aprobación:

- **Código** según `conventions.md`.
- **Docs** según el impacto: edita en sitio, sin historial; un `R<n>`/`D<n>` nuevo lleva el siguiente id. `tasks.md` no se toca.
- **Tests**:
  - fix: un test del `R<n>` que reproduce el bug, en el `@Nested` de su operación;
  - `R<n>` cambiado: ajusta sus tests;
  - chore/refactor: sin tests nuevos, salvo que un `R<n>` lo exija.
- `./mvnw clean install` debe terminar en `BUILD SUCCESS`.
- Si el plan resulta inviable: `node .claude/hooks/state.js revoke`, explica y presenta un plan nuevo.

## 6. Revisión

- Impacto `D<n>` o `R<n>`: lanza `reviewer` con el resumen del plan y los `R<n>`/`D<n>` afectados. Si responde `CHANGES_REQUESTED`, corrige y relánzalo (máximo 3).
- Impacto `ninguno`: basta el build verde.

## 7. Cierre

Reporte breve: qué cambió, tests y docs tocados.

Commit y PR **solo si el humano lo pide**:

1. Commit con mensaje `<type>: <descripción en inglés>`.
2. `git push -u origin <type>/<slug>` y `gh pr create` con `.github/PULL_REQUEST_TEMPLATE.md`.
3. Tras abrir el PR: `node .claude/hooks/state.js clear`.

## Nunca

- Tocar código antes de `aprobado`.
- Cambiar `status` en `features.json` ni crear specs nuevos.
- Esquivar un bloqueo `[harness]`.
