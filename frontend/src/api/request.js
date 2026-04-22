import axios from 'axios'
import { ElMessage } from 'element-plus'

const getApiUrl = () => {
  // 生产环境：使用环境变量
  if (import.meta.env.PROD) {
    return import.meta.env.VITE_API_BASE_URL || 'https://your-backend-url.fcapp.run'
  }
  
  // 开发环境：使用代理
  return '/api'
}

const API_URL = getApiUrl()

const request = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : `${API_URL}/api`,
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const cache = new Map()
const CACHE_TTL = 30000

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    if (config.method === 'get' && config.cache !== false) {
      const cacheKey = `${config.baseURL}${config.url}?${JSON.stringify(config.params || {})}`
      const cached = cache.get(cacheKey)
      if (cached && Date.now() - cached.time < CACHE_TTL) {
        config.__cached = cached.data
        config.adapter = () => Promise.resolve({
          data: cached.data,
          status: 200,
          statusText: 'OK',
          headers: {},
          config
        })
      }
    }

    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const resData = response.data

    if (resData && typeof resData === 'object' && 'code' in resData && 'data' in resData) {
      if (resData.code !== 200) {
        const errorMsg = resData.message || '请求失败'
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }

      if (response.config.method === 'get' && response.config.cache !== false) {
        const cacheKey = `${response.config.baseURL}${response.config.url}?${JSON.stringify(response.config.params || {})}`
        cache.set(cacheKey, { data: resData, time: Date.now() })
      }

      if (cache.size > 50) {
        const oldestKey = cache.keys().next().value
        cache.delete(oldestKey)
      }

      return resData.data
    }

    if (response.config.method === 'get' && response.config.cache !== false) {
      const cacheKey = `${response.config.baseURL}${response.config.url}?${JSON.stringify(response.config.params || {})}`
      cache.set(cacheKey, { data: resData, time: Date.now() })
    }

    if (cache.size > 50) {
      const oldestKey = cache.keys().next().value
      cache.delete(oldestKey)
    }

    return resData
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      const errorMsg = data?.message || '请求失败'

      if (status === 401) {
        localStorage.removeItem('token')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      } else if (status === 400) {
        ElMessage.warning(errorMsg)
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else {
        ElMessage.error(errorMsg)
      }

      return Promise.reject(data || error)
    }

    ElMessage.error('网络连接异常，请检查网络')
    return Promise.reject(error)
  }
)

export function clearCache() {
  cache.clear()
}

export function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  const baseUrl = import.meta.env.PROD
    ? (import.meta.env.VITE_API_BASE_URL || '')
    : ''
  const cleanPath = url.replace(/^\/+/, '').replace(/^uploads\//, '')
  return baseUrl + '/api/photos/serve/' + cleanPath
}

export function getThumbnailUrl(url) {
  const fullUrl = getImageUrl(url)
  if (!fullUrl) return ''
  if (fullUrl.includes('supabase.co')) {
    return fullUrl + '?width=300&height=300&resize=cover'
  }
  return fullUrl
}

export default request
