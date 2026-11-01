import '@fontsource-variable/geist'
import '@fontsource-variable/geist-mono'
import '@fontsource/big-shoulders-display/800'
import '@fontsource/big-shoulders-display/900'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './index.css'

const root = document.getElementById('root')
if (!root) {
  throw new Error('Elemento #root não encontrado')
}

createRoot(root).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
