import { describe, it, expect, vi } from 'vitest'
import * as authApi from '@/api/auth'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

import request from '@/api/request'

describe('Auth API 认证接口', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('login 登录', () => {
    it('发送POST请求到 /auth/login', async () => {
      const loginResult = { token: 'test-token', user: { id: 1, username: 'yaoyao' } }
      request.post.mockResolvedValue(loginResult)

      const result = await authApi.login('yaoyao', 'yaoyao20230228')

      expect(request.post).toHaveBeenCalledWith('/auth/login', {
        username: 'yaoyao',
        password: 'yaoyao20230228'
      })
      expect(result).toEqual(loginResult)
    })
  })

  describe('getMe 获取用户信息', () => {
    it('发送GET请求到 /auth/me', async () => {
      const meResult = { user: { id: 1, username: 'yaoyao', name: '瑶瑶' } }
      request.get.mockResolvedValue(meResult)

      const result = await authApi.getMe()

      expect(request.get).toHaveBeenCalledWith('/auth/me')
      expect(result).toEqual(meResult)
    })
  })
})
