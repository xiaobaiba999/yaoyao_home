import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { isWechat, getEnvironment } from '@/utils/envDetect'
import { getWechatAuthUrl, getWechatUserInfo, parseAuthCallback, initWechatAuth } from '@/api/wechat'

export const useWechatStore = defineStore('wechat', () => {
  const openid = ref(localStorage.getItem('wechat_openid') || '')
  const nickname = ref(localStorage.getItem('wechat_nickname') || '')
  const avatar = ref(localStorage.getItem('wechat_avatar') || '')
  const isAuth = ref(false)
  const loading = ref(false)
  const environment = ref(getEnvironment())

  const isWechatEnv = computed(() => isWechat())
  const isLoggedIn = computed(() => !!openid.value)

  function setUserInfo(info) {
    openid.value = info.openid || ''
    nickname.value = info.nickname || ''
    avatar.value = info.headimgurl || info.avatar || ''
    isAuth.value = true

    localStorage.setItem('wechat_openid', openid.value)
    localStorage.setItem('wechat_nickname', nickname.value)
    localStorage.setItem('wechat_avatar', avatar.value)
  }

  function clearUserInfo() {
    openid.value = ''
    nickname.value = ''
    avatar.value = ''
    isAuth.value = false

    localStorage.removeItem('wechat_openid')
    localStorage.removeItem('wechat_nickname')
    localStorage.removeItem('wechat_avatar')
    localStorage.removeItem('wechat_token')
  }

  async function startAuth() {
    if (!isWechat()) {
      return { success: false, error: 'Not in WeChat environment' }
    }

    loading.value = true
    const authUrl = getWechatAuthUrl()
    window.location.href = authUrl
  }

  async function handleCallback() {
    const result = parseAuthCallback()
    if (!result.success) {
      return result
    }

    loading.value = true
    try {
      const userInfo = await getWechatUserInfo(result.code)
      setUserInfo(userInfo)
      loading.value = false
      return { success: true }
    } catch (error) {
      loading.value = false
      return { success: false, error: error.message || '授权失败' }
    }
  }

  function checkAuth() {
    const result = initWechatAuth()
    if (result.hasToken) {
      isAuth.value = true
    }
    return result
  }

  return {
    openid,
    nickname,
    avatar,
    isAuth,
    loading,
    environment,
    isWechatEnv,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
    startAuth,
    handleCallback,
    checkAuth
  }
})
