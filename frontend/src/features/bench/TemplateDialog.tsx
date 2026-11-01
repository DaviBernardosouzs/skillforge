import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { api } from '../../api/client'
import { Button } from '../../ui/Button'
import { cn } from '../../ui/cn'
import { Dialog } from '../../ui/Dialog'
import { Field, Input, Textarea } from '../../ui/Field'
import { useTemplates } from '../lint/useLint'

const schema = z.object({
  name: z
    .string()
    .regex(/^[a-z0-9]+(-[a-z0-9]+)*$/, 'Use kebab-case, ex.: revisao-de-codigo.')
    .max(64, 'Até 64 caracteres.'),
  description: z
    .string()
    .trim()
    .min(10, 'Diga o que a skill faz e quando usar.')
    .max(1024, 'Até 1024 caracteres.'),
})

type Values = z.infer<typeof schema>

interface TemplateDialogProps {
  open: boolean
  onClose: () => void
  onCreate: (content: string) => void
}

/** Moldes prontos: GET /api/templates + POST /api/skills/from-template. */
export function TemplateDialog({ open, onClose, onCreate }: TemplateDialogProps) {
  const templates = useTemplates()
  const [templateId, setTemplateId] = useState<string | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { name: '', description: '' } })

  const chosen = templateId ?? templates.data?.[0]?.id ?? null

  const submit = handleSubmit(async (values) => {
    if (!chosen) return
    setSubmitError(null)
    try {
      const result = await api.fromTemplate({ templateId: chosen, ...values })
      onCreate(result.content)
      reset()
      onClose()
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : 'Não foi possível usar o molde.')
    }
  })

  return (
    <Dialog
      open={open}
      onClose={onClose}
      label="Moldes prontos · templates de SKILL.md"
      title="De um molde pronto"
    >
      <form onSubmit={submit} className="flex flex-col gap-5" noValidate>
        <fieldset className="flex flex-col gap-2">
          <legend className="mb-2 text-sm font-medium">Molde</legend>
          {templates.isLoading && <p className="label-mono">Buscando moldes…</p>}
          {templates.isError && <p className="text-sm text-slag-400">{templates.error.message}</p>}
          <div className="grid gap-px bg-iron-700 sm:grid-cols-2">
            {templates.data?.map((template) => (
              <label
                key={template.id}
                className={cn(
                  'flex cursor-pointer flex-col gap-1 p-3 has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-heat-5',
                  chosen === template.id ? 'heat-glow' : 'bg-coal-900 hover:bg-iron-850',
                )}
              >
                <input
                  type="radio"
                  name="template"
                  value={template.id}
                  checked={chosen === template.id}
                  onChange={() => setTemplateId(template.id)}
                  className="sr-only"
                />
                <span className="font-medium">{template.title}</span>
                <span className="text-xs leading-relaxed text-iron-300">{template.description}</span>
              </label>
            ))}
          </div>
        </fieldset>
        <Field label="Nome da skill" error={errors.name?.message}>
          {({ id, describedBy, invalid }) => (
            <Input
              id={id}
              aria-describedby={describedBy}
              aria-invalid={invalid}
              className="font-mono"
              {...register('name')}
            />
          )}
        </Field>
        <Field
          label="Description"
          hint='O que faz + quando usar. Ex.: "Revisa PRs Java. Use quando o usuário pedir revisão de código."'
          error={errors.description?.message}
        >
          {({ id, describedBy, invalid }) => (
            <Textarea
              id={id}
              aria-describedby={describedBy}
              aria-invalid={invalid}
              rows={3}
              {...register('description')}
            />
          )}
        </Field>
        {submitError && (
          <p role="alert" className="text-sm text-slag-400">
            {submitError}
          </p>
        )}
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose}>
            Cancelar
          </Button>
          <Button variant="forge" type="submit" disabled={isSubmitting || !chosen}>
            {isSubmitting ? 'Moldando…' : 'Usar molde'}
          </Button>
        </div>
      </form>
    </Dialog>
  )
}
