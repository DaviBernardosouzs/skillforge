import { base64Size, readUpload, toBase64 } from './files'

test('texto UTF-8 continua texto', async () => {
  const file = new File(['# Título com acento'], 'a.md', { type: 'text/markdown' })
  expect(await readUpload(file)).toEqual({ content: '# Título com acento', encoding: 'utf8' })
})

test('binário vira base64', async () => {
  const file = new File([new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x00, 0xff])], 'logo.png', {
    type: 'image/png',
  })
  const result = await readUpload(file)
  expect(result.encoding).toBe('base64')
  expect(base64Size(result.content)).toBe(6)
  expect(result.content).toBe(toBase64(new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x00, 0xff])))
})
