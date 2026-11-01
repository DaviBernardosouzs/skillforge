import { findLine, readFrontmatterField } from './frontmatter'

test('lê name e description do frontmatter', () => {
  const md = '---\nname: revisao\ndescription: "Revisa: PRs."\n---\n# corpo'
  expect(readFrontmatterField(md, 'name')).toBe('revisao')
  expect(readFrontmatterField(md, 'description')).toBe('Revisa: PRs.')
})

test('sem frontmatter devolve vazio', () => {
  expect(readFrontmatterField('# só corpo', 'name')).toBe('')
  expect(readFrontmatterField('---\nname: a\nsem fechar', 'name')).toBe('')
})

test('acha a linha de um trecho', () => {
  expect(findLine('a\nb\nreferences/x.md', 'references/x.md')).toBe(3)
  expect(findLine('a', 'zzz')).toBeNull()
})
