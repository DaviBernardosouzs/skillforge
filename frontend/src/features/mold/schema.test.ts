import { EMPTY_MOLD, isComposable, moldSchema, toSkillDraft } from './schema'

test('descarta linhas em branco ao montar o SkillDraft', () => {
  const draft = toSkillDraft({
    ...EMPTY_MOLD,
    name: ' revisao ',
    purpose: 'Revisa PRs',
    triggers: [{ value: 'pedirem revisão' }, { value: '  ' }],
    steps: [
      { title: 'Leia', detail: 'Leia o diff inteiro.' },
      { title: '', detail: 'sem título' },
    ],
    examples: [{ input: 'a', expectedOutput: '' }],
    resources: [{ kind: 'REFERENCE', fileName: 'checklist', purpose: 'critérios' }],
  })

  expect(draft).toMatchObject({
    name: 'revisao',
    triggers: ['pedirem revisão'],
    steps: [{ title: 'Leia', detail: 'Leia o diff inteiro.' }],
    examples: [],
    resources: [{ kind: 'REFERENCE', fileName: 'checklist', purpose: 'critérios' }],
  })
  expect(isComposable(draft)).toBe(true)
})

test('obrigatórios faltando impedem a prévia', () => {
  expect(isComposable(toSkillDraft(EMPTY_MOLD))).toBe(false)
  const result = moldSchema.safeParse(EMPTY_MOLD)
  expect(result.success).toBe(false)
  const paths = result.success ? [] : result.error.issues.map((i) => i.path.join('.'))
  expect(paths).toEqual(expect.arrayContaining(['name', 'purpose', 'triggers', 'steps']))
})
