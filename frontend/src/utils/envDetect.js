export function isWechat() {
  const ua = navigator.userAgent.toLowerCase()
  return ua.includes('micromessenger')
}

export function isMiniProgram() {
  return window.__wxjs_environment === 'miniprogram' || 
         /miniprogram/i.test(navigator.userAgent)
}

export function getEnvironment() {
  if (isMiniProgram()) return 'miniprogram'
  if (isWechat()) return 'wechat'
  return 'browser'
}

export function supportOpenTag() {
  return isWechat()
}
