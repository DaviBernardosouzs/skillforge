import { useEffect, useRef } from 'react'
import { useReducedMotion } from '../hooks/useReducedMotion'
import { heatColor, isAlive, spawnEmber, stepEmber, type Particle } from '../lib/particles'

interface EmberFieldProps {
  /** Faixas horizontais (0 a 1) onde as brasas nascem. Padrão: as margens, longe do texto. */
  zones?: [number, number][]
  /** Brasas vivas ao mesmo tempo. Poucas, de propósito. */
  count?: number
  className?: string
}

const DEFAULT_ZONES: [number, number][] = [
  [0, 0.08],
  [0.92, 1],
]

/** Brasas flutuando no fundo. Some por completo com prefers-reduced-motion. */
export function EmberField({ zones = DEFAULT_ZONES, count = 14, className }: EmberFieldProps) {
  const reduced = useReducedMotion()
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    const ctx = canvas?.getContext('2d')
    if (reduced || !canvas || !ctx) return

    let embers: Particle[] = []
    let frame = 0
    let last = performance.now()
    let width = 0
    let height = 0

    const resize = () => {
      const dpr = Math.min(window.devicePixelRatio || 1, 2)
      width = canvas.clientWidth
      height = canvas.clientHeight
      canvas.width = width * dpr
      canvas.height = height * dpr
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    }
    resize()
    const observer = new ResizeObserver(resize)
    observer.observe(canvas)

    const tick = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05)
      last = now
      if (embers.length < count && Math.random() < dt * count * 0.4) {
        embers.push(spawnEmber(width, height, zones))
      }
      embers = embers.map((e) => stepEmber(e, dt, now / 1000)).filter(isAlive)

      ctx.clearRect(0, 0, width, height)
      for (const e of embers) {
        ctx.fillStyle = `rgb(${heatColor(e.life)} / ${(e.life * 0.7).toFixed(3)})`
        ctx.fillRect(e.x, e.y, e.size, e.size)
      }
      frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)

    const onVisibility = () => {
      cancelAnimationFrame(frame)
      if (!document.hidden) {
        last = performance.now()
        frame = requestAnimationFrame(tick)
      }
    }
    document.addEventListener('visibilitychange', onVisibility)

    return () => {
      cancelAnimationFrame(frame)
      observer.disconnect()
      document.removeEventListener('visibilitychange', onVisibility)
    }
  }, [reduced, count, zones])

  if (reduced) return null
  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      className={className ?? 'pointer-events-none fixed inset-0 h-full w-full'}
    />
  )
}
