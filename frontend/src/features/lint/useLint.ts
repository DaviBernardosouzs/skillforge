import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { api } from '../../api/client'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import { toLibraryRequest, toSkillRequest } from '../library/request'
import type { LibraryDraft, SkillEntry } from '../library/types'

export const SKILL_LINT_DELAY = 600
const LIBRARY_LINT_DELAY = 700
const KEBAB = /^[a-z0-9]+(-[a-z0-9]+)*$/

/** Têmpera da skill aberta: POST /api/skills/lint com debounce enquanto a pessoa digita. */
export function useSkillLint(skill: SkillEntry | undefined) {
  const body = skill ? JSON.stringify({ content: skill.content, files: toSkillRequest(skill).files }) : null
  const debounced = useDebouncedValue(body, SKILL_LINT_DELAY)
  const query = useQuery({
    queryKey: ['lint-skill', debounced],
    queryFn: () => api.lintSkill(JSON.parse(debounced as string)),
    enabled: debounced !== null,
    placeholderData: keepPreviousData,
    staleTime: Infinity,
  })
  return { ...query, isPending: body !== debounced || query.isFetching }
}

/** O que impede de validar (e forjar) a biblioteca inteira, antes mesmo de chamar a API. */
export function libraryBlocker(draft: LibraryDraft): string | null {
  if (!KEBAB.test(draft.name.trim())) return 'O nome da biblioteca precisa estar em kebab-case.'
  if (!draft.author.trim()) return 'Informe o autor da biblioteca.'
  if (draft.skills.length === 0) return 'Adicione pelo menos uma skill ao estoque.'
  return null
}

/** Têmpera da biblioteca inteira (regras entre skills, como name-unique). */
export function useLibraryLint(draft: LibraryDraft) {
  const blocker = libraryBlocker(draft)
  const body = blocker ? null : JSON.stringify(toLibraryRequest(draft))
  const debounced = useDebouncedValue(body, LIBRARY_LINT_DELAY)
  const query = useQuery({
    queryKey: ['lint-library', debounced],
    queryFn: () => api.lintLibrary(JSON.parse(debounced as string)),
    enabled: debounced !== null,
    placeholderData: keepPreviousData,
    staleTime: Infinity,
  })
  return { ...query, blocker, isPending: body !== debounced || query.isFetching }
}

export function useTemplates() {
  return useQuery({ queryKey: ['templates'], queryFn: api.templates, staleTime: Infinity })
}
