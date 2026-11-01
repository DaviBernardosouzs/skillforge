import type { FileRequest, LibraryRequest, SkillRequest } from '../../api/types'
import type { LibraryDraft, SkillEntry } from './types'

/** Monta o corpo de /api/skills/lint, /libraries/* a partir do estado do navegador. */
export function toSkillRequest(skill: SkillEntry): SkillRequest {
  return {
    content: skill.content,
    files: skill.files.map((file): FileRequest => ({
      path: file.path.trim(),
      content: file.content,
      encoding: file.encoding,
    })),
    // Em branco vira null no backend (skill sem categoria)
    category: skill.category.trim(),
    tags: skill.tags.map((t) => t.trim()).filter(Boolean),
    related: skill.related.map((r) => r.trim()).filter(Boolean),
  }
}

export function toLibraryRequest(draft: LibraryDraft): LibraryRequest {
  return {
    name: draft.name.trim(),
    author: draft.author.trim(),
    license: draft.license,
    skills: draft.skills.map(toSkillRequest),
  }
}
