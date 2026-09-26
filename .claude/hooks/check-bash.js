#!/usr/bin/env node
'use strict';
// PreToolUse (Bash|PowerShell): heurística para que la shell no esquive check-spec-gate.js.
// No es infalible: detecta las formas habituales de escribir en rutas protegidas.

const lib = require('./lib');

const STATE_CLI = /^node\s+["']?(?:\$CLAUDE_PROJECT_DIR\/|\$env:CLAUDE_PROJECT_DIR\/|\.\/)?\.claude\/hooks\/state\.js["']?(\s|$)/;
const WRITER =
  /(^|[\s(])(tee|sed\s+-i|perl\s+-\w*i|cp|mv|rm|touch|truncate|dd|ln|Set-Content|Add-Content|Out-File|New-Item|Remove-Item|Copy-Item|Move-Item|Rename-Item|Clear-Content)(\s|$)|\bnode\s+-e\b|\bpython\d*(\.\d+)?\s+-c\b|WriteAllText|writeFileSync/i;
const CODE_REWRITE = /spotless:apply|\bgit\s+(restore|apply|clean|stash|reset\s+--hard|checkout\s+--)\b/i;
const NEEDS_HUMAN = /\bgit\s+(commit|push)\b|\bgh\s+pr\s+create\b/i;

const TARGETS = [
  { cat: 'state', re: /\.claude[\\/]state/i },
  { cat: 'harness', re: /\.claude[\\/](hooks|agents|skills|settings)/i },
  { cat: 'features', re: /features\.json/i },
  { cat: 'spec', re: /(^|[\s"'=\\/])specs[\\/]/i },
  { cat: 'code', re: /(^|[\s"'=\\/])src[\\/]|pom\.xml/i },
];

function stripNoise(seg) {
  return seg.replace(/\d?>&\d/g, '').replace(/\d?>{1,2}\s*(\/dev\/null|\$null|nul)\b/gi, '');
}

function redirectTargets(seg) {
  return [...seg.matchAll(/>{1,2}\s*["']?([^\s"';|&]+)/g)].map((m) => m[1]);
}

function categoriesWritten(seg) {
  const cats = new Set();
  for (const target of redirectTargets(seg)) {
    for (const { cat, re } of TARGETS) if (re.test(target)) cats.add(cat);
  }
  if (WRITER.test(seg)) {
    for (const { cat, re } of TARGETS) if (re.test(seg)) cats.add(cat);
  }
  if (CODE_REWRITE.test(seg)) cats.add('code');
  return cats;
}

try {
  const input = lib.readInput();
  const root = lib.projectRoot(input);
  const cmd = (input.tool_input || {}).command || '';
  const segments = cmd.split(/&&|\|\|?|;|\r?\n/).map((s) => stripNoise(s.trim())).filter(Boolean);

  let needsHuman = false;
  for (const seg of segments) {
    if (STATE_CLI.test(seg)) continue;
    if (NEEDS_HUMAN.test(seg)) needsHuman = true;
    const cats = categoriesWritten(seg);
    if (cats.has('state')) lib.denyTool('.claude/state/ solo se modifica con `node .claude/hooks/state.js`.');
    if (cats.has('harness')) lib.denyTool('El harness lo edita el humano, no los agentes.');
    if (cats.has('features')) lib.denyTool('features.json se edita con Edit/Write para que el hook valide el cambio.');
    if (cats.has('spec')) lib.denyTool('specs/ se edita con Edit/Write para que el hook valide el cambio.');
    if (cats.has('code')) {
      const why = lib.codeGate(root);
      if (why) lib.denyTool(`${why} (comando que escribe en src/ o pom.xml)`);
    }
  }
  if (needsHuman) lib.askTool('Commit, push y PR solo cuando el humano lo pide. Confirma.');
  process.exit(0);
} catch (e) {
  lib.denyTool(`Error interno del harness, comando bloqueado: ${e.message}`);
}
