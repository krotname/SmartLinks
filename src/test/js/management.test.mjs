import {test} from 'node:test';
import assert from 'node:assert/strict';
import {dictionaries, translate, chooseLocale} from '../../main/resources/static/manage/i18n.mjs';
import {ruleToFields, fieldsToRule, splitDefinition, sameDefinition} from '../../main/resources/static/manage/definition.mjs';

test('Russian and English have exactly the same translation keys', () => {
  assert.deepEqual(Object.keys(dictionaries.ru).sort(), Object.keys(dictionaries.en).sort());
  for (const locale of ['ru', 'en']) for (const value of Object.values(dictionaries[locale])) assert.ok(value.length);
  assert.equal(translate('en', 'saveEdit'), 'Save changes');
  assert.equal(translate('ru', 'saveEdit'), 'Сохранить изменения');
});
test('explicit locale wins over browser language; unsupported locales use Russian', () => {
  assert.equal(chooseLocale('ru', 'en-US'), 'ru');
  assert.equal(chooseLocale(null, 'en-US'), 'en');
  assert.equal(chooseLocale('de', 'de-DE'), 'ru');
});
test('smart rules round-trip multiple devices, languages, dates, predicate order and extra arguments', () => {
  const original = {predicates: ['Language', 'DeviceType', 'DateRange'], args: {
    language: ['ru', 'en-US'], devices: ['desktop', 'mobile'],
    startWith: '2026-10-01T00:00:00', endWith: '2027-01-01T00:00:00', metadata: 'keep'},
    redirectTo: 'https://example.com/smart'};
  assert.deepEqual(fieldsToRule(ruleToFields(original), original), original);
});
test('changing a field preserves unrelated rules and arguments', () => {
  const original = {predicates: ['DeviceType'], args: {devices: ['mobile'], metadata: 7}, redirectTo: 'https://example.com/old'};
  const fields = {...ruleToFields(original), url: 'https://example.com/new'};
  assert.equal(fieldsToRule(fields, original).redirectTo, 'https://example.com/new');
  assert.equal(fieldsToRule(fields, original).args.metadata, 7);
});
test('unsupported future predicates cannot be silently dropped by the editor', () => {
  assert.throws(() => ruleToFields({predicates: ['Future'], args: {}, redirectTo: 'https://example.com/'}));
  assert.throws(() => splitDefinition({id: 'a', rules: [{predicates: ['Language'], args: {language:['ru']}, redirectTo:'https://example.com/'}]}));
});
test('readback comparison ignores argument key order but preserves rule order', () => {
  const a = {id:'a', rules:[{predicates:['Language'],args:{language:['ru'],other:1},redirectTo:'https://example.com/'},
    {predicates:[],args:{},redirectTo:'https://example.com/fallback'}]};
  const b = structuredClone(a); b.rules[0].args = {other:1,language:['ru']};
  assert.ok(sameDefinition(a,b));
  b.rules.reverse(); assert.equal(sameDefinition(a,b),false);
});
