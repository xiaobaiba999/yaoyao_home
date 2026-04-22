import request from './request'

const MAX_FILE_SIZE = 50 * 1024 * 1024

export const validateFileSize = (file, maxSize = MAX_FILE_SIZE) => {
  if (!file) {
    return { valid: false, error: '文件不存在' }
  }
  if (file.size > maxSize) {
    const maxMB = (maxSize / 1024 / 1024).toFixed(2)
    const fileMB = (file.size / 1024 / 1024).toFixed(2)
    return { valid: false, error: `文件大小超出限制，最大允许 ${maxMB}MB，当前文件 ${fileMB}MB` }
  }
  return { valid: true, error: null }
}

export const getPhotos = () => request.get('/photos')

export const uploadPhoto = (formData) => {
  return request.post('/photos', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const uploadToGitee = (formData) => {
  return request.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const deletePhoto = (id) => request.delete(`/photos/${id}`)

export const downloadPhoto = async (id) => {
  const baseUrl = import.meta.env.PROD
    ? (import.meta.env.VITE_API_BASE_URL || '')
    : ''
  const url = `${baseUrl}/api/photos/download/${id}`
  try {
    const response = await fetch(url)
    if (!response.ok) throw new Error('下载失败')
    const blob = await response.blob()
    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = 'photo.png'
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="?(.+?)"?$/)
      if (match) {
        filename = decodeURIComponent(match[1])
      }
    }
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(a.href)
  } catch (e) {
    console.error('下载照片失败', e)
    throw e
  }
}
