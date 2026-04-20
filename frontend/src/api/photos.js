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
