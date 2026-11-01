import { z } from 'zod'
import type { SkillDraft } from '../../api/types'

const text = z.object({ value: z.string() })

/** Formulário do Molde. Listas de texto viram { value } por causa do useFieldArray. */
export const moldSchema = z
  .object({
    name: z
      .string()
      .min(1, 'Dê um nome à skill.')
      .max(64, 'Até 64 caracteres.')
      .regex(
        /^[a-z0-9]+(-[a-z0-9]+)*$/,
        'Use kebab-case: minúsculas, números e hífens (ex.: revisao-de-codigo).',
      ),
    purpose: z.string().trim().min(1, 'Diga em uma frase o que a skill faz.'),
    triggers: z.array(text),
    antiTriggers: z.array(text),
    inputs: z.array(text),
    steps: z.array(z.object({ title: z.string(), detail: z.string() })),
    outputFormat: z.string(),
    examples: z.array(z.object({ input: z.string(), expectedOutput: z.string() })),
    pitfalls: z.array(text),
    qualityChecks: z.array(text),
    resources: z.array(
      z.object({ kind: z.enum(['SCRIPT', 'REFERENCE', 'ASSET']), fileName: z.string(), purpose: z.string() }),
    ),
  })
  .superRefine((values, ctx) => {
    if (!values.triggers.some((t) => t.value.trim())) {
      ctx.addIssue({ code: 'custom', path: ['triggers'], message: 'Liste pelo menos uma situação de uso.' })
    }
    if (!values.steps.some((s) => s.title.trim())) {
      ctx.addIssue({ code: 'custom', path: ['steps'], message: 'Descreva pelo menos um passo do processo.' })
    }
  })

export type MoldValues = z.infer<typeof moldSchema>

export const EMPTY_MOLD: MoldValues = {
  name: '',
  purpose: '',
  triggers: [{ value: '' }],
  antiTriggers: [],
  inputs: [],
  steps: [{ title: '', detail: '' }],
  outputFormat: '',
  examples: [],
  pitfalls: [],
  qualityChecks: [],
  resources: [],
}

const values = (items: { value: string }[]) => items.map((i) => i.value.trim()).filter(Boolean)

/** Converte o formulário no SkillDraft da API, descartando linhas em branco. */
export function toSkillDraft(form: MoldValues): SkillDraft {
  return {
    name: form.name.trim(),
    purpose: form.purpose.trim(),
    triggers: values(form.triggers),
    antiTriggers: values(form.antiTriggers),
    inputs: values(form.inputs),
    steps: form.steps
      .filter((s) => s.title.trim())
      .map((s) => ({ title: s.title.trim(), detail: s.detail.trim() })),
    outputFormat: form.outputFormat.trim(),
    examples: form.examples
      .filter((e) => e.input.trim() && e.expectedOutput.trim())
      .map((e) => ({ input: e.input.trim(), expectedOutput: e.expectedOutput.trim() })),
    pitfalls: values(form.pitfalls),
    qualityChecks: values(form.qualityChecks),
    resources: form.resources
      .filter((r) => r.fileName.trim())
      .map((r) => ({ kind: r.kind, fileName: r.fileName.trim(), purpose: r.purpose.trim() })),
  }
}

/** Já dá para pedir a prévia? (os obrigatórios da API estão preenchidos) */
export function isComposable(draft: SkillDraft): boolean {
  return Boolean(draft.name && draft.purpose && draft.triggers.length > 0 && draft.steps.length > 0)
}

export type MoldFieldName = Exclude<keyof MoldValues, never>

/** As etapas do Molde e os campos (nomes do SkillDraft) de cada uma. */
export const MOLD_STEPS: { id: string; title: string; fields: MoldFieldName[] }[] = [
  { id: 'essencia', title: 'Essência', fields: ['name', 'purpose'] },
  { id: 'quando', title: 'Quando usar', fields: ['triggers', 'antiTriggers'] },
  { id: 'processo', title: 'Entradas e processo', fields: ['inputs', 'steps'] },
  { id: 'saida', title: 'Saída e exemplos', fields: ['outputFormat', 'examples'] },
  { id: 'verificacao', title: 'Armadilhas e verificação', fields: ['pitfalls', 'qualityChecks'] },
  { id: 'recursos', title: 'Recursos', fields: ['resources'] },
]

/** Cada conselho do mestre (QualityHint.id) aponta para a etapa que o resolve. */
export const HINT_STEP: Record<string, number> = {
  'description-truncated': 0,
  'few-triggers': 1,
  'no-anti-triggers': 1,
  'vague-steps': 2,
  'no-output-format': 3,
  'no-examples': 3,
  'no-quality-checks': 4,
  'long-body': 5,
}

/** Quantos conselhos o mestre pode dar (as QualityRule do backend). */
export const TOTAL_HINTS = Object.keys(HINT_STEP).length
