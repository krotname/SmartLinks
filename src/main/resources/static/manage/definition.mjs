const names = ['DeviceType', 'Language', 'DateRange'];

// This module maps form fields to the existing wire format; the server owns validation.
export function ruleToFields(rule) {
  if (rule.predicates.some(name => !names.includes(name))) throw new Error('unsupported');
  return {url: rule.redirectTo, devices: rule.predicates.includes('DeviceType') ? rule.args.devices.join(', ') : '',
    language: rule.predicates.includes('Language') ? rule.args.language.join(', ') : '',
    start: rule.predicates.includes('DateRange') ? rule.args.startWith : '',
    end: rule.predicates.includes('DateRange') ? rule.args.endWith : ''};
}

export function fieldsToRule(fields, original = {predicates: [], args: {}}) {
  const active = {DeviceType: Boolean(fields.devices.trim()), Language: Boolean(fields.language.trim()),
    DateRange: Boolean(fields.start || fields.end)};
  const predicates = [...original.predicates.filter(name => active[name]),
    ...names.filter(name => active[name] && !original.predicates.includes(name))];
  const args = {...original.args};
  for (const key of ['devices', 'language', 'startWith', 'endWith']) delete args[key];
  const list = value => value.split(',').map(part => part.trim());
  if (active.DeviceType) args.devices = list(fields.devices);
  if (active.Language) args.language = list(fields.language);
  if (active.DateRange) { args.startWith = fields.start; args.endWith = fields.end; }
  return {predicates, args, redirectTo: fields.url.trim()};
}

export function splitDefinition(link) {
  const fallback = link.rules.at(-1);
  if (!fallback || fallback.predicates.length) throw new Error('unsupported');
  return {conditional: link.rules.slice(0, -1).map(rule => ({fields: ruleToFields(rule), original: rule})), fallback};
}

export function sameDefinition(first, second) {
  const canonical = value => Array.isArray(value) ? value.map(canonical)
    : value && typeof value === 'object' ? Object.fromEntries(Object.keys(value).sort().map(key => [key, canonical(value[key])])) : value;
  return JSON.stringify(canonical(first)) === JSON.stringify(canonical(second));
}
