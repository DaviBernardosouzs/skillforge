import { heatColor, isAlive, spawnEmber, spawnSparks, stepEmber, stepSpark } from './particles'

const fixed = () => 0.5

test('faíscas saem para cima e caem com a gravidade', () => {
  const [spark] = spawnSparks(100, 100, 1, fixed)
  expect(spark).toBeDefined()
  if (!spark) return
  expect(spark.vy).toBeLessThan(0)

  const later = stepSpark(stepSpark(spark, 0.5), 0.5)
  expect(later.vy).toBeGreaterThan(spark.vy)
  expect(later.life).toBeLessThan(spark.life)
})

test('faíscas morrem com o tempo', () => {
  const first = spawnSparks(0, 0, 1, fixed)[0]
  if (!first) throw new Error('nenhuma faísca criada')
  let spark = first
  for (let i = 0; i < 60; i++) spark = stepSpark(spark, 1 / 30)
  expect(isAlive(spark)).toBe(false)
})

test('brasas nascem só nas zonas pedidas e sobem', () => {
  const ember = spawnEmber(1000, 800, [[0, 0.1]], fixed)
  expect(ember.x).toBeGreaterThanOrEqual(0)
  expect(ember.x).toBeLessThanOrEqual(100)
  expect(ember.y).toBeGreaterThan(800)
  expect(stepEmber(ember, 1, 0).y).toBeLessThan(ember.y)
})

test('cor esfria conforme a vida acaba', () => {
  expect(heatColor(1)).toBe('251 233 196')
  expect(heatColor(0.1)).toBe('168 50 27')
})
