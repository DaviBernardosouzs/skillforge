import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { useLibraryStore } from '../features/library/store'
import ForgePage from './ForgePage'

function renderPage() {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <ForgePage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => useLibraryStore.getState().reset())

test('mostra as ações principais', () => {
  renderPage()
  expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/Forje sua biblioteca de skills/)
  expect(screen.getByRole('button', { name: 'Acender a forja' })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Abrir rascunho' })).toBeInTheDocument()
  expect(screen.queryByText('Peça na bigorna')).not.toBeInTheDocument()
})

test('com rascunho salvo mostra a peça na bigorna', () => {
  useLibraryStore.getState().setMeta({ name: 'biblioteca-do-time' })
  useLibraryStore.getState().addSkill({ content: '---\nname: a\n---' })
  useLibraryStore.getState().addSkill({ content: '---\nname: b\n---' })
  renderPage()
  expect(screen.getByText('Peça na bigorna')).toBeInTheDocument()
  expect(screen.getByText('biblioteca-do-time')).toBeInTheDocument()
  expect(screen.getByText('2 skills em trabalho')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Continuar' })).toBeInTheDocument()
})
