import { ArrowRight, FolderOpen } from '@phosphor-icons/react'
import { useRef, useState, type ChangeEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { DraftFileError, parseDraftFile } from '../features/library/draftFile'
import { hasWork, useLibraryStore } from '../features/library/store'
import { ForgeHearth } from '../features/forge/ForgeHearth'
import { GraphPreview } from '../features/forge/GraphPreview'
import { Button } from '../ui/Button'
import { EmberField } from '../ui/EmberField'
import { SectionLabel } from '../ui/SectionLabel'

const steps = [
  {
    number: '01',
    title: 'Reunir',
    place: 'Estoque',
    text: 'Escreva, cole um SKILL.md ou parta de um molde pronto. Cada skill vira um lingote no estoque.',
  },
  {
    number: '02',
    title: 'Moldar',
    place: 'Molde',
    text: 'Responda perguntas guiadas e receba um SKILL.md com description, processo, exemplos e verificação.',
  },
  {
    number: '03',
    title: 'Forjar',
    place: 'Têmpera e forja',
    text: 'A têmpera valida tudo ao vivo. Sem trincas, a forja entrega um .zip com git, CI e o vault do Obsidian.',
  },
]

export default function ForgePage() {
  const navigate = useNavigate()
  const draft = useLibraryStore((s) => s.draft)
  const replaceDraft = useLibraryStore((s) => s.replaceDraft)
  const fileInput = useRef<HTMLInputElement>(null)
  const [importError, setImportError] = useState<string | null>(null)

  async function onImport(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    try {
      replaceDraft(parseDraftFile(await file.text()))
      navigate('/bancada')
    } catch (error) {
      setImportError(error instanceof DraftFileError ? error.message : 'Não foi possível ler esse arquivo.')
    }
  }

  return (
    <div className="relative isolate overflow-hidden">
      <EmberField className="pointer-events-none absolute inset-0 -z-10 h-full w-full" />

      <section className="mx-auto grid w-full max-w-[1400px] gap-10 px-4 pt-14 pb-20 md:grid-cols-[1.15fr_0.85fr] md:items-center md:pt-20">
        <div className="flex flex-col gap-7">
          <SectionLabel>Gerador de bibliotecas de Agent Skills</SectionLabel>
          <h1 className="font-display text-[clamp(3.2rem,8vw,7.5rem)] leading-[0.86] font-black tracking-[-0.01em] uppercase">
            Forje sua biblioteca de skills
            <span className="block text-heat-4">e leve para o GitHub</span>
          </h1>
          <p className="max-w-[56ch] text-lg leading-relaxed text-iron-300">
            Reúna suas skills, molde cada <code className="font-mono text-base text-iron-100">SKILL.md</code>,
            inspecione a têmpera e receba um repositório Git pronto para{' '}
            <code className="font-mono text-base">git push</code>, com validação no CI e o grafo da biblioteca
            no Obsidian.
          </p>
          <div className="flex flex-wrap items-center gap-3">
            <Button
              variant="forge"
              size="lg"
              icon={<ArrowRight size={18} weight="bold" />}
              onClick={() => navigate('/bancada')}
            >
              Acender a forja
            </Button>
            <Button
              variant="steel"
              size="lg"
              icon={<FolderOpen size={18} />}
              onClick={() => fileInput.current?.click()}
            >
              Abrir rascunho
            </Button>
            <input
              ref={fileInput}
              type="file"
              accept="application/json,.json"
              className="sr-only"
              tabIndex={-1}
              aria-label="Arquivo de rascunho (.json)"
              onChange={onImport}
            />
          </div>
          <p role="status" aria-live="polite" className="min-h-5 text-sm text-slag-400">
            {importError}
          </p>

          {hasWork(draft) && (
            <div className="heat-glow flex max-w-xl flex-wrap items-center justify-between gap-4 border border-iron-700 p-4">
              <div className="flex flex-col gap-1">
                <span className="label-mono">Peça na bigorna</span>
                <span className="font-mono text-base text-iron-100">
                  {draft.name || 'biblioteca sem nome'}
                </span>
                <span className="text-sm text-iron-300">
                  {draft.skills.length} {draft.skills.length === 1 ? 'skill' : 'skills'} em trabalho
                </span>
              </div>
              <Button
                variant="forge"
                onClick={() => navigate('/bancada')}
                icon={<ArrowRight size={16} weight="bold" />}
              >
                Continuar
              </Button>
            </div>
          )}
        </div>

        <ForgeHearth className="relative mx-auto aspect-[4/5] w-full max-w-[460px]" />
      </section>

      <section aria-labelledby="passos" className="border-y border-iron-700 bg-coal-900/80">
        <div className="mx-auto grid max-w-[1400px] gap-px bg-iron-700 md:grid-cols-[1fr_1.25fr_1fr]">
          <h2 id="passos" className="sr-only">
            Como funciona
          </h2>
          {steps.map((step, index) => (
            <div
              key={step.number}
              className={
                index === 1
                  ? 'flex flex-col gap-3 bg-iron-850 px-6 py-10 md:py-14'
                  : 'flex flex-col gap-3 bg-coal-900 px-6 py-10'
              }
            >
              <div className="flex items-baseline gap-3">
                <span className="font-mono text-sm text-heat-4">{step.number}</span>
                <span className="label-mono">{step.place}</span>
              </div>
              <h3 className="font-display text-4xl font-extrabold uppercase">{step.title}</h3>
              <p className="max-w-[44ch] text-iron-300">{step.text}</p>
            </div>
          ))}
        </div>
      </section>

      <section
        aria-labelledby="mapa"
        className="mx-auto grid w-full max-w-[1400px] gap-10 px-4 py-20 md:grid-cols-[0.8fr_1.2fr] md:items-center"
      >
        <div className="flex flex-col gap-4">
          <SectionLabel>Mapa da liga</SectionLabel>
          <h2 id="mapa" className="font-display text-5xl leading-[0.9] font-black uppercase">
            Cada biblioteca sai com seu próprio grafo
          </h2>
          <p className="max-w-[52ch] text-iron-300">
            Abra a pasta no Obsidian e o Graph View mostra as skills ligadas às categorias, referências,
            scripts e skills relacionadas. Esta é a prévia de uma biblioteca de exemplo com 3 skills.
          </p>
          <Link to="/mapa" className="font-mono text-sm text-heat-5 underline-offset-4 hover:underline">
            Ver o mapa da sua biblioteca →
          </Link>
        </div>
        <div className="surface-steel border border-iron-700 p-4">
          <GraphPreview className="h-auto w-full" />
        </div>
      </section>
    </div>
  )
}
