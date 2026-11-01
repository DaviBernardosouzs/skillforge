import type { HTMLAttributes, ReactNode } from 'react'
import { cn } from './cn'

interface PanelProps extends HTMLAttributes<HTMLElement> {
  /** Rótulo técnico do painel, ex.: "Têmpera · validação do SKILL.md". */
  label?: ReactNode
  actions?: ReactNode
  /** Peça em trabalho: o calor sobe da base do painel. */
  active?: boolean
  as?: 'section' | 'div' | 'aside'
}

export function Panel({
  label,
  actions,
  active,
  as: Tag = 'section',
  className,
  children,
  ...props
}: PanelProps) {
  return (
    <Tag
      className={cn(
        'flex min-h-0 flex-col border border-iron-700',
        active ? 'heat-glow' : 'surface-steel',
        className,
      )}
      {...props}
    >
      {(label || actions) && (
        <header className="flex min-h-11 items-center justify-between gap-3 border-b border-iron-700 px-4">
          {label && <h2 className="label-mono">{label}</h2>}
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </header>
      )}
      {children}
    </Tag>
  )
}
