import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { LeagueMap } from '../features/graph/LeagueMap'
import { EDGE_STYLE, NODE_STYLE } from '../features/graph/styles'
import { toLibraryRequest } from '../features/library/request'
import { useLibraryStore } from '../features/library/store'
import { readFrontmatterField } from '../lib/frontmatter'
import { libraryBlocker } from '../features/lint/useLint'
import { Button } from '../ui/Button'
import { Panel } from '../ui/Panel'
import { SectionLabel } from '../ui/SectionLabel'

export default function MapPage() {
  const navigate = useNavigate()
  const draft = useLibraryStore((s) => s.draft)
  const select = useLibraryStore((s) => s.select)
  // O grafo não depende do autor: um valor provisório permite ver o mapa antes de preencher
  const previewDraft = { ...draft, author: draft.author.trim() || 'rascunho' }
  const blocker = libraryBlocker(previewDraft)
  const body = blocker ? null : JSON.stringify(toLibraryRequest(previewDraft))

  const graph = useQuery({
    queryKey: ['graph', body],
    queryFn: () => api.graph(JSON.parse(body as string)),
    enabled: body !== null,
    staleTime: Infinity,
  })

  function openSkill(name: string) {
    const skill = draft.skills.find((s) => readFrontmatterField(s.content, 'name') === name)
    if (skill) select(skill.id)
    navigate('/bancada')
  }

  return (
    <div className="mx-auto grid w-full max-w-[1400px] gap-8 px-4 py-10 lg:grid-cols-[minmax(0,1fr)_280px]">
      <div className="flex flex-col gap-6">
        <header className="flex flex-col gap-3">
          <SectionLabel>Mapa da liga · grafo da biblioteca</SectionLabel>
          <h1 className="font-display text-5xl leading-[0.9] font-black uppercase md:text-6xl">
            {draft.name || 'Mapa da liga'}
          </h1>
          <p className="max-w-[60ch] text-iron-300">
            É assim que sua biblioteca vai aparecer no Graph View do Obsidian. Passe o mouse para ver as
            ligações de um nó; clique numa skill para abri-la na bancada.
          </p>
        </header>

        <Panel label="Liga · nós e ligações" className="min-h-[420px]">
          {blocker && (
            <div className="flex flex-col items-start gap-3 p-6">
              <p className="text-iron-300">{blocker}</p>
              <Button variant="steel" onClick={() => navigate('/bancada')}>
                Ir para a bancada
              </Button>
            </div>
          )}
          {!blocker && graph.isLoading && (
            <p role="status" className="label-mono p-6">
              Fundindo a liga…
            </p>
          )}
          {graph.isError && (
            <div role="alert" className="flex flex-col items-start gap-3 p-6">
              <p className="text-slag-400">Não foi possível montar o grafo: {graph.error.message}</p>
              <Button size="sm" onClick={() => void graph.refetch()}>
                Tentar de novo
              </Button>
            </div>
          )}
          {graph.data && <LeagueMap graph={graph.data} onOpenSkill={openSkill} />}
        </Panel>
        {graph.data && (
          <p className="text-xs text-iron-400">
            Skills com erro de nome ou sem description ficam fora do mapa até serem corrigidas na bancada.
          </p>
        )}
      </div>

      <aside aria-labelledby="legenda" className="flex flex-col gap-6 lg:pt-40">
        <h2 id="legenda" className="label-mono">
          Legenda
        </h2>
        <ul className="flex flex-col gap-3">
          {Object.entries(NODE_STYLE).map(([type, style]) => (
            <li key={type} className="flex items-center gap-3 text-sm">
              <svg width="18" height="18" aria-hidden="true">
                <circle cx="9" cy="9" r="7" fill={style.fill} stroke={style.stroke} strokeWidth="1.5" />
              </svg>
              {style.label}
            </li>
          ))}
        </ul>
        <ul className="flex flex-col gap-3">
          {Object.entries(EDGE_STYLE).map(([type, style]) => (
            <li key={type} className="flex items-center gap-3 text-sm">
              <svg width="32" height="10" aria-hidden="true">
                <line
                  x1="0"
                  y1="5"
                  x2="32"
                  y2="5"
                  stroke={style.stroke}
                  strokeWidth={style.width + 0.4}
                  strokeDasharray={style.dash}
                />
              </svg>
              <span>
                {style.label === 'related'
                  ? 'relacionada'
                  : style.label === 'mentions'
                    ? 'menciona'
                    : style.label}{' '}
                <span className="font-mono text-xs text-iron-400">{type}</span>
              </span>
            </li>
          ))}
        </ul>
        {graph.data && (
          <p className="font-mono text-xs text-iron-400">
            {graph.data.nodes.length} nós · {graph.data.edges.length} ligações
          </p>
        )}
      </aside>
    </div>
  )
}
