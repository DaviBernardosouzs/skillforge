import { FileArrowUp, Plus, Trash } from '@phosphor-icons/react'
import { lazy, Suspense, useRef, useState, type ChangeEvent, type Ref } from 'react'
import { base64Size, readUpload } from '../../lib/files'
import { readFrontmatterField } from '../../lib/frontmatter'
import { Button } from '../../ui/Button'
import { Field, Input, Select } from '../../ui/Field'
import { Panel } from '../../ui/Panel'
import { Tabs } from '../../ui/Tabs'
import type { EditorMark, SkillEditorHandle } from '../editor/SkillEditor'
import { useLibraryStore } from '../library/store'
import type { SkillEntry } from '../library/types'
import { GraphFields } from './GraphFields'

// CodeMirror é pesado: carrega sob demanda, só quando a Bigorna aparece
const SkillEditor = lazy(() => import('../editor/SkillEditor'))

const SKILL_TAB = 'skill'
const FOLDERS = ['references', 'scripts', 'assets'] as const

interface AnvilProps {
  skill: SkillEntry | undefined
  marks: EditorMark[]
  editorRef: Ref<SkillEditorHandle>
  className?: string
}

function EditorFallback() {
  return (
    <div className="flex h-full items-center justify-center bg-coal-900" role="status">
      <span className="label-mono">Aquecendo o editor…</span>
    </div>
  )
}

/** Bigorna: o SKILL.md selecionado, seus arquivos e os campos do grafo. */
export function Anvil({ skill, marks, editorRef, className }: AnvilProps) {
  const { updateSkill, addFile, updateFile, removeFile } = useLibraryStore.getState()
  const [tab, setTab] = useState<string>(SKILL_TAB)
  const [newFolder, setNewFolder] = useState<(typeof FOLDERS)[number]>('references')
  const [newName, setNewName] = useState('')
  const upload = useRef<HTMLInputElement>(null)

  if (!skill) {
    return (
      <Panel label="Bigorna · editor do SKILL.md" className={className}>
        <div className="flex flex-1 flex-col items-start justify-center gap-3 p-8">
          <p className="font-display text-4xl font-extrabold uppercase">Bigorna livre</p>
          <p className="max-w-[48ch] text-iron-300">
            Escolha uma skill no estoque ou crie uma nova. Aqui você edita o SKILL.md e os arquivos de apoio.
          </p>
        </div>
      </Panel>
    )
  }

  const file = skill.files.find((f) => f.id === tab)
  const activeTab = file ? tab : SKILL_TAB
  const name = readFrontmatterField(skill.content, 'name') || '(sem nome)'

  function createFile() {
    const fileName = newName.trim()
    if (!fileName || !skill) return
    const id = addFile(skill.id, { path: `${newFolder}/${fileName}`, content: '', encoding: 'utf8' })
    setNewName('')
    setTab(id)
  }

  async function onUpload(event: ChangeEvent<HTMLInputElement>) {
    const uploaded = event.target.files?.[0]
    event.target.value = ''
    if (!uploaded || !skill) return
    const { content, encoding } = await readUpload(uploaded)
    const folder = encoding === 'base64' ? 'assets' : newFolder
    setTab(addFile(skill.id, { path: `${folder}/${uploaded.name}`, content, encoding }))
  }

  return (
    <Panel label={`Bigorna · ${name}`} active className={className}>
      <Tabs
        label="Arquivos da skill"
        value={activeTab}
        onChange={setTab}
        className="border-b border-iron-700"
        items={[
          { id: SKILL_TAB, label: 'SKILL.md' },
          ...skill.files.map((f) => ({ id: f.id, label: f.path || 'sem caminho' })),
        ]}
      />

      <div className="min-h-[340px] flex-1 overflow-hidden">
        {!file && (
          <Suspense fallback={<EditorFallback />}>
            <SkillEditor
              ref={editorRef}
              value={skill.content}
              onChange={(content) => updateSkill(skill.id, { content })}
              marks={marks}
              ariaLabel={`SKILL.md de ${name}`}
            />
          </Suspense>
        )}
        {file && file.encoding === 'utf8' && (
          <Suspense fallback={<EditorFallback />}>
            <SkillEditor
              value={file.content}
              onChange={(content) => updateFile(skill.id, file.id, { content })}
              language={file.path.endsWith('.md') ? 'markdown' : 'plain'}
              ariaLabel={`Conteúdo de ${file.path}`}
            />
          </Suspense>
        )}
        {file && file.encoding === 'base64' && (
          <div className="flex h-full flex-col justify-center gap-2 bg-coal-900 p-8">
            <span className="label-mono">Arquivo binário</span>
            <p className="text-iron-300">
              {file.path} · {base64Size(file.content).toLocaleString('pt-BR')} bytes, enviado em base64.
            </p>
          </div>
        )}
      </div>

      <div className="flex flex-col gap-4 border-t border-iron-700 p-4">
        {file ? (
          <div className="flex flex-wrap items-end gap-3">
            <Field
              label="Caminho do arquivo"
              hint="Dentro de references/, scripts/ ou assets/."
              className="min-w-56 flex-1"
            >
              {({ id, describedBy }) => (
                <Input
                  id={id}
                  aria-describedby={describedBy}
                  value={file.path}
                  onChange={(e) => updateFile(skill.id, file.id, { path: e.target.value })}
                  className="font-mono"
                  spellCheck={false}
                />
              )}
            </Field>
            <Button
              variant="ghost"
              icon={<Trash size={16} />}
              onClick={() => {
                removeFile(skill.id, file.id)
                setTab(SKILL_TAB)
              }}
            >
              Remover arquivo
            </Button>
          </div>
        ) : (
          <div className="flex flex-wrap items-end gap-3">
            <Field label="Pasta" className="w-36">
              {({ id }) => (
                <Select
                  id={id}
                  value={newFolder}
                  onChange={(e) => setNewFolder(e.target.value as (typeof FOLDERS)[number])}
                >
                  {FOLDERS.map((f) => (
                    <option key={f} value={f}>
                      {f}/
                    </option>
                  ))}
                </Select>
              )}
            </Field>
            <Field label="Novo arquivo" className="min-w-40 flex-1">
              {({ id }) => (
                <Input
                  id={id}
                  value={newName}
                  placeholder="checklist.md"
                  onChange={(e) => setNewName(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      createFile()
                    }
                  }}
                  className="font-mono"
                />
              )}
            </Field>
            <Button variant="steel" icon={<Plus size={16} />} onClick={createFile} disabled={!newName.trim()}>
              Criar
            </Button>
            <Button variant="steel" icon={<FileArrowUp size={16} />} onClick={() => upload.current?.click()}>
              Enviar arquivo
            </Button>
            <input
              ref={upload}
              type="file"
              className="sr-only"
              tabIndex={-1}
              aria-label="Enviar arquivo"
              onChange={onUpload}
            />
          </div>
        )}

        <details className="group border-t border-iron-700 pt-3" open>
          <summary className="label-mono cursor-pointer list-none py-1 hover:text-iron-100">
            Liga · campos do grafo do Obsidian
          </summary>
          <div className="pt-3">
            <GraphFields skill={skill} />
          </div>
        </details>
      </div>
    </Panel>
  )
}
