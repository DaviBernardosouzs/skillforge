import { render, screen } from '@testing-library/react'
import App from './App'

test('renderiza a tela inicial temática', () => {
  render(<App />)
  expect(screen.getByText(/SkillForge/)).toBeInTheDocument()
})
