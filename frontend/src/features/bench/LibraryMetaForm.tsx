import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import type { License } from '../../api/types'
import { Field, Input, Select } from '../../ui/Field'
import { useLibraryStore } from '../library/store'

const LICENSES: { value: License; label: string }[] = [
  { value: 'MIT', label: 'MIT' },
  { value: 'APACHE_2_0', label: 'Apache 2.0' },
  { value: 'CC_BY_4_0', label: 'CC BY 4.0' },
  { value: 'NONE', label: 'Sem licença' },
]

const schema = z.object({
  name: z
    .string()
    .min(1, 'Dê um nome à biblioteca.')
    .max(64, 'Até 64 caracteres.')
    .regex(
      /^[a-z0-9]+(-[a-z0-9]+)*$/,
      'Use kebab-case: minúsculas, números e hífens simples (ex.: minhas-skills).',
    ),
  author: z.string().trim().min(1, 'Informe o autor: vai para o README, a licença e o commit.').max(100),
  license: z.enum(['MIT', 'APACHE_2_0', 'CC_BY_4_0', 'NONE']),
})

type Meta = z.infer<typeof schema>

/** Nome, autor e licença, validados enquanto a pessoa digita e salvos no rascunho. */
export function LibraryMetaForm() {
  const draft = useLibraryStore((s) => s.draft)
  const setMeta = useLibraryStore((s) => s.setMeta)
  const {
    register,
    control,
    trigger,
    formState: { errors },
  } = useForm<Meta>({
    resolver: zodResolver(schema),
    mode: 'onChange',
    defaultValues: { name: draft.name, author: draft.author, license: draft.license },
  })

  const values = useWatch({ control })

  // Mostra os erros já ao abrir (ex.: rascunho importado com nome inválido)
  useEffect(() => {
    void trigger()
  }, [trigger])

  // Salva no rascunho mesmo com erro: a pessoa vê o problema e o trabalho não se perde
  useEffect(() => {
    setMeta(values)
  }, [values, setMeta])

  return (
    <form className="flex flex-col gap-4" onSubmit={(e) => e.preventDefault()} noValidate>
      <Field
        label="Nome da biblioteca"
        hint="kebab-case: vira o nome da pasta e do .zip."
        error={errors.name?.message}
      >
        {({ id, describedBy, invalid }) => (
          <Input
            id={id}
            aria-describedby={describedBy}
            aria-invalid={invalid}
            autoComplete="off"
            spellCheck={false}
            className="font-mono"
            {...register('name')}
          />
        )}
      </Field>
      <Field label="Autor" error={errors.author?.message}>
        {({ id, describedBy, invalid }) => (
          <Input
            id={id}
            aria-describedby={describedBy}
            aria-invalid={invalid}
            autoComplete="name"
            {...register('author')}
          />
        )}
      </Field>
      <Field label="Licença">
        {({ id }) => (
          <Select id={id} {...register('license')}>
            {LICENSES.map((l) => (
              <option key={l.value} value={l.value}>
                {l.label}
              </option>
            ))}
          </Select>
        )}
      </Field>
    </form>
  )
}
