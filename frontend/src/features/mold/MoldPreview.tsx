import { ArrowRight, Lightbulb } from '@phosphor-icons/react'
import type { DraftResponse } from '../../api/types'
import { Button } from '../../ui/Button'
import { HeatMeter } from '../../ui/HeatMeter'
import { Panel } from '../../ui/Panel'
import { Seal } from '../../ui/Seal'
import { RULE_LABELS } from '../lint/violations'
import { HINT_STEP, MOLD_STEPS, TOTAL_HINTS } from './schema'

interface MoldPreviewProps {
  result: DraftResponse | undefined
  pending: boolean
  error: string | null
  composable: boolean
  onGoToStep: (step: number) => void
  onTakeToBench: () => void
}

/** Prévia ao vivo: SKILL.md gerado, esqueletos, têmpera e conselhos do mestre. */
export function MoldPreview({
  result,
  pending,
  error,
  composable,
  onGoToStep,
  onTakeToBench,
}: MoldPreviewProps) {
  const hints = result?.hints ?? []
  const errors = result?.lint.errorCount ?? 0
  // Temperatura = conselhos já atendidos; uma trinca esfria a peça
  const heat = result ? (errors > 0 ? 0.15 : (TOTAL_HINTS - hints.length) / TOTAL_HINTS) : 0
  const ready = Boolean(result) && errors === 0

  return (
    <Panel
      label="Prévia · SKILL.md gerado"
      active={ready}
      className="lg:sticky lg:top-20 lg:max-h-[calc(100dvh-6rem)]"
    >
      <div className="flex flex-col gap-5 overflow-y-auto p-4">
        <HeatMeter
          value={heat}
          label="Temperatura da skill"
          description={
            result
              ? errors > 0
                ? 'Há trincas: corrija antes de levar à bancada.'
                : `${TOTAL_HINTS - hints.length} de ${TOTAL_HINTS} conselhos do mestre atendidos.`
              : 'Preencha os campos obrigatórios para acender a peça.'
          }
        />

        <p role="status" aria-live="polite" className="label-mono">
          {!composable
            ? 'Aguardando os obrigatórios'
            : pending
              ? 'Moldando…'
              : error
                ? 'Falha na prévia'
                : 'Prévia atualizada'}
        </p>
        {error && (
          <p role="alert" className="text-sm text-slag-400">
            {error}
          </p>
        )}

        {result && (
          <>
            {hints.length > 0 && (
              <section aria-labelledby="hints-title" className="flex flex-col gap-2">
                <h3 id="hints-title" className="label-mono">
                  Conselhos do mestre · dicas de qualidade
                </h3>
                <ul className="flex flex-col gap-2">
                  {hints.map((hint) => {
                    const step = HINT_STEP[hint.id]
                    return (
                      <li key={hint.id}>
                        <button
                          type="button"
                          onClick={() => step !== undefined && onGoToStep(step)}
                          className="flex w-full gap-3 border border-iron-700 bg-coal-900 p-3 text-left hover:border-heat-4/60"
                        >
                          <Lightbulb size={16} className="mt-0.5 shrink-0 text-heat-5" aria-hidden="true" />
                          <span className="flex flex-col gap-1">
                            <span className="text-sm text-iron-100">{hint.message}</span>
                            {step !== undefined && (
                              <span className="font-mono text-[0.6875rem] text-iron-400">
                                {hint.id} · resolver em “{MOLD_STEPS[step]?.title}”
                              </span>
                            )}
                          </span>
                        </button>
                      </li>
                    )
                  })}
                </ul>
              </section>
            )}

            <section aria-labelledby="lint-title" className="flex flex-col gap-2">
              <h3 id="lint-title" className="label-mono">
                Têmpera · validação
              </h3>
              {result.lint.violations.length === 0 ? (
                <Seal temper="tempered" />
              ) : (
                <ul className="flex flex-col gap-1.5">
                  {result.lint.violations.map((v, i) => (
                    <li key={`${v.ruleId}-${i}`} className="flex items-start gap-2 text-sm">
                      <Seal temper={v.severity === 'ERROR' ? 'crack' : 'hot'} className="shrink-0" />
                      <span className="text-iron-300">
                        {RULE_LABELS[v.ruleId] ?? v.ruleId}: {v.message}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section aria-labelledby="md-title" className="flex flex-col gap-2">
              <h3 id="md-title" className="label-mono">
                SKILL.md
              </h3>
              <pre className="max-h-96 overflow-auto border border-iron-700 bg-coal-900 p-3 font-mono text-xs leading-relaxed whitespace-pre-wrap text-iron-100">
                {result.skillMd}
              </pre>
            </section>

            {result.stubFiles.length > 0 && (
              <section aria-labelledby="stubs-title" className="flex flex-col gap-2">
                <h3 id="stubs-title" className="label-mono">
                  Esqueletos de arquivos ({result.stubFiles.length})
                </h3>
                <ul className="flex flex-col gap-px bg-iron-700">
                  {result.stubFiles.map((file) => (
                    <li key={file.path} className="bg-coal-900 px-3 py-2 font-mono text-xs text-iron-300">
                      {file.path}
                    </li>
                  ))}
                </ul>
              </section>
            )}
          </>
        )}

        <Button
          variant="forge"
          size="lg"
          icon={<ArrowRight size={18} weight="bold" />}
          disabled={!ready || pending}
          onClick={onTakeToBench}
        >
          Levar à bancada
        </Button>
      </div>
    </Panel>
  )
}
