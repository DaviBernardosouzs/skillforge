import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import * as client from '../../api/client'
import type { Violation } from '../../api/types'
import { server } from '../../test/msw'
import type { LibraryDraft } from '../library/types'
import { ForgeCeremony } from './ForgeCeremony'

const draft: LibraryDraft = {
  version: 1,
  name: 'minhas-skills',
  author: 'Ana',
  license: 'MIT',
  skills: [
    {
      id: 'a',
      content: '---\nname: revisao\ndescription: Revisa. Use quando pedirem.\n---\n\n# Revisao\n',
      files: [{ id: 'f', path: 'references/guia.md', content: '# Guia', encoding: 'utf8' }],
      category: 'dev',
      tags: [],
      related: [],
    },
  ],
}

const saveBlob = vi.spyOn(client, 'saveBlob').mockImplementation(() => {})

function renderCeremony(props: Partial<Parameters<typeof ForgeCeremony>[0]> = {}) {
  const onCracked = vi.fn()
  const onClose = vi.fn()
  render(
    <ForgeCeremony
      open
      draft={draft}
      warnings={0}
      onClose={onClose}
      onCracked={onCracked}
      minDuration={0}
      {...props}
    />,
  )
  return { onCracked, onClose }
}

beforeEach(() => saveBlob.mockClear())

test('resume o que será gerado a partir do rascunho', () => {
  renderCeremony()
  const summary = screen.getByText('Skills').closest('dl')
  expect(summary).toHaveTextContent('Skills1')
  expect(summary).toHaveTextContent('Arquivos extras1')
  expect(summary).toHaveTextContent('Categoriasdev')
  expect(summary).toHaveTextContent('LicençaMIT')
})

test('metal quente exige confirmar os avisos antes de forjar', async () => {
  renderCeremony({ warnings: 2 })
  const forge = screen.getByRole('button', { name: 'Forjar' })
  expect(forge).toBeDisabled()
  await userEvent.click(screen.getByRole('checkbox'))
  expect(forge).toBeEnabled()
})

test('forjar baixa o zip e mostra os próximos passos', async () => {
  server.use(
    http.post('*/api/libraries/generate', () =>
      HttpResponse.arrayBuffer(new Uint8Array([0x50, 0x4b, 3, 4]).buffer, {
        headers: {
          'Content-Type': 'application/zip',
          'Content-Disposition': 'attachment; filename="minhas-skills.zip"',
          'X-Lint-Warnings': '0',
        },
      }),
    ),
  )
  renderCeremony()
  await userEvent.click(screen.getByRole('button', { name: 'Forjar' }))

  expect(await screen.findByRole('heading', { name: 'Peça forjada' })).toBeInTheDocument()
  expect(saveBlob).toHaveBeenCalledWith(expect.any(Blob), 'minhas-skills.zip')
  expect(
    screen.getByText(/git remote add origin git@github.com:<usuario>\/minhas-skills.git/),
  ).toBeInTheDocument()
  expect(screen.getByRole('status')).toHaveTextContent('Download de minhas-skills.zip iniciado')
})

test('422 devolve as violações para a bancada', async () => {
  const violations: Violation[] = [
    { ruleId: 'body-not-empty', severity: 'ERROR', skill: 'revisao', message: 'Corpo vazio.' },
  ]
  server.use(
    http.post('*/api/libraries/generate', () =>
      HttpResponse.json({ title: 'Biblioteca inválida', status: 422, violations }, { status: 422 }),
    ),
  )
  const { onCracked } = renderCeremony()
  await userEvent.click(screen.getByRole('button', { name: 'Forjar' }))
  await vi.waitFor(() => expect(onCracked).toHaveBeenCalledWith(violations))
  expect(saveBlob).not.toHaveBeenCalled()
})

test('413 diz qual limite estourou e não oferece tentar de novo', async () => {
  server.use(
    http.post('*/api/libraries/generate', () =>
      HttpResponse.json(
        { title: 'Limite excedido', status: 413, detail: 'A biblioteca tem 60 skills; o máximo é 50.' },
        { status: 413 },
      ),
    ),
  )
  renderCeremony()
  await userEvent.click(screen.getByRole('button', { name: 'Forjar' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('A biblioteca tem 60 skills; o máximo é 50.')
  expect(screen.getByRole('heading', { name: 'Peça grande demais para a forja' })).toBeInTheDocument()
  expect(screen.queryByRole('button', { name: 'Tentar de novo' })).not.toBeInTheDocument()
})

test('falha de rede mostra mensagem honesta e permite tentar de novo', async () => {
  let calls = 0
  server.use(
    http.post('*/api/libraries/generate', () => {
      calls++
      return HttpResponse.error()
    }),
  )
  renderCeremony()
  await userEvent.click(screen.getByRole('button', { name: 'Forjar' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível falar com a forja')
  await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))
  await vi.waitFor(() => expect(calls).toBe(2))
})
