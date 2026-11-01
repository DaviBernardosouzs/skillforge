import { createSafeStorage } from '../../lib/safeStorage'
import { parseDraftFile, toDraftFile } from './draftFile'
import { toLibraryRequest } from './request'
import { DRAFT_STORAGE_KEY, draftStorage, useLibraryStore } from './store'

const md = (name: string) =>
  `---\nname: ${name}\ndescription: Faz ${name}. Use quando pedirem.\n---\n# ${name}`

beforeEach(() => {
  useLibraryStore.getState().reset()
  localStorage.clear()
})

test('adiciona skill e seleciona a nova', () => {
  const id = useLibraryStore
    .getState()
    .addSkill({ content: md('a'), files: [{ path: 'references/x.md', content: 'x', encoding: 'utf8' }] })
  const { draft, selectedId } = useLibraryStore.getState()
  expect(draft.skills).toHaveLength(1)
  expect(selectedId).toBe(id)
  expect(draft.skills[0]?.files[0]?.id).toBeTruthy()
})

test('remove e desfaz na mesma posição', () => {
  const store = useLibraryStore.getState()
  store.addSkill({ content: md('a') })
  const b = store.addSkill({ content: md('b') })
  store.addSkill({ content: md('c') })

  const removed = useLibraryStore.getState().removeSkill(b)
  expect(useLibraryStore.getState().draft.skills.map((s) => s.content)).toEqual([md('a'), md('c')])
  if (!removed) throw new Error('skill não removida')

  useLibraryStore.getState().restoreSkill(removed)
  expect(useLibraryStore.getState().draft.skills.map((s) => s.content)).toEqual([md('a'), md('b'), md('c')])
})

test('reordena sem sair dos limites', () => {
  const store = useLibraryStore.getState()
  const a = store.addSkill({ content: md('a') })
  store.addSkill({ content: md('b') })

  useLibraryStore.getState().moveSkill(a, 1)
  expect(useLibraryStore.getState().draft.skills[1]?.id).toBe(a)
  useLibraryStore.getState().moveSkill(a, 1)
  expect(useLibraryStore.getState().draft.skills[1]?.id).toBe(a)
})

test('duplica com ids novos', () => {
  const a = useLibraryStore
    .getState()
    .addSkill({ content: md('a'), files: [{ path: 'scripts/x.sh', content: '', encoding: 'utf8' }] })
  const copy = useLibraryStore.getState().duplicateSkill(a)
  const skills = useLibraryStore.getState().draft.skills
  expect(skills).toHaveLength(2)
  expect(copy).not.toBe(a)
  expect(skills[1]?.files[0]?.id).not.toBe(skills[0]?.files[0]?.id)
})

test('persiste no localStorage depois do debounce', () => {
  useLibraryStore.getState().addSkill({ content: md('a') })
  draftStorage.flush()
  expect(localStorage.getItem(DRAFT_STORAGE_KEY)).toContain('name: a')
})

test('localStorage falhando não derruba nada', () => {
  const broken = {
    getItem: () => {
      throw new Error('SecurityError')
    },
    setItem: () => {
      throw new Error('QuotaExceededError')
    },
    removeItem: () => {
      throw new Error('SecurityError')
    },
  } as unknown as Storage
  const warn = vi.spyOn(console, 'warn').mockImplementation(() => {})
  const storage = createSafeStorage(0, () => broken)

  expect(storage.getItem('x')).toBeNull()
  expect(() => {
    storage.setItem('x', '1')
    storage.flush()
    storage.removeItem('x')
  }).not.toThrow()
  expect(warn).toHaveBeenCalled()
  warn.mockRestore()
})

test('monta o LibraryRequest a partir do estado', () => {
  const store = useLibraryStore.getState()
  store.setMeta({ name: 'minhas-skills', author: ' Bernardo ', license: 'APACHE_2_0' })
  store.addSkill({
    content: md('a'),
    files: [{ path: ' references/x.md ', content: 'eA==', encoding: 'base64' }],
    category: ' dev ',
    tags: ['java', ' '],
    related: ['b'],
  })

  expect(toLibraryRequest(useLibraryStore.getState().draft)).toEqual({
    name: 'minhas-skills',
    author: 'Bernardo',
    license: 'APACHE_2_0',
    skills: [
      {
        content: md('a'),
        files: [{ path: 'references/x.md', content: 'eA==', encoding: 'base64' }],
        category: 'dev',
        tags: ['java'],
        related: ['b'],
      },
    ],
  })
})

test('exporta e importa o rascunho', () => {
  useLibraryStore.getState().setMeta({ name: 'lib', author: 'Ana' })
  useLibraryStore.getState().addSkill({ content: md('a'), tags: ['x'] })
  const text = JSON.stringify(toDraftFile(useLibraryStore.getState().draft))

  const imported = parseDraftFile(text)
  expect(imported.name).toBe('lib')
  expect(imported.skills[0]?.tags).toEqual(['x'])
  expect(() => parseDraftFile('{"nada": 1}')).toThrow(/rascunho/)
  expect(() => parseDraftFile('não é json')).toThrow(/JSON/)
})
