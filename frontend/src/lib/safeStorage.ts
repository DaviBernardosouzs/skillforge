import type { StateStorage } from 'zustand/middleware'

/**
 * localStorage que nunca derruba o app (aba anônima, cota cheia, armazenamento bloqueado)
 * e grava com debounce para não serializar a biblioteca a cada tecla.
 */
export function createSafeStorage(
  delayMs = 400,
  backend: () => Storage = () => window.localStorage,
): StateStorage & {
  flush: () => void
} {
  const pending = new Map<string, string>()
  let timer: ReturnType<typeof setTimeout> | null = null

  const flush = () => {
    if (timer) clearTimeout(timer)
    timer = null
    for (const [key, value] of pending) {
      try {
        backend().setItem(key, value)
      } catch (error) {
        console.warn('Não foi possível salvar o rascunho neste navegador.', error)
      }
    }
    pending.clear()
  }

  return {
    getItem(key) {
      try {
        return backend().getItem(key)
      } catch {
        return null
      }
    },
    setItem(key, value) {
      pending.set(key, value)
      if (timer) clearTimeout(timer)
      timer = setTimeout(flush, delayMs)
    },
    removeItem(key) {
      pending.delete(key)
      try {
        backend().removeItem(key)
      } catch {
        // armazenamento indisponível: não há o que remover
      }
    },
    flush,
  }
}
