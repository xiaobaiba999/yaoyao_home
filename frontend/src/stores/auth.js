import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getMe } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(null)

  const isAuthenticated = computed(() => !!token.value)

  async function loginAction(username, password) {
    try {
      const response = await login(username, password)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      return { success: true }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  async function checkAuth() {
    if (!token.value) return false

    try {
      const response = await getMe()
      user.value = response.user
      return true
    } catch (error) {
      logout()
      return false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    user,
    isAuthenticated,
    loginAction,
    checkAuth,
    logout
  }
})
