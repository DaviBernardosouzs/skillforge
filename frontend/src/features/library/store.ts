import { create } from 'zustand'
import { createJSONStorage, persist } from 'zustand/middleware'
import { newId } from '../../lib/id'
import { createSafeStorage } from '../../lib/safeStorage'
import { EMPTY_DRAFT, type FileEntry, type LibraryDraft, type SkillEntry } from './types'

export const DRAFT_STORAGE_KEY = 'skillforge:draft:v1'
export const draftStorage = createSafeStorage()

// O debounce não pode engolir as últimas alterações: grava na hora ao sair ou esconder a aba
if (typeof window !== 'undefined') {
  window.addEventListener('pagehide', () => draftStorage.flush())
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') draftStorage.flush()
  })
}

export type NewSkill = Partial<Omit<SkillEntry, 'id' | 'files'>> & {
  content: string
  files?: Omit<FileEntry, 'id'>[]
}

export interface RemovedSkill {
  skill: SkillEntry
  index: number
}

interface LibraryState {
  draft: LibraryDraft
  selectedId: string | null
  setMeta: (meta: Partial<Pick<LibraryDraft, 'name' | 'author' | 'license'>>) => void
  addSkill: (skill: NewSkill) => string
  updateSkill: (id: string, patch: Partial<Omit<SkillEntry, 'id'>>) => void
  removeSkill: (id: string) => RemovedSkill | null
  restoreSkill: (removed: RemovedSkill) => void
  duplicateSkill: (id: string) => string | null
  moveSkill: (id: string, delta: -1 | 1) => void
  select: (id: string | null) => void
  addFile: (skillId: string, file: Omit<FileEntry, 'id'>) => string
  updateFile: (skillId: string, fileId: string, patch: Partial<Omit<FileEntry, 'id'>>) => void
  removeFile: (skillId: string, fileId: string) => void
  replaceDraft: (draft: LibraryDraft) => void
  reset: () => void
}

function withIds(files: Omit<FileEntry, 'id'>[] = []): FileEntry[] {
  return files.map((file) => ({ ...file, id: newId() }))
}

function mapSkill(draft: LibraryDraft, id: string, change: (skill: SkillEntry) => SkillEntry): LibraryDraft {
  return { ...draft, skills: draft.skills.map((skill) => (skill.id === id ? change(skill) : skill)) }
}

export const useLibraryStore = create<LibraryState>()(
  persist(
    (set, get) => ({
      draft: EMPTY_DRAFT,
      selectedId: null,

      setMeta: (meta) => set((state) => ({ draft: { ...state.draft, ...meta } })),

      addSkill: ({ content, files, category = '', tags = [], related = [] }) => {
        const id = newId()
        const skill: SkillEntry = { id, content, files: withIds(files), category, tags, related }
        set((state) => ({
          draft: { ...state.draft, skills: [...state.draft.skills, skill] },
          selectedId: id,
        }))
        return id
      },

      updateSkill: (id, patch) =>
        set((state) => ({ draft: mapSkill(state.draft, id, (s) => ({ ...s, ...patch })) })),

      removeSkill: (id) => {
        const { draft, selectedId } = get()
        const index = draft.skills.findIndex((s) => s.id === id)
        const skill = draft.skills[index]
        if (!skill) return null
        const skills = draft.skills.filter((s) => s.id !== id)
        const nextSelected =
          selectedId === id ? (skills[Math.min(index, skills.length - 1)]?.id ?? null) : selectedId
        set({ draft: { ...draft, skills }, selectedId: nextSelected })
        return { skill, index }
      },

      restoreSkill: ({ skill, index }) =>
        set((state) => {
          const skills = [...state.draft.skills]
          skills.splice(Math.min(index, skills.length), 0, skill)
          return { draft: { ...state.draft, skills }, selectedId: skill.id }
        }),

      duplicateSkill: (id) => {
        const { draft } = get()
        const index = draft.skills.findIndex((s) => s.id === id)
        const original = draft.skills[index]
        if (!original) return null
        const copy: SkillEntry = {
          ...original,
          id: newId(),
          files: original.files.map((f) => ({ ...f, id: newId() })),
        }
        const skills = [...draft.skills]
        skills.splice(index + 1, 0, copy)
        set({ draft: { ...draft, skills }, selectedId: copy.id })
        return copy.id
      },

      moveSkill: (id, delta) =>
        set((state) => {
          const skills = [...state.draft.skills]
          const from = skills.findIndex((s) => s.id === id)
          const to = from + delta
          const moving = skills[from]
          if (!moving || to < 0 || to >= skills.length) return state
          skills.splice(from, 1)
          skills.splice(to, 0, moving)
          return { draft: { ...state.draft, skills } }
        }),

      select: (id) => set({ selectedId: id }),

      addFile: (skillId, file) => {
        const id = newId()
        set((state) => ({
          draft: mapSkill(state.draft, skillId, (s) => ({ ...s, files: [...s.files, { ...file, id }] })),
        }))
        return id
      },

      updateFile: (skillId, fileId, patch) =>
        set((state) => ({
          draft: mapSkill(state.draft, skillId, (s) => ({
            ...s,
            files: s.files.map((f) => (f.id === fileId ? { ...f, ...patch } : f)),
          })),
        })),

      removeFile: (skillId, fileId) =>
        set((state) => ({
          draft: mapSkill(state.draft, skillId, (s) => ({
            ...s,
            files: s.files.filter((f) => f.id !== fileId),
          })),
        })),

      replaceDraft: (draft) => set({ draft, selectedId: draft.skills[0]?.id ?? null }),

      reset: () => set({ draft: EMPTY_DRAFT, selectedId: null }),
    }),
    {
      name: DRAFT_STORAGE_KEY,
      storage: createJSONStorage(() => draftStorage),
      partialize: (state) => ({ draft: state.draft, selectedId: state.selectedId }),
      version: 1,
    },
  ),
)

/** Há trabalho guardado que vale mostrar como "Peça na bigorna"? */
export function hasWork(draft: LibraryDraft): boolean {
  return draft.skills.length > 0
}
