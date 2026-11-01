import { markdown, markdownLanguage } from '@codemirror/lang-markdown'
import { yamlFrontmatter } from '@codemirror/lang-yaml'
import { lintGutter, setDiagnostics, type Diagnostic } from '@codemirror/lint'
import { EditorView } from '@codemirror/view'
import CodeMirror, { type ReactCodeMirrorRef } from '@uiw/react-codemirror'
import { useEffect, useImperativeHandle, useMemo, useRef, type Ref } from 'react'
import { forgeEditorTheme, forgeHighlighting } from './theme'

export interface EditorMark {
  /** Linha 1-based. */
  line: number
  severity: 'error' | 'warning'
  message: string
}

export interface SkillEditorHandle {
  /** Leva o cursor ao início da linha e foca o editor. */
  goToLine: (line: number) => void
}

export type EditorLanguage = 'skill' | 'markdown' | 'plain'

interface SkillEditorProps {
  value: string
  onChange: (value: string) => void
  language?: EditorLanguage
  marks?: EditorMark[]
  ariaLabel: string
  ref?: Ref<SkillEditorHandle>
}

/** CodeMirror 6 com Markdown e YAML no frontmatter. As trincas da têmpera viram sublinhados na linha. */
export default function SkillEditor({
  value,
  onChange,
  language = 'skill',
  marks = [],
  ariaLabel,
  ref,
}: SkillEditorProps) {
  const cm = useRef<ReactCodeMirrorRef>(null)

  const extensions = useMemo(() => {
    const lang =
      language === 'skill'
        ? [yamlFrontmatter({ content: markdown({ base: markdownLanguage }) })]
        : language === 'markdown'
          ? [markdown({ base: markdownLanguage })]
          : []
    return [
      ...lang,
      forgeHighlighting,
      lintGutter(),
      EditorView.lineWrapping,
      EditorView.contentAttributes.of({ 'aria-label': ariaLabel }),
    ]
  }, [language, ariaLabel])

  useEffect(() => {
    const view = cm.current?.view
    if (!view) return
    const doc = view.state.doc
    const diagnostics: Diagnostic[] = marks
      .filter((mark) => mark.line >= 1)
      .map((mark) => {
        const line = doc.line(Math.min(mark.line, doc.lines))
        return {
          from: line.from,
          to: Math.max(line.to, line.from),
          severity: mark.severity,
          message: mark.message,
        }
      })
    view.dispatch(setDiagnostics(view.state, diagnostics))
  }, [marks, value])

  useImperativeHandle(ref, () => ({
    goToLine(lineNumber) {
      const view = cm.current?.view
      if (!view) return
      const line = view.state.doc.line(Math.min(Math.max(lineNumber, 1), view.state.doc.lines))
      view.dispatch({ selection: { anchor: line.from }, scrollIntoView: true })
      view.focus()
    },
  }))

  return (
    <CodeMirror
      ref={cm}
      value={value}
      onChange={onChange}
      theme={forgeEditorTheme}
      extensions={extensions}
      height="100%"
      className="h-full"
      basicSetup={{ foldGutter: false, highlightActiveLine: true, autocompletion: false, searchKeymap: true }}
    />
  )
}
