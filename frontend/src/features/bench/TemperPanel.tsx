import { useState } from 'react'
import type { Violation } from '../../api/types'
import { cn } from '../../ui/cn'
import { Panel } from '../../ui/Panel'
import { Seal } from '../../ui/Seal'
import { Tabs } from '../../ui/Tabs'
import { RULE_LABELS, temperOf } from '../lint/violations'
import { MarkdownPreview } from './MarkdownPreview'

interface TemperPanelProps {
  content: string | undefined
  violations: Violation[] | undefined
  inspecting: boolean
  error: string | null
  /** Muda a cada nova inspeção: dispara a varredura de brilho. */
  inspectionKey: number
  onLocate: (violation: Violation) => void
  className?: string
}

function summary(violations: Violation[] | undefined, inspecting: boolean): string {
  if (!violations) return inspecting ? 'Inspecionando a peça…' : 'Peça ainda não inspecionada.'
  const errors = violations.filter((v) => v.severity === 'ERROR').length
  const warnings = violations.length - errors
  if (violations.length === 0) return 'Têmpera aprovada: nenhuma trinca, nenhum metal quente.'
  return `${errors} ${errors === 1 ? 'trinca' : 'trincas'} e ${warnings} com metal quente.`
}

/** Têmpera: resultado do lint enquanto a pessoa digita, ou a prévia renderizada. */
export function TemperPanel({
  content,
  violations,
  inspecting,
  error,
  inspectionKey,
  onLocate,
  className,
}: TemperPanelProps) {
  const [view, setView] = useState<'temper' | 'preview'>('temper')
  const temper = temperOf(violations)

  return (
    <Panel label="Têmpera · validação do SKILL.md" className={cn('relative overflow-hidden', className)}>
      {/* Varredura de brilho a cada inspeção */}
      <span
        key={inspectionKey}
        aria-hidden="true"
        className="pointer-events-none absolute inset-y-0 left-0 z-10 w-full animate-sweep bg-[linear-gradient(90deg,transparent,rgb(245_196_106/0.12),transparent)]"
      />
      <Tabs
        label="Têmpera ou prévia"
        value={view}
        onChange={setView}
        className="border-b border-iron-700"
        items={[
          { id: 'temper', label: 'Têmpera' },
          { id: 'preview', label: 'Prévia' },
        ]}
      />
      <div className="flex-1 overflow-y-auto p-4">
        {view === 'preview' ? (
          content !== undefined ? (
            <MarkdownPreview content={content} />
          ) : (
            <p className="text-sm text-iron-400">Nenhuma skill selecionada.</p>
          )
        ) : (
          <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between gap-2">
              <Seal temper={inspecting && !violations ? 'raw' : temper} />
              {inspecting && <span className="label-mono">inspecionando…</span>}
            </div>
            <p role="status" aria-live="polite" className="text-sm text-iron-300">
              {content === undefined ? 'Nenhuma skill selecionada.' : summary(violations, inspecting)}
            </p>
            {error && (
              <p role="alert" className="text-sm text-slag-400">
                {error}
              </p>
            )}
            {violations && violations.length > 0 && (
              <ul className="flex flex-col gap-2" aria-label="Violações encontradas">
                {violations.map((violation, index) => (
                  <li key={`${violation.ruleId}-${index}`}>
                    <button
                      type="button"
                      onClick={() => onLocate(violation)}
                      className={cn(
                        'flex w-full flex-col gap-1.5 border p-3 text-left transition-colors',
                        violation.severity === 'ERROR'
                          ? 'crack-line border-slag-400/50 bg-slag-900 hover:border-slag-400'
                          : 'border-heat-4/40 bg-heat-1/20 hover:border-heat-4',
                      )}
                    >
                      <span className="flex items-center justify-between gap-2">
                        <span className="text-sm font-medium text-iron-100">
                          {RULE_LABELS[violation.ruleId] ?? violation.ruleId}
                        </span>
                        <Seal temper={violation.severity === 'ERROR' ? 'crack' : 'hot'} />
                      </span>
                      <span className="text-xs leading-relaxed text-iron-300">{violation.message}</span>
                      <span className="font-mono text-[0.6875rem] text-iron-400">
                        {violation.ruleId} · ir para o trecho
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>
    </Panel>
  )
}
