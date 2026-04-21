<template>
  <div id="app" @click="handleFirstInteraction">
    <PreLoader :loading="isLoading" />
    <NavBar v-if="!isLoginPage" />
    <router-view v-slot="{ Component }">
      <transition name="page-flip" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    <MusicPlayer v-if="!isLoginPage" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useBgmStore } from '@/stores/bgm'
import { useThemeStore } from '@/stores/theme'
import NavBar from '@/components/NavBar.vue'
import PreLoader from '@/components/PreLoader.vue'
import MusicPlayer from '@/components/MusicPlayer.vue'

const authStore = useAuthStore()
const bgmStore = useBgmStore()
const themeStore = useThemeStore()
const route = useRoute()
const isLoading = ref(true)
const hasInteracted = ref(false)

const isLoginPage = computed(() => route.path === '/login')

function handleFirstInteraction() {
  if (!hasInteracted.value && bgmStore.bgmEnabled && !bgmStore.isPlaying) {
    hasInteracted.value = true
    bgmStore.setUserInteracted()
    if (bgmStore.playlist.length > 0) {
      bgmStore.playTrack(bgmStore.currentTrackIndex)
    }
  }
}

function prefetchRoutes() {
  import('@/views/Album.vue')
  import('@/views/Timeline.vue')
  import('@/views/Guestbook.vue')
  import('@/views/Diary.vue')
  import('@/views/Wishlist.vue')
  import('@/views/Future.vue')
}

onMounted(() => {
  themeStore.loadTheme()
  authStore.checkAuth()
  bgmStore.initVisibilityListener()
  setTimeout(() => {
    isLoading.value = false
    requestIdleCallback(() => {
      prefetchRoutes()
    })
  }, 600)
})

onUnmounted(() => {
  bgmStore.cleanupVisibilityListener()
})

watch(route, () => {
  window.scrollTo(0, 0)
})
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
  background: var(--color-bg);
  transition: background-color 0.4s ease;
}
</style>
