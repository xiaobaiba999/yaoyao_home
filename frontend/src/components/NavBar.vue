<template>
  <nav class="navbar" :class="{ 'navbar--scrolled': isScrolled }" role="navigation" aria-label="主导航">
    <div class="navbar__container">
      <div class="navbar__logo" @click="goHome" role="button" tabindex="0" aria-label="返回首页">
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
          :aria-label="item.name"
        >
          <svg class="menu-item-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <component :is="item.iconComponent" />
          </svg>
          <span class="menu-item-text">{{ item.name }}</span>
        </router-link>
      </div>

      <button
        class="navbar__hamburger"
        @click.stop="toggleMobileMenu"
        :class="{ 'navbar__hamburger--active': isMobileMenuOpen }"
        type="button"
        :aria-label="isMobileMenuOpen ? '关闭菜单' : '打开菜单'"
        :aria-expanded="isMobileMenuOpen"
        aria-controls="mobile-menu"
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
import { ref, h, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const HomeIcon = {
  render() {
    return h('g', [
      h('path', { d: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' }),
      h('polyline', { points: '9 22 9 12 15 12 15 22' })
    ])
  }
}

const AnniversaryIcon = {
  render() {
    return h('g', [
      h('path', { d: 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z' })
    ])
  }
}

const DiaryIcon = {
  render() {
    return h('g', [
      h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
      h('polyline', { points: '14 2 14 8 20 8' }),
      h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
      h('line', { x1: '16', y1: '17', x2: '8', y2: '17' })
    ])
  }
}

const AlbumIcon = {
  render() {
    return h('g', [
      h('rect', { x: '3', y: '3', width: '18', height: '18', rx: '2', ry: '2' }),
      h('circle', { cx: '8.5', cy: '8.5', r: '1.5' }),
      h('polyline', { points: '21 15 16 10 5 21' })
    ])
  }
}

const WishlistIcon = {
  render() {
    return h('g', [
      h('path', { d: 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z' })
    ])
  }
}

const TimelineIcon = {
  render() {
    return h('g', [
      h('circle', { cx: '12', cy: '12', r: '10' }),
      h('polyline', { points: '12 6 12 12 16 14' })
    ])
  }
}

const FutureIcon = {
  render() {
    return h('g', [
      h('path', { d: 'M4 19.5A2.5 2.5 0 0 1 6.5 17H20' }),
      h('path', { d: 'M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z' }),
      h('line', { x1: '12', y1: '7', x2: '12', y2: '13' }),
      h('line', { x1: '9', y1: '10', x2: '15', y2: '10' })
    ])
  }
}

const router = useRouter()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)
const menuItems = [
  { name: '首页', path: '/', iconComponent: HomeIcon },
  { name: '纪念日', path: '/anniversary', iconComponent: AnniversaryIcon },
  { name: '日记', path: '/diary', iconComponent: DiaryIcon },
  { name: '相册', path: '/album', iconComponent: AlbumIcon },
  { name: '心愿单', path: '/wishlist', iconComponent: WishlistIcon },
  { name: '时间线', path: '/timeline', iconComponent: TimelineIcon },
  { name: '未来计划', path: '/future', iconComponent: FutureIcon }
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
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
}

.menu-item-icon {
  width: 16px;
  height: 16px;
  display: none;
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
  border: none;
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
    padding: 8px 0;
    gap: 0;
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
                opacity 0.3s ease,
                visibility 0.3s ease;
    z-index: 999;
  }

  .navbar__menu--open {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .navbar__menu-item {
    padding: 14px 24px;
    width: 100%;
    font-size: 15px;
    border-bottom: 1px solid rgba(232, 212, 208, 0.15);
    min-height: 48px;
  }

  .menu-item-icon {
    display: block;
    width: 32px;
    height: 32px;
    flex-shrink: 0;
  }

  .menu-item-text {
    font-size: 15px;
  }

  .navbar__menu-item::after {
    display: none;
  }

  .navbar__menu-item:hover {
    background: rgba(232, 212, 208, 0.1);
  }

  .navbar__menu-item.router-link-active {
    background: rgba(232, 212, 208, 0.15);
    color: var(--color-pink);
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
