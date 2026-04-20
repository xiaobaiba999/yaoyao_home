<template>
  <nav class="navbar" :class="{ 'navbar--scrolled': isScrolled }">
    <div class="navbar__container">
      <div class="navbar__logo" @click="goHome">
        <svg class="logo-icon" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M16 28C16 28 4 20 4 12C4 7.58 7.58 4 12 4C14.12 4 16 5.5 16 5.5C16 5.5 17.88 4 20 4C24.42 4 28 7.58 28 12C28 20 16 28 16 28Z" />
        </svg>
        <span class="logo-text">首页</span>
      </div>

      <div class="navbar__menu" :class="{ 'navbar__menu--open': isMobileMenuOpen }">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="navbar__menu-item"
          @click="closeMobileMenu"
        >
          {{ item.name }}
        </router-link>
      </div>

      <button 
        class="navbar__hamburger" 
        @click.stop="toggleMobileMenu" 
        @touchstart.passive="onHamburgerTouchStart"
        @touchend.prevent="onHamburgerTouchEnd"
        :class="{ 'navbar__hamburger--active': isMobileMenuOpen }"
        type="button"
        aria-label="菜单"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>

    <Transition name="fade">
      <div v-if="isMobileMenuOpen" class="navbar__mobile-overlay" @click="closeMobileMenu"></div>
    </Transition>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)
const menuItems = [
  { name: '首页', path: '/' },
  { name: '纪念日', path: '/anniversary' },
  { name: '日记', path: '/diary' },
  { name: '相册', path: '/album' },
  { name: '心愿单', path: '/wishlist' },
  { name: '时间线', path: '/timeline' },
  { name: '未来计划', path: '/future' }
]

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  document.body.style.overflow = isMobileMenuOpen.value ? 'hidden' : ''
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
  document.body.style.overflow = ''
}

const goHome = () => {
  router.push('/')
  closeMobileMenu()
}

const onHamburgerTouchStart = () => {
  hamburgerTouchStart.value = Date.now()
}

const onHamburgerTouchEnd = (e) => {
  const duration = Date.now() - hamburgerTouchStart.value
  if (duration < 300) {
    toggleMobileMenu()
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(250, 248, 245, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 1000;
  transition: all 0.3s ease;
}

.navbar--scrolled {
  background: rgba(250, 248, 245, 0.95);
}

.navbar__container {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar__logo {
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.navbar__logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  width: 28px;
  height: 28px;
  color: var(--color-pink);
  transition: color 0.3s ease;
}

.logo-text {
  font-size: 14px;
  color: var(--color-pink);
  margin-left: 6px;
  font-weight: 400;
  transition: color 0.3s ease;
}

.navbar__logo:hover .logo-icon {
  color: var(--color-accent);
}

.navbar__logo:hover .logo-text {
  color: var(--color-accent);
}

.navbar__menu {
  display: flex;
  align-items: center;
  gap: 32px;
}

.navbar__menu-item {
  font-size: 14px;
  font-weight: 300;
  color: var(--color-text-light);
  letter-spacing: 0.5px;
  position: relative;
  transition: color 0.3s ease;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

.navbar__menu-item::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--color-accent);
  transition: width 0.3s ease;
}

.navbar__menu-item:hover {
  color: var(--color-accent);
}

.navbar__menu-item:hover::after {
  width: 100%;
}

.navbar__menu-item.router-link-active {
  color: var(--color-pink);
}

.navbar__menu-item.router-link-active::after {
  width: 100%;
}

.navbar__hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 44px;
  height: 44px;
  background: transparent;
  gap: 5px;
  padding: 4px;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  cursor: pointer;
}

.navbar__hamburger span {
  display: block;
  width: 20px;
  height: 1.5px;
  background: var(--color-text-light);
  transition: all 0.3s ease;
  border-radius: 1px;
}

.navbar__hamburger--active span:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.navbar__hamburger--active span:nth-child(2) {
  opacity: 0;
}

.navbar__hamburger--active span:nth-child(3) {
  transform: rotate(-45deg) translate(5px, -5px);
}

.navbar__mobile-overlay {
  display: none;
}

@media (max-width: 768px) {
  .navbar__hamburger {
    display: flex;
  }

  .navbar__menu {
    position: fixed;
    top: 60px;
    left: 0;
    right: 0;
    background: rgba(250, 248, 245, 0.98);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    flex-direction: column;
    padding: 24px 0;
    gap: 0;
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
  }

  .navbar__menu--open {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .navbar__menu-item {
    padding: 16px 24px;
    width: 100%;
    text-align: center;
    font-size: 15px;
    border-bottom: 1px solid rgba(232, 212, 208, 0.2);
  }

  .navbar__menu-item::after {
    display: none;
  }

  .navbar__menu-item:hover {
    background: rgba(232, 212, 208, 0.1);
  }

  .navbar__mobile-overlay {
    display: block;
    position: fixed;
    top: 60px;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.1);
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
</style>
