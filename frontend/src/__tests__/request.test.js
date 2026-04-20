import { describe, it, expect, vi, beforeEach } from 'vitest'
import request, { clearCache } from '@/api/request'

describe('Axios 封装 request 模块', () => {
  beforeEach(() => {
    clearCache()
  })

  describe('clearCache 缓存清理', () => {
    it('clearCache 函数可正常调用不报错', () => {
      expect(() => clearCache()).not.toThrow()
    })
  })

  describe('request 实例', () => {
    it('request 对象存在且有 get/post/put/delete 方法', () => {
      expect(request).toBeDefined()
      expect(typeof request.get).toBe('function')
      expect(typeof request.post).toBe('function')
      expect(typeof request.put).toBe('function')
      expect(typeof request.delete).toBe('function')
    })

    it('request 有默认配置', () => {
      expect(request.defaults).toBeDefined()
      expect(request.defaults.timeout).toBe(120000)
    })
  })
})
