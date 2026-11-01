import { Circle, Fire, SealCheck } from '@phosphor-icons/react'
import type { ReactNode } from 'react'
import { cn } from './cn'

/** Estado de têmpera de uma peça. */
export type Temper = 'tempered' | 'hot' | 'crack' | 'raw'

const styles: Record<Temper, string> = {
  tempered: 'border-steel-400/50 bg-steel-600/30 text-steel-300',
  hot: 'border-heat-4/50 bg-heat-1/40 text-heat-5',
  crack: 'border-slag-400/60 bg-slag-900 text-slag-400 crack-line',
  raw: 'border-iron-600 bg-iron-800 text-iron-300',
}

const labels: Record<Temper, string> = {
  tempered: 'Aprovada',
  hot: 'Metal quente',
  crack: 'Trinca',
  raw: 'Bruta',
}

function CrackIcon() {
  return (
    <svg viewBox="0 0 16 16" width="12" height="12" fill="none" aria-hidden="true">
      <path d="M9 1 6.5 6l3 1.5L5 15" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="bevel" />
    </svg>
  )
}

const icons: Record<Temper, ReactNode> = {
  tempered: <SealCheck size={12} weight="bold" />,
  hot: <Fire size={12} weight="bold" />,
  crack: <CrackIcon />,
  raw: <Circle size={10} weight="bold" />,
}

interface SealProps {
  temper: Temper
  /** Texto alternativo; por padrão usa o nome do estado. */
  children?: ReactNode
  className?: string
}

/** Selo de têmpera: aprovada, metal quente (aviso), trinca (erro) ou bruta (não inspecionada). */
export function Seal({ temper, children, className }: SealProps) {
  return (
    <span
      className={cn(
        'inline-flex h-6 items-center gap-1.5 border px-2 font-mono text-[0.6875rem] tracking-[0.08em] uppercase',
        styles[temper],
        className,
      )}
    >
      {icons[temper]}
      {children ?? labels[temper]}
    </span>
  )
}
