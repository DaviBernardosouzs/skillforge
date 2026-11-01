import { useEffect } from 'react'
import { Button } from '../../ui/Button'

interface UndoToastProps {
  message: string | null
  onUndo: () => void
  onDismiss: () => void
  timeoutMs?: number
}

/** Aviso com "Desfazer" depois de remover uma skill. */
export function UndoToast({ message, onUndo, onDismiss, timeoutMs = 7000 }: UndoToastProps) {
  useEffect(() => {
    if (!message) return
    const timer = setTimeout(onDismiss, timeoutMs)
    return () => clearTimeout(timer)
  }, [message, onDismiss, timeoutMs])

  return (
    <div role="status" aria-live="polite" className="fixed bottom-24 left-4 z-50">
      {message && (
        <div className="surface-steel flex items-center gap-4 border border-iron-600 py-2 pr-2 pl-4">
          <span className="text-sm text-iron-100">{message}</span>
          <Button size="sm" variant="forge" onClick={onUndo}>
            Desfazer
          </Button>
        </div>
      )}
    </div>
  )
}
