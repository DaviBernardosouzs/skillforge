import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

/** Corpo do SKILL.md renderizado (sem o frontmatter). */
export function MarkdownPreview({ content }: { content: string }) {
  const body = content.replace(/\r\n/g, '\n').replace(/^---\n[\s\S]*?\n---[ \t]*(\n|$)/, '')
  return (
    <div
      className={[
        'max-w-none text-sm leading-relaxed text-iron-300',
        '[&_h1]:mb-3 [&_h1]:font-display [&_h1]:text-3xl [&_h1]:font-extrabold [&_h1]:text-iron-100 [&_h1]:uppercase',
        '[&_h2]:mt-6 [&_h2]:mb-2 [&_h2]:font-display [&_h2]:text-xl [&_h2]:font-extrabold [&_h2]:text-iron-100 [&_h2]:uppercase',
        '[&_h3]:mt-4 [&_h3]:mb-1 [&_h3]:font-semibold [&_h3]:text-iron-100',
        '[&_p]:my-2 [&_ul]:my-2 [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:my-2 [&_ol]:list-decimal [&_ol]:pl-5',
        '[&_a]:text-steel-300 [&_a]:underline [&_strong]:text-iron-100',
        '[&_code]:bg-coal-900 [&_code]:px-1 [&_code]:font-mono [&_code]:text-heat-4',
        '[&_pre]:my-3 [&_pre]:overflow-x-auto [&_pre]:border [&_pre]:border-iron-700 [&_pre]:bg-coal-900 [&_pre]:p-3',
        '[&_table]:my-3 [&_td]:border [&_td]:border-iron-700 [&_td]:px-2 [&_th]:border [&_th]:border-iron-700 [&_th]:px-2',
      ].join(' ')}
    >
      {body.trim() ? (
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{body}</ReactMarkdown>
      ) : (
        <p className="text-iron-400">O corpo está vazio.</p>
      )}
    </div>
  )
}
