<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden"
       style="background: linear-gradient(160deg, #FFF8F0 0%, #FDE8E8 40%, #DBEAFE 100%)">

    <div class="absolute inset-0 pointer-events-none overflow-hidden">
      <div v-for="i in 6" :key="i"
           class="absolute rounded-full opacity-20"
           :class="[
             i % 2 === 0 ? 'bg-pink-soft' : 'bg-blue-soft',
           ]"
           :style="bubbleStyles[i - 1]">
      </div>
    </div>

    <div class="w-full max-w-sm relative z-10">
      <div class="bg-white/80 backdrop-blur-xl rounded-3xl p-8 sm:p-10 shadow-[0_8px_40px_-12px_rgba(0,0,0,0.08)]">

        <div class="text-center mb-10">
          <div class="inline-flex items-center justify-center w-16 h-16 mb-5">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full">
              <path d="M24 40C24 40 6 28 6 16C6 10 10 6 16 6C19.5 6 22.5 8 24 11C25.5 8 28.5 6 32 6C38 6 42 10 42 16C42 28 24 40 24 40Z"
                    stroke="url(#heartGrad)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none">
                <animate attributeName="d"
                  values="M24 40C24 40 6 28 6 16C6 10 10 6 16 6C19.5 6 22.5 8 24 11C25.5 8 28.5 6 32 6C38 6 42 10 42 16C42 28 24 40 24 40Z;
                          M24 38C24 38 8 27 8 16C8 11 11 7 16 7C19 7 22 9 24 12C26 9 29 7 32 7C37 7 40 11 40 16C40 27 24 38 24 38Z;
                          M24 40C24 40 6 28 6 16C6 10 10 6 16 6C19.5 6 22.5 8 24 11C25.5 8 28.5 6 32 6C38 6 42 10 42 16C42 28 24 40 24 40Z"
                  dur="3s" repeatCount="indefinite" />
              </path>
              <defs>
                <linearGradient id="heartGrad" x1="6" y1="6" x2="42" y2="40">
                  <stop offset="0%" stop-color="#E8A0A0"/>
                  <stop offset="100%" stop-color="#93B5CF"/>
                </linearGradient>
              </defs>
            </svg>
          </div>

          <h1 class="text-xl font-medium text-gray-700 tracking-wide mb-2">瑶瑶纪念空间</h1>
          <p class="text-xs text-gray-400 tracking-widest">专属我们的美好时光</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <div class="space-y-1.5">
            <div class="relative group">
              <svg class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300 group-focus-within:text-pink-accent transition-colors duration-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input
                type="text"
                v-model="username"
                placeholder="请输入账号"
                required
                class="w-full pl-10 pr-4 py-3 bg-transparent border-b border-gray-200 text-sm text-gray-600 placeholder:text-gray-300 focus:outline-none focus:border-pink-accent transition-all duration-500"
              />
              <div class="absolute bottom-0 left-0 h-px w-0 bg-gradient-to-r from-pink-accent to-blue-accent transition-all duration-500 group-focus-within:w-full"></div>
            </div>
          </div>

          <div class="space-y-1.5">
            <div class="relative group">
              <svg class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300 group-focus-within:text-pink-accent transition-colors duration-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="password"
                placeholder="请输入密码"
                required
                class="w-full pl-10 pr-10 py-3 bg-transparent border-b border-gray-200 text-sm text-gray-600 placeholder:text-gray-300 focus:outline-none focus:border-pink-accent transition-all duration-500"
              />
              <div class="absolute bottom-0 left-0 h-px w-0 bg-gradient-to-r from-pink-accent to-blue-accent transition-all duration-500 group-focus-within:w-full"></div>
              <button type="button" @click="showPassword = !showPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-gray-300 hover:text-pink-accent transition-colors duration-300">
                <svg v-if="!showPassword" class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
          </div>

          <transition name="fade">
            <div v-if="errorMessage" class="text-center py-2 px-3 rounded-xl bg-pink-light/50 text-pink-accent text-xs tracking-wide">
              {{ errorMessage }}
            </div>
          </transition>

          <button type="submit" :disabled="isLoading"
                  class="w-full py-3.5 rounded-2xl text-white text-sm font-medium tracking-wider
                         bg-gradient-to-r from-pink-accent to-blue-accent
                         hover:shadow-[0_4px_20px_-4px_rgba(232,160,160,0.4)]
                         active:scale-[0.98]
                         disabled:opacity-50 disabled:cursor-not-allowed
                         transition-all duration-300 ease-out
                         relative overflow-hidden">
            <span v-if="isLoading" class="inline-flex items-center gap-2">
              <svg class="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" opacity="0.3"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              登录中
            </span>
            <span v-else>登 录</span>
          </button>
        </form>

        <div class="mt-8 pt-6 border-t border-gray-100 text-center">
          <div class="flex items-center justify-center gap-3 text-gray-300">
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span class="text-[10px] tracking-[0.2em]">欢迎来到我们的专属空间</span>
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8h1a4 4 0 0 1 0 8h-1"/>
              <path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/>
              <line x1="6" y1="1" x2="6" y2="4"/>
              <line x1="10" y1="1" x2="10" y2="4"/>
              <line x1="14" y1="1" x2="14" y2="4"/>
            </svg>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')
const bubbleStyles = ref([])

onMounted(() => {
  const positions = [
    { w: 120, h: 120, top: '8%', left: '-5%', delay: '0s', dur: '20s' },
    { w: 80, h: 80, top: '60%', left: '-8%', delay: '3s', dur: '25s' },
    { w: 100, h: 100, top: '20%', right: '-6%', delay: '5s', dur: '22s' },
    { w: 60, h: 60, top: '70%', right: '-4%', delay: '8s', dur: '18s' },
    { w: 90, h: 90, top: '40%', left: '10%', delay: '2s', dur: '24s' },
    { w: 70, h: 70, top: '10%', right: '15%', delay: '6s', dur: '19s' },
  ]
  bubbleStyles.value = positions.map(p => ({
    width: `${p.w}px`,
    height: `${p.h}px`,
    top: p.top,
    left: p.left,
    right: p.right,
    animation: `float-bubble ${p.dur} ${p.delay} ease-in-out infinite alternate`,
  }))
})

async function handleLogin() {
  if (!username.value || !password.value) {
    errorMessage.value = '请输入账号和密码'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await authStore.loginAction(username.value, password.value)

    if (result.success) {
      router.push('/')
    } else {
      errorMessage.value = result.message || '账号或密码错误'
    }
  } catch (error) {
    errorMessage.value = '登录失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
@keyframes float-bubble {
  0% {
    transform: translateY(0) translateX(0);
  }
  100% {
    transform: translateY(-30px) translateX(15px);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

input::placeholder {
  font-weight: 300;
  letter-spacing: 0.05em;
}

input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 30px white inset !important;
  -webkit-text-fill-color: #4B5563 !important;
}
</style>
