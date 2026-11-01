import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { lazy, Suspense, type ReactNode } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { ErrorBoundary } from './ui/ErrorBoundary'
import { Layout } from './ui/Layout'
import { PageFallback } from './ui/PageFallback'

const ForgePage = lazy(() => import('./pages/ForgePage'))
const BenchPage = lazy(() => import('./pages/BenchPage'))
const DesignPage = lazy(() => import('./pages/DesignPage'))

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
})

function Screen({ area, children }: { area: string; children: ReactNode }) {
  return (
    <ErrorBoundary area={area}>
      <Suspense fallback={<PageFallback />}>{children}</Suspense>
    </ErrorBoundary>
  )
}

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route
          index
          element={
            <Screen area="A Forja">
              <ForgePage />
            </Screen>
          }
        />
        <Route
          path="bancada"
          element={
            <Screen area="a Bancada">
              <BenchPage />
            </Screen>
          }
        />
        <Route
          path="design"
          element={
            <Screen area="o catálogo de design">
              <DesignPage />
            </Screen>
          }
        />
      </Route>
    </Routes>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </QueryClientProvider>
  )
}
