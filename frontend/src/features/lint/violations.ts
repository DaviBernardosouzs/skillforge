import type { Violation } from '../../api/types'
import { findLine, hasFrontmatter, readFrontmatterField } from '../../lib/frontmatter'
import type { Temper } from '../../ui/Seal'
import type { SkillEntry } from '../library/types'

const UNNAMED = '(sem nome)'

/**
 * Distribui as violações da biblioteca (do /libraries/lint ou do ProblemDetail 422) pelas skills.
 * O backend identifica a skill pelo name, por "(sem nome)" ou por "skill #N" quando o
 * frontmatter nem pôde ser lido.
 */
export function violationsBySkill(skills: SkillEntry[], violations: Violation[]): Map<string, Violation[]> {
  const result = new Map<string, Violation[]>(skills.map((s) => [s.id, []]))
  const names = skills.map((s) => readFrontmatterField(s.content, 'name').trim())

  for (const violation of violations) {
    const position = /^skill #(\d+)$/.exec(violation.skill)
    const targets = position
      ? [skills[Number(position[1]) - 1]]
      : skills.filter((s, i) =>
          violation.skill === UNNAMED
            ? hasFrontmatter(s.content) && names[i] === ''
            : names[i] === violation.skill,
        )
    for (const skill of targets) {
      if (skill) result.get(skill.id)?.push(violation)
    }
  }
  return result
}

/** Selo de têmpera: trinca se há ERROR, metal quente se só WARNING, aprovada se nada. */
export function temperOf(violations: Violation[] | undefined): Temper {
  if (!violations) return 'raw'
  if (violations.some((v) => v.severity === 'ERROR')) return 'crack'
  if (violations.length > 0) return 'hot'
  return 'tempered'
}

/** Linha do SKILL.md onde a violação "trinca", para marcar no editor e levar o cursor. */
export function locateViolation(content: string, violation: Violation): number {
  const quoted = /'([^']+)'/.exec(violation.message)?.[1]
  switch (violation.ruleId) {
    case 'frontmatter-present':
      return 1
    case 'name-required':
    case 'name-format':
    case 'name-unique':
      return findLine(content, 'name:') ?? 1
    case 'description-required':
    case 'description-length':
    case 'description-trigger':
      return findLine(content, 'description:') ?? 1
    case 'references-exist':
      return (quoted && findLine(content, quoted)) || bodyStart(content)
    default:
      return bodyStart(content)
  }
}

function bodyStart(content: string): number {
  const lines = content.replace(/\r\n/g, '\n').split('\n')
  if (lines[0] !== '---') return 1
  const close = lines.indexOf('---', 1)
  if (close < 0) return 1
  const firstText = lines.findIndex((line, i) => i > close && line.trim() !== '')
  return firstText < 0 ? Math.min(close + 2, lines.length) : firstText + 1
}

/** Nome amigável das regras, com o id técnico ao lado na interface. */
export const RULE_LABELS: Record<string, string> = {
  'frontmatter-present': 'Frontmatter ausente ou inválido',
  'name-required': 'Falta o name',
  'name-format': 'name fora do formato',
  'name-unique': 'name repetido',
  'description-required': 'Falta a description',
  'description-length': 'description longa demais',
  'description-trigger': 'description não diz quando usar',
  'references-exist': 'Link para arquivo que não existe',
  'body-not-empty': 'Corpo vazio',
  'body-length': 'Corpo longo demais',
  'file-path': 'Caminho de arquivo recusado',
  'related-exists': 'Skill relacionada não existe',
}
