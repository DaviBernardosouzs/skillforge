import type { LayoutLink, LayoutNode } from './layout'

/** Tipos de nó na escala de temperatura; referências em aço frio. */
export const NODE_STYLE: Record<LayoutNode['node']['type'], { fill: string; stroke: string; label: string }> =
  {
    INDEX: { fill: 'var(--color-heat-6)', stroke: 'var(--color-heat-5)', label: 'Índice' },
    CATEGORY: { fill: 'var(--color-heat-5)', stroke: 'var(--color-heat-4)', label: 'Categoria' },
    SKILL: { fill: 'var(--color-heat-3)', stroke: 'var(--color-heat-4)', label: 'Skill' },
    REFERENCE: { fill: 'var(--color-steel-400)', stroke: 'var(--color-steel-300)', label: 'Referência' },
    SCRIPT: { fill: 'var(--color-heat-1)', stroke: 'var(--color-heat-3)', label: 'Script' },
  }

export const EDGE_STYLE: Record<
  LayoutLink['edge']['type'],
  { stroke: string; dash?: string; width: number; label: string }
> = {
  CONTAINS: { stroke: 'var(--color-iron-600)', width: 1.2, label: 'contém' },
  HAS_FILE: { stroke: 'var(--color-iron-600)', dash: '2 3', width: 1, label: 'arquivo' },
  RELATED: { stroke: 'var(--color-heat-4)', dash: '6 4', width: 1.8, label: 'related' },
  MENTIONS: { stroke: 'var(--color-steel-400)', dash: '1 4', width: 1.4, label: 'mentions' },
}
