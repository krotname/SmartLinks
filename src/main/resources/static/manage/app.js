import {initLocale} from './i18n.mjs';
import {fieldsToRule, splitDefinition, sameDefinition} from './definition.mjs';

const form = document.querySelector('#create'), rules = document.querySelector('#rules');
const message = document.querySelector('#message'), result = document.querySelector('#result');
let current = null, messageKey = '', busy = false;
const originals = new WeakMap();
const locale = initLocale(() => { message.textContent = messageKey ? locale.t(messageKey) : ''; });
const show = key => { messageKey = key; message.textContent = key ? locale.t(key) : ''; };
locale.apply();

function setBusy(value) {
  busy = value;
  for (const control of document.querySelectorAll('button, input, select')) {
    if (!control.hasAttribute('data-locale')) control.disabled = value;
  }
  document.querySelector('#generate').disabled = value || Boolean(current);
}
function mode(saved) {
  current = saved;
  form.elements.code.readOnly = Boolean(saved);
  document.querySelector('#generate').disabled = Boolean(saved);
  document.querySelector('#edit-hint').hidden = !saved;
  document.querySelector('#save').dataset.i18n = saved ? 'saveEdit' : 'saveCreate';
  locale.apply();
}
async function request(url, options = {}) {
  const response = await fetch(url, {cache:'no-store', ...options});
  const error = ({400:'invalid',401:'expired',403:'forbidden',404:'missing',409:'conflict',412:'stale',428:'stale'})[response.status];
  if (response.redirected) throw new Error('expired');
  if (!response.ok) throw new Error(error || 'failed');
  return response;
}
async function csrfHeaders() {
  const csrf = await (await request('csrf')).json();
  return {'Content-Type':'application/json', [csrf.headerName]:csrf.token};
}
async function read(id) {
  const response = await request('api/smartlinks/' + encodeURIComponent(id));
  const definition = await response.json(), etag = response.headers.get('ETag');
  if (!etag) throw new Error('failed');
  return {definition, etag, url:response.headers.get('Content-Location')};
}
function appendRule(fields = {url:'',devices:'',language:'',start:'',end:''}, original) {
  const element = document.querySelector('#rule-template').content.cloneNode(true).firstElementChild;
  for (const [key, value] of Object.entries(fields)) element.querySelector('[data-' + key + ']').value = value;
  if (original) originals.set(element, original);
  element.querySelector('[data-remove]').onclick = () => element.remove();
  element.querySelector('[data-up]').onclick = () => {
    if (element.previousElementSibling) rules.insertBefore(element, element.previousElementSibling);
  };
  element.querySelector('[data-down]').onclick = () => {
    if (element.nextElementSibling) rules.insertBefore(element.nextElementSibling, element);
  };
  rules.append(element); locale.apply();
}
function definition() {
  const entries = Array.from(rules.children, element => fieldsToRule(
    Object.fromEntries(['url','devices','language','start','end'].map(key => [key,element.querySelector('[data-' + key + ']').value])),
    originals.get(element)));
  const fallback = current ? structuredClone(current.definition.rules.at(-1)) : {predicates:[],args:{}};
  entries.push({...fallback, redirectTo:form.elements.destination.value.trim()});
  return {id:form.elements.code.value.trim(), rules:entries};
}
document.querySelector('#lookup').addEventListener('submit', async event => {
  event.preventDefault(); if (busy) return;
  const code = event.currentTarget.elements.lookupCode.value.trim();
  setBusy(true); show('');
  try {
    const saved = await read(code);
    let parts; try { parts = splitDefinition(saved.definition); } catch { throw new Error('unsupported'); }
    rules.replaceChildren();
    for (const rule of parts.conditional) appendRule(rule.fields, rule.original);
    form.elements.destination.value = parts.fallback.redirectTo;
    form.elements.code.value = saved.definition.id;
    document.querySelector('#advanced').open = parts.conditional.length > 0;
    document.querySelector('#short-url').value = saved.url;
    result.hidden = true; mode(saved); show('loaded');
  } catch (error) { show(error instanceof TypeError ? 'failed' : error.message); }
  finally { setBusy(false); }
});
document.querySelector('#new').onclick = () => {
  if (busy) return;
  form.reset(); rules.replaceChildren(); mode(null); result.hidden = true; show('');
};
document.querySelector('#generate').onclick = () => {
  form.elements.code.value = Array.from(crypto.getRandomValues(new Uint8Array(8)), n => n.toString(16).padStart(2,'0')).join('');
};
document.querySelector('#add-rule').onclick = () => appendRule();
form.addEventListener('submit', async event => {
  event.preventDefault(); if (busy) return;
  const link = definition(), editing = Boolean(current);
  setBusy(true); result.hidden = true; show('');
  let dispatched = false;
  try {
    const headers = await csrfHeaders();
    if (editing) headers['If-Match'] = current.etag;
    dispatched = true;
    const response = await request('api/smartlinks' + (editing ? '/' + encodeURIComponent(link.id) : ''),
      {method:editing ? 'PUT':'POST', headers, body:JSON.stringify(link)});
    const write = await response.json(), saved = await read(link.id);
    if (!sameDefinition(saved.definition,link) || (editing && saved.etag !== response.headers.get('ETag'))) throw new Error('mismatch');
    if (!editing) document.querySelector('#short-url').value = write.url;
    mode(saved); result.hidden = false; show(editing ? 'updated':'created');
  } catch (error) { show(error instanceof TypeError ? (dispatched ? 'uncertain':'failed') : error.message); }
  finally { setBusy(false); }
});
document.querySelector('#copy').onclick = async () => {
  try { await navigator.clipboard.writeText(document.querySelector('#short-url').value); show('copied'); }
  catch { document.querySelector('#short-url').select(); show('manualCopy'); }
};
document.querySelector('#logout').onclick = async () => {
  if (busy) return;
  try {
    const headers = await csrfHeaders();
    const response = await fetch('logout', {method:'POST',headers});
    if (!response.ok) throw new Error('failed');
    location.assign('login');
  } catch (error) { show(error instanceof TypeError ? 'failed' : error.message); }
};
