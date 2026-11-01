import { HEAT_SCALE } from '../lib/heat'
import { cn } from './cn'

interface HeatMeterProps {
  /** 0 a 1. */
  value: number
  label: string
  /** Texto curto que explica a leitura (lido por leitores de tela). */
  description?: string
  className?: string
}

/** Medidor segmentado: cada segmento acende na cor da temperatura correspondente. */
export function HeatMeter({ value, label, description, className }: HeatMeterProps) {
  const clamped = Math.min(1, Math.max(0, value))
  const lit = Math.round(clamped * HEAT_SCALE.length)
  return (
    <div className={cn('flex flex-col gap-2', className)}>
      <div className="flex items-baseline justify-between">
        <span className="label-mono">{label}</span>
        <span className="font-mono text-xs text-iron-300">{Math.round(clamped * 100)}%</span>
      </div>
      <div
        role="meter"
        aria-label={label}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(clamped * 100)}
        aria-valuetext={description}
        className="grid grid-cols-6 gap-px border border-iron-700 bg-iron-700"
      >
        {HEAT_SCALE.map((color, index) => (
          <span
            key={color}
            className="h-2.5 transition-[background-color,opacity] duration-500"
            style={{ backgroundColor: index < lit ? color : 'var(--color-coal-900)' }}
          />
        ))}
      </div>
      {description && <p className="text-xs text-iron-400">{description}</p>}
    </div>
  )
}
