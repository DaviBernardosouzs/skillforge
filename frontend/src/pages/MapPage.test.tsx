import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useLibraryStore } from '../features/library/store'
import { server } from '../test/msw'
import MapPage from './MapPage'

const node = (id: string, type: string, label = id) => ({
  id,
  type,
  label,
  path: null,
  description: null,
  category: null,
  tags: [],
})

const graph = {
  nodes: [
    node('index', 'INDEX', 'minhas-skills'),
    node('category:dev', 'CATEGORY', 'dev'),
    node('skill:revisao', 'SKILL', 'revisao'),
  ],
  edges: [
    { from: 'index', to: 'category:dev', type: 'CONTAINS' },
    { from: 'category:dev', to: 'skill:revisao', type: 'CONTAINS' },
  ],
}

function renderMap() {
  return render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter initialEntries={['/mapa']}>
        <Routes>
          <Route path="/mapa" element={<MapPage />} />
          <Route path="/bancada" element={<p>bancada aberta</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  useLibraryStore.getState().reset()
})

test('biblioteca incompleta explica o que falta em vez de chamar a API', () => {
  renderMap()
  expect(screen.getByText('Adicione pelo menos uma skill ao estoque.')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Ir para a bancada' })).toBeInTheDocument()
})

test('desenha o grafo e o clique numa skill abre a bancada com ela selecionada', async () => {
  useLibraryStore.getState().setMeta({ name: 'minhas-skills', author: '', license: 'MIT' })
  useLibraryStore.getState().addSkill({
    content: '---\nname: revisao\ndescription: Revisa. Use quando pedirem.\n---\n\n# Revisao\n',
  })
  let body: { author?: string } = {}
  server.use(
    http.post('*/api/libraries/graph', async ({ request }) => {
      body = (await request.json()) as { author?: string }
      return HttpResponse.json(graph)
    }),
  )
  renderMap()

  const skill = await screen.findByRole('button', { name: 'Abrir a skill revisao na bancada' })
  expect(body.author).toBe('rascunho')
  expect(screen.getByText('3 nós · 2 ligações')).toBeInTheDocument()

  await userEvent.click(skill)
  expect(screen.getByText('bancada aberta')).toBeInTheDocument()
  const { draft, selectedId } = useLibraryStore.getState()
  expect(selectedId).toBe(draft.skills[0]?.id)
})

test('erro da API aparece com opção de tentar de novo', async () => {
  useLibraryStore.getState().setMeta({ name: 'minhas-skills', author: 'Ana', license: 'MIT' })
  useLibraryStore.getState().addSkill({
    content: '---\nname: revisao\ndescription: Revisa. Use quando pedirem.\n---\n\n# Revisao\n',
  })
  server.use(http.post('*/api/libraries/graph', () => HttpResponse.json({ title: 'x' }, { status: 500 })))
  renderMap()
  expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível montar o grafo')
  expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeInTheDocument()
})
