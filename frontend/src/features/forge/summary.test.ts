import { ApiError, NetworkError } from '../../api/problem'
import { EMPTY_DRAFT, type SkillEntry } from '../library/types'
import { describeForgeError, nextSteps, summarize } from './summary'

const skill = (category: string, files = 0): SkillEntry => ({
  id: category + files,
  content: '',
  files: Array.from({ length: files }, (_, i) => ({
    id: `${i}`,
    path: `references/${i}.md`,
    content: '',
    encoding: 'utf8',
  })),
  category,
  tags: [],
  related: [],
})

test('conta skills, arquivos e categorias distintas, sem as em branco', () => {
  const summary = summarize({
    ...EMPTY_DRAFT,
    skills: [skill('dev', 2), skill(' dev '), skill(''), skill('docs', 1)],
  })
  expect(summary).toEqual({ skills: 4, categories: ['dev', 'docs'], files: 3, license: 'MIT' })
})

test('cada falha tem uma mensagem própria', () => {
  expect(describeForgeError(new ApiError(413, { detail: 'Limite X.' }))).toMatchObject({
    retry: false,
    detail: expect.stringContaining('Limite X.'),
  })
  expect(
    describeForgeError(new ApiError(400, { errors: [{ field: 'name', message: 'inválido' }] })).detail,
  ).toBe('name: inválido')
  expect(describeForgeError(new ApiError(500, {}))).toMatchObject({ retry: true })
  expect(describeForgeError(new NetworkError())).toMatchObject({
    retry: true,
    title: 'Sem contato com a forja',
  })
})

test('próximos passos usam o nome da biblioteca', () => {
  expect(nextSteps('minhas-skills')).toBe(
    'unzip minhas-skills.zip && cd minhas-skills\n' +
      'git remote add origin git@github.com:<usuario>/minhas-skills.git\n' +
      'git push -u origin main',
  )
})
