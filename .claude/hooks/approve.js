#!/usr/bin/env node
'use strict';
// UserPromptSubmit: la única vía de aprobación. Solo actúa si el prompt del humano es exactamente "aprobado".
//   feature en spec_ready -> in_progress (en docs/features.json)
//   fix sin aprobar       -> approved: true (en .claude/state/current.json)

const lib = require('./lib');

function say(text) {
  lib.addContext('UserPromptSubmit', `[harness] ${text}`);
}

try {
  const input = lib.readInput();
  const prompt = (input.prompt || '').trim().toLowerCase().replace(/^[¡\s]+|[.!\s]+$/g, '');
  if (prompt !== 'aprobado') process.exit(0);

  const root = lib.projectRoot(input);
  const { cur, error } = lib.activeWork(root);
  if (error) say(`"aprobado" sin efecto: ${error}`);

  if (cur.kind === 'fix') {
    if (cur.approved) say('El plan ya estaba aprobado. Continúa la implementación.');
    lib.writeCurrent(root, { ...cur, approved: true, approvedAt: new Date().toISOString() });
    say(`Plan aprobado (${cur.branch}). Implementa según /fix; si sus instrucciones no están en contexto, pide al humano ejecutar /fix de nuevo.`);
  }

  const data = lib.readFeatures(root);
  const f = lib.findFeature(data, cur.id);
  if (!f) say(`"aprobado" sin efecto: la feature ${cur.id} no existe.`);
  const st = lib.statusOf(f);
  if (st === 'in_progress') say(`La feature ${f.id} ya estaba en in_progress. Continúa con /feature ${f.id}.`);
  if (st !== 'spec_ready') say(`"aprobado" sin efecto: la feature ${f.id} está en ${st}, no en spec_ready.`);
  if (!lib.specComplete(lib.specDir(root, f, cur.slug))) say(`"aprobado" sin efecto: spec incompleto para la feature ${f.id}.`);

  f.status = 'in_progress';
  lib.writeFeatures(root, data);
  say(
    `Spec aprobado: la feature ${f.id} pasó a in_progress. Continúa /feature ${f.id} con la fase de implementación; ` +
      `si sus instrucciones no están en contexto, pide al humano ejecutar /feature ${f.id}.`
  );
} catch (e) {
  lib.addContext('UserPromptSubmit', `[harness] Error interno al procesar "aprobado": ${e.message}. Nada fue aprobado.`);
}
