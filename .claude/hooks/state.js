#!/usr/bin/env node
'use strict';
// CLI del estado local del worktree (.claude/state/current.json). Nunca aprueba: eso solo lo hace approve.js.
//
//   node .claude/hooks/state.js start feature <id> <slug>   rama actual = feat/<slug>
//   node .claude/hooks/state.js start fix <slug>            rama actual = fix|chore|refactor/<slug>
//   node .claude/hooks/state.js revoke                      quita la aprobación de un fix (plan nuevo)
//   node .claude/hooks/state.js show
//   node .claude/hooks/state.js clear                       borra current.json y review.md

const fs = require('fs');
const path = require('path');
const lib = require('./lib');

function fail(msg) {
  process.stderr.write(`[harness] ${msg}\n`);
  process.exit(1);
}

const root = lib.projectRoot();
const br = lib.branch(root);
const cur = lib.readCurrent(root);
const [cmd, kind, ...rest] = process.argv.slice(2);

function startFeature(id, slug) {
  if (!/^\d+$/.test(id || '') || !lib.SLUG.test(slug || '')) fail('Uso: start feature <id> <slug-kebab-case>');
  if (br !== `feat/${slug}`) fail(`La rama debe ser feat/${slug} (actual: ${br}).`);
  const data = lib.readFeatures(root);
  const f = lib.findFeature(data, id);
  if (!f) fail(`La feature ${id} no existe en ${lib.FEATURES}.`);
  if (!f.module) fail(`La feature ${id} no tiene module.`);
  if (lib.statusOf(f) === 'done') fail(`La feature ${id} ya está done.`);
  const deps = lib.unmetDeps(data, f);
  if (deps.length) fail(`Dependencias sin done: ${deps.join(', ')}.`);
  if (cur && cur.branch === br && !(cur.kind === 'feature' && Number(cur.id) === f.id)) {
    fail(`Esta rama ya tiene otro trabajo activo (${cur.kind} ${cur.id || cur.slug}).`);
  }
  lib.writeCurrent(root, { kind: 'feature', id: f.id, slug, branch: br, startedAt: new Date().toISOString() });
  console.log(`Trabajo activo: feature ${f.id} (${br}) -> specs/${f.module}/${f.id}-${slug}/`);
}

function startFix(slug) {
  if (!lib.SLUG.test(slug || '')) fail('Uso: start fix <slug-kebab-case>');
  const type = (br || '').split('/')[0];
  if (!lib.FIX_TYPES.includes(type) || br !== `${type}/${slug}`) {
    fail(`La rama debe ser <${lib.FIX_TYPES.join('|')}>/${slug} (actual: ${br}).`);
  }
  if (cur && cur.branch === br) {
    if (cur.kind === 'fix' && cur.slug === slug) return console.log(`Trabajo activo sin cambios: ${br}.`);
    fail(`Esta rama ya tiene otro trabajo activo (${cur.kind} ${cur.id || cur.slug}).`);
  }
  lib.writeCurrent(root, { kind: 'fix', type, slug, branch: br, approved: false, startedAt: new Date().toISOString() });
  console.log(`Trabajo activo: ${type} ${slug} (${br}). Presenta el plan y espera "aprobado".`);
}

switch (cmd) {
  case 'start':
    if (kind === 'feature') startFeature(rest[0], rest[1]);
    else if (kind === 'fix') startFix(rest[0]);
    else fail('Uso: start feature <id> <slug> | start fix <slug>');
    break;
  case 'revoke':
    if (!cur || cur.kind !== 'fix') fail('No hay un fix activo.');
    lib.writeCurrent(root, { ...cur, approved: false });
    console.log('Aprobación retirada: presenta el plan nuevo y espera "aprobado".');
    break;
  case 'show':
    console.log(cur ? JSON.stringify(cur, null, 2) : 'Sin trabajo activo.');
    break;
  case 'clear':
    for (const f of [lib.CURRENT, lib.REVIEW]) fs.rmSync(path.join(root, f), { force: true });
    console.log('Estado local limpio.');
    break;
  default:
    fail('Uso: state.js start feature <id> <slug> | start fix <slug> | revoke | show | clear');
}
