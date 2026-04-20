import { describe, it, expect, vi } from 'vitest'
import * as photosApi from '@/api/photos'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn()
  }
}))

import request from '@/api/request'

describe('Photos API 照片接口', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('validateFileSize 文件大小校验', () => {
    it('空文件校验失败', () => {
      const result = photosApi.validateFileSize(null)
      expect(result.valid).toBe(false)
    })

    it('undefined文件校验失败', () => {
      const result = photosApi.validateFileSize(undefined)
      expect(result.valid).toBe(false)
    })

    it('超过50MB的文件校验失败', () => {
      const bigFile = { size: 51 * 1024 * 1024 }
      const result = photosApi.validateFileSize(bigFile)
      expect(result.valid).toBe(false)
      expect(result.error).toContain('50')
    })

    it('50MB以内的文件校验通过', () => {
      const normalFile = { size: 10 * 1024 * 1024 }
      const result = photosApi.validateFileSize(normalFile)
      expect(result.valid).toBe(true)
      expect(result.error).toBeNull()
    })

    it('0字节文件校验通过（大小检查只检查上限）', () => {
      const emptyFile = { size: 0 }
      const result = photosApi.validateFileSize(emptyFile)
      expect(result.valid).toBe(true)
    })

    it('自定义最大大小', () => {
      const file = { size: 6 * 1024 * 1024 }
      const result = photosApi.validateFileSize(file, 5 * 1024 * 1024)
      expect(result.valid).toBe(false)
    })
  })

  describe('getPhotos 获取照片列表', () => {
    it('调用 GET /photos', async () => {
      request.get.mockResolvedValue([{ id: '1', url: 'test.jpg' }])

      const result = await photosApi.getPhotos()
      expect(request.get).toHaveBeenCalledWith('/photos')
      expect(result).toEqual([{ id: '1', url: 'test.jpg' }])
    })
  })

  describe('uploadPhoto 上传照片', () => {
    it('调用 POST /photos 并传递 FormData', async () => {
      const mockPhoto = { id: '1', url: 'test.jpg' }
      request.post.mockResolvedValue(mockPhoto)

      const formData = new FormData()
      formData.append('photo', new Blob(['test'], { type: 'image/jpeg' }), 'test.jpg')

      const result = await photosApi.uploadPhoto(formData)
      expect(request.post).toHaveBeenCalledWith('/photos', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    })
  })

  describe('uploadToGitee 通用Gitee上传', () => {
    it('调用 POST /upload 并传递 FormData', async () => {
      request.post.mockResolvedValue({ url: 'https://gitee.com/test.jpg', filename: 'test.jpg' })

      const formData = new FormData()
      formData.append('file', new Blob(['test'], { type: 'image/jpeg' }), 'test.jpg')

      const result = await photosApi.uploadToGitee(formData)
      expect(request.post).toHaveBeenCalledWith('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    })
  })

  describe('deletePhoto 删除照片', () => {
    it('调用 DELETE /photos/:id', async () => {
      request.delete.mockResolvedValue('删除成功')

      await photosApi.deletePhoto('1')
      expect(request.delete).toHaveBeenCalledWith('/photos/1')
    })
  })
})
