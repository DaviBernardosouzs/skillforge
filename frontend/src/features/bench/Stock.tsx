import { ClipboardText, Plus, Scroll } from '@phosphor-icons/react'
import { AnimatePresence } from 'motion/react'
import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { Violation } from '../../api/types'
import { Button } from '../../ui/Button'
import { Panel } from '../../ui/Panel'
import { useLibraryStore, type RemovedSkill } from '../library/store'
import { temperOf } from '../lint/violations'
import { LibraryMetaForm } from './LibraryMetaForm'
import { PasteDialog } from './PasteDialog'
import { SkillIngot } from './SkillIngot'
import { TemplateDialog } from './TemplateDialog'
import { UndoToast } from './UndoToast'

interface StockProps {
  /** Violações por skill vindas da têmpera da biblioteca; undefined = ainda não inspecionada. */
  violations: Map<string, Violation[]> | undefined
  className?: string
}

/** Estoque: dados da biblioteca e as skills como lingotes. */
export function Stock({ violations, className }: StockProps) {
  const navigate = useNavigate()
  const skills = useLibraryStore((s) => s.draft.skills)
  const selectedId = useLibraryStore((s) => s.selectedId)
  const { select, moveSkill, duplicateSkill, removeSkill, restoreSkill, addSkill } =
    useLibraryStore.getState()
  const [dialog, setDialog] = useState<'template' | 'paste' | null>(null)
  const [removed, setRemoved] = useState<RemovedSkill | null>(null)
  const dismiss = useCallback(() => setRemoved(null), [setRemoved])

  return (
    <Panel label="Estoque · biblioteca e skills" className={className}>
      <div className="flex flex-col gap-6 overflow-y-auto p-4">
        <LibraryMetaForm />

        <div className="flex flex-col gap-3">
          <div className="flex items-baseline justify-between">
            <h3 className="label-mono">Lingotes ({skills.length})</h3>
          </div>
          <div className="grid grid-cols-1 gap-2">
            <Button
              variant="forge"
              size="sm"
              icon={<Plus size={14} weight="bold" />}
              onClick={() => navigate('/molde')}
            >
              Nova pelo Molde
            </Button>
            <div className="grid grid-cols-2 gap-2">
              <Button
                variant="steel"
                size="sm"
                icon={<Scroll size={14} />}
                onClick={() => setDialog('template')}
              >
                Molde pronto
              </Button>
              <Button
                variant="steel"
                size="sm"
                icon={<ClipboardText size={14} />}
                onClick={() => setDialog('paste')}
              >
                Colar SKILL.md
              </Button>
            </div>
          </div>

          {skills.length === 0 ? (
            <p className="border border-dashed border-iron-700 p-4 text-sm leading-relaxed text-iron-400">
              O estoque está vazio. Comece pelo Molde, por um molde pronto ou colando um SKILL.md que você já
              tem.
            </p>
          ) : (
            <ul className="flex flex-col gap-2" aria-label="Skills da biblioteca">
              <AnimatePresence initial={false}>
                {skills.map((skill, index) => (
                  <SkillIngot
                    key={skill.id}
                    skill={skill}
                    index={index}
                    total={skills.length}
                    selected={skill.id === selectedId}
                    temper={temperOf(violations?.get(skill.id))}
                    onSelect={() => select(skill.id)}
                    onMove={(delta) => moveSkill(skill.id, delta)}
                    onDuplicate={() => duplicateSkill(skill.id)}
                    onRemove={() => setRemoved(removeSkill(skill.id))}
                  />
                ))}
              </AnimatePresence>
            </ul>
          )}
        </div>
      </div>

      <TemplateDialog
        open={dialog === 'template'}
        onClose={() => setDialog(null)}
        onCreate={(content) => addSkill({ content })}
      />
      <PasteDialog
        open={dialog === 'paste'}
        onClose={() => setDialog(null)}
        onCreate={(content) => addSkill({ content })}
      />
      <UndoToast
        message={removed ? 'Skill removida do estoque.' : null}
        onUndo={() => {
          if (removed) restoreSkill(removed)
          setRemoved(null)
        }}
        onDismiss={dismiss}
      />
    </Panel>
  )
}
