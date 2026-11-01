import { useState } from 'react'
import { Button } from '../../ui/Button'
import { Dialog } from '../../ui/Dialog'
import { Field, Textarea } from '../../ui/Field'

interface PasteDialogProps {
  open: boolean
  onClose: () => void
  onCreate: (content: string) => void
}

/** Colar um SKILL.md pronto (de outro repositório, de um chat...). */
export function PasteDialog({ open, onClose, onCreate }: PasteDialogProps) {
  const [content, setContent] = useState('')
  return (
    <Dialog open={open} onClose={onClose} label="Matéria-prima · SKILL.md existente" title="Colar SKILL.md">
      <form
        className="flex flex-col gap-4"
        onSubmit={(event) => {
          event.preventDefault()
          if (!content.trim()) return
          onCreate(content)
          setContent('')
          onClose()
        }}
      >
        <Field
          label="Conteúdo do SKILL.md"
          hint="Frontmatter com name e description, seguido do corpo em Markdown."
        >
          {({ id, describedBy }) => (
            <Textarea
              id={id}
              aria-describedby={describedBy}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={14}
              spellCheck={false}
              className="font-mono text-xs"
              placeholder={
                '---\nname: minha-skill\ndescription: O que faz. Use quando...\n---\n\n# Minha skill'
              }
            />
          )}
        </Field>
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={onClose}>
            Cancelar
          </Button>
          <Button variant="forge" type="submit" disabled={!content.trim()}>
            Levar ao estoque
          </Button>
        </div>
      </form>
    </Dialog>
  )
}
