import { expect, test, type Page } from '@playwright/test'
import { readFile } from 'node:fs/promises'

// Ponta a ponta com o backend real: ./mvnw spring-boot:run antes (ou SKILLFORGE_API_URL para outra porta).

async function fillLibrary(page: Page) {
  await page.getByRole('textbox', { name: 'Nome da biblioteca' }).fill('skills-e2e')
  await page.getByRole('textbox', { name: 'Autor' }).fill('Ana Teste')
}

async function pasteSkill(page: Page, content: string) {
  await page.getByRole('button', { name: 'Colar SKILL.md' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByRole('textbox', { name: 'Conteúdo do SKILL.md' }).fill(content)
  await dialog.getByRole('button', { name: 'Levar ao estoque' }).click()
  await expect(dialog).toBeHidden()
}

const VALID_SKILL = `---
name: resumo-de-reuniao
description: Resume atas de reunião em decisões e próximos passos. Use quando o usuário colar uma transcrição ou pedir um resumo de reunião.
---

# Resumo de reunião

## Processo
1. Leia a transcrição inteira.
2. Liste decisões, responsáveis e prazos.
`

test('cria uma skill pelo Molde e leva à bancada sem trincas', async ({ page }) => {
  await page.goto('/molde')
  await page.getByRole('textbox', { name: /Nome/ }).fill('revisao-de-codigo')
  await page.getByRole('textbox', { name: /Propósito/ }).fill('Revisa pull requests apontando bugs e riscos')
  await page.getByRole('button', { name: 'Próxima etapa' }).click()
  await page.getByRole('textbox', { name: 'Quando usar, item 1' }).fill('o usuário pedir revisão de um PR')
  await page.getByRole('button', { name: 'Próxima etapa' }).click()
  await page.getByRole('textbox', { name: 'Título do passo 1' }).fill('Leia o diff')
  await page
    .getByRole('textbox', { name: 'Detalhe do passo 1' })
    .fill('Leia o diff inteiro antes de comentar.')

  const take = page.getByRole('button', { name: 'Levar à bancada' })
  await expect(take).toBeEnabled({ timeout: 10_000 })
  await take.click()

  await expect(page).toHaveURL(/\/bancada$/)
  await expect(page.getByRole('list', { name: 'Skills da biblioteca' })).toContainText('revisao-de-codigo')
  // A têmpera da skill aberta não acusa trinca (aviso de metal quente pode existir)
  await expect(page.getByText(/Têmpera aprovada|^0 trincas e/)).toBeVisible({ timeout: 10_000 })
})

test('biblioteca com trinca não forja e mostra a violação', async ({ page }) => {
  await page.goto('/bancada')
  await fillLibrary(page)
  await pasteSkill(page, `${VALID_SKILL}\nVeja o [guia](references/nao-existe.md).\n`)

  const forge = page.getByRole('button', { name: 'Forjar', exact: true })
  await expect(page.getByText('Link para arquivo que não existe').first()).toBeVisible({ timeout: 10_000 })
  await expect(forge).toBeDisabled()
  await expect(forge).toHaveAccessibleDescription(/trinca/)
})

test('forjar uma biblioteca válida baixa um zip', async ({ page }) => {
  await page.goto('/bancada')
  await fillLibrary(page)
  await pasteSkill(page, VALID_SKILL)

  const forge = page.getByRole('button', { name: 'Forjar', exact: true })
  await expect(forge).toBeEnabled({ timeout: 10_000 })
  await forge.click()

  const dialog = page.getByRole('dialog')
  const confirm = dialog.getByRole('checkbox')
  if (await confirm.isVisible()) await confirm.check()

  const download = page.waitForEvent('download')
  await dialog.getByRole('button', { name: 'Forjar', exact: true }).click()
  const file = await download
  expect(file.suggestedFilename()).toBe('skills-e2e.zip')

  const bytes = await readFile((await file.path()) as string)
  expect(bytes.subarray(0, 2).toString()).toBe('PK')
  await expect(dialog.getByRole('heading', { name: 'Peça forjada' })).toBeVisible()
  await expect(dialog).toContainText('git push -u origin main')
})
