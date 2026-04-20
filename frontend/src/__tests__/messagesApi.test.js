import { describe, it, expect, vi } from 'vitest'
import * as messagesApi from '@/api/messages'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn()
  }
}))

import request from '@/api/request'

describe('Messages API 留言接口', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getMessages 获取留言列表', () => {
    it('使用默认分页参数', async () => {
      request.get.mockResolvedValue({ data: [], total: 0, page: 1, hasMore: false })

      await messagesApi.getMessages()
      expect(request.get).toHaveBeenCalledWith('/messages?page=1&limit=10')
    })

    it('传入自定义分页参数', async () => {
      request.get.mockResolvedValue({ data: [], total: 0, page: 2, hasMore: false })

      await messagesApi.getMessages(2, 20)
      expect(request.get).toHaveBeenCalledWith('/messages?page=2&limit=20')
    })
  })

  describe('createMessage 创建留言', () => {
    it('发送POST请求创建留言', async () => {
      const newMsg = { id: '1', content: '测试', author: '用户' }
      request.post.mockResolvedValue(newMsg)

      const result = await messagesApi.createMessage({ content: '测试', author: '用户' })
      expect(request.post).toHaveBeenCalledWith('/messages', { content: '测试', author: '用户' })
    })
  })

  describe('deleteMessage 删除留言', () => {
    it('发送DELETE请求删除留言', async () => {
      request.delete.mockResolvedValue('删除成功')

      await messagesApi.deleteMessage('1')
      expect(request.delete).toHaveBeenCalledWith('/messages/1')
    })
  })
})
