import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { Violation } from '../../api/types'
import { TemperPanel } from './TemperPanel'

const crack: Violation = {
  ruleId: 'references-exist',
  severity: 'ERROR',
  skill: 'revisao',
  message: "O corpo linka 'references/nao.md', mas esse arquivo não foi enviado.",
}
const hot: Violation = {
  ruleId: 'description-trigger',
  severity: 'WARNING',
  skill: 'revisao',
  message: 'Diga quando usar.',
}

test('mostra trincas e metal quente com regra e mensagem', () => {
  render(
    <TemperPanel
      content="---\nname: revisao\n---"
      violations={[crack, hot]}
      inspecting={false}
      error={null}
      inspectionKey={1}
      onLocate={() => {}}
    />,
  )
  expect(screen.getByRole('status')).toHaveTextContent('1 trinca e 1 com metal quente.')
  expect(screen.getByText('Link para arquivo que não existe')).toBeInTheDocument()
  expect(screen.getByText(/references-exist/)).toBeInTheDocument()
  expect(screen.getByText('Diga quando usar.')).toBeInTheDocument()
})

test('clicar numa violação leva ao trecho', async () => {
  const onLocate = vi.fn()
  render(
    <TemperPanel
      content="x"
      violations={[crack]}
      inspecting={false}
      error={null}
      inspectionKey={1}
      onLocate={onLocate}
    />,
  )
  await userEvent.click(screen.getByRole('button', { name: /Link para arquivo que não existe/ }))
  expect(onLocate).toHaveBeenCalledWith(crack)
})

test('sem violações a têmpera é aprovada', () => {
  render(
    <TemperPanel
      content="x"
      violations={[]}
      inspecting={false}
      error={null}
      inspectionKey={1}
      onLocate={() => {}}
    />,
  )
  expect(screen.getByRole('status')).toHaveTextContent('Têmpera aprovada')
})
