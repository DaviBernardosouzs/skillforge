import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { Tabs } from './Tabs'

function Harness() {
  const [value, setValue] = useState<'a' | 'b'>('a')
  return (
    <Tabs
      label="Arquivos"
      value={value}
      onChange={setValue}
      items={[
        { id: 'a', label: 'SKILL.md' },
        { id: 'b', label: 'checklist.md' },
      ]}
    />
  )
}

test('setas do teclado trocam de aba', async () => {
  render(<Harness />)
  const first = screen.getByRole('tab', { name: 'SKILL.md' })
  first.focus()
  await userEvent.keyboard('{ArrowRight}')
  expect(screen.getByRole('tab', { name: 'checklist.md' })).toHaveAttribute('aria-selected', 'true')
  expect(screen.getByRole('tab', { name: 'checklist.md' })).toHaveFocus()
})
