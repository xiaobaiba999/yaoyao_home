import { isWechat } from '@/utils/envDetect'
import { generateSecureToken } from '@/utils/security'
import request from './request'

const WECHAT_APP_ID = import.meta.env.VITE_WECHAT_APP_ID || ''
const REDIRECT_URI = encodeURIComponent(window.location.origin + '/auth/wechat/callback')

export function getWechatAuthUrl() {
  const state = generateSecureToken()
  sessionStorage.setItem('wechat_auth_state', state)

  return `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${WECHAT_APP_ID}&redirect_uri=${REDIRECT_URI}&response_type=Code&scope=snsapi_userinfo&state=${state}#wechat_redirect`
}

export function parseAuthCallback() {
  const url = new URL(window.location.href)
  const code = url.searchParams.get('code')
  const state = url.searchParams.get('state')
  const storedState = sessionStorage.getItem('wechat_auth_state')

  if (state !== storedState) {
    return { success: false, error: 'State mismatch' }
  }

  return { success: true, code }
}

export async function getWechatUserInfo(code) {
  return request.get(`/wechat/auth?code=${code}`)
}

export function initWechatAuth() {
  if (!isWechat()) {
    return { needAuth: false }
  }

  const token = localStorage.getItem('wechat_token')
  if (token) {
    return { needAuth: false, hasToken: true }
  }

  return { needAuth: true }
}
