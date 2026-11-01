import { ArrowClockwise, Check, Copy, DownloadSimple, Fire } from '@phosphor-icons/react'
import { useEffect, useRef, useState } from 'react'
import { generateLibrary, saveBlob, type GeneratedZip } from '../../api/client'
import { ApiError } from '../../api/problem'
import type { Violation } from '../../api/types'
import { Button } from '../../ui/Button'
import { Dialog } from '../../ui/Dialog'
import { Seal } from '../../ui/Seal'
import { toLibraryRequest } from '../library/request'
import type { LibraryDraft } from '../library/types'
import { ForgeHearth } from './ForgeHearth'
import { describeForgeError, nextSteps, summarize } from './summary'

/** Ciclo mínimo da cerimônia: se a API responder rápido, a forja não some num piscar. */
export const MIN_CEREMONY_MS = 1200

type Stage =
  | { kind: 'review' }
  | { kind: 'forging' }
  | { kind: 'done'; zip: GeneratedZip }
  | { kind: 'failed'; title: string; detail: string; retry: boolean }

interface ForgeCeremonyProps {
  open: boolean
  draft: LibraryDraft
  /** Avisos (metal quente) da última têmpera da biblioteca. */
  warnings: number
  onClose: () => void
  /** 422: a peça trincou; as violações voltam para a bancada. */
  onCracked: (violations: Violation[]) => void
  minDuration?: number
}

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * Modal da cerimônia: resumo → golpes durante a requisição → peça esfriando e próximos passos.
 * Monte só enquanto estiver aberto: cada abertura começa do resumo.
 */
export function ForgeCeremony({
  open,
  draft,
  warnings,
  onClose,
  onCracked,
  minDuration = MIN_CEREMONY_MS,
}: ForgeCeremonyProps) {
  const [stage, setStage] = useState<Stage>({ kind: 'review' })
  const [accepted, setAccepted] = useState(false)
  const summary = summarize(draft)

  async function forge() {
    setStage({ kind: 'forging' })
    try {
      const [zip] = await Promise.all([generateLibrary(toLibraryRequest(draft)), wait(minDuration)])
      saveBlob(zip.blob, zip.fileName)
      setStage({ kind: 'done', zip })
    } catch (error) {
      if (error instanceof ApiError && error.status === 422) {
        onCracked(error.problem.violations ?? [])
        return
      }
      setStage({ kind: 'failed', ...describeForgeError(error) })
    }
  }

  const forging = stage.kind === 'forging'
  const needsAcceptance = warnings > 0 && !accepted

  return (
    <Dialog
      open={open}
      onClose={onClose}
      dismissable={!forging}
      label="Forjar · gerar o repositório .zip"
      title={stage.kind === 'done' ? 'Peça forjada' : stage.kind === 'failed' ? stage.title : 'Forjar'}
      className="w-[min(760px,calc(100vw-2rem))]"
    >
      <div className="grid gap-6 sm:grid-cols-[200px_minmax(0,1fr)]">
        <ForgeHearth
          beat={480}
          cooled={stage.kind === 'done'}
          className={`relative mx-auto aspect-[4/5] w-40 sm:w-full ${stage.kind === 'review' || stage.kind === 'failed' ? 'opacity-70' : ''}`}
          idle={!forging}
        />

        <div className="flex min-w-0 flex-col gap-5">
          <p role="status" aria-live="polite" className="sr-only">
            {forging && 'Forjando a biblioteca…'}
            {stage.kind === 'done' && `Biblioteca forjada. Download de ${stage.zip.fileName} iniciado.`}
          </p>

          {(stage.kind === 'review' || forging) && (
            <>
              <dl className="grid grid-cols-2 gap-x-6 gap-y-3 font-mono text-sm">
                <SummaryItem term="Skills" value={summary.skills} />
                <SummaryItem term="Arquivos extras" value={summary.files} />
                <SummaryItem
                  term="Categorias"
                  value={summary.categories.length > 0 ? summary.categories.join(', ') : 'nenhuma'}
                />
                <SummaryItem term="Licença" value={summary.license} />
              </dl>
              <p className="text-sm text-iron-300">
                Sai da forja um repositório Git com <span className="font-mono">{draft.name}/</span>, um
                commit inicial, CI de validação e o vault do Obsidian.
              </p>
              {warnings > 0 && (
                <label className="flex items-start gap-3 border border-heat-4/40 bg-heat-1/20 p-3 text-sm">
                  <input
                    type="checkbox"
                    checked={accepted}
                    disabled={forging}
                    onChange={(e) => setAccepted(e.target.checked)}
                    className="mt-1 accent-[var(--color-heat-4)]"
                  />
                  <span>
                    <Seal temper="hot">{warnings} metal quente</Seal> Há{' '}
                    {warnings === 1 ? 'um aviso' : `${warnings} avisos`} na têmpera. Eles não impedem a forja,
                    mas entram no relatório do CI. Forjar assim mesmo.
                  </span>
                </label>
              )}
              <div className="flex flex-wrap justify-end gap-3">
                <Button variant="ghost" onClick={onClose} disabled={forging}>
                  Voltar à bancada
                </Button>
                <Button
                  variant="forge"
                  size="lg"
                  icon={<Fire size={18} weight="fill" />}
                  disabled={forging || needsAcceptance}
                  onClick={() => void forge()}
                >
                  {forging ? 'Forjando…' : 'Forjar'}
                </Button>
              </div>
            </>
          )}

          {stage.kind === 'done' && (
            <DoneStage zip={stage.zip} name={draft.name} warnings={stage.zip.warnings} onClose={onClose} />
          )}

          {stage.kind === 'failed' && (
            <div className="flex flex-col gap-4">
              <p role="alert" className="text-slag-400">
                {stage.detail}
              </p>
              <div className="flex flex-wrap justify-end gap-3">
                <Button variant="ghost" onClick={onClose}>
                  Voltar à bancada
                </Button>
                {stage.retry && (
                  <Button variant="forge" icon={<ArrowClockwise size={16} />} onClick={() => void forge()}>
                    Tentar de novo
                  </Button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </Dialog>
  )
}

function SummaryItem({ term, value }: { term: string; value: string | number }) {
  return (
    <div className="flex min-w-0 flex-col gap-0.5">
      <dt className="label-mono">{term}</dt>
      <dd className="truncate text-iron-100">{value}</dd>
    </div>
  )
}

function DoneStage({
  zip,
  name,
  warnings,
  onClose,
}: {
  zip: GeneratedZip
  name: string
  warnings: number
  onClose: () => void
}) {
  const steps = nextSteps(name)
  const [copied, setCopied] = useState(false)
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined)
  useEffect(() => () => clearTimeout(timer.current), [])

  async function copy() {
    try {
      await navigator.clipboard.writeText(steps)
      setCopied(true)
      clearTimeout(timer.current)
      timer.current = setTimeout(() => setCopied(false), 2000)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <p className="text-iron-300">
        <span className="font-mono text-steel-300">{zip.fileName}</span> foi baixado
        {warnings > 0 ? `, com ${warnings} ${warnings === 1 ? 'aviso' : 'avisos'} de metal quente` : ''}. Leve
        para o GitHub:
      </p>
      <div className="relative border border-iron-700 bg-coal-950">
        <pre className="overflow-x-auto p-4 pr-12 font-mono text-sm leading-relaxed text-iron-100">
          <code>{steps}</code>
        </pre>
        <button
          type="button"
          onClick={() => void copy()}
          aria-label={copied ? 'Comandos copiados' : 'Copiar comandos'}
          className="absolute top-2 right-2 p-2 text-iron-400 hover:text-iron-100"
        >
          {copied ? <Check size={16} /> : <Copy size={16} />}
        </button>
      </div>
      <p className="text-sm text-iron-300">
        Troque <span className="font-mono">&lt;usuario&gt;</span> pelo seu usuário do GitHub e crie o
        repositório vazio antes do push. Abra a pasta no Obsidian para ver o Mapa da liga.
      </p>
      <div className="flex flex-wrap justify-end gap-3">
        <Button
          variant="ghost"
          icon={<DownloadSimple size={16} />}
          onClick={() => saveBlob(zip.blob, zip.fileName)}
        >
          Baixar de novo
        </Button>
        <Button variant="quench" onClick={onClose}>
          Voltar à bancada
        </Button>
      </div>
    </div>
  )
}
