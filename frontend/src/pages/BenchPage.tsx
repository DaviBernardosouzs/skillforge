import { Export } from '@phosphor-icons/react'
import { useMemo, useRef, useState } from 'react'
import { generateLibrary, saveBlob } from '../api/client'
import type { Violation } from '../api/types'
import { Anvil } from '../features/bench/Anvil'
import { BenchFooter } from '../features/bench/BenchFooter'
import { Stock } from '../features/bench/Stock'
import { TemperPanel } from '../features/bench/TemperPanel'
import type { EditorMark, SkillEditorHandle } from '../features/editor/SkillEditor'
import { downloadDraft } from '../features/library/draftFile'
import { toLibraryRequest } from '../features/library/request'
import { useLibraryStore } from '../features/library/store'
import { useLibraryLint, useSkillLint } from '../features/lint/useLint'
import { locateViolation, violationsBySkill } from '../features/lint/violations'
import { Button } from '../ui/Button'
import { Tabs } from '../ui/Tabs'

/** Regras que só a têmpera da biblioteca inteira enxerga (dependem das outras skills). */
const LIBRARY_RULES = new Set(['name-unique', 'related-exists'])

type Zone = 'stock' | 'anvil' | 'temper'

export default function BenchPage() {
  const draft = useLibraryStore((s) => s.draft)
  const selectedId = useLibraryStore((s) => s.selectedId)
  const skill = draft.skills.find((s) => s.id === selectedId) ?? draft.skills[0]
  const editor = useRef<SkillEditorHandle>(null)
  const [zone, setZone] = useState<Zone>('anvil')

  const skillLint = useSkillLint(skill)
  const libraryLint = useLibraryLint(draft)

  const byskill = useMemo(
    () => (libraryLint.data ? violationsBySkill(draft.skills, libraryLint.data.violations) : undefined),
    [libraryLint.data, draft.skills],
  )

  const violations: Violation[] | undefined = useMemo(() => {
    if (!skill || !skillLint.data) return undefined
    const fromLibrary = (byskill?.get(skill.id) ?? []).filter((v) => LIBRARY_RULES.has(v.ruleId))
    return [...skillLint.data.violations, ...fromLibrary]
  }, [skill, skillLint.data, byskill])

  const marks: EditorMark[] = useMemo(
    () =>
      skill && violations
        ? violations.map((v) => ({
            line: locateViolation(skill.content, v),
            severity: v.severity === 'ERROR' ? 'error' : 'warning',
            message: `${v.ruleId}: ${v.message}`,
          }))
        : [],
    [skill, violations],
  )

  const errors = libraryLint.data?.errorCount ?? 0
  const warnings = libraryLint.data?.warningCount ?? 0
  const blockReason =
    libraryLint.blocker ??
    (libraryLint.isPending && !libraryLint.data
      ? 'Aguarde a têmpera da biblioteca.'
      : errors > 0
        ? `Há ${errors} ${errors === 1 ? 'trinca' : 'trincas'}. Corrija antes de forjar.`
        : libraryLint.isError
          ? 'A têmpera da biblioteca falhou. Confira a conexão com a forja.'
          : null)

  async function forge() {
    const result = await generateLibrary(toLibraryRequest(draft))
    saveBlob(result.blob, result.fileName)
  }

  function locate(violation: Violation) {
    if (!skill) return
    setZone('anvil')
    requestAnimationFrame(() => editor.current?.goToLine(locateViolation(skill.content, violation)))
  }

  const zoneClass = (z: Zone) => (zone === z ? 'flex' : 'hidden lg:flex')

  return (
    <div className="flex flex-1 flex-col">
      <div className="mx-auto flex w-full max-w-[1600px] items-center justify-between gap-4 px-4 pt-6 pb-4">
        <div className="flex flex-col gap-1">
          <span className="label-mono">Bancada · editor da biblioteca</span>
          <h1 className="font-display text-4xl leading-none font-black uppercase">
            {draft.name || 'Biblioteca sem nome'}
          </h1>
        </div>
        <Button variant="ghost" size="sm" icon={<Export size={14} />} onClick={() => downloadDraft(draft)}>
          Exportar rascunho (.json)
        </Button>
      </div>

      <Tabs
        label="Zonas da bancada"
        value={zone}
        onChange={setZone}
        className="mx-4 mb-3 lg:hidden"
        items={[
          { id: 'stock', label: 'Estoque' },
          { id: 'anvil', label: 'Bigorna' },
          { id: 'temper', label: 'Têmpera' },
        ]}
      />

      <div className="mx-auto grid w-full max-w-[1600px] flex-1 gap-3 px-4 pb-6 lg:grid-cols-[300px_minmax(0,1fr)_340px]">
        <Stock violations={byskill} className={`${zoneClass('stock')} lg:max-h-[calc(100dvh-14rem)]`} />
        <Anvil skill={skill} marks={marks} editorRef={editor} className={zoneClass('anvil')} />
        <TemperPanel
          content={skill?.content}
          violations={violations}
          inspecting={skillLint.isPending}
          error={skillLint.isError ? skillLint.error.message : null}
          inspectionKey={skillLint.dataUpdatedAt}
          onLocate={locate}
          className={`${zoneClass('temper')} lg:max-h-[calc(100dvh-14rem)]`}
        />
      </div>

      <BenchFooter
        errors={errors}
        warnings={warnings}
        inspected={Boolean(libraryLint.data) && !libraryLint.blocker}
        inspecting={libraryLint.isPending}
        blockReason={blockReason}
        onForge={() => void forge()}
      />
    </div>
  )
}
