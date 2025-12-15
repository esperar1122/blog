import { describe, it, expect } from 'vitest'

describe('Health Check', () => {
  it('should pass a simple test', () => {
    expect(1 + 1).toBe(2)
  })

  it('should verify environment variables', () => {
    expect(import.meta.env).toBeDefined()
  })
})