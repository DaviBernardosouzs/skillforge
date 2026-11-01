/**
 * Leitura leve do frontmatter para exibir nome e description na interface.
 * A validação de verdade é sempre do backend (POST /api/skills/lint).
 */
export function readFrontmatterField(content: string, field: 'name' | 'description'): string {
  const text = content.replace(/\r\n/g, '\n')
  if (!text.startsWith('---\n')) return ''
  const end = text.indexOf('\n---', 4)
  if (end < 0) return ''
  const yaml = text.slice(4, end)
  const match = yaml.match(new RegExp(`^${field}:[ \\t]*(.*)$`, 'm'))
  if (!match?.[1]) return ''
  return match[1].trim().replace(/^(['"])(.*)\1$/, '$2')
}

/** O arquivo começa com um bloco --- fechado (o parser do backend consegue ler)? */
export function hasFrontmatter(content: string): boolean {
  const text = content.replace(/\r\n/g, '\n')
  return text.startsWith('---\n') && /\n---[ \t]*(\n|$)/.test(text.slice(3))
}

/** Linha (1-based) em que o texto aparece pela primeira vez, para levar o cursor até ele. */
export function findLine(content: string, needle: string): number | null {
  if (!needle) return null
  const index = content.indexOf(needle)
  if (index < 0) return null
  return content.slice(0, index).split('\n').length
}
