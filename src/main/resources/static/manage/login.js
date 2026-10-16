import {initLocale} from './i18n.mjs';
const message = document.querySelector('#message');
let messageKey = new URLSearchParams(location.search).has('error') ? 'loginError' : '';
const locale = initLocale(() => { message.textContent = messageKey ? locale.t(messageKey) : ''; });
locale.apply();
async function prepareLogin() {
  try {
    const response = await fetch('csrf', {cache:'no-store'});
    if (!response.ok) throw new Error();
    const csrf = await response.json();
    document.querySelector('#csrf').name = csrf.parameterName;
    document.querySelector('#csrf').value = csrf.token;
    document.querySelector('#submit').disabled = false;
  } catch { messageKey = 'csrfError'; locale.apply(); }
}
prepareLogin();