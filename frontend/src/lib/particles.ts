/**
 * Física das partículas da forja, sem DOM: faíscas de impacto e brasas flutuantes.
 * Separado do canvas para poder ser testado.
 */

export interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  /** Vida restante, de 1 a 0. */
  life: number
  /** Quanto da vida perde por segundo. */
  decay: number
  size: number
}

export type Random = () => number

const GRAVITY = 900 // px/s²
const DRAG = 0.9 // fração de velocidade mantida por segundo nas brasas

/** Faíscas de um golpe de martelo: saem em leque para cima, caem com gravidade. */
export function spawnSparks(x: number, y: number, count: number, random: Random = Math.random): Particle[] {
  return Array.from({ length: count }, () => {
    const angle = -Math.PI / 2 + (random() - 0.5) * Math.PI * 1.1
    const speed = 260 + random() * 520
    return {
      x,
      y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 1,
      decay: 1.4 + random() * 1.6,
      size: 1 + random() * 1.8,
    }
  })
}

/** Brasa ambiente: nasce na base, sobe devagar e some antes de chegar ao meio da tela. */
export function spawnEmber(
  width: number,
  height: number,
  zones: [number, number][],
  random: Random = Math.random,
): Particle {
  const zone = zones[Math.floor(random() * zones.length)] ?? [0, 1]
  return {
    x: (zone[0] + random() * (zone[1] - zone[0])) * width,
    y: height + 4,
    vx: (random() - 0.5) * 18,
    vy: -(22 + random() * 38),
    life: 1,
    decay: 0.12 + random() * 0.12,
    size: 0.8 + random() * 1.4,
  }
}

export function stepSpark(p: Particle, dt: number): Particle {
  return {
    ...p,
    x: p.x + p.vx * dt,
    y: p.y + p.vy * dt,
    vy: p.vy + GRAVITY * dt,
    life: p.life - p.decay * dt,
  }
}

export function stepEmber(p: Particle, dt: number, time: number): Particle {
  const sway = Math.sin(time * 1.3 + p.x * 0.05) * 6
  return {
    ...p,
    x: p.x + (p.vx + sway) * dt,
    y: p.y + p.vy * dt,
    vx: p.vx * Math.pow(DRAG, dt),
    life: p.life - p.decay * dt,
  }
}

export const isAlive = (p: Particle) => p.life > 0

/** Cor da partícula pela vida: branco-amarelado quando nova, esfria para vermelho escuro. */
export function heatColor(life: number): string {
  if (life > 0.75) return '251 233 196'
  if (life > 0.5) return '245 196 106'
  if (life > 0.25) return '238 138 47'
  return '168 50 27'
}
