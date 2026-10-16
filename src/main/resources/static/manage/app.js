'use strict';
const form = document.querySelector('#create');
const rules = document.querySelector('#rules');
const message = document.querySelector('#message');
const result = document.querySelector('#result');
let csrf;

async function loadCsrf() {
  const response = await fetch('csrf', {cache: 'no-store'});
  if (!response.ok) throw new Error('Не удалось подготовить запрос. Обновите страницу.');
  csrf = await response.json();
}

document.querySelector('#generate').addEventListener('click', () => {
  form.elements.code.value = Array.from(crypto.getRandomValues(new Uint8Array(8)), n => n.toString(16).padStart(2, '0')).join('');
});

document.querySelector('#add-rule').addEventListener('click', () => {
  const element = document.querySelector('#rule-template').content.cloneNode(true).firstElementChild;
  element.querySelector('[data-remove]').onclick = () => element.remove();
  element.querySelector('[data-up]').onclick = () => {
    if (element.previousElementSibling) rules.insertBefore(element, element.previousElementSibling);
  };
  element.querySelector('[data-down]').onclick = () => {
    if (element.nextElementSibling) rules.insertBefore(element.nextElementSibling, element);
  };
  rules.append(element);
});

// This serializes the existing API format. Validation and predicate evaluation stay on the server.
function definition() {
  const entries = Array.from(rules.children, element => {
    const predicates = [], args = {};
    const value = selector => element.querySelector(selector).value.trim();
    if (value('[data-device]')) {
      predicates.push('DeviceType'); args.devices = [value('[data-device]')];
    }
    if (value('[data-language]')) {
      predicates.push('Language'); args.language = value('[data-language]').split(',').map(tag => tag.trim());
    }
    if (value('[data-start]') || value('[data-end]')) {
      predicates.push('DateRange'); args.startWith = value('[data-start]'); args.endWith = value('[data-end]');
    }
    return {predicates, args, redirectTo: value('[data-url]')};
  });
  entries.push({predicates: [], args: {}, redirectTo: form.elements.destination.value.trim()});
  return {id: form.elements.code.value.trim(), rules: entries};
}

async function readSaved(id) {
  const response = await fetch('api/smartlinks/' + encodeURIComponent(id), {cache: 'no-store'});
  if (!response.ok) throw new Error('Не удалось проверить сохранённую ссылку. Проверьте вход и повторите чтение.');
  return response.json();
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  result.hidden = true;
  message.textContent = '';
  document.querySelector('#save').disabled = true;
  try {
    await loadCsrf();
    const link = definition();
    const response = await fetch('api/smartlinks', {method: 'POST',
      headers: {'Content-Type': 'application/json', [csrf.headerName]: csrf.token}, body: JSON.stringify(link)});
    if (response.status === 409) throw new Error('Этот код уже занят. Выберите другой код.');
    if (response.status === 401 || response.redirected) throw new Error('Сессия истекла. Войдите заново.');
    if (response.status === 403) throw new Error('Защита запроса отклонила создание. Обновите страницу и войдите заново.');
    if (!response.ok) throw new Error('Ссылка не создана: ' + await response.text());
    const created = await response.json();
    const saved = await readSaved(created.id);
    if (saved.id !== link.id || JSON.stringify(saved.rules) !== JSON.stringify(link.rules)) {
      // JSON object key order is not significant; compare the returned definition explicitly.
      if (saved.id !== link.id || saved.rules.length !== link.rules.length || saved.rules.some((rule, i) =>
        rule.redirectTo !== link.rules[i].redirectTo || JSON.stringify(rule.predicates) !== JSON.stringify(link.rules[i].predicates)
        || Object.keys(rule.args).length !== Object.keys(link.rules[i].args).length
        || Object.keys(rule.args).some(key => JSON.stringify(rule.args[key]) !== JSON.stringify(link.rules[i].args[key])))) {
        throw new Error('Ссылка создана, но результат чтения отличается. Не повторяйте создание с тем же кодом.');
      }
    }
    document.querySelector('#short-url').value = created.url;
    result.hidden = false;
    message.textContent = 'Создание и чтение подтверждены.';
  } catch (error) {
    message.textContent = error instanceof TypeError ? 'Ответ не получен. Код мог сохраниться; не повторяйте создание вслепую.' : error.message;
  } finally {
    document.querySelector('#save').disabled = false;
  }
});

document.querySelector('#copy').onclick = async () => {
  try { await navigator.clipboard.writeText(document.querySelector('#short-url').value); message.textContent = 'Ссылка скопирована.'; }
  catch { document.querySelector('#short-url').select(); message.textContent = 'Выделенная ссылка готова для копирования вручную.'; }
};
document.querySelector('#logout').onclick = async () => {
  try {
    await loadCsrf();
    const response = await fetch('logout', {method: 'POST', headers: {[csrf.headerName]: csrf.token}});
    if (!response.ok) throw new Error('Не удалось выйти.');
    location.assign('login');
  } catch (error) { message.textContent = error.message; }
};
