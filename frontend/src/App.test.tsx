import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AppRoutes } from './App'

function renderAt(path: string) {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={[path]}>
        <AppRoutes />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

test('a navegação mostra o vocabulário da forja', async () => {
  renderAt('/')
  expect(await screen.findByRole('navigation', { name: 'Principal' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: 'Bancada' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: 'Mapa da liga' })).toBeInTheDocument()
})

test('a página de design lista os componentes', async () => {
  renderAt('/design')
  expect(
    await screen.findByRole('heading', { name: 'Escala de temperatura' }, { timeout: 5000 }),
  ).toBeInTheDocument()
  expect(screen.getByText('Trinca')).toBeInTheDocument()
  expect(screen.getByRole('meter', { name: 'Temperatura da skill' })).toBeInTheDocument()
})
