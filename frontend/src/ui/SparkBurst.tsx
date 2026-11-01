import { useEffect, useImperativeHandle, useRef, type Ref } from 'react'
import { useReducedMotion } from '../hooks/useReducedMotion'
import { heatColor, isAlive, spawnSparks, stepSpark, type Particle } from '../lib/particles'

export interface SparkBurstHandle {
  /** Solta faíscas a partir de (x, y), em pixels relativos ao canvas. */
  burst: (x: number, y: number, count?: number) => void
}

/**
 * Canvas sobreposto que desenha faíscas de impacto. O loop só roda enquanto há faísca viva.
 * Com prefers-reduced-motion, burst não faz nada.
 */
export function SparkBurst({ ref, className }: { ref?: Ref<SparkBurstHandle>; className?: string }) {
  const reduced = useReducedMotion()
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const sparks = useRef<Particle[]>([])
  const frame = useRef(0)

  useEffect(() => () => cancelAnimationFrame(frame.current), [])

  useImperativeHandle(ref, () => {
    let last = 0

    function draw(now: number) {
      const canvas = canvasRef.current
      const ctx = canvas?.getContext('2d')
      if (!canvas || !ctx) return
      const dt = Math.min((now - last) / 1000, 0.05)
      last = now
      sparks.current = sparks.current.map((s) => stepSpark(s, dt)).filter(isAlive)

      ctx.clearRect(0, 0, canvas.clientWidth, canvas.clientHeight)
      for (const s of sparks.current) {
        ctx.strokeStyle = `rgb(${heatColor(s.life)} / ${s.life.toFixed(3)})`
        ctx.lineWidth = s.size
        ctx.beginPath()
        ctx.moveTo(s.x, s.y)
        ctx.lineTo(s.x - s.vx * 0.018, s.y - s.vy * 0.018)
        ctx.stroke()
      }
      frame.current = sparks.current.length > 0 ? requestAnimationFrame(draw) : 0
    }

    return {
      burst(x, y, count = 26) {
        const canvas = canvasRef.current
        const ctx = canvas?.getContext('2d')
        if (reduced || !canvas || !ctx) return
        const dpr = Math.min(window.devicePixelRatio || 1, 2)
        if (canvas.width !== canvas.clientWidth * dpr) {
          canvas.width = canvas.clientWidth * dpr
          canvas.height = canvas.clientHeight * dpr
          ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
        }
        sparks.current.push(...spawnSparks(x, y, count))
        if (!frame.current) {
          last = performance.now()
          frame.current = requestAnimationFrame(draw)
        }
      },
    }
  }, [reduced])

  if (reduced) return null
  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      className={className ?? 'pointer-events-none absolute inset-0 h-full w-full'}
    />
  )
}
