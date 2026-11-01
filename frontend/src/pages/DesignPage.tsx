import { ArrowRight, Plus } from '@phosphor-icons/react'
import { useRef, useState } from 'react'
import { Button } from '../ui/Button'
import { Field, Input, Select, Textarea } from '../ui/Field'
import { HEAT_SCALE } from '../lib/heat'
import { HeatMeter } from '../ui/HeatMeter'
import { Panel } from '../ui/Panel'
import { Seal } from '../ui/Seal'
import { SectionLabel } from '../ui/SectionLabel'
import { SparkBurst, type SparkBurstHandle } from '../ui/SparkBurst'
import { Tabs } from '../ui/Tabs'

const tokens = [
  ['coal-950', 'Fundo'],
  ['coal-900', 'Campo'],
  ['iron-850', 'Superfície'],
  ['iron-800', 'Painel'],
  ['iron-700', 'Borda'],
  ['iron-400', 'Rótulo'],
  ['iron-300', 'Texto secundário'],
  ['iron-100', 'Texto'],
  ['steel-300', 'Aço temperado'],
  ['slag-400', 'Escória (erro)'],
] as const

/** Página interna com todos os componentes do sistema de design. */
export default function DesignPage() {
  const [tab, setTab] = useState<'a' | 'b' | 'c'>('a')
  const [heat, setHeat] = useState(0.6)
  const sparks = useRef<SparkBurstHandle>(null)

  return (
    <div className="mx-auto flex w-full max-w-[1400px] flex-col gap-16 px-4 py-16">
      <header className="flex flex-col gap-3">
        <SectionLabel>Catálogo interno · sistema de design</SectionLabel>
        <h1 className="font-display text-5xl leading-[0.9] font-black uppercase md:text-7xl">
          Ferro, carvão e brasa
        </h1>
        <p className="max-w-[65ch] text-iron-300">
          Calor é estado: quanto mais quente, mais ativa ou pronta a peça. Aço temperado marca o que está
          concluído.
        </p>
      </header>

      <section className="flex flex-col gap-6" aria-labelledby="t-temp">
        <h2 id="t-temp" className="font-display text-3xl font-extrabold uppercase">
          Escala de temperatura
        </h2>
        <div className="grid grid-cols-3 gap-px border border-iron-700 bg-iron-700 md:grid-cols-6">
          {HEAT_SCALE.map((color, i) => (
            <div key={color} className="flex h-28 flex-col justify-end p-3" style={{ background: color }}>
              <span className={i < 2 ? 'font-mono text-xs text-heat-6' : 'font-mono text-xs text-coal-950'}>
                heat-{i + 1}
              </span>
            </div>
          ))}
        </div>
        <div className="grid grid-cols-2 gap-px border border-iron-700 bg-iron-700 sm:grid-cols-5">
          {tokens.map(([token, name]) => (
            <div key={token} className="flex items-center gap-3 bg-coal-900 p-3">
              <span
                className="h-8 w-8 shrink-0 border border-iron-600"
                style={{ background: `var(--color-${token})` }}
              />
              <span className="flex flex-col">
                <span className="text-sm">{name}</span>
                <span className="font-mono text-xs text-iron-400">{token}</span>
              </span>
            </div>
          ))}
        </div>
      </section>

      <section className="grid gap-10 md:grid-cols-[2fr_1fr]" aria-labelledby="t-type">
        <div className="flex flex-col gap-4">
          <h2 id="t-type" className="font-display text-3xl font-extrabold uppercase">
            Tipografia
          </h2>
          <p className="font-display text-6xl leading-[0.9] font-black uppercase">Big Shoulders Display</p>
          <p className="max-w-[65ch] text-base leading-relaxed text-iron-300">
            Geist para texto e formulários: legível em tamanhos pequenos, sem cara de template.
          </p>
          <p className="font-mono text-sm text-heat-5">Geist Mono · revisao-de-codigo/SKILL.md</p>
        </div>
        <Panel label="Painel ativo" active className="justify-end p-4">
          <p className="text-sm text-iron-300">O calor sobe da base da peça em trabalho.</p>
        </Panel>
      </section>

      <section className="flex flex-col gap-6" aria-labelledby="t-btn">
        <h2 id="t-btn" className="font-display text-3xl font-extrabold uppercase">
          Botões e selos
        </h2>
        <div className="flex flex-wrap items-center gap-3">
          <Button variant="forge" size="lg" icon={<ArrowRight size={18} weight="bold" />}>
            Forjar
          </Button>
          <Button variant="forge">Acender a forja</Button>
          <Button variant="steel" icon={<Plus size={16} />}>
            Nova pelo Molde
          </Button>
          <Button variant="quench">Concluído</Button>
          <Button variant="ghost">Cancelar</Button>
          <Button variant="forge" disabled>
            Bloqueado
          </Button>
        </div>
        <div className="flex flex-wrap gap-2">
          <Seal temper="tempered" />
          <Seal temper="hot" />
          <Seal temper="crack" />
          <Seal temper="raw" />
        </div>
      </section>

      <section className="grid gap-10 md:grid-cols-2" aria-labelledby="t-form">
        <div className="flex flex-col gap-5">
          <h2 id="t-form" className="font-display text-3xl font-extrabold uppercase">
            Campos
          </h2>
          <Field label="Nome da biblioteca" hint="kebab-case: vira o nome da pasta.">
            {({ id, describedBy }) => (
              <Input id={id} aria-describedby={describedBy} defaultValue="minhas-skills" />
            )}
          </Field>
          <Field label="Nome com erro" error="Use só minúsculas, números e hífens.">
            {({ id, describedBy, invalid }) => (
              <Input
                id={id}
                aria-describedby={describedBy}
                aria-invalid={invalid}
                defaultValue="Minhas Skills"
              />
            )}
          </Field>
          <Field label="Licença">
            {({ id }) => (
              <Select id={id} defaultValue="MIT">
                <option value="MIT">MIT</option>
                <option value="APACHE_2_0">Apache 2.0</option>
              </Select>
            )}
          </Field>
          <Field label="Propósito">
            {({ id }) => <Textarea id={id} placeholder="O que a skill faz, em uma frase." />}
          </Field>
        </div>
        <div className="flex flex-col gap-5">
          <h2 className="font-display text-3xl font-extrabold uppercase">Abas e medidor</h2>
          <Tabs
            label="Exemplo de abas"
            value={tab}
            onChange={setTab}
            items={[
              { id: 'a', label: 'SKILL.md' },
              { id: 'b', label: 'references/checklist.md' },
              { id: 'c', label: 'scripts/run.sh' },
            ]}
          />
          <HeatMeter value={heat} label="Temperatura da skill" description="Leitura de exemplo" />
          <input
            type="range"
            min={0}
            max={1}
            step={0.05}
            value={heat}
            onChange={(e) => setHeat(Number(e.target.value))}
            aria-label="Ajustar temperatura de exemplo"
            className="accent-[var(--color-heat-3)]"
          />
        </div>
      </section>

      <section className="flex flex-col gap-4" aria-labelledby="t-fx">
        <h2 id="t-fx" className="font-display text-3xl font-extrabold uppercase">
          Faíscas
        </h2>
        <div className="relative h-64 overflow-hidden border border-iron-700 bg-coal-900">
          <SparkBurst ref={sparks} />
          <button
            type="button"
            className="absolute inset-0 flex items-end p-4 text-left"
            onClick={(e) => {
              const rect = e.currentTarget.getBoundingClientRect()
              sparks.current?.burst(e.clientX - rect.left, e.clientY - rect.top)
            }}
          >
            <span className="label-mono">Clique para golpear · some com movimento reduzido</span>
          </button>
        </div>
      </section>
    </div>
  )
}
