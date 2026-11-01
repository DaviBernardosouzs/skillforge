import { z } from 'zod'
import { newId } from '../../lib/id'
import type { LibraryDraft } from './types'

const fileSchema = z.object({
  path: z.string(),
  content: z.string(),
  encoding: z.enum(['utf8', 'base64']).default('utf8'),
})

const skillSchema = z.object({
  content: z.string(),
  files: z.array(fileSchema).default([]),
  category: z.string().default(''),
  tags: z.array(z.string()).default([]),
  related: z.array(z.string()).default([]),
})

/** Formato do arquivo "Exportar rascunho": igual ao LibraryRequest, para servir também na API. */
export const draftFileSchema = z.object({
  name: z.string(),
  author: z.string().default(''),
  license: z.enum(['MIT', 'APACHE_2_0', 'CC_BY_4_0', 'NONE']).default('MIT'),
  skills: z.array(skillSchema),
})

export type DraftFile = z.infer<typeof draftFileSchema>

export function toDraftFile(draft: LibraryDraft): DraftFile {
  return {
    name: draft.name,
    author: draft.author,
    license: draft.license,
    skills: draft.skills.map(({ content, files, category, tags, related }) => ({
      content,
      files: files.map(({ path, content: fileContent, encoding }) => ({
        path,
        content: fileContent,
        encoding,
      })),
      category,
      tags,
      related,
    })),
  }
}

export class DraftFileError extends Error {}

/** Lê um .json exportado (ou um LibraryRequest, como examples/library-request.json). */
export function parseDraftFile(text: string): LibraryDraft {
  let json: unknown
  try {
    json = JSON.parse(text)
  } catch {
    throw new DraftFileError('O arquivo não é um JSON válido.')
  }
  const result = draftFileSchema.safeParse(json)
  if (!result.success) {
    const issue = result.error.issues[0]
    throw new DraftFileError(
      `O arquivo não parece um rascunho do SkillForge${issue ? ` (${issue.path.join('.') || 'raiz'}: ${issue.message})` : ''}.`,
    )
  }
  const data = result.data
  return {
    version: 1,
    name: data.name,
    author: data.author,
    license: data.license,
    skills: data.skills.map((skill) => ({
      ...skill,
      id: newId(),
      files: skill.files.map((file) => ({ ...file, id: newId() })),
    })),
  }
}

export function downloadDraft(draft: LibraryDraft) {
  const blob = new Blob([JSON.stringify(toDraftFile(draft), null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${draft.name || 'rascunho'}.skillforge.json`
  link.click()
  URL.revokeObjectURL(url)
}
