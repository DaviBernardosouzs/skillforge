import { useId, useRef, type KeyboardEvent, type ReactNode } from 'react'
import { cn } from './cn'

export interface TabItem<T extends string> {
  id: T
  label: ReactNode
  badge?: ReactNode
}

interface TabsProps<T extends string> {
  items: TabItem<T>[]
  value: T
  onChange: (id: T) => void
  label: string
  className?: string
}

/** Abas acessíveis: setas, Home e End movem entre abas (padrão WAI-ARIA). */
export function Tabs<T extends string>({ items, value, onChange, label, className }: TabsProps<T>) {
  const baseId = useId()
  const refs = useRef<(HTMLButtonElement | null)[]>([])

  function onKeyDown(event: KeyboardEvent, index: number) {
    const last = items.length - 1
    const next =
      event.key === 'ArrowRight'
        ? index === last
          ? 0
          : index + 1
        : event.key === 'ArrowLeft'
          ? index === 0
            ? last
            : index - 1
          : event.key === 'Home'
            ? 0
            : event.key === 'End'
              ? last
              : null
    if (next === null) return
    event.preventDefault()
    const item = items[next]
    if (item) {
      onChange(item.id)
      refs.current[next]?.focus()
    }
  }

  return (
    <div
      role="tablist"
      aria-label={label}
      className={cn('flex w-fit max-w-full gap-px overflow-x-auto bg-iron-700', className)}
    >
      {items.map((item, index) => {
        const selected = item.id === value
        return (
          <button
            key={item.id}
            ref={(el) => {
              refs.current[index] = el
            }}
            type="button"
            role="tab"
            id={`${baseId}-${item.id}`}
            aria-selected={selected}
            tabIndex={selected ? 0 : -1}
            onClick={() => onChange(item.id)}
            onKeyDown={(event) => onKeyDown(event, index)}
            className={cn(
              'flex h-10 shrink-0 items-center gap-2 px-4 font-mono text-xs tracking-[0.08em] uppercase transition-colors',
              selected
                ? 'bg-iron-850 text-heat-5 shadow-[inset_0_-2px_0_var(--color-heat-3)]'
                : 'bg-coal-900 text-iron-400 hover:text-iron-100',
            )}
          >
            {item.label}
            {item.badge}
          </button>
        )
      })}
    </div>
  )
}
