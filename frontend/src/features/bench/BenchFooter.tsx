import { ArrowRight } from '@phosphor-icons/react'
import type { ReactNode } from 'react'
import { Button } from '../../ui/Button'
import { Seal } from '../../ui/Seal'

interface BenchFooterProps {
  errors: number
  warnings: number
  inspecting: boolean
  inspected: boolean
  /** Motivo de o Forjar estar bloqueado; null libera. */
  blockReason: string | null
  onForge: () => void
  extra?: ReactNode
}

/** Resumo da biblioteca inteira e o botão Forjar. */
export function BenchFooter({
  errors,
  warnings,
  inspecting,
  inspected,
  blockReason,
  onForge,
  extra,
}: BenchFooterProps) {
  return (
    <footer className="sticky bottom-0 z-30 border-t border-iron-700 bg-coal-950/95 backdrop-blur-sm">
      <div className="mx-auto flex max-w-[1600px] flex-wrap items-center justify-between gap-3 px-4 py-3">
        <div className="flex flex-wrap items-center gap-3" role="status" aria-live="polite">
          <span className="label-mono">Biblioteca</span>
          {!inspected ? (
            <Seal temper="raw">{inspecting ? 'Inspecionando' : 'Não inspecionada'}</Seal>
          ) : (
            <>
              <Seal temper={errors > 0 ? 'crack' : 'tempered'}>
                {errors} {errors === 1 ? 'trinca' : 'trincas'}
              </Seal>
              <Seal temper={warnings > 0 ? 'hot' : 'tempered'}>{warnings} metal quente</Seal>
            </>
          )}
          {extra}
        </div>
        <div className="flex items-center gap-3">
          {blockReason && (
            <p id="forge-blocked" className="max-w-[40ch] text-xs text-iron-300">
              {blockReason}
            </p>
          )}
          <Button
            variant="forge"
            size="lg"
            icon={<ArrowRight size={18} weight="bold" />}
            disabled={blockReason !== null}
            aria-describedby={blockReason ? 'forge-blocked' : undefined}
            onClick={onForge}
          >
            Forjar
          </Button>
        </div>
      </div>
    </footer>
  )
}
