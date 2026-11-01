/**
 * Prévia estática do grafo de uma biblioteca de exemplo (a de examples/library-request.json):
 * índice, 2 categorias, 3 skills e seus arquivos. Posições fixas, sem simulação.
 */
const nodes = [
  { id: 'index', x: 200, y: 150, r: 9, color: 'var(--color-heat-6)', label: 'minhas-skills' },
  { id: 'dev', x: 110, y: 90, r: 7, color: 'var(--color-heat-4)', label: 'desenvolvimento' },
  { id: 'docs', x: 300, y: 205, r: 7, color: 'var(--color-heat-4)', label: 'documentacao' },
  { id: 'rev', x: 60, y: 160, r: 6, color: 'var(--color-heat-3)', label: 'revisao-de-codigo' },
  { id: 'test', x: 170, y: 40, r: 6, color: 'var(--color-heat-3)', label: 'gerar-testes' },
  { id: 'adr', x: 330, y: 120, r: 6, color: 'var(--color-heat-3)', label: 'registro-de-decisao' },
  { id: 'r1', x: 30, y: 215, r: 4, color: 'var(--color-steel-400)', label: '' },
  { id: 'r2', x: 240, y: 30, r: 4, color: 'var(--color-steel-400)', label: '' },
  { id: 's1', x: 120, y: 12, r: 4, color: 'var(--color-iron-400)', label: '' },
]

const edges: [string, string, 'contains' | 'related' | 'file'][] = [
  ['index', 'dev', 'contains'],
  ['index', 'docs', 'contains'],
  ['dev', 'rev', 'contains'],
  ['dev', 'test', 'contains'],
  ['docs', 'adr', 'contains'],
  ['rev', 'r1', 'file'],
  ['test', 'r2', 'file'],
  ['test', 's1', 'file'],
  ['rev', 'test', 'related'],
  ['adr', 'rev', 'related'],
]

const byId = new Map(nodes.map((n) => [n.id, n]))

export function GraphPreview({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 380 240"
      role="img"
      aria-label="Prévia do grafo: índice ligado a 2 categorias, 3 skills, suas referências e as skills relacionadas"
      className={className}
    >
      {edges.map(([from, to, kind]) => {
        const a = byId.get(from)
        const b = byId.get(to)
        if (!a || !b) return null
        return (
          <line
            key={`${from}-${to}`}
            x1={a.x}
            y1={a.y}
            x2={b.x}
            y2={b.y}
            stroke={kind === 'related' ? 'var(--color-heat-4)' : 'var(--color-iron-600)'}
            strokeWidth={kind === 'related' ? 1.4 : 1}
            strokeDasharray={kind === 'related' ? '4 3' : undefined}
          />
        )
      })}
      {nodes.map((n) => (
        <g key={n.id}>
          <circle cx={n.x} cy={n.y} r={n.r} fill={n.color} />
          {n.label && (
            <text
              x={n.x > 250 ? n.x - n.r - 5 : n.x + n.r + 5}
              textAnchor={n.x > 250 ? 'end' : 'start'}
              y={n.y + 3}
              fill="var(--color-iron-300)"
              fontFamily="var(--font-mono)"
              fontSize="8.5"
            >
              {n.label}
            </text>
          )}
        </g>
      ))}
    </svg>
  )
}
