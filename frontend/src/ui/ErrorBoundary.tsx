import { Component, type ErrorInfo, type ReactNode } from 'react'
import { Button } from './Button'

interface Props {
  /** Nome da área, para a mensagem ("a Bancada", "o Mapa da liga"). */
  area: string
  children: ReactNode
}

interface State {
  error: Error | null
}

/** Uma falha numa tela não derruba o app: mostra o que houve e permite tentar de novo. */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(`Falha em ${this.props.area}`, error, info.componentStack)
  }

  render() {
    if (!this.state.error) return this.props.children
    return (
      <div role="alert" className="mx-auto flex max-w-xl flex-col gap-4 px-4 py-24">
        <p className="label-mono text-slag-400">Peça trincada</p>
        <h1 className="font-display text-4xl font-extrabold uppercase">Algo quebrou em {this.props.area}</h1>
        <p className="text-iron-300">
          Seu rascunho continua salvo neste navegador. Tente de novo; se repetir, recarregue a página.
        </p>
        <pre className="overflow-x-auto border border-iron-700 bg-coal-900 p-3 font-mono text-xs text-iron-400">
          {this.state.error.message}
        </pre>
        <div>
          <Button variant="forge" onClick={() => this.setState({ error: null })}>
            Tentar de novo
          </Button>
        </div>
      </div>
    )
  }
}
