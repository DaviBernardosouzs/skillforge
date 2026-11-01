import { useEffect, useState } from 'react'

/** Devolve o valor só depois de ele ficar parado por `delay` ms (para lint enquanto digita). */
export function useDebouncedValue<T>(value: T, delay: number): T {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay)
    return () => clearTimeout(timer)
  }, [value, delay])
  return debounced
}
