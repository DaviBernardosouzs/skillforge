import { ApiError, NetworkError } from '../../api/problem'
import type { LibraryDraft } from '../library/types'

export interface ForgeSummary {
  skills: number
  categories: string[]
  /** Arquivos enviados junto das skills (references/, scripts/, assets/), sem contar os SKILL.md. */
  files: number
  license: string
}

/** O que vai sair da forja, contado a partir do rascunho real. */
export function summarize(draft: LibraryDraft): ForgeSummary {
  const categories = [...new Set(draft.skills.map((s) => s.category.trim()).filter(Boolean))].sort()
  return {
    skills: draft.skills.length,
    categories,
    files: draft.skills.reduce((total, s) => total + s.files.length, 0),
    license: draft.license,
  }
}

/** Mensagem honesta para cada falha do /generate que não é 422 (esse volta para a bancada). */
export function describeForgeError(error: unknown): { title: string; detail: string; retry: boolean } {
  if (error instanceof ApiError) {
    if (error.status === 413) {
      return {
        title: 'Peça grande demais para a forja',
        detail: `${error.problem.detail ?? 'A biblioteca passou de um limite de tamanho.'} Remova arquivos ou skills e tente de novo.`,
        retry: false,
      }
    }
    if (error.status === 400) {
      const fields = (error.problem.errors ?? []).map((e) => `${e.field}: ${e.message}`).join('; ')
      return {
        title: 'A forja recusou a encomenda',
        detail: fields || error.message,
        retry: false,
      }
    }
    return {
      title: 'A forja falhou',
      detail: `O servidor respondeu ${error.status}. Seu rascunho continua salvo; tente de novo em instantes.`,
      retry: true,
    }
  }
  if (error instanceof NetworkError) {
    return { title: 'Sem contato com a forja', detail: error.message, retry: true }
  }
  return {
    title: 'A forja falhou',
    detail: 'Aconteceu um erro inesperado. Seu rascunho continua salvo.',
    retry: true,
  }
}

/** Comandos para levar o zip ao GitHub. */
export function nextSteps(name: string): string {
  return [
    `unzip ${name}.zip && cd ${name}`,
    `git remote add origin git@github.com:<usuario>/${name}.git`,
    'git push -u origin main',
  ].join('\n')
}
