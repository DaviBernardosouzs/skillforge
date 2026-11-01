import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import type { SkillDraft } from '../api/types'
import { useLibraryStore } from '../features/library/store'
import { draftResponse, server } from '../test/msw'
import MoldPage from './MoldPage'

function renderMold() {
  return render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter initialEntries={['/molde']}>
        <Routes>
          <Route path="/molde" element={<MoldPage />} />
          <Route path="/bancada" element={<p>Bancada aberta</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => useLibraryStore.getState().reset())

test('monta as etapas com as perguntas que a API manda', async () => {
  renderMold()
  expect(await screen.findByText('Como a skill vai se chamar?')).toBeInTheDocument()
  expect(screen.getByText('O que a skill faz, em uma frase?')).toBeInTheDocument()

  await userEvent.click(screen.getByRole('button', { name: /Quando usar/ }))
  expect(screen.getByText('Em que situações usar?')).toBeInTheDocument()
  expect(screen.getByText('Quando não usar?')).toBeInTheDocument()
})

test('não avança sem os obrigatórios da etapa', async () => {
  renderMold()
  await screen.findByText('Como a skill vai se chamar?')
  await userEvent.click(screen.getByRole('button', { name: 'Próxima etapa' }))
  expect(await screen.findByText('Dê um nome à skill.')).toBeInTheDocument()
  expect(screen.getByRole('heading', { level: 2, name: 'Essência' })).toBeInTheDocument()
})

test('com os obrigatórios a prévia aparece com conselhos e leva a skill à bancada', async () => {
  let sent: SkillDraft | undefined
  server.use(
    http.post('*/api/skills/draft', async ({ request }) => {
      sent = (await request.json()) as SkillDraft
      return HttpResponse.json(draftResponse)
    }),
  )
  renderMold()
  const user = userEvent.setup()

  await user.type(await screen.findByRole('textbox', { name: /Nome/ }), 'revisao')
  await user.type(screen.getByRole('textbox', { name: /Propósito/ }), 'Revisa PRs')
  await user.click(screen.getByRole('button', { name: 'Próxima etapa' }))
  await user.type(
    await screen.findByRole('textbox', { name: 'Quando usar, item 1' }),
    'pedirem revisão{Enter}',
  )
  expect(screen.getByRole('textbox', { name: 'Quando usar, item 2' })).toHaveFocus()

  await user.click(screen.getByRole('button', { name: /Entradas e processo/ }))
  await user.type(screen.getByRole('textbox', { name: 'Título do passo 1' }), 'Leia')
  await user.type(screen.getByRole('textbox', { name: 'Detalhe do passo 1' }), 'Leia o diff inteiro antes.')

  expect(
    await screen.findByText('Adicione pelo menos um exemplo.', {}, { timeout: 3000 }),
  ).toBeInTheDocument()
  expect(sent).toMatchObject({ name: 'revisao', triggers: ['pedirem revisão'], steps: [{ title: 'Leia' }] })

  await user.click(screen.getByRole('button', { name: /no-examples/ }))
  expect(screen.getByRole('heading', { level: 2, name: 'Saída e exemplos' })).toBeInTheDocument()

  const take = screen.getByRole('button', { name: 'Levar à bancada' })
  await waitFor(() => expect(take).toBeEnabled())
  await user.click(take)
  expect(await screen.findByText('Bancada aberta')).toBeInTheDocument()
  expect(useLibraryStore.getState().draft.skills[0]?.content).toBe(draftResponse.skillMd)
})
