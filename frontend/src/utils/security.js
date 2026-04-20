export function sanitizeInput(input) {
  if (typeof input !== 'string') return input
  return input
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;')
}

export function maskUserInfo(info) {
  if (!info) return info
  
  if (info.openid) {
    info.openid = info.openid.substring(0, 8) + '****'
  }
  
  if (info.phone) {
    info.phone = info.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  }
  
  return info
}

export function generateSecureToken() {
  const array = new Uint8Array(16)
  crypto.getRandomValues(array)
  return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('')
}

export function verifyOrigin(origin) {
  const allowedOrigins = [
    window.location.origin,
    'https://open.weixin.qq.com'
  ]
  return allowedOrigins.includes(origin)
}

export function safePostMessage(target, message, targetOrigin = '*') {
  if (target && typeof target.postMessage === 'function') {
    const safeMessage = JSON.parse(JSON.stringify(message))
    target.postMessage(safeMessage, targetOrigin)
  }
}
