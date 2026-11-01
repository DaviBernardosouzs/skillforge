import type { Encoding } from '../features/library/types'

/** Lê um arquivo enviado: texto UTF-8 válido fica como texto; o resto (imagens etc.) vira base64. */
export async function readUpload(file: File): Promise<{ content: string; encoding: Encoding }> {
  const bytes = new Uint8Array(await file.arrayBuffer())
  try {
    const text = new TextDecoder('utf-8', { fatal: true }).decode(bytes)
    if (!text.includes('\u0000')) return { content: text, encoding: 'utf8' }
  } catch {
    // não é UTF-8: binário
  }
  return { content: toBase64(bytes), encoding: 'base64' }
}

export function toBase64(bytes: Uint8Array): string {
  let binary = ''
  const chunk = 0x8000
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunk))
  }
  return btoa(binary)
}

export function base64Size(content: string): number {
  const clean = content.replace(/\s/g, '')
  const padding = clean.endsWith('==') ? 2 : clean.endsWith('=') ? 1 : 0
  return Math.max(0, Math.floor((clean.length * 3) / 4) - padding)
}
