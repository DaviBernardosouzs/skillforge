import { X } from '@phosphor-icons/react'
import { useId, useState, type KeyboardEvent } from 'react'
import { readFrontmatterField } from '../../lib/frontmatter'
import { Field, Input } from '../../ui/Field'
import { useLibraryStore } from '../library/store'
import type { SkillEntry } from '../library/types'

interface ChipInputProps {
  label: string
  hint: string
  values: string[]
  suggestions?: string[]
  onChange: (values: string[]) => void
}

/** Lista de valores: Enter ou vírgula adiciona, Backspace no campo vazio remove o último. */
function ChipInput({ label, hint, values, suggestions = [], onChange }: ChipInputProps) {
  const [text, setText] = useState('')
  const listId = useId()

  function add(raw: string) {
    const value = raw.trim().replace(/,$/, '')
    if (value && !values.includes(value)) onChange([...values, value])
    setText('')
  }

  function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault()
      add(text)
    } else if (event.key === 'Backspace' && !text && values.length > 0) {
      onChange(values.slice(0, -1))
    }
  }

  return (
    <Field label={label} hint={hint}>
      {({ id, describedBy }) => (
        <div className="flex flex-col gap-2">
          {values.length > 0 && (
            <ul className="flex flex-wrap gap-1.5" aria-label={`${label} adicionadas`}>
              {values.map((value) => (
                <li
                  key={value}
                  className="flex items-center gap-1 border border-iron-600 bg-iron-800 py-0.5 pr-0.5 pl-2 font-mono text-xs"
                >
                  {value}
                  <button
                    type="button"
                    onClick={() => onChange(values.filter((v) => v !== value))}
                    aria-label={`Remover ${value}`}
                    className="p-0.5 text-iron-400 hover:text-iron-100"
                  >
                    <X size={12} />
                  </button>
                </li>
              ))}
            </ul>
          )}
          <Input
            id={id}
            aria-describedby={describedBy}
            value={text}
            list={suggestions.length > 0 ? listId : undefined}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={onKeyDown}
            onBlur={() => text && add(text)}
            className="font-mono"
          />
          {suggestions.length > 0 && (
            <datalist id={listId}>
              {suggestions
                .filter((s) => !values.includes(s))
                .map((s) => (
                  <option key={s} value={s} />
                ))}
            </datalist>
          )}
        </div>
      )}
    </Field>
  )
}

/** Campos que só existem para o grafo do Obsidian (não vão para o SKILL.md). */
export function GraphFields({ skill }: { skill: SkillEntry }) {
  const skills = useLibraryStore((s) => s.draft.skills)
  const updateSkill = useLibraryStore((s) => s.updateSkill)
  const categoryList = useId()

  const categories = [...new Set(skills.map((s) => s.category.trim()).filter(Boolean))]
  const otherNames = skills
    .filter((s) => s.id !== skill.id)
    .map((s) => readFrontmatterField(s.content, 'name'))
    .filter(Boolean)

  return (
    <div className="grid gap-4 md:grid-cols-3">
      <Field label="Categoria" hint="Agrupa skills no grafo e no canvas.">
        {({ id, describedBy }) => (
          <>
            <Input
              id={id}
              aria-describedby={describedBy}
              list={categoryList}
              value={skill.category}
              onChange={(e) => updateSkill(skill.id, { category: e.target.value })}
            />
            <datalist id={categoryList}>
              {categories.map((c) => (
                <option key={c} value={c} />
              ))}
            </datalist>
          </>
        )}
      </Field>
      <ChipInput
        label="Tags"
        hint="Enter ou vírgula para adicionar."
        values={skill.tags}
        onChange={(tags) => updateSkill(skill.id, { tags })}
      />
      <ChipInput
        label="Relacionadas"
        hint="Outras skills desta biblioteca."
        values={skill.related}
        suggestions={otherNames}
        onChange={(related) => updateSkill(skill.id, { related })}
      />
    </div>
  )
}
