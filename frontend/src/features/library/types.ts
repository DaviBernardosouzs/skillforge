import type { License } from '../../api/types'

export type Encoding = 'utf8' | 'base64'

export interface FileEntry {
  id: string
  path: string
  content: string
  encoding: Encoding
}

export interface SkillEntry {
  id: string
  content: string
  files: FileEntry[]
  category: string
  tags: string[]
  related: string[]
}

/** A biblioteca em edição. Fica só no navegador: o backend não guarda nada. */
export interface LibraryDraft {
  version: 1
  name: string
  author: string
  license: License
  skills: SkillEntry[]
}

export const EMPTY_DRAFT: LibraryDraft = {
  version: 1,
  name: 'minhas-skills',
  author: '',
  license: 'MIT',
  skills: [],
}
