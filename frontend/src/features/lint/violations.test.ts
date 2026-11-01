import type { Violation } from '../../api/types'
import type { SkillEntry } from '../library/types'
import { locateViolation, temperOf, violationsBySkill } from './violations'

const skill = (id: string, content: string): SkillEntry => ({
  id,
  content,
  files: [],
  category: '',
  tags: [],
  related: [],
})
const v = (
  ruleId: string,
  skillLabel: string,
  severity: 'ERROR' | 'WARNING' = 'ERROR',
  message = '',
): Violation => ({
  ruleId,
  skill: skillLabel,
  severity,
  message,
})

const skills = [
  skill('a', '---\nname: revisao\ndescription: x\n---\n'),
  skill('b', '# sem frontmatter'),
  skill('c', '---\ndescription: x\n---\n'),
  skill('d', '---\nname: revisao\n---\n'),
]

test('converte violações do ProblemDetail em marcações por skill', () => {
  const map = violationsBySkill(skills, [
    v('description-trigger', 'revisao', 'WARNING'),
    v('frontmatter-present', 'skill #2'),
    v('name-required', '(sem nome)'),
  ])

  expect(map.get('a')?.map((x) => x.ruleId)).toEqual(['description-trigger'])
  expect(map.get('b')?.map((x) => x.ruleId)).toEqual(['frontmatter-present'])
  expect(map.get('c')?.map((x) => x.ruleId)).toEqual(['name-required'])
  expect(map.get('d')?.map((x) => x.ruleId)).toEqual(['description-trigger'])
})

test('ignora violações de skills que não existem mais', () => {
  const map = violationsBySkill(skills, [v('name-format', 'fantasma'), v('frontmatter-present', 'skill #9')])
  expect([...map.values()].flat()).toHaveLength(0)
})

test('selo de têmpera segue a pior severidade', () => {
  expect(temperOf(undefined)).toBe('raw')
  expect(temperOf([])).toBe('tempered')
  expect(temperOf([v('x', 'a', 'WARNING')])).toBe('hot')
  expect(temperOf([v('x', 'a', 'WARNING'), v('y', 'a', 'ERROR')])).toBe('crack')
})

test('localiza a linha da trinca no SKILL.md', () => {
  const content = '---\nname: Ruim\ndescription: d\n---\n\n# Corpo\n\nVeja [x](references/nao.md).'
  expect(locateViolation(content, v('name-format', 'Ruim'))).toBe(2)
  expect(locateViolation(content, v('description-trigger', 'Ruim'))).toBe(3)
  expect(
    locateViolation(
      content,
      v('references-exist', 'Ruim', 'ERROR', "O corpo linka 'references/nao.md', mas..."),
    ),
  ).toBe(8)
  expect(locateViolation(content, v('body-length', 'Ruim'))).toBe(6)
  expect(locateViolation('# sem', v('frontmatter-present', 'skill #1'))).toBe(1)
})
