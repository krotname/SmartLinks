export const dictionaries = {
  ru: {
    title: 'Управление ссылками', loginTitle: 'Вход · SmartLinks', language: 'Язык / Language', logout: 'Выйти',
    subtitle: 'Короткие ссылки на n-0.ru · SmartLinks 0.2.0', owner: 'Вход для владельца · SmartLinks 0.2.0',
    username: 'Логин', password: 'Пароль', signIn: 'Войти', site: 'На сайт',
    loginError: 'Не удалось войти. Проверьте логин и пароль.', csrfError: 'Не удалось подготовить безопасный запрос. Обновите страницу.',
    destination: 'Адрес назначения по умолчанию', code: 'Короткий код', generate: 'Сгенерировать код',
    codeHint: 'Латинские буквы, цифры, точка, дефис и подчёркивание. Код существующей ссылки сохраняется.',
    advanced: 'Умная ссылка: условия перехода', ruleHint: 'Правила проверяются сверху вниз. Используется первое совпадение всех условий. Затем — адрес по умолчанию. Даты и время — МСК (UTC+03:00), обе границы включены.',
    addRule: 'Добавить правило', saveCreate: 'Создать ссылку', saveEdit: 'Сохранить изменения',
    load: 'Открыть для редактирования', lookup: 'Код существующей ссылки', newLink: 'Новая ссылка',
    editHint: 'Редактирование. Если ссылка изменится в другой вкладке, сохранение будет отклонено. Загрузите её заново и перенесите изменения.',
    saved: 'Ссылка сохранена', shortUrl: 'Короткая ссылка', copy: 'Скопировать', copied: 'Ссылка скопирована.',
    manualCopy: 'Ссылка выделена для копирования вручную.', rule: 'Правило', ruleUrl: 'Адрес для этого правила',
    devices: 'Устройства', devicesHint: 'Через запятую: mobile — телефон/планшет, desktop — компьютер, unknown — не определено. Пусто — любое.',
    ruleLanguage: 'Языки', languageHint: 'Языковые теги через запятую, например ru, en-US. Пусто — любой язык.',
    start: 'Начало, МСК', end: 'Конец, МСК', up: 'Выше', down: 'Ниже', remove: 'Удалить правило',
    created: 'Создание и чтение подтверждены.', updated: 'Изменение и чтение подтверждены.', loaded: 'Ссылка загружена для редактирования.',
    conflict: 'Этот код уже занят. Откройте ссылку для редактирования или выберите другой код.',
    stale: 'Ссылка уже изменена. Сначала откройте актуальную версию и перенесите свои изменения.',
    expired: 'Сессия истекла. Войдите заново.', forbidden: 'Защита запроса отклонила операцию. Обновите страницу и войдите заново.',
    missing: 'Ссылка с таким кодом не найдена.', invalid: 'Проверьте адреса, короткий код и параметры правил.',
    failed: 'Сервис временно недоступен. Повторите чтение позже.', uncertain: 'Результат сохранения неизвестен. Сначала прочитайте ссылку по коду; не повторяйте сохранение вслепую.',
    mismatch: 'Сохранение принято, но прочитанная версия отличается. Загрузите актуальную ссылку перед новой правкой.',
    unsupported: 'Эта ссылка содержит условия, которые форма пока не может безопасно отобразить. Изменения не внесены.'
  },
  en: {
    title: 'Link management', loginTitle: 'Sign in · SmartLinks', language: 'Язык / Language', logout: 'Sign out',
    subtitle: 'Short links on n-0.ru · SmartLinks 0.2.0', owner: 'Owner sign-in · SmartLinks 0.2.0',
    username: 'Username', password: 'Password', signIn: 'Sign in', site: 'Back to website',
    loginError: 'Sign-in failed. Check your username and password.', csrfError: 'Could not prepare a secure request. Reload this page.',
    destination: 'Default destination URL', code: 'Short code', generate: 'Generate code',
    codeHint: 'Latin letters, digits, dots, hyphens and underscores. Existing short codes stay unchanged.',
    advanced: 'Smart link: redirect conditions', ruleHint: 'Rules run from top to bottom. The first rule matching all its conditions wins, followed by the default URL. All dates and times use Moscow time (UTC+03:00); both boundaries are inclusive.',
    addRule: 'Add rule', saveCreate: 'Create link', saveEdit: 'Save changes',
    load: 'Open for editing', lookup: 'Existing short code', newLink: 'New link',
    editHint: 'Editing. If another tab changes this link, saving will be rejected. Reload the link and reapply your changes.',
    saved: 'Link saved', shortUrl: 'Short URL', copy: 'Copy', copied: 'Link copied.',
    manualCopy: 'The link is selected for manual copying.', rule: 'Rule', ruleUrl: 'Destination for this rule',
    devices: 'Devices', devicesHint: 'Comma-separated: mobile — phone/tablet, desktop — computer, unknown — unidentified. Empty means any device.',
    ruleLanguage: 'Languages', languageHint: 'Comma-separated language tags, for example ru, en-US. Empty means any language.',
    start: 'Start, Moscow time', end: 'End, Moscow time', up: 'Move up', down: 'Move down', remove: 'Remove rule',
    created: 'Creation and readback confirmed.', updated: 'Update and readback confirmed.', loaded: 'Link loaded for editing.',
    conflict: 'This code is already taken. Open the link for editing or choose another code.',
    stale: 'The link has already changed. Reload the latest version before reapplying your changes.',
    expired: 'Your session has expired. Sign in again.', forbidden: 'Request protection rejected the operation. Reload this page and sign in again.',
    missing: 'No link with this code was found.', invalid: 'Check the destination URLs, short code and rule parameters.',
    failed: 'The service is temporarily unavailable. Try reading the link again later.', uncertain: 'The save outcome is unknown. Read the link by code before retrying; do not blindly repeat the save.',
    mismatch: 'The save was accepted, but readback differs. Reload the latest link before another edit.',
    unsupported: 'This link contains conditions the form cannot safely represent yet. No changes were made.'
  }
};

export const chooseLocale = (saved, browser) => ['ru', 'en'].includes(saved) ? saved : browser?.toLowerCase().startsWith('en') ? 'en' : 'ru';
export const translate = (locale, key) => dictionaries[locale]?.[key] ?? dictionaries.ru[key] ?? key;

export function initLocale(onChange = () => {}) {
  let saved; try { saved = localStorage.getItem('smartlinks.locale'); } catch { /* Storage is optional. */ }
  let locale = chooseLocale(saved, navigator.language);
  const apply = () => {
    document.documentElement.lang = locale;
    for (const node of document.querySelectorAll('[data-i18n]')) node.textContent = translate(locale, node.dataset.i18n);
    for (const select of document.querySelectorAll('[data-locale]')) select.value = locale;
    document.title = translate(locale, document.querySelector('#login') ? 'loginTitle' : 'title') + (document.querySelector('#login') ? '' : ' · SmartLinks');
    onChange();
  };
  for (const select of document.querySelectorAll('[data-locale]')) select.addEventListener('change', () => {
    locale = select.value;
    try { localStorage.setItem('smartlinks.locale', locale); } catch { /* Storage is optional. */ }
    apply();
  });
  return {t: key => translate(locale, key), apply};
}
