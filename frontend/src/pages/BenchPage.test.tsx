import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { useImperativeHandle } from 'react'
import { MemoryRouter } from 'react-router-dom'
import type { SkillEditorHandle } from '../features/editor/SkillEditor'
import { useLibraryStore } from '../features/library/store'
import { report, server } from '../test/msw'
import BenchPage from './BenchPage'

const goToLine = vi.fn()

// CodeMirror não roda no jsdom: um textarea faz o papel do editor
vi.mock('../features/editor/SkillEditor', () => ({
  default: function FakeEditor(props: {
    value: string
    onChange: (v: string) => void
    ariaLabel: string
    ref?: React.Ref<SkillEditorHandle>
  }) {
    useImperativeHandle(props.ref, () => ({ goToLine }))
    return (
      <textarea
        aria-label={props.ariaLabel}
        value={props.value}
        onChange={(e) => props.onChange(e.target.value)}
      />
    )
  },
}))

const CONTENT =
  '---\nname: revisao\ndescription: Revisa. Use quando pedirem.\n---\n\n# Revisao\n\nVeja [x](references/nao.md).'

function renderBench() {
  return render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter>
        <BenchPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  goToLine.mockClear()
  useLibraryStore.getState().reset()
  useLibraryStore.getState().setMeta({ name: 'minhas-skills', author: 'Ana', license: 'MIT' })
  useLibraryStore.getState().addSkill({ content: CONTENT })
})

test('trinca bloqueia o Forjar com explicação e leva o cursor ao trecho', async () => {
  const violation = {
    ruleId: 'references-exist',
    severity: 'ERROR' as const,
    skill: 'revisao',
    message: "O corpo linka 'references/nao.md', mas esse arquivo não foi enviado.",
  }
  server.use(
    http.post('*/api/skills/lint', () => HttpResponse.json(report([violation]))),
    http.post('*/api/libraries/lint', () => HttpResponse.json(report([violation]))),
  )
  renderBench()

  const forge = screen.getByRole('button', { name: 'Forjar' })
  await waitFor(
    () => expect(screen.getAllByText('Link para arquivo que não existe').length).toBeGreaterThan(0),
    {
      timeout: 3000,
    },
  )
  await waitFor(() => expect(forge).toBeDisabled())
  expect(forge).toHaveAccessibleDescription('Há 1 trinca. Corrija antes de forjar.')

  await userEvent.click(screen.getByRole('button', { name: /Link para arquivo que não existe/ }))
  await waitFor(() => expect(goToLine).toHaveBeenCalledWith(8))
})

test('biblioteca aprovada libera o Forjar', async () => {
  renderBench()
  const forge = screen.getByRole('button', { name: 'Forjar' })
  await waitFor(() => expect(forge).toBeEnabled(), { timeout: 3000 })
})

test('nome de biblioteca inválido bloqueia sem chamar a API', async () => {
  useLibraryStore.getState().setMeta({ name: 'Nome Ruim' })
  renderBench()
  expect(await screen.findByText('O nome da biblioteca precisa estar em kebab-case.')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Forjar' })).toBeDisabled()
})

test('422 na forja trinca a peça: volta à bancada com a violação marcada e o Forjar bloqueado', async () => {
  const violation = {
    ruleId: 'body-not-empty',
    severity: 'ERROR' as const,
    skill: 'revisao',
    message: 'O corpo do SKILL.md está vazio.',
  }
  server.use(
    http.post('*/api/libraries/generate', () =>
      HttpResponse.json(
        { title: 'Biblioteca inválida', status: 422, violations: [violation] },
        { status: 422 },
      ),
    ),
  )
  renderBench()
  const forge = screen.getByRole('button', { name: 'Forjar' })
  await waitFor(() => expect(forge).toBeEnabled(), { timeout: 3000 })
  await userEvent.click(forge)
  const dialog = await screen.findByRole('dialog')
  await userEvent.click(within(dialog).getByRole('button', { name: 'Forjar' }))

  expect(
    await screen.findByText(/A peça trincou na forja: 1 violação marcada/, {}, { timeout: 3000 }),
  ).toBeInTheDocument()
  expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Forjar' })).toBeDisabled()

  useLibraryStore.getState().setMeta({ author: 'Ana Maria' })
  await waitFor(() => expect(screen.queryByText(/A peça trincou/)).not.toBeInTheDocument())
})
