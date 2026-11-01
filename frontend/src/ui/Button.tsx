import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { cn } from './cn'

type Variant = 'forge' | 'steel' | 'ghost' | 'quench'
type Size = 'sm' | 'md' | 'lg'

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  icon?: ReactNode
}

const variants: Record<Variant, string> = {
  // Ação principal: metal em brasa, texto carvão (4,7:1)
  forge:
    'text-coal-950 [--chamfer-bg:var(--color-heat-3)] hover:[--chamfer-bg:var(--color-heat-4)] disabled:[--chamfer-bg:var(--color-iron-700)] disabled:text-iron-400',
  // Secundária: aço com borda clara
  steel:
    'text-iron-100 [--chamfer-bg:var(--color-iron-700)] hover:[--chamfer-bg:var(--color-iron-600)] disabled:text-iron-400',
  ghost:
    'text-iron-300 hover:text-iron-100 [--chamfer-bg:transparent] hover:[--chamfer-bg:var(--color-iron-800)]',
  // Concluído / estável: aço temperado
  quench: 'text-coal-950 [--chamfer-bg:var(--color-steel-300)] hover:[--chamfer-bg:var(--color-steel-400)]',
}

const sizes: Record<Size, string> = {
  sm: 'h-8 gap-1.5 px-3 text-xs [--chamfer:5px]',
  md: 'h-10 gap-2 px-4 text-sm [--chamfer:7px]',
  lg: 'h-13 gap-2.5 px-6 text-base [--chamfer:10px]',
}

export function Button({
  variant = 'steel',
  size = 'md',
  icon,
  className,
  children,
  type,
  ...props
}: ButtonProps) {
  return (
    <button
      type={type ?? 'button'}
      className={cn(
        'chamfer inline-flex shrink-0 items-center justify-center font-medium whitespace-nowrap select-none',
        'transition-[transform,color] duration-150 active:translate-y-px active:scale-[0.98]',
        'disabled:cursor-not-allowed disabled:active:translate-y-0 disabled:active:scale-100',
        variants[variant],
        sizes[size],
        className,
      )}
      {...props}
    >
      {icon && (
        <span aria-hidden="true" className="inline-flex">
          {icon}
        </span>
      )}
      {children}
    </button>
  )
}
