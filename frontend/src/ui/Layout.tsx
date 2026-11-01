import { NavLink, Outlet } from 'react-router-dom'
import { cn } from './cn'
import { Grain } from './Grain'

const links = [
  { to: '/', label: 'A Forja', end: true },
  { to: '/bancada', label: 'Bancada' },
  { to: '/molde', label: 'Molde' },
  { to: '/mapa', label: 'Mapa da liga' },
]

export function Layout() {
  return (
    <div className="flex min-h-[100dvh] flex-col">
      <a
        href="#conteudo"
        className="sr-only z-[70] bg-heat-3 px-3 py-2 text-coal-950 focus:not-sr-only focus:fixed focus:top-2 focus:left-2"
      >
        Pular para o conteúdo
      </a>
      <header className="sticky top-0 z-40 border-b border-iron-700 bg-coal-950/90 backdrop-blur-sm">
        <div className="mx-auto flex h-14 max-w-[1400px] items-center justify-between gap-4 px-4">
          <NavLink to="/" className="flex items-baseline gap-2" aria-label="SkillForge, ir para A Forja">
            <span className="font-display text-2xl leading-none font-black tracking-tight uppercase">
              Skillforge
            </span>
            <span aria-hidden="true" className="h-2 w-2 bg-heat-3" />
          </NavLink>
          <nav aria-label="Principal" className="-mr-2 overflow-x-auto">
            <ul className="flex">
              {links.map((link) => (
                <li key={link.to}>
                  <NavLink
                    to={link.to}
                    end={link.end}
                    className={({ isActive }) =>
                      cn(
                        'flex h-14 items-center px-3 font-mono text-[0.6875rem] tracking-[0.12em] whitespace-nowrap uppercase transition-colors',
                        isActive
                          ? 'text-heat-5 shadow-[inset_0_-2px_0_var(--color-heat-3)]'
                          : 'text-iron-400 hover:text-iron-100',
                      )
                    }
                  >
                    {link.label}
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>
        </div>
      </header>
      <main id="conteudo" className="flex min-h-0 flex-1 flex-col">
        <Outlet />
      </main>
      <Grain />
    </div>
  )
}
