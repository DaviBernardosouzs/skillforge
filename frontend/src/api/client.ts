import createClient from 'openapi-fetch'
import { ApiError, NetworkError, toApiError } from './problem'
import type { paths } from './schema'
import type {
  ContentResponse,
  DraftField,
  DraftResponse,
  LibraryGraph,
  LibraryRequest,
  LintReport,
  SkillDraft,
  SkillRequest,
  TemplateSummary,
} from './types'

/** Mesma origem: em dev o Vite faz proxy de /api; no Docker, o Nginx. */
const client = createClient<paths>({
  baseUrl: typeof window === 'undefined' ? '' : window.location.origin,
  // Resolve o fetch a cada chamada (e não na carga do módulo): permite interceptar nos testes
  fetch: (request) => globalThis.fetch(request),
})

async function unwrap<T>(call: Promise<{ data?: T; error?: unknown; response: Response }>): Promise<T> {
  let result: { data?: T; error?: unknown; response: Response }
  try {
    result = await call
  } catch {
    throw new NetworkError()
  }
  if (!result.response.ok || result.data === undefined) {
    throw new ApiError(result.response.status, (result.error ?? {}) as ApiError['problem'])
  }
  return result.data
}

export const api = {
  templates: (): Promise<TemplateSummary[]> => unwrap(client.GET('/api/templates')),

  fromTemplate: (body: { templateId: string; name: string; description: string }): Promise<ContentResponse> =>
    unwrap(client.POST('/api/skills/from-template', { body })),

  lintSkill: (body: Pick<SkillRequest, 'content' | 'files'>): Promise<LintReport> =>
    unwrap(client.POST('/api/skills/lint', { body })),

  lintLibrary: (body: LibraryRequest): Promise<LintReport> =>
    unwrap(client.POST('/api/libraries/lint', { body })),

  graph: (body: LibraryRequest): Promise<LibraryGraph> =>
    unwrap(client.POST('/api/libraries/graph', { body })),

  draftTemplate: (): Promise<DraftField[]> => unwrap(client.GET('/api/skills/draft/template')),

  draft: (body: SkillDraft): Promise<DraftResponse> => unwrap(client.POST('/api/skills/draft', { body })),
}

export interface GeneratedZip {
  blob: Blob
  fileName: string
  warnings: number
}

/** POST /api/libraries/generate devolve application/zip: lido como Blob, fora do openapi-fetch. */
export async function generateLibrary(body: LibraryRequest, signal?: AbortSignal): Promise<GeneratedZip> {
  let response: Response
  try {
    response = await fetch(`${window.location.origin}/api/libraries/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/zip, application/problem+json' },
      body: JSON.stringify(body),
      signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new NetworkError()
  }
  if (!response.ok) throw await toApiError(response)
  const disposition = response.headers.get('Content-Disposition') ?? ''
  const fileName = /filename="([^"]+)"/.exec(disposition)?.[1] ?? `${body.name}.zip`
  return {
    blob: await response.blob(),
    fileName,
    warnings: Number(response.headers.get('X-Lint-Warnings') ?? 0),
  }
}

export function saveBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
