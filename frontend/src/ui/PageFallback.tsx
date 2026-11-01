/** Enquanto o código de uma tela carrega (code-splitting por rota). */
export function PageFallback() {
  return (
    <div className="flex flex-1 items-center justify-center p-12" role="status" aria-live="polite">
      <span className="label-mono">Aquecendo a peça…</span>
    </div>
  )
}
