'use strict';
// Utilidades compartidas del harness. Todas las rutas se resuelven contra la raíz
// del worktree actual, así cada worktree tiene su propio .claude/state/.

const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const FEATURES = 'docs/features.json';
const STATE_DIR = '.claude/state';
const CURRENT = `${STATE_DIR}/current.json`;
const REVIEW = `${STATE_DIR}/review.md`;
const SPEC_FILES = ['requirements.md', 'design.md', 'tasks.md'];
const FIX_TYPES = ['fix', 'chore', 'refactor'];
const MAIN_BRANCHES = ['main', 'master'];
const SLUG = /^[a-z0-9]+(-[a-z0-9]+)*$/;

function readInput() {
  try {
    const raw = fs.readFileSync(0, 'utf8');
    return raw.trim() ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

function git(args, cwd) {
  try {
    return execFileSync('git', args, { cwd, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
  } catch {
    return null;
  }
}

function projectRoot(input = {}) {
  if (process.env.CLAUDE_PROJECT_DIR) return path.resolve(process.env.CLAUDE_PROJECT_DIR);
  const cwd = input.cwd || process.cwd();
  return git(['rev-parse', '--show-toplevel'], cwd) || cwd;
}

function branch(root) {
  return git(['rev-parse', '--abbrev-ref', 'HEAD'], root);
}

// Ruta relativa a la raíz con "/"; null si queda fuera del worktree.
function rel(root, file) {
  if (!file) return null;
  const r = path.relative(root, path.resolve(root, file));
  if (!r || r.startsWith('..') || path.isAbsolute(r)) return null;
  return r.split(path.sep).join('/');
}

function readText(file) {
  try {
    return fs.readFileSync(file, 'utf8');
  } catch {
    return null;
  }
}

function readJson(file) {
  const raw = readText(file);
  if (raw === null) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function readFeatures(root) {
  return readJson(path.join(root, FEATURES));
}

function writeFeatures(root, data) {
  const file = path.join(root, FEATURES);
  const eol = (readText(file) || '').includes('\r\n') ? '\r\n' : '\n';
  fs.writeFileSync(file, JSON.stringify(data, null, 2).replace(/\n/g, eol) + eol);
}

function featuresList(data) {
  return data && Array.isArray(data.features) ? data.features : [];
}

function findFeature(data, id) {
  return featuresList(data).find((f) => Number(f.id) === Number(id)) || null;
}

// Sin campo status = pending.
function statusOf(feature) {
  return (feature && feature.status) || 'pending';
}

function unmetDeps(data, feature) {
  return (feature.depends_on || []).filter((id) => statusOf(findFeature(data, id)) !== 'done');
}

// specs/<module>/<id>-<slug>/ ; sin slug se busca por prefijo "<id>-".
function specDir(root, feature, slug) {
  if (!feature || !feature.module) return null;
  const base = path.join(root, 'specs', feature.module);
  if (slug) return path.join(base, `${feature.id}-${slug}`);
  try {
    const hit = fs.readdirSync(base).find((d) => d.startsWith(`${feature.id}-`));
    return hit ? path.join(base, hit) : null;
  } catch {
    return null;
  }
}

function specComplete(dir) {
  return !!dir && SPEC_FILES.every((f) => (readText(path.join(dir, f)) || '').trim().length > 0);
}

function hasPendingTasks(dir) {
  const tasks = dir && readText(path.join(dir, 'tasks.md'));
  return tasks === null || /^\s*[-*]\s*\[ \]/m.test(tasks);
}

function readCurrent(root) {
  return readJson(path.join(root, CURRENT));
}

function writeCurrent(root, obj) {
  fs.mkdirSync(path.join(root, STATE_DIR), { recursive: true });
  fs.writeFileSync(path.join(root, CURRENT), JSON.stringify(obj, null, 2) + '\n');
}

function reviewApproved(root) {
  return /^VERDICT:\s*APPROVED\b/m.test(readText(path.join(root, REVIEW)) || '');
}

// Trabajo activo válido para esta rama, o el motivo por el que no lo hay.
function activeWork(root) {
  const br = branch(root);
  if (!br) return { error: 'No se pudo leer la rama de git.' };
  if (MAIN_BRANCHES.includes(br)) {
    return { error: `Estás en \`${br}\`: nunca se edita aquí. Usa /feature <id> o /fix <descripción>.` };
  }
  const cur = readCurrent(root);
  if (!cur) return { error: 'Sin trabajo activo (.claude/state/current.json). Usa /feature <id> o /fix <descripción>.' };
  if (cur.branch !== br) {
    return { error: `El trabajo activo es de la rama \`${cur.branch}\` y estás en \`${br}\`.` };
  }
  return { cur, br };
}

// null = se puede tocar código (src/**, pom.xml); si no, el motivo.
function codeGate(root) {
  const { cur, error } = activeWork(root);
  if (error) return error;
  if (cur.kind === 'fix') {
    return cur.approved ? null : 'Plan sin aprobar: el humano debe escribir "aprobado".';
  }
  const data = readFeatures(root);
  const f = findFeature(data, cur.id);
  if (!f) return `La feature ${cur.id} no existe en ${FEATURES}.`;
  if (statusOf(f) !== 'in_progress') {
    return `La feature ${f.id} está en \`${statusOf(f)}\`: el código solo se toca en \`in_progress\` (tras "aprobado").`;
  }
  if (!specComplete(specDir(root, f, cur.slug))) return `Spec incompleto en specs/${f.module}/${f.id}-${cur.slug}/.`;
  const deps = unmetDeps(data, f);
  if (deps.length) return `Dependencias sin \`done\`: ${deps.join(', ')}.`;
  return null;
}

function classify(relPath) {
  if (!relPath) return 'outside';
  if (relPath === 'pom.xml' || relPath.startsWith('src/')) return 'code';
  if (relPath.startsWith('specs/')) return 'spec';
  if (relPath === FEATURES) return 'features';
  if (relPath === REVIEW) return 'review';
  if (relPath.startsWith(`${STATE_DIR}/`)) return 'state';
  if (/^\.claude\/(hooks|agents|skills)\//.test(relPath) || /^\.claude\/settings(\.local)?\.json$/.test(relPath)) {
    return 'harness';
  }
  return 'free';
}

function denyTool(reason) {
  process.stdout.write(
    JSON.stringify({
      hookSpecificOutput: {
        hookEventName: 'PreToolUse',
        permissionDecision: 'deny',
        permissionDecisionReason: `[harness] ${reason}`,
      },
    })
  );
  process.exit(0);
}

function askTool(reason) {
  process.stdout.write(
    JSON.stringify({
      hookSpecificOutput: {
        hookEventName: 'PreToolUse',
        permissionDecision: 'ask',
        permissionDecisionReason: `[harness] ${reason}`,
      },
    })
  );
  process.exit(0);
}

function addContext(event, text) {
  process.stdout.write(JSON.stringify({ hookSpecificOutput: { hookEventName: event, additionalContext: text } }));
  process.exit(0);
}

module.exports = {
  FEATURES,
  STATE_DIR,
  CURRENT,
  REVIEW,
  SPEC_FILES,
  FIX_TYPES,
  MAIN_BRANCHES,
  SLUG,
  readInput,
  git,
  projectRoot,
  branch,
  rel,
  readText,
  readFeatures,
  writeFeatures,
  featuresList,
  findFeature,
  statusOf,
  unmetDeps,
  specDir,
  specComplete,
  hasPendingTasks,
  readCurrent,
  writeCurrent,
  reviewApproved,
  activeWork,
  codeGate,
  classify,
  denyTool,
  askTool,
  addContext,
};
