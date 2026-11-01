import { ArrowDown, ArrowUp, CopySimple, Trash } from '@phosphor-icons/react'
import { motion } from 'motion/react'
import { useReducedMotion } from '../../hooks/useReducedMotion'
import { readFrontmatterField } from '../../lib/frontmatter'
import { cn } from '../../ui/cn'
import { Seal, type Temper } from '../../ui/Seal'
import type { SkillEntry } from '../library/types'

interface SkillIngotProps {
  skill: SkillEntry
  index: number
  total: number
  selected: boolean
  temper: Temper
  onSelect: () => void
  onMove: (delta: -1 | 1) => void
  onDuplicate: () => void
  onRemove: () => void
}

/** Uma skill no estoque, como um lingote: nome, categoria e selo de têmpera. */
export function SkillIngot({
  skill,
  index,
  total,
  selected,
  temper,
  onSelect,
  onMove,
  onDuplicate,
  onRemove,
}: SkillIngotProps) {
  const reduced = useReducedMotion()
  const name = readFrontmatterField(skill.content, 'name') || '(sem nome)'
  const action = 'p-1.5 text-iron-400 hover:text-iron-100 disabled:opacity-30 disabled:hover:text-iron-400'

  return (
    <motion.li
      layout={!reduced}
      initial={{ opacity: 0, y: reduced ? 0 : -6 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, x: reduced ? 0 : -16 }}
      transition={{ type: 'spring', stiffness: 260, damping: 28 }}
      className={cn(
        'group relative overflow-hidden border',
        selected ? 'heat-glow border-heat-3/60' : 'border-iron-700 bg-coal-900 hover:border-iron-600',
      )}
    >
      {/* A peça nasce incandescente e esfria até a cor de repouso */}
      <motion.span
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 bg-[linear-gradient(90deg,var(--color-heat-5),var(--color-heat-3))]"
        initial={{ opacity: 0.85 }}
        animate={{ opacity: 0 }}
        transition={{ duration: 1.4, ease: 'easeOut' }}
      />
      <button
        type="button"
        onClick={onSelect}
        aria-current={selected ? 'true' : undefined}
        className="relative flex w-full flex-col gap-2 px-3 pt-3 pb-2 text-left"
      >
        <span className="flex items-start justify-between gap-2">
          <span className="min-w-0 truncate font-mono text-sm text-iron-100">{name}</span>
          <Seal temper={temper} className="shrink-0" />
        </span>
        <span className="text-xs text-iron-400">{skill.category || 'sem categoria'}</span>
      </button>
      <div className="relative flex justify-end gap-0.5 px-1.5 pb-1.5">
        <button
          type="button"
          className={action}
          disabled={index === 0}
          onClick={() => onMove(-1)}
          aria-label={`Subir ${name}`}
        >
          <ArrowUp size={14} />
        </button>
        <button
          type="button"
          className={action}
          disabled={index === total - 1}
          onClick={() => onMove(1)}
          aria-label={`Descer ${name}`}
        >
          <ArrowDown size={14} />
        </button>
        <button type="button" className={action} onClick={onDuplicate} aria-label={`Duplicar ${name}`}>
          <CopySimple size={14} />
        </button>
        <button
          type="button"
          className={cn(action, 'hover:text-slag-400')}
          onClick={onRemove}
          aria-label={`Remover ${name}`}
        >
          <Trash size={14} />
        </button>
      </div>
    </motion.li>
  )
}
