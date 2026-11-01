import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, ArrowRight } from '@phosphor-icons/react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { FormProvider, useForm, useWatch } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { DraftField } from '../api/types'
import { useDebouncedValue } from '../hooks/useDebouncedValue'
import { useLibraryStore } from '../features/library/store'
import { MoldField } from '../features/mold/fields'
import { MoldPreview } from '../features/mold/MoldPreview'
import {
  EMPTY_MOLD,
  isComposable,
  MOLD_STEPS,
  moldSchema,
  toSkillDraft,
  type MoldValues,
} from '../features/mold/schema'
import { Button } from '../ui/Button'
import { cn } from '../ui/cn'
import { SectionLabel } from '../ui/SectionLabel'

const PREVIEW_DELAY = 500

export default function MoldPage() {
  const navigate = useNavigate()
  const addSkill = useLibraryStore((s) => s.addSkill)
  const [step, setStep] = useState(0)

  const guide = useQuery({ queryKey: ['draft-template'], queryFn: api.draftTemplate, staleTime: Infinity })
  const form = useForm<MoldValues>({
    resolver: zodResolver(moldSchema),
    defaultValues: EMPTY_MOLD,
    mode: 'onTouched',
  })
  const values = useWatch({ control: form.control }) as MoldValues

  const draft = useMemo(() => toSkillDraft({ ...EMPTY_MOLD, ...values }), [values])
  const composable = isComposable(draft)
  const debounced = useDebouncedValue(composable ? JSON.stringify(draft) : null, PREVIEW_DELAY)
  const preview = useQuery({
    queryKey: ['draft', debounced],
    queryFn: () => api.draft(JSON.parse(debounced as string)),
    enabled: debounced !== null,
    placeholderData: keepPreviousData,
    staleTime: Infinity,
  })

  const guides = useMemo(() => new Map((guide.data ?? []).map((f) => [f.name, f])), [guide.data])
  const current = MOLD_STEPS[step]
  const last = MOLD_STEPS.length - 1

  async function next() {
    if (!current) return
    const valid = await form.trigger(current.fields)
    if (valid) setStep((s) => Math.min(s + 1, last))
  }

  function takeToBench() {
    const result = preview.data
    if (!result) return
    addSkill({
      content: result.skillMd,
      files: result.stubFiles.map((f) => ({
        path: f.path,
        content: f.content,
        encoding: f.encoding === 'base64' ? 'base64' : 'utf8',
      })),
    })
    navigate('/bancada')
  }

  return (
    <div className="mx-auto grid w-full max-w-[1400px] gap-8 px-4 py-10 lg:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]">
      <div className="flex flex-col gap-8">
        <header className="flex flex-col gap-3">
          <SectionLabel>Molde · assistente de redação</SectionLabel>
          <h1 className="font-display text-5xl leading-[0.9] font-black uppercase md:text-6xl">
            Molde uma skill nova
          </h1>
          <p className="max-w-[60ch] text-iron-300">
            Responda as perguntas e o molde monta um SKILL.md seguindo boas práticas: description com o que
            faz e quando usar, processo, exemplos e verificação.
          </p>
        </header>

        <nav aria-label="Etapas do molde">
          <ol className="grid grid-cols-3 gap-px bg-iron-700 sm:grid-cols-6">
            {MOLD_STEPS.map((s, index) => (
              <li key={s.id}>
                <button
                  type="button"
                  onClick={() => setStep(index)}
                  aria-current={index === step ? 'step' : undefined}
                  className={cn(
                    'flex h-full w-full flex-col gap-1 px-3 py-2.5 text-left transition-colors',
                    index === step
                      ? 'heat-glow'
                      : index < step
                        ? 'bg-iron-850'
                        : 'bg-coal-900 hover:bg-iron-850',
                  )}
                >
                  <span className={cn('font-mono text-xs', index <= step ? 'text-heat-4' : 'text-iron-400')}>
                    {String(index + 1).padStart(2, '0')}
                  </span>
                  <span className="text-xs leading-tight text-iron-100">{s.title}</span>
                </button>
              </li>
            ))}
          </ol>
        </nav>

        {guide.isLoading && (
          <p role="status" className="label-mono">
            Buscando as perguntas do molde…
          </p>
        )}
        {guide.isError && (
          <div
            role="alert"
            className="flex flex-col items-start gap-3 border border-slag-400/50 bg-slag-900 p-4"
          >
            <p className="text-sm text-iron-100">
              Não foi possível carregar as perguntas: {guide.error.message}
            </p>
            <Button size="sm" onClick={() => void guide.refetch()}>
              Tentar de novo
            </Button>
          </div>
        )}

        {guide.data && current && (
          <FormProvider {...form}>
            <form
              className="flex flex-col gap-8"
              noValidate
              onSubmit={(e) => {
                e.preventDefault()
                void next()
              }}
            >
              <h2 className="font-display text-3xl font-extrabold uppercase">{current.title}</h2>
              {current.fields.map((name) => {
                const fieldGuide: DraftField | undefined = guides.get(name)
                return fieldGuide ? <MoldField key={name} guide={fieldGuide} /> : null
              })}
              <div className="flex justify-between gap-3 border-t border-iron-700 pt-5">
                <Button
                  variant="ghost"
                  icon={<ArrowLeft size={16} />}
                  disabled={step === 0}
                  onClick={() => setStep((s) => Math.max(s - 1, 0))}
                >
                  Voltar
                </Button>
                {step < last && (
                  <Button variant="steel" icon={<ArrowRight size={16} />} onClick={() => void next()}>
                    Próxima etapa
                  </Button>
                )}
              </div>
            </form>
          </FormProvider>
        )}
      </div>

      <MoldPreview
        result={composable ? preview.data : undefined}
        pending={composable && (debounced !== JSON.stringify(draft) || preview.isFetching)}
        error={preview.isError ? preview.error.message : null}
        composable={composable}
        onGoToStep={setStep}
        onTakeToBench={takeToBench}
      />
    </div>
  )
}
