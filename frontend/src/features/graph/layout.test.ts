import type { LibraryGraph } from '../../api/types'
import { computeLayout, neighborsOf } from './layout'

const node = (id: string, type: 'INDEX' | 'CATEGORY' | 'SKILL' | 'REFERENCE' | 'SCRIPT') => ({
  id,
  type,
  label: id,
  path: null,
  description: null,
  category: null,
  tags: [],
})

const graph = {
  nodes: [
    node('index', 'INDEX'),
    node('category:dev', 'CATEGORY'),
    node('skill:a', 'SKILL'),
    node('skill:b', 'SKILL'),
  ],
  edges: [
    { from: 'index', to: 'category:dev', type: 'CONTAINS' },
    { from: 'category:dev', to: 'skill:a', type: 'CONTAINS' },
    { from: 'category:dev', to: 'skill:b', type: 'CONTAINS' },
    { from: 'skill:a', to: 'skill:b', type: 'RELATED' },
    { from: 'skill:a', to: 'fantasma', type: 'MENTIONS' },
  ],
} as unknown as LibraryGraph

test('calcula posições finitas e ignora arestas para nós inexistentes', () => {
  const { nodes, links } = computeLayout(graph, 800, 600)
  expect(links).toHaveLength(4)
  for (const n of nodes) {
    expect(Number.isFinite(n.x)).toBe(true)
    expect(Number.isFinite(n.y)).toBe(true)
  }
})

test('mesma entrada, mesmo layout', () => {
  const a = computeLayout(graph, 800, 600).nodes.map((n) => [n.x, n.y])
  const b = computeLayout(graph, 800, 600).nodes.map((n) => [n.x, n.y])
  expect(a).toEqual(b)
})

test('vizinhos incluem o próprio nó e as ligações nos dois sentidos', () => {
  const { links } = computeLayout(graph, 800, 600)
  expect([...neighborsOf(links, 'skill:a')].sort()).toEqual(['category:dev', 'skill:a', 'skill:b'])
})
