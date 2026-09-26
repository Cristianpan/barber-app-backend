#!/usr/bin/env node
'use strict';
// SessionStart: copia .env si falta en este worktree e inyecta el trabajo activo en el contexto.

const fs = require('fs');
const path = require('path');
const lib = require('./lib');

const lines = [];

function copyEnvFromMainWorktree(root) {
  if (fs.existsSync(path.join(root, '.env'))) return;
  const list = lib.git(['worktree', 'list', '--porcelain'], root) || '';
  const first = list.split(/\r?\n/).find((l) => l.startsWith('worktree '));
  const mainWt = first && first.slice('worktree '.length).trim();
  if (!mainWt || path.resolve(mainWt) === path.resolve(root)) return;
  const source = path.join(mainWt, '.env');
  if (!fs.existsSync(source)) return;
  fs.copyFileSync(source, path.join(root, '.env'));
  lines.push('.env copiado desde el worktree principal.');
}

function describeWork(root) {
  const br = lib.branch(root);
  const cur = lib.readCurrent(root);
  if (!cur) {
    return `Rama \`${br}\`, sin trabajo activo. Flujos: /feature <id> (feature nueva) o /fix <descripción> (fix, chore, refactor).`;
  }
  if (cur.branch !== br) {
    return `Hay estado de la rama \`${cur.branch}\` y estás en \`${br}\`. Vuelve a esa rama o, si ya no aplica, \`node .claude/hooks/state.js clear\`.`;
  }
  if (cur.kind === 'fix') {
    return cur.approved
      ? `Trabajo activo: ${cur.type} ${cur.slug} (\`${br}\`), plan aprobado. Para continuar, el humano ejecuta /fix.`
      : `Trabajo activo: ${cur.type} ${cur.slug} (\`${br}\`), plan sin aprobar.`;
  }
  const f = lib.findFeature(lib.readFeatures(root), cur.id);
  return `Trabajo activo: feature ${cur.id} "${f ? f.name : '?'}" (\`${br}\`), status ${lib.statusOf(f)}. Retomar con /feature ${cur.id}.`;
}

try {
  const input = lib.readInput();
  const root = lib.projectRoot(input);
  copyEnvFromMainWorktree(root);
  lines.push(describeWork(root));
  lib.addContext('SessionStart', `[harness] ${lines.join(' ')}`);
} catch (e) {
  lib.addContext('SessionStart', `[harness] No se pudo leer el estado: ${e.message}`);
}
