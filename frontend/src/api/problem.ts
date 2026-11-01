import type { Violation } from './types'

/**
 * Corpo de erro RFC 9457 (ProblemDetail) devolvido pelo ApiExceptionHandler.
 * É um formato padrão do HTTP, por isso não vem do OpenAPI gerado.
 */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  /** 422: violações do lint. */
  violations?: Violation[]
  /** 400: erros de Bean Validation por campo. */
  errors?: { field: string; message: string }[]
}

export class ApiError extends Error {
  readonly status: number
  readonly problem: ProblemDetail

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Erro ${status}`)
    this.status = status
    this.problem = problem
  }
}

/** Falha de rede (backend fora do ar, sem internet). */
export class NetworkError extends Error {
  constructor() {
    super('Não foi possível falar com a forja. Confira a conexão e se o backend está no ar.')
  }
}

export async function toApiError(response: Response): Promise<ApiError> {
  let problem: ProblemDetail = { status: response.status, title: response.statusText }
  try {
    problem = { ...problem, ...((await response.json()) as ProblemDetail) }
  } catch {
    // corpo vazio ou não JSON (ex.: proxy fora do ar)
  }
  return new ApiError(response.status, problem)
}
