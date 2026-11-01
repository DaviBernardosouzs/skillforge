import { useReducedMotion as useMotionReducedMotion } from 'motion/react'

/** true quando o sistema pede menos movimento: sem partículas nem pulsos. */
export function useReducedMotion(): boolean {
  return useMotionReducedMotion() ?? false
}
