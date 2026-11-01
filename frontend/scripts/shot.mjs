// Captura rápida de uma rota para conferência visual durante o desenvolvimento.
// Uso: node scripts/shot.mjs <url> <saida.png> [largura] [altura] [fullPage]
import { chromium } from '@playwright/test'

const [url, out, width = '1440', height = '900', full = 'false'] = process.argv.slice(2)
const browser = await chromium.launch()
const page = await browser.newPage({ viewport: { width: Number(width), height: Number(height) }, colorScheme: 'dark' })
await page.goto(url, { waitUntil: 'networkidle' })
await page.waitForTimeout(600)
await page.screenshot({ path: out, fullPage: full === 'true' })
await browser.close()
