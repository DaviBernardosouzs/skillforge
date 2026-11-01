import type { ReactNode } from 'react'
import { cn } from './cn'

/** Rótulo técnico entre colchetes: "[ TÊMPERA ]". */
export function SectionLabel({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <p className={cn('label-mono', className)}>
      <span aria-hidden="true" className="text-iron-600">
        [{' '}
      </span>
      {children}
      <span aria-hidden="true" className="text-iron-600">
        {' '}
        ]
      </span>
    </p>
  )
}
