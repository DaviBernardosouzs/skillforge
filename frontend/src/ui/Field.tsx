import {
  useId,
  type InputHTMLAttributes,
  type ReactNode,
  type SelectHTMLAttributes,
  type TextareaHTMLAttributes,
} from 'react'
import { cn } from './cn'

interface FieldProps {
  label: ReactNode
  hint?: ReactNode
  error?: ReactNode
  /** Recebe o id do controle e os ids de ajuda/erro para aria-describedby. */
  children: (ids: { id: string; describedBy: string | undefined; invalid: boolean }) => ReactNode
  className?: string
}

/** Rótulo acima, ajuda opcional e erro abaixo do controle. */
export function Field({ label, hint, error, children, className }: FieldProps) {
  const id = useId()
  const hintId = hint ? `${id}-hint` : undefined
  const errorId = error ? `${id}-error` : undefined
  const describedBy = [hintId, errorId].filter(Boolean).join(' ') || undefined
  return (
    <div className={cn('flex flex-col gap-2', className)}>
      <label htmlFor={id} className="text-sm font-medium text-iron-100">
        {label}
      </label>
      {children({ id, describedBy, invalid: Boolean(error) })}
      {hint && !error && (
        <p id={hintId} className="text-xs leading-relaxed text-iron-400">
          {hint}
        </p>
      )}
      {error && (
        <p id={errorId} className="text-xs leading-relaxed text-slag-400" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}

const control =
  'w-full border border-iron-700 bg-coal-900 px-3 text-sm text-iron-100 placeholder:text-iron-400/70 ' +
  'shadow-[inset_0_1px_2px_rgb(0_0_0/0.5)] transition-colors hover:border-iron-600 ' +
  'focus-visible:border-heat-4 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-heat-4/40 ' +
  'aria-[invalid=true]:border-slag-400 disabled:opacity-60'

export function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={cn(control, 'h-10', className)} {...props} />
}

export function Textarea({ className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea className={cn(control, 'min-h-24 py-2 leading-relaxed', className)} {...props} />
}

export function Select({ className, children, ...props }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select className={cn(control, 'h-10 appearance-none bg-coal-900 pr-8', className)} {...props}>
      {children}
    </select>
  )
}
