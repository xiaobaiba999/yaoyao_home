import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useAuthStore } from '@/stores/auth'
import { createPinia, setActivePinia } from 'pinia'
import * as authApi from '@/api/auth'

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  getMe: vi.fn()
}))

describe('Auth Store 认证状态管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('初始状态', () => {
    it('初始状态未认证', () => {
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(false)
      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
    })

    it('localStorage有token时从本地恢复', () => {
      localStorage.setItem('token', 'saved-token')
      const store = useAuthStore()
      expect(store.token).toBe('saved-token')
      expect(store.isAuthenticated).toBe(true)
    })
  })

  describe('loginAction 登录', () => {
    it('登录成功存储token和用户信息', async () => {
      authApi.login.mockResolvedValue({
        token: 'test-token',
        user: { id: 1, username: 'yaoyao', name: '瑶瑶' }
      })

      const store = useAuthStore()
      const result = await store.loginAction('yaoyao', 'yaoyao20230228')

      expect(result.success).toBe(true)
      expect(store.token).toBe('test-token')
      expect(store.user.username).toBe('yaoyao')
      expect(localStorage.getItem('token')).toBe('test-token')
    })

    it('登录失败不存储信息', async () => {
      authApi.login.mockRejectedValue(new Error('账号或密码错误'))

      const store = useAuthStore()
      const result = await store.loginAction('wrong', 'wrong')

      expect(result.success).toBe(false)
      expect(store.token).toBeNull()
      expect(store.isAuthenticated).toBe(false)
    })
  })

  describe('checkAuth 验证认证', () => {
    it('无token时返回false', async () => {
      const store = useAuthStore()
      const result = await store.checkAuth()
      expect(result).toBe(false)
    })

    it('有效token时获取用户信息返回true', async () => {
      localStorage.setItem('token', 'valid-token')
      const store = useAuthStore()

      authApi.getMe.mockResolvedValue({
        user: { id: 1, username: 'yaoyao', name: '瑶瑶' }
      })

      const result = await store.checkAuth()
      expect(result).toBe(true)
      expect(store.user.username).toBe('yaoyao')
    })

    it('无效token时清除认证状态返回false', async () => {
      localStorage.setItem('token', 'invalid-token')
      const store = useAuthStore()

      authApi.getMe.mockRejectedValue(new Error('未授权'))

      const result = await store.checkAuth()
      expect(result).toBe(false)
      expect(store.token).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  describe('logout 登出', () => {
    it('登出清除所有认证信息', async () => {
      authApi.login.mockResolvedValue({
        token: 'test-token',
        user: { id: 1, username: 'yaoyao', name: '瑶瑶' }
      })

      const store = useAuthStore()
      await store.loginAction('yaoyao', 'yaoyao20230228')
      expect(store.isAuthenticated).toBe(true)

      store.logout()
      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
    })
  })
})
