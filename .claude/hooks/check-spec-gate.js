#!/usr/bin/env node
'use strict';
// PreToolUse (Edit|Write|MultiEdit|NotebookEdit): decide si el agente puede escribir en la ruta.
// Falla cerrado: cualquier error inesperado bloquea la edición.

const fs = require('fs');
const path = require('path');
const lib = require('./lib');

function contentAfter(root, input) {
  const t = input.tool_input || {};
  if (input.tool_name === 'Write') return t.content || '';
  let text = lib.readText(path.resolve(root, t.file_path)) || '';
  const edits = input.tool_name === 'MultiEdit' ? t.edits || [] : [t];
  for (const e of edits) {
    text = e.replace_all
      ? text.split(e.old_string).join(e.new_string)
      : text.replace(e.old_string, () => e.new_string);
  }
  return text;
}

function specGate(root, relPath) {
  const { cur, error } = lib.activeWork(root);
  if (error) return error;
  const parts = relPath.split('/');
  if (parts.length !== 4 || !lib.SPEC_FILES.includes(parts[3])) {
    return 'En specs/ solo existen specs/<module>/<id>-<slug>/{requirements,design,tasks}.md.';
  }
  const [, module, dir, file] = parts;

  if (cur.kind === 'fix') {
    if (!cur.approved) return 'Plan sin aprobar: el humano debe escribir "aprobado".';
    if (file === 'tasks.md') return 'Un fix no toca tasks.md (es el plan original de la feature).';
    if (!fs.existsSync(path.join(root, relPath))) return 'Un fix solo edita specs existentes; algo nuevo es una feature.';
    return null;
  }

  const f = lib.findFeature(lib.readFeatures(root), cur.id);
  if (!f) return `La feature ${cur.id} no existe en ${lib.FEATURES}.`;
  if (module !== f.module || dir !== `${f.id}-${cur.slug}`) {
    return `Solo se edita el spec de la feature activa: specs/${f.module}/${f.id}-${cur.slug}/.`;
  }
  const st = lib.statusOf(f);
  if (file === 'tasks.md') {
    return ['pending', 'spec_ready', 'in_progress'].includes(st) ? null : `tasks.md no se edita en \`${st}\`.`;
  }
  return ['pending', 'spec_ready'].includes(st)
    ? null
    : `${file} no se edita en \`${st}\`. Para reabrir el spec, el leader regresa la feature a spec_ready.`;
}

function transitionGate(root, f, from, to) {
  if (to === 'in_progress') return 'Solo el hook de aprobación pasa una feature a in_progress (el humano escribe "aprobado").';
  const { cur, error } = lib.activeWork(root);
  if (error) return error;
  if (cur.kind !== 'feature' || Number(cur.id) !== f.id) return `Solo cambia el status de la feature activa (${cur.kind} ${cur.id || cur.slug}).`;
  const dir = lib.specDir(root, f, cur.slug);
  switch (`${from}->${to}`) {
    case 'pending->spec_ready':
      return lib.specComplete(dir) ? null : 'spec_ready exige requirements.md, design.md y tasks.md.';
    case 'in_progress->spec_ready':
      return null; // reabrir el spec: exige un nuevo "aprobado"
    case 'in_progress->done':
      if (lib.hasPendingTasks(dir)) return 'done exige todas las tasks en [x].';
      if (!lib.reviewApproved(root)) return `done exige ${lib.REVIEW} con "VERDICT: APPROVED".`;
      return null;
    default:
      return `Transición no permitida: ${from} → ${to}.`;
  }
}

function featuresGate(root, newRaw) {
  let next;
  try {
    next = JSON.parse(newRaw);
  } catch {
    return `${lib.FEATURES} quedaría con JSON inválido.`;
  }
  const prevList = lib.featuresList(lib.readFeatures(root));
  const nextList = lib.featuresList(next);

  const ids = new Set();
  for (const f of nextList) {
    if (!Number.isInteger(f.id)) return `id inválido (${JSON.stringify(f.id)}): debe ser entero.`;
    if (ids.has(f.id)) return `id ${f.id} duplicado.`;
    ids.add(f.id);
  }
  for (const p of prevList) if (!ids.has(p.id)) return `No se borran features (id ${p.id}).`;

  for (const n of nextList) {
    const p = prevList.find((x) => x.id === n.id);
    if (!p) {
      if (n.status && n.status !== 'pending') return `La feature nueva ${n.id} nace sin status (pending).`;
      continue;
    }
    const { status: ps, ...pRest } = p;
    const { status: ns, ...nRest } = n;
    const from = ps || 'pending';
    const to = ns || 'pending';
    if (JSON.stringify(pRest) !== JSON.stringify(nRest) && from !== 'pending') {
      return `La feature ${n.id} está en \`${from}\`: solo cambia su status. Un cambio de requisitos va por /fix.`;
    }
    if (from !== to) {
      const why = transitionGate(root, n, from, to);
      if (why) return why;
    }
  }
  return null;
}

try {
  const input = lib.readInput();
  const root = lib.projectRoot(input);
  const t = input.tool_input || {};
  const relPath = lib.rel(root, t.file_path || t.notebook_path);

  let why = null;
  switch (lib.classify(relPath)) {
    case 'code':
      why = lib.codeGate(root);
      break;
    case 'spec':
      why = specGate(root, relPath);
      break;
    case 'features':
      why = featuresGate(root, contentAfter(root, input));
      break;
    case 'state':
      why = '.claude/state/ lo escriben solo los hooks y state.js (salvo review.md).';
      break;
    case 'harness':
      why = 'El harness (.claude/hooks, agents, skills, settings) lo edita el humano, no los agentes.';
      break;
    default:
      break;
  }
  if (why) lib.denyTool(why);
  process.exit(0);
} catch (e) {
  lib.denyTool(`Error interno del harness, edición bloqueada: ${e.message}`);
}
