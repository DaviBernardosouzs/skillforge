import type { components } from './schema'

/** Tipos gerados do OpenAPI do backend (npm run gen:api). Nada de DTO tipado à mão. */
type Schemas = components['schemas']

export type LibraryRequest = Schemas['LibraryRequest']
export type SkillRequest = Schemas['SkillRequest']
export type FileRequest = Schemas['FileRequest']
export type License = LibraryRequest['license']
export type LintReport = Schemas['LintReportResponse']
export type Violation = Schemas['Violation']
export type TemplateSummary = Schemas['TemplateSummary']
export type ContentResponse = Schemas['ContentResponse']
export type SkillDraft = Schemas['SkillDraft']
export type DraftResponse = Schemas['DraftResponse']
export type DraftField = Schemas['Field']
export type QualityHint = Schemas['QualityHint']
export type StubFile = Schemas['StubFileResponse']
export type LibraryGraph = Schemas['LibraryGraph']
export type GraphEdge = Schemas['GraphEdge']

/** O backend devolve null em path/category/description de nós que não são skill. */
export type GraphNode = Omit<Schemas['GraphNode'], 'path' | 'category' | 'description'> & {
  path: string | null
  category: string | null
  description: string | null
}
