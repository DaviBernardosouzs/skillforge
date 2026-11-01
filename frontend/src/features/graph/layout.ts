import {
  forceCenter,
  forceCollide,
  forceLink,
  forceManyBody,
  forceSimulation,
  type Simulation,
  type SimulationLinkDatum,
  type SimulationNodeDatum,
} from 'd3-force'
import type { GraphEdge, GraphNode, LibraryGraph } from '../../api/types'

export interface LayoutNode extends SimulationNodeDatum {
  id: string
  node: GraphNode
  radius: number
}

export interface LayoutLink extends SimulationLinkDatum<LayoutNode> {
  edge: GraphEdge
}

const RADIUS: Record<GraphNode['type'], number> = {
  INDEX: 14,
  CATEGORY: 11,
  SKILL: 9,
  REFERENCE: 5,
  SCRIPT: 5,
}
const DISTANCE: Record<GraphEdge['type'], number> = {
  CONTAINS: 90,
  HAS_FILE: 45,
  RELATED: 130,
  MENTIONS: 150,
}

/** Monta a simulação de forças. Posições iniciais em círculo: mesma entrada, mesmo desenho. */
export function createSimulation(graph: LibraryGraph, width: number, height: number) {
  const nodes: LayoutNode[] = graph.nodes.map((node, i) => {
    const angle = (i / Math.max(graph.nodes.length, 1)) * Math.PI * 2
    return {
      id: node.id,
      node: node as GraphNode,
      radius: RADIUS[node.type],
      x: width / 2 + Math.cos(angle) * 120,
      y: height / 2 + Math.sin(angle) * 120,
    }
  })
  const ids = new Set(nodes.map((n) => n.id))
  const links: LayoutLink[] = graph.edges
    .filter((edge) => ids.has(edge.from) && ids.has(edge.to))
    .map((edge) => ({ source: edge.from, target: edge.to, edge }))

  const simulation: Simulation<LayoutNode, LayoutLink> = forceSimulation(nodes)
    .force(
      'link',
      forceLink<LayoutNode, LayoutLink>(links)
        .id((d) => d.id)
        .distance((l) => DISTANCE[l.edge.type])
        .strength((l) => (l.edge.type === 'CONTAINS' || l.edge.type === 'HAS_FILE' ? 0.9 : 0.15)),
    )
    .force(
      'charge',
      forceManyBody<LayoutNode>().strength((d) => -60 - d.radius * 18),
    )
    .force(
      'collide',
      forceCollide<LayoutNode>((d) => d.radius + 18),
    )
    .force('center', forceCenter(width / 2, height / 2))
    .stop()

  return { simulation, nodes, links }
}

/** Layout estático pré-calculado (movimento reduzido e testes). */
export function computeLayout(graph: LibraryGraph, width: number, height: number, ticks = 300) {
  const result = createSimulation(graph, width, height)
  result.simulation.tick(ticks)
  return result
}

/** Vizinhos diretos de um nó (para o destaque ao passar o mouse). */
export function neighborsOf(links: LayoutLink[], id: string): Set<string> {
  const result = new Set([id])
  for (const link of links) {
    const source = typeof link.source === 'object' ? link.source.id : String(link.source)
    const target = typeof link.target === 'object' ? link.target.id : String(link.target)
    if (source === id) result.add(target)
    if (target === id) result.add(source)
  }
  return result
}
