'use strict';
async function prepareLogin() {
  const message = document.querySelector('#message');
  if (new URLSearchParams(location.search).has('error')) message.textContent = 'Не удалось войти. Проверьте логин и пароль.';
  try {
    const response = await fetch('csrf', {cache: 'no-store'});
    if (!response.ok) throw new Error();
    const csrf = await response.json();
    document.querySelector('#csrf').name = csrf.parameterName;
    document.querySelector('#csrf').value = csrf.token;
    document.querySelector('#submit').disabled = false;
  } catch {
    message.textContent = 'Не удалось подготовить безопасный вход. Обновите страницу.';
  }
}
prepareLogin();
