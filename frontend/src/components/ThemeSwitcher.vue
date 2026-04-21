<template>
  <div class="theme-switcher">
    <button
      class="theme-trigger"
      @click="handleTriggerClick"
      :class="{ active: themeStore.showPanel, minimized: isScrolled }"
      :style="triggerStyle"
      title="切换主题"
    >
      <div class="trigger-bg"></div>
      <div class="trigger-content">
        <span class="trigger-day" :style="{ color: textColor }">我们在一起的第{{ daysTogether }}天</span>
        <span class="trigger-sub" :style="{ color: subTextColor }">时光荏苒，每一刻都是爱的见证</span>
      </div>
      <div class="trigger-heart" :style="{ color: heartColor }">
        <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
        </svg>
      </div>
      <div class="trigger-glow"></div>
    </button>

    <Teleport to="body">
      <Transition name="overlay">
        <div
          v-if="themeStore.showPanel"
          class="theme-overlay"
          @click="themeStore.closePanel"
        />
      </Transition>

      <Transition name="panel">
        <div v-if="themeStore.showPanel" class="theme-panel" @click.stop>
          <div class="panel-header">
            <h3 class="panel-title">主题设置</h3>
            <button class="panel-close" @click="themeStore.closePanel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <div class="panel-body">
            <section class="panel-section">
              <div class="section-header">
                <span class="section-title">跟随系统</span>
                <button
                  class="toggle-btn"
                  :class="{ active: themeStore.followSystem }"
                  @click="themeStore.toggleFollowSystem"
                >
                  <span class="toggle-thumb"/>
                </button>
              </div>
              <p class="section-desc">自动匹配操作系统的浅色/深色模式</p>
            </section>

            <section class="panel-section" v-for="category in themeCategories" :key="category.id">
              <h4 class="section-title">{{ category.name }}</h4>
              <div class="theme-grid">
                <button
                  v-for="themeId in category.themeIds"
                  :key="themeId"
                  class="theme-card"
                  :class="{ active: themeStore.currentThemeId === themeId }"
                  @click="selectTheme(themeId)"
                  :disabled="themeStore.followSystem"
                >
                  <div class="card-preview">
                    <span class="preview-dot" :style="{ background: themePresets[themeId].colors.bg }"/>
                    <span class="preview-dot" :style="{ background: themePresets[themeId].colors.accent }"/>
                    <span class="preview-dot" :style="{ background: themePresets[themeId].colors.pink }"/>
                  </div>
                  <div class="card-info">
                    <span class="card-icon">{{ themePresets[themeId].icon }}</span>
                    <span class="card-name">{{ themePresets[themeId].name }}</span>
                  </div>
                  <span v-if="themeStore.currentThemeId === themeId" class="card-check">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                  </span>
                </button>
              </div>
            </section>

            <section class="panel-section">
              <div class="section-header">
                <span class="section-title">微调主题</span>
                <button class="reset-btn" @click="themeStore.resetAdjustments" v-if="hasAdjustments">重置</button>
              </div>
              <p class="section-desc">调整当前主题的色相、饱和度和明度</p>

              <div class="slider-group">
                <label class="slider-label">
                  <span>色相</span>
                  <span class="slider-value">{{ themeStore.customAdjustments.hue }}°</span>
                </label>
                <input
                  type="range"
                  min="-30"
                  max="30"
                  step="1"
                  v-model.number="themeStore.customAdjustments.hue"
                  @input="onAdjust"
                  class="slider hue-slider"
                />
              </div>

              <div class="slider-group">
                <label class="slider-label">
                  <span>饱和度</span>
                  <span class="slider-value">{{ themeStore.customAdjustments.saturation > 0 ? '+' : '' }}{{ themeStore.customAdjustments.saturation }}%</span>
                </label>
                <input
                  type="range"
                  min="-20"
                  max="20"
                  step="1"
                  v-model.number="themeStore.customAdjustments.saturation"
                  @input="onAdjust"
                  class="slider sat-slider"
                />
              </div>

              <div class="slider-group">
                <label class="slider-label">
                  <span>明度</span>
                  <span class="slider-value">{{ themeStore.customAdjustments.lightness > 0 ? '+' : '' }}{{ themeStore.customAdjustments.lightness }}%</span>
                </label>
                <input
                  type="range"
                  min="-15"
                  max="15"
                  step="1"
                  v-model.number="themeStore.customAdjustments.lightness"
                  @input="onAdjust"
                  class="slider light-slider"
                />
              </div>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { themePresets, themeCategories } from '@/config/themes'

const themeStore = useThemeStore()

const START_DATE = new Date('2023-05-20')
const isScrolled = ref(false)

const daysTogether = computed(() => {
  const now = new Date()
  const diff = now.getTime() - START_DATE.getTime()
  return Math.floor(diff / (1000 * 60 * 60 * 24))
})

const hasAdjustments = computed(() => {
  const a = themeStore.customAdjustments
  return a.hue !== 0 || a.saturation !== 0 || a.lightness !== 0
})

function hexToRgb(hex) {
  if (!hex || !hex.startsWith('#')) return { r: 128, g: 128, b: 128 }
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return { r, g, b }
}

function relativeLuminance(r, g, b) {
  const [rs, gs, bs] = [r, g, b].map(c => {
    const s = c / 255
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4)
  })
  return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs
}

function contrastRatio(lum1, lum2) {
  const lighter = Math.max(lum1, lum2)
  const darker = Math.min(lum1, lum2)
  return (lighter + 0.05) / (darker + 0.05)
}

function getContrastTextColor(bgHex, minRatio = 4.5) {
  const bg = hexToRgb(bgHex)
  const bgLum = relativeLuminance(bg.r, bg.g, bg.b)
  const whiteLum = relativeLuminance(255, 255, 255)
  const blackLum = relativeLuminance(0, 0, 0)
  const whiteContrast = contrastRatio(whiteLum, bgLum)
  const blackContrast = contrastRatio(bgLum, blackLum)
  if (whiteContrast >= minRatio && whiteContrast >= blackContrast) return '#FFFFFF'
  if (blackContrast >= minRatio) return '#000000'
  return whiteContrast > blackContrast ? '#FFFFFF' : '#000000'
}

const currentAccentColor = computed(() => {
  const colors = themeStore.adjustedColors
  return colors?.accent || '#E8A0B0'
})

const textColor = computed(() => getContrastTextColor(currentAccentColor.value))
const subTextColor = computed(() => {
  const base = getContrastTextColor(currentAccentColor.value)
  return base === '#FFFFFF' ? 'rgba(255, 255, 255, 0.85)' : 'rgba(0, 0, 0, 0.7)'
})
const heartColor = computed(() => getContrastTextColor(currentAccentColor.value))

const triggerStyle = computed(() => ({}))

let adjustTimer = null

function handleScroll() {
  isScrolled.value = window.scrollY > 80
}

function handleTriggerClick() {
  themeStore.togglePanel()
}

function selectTheme(themeId) {
  if (themeStore.followSystem) return
  themeStore.applyTheme(themeId)
}

function onAdjust() {
  if (adjustTimer) clearTimeout(adjustTimer)
  adjustTimer = setTimeout(() => {
    themeStore.applyCustomAdjustments()
  }, 50)
}

function handleEscape(e) {
  if (e.key === 'Escape' && themeStore.showPanel) {
    themeStore.closePanel()
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  document.addEventListener('keydown', handleEscape)
  themeStore.initSystemThemeListener()
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('keydown', handleEscape)
  themeStore.cleanup()
})
</script>

<style scoped>
.theme-trigger {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  min-width: 180px;
  min-height: 52px;
  background: transparent;
}

.theme-trigger.minimized {
  min-width: 48px;
  min-height: 48px;
  width: 48px;
  height: 48px;
  padding: 0;
  border-radius: 50%;
}

.trigger-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-pink) 50%, var(--color-accent-dark) 100%);
  border-radius: 24px;
  opacity: 0.9;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-trigger.minimized .trigger-bg {
  border-radius: 50%;
  opacity: 1;
}

.theme-trigger:hover .trigger-bg {
  opacity: 1;
}

.trigger-glow {
  position: absolute;
  inset: -2px;
  border-radius: 26px;
  background: linear-gradient(135deg, var(--color-accent-light), var(--color-pink-light), var(--color-accent));
  opacity: 0;
  z-index: -1;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  filter: blur(8px);
}

.theme-trigger.minimized .trigger-glow {
  border-radius: 50%;
}

.theme-trigger:hover .trigger-glow {
  opacity: 0.6;
}

.theme-trigger:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.theme-trigger.minimized:hover {
  transform: translateY(-2px) scale(1.08);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.theme-trigger:active {
  transform: translateY(0) scale(0.98);
}

.theme-trigger.active {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.trigger-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-trigger.minimized .trigger-content {
  opacity: 0;
  transform: scale(0.5);
  pointer-events: none;
  position: absolute;
}

.trigger-day {
  font-size: 13px;
  font-weight: 700;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  letter-spacing: 0.5px;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.trigger-sub {
  font-size: 9px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  letter-spacing: 0.3px;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.trigger-heart {
  position: absolute;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.theme-trigger.minimized .trigger-heart {
  opacity: 1;
  transform: scale(1);
}

.theme-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(2px);
  z-index: 9998;
}

.theme-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 420px;
  max-width: calc(100vw - 32px);
  max-height: 80vh;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.panel-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-title);
}

.panel-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.panel-close:hover {
  background: var(--color-bg-tertiary);
}

.panel-close svg {
  width: 18px;
  height: 18px;
  color: var(--color-text-light);
}

.panel-body {
  padding: 8px 24px 24px;
  overflow-y: auto;
  flex: 1;
}

.panel-section {
  padding: 16px 0;
}

.panel-section + .panel-section {
  border-top: 1px solid var(--color-border-light);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-title);
}

.section-desc {
  font-size: 0.78rem;
  color: var(--color-text-lighter);
  margin-top: 4px;
}

.toggle-btn {
  position: relative;
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: var(--color-border);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
  flex-shrink: 0;
}

.toggle-btn.active {
  background: var(--color-accent);
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s cubic-bezier(0.68, -0.2, 0.27, 1.2);
}

.toggle-btn.active .toggle-thumb {
  transform: translateX(20px);
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 12px;
}

.theme-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  background: var(--color-bg);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.25s ease;
}

.theme-card:hover:not(:disabled) {
  border-color: var(--color-border);
  transform: translateY(-2px);
}

.theme-card.active {
  border-color: var(--color-accent);
  background: var(--color-accent-lighter);
}

.theme-card:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.card-preview {
  display: flex;
  gap: 4px;
}

.preview-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid var(--color-border-light);
}

.card-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-icon {
  font-size: 0.85rem;
}

.card-name {
  font-size: 0.75rem;
  color: var(--color-text);
  white-space: nowrap;
}

.card-check {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 16px;
  height: 16px;
  color: var(--color-accent-dark);
}

.card-check svg {
  width: 100%;
  height: 100%;
}

.reset-btn {
  font-size: 0.78rem;
  color: var(--color-accent-dark);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  transition: all 0.2s ease;
}

.reset-btn:hover {
  background: var(--color-accent-lighter);
}

.slider-group {
  margin-top: 14px;
}

.slider-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: var(--color-text-light);
  margin-bottom: 6px;
}

.slider-value {
  font-variant-numeric: tabular-nums;
  min-width: 40px;
  text-align: right;
  color: var(--color-text);
  font-weight: 500;
}

.slider {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 4px;
  border-radius: 2px;
  outline: none;
  cursor: pointer;
}

.hue-slider {
  background: linear-gradient(to right, #ff6b6b, #ffd93d, #6bcb77, #4d96ff, #9b59b6, #ff6b6b);
}

.sat-slider {
  background: linear-gradient(to right, var(--color-bg-tertiary), var(--color-accent));
}

.light-slider {
  background: linear-gradient(to right, #1a1a1a, var(--color-bg-secondary), #ffffff);
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  border: 2px solid var(--color-accent);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.slider::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}

.slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  border: 2px solid var(--color-accent);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}

.overlay-enter-active,
.overlay-leave-active {
  transition: opacity 0.3s ease;
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

.panel-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.panel-leave-active {
  transition: all 0.25s ease-in;
}

.panel-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.9);
}

.panel-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.95);
}

@media (max-width: 768px) {
  .theme-trigger {
    min-width: 140px;
    padding: 8px 14px;
    border-radius: 20px;
  }

  .theme-trigger.minimized {
    min-width: 42px;
    min-height: 42px;
    width: 42px;
    height: 42px;
  }

  .trigger-day {
    font-size: 11px;
  }

  .trigger-sub {
    font-size: 8px;
  }
}

@media (max-width: 480px) {
  .theme-panel {
    width: 100%;
    max-width: 100%;
    max-height: 85vh;
    top: auto;
    bottom: 0;
    left: 0;
    transform: none;
    border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  }

  .panel-enter-from {
    opacity: 0;
    transform: translateY(100%);
  }

  .panel-leave-to {
    opacity: 0;
    transform: translateY(100%);
  }

  .theme-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .theme-trigger {
    min-width: 120px;
    padding: 6px 12px;
  }

  .theme-trigger.minimized {
    min-width: 38px;
    min-height: 38px;
    width: 38px;
    height: 38px;
  }

  .trigger-day {
    font-size: 10px;
  }

  .trigger-sub {
    font-size: 7px;
  }
}
</style>
