import { X } from '@phosphor-icons/react'
import { useEffect, useRef, type ReactNode } from 'react'
import { cn } from './cn'

interface DialogProps {
  open: boolean
  onClose: () => void
  title: ReactNode
  label?: ReactNode
  children: ReactNode
  className?: string
  /** Bloqueia fechar (ex.: durante a forja). */
  dismissable?: boolean
}

/** <dialog> nativo: foco preso, Esc fecha e o resto da página fica inerte. */
export function Dialog({
  open,
  onClose,
  title,
  label,
  children,
  className,
  dismissable = true,
}: DialogProps) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) {
      if (typeof dialog.showModal === 'function') dialog.showModal()
      else dialog.setAttribute('open', '')
    }
    if (!open && dialog.open) {
      if (typeof dialog.close === 'function') dialog.close()
      else dialog.removeAttribute('open')
    }
  }, [open])

  return (
    <dialog
      ref={ref}
      onCancel={(event) => {
        event.preventDefault()
        if (dismissable) onClose()
      }}
      aria-labelledby="dialog-title"
      className={cn(
        'm-auto max-h-[90dvh] w-[min(640px,calc(100vw-2rem))] border border-iron-600 bg-iron-850 p-0 text-iron-100 shadow-[var(--shadow-cold)]',
        'backdrop:bg-coal-950/80 backdrop:backdrop-blur-[2px]',
        className,
      )}
    >
      {open && (
        <div className="flex max-h-[90dvh] flex-col">
          <header className="flex items-start justify-between gap-4 border-b border-iron-700 px-5 py-4">
            <div className="flex flex-col gap-1">
              {label && <span className="label-mono">{label}</span>}
              <h2 id="dialog-title" className="font-display text-3xl leading-none font-extrabold uppercase">
                {title}
              </h2>
            </div>
            {dismissable && (
              <button
                type="button"
                onClick={onClose}
                aria-label="Fechar"
                className="p-1 text-iron-400 hover:text-iron-100"
              >
                <X size={20} />
              </button>
            )}
          </header>
          <div className="overflow-y-auto p-5">{children}</div>
        </div>
      )}
    </dialog>
  )
}
