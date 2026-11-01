import { motion, useAnimationControls } from 'motion/react'
import { useEffect, useRef } from 'react'
import { useReducedMotion } from '../../hooks/useReducedMotion'
import { SparkBurst, type SparkBurstHandle } from '../../ui/SparkBurst'

interface ForgeHearthProps {
  /** Intervalo entre golpes, em ms. */
  beat?: number
  className?: string
}

/**
 * A peça incandescente sobre o bloco de aço. Sem ilustração literal: só forma, calor e ritmo.
 * A cada golpe a peça comprime, clareia e solta faíscas. Com movimento reduzido fica estática.
 */
export function ForgeHearth({ beat = 2200, className }: ForgeHearthProps) {
  const reduced = useReducedMotion()
  const billet = useAnimationControls()
  const sparks = useRef<SparkBurstHandle>(null)
  const stage = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (reduced) return
    const strike = () => {
      const rect = stage.current?.getBoundingClientRect()
      if (rect) sparks.current?.burst(rect.width * 0.5, rect.height * 0.555, 30)
      void billet.start({
        scaleY: [1, 0.93, 1.02, 1],
        scaleX: [1, 1.03, 0.995, 1],
        filter: ['brightness(1)', 'brightness(1.35)', 'brightness(1.05)', 'brightness(1)'],
        transition: { duration: 0.55, times: [0, 0.12, 0.4, 1], ease: 'easeOut' },
      })
    }
    const first = setTimeout(strike, 500)
    const interval = setInterval(strike, beat)
    return () => {
      clearTimeout(first)
      clearInterval(interval)
    }
  }, [reduced, beat, billet])

  return (
    <div ref={stage} aria-hidden="true" className={className ?? 'relative aspect-[4/5] w-full'}>
      {/* Luz da forja vindo de baixo */}
      <div className="absolute inset-x-[-10%] bottom-[-5%] h-[70%] bg-[radial-gradient(ellipse_at_50%_100%,rgb(217_83_31/0.38),rgb(168_50_27/0.14)_40%,transparent_70%)]" />

      {/* Bloco de aço */}
      <div
        className="absolute inset-x-[8%] top-[60%] bottom-[12%] surface-steel border border-iron-700"
        style={{ clipPath: 'polygon(6% 0, 94% 0, 100% 14%, 100% 100%, 0 100%, 0 14%)' }}
      >
        <div className="absolute inset-x-0 top-0 h-[2px] bg-gradient-to-r from-transparent via-heat-4/70 to-transparent" />
        <div className="absolute inset-x-[10%] top-[30%] h-px bg-iron-700" />
        <div className="absolute right-[10%] bottom-[18%] font-mono text-[0.625rem] tracking-[0.2em] text-iron-600 uppercase">
          liga 01 · skills
        </div>
      </div>
      <div className="absolute inset-x-[20%] top-[88%] bottom-[2%] border-x border-b border-iron-700 bg-coal-900" />

      {/* A peça */}
      <motion.div
        animate={billet}
        className="absolute inset-x-[22%] top-[46%] h-[14%] origin-bottom"
        style={{
          background:
            'linear-gradient(180deg, var(--color-heat-6) 0%, var(--color-heat-5) 22%, var(--color-heat-4) 58%, var(--color-heat-3) 85%, var(--color-heat-2) 100%)',
          boxShadow: 'inset 0 -6px 12px rgb(110 31 20 / 0.6), inset 0 2px 0 rgb(255 255 255 / 0.5)',
          clipPath: 'polygon(3% 0, 97% 0, 100% 100%, 0 100%)',
        }}
      >
        <div className="absolute inset-x-[12%] top-[38%] h-[2px] bg-heat-6/70" />
        <div className="absolute inset-x-[30%] top-[64%] h-px bg-heat-2/50" />
      </motion.div>

      {/* Ar quente acima da peça */}
      <div className="absolute inset-x-[15%] top-[22%] h-[30%] bg-[radial-gradient(ellipse_at_50%_100%,rgb(245_196_106/0.16),transparent_65%)]" />

      <SparkBurst ref={sparks} />
    </div>
  )
}
