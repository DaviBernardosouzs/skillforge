import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import type { DraftField, DraftResponse, LintReport, TemplateSummary, Violation } from '../api/types'

export const report = (violations: Violation[] = []): LintReport => ({
  violations,
  errorCount: violations.filter((v) => v.severity === 'ERROR').length,
  warningCount: violations.filter((v) => v.severity === 'WARNING').length,
  hasErrors: violations.some((v) => v.severity === 'ERROR'),
})

export const templates: TemplateSummary[] = [
  { id: 'code-review', title: 'Revisão de código', description: 'Revisa mudanças seguindo um checklist.' },
]

export const draftFields: DraftField[] = [
  {
    name: 'name',
    label: 'Nome',
    required: true,
    type: 'TEXT',
    question: 'Como a skill vai se chamar?',
    example: 'revisao-de-codigo',
    hint: 'kebab-case',
  },
  {
    name: 'purpose',
    label: 'Propósito',
    required: true,
    type: 'TEXTAREA',
    question: 'O que a skill faz, em uma frase?',
    example: 'Revisa PRs.',
    hint: '',
  },
  {
    name: 'triggers',
    label: 'Quando usar',
    required: true,
    type: 'TEXT_LIST',
    question: 'Em que situações usar?',
    example: 'o usuário pedir revisão',
    hint: '',
  },
  {
    name: 'antiTriggers',
    label: 'Quando NÃO usar',
    required: false,
    type: 'TEXT_LIST',
    question: 'Quando não usar?',
    example: 'escrever do zero',
    hint: '',
  },
  {
    name: 'inputs',
    label: 'Entradas',
    required: false,
    type: 'TEXT_LIST',
    question: 'O que a skill recebe?',
    example: 'o diff',
    hint: '',
  },
  {
    name: 'steps',
    label: 'Processo',
    required: true,
    type: 'STEP_LIST',
    question: 'Quais são os passos?',
    example: 'Leia o diff',
    hint: '',
  },
  {
    name: 'outputFormat',
    label: 'Formato da saída',
    required: false,
    type: 'TEXTAREA',
    question: 'Como é a entrega?',
    example: 'Lista',
    hint: '',
  },
  {
    name: 'examples',
    label: 'Exemplos',
    required: false,
    type: 'EXAMPLE_LIST',
    question: 'Um exemplo?',
    example: 'entrada → saída',
    hint: '',
  },
  {
    name: 'pitfalls',
    label: 'Armadilhas',
    required: false,
    type: 'TEXT_LIST',
    question: 'Erros comuns?',
    example: 'só estilo',
    hint: '',
  },
  {
    name: 'qualityChecks',
    label: 'Verificação antes de entregar',
    required: false,
    type: 'TEXT_LIST',
    question: 'O que conferir?',
    example: 'riscos explicados',
    hint: '',
  },
  {
    name: 'resources',
    label: 'Recursos',
    required: false,
    type: 'RESOURCE_LIST',
    question: 'Precisa de arquivos?',
    example: 'checklist.md',
    hint: '',
  },
]

export const draftResponse: DraftResponse = {
  skillMd:
    '---\nname: revisao\ndescription: Revisa PRs. Use quando o usuário pedir revisão.\n---\n\n# Revisao\n',
  stubFiles: [],
  lint: report(),
  hints: [{ id: 'no-examples', message: 'Adicione pelo menos um exemplo.' }],
}

export const handlers = [
  http.get('*/api/templates', () => HttpResponse.json(templates)),
  http.post('*/api/skills/lint', () => HttpResponse.json(report())),
  http.post('*/api/libraries/lint', () => HttpResponse.json(report())),
  http.get('*/api/skills/draft/template', () => HttpResponse.json(draftFields)),
  http.post('*/api/skills/draft', () => HttpResponse.json(draftResponse)),
]

export const server = setupServer(...handlers)
