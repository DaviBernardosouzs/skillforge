import { HighlightStyle, syntaxHighlighting } from '@codemirror/language'
import { EditorView } from '@codemirror/view'
import { tags as t } from '@lezer/highlight'

/** Tema do editor no material da forja: carvão, ferro e destaque em brasa. */
export const forgeEditorTheme = EditorView.theme(
  {
    '&': {
      color: 'var(--color-iron-100)',
      backgroundColor: 'var(--color-coal-900)',
      fontSize: '13.5px',
      height: '100%',
    },
    '.cm-scroller': { fontFamily: 'var(--font-mono)', lineHeight: '1.65' },
    '.cm-content': { caretColor: 'var(--color-heat-5)', padding: '12px 0' },
    '.cm-cursor, .cm-dropCursor': { borderLeftColor: 'var(--color-heat-5)', borderLeftWidth: '2px' },
    '&.cm-focused': { outline: 'none' },
    '&.cm-focused .cm-selectionBackground, .cm-selectionBackground, ::selection': {
      backgroundColor: 'rgb(168 50 27 / 0.45) !important',
    },
    '.cm-activeLine': { backgroundColor: 'rgb(255 255 255 / 0.025)' },
    '.cm-gutters': {
      backgroundColor: 'var(--color-coal-950)',
      color: 'var(--color-iron-600)',
      border: 'none',
      borderRight: '1px solid var(--color-iron-700)',
    },
    '.cm-activeLineGutter': { backgroundColor: 'transparent', color: 'var(--color-heat-4)' },
    '.cm-lintRange-error': {
      backgroundImage: 'none',
      textDecoration: 'underline wavy var(--color-slag-400)',
      textUnderlineOffset: '3px',
    },
    '.cm-lintRange-warning': {
      backgroundImage: 'none',
      textDecoration: 'underline dotted var(--color-heat-4)',
      textUnderlineOffset: '3px',
    },
    '.cm-lint-marker-error': { content: 'none' },
    '.cm-tooltip': {
      backgroundColor: 'var(--color-iron-800)',
      border: '1px solid var(--color-iron-600)',
      color: 'var(--color-iron-100)',
    },
    '.cm-diagnostic-error': { borderLeftColor: 'var(--color-slag-400)' },
    '.cm-diagnostic-warning': { borderLeftColor: 'var(--color-heat-4)' },
    '.cm-panels': { backgroundColor: 'var(--color-iron-850)', color: 'var(--color-iron-100)' },
  },
  { dark: true },
)

const highlight = HighlightStyle.define([
  { tag: t.heading, color: 'var(--color-heat-5)', fontWeight: '700' },
  { tag: [t.strong], fontWeight: '700', color: 'var(--color-iron-100)' },
  { tag: [t.emphasis], fontStyle: 'italic' },
  { tag: [t.link, t.url], color: 'var(--color-steel-300)' },
  { tag: [t.monospace], color: 'var(--color-heat-4)' },
  { tag: [t.propertyName, t.definition(t.propertyName)], color: 'var(--color-heat-4)' },
  { tag: [t.string, t.content], color: 'var(--color-iron-100)' },
  { tag: [t.meta, t.processingInstruction, t.contentSeparator], color: 'var(--color-iron-400)' },
  { tag: [t.comment], color: 'var(--color-iron-400)', fontStyle: 'italic' },
  { tag: [t.list, t.quote], color: 'var(--color-iron-300)' },
  { tag: [t.keyword, t.bool, t.number], color: 'var(--color-steel-300)' },
])

export const forgeHighlighting = syntaxHighlighting(highlight)
