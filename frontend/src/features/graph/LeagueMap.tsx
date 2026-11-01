import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import type { LibraryGraph } from '../../api/types'
import { useReducedMotion } from '../../hooks/useReducedMotion'
import { computeLayout, createSimulation, neighborsOf, type LayoutLink, type LayoutNode } from './layout'
import { EDGE_STYLE, NODE_STYLE } from './styles'

const WIDTH = 960
const HEIGHT = 620

const endpoint = (value: LayoutLink['source']) => value as LayoutNode

interface LeagueMapProps {
  graph: LibraryGraph
  onOpenSkill: (name: string) => void
}

/** Grafo de forças da biblioteca, com destaque de vizinhos e skills clicáveis. */
export function LeagueMap({ graph, onOpenSkill }: LeagueMapProps) {
  const reduced = useReducedMotion()
  const [, setFrame] = useState(0)
  const [focus, setFocus] = useState<string | null>(null)
  const frameRef = useRef(0)

  const layout = useMemo(
    () => (reduced ? computeLayout(graph, WIDTH, HEIGHT) : createSimulation(graph, WIDTH, HEIGHT)),
    [graph, reduced],
  )

  useEffect(() => {
    if (reduced) return
    const { simulation } = layout
    simulation.alpha(1).restart()
    simulation.on('tick', () => {
      cancelAnimationFrame(frameRef.current)
      frameRef.current = requestAnimationFrame(() => setFrame((f) => f + 1))
    })
    return () => {
      simulation.stop()
      simulation.on('tick', null)
      cancelAnimationFrame(frameRef.current)
    }
  }, [layout, reduced])

  const highlighted = focus ? neighborsOf(layout.links, focus) : null

  function onKey(event: KeyboardEvent, node: LayoutNode) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      onOpenSkill(node.node.label)
    }
  }

  return (
    <svg
      viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
      className="h-auto w-full touch-pan-y"
      role="group"
      aria-label={`Grafo com ${graph.nodes.length} nós e ${graph.edges.length} ligações`}
    >
      <defs>
        <marker
          id="arrow-related"
          viewBox="0 0 8 8"
          refX="7"
          refY="4"
          markerWidth="7"
          markerHeight="7"
          orient="auto"
        >
          <path d="M0 0 8 4 0 8z" fill="var(--color-heat-4)" />
        </marker>
      </defs>
      <g>
        {layout.links.map((link) => {
          const a = endpoint(link.source)
          const b = endpoint(link.target)
          const style = EDGE_STYLE[link.edge.type]
          const dim = highlighted && !(highlighted.has(a.id) && highlighted.has(b.id))
          return (
            <line
              key={`${link.edge.from}-${link.edge.to}-${link.edge.type}`}
              x1={a.x}
              y1={a.y}
              x2={b.x}
              y2={b.y}
              stroke={style.stroke}
              strokeWidth={style.width}
              strokeDasharray={style.dash}
              markerEnd={link.edge.type === 'RELATED' ? 'url(#arrow-related)' : undefined}
              opacity={dim ? 0.12 : 0.9}
              className="transition-opacity duration-200"
            />
          )
        })}
      </g>
      <g>
        {layout.nodes.map((n) => {
          const style = NODE_STYLE[n.node.type]
          const dim = highlighted && !highlighted.has(n.id)
          const isSkill = n.node.type === 'SKILL'
          const showLabel = n.node.type !== 'REFERENCE' && n.node.type !== 'SCRIPT'
          return (
            <g
              key={n.id}
              transform={`translate(${n.x ?? 0} ${n.y ?? 0})`}
              opacity={dim ? 0.2 : 1}
              className="transition-opacity duration-200"
              onMouseEnter={() => setFocus(n.id)}
              onMouseLeave={() => setFocus(null)}
              onFocus={() => setFocus(n.id)}
              onBlur={() => setFocus(null)}
              {...(isSkill
                ? {
                    role: 'button',
                    tabIndex: 0,
                    'aria-label': `Abrir a skill ${n.node.label} na bancada`,
                    onClick: () => onOpenSkill(n.node.label),
                    onKeyDown: (e: KeyboardEvent) => onKey(e, n),
                    style: { cursor: 'pointer' },
                  }
                : { 'aria-hidden': true })}
            >
              <circle r={n.radius} fill={style.fill} stroke={style.stroke} strokeWidth={1.5} />
              {(showLabel || focus === n.id) && (
                <text
                  x={n.radius + 6}
                  y={4}
                  fill={focus === n.id ? 'var(--color-iron-100)' : 'var(--color-iron-300)'}
                  fontFamily="var(--font-mono)"
                  fontSize={n.node.type === 'INDEX' ? 14 : 12}
                  paintOrder="stroke"
                  stroke="var(--color-coal-950)"
                  strokeWidth={4}
                >
                  {n.node.label}
                </text>
              )}
            </g>
          )
        })}
      </g>
    </svg>
  )
}
