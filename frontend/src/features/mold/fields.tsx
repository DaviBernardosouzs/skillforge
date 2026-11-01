import { ArrowDown, ArrowUp, Plus, X } from '@phosphor-icons/react'
import type { KeyboardEvent, ReactNode } from 'react'
import { useFieldArray, useFormContext, type FieldErrors } from 'react-hook-form'
import type { DraftField } from '../../api/types'
import { Button } from '../../ui/Button'
import { Field, Input, Select, Textarea } from '../../ui/Field'
import type { MoldValues } from './schema'

type TextListName = 'triggers' | 'antiTriggers' | 'inputs' | 'pitfalls' | 'qualityChecks'

function errorOf(errors: FieldErrors<MoldValues>, name: keyof MoldValues): string | undefined {
  const error = errors[name] as { message?: string; root?: { message?: string } } | undefined
  return error?.message ?? error?.root?.message
}

const iconButton = 'p-2 text-iron-400 hover:text-iron-100 disabled:opacity-30'

/** Cabeçalho comum: rótulo e pergunta-guia vindos da API. */
function ListShell({
  guide,
  error,
  children,
  onAdd,
  addLabel,
}: {
  guide: DraftField
  error?: string
  children: ReactNode
  onAdd: () => void
  addLabel: string
}) {
  return (
    <fieldset className="flex flex-col gap-3">
      <legend className="flex flex-col gap-1">
        <span className="text-sm font-medium text-iron-100">
          {guide.label}
          {guide.required && (
            <span className="ml-1 text-heat-4" aria-hidden="true">
              *
            </span>
          )}
          {guide.required && <span className="sr-only"> (obrigatório)</span>}
        </span>
        <span className="text-sm text-iron-300">{guide.question}</span>
      </legend>
      {guide.hint && <p className="text-xs text-iron-400">{guide.hint}</p>}
      {children}
      {error && (
        <p role="alert" className="text-xs text-slag-400">
          {error}
        </p>
      )}
      <div>
        <Button variant="steel" size="sm" icon={<Plus size={14} />} onClick={onAdd}>
          {addLabel}
        </Button>
      </div>
    </fieldset>
  )
}

export function TextListField({ name, guide }: { name: TextListName; guide: DraftField }) {
  const { control, register, formState } = useFormContext<MoldValues>()
  const { fields, append, remove } = useFieldArray({ control, name })

  function onKeyDown(event: KeyboardEvent<HTMLInputElement>, index: number) {
    if (event.key === 'Enter') {
      event.preventDefault()
      if (index === fields.length - 1) append({ value: '' }, { shouldFocus: true })
    }
  }

  return (
    <ListShell
      guide={guide}
      error={errorOf(formState.errors, name)}
      onAdd={() => append({ value: '' }, { shouldFocus: true })}
      addLabel="Adicionar"
    >
      {fields.length > 0 && (
        <ol className="flex flex-col gap-2">
          {fields.map((item, index) => (
            <li key={item.id} className="flex items-center gap-2">
              <span className="w-6 shrink-0 text-right font-mono text-xs text-iron-400">{index + 1}</span>
              <Input
                aria-label={`${guide.label}, item ${index + 1}`}
                placeholder={index === 0 ? guide.example : undefined}
                onKeyDown={(e) => onKeyDown(e, index)}
                {...register(`${name}.${index}.value`)}
              />
              <button
                type="button"
                className={iconButton}
                onClick={() => remove(index)}
                aria-label={`Remover item ${index + 1} de ${guide.label}`}
              >
                <X size={16} />
              </button>
            </li>
          ))}
        </ol>
      )}
    </ListShell>
  )
}

export function StepsField({ guide }: { guide: DraftField }) {
  const { control, register, formState } = useFormContext<MoldValues>()
  const { fields, append, remove, move } = useFieldArray({ control, name: 'steps' })

  return (
    <ListShell
      guide={guide}
      error={errorOf(formState.errors, 'steps')}
      onAdd={() => append({ title: '', detail: '' }, { shouldFocus: true })}
      addLabel="Adicionar passo"
    >
      <ol className="flex flex-col gap-3">
        {fields.map((item, index) => (
          <li
            key={item.id}
            className="grid gap-2 border border-iron-700 bg-coal-900 p-3 sm:grid-cols-[2rem_1fr_auto]"
          >
            <span className="font-mono text-sm text-heat-4">{String(index + 1).padStart(2, '0')}</span>
            <div className="flex flex-col gap-2">
              <Input
                aria-label={`Título do passo ${index + 1}`}
                placeholder={index === 0 ? 'Leia o diff' : 'Título curto'}
                {...register(`steps.${index}.title`)}
              />
              <Textarea
                aria-label={`Detalhe do passo ${index + 1}`}
                rows={2}
                placeholder={index === 0 ? guide.example : 'O que fazer e por quê'}
                {...register(`steps.${index}.detail`)}
              />
            </div>
            <div className="flex sm:flex-col">
              <button
                type="button"
                className={iconButton}
                disabled={index === 0}
                onClick={() => move(index, index - 1)}
                aria-label={`Subir passo ${index + 1}`}
              >
                <ArrowUp size={16} />
              </button>
              <button
                type="button"
                className={iconButton}
                disabled={index === fields.length - 1}
                onClick={() => move(index, index + 1)}
                aria-label={`Descer passo ${index + 1}`}
              >
                <ArrowDown size={16} />
              </button>
              <button
                type="button"
                className={iconButton}
                onClick={() => remove(index)}
                aria-label={`Remover passo ${index + 1}`}
              >
                <X size={16} />
              </button>
            </div>
          </li>
        ))}
      </ol>
    </ListShell>
  )
}

export function ExamplesField({ guide }: { guide: DraftField }) {
  const { control, register } = useFormContext<MoldValues>()
  const { fields, append, remove } = useFieldArray({ control, name: 'examples' })

  return (
    <ListShell
      guide={guide}
      onAdd={() => append({ input: '', expectedOutput: '' }, { shouldFocus: true })}
      addLabel="Adicionar exemplo"
    >
      <ol className="flex flex-col gap-3">
        {fields.map((item, index) => (
          <li
            key={item.id}
            className="grid gap-2 border border-iron-700 bg-coal-900 p-3 sm:grid-cols-[1fr_1fr_auto]"
          >
            <Textarea
              aria-label={`Entrada do exemplo ${index + 1}`}
              rows={2}
              placeholder="Entrada"
              {...register(`examples.${index}.input`)}
            />
            <Textarea
              aria-label={`Saída esperada do exemplo ${index + 1}`}
              rows={2}
              placeholder="Saída esperada"
              {...register(`examples.${index}.expectedOutput`)}
            />
            <button
              type="button"
              className={iconButton}
              onClick={() => remove(index)}
              aria-label={`Remover exemplo ${index + 1}`}
            >
              <X size={16} />
            </button>
          </li>
        ))}
      </ol>
      {fields.length === 0 && <p className="text-xs text-iron-400">Ex.: {guide.example}</p>}
    </ListShell>
  )
}

export function ResourcesField({ guide }: { guide: DraftField }) {
  const { control, register } = useFormContext<MoldValues>()
  const { fields, append, remove } = useFieldArray({ control, name: 'resources' })

  return (
    <ListShell
      guide={guide}
      onAdd={() => append({ kind: 'REFERENCE', fileName: '', purpose: '' }, { shouldFocus: true })}
      addLabel="Adicionar recurso"
    >
      <ol className="flex flex-col gap-3">
        {fields.map((item, index) => (
          <li
            key={item.id}
            className="grid gap-2 border border-iron-700 bg-coal-900 p-3 sm:grid-cols-[9rem_1fr_1.4fr_auto]"
          >
            <Select aria-label={`Tipo do recurso ${index + 1}`} {...register(`resources.${index}.kind`)}>
              <option value="REFERENCE">references/</option>
              <option value="SCRIPT">scripts/</option>
              <option value="ASSET">assets/</option>
            </Select>
            <Input
              aria-label={`Nome do arquivo ${index + 1}`}
              placeholder="checklist.md"
              className="font-mono"
              {...register(`resources.${index}.fileName`)}
            />
            <Input
              aria-label={`Propósito do recurso ${index + 1}`}
              placeholder="Para que serve"
              {...register(`resources.${index}.purpose`)}
            />
            <button
              type="button"
              className={iconButton}
              onClick={() => remove(index)}
              aria-label={`Remover recurso ${index + 1}`}
            >
              <X size={16} />
            </button>
          </li>
        ))}
      </ol>
      {fields.length === 0 && <p className="text-xs text-iron-400">Ex.: {guide.example}</p>}
    </ListShell>
  )
}

export function SimpleField({
  name,
  guide,
}: {
  name: 'name' | 'purpose' | 'outputFormat'
  guide: DraftField
}) {
  const { register, formState } = useFormContext<MoldValues>()
  const error = errorOf(formState.errors, name)
  const label = (
    <span className="flex flex-col gap-1">
      <span>
        {guide.label}
        {guide.required && (
          <span className="ml-1 text-heat-4" aria-hidden="true">
            *
          </span>
        )}
      </span>
      <span className="font-normal text-iron-300">{guide.question}</span>
    </span>
  )
  return (
    <Field label={label} hint={guide.hint || undefined} error={error}>
      {({ id, describedBy, invalid }) =>
        name === 'name' ? (
          <Input
            id={id}
            aria-describedby={describedBy}
            aria-invalid={invalid}
            aria-required={guide.required}
            placeholder={guide.example}
            className="font-mono"
            autoComplete="off"
            spellCheck={false}
            {...register(name)}
          />
        ) : (
          <Textarea
            id={id}
            aria-describedby={describedBy}
            aria-invalid={invalid}
            aria-required={guide.required}
            placeholder={guide.example}
            rows={3}
            {...register(name)}
          />
        )
      }
    </Field>
  )
}

/** Escolhe o componente de cada campo pelo nome do SkillDraft. */
export function MoldField({ guide }: { guide: DraftField }) {
  switch (guide.name) {
    case 'name':
    case 'purpose':
    case 'outputFormat':
      return <SimpleField name={guide.name} guide={guide} />
    case 'triggers':
    case 'antiTriggers':
    case 'inputs':
    case 'pitfalls':
    case 'qualityChecks':
      return <TextListField name={guide.name} guide={guide} />
    case 'steps':
      return <StepsField guide={guide} />
    case 'examples':
      return <ExamplesField guide={guide} />
    case 'resources':
      return <ResourcesField guide={guide} />
    default:
      return null
  }
}
