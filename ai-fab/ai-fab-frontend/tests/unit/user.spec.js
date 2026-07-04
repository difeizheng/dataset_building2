import { describe, it, expect } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('initializes with empty token', () => {
    const store = useUserStore()
    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })

  it('can logout', () => {
    const store = useUserStore()
    store.token = 'test-token'
    store.userInfo = { username: 'test' }
    store.logout()
    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })

  it('checks permissions', () => {
    const store = useUserStore()
    store.userInfo = { roles: ['admin'], permissions: ['read', 'write'] }
    expect(store.hasPermission('read')).toBe(true)
    expect(store.hasPermission('delete')).toBe(true) // admin has all
  })
})
