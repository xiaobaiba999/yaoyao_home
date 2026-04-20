import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { themePresets, themeCategories, defaultThemeId } from '@/config/themes'

const STORAGE_KEY = 'yaoyao_theme'
const CUSTOM_KEY = 'yaoyao_theme_custom'

function hexToHSL(hex) {
  let r = parseInt(hex.slice(1, 3), 16) / 255
  let g = parseInt(hex.slice(3, 5), 16) / 255
  let b = parseInt(hex.slice(5, 7), 16) / 255
  const max = Math.max(r, g, b), min = Math.min(r, g, b)
  let h, s, l = (max + min) / 2
  if (max === min) {
    h = s = 0
  } else {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break
      case g: h = ((b - r) / d + 2) / 6; break
      case b: h = ((r - g) / d + 4) / 6; break
    }
  }
  return { h: Math.round(h * 360), s: Math.round(s * 100), l: Math.round(l * 100) }
}

function hslToHex(h, s, l) {
  s /= 100; l /= 100
  const a = s * Math.min(l, 1 - l)
  const f = n => {
    const k = (n + h / 30) % 12
    const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1)
    return Math.round(255 * color).toString(16).padStart(2, '0')
  }
  return `#${f(0)}${f(8)}${f(4)}`
}

function adjustColor(hex, hueShift = 0, satShift = 0, lightShift = 0) {
  if (!hex || !hex.startsWith('#')) return hex
  const hsl = hexToHSL(hex)
  hsl.h = (hsl.h + hueShift + 360) % 360
  hsl.s = Math.max(0, Math.min(100, hsl.s + satShift))
  hsl.l = Math.max(0, Math.min(100, hsl.l + lightShift))
  return hslToHex(hsl.h, hsl.s, hsl.l)
}

function applyColorsToRoot(colors) {
  const root = document.documentElement
  for (const [key, value] of Object.entries(colors)) {
    const cssKey = `--color-${key.replace(/([A-Z])/g, '-$1').toLowerCase()}`
    root.style.setProperty(cssKey, value)
  }
  root.setAttribute('data-theme-mode', colors.bg === '#1A1A1E' ? 'dark' : 'light')
}

function getSystemDarkMode() {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

export const useThemeStore = defineStore('theme', () => {
  const currentThemeId = ref(defaultThemeId)
  const followSystem = ref(false)
  const showPanel = ref(false)
  const isTransitioning = ref(false)
  const customAdjustments = ref({ hue: 0, saturation: 0, lightness: 0 })

  const currentTheme = computed(() => {
    return themePresets[currentThemeId.value] || themePresets[defaultThemeId]
  })

  const isDarkMode = computed(() => {
    return currentTheme.value?.mode === 'dark'
  })

  const adjustedColors = computed(() => {
    const base = currentTheme.value.colors
    const adj = customAdjustments.value
    if (adj.hue === 0 && adj.saturation === 0 && adj.lightness === 0) return base
    const result = {}
    for (const [key, value] of Object.entries(base)) {
      if (value.startsWith('#')) {
        result[key] = adjustColor(value, adj.hue, adj.saturation, adj.lightness)
      } else {
        result[key] = value
      }
    }
    return result
  })

  function applyTheme(themeId, animate = true) {
    const theme = themePresets[themeId]
    if (!theme) return

    if (animate) {
      isTransitioning.value = true
      document.documentElement.classList.add('theme-transitioning')
    }

    currentThemeId.value = themeId
    const colors = customAdjustments.value.hue === 0 &&
      customAdjustments.value.saturation === 0 &&
      customAdjustments.value.lightness === 0
      ? theme.colors
      : adjustedColors.value

    applyColorsToRoot(colors)
    persistTheme()

    if (animate) {
      requestAnimationFrame(() => {
        setTimeout(() => {
          document.documentElement.classList.remove('theme-transitioning')
          isTransitioning.value = false
        }, 400)
      })
    }
  }

  function applyCustomAdjustments() {
    applyColorsToRoot(adjustedColors.value)
    persistTheme()
  }

  function resetAdjustments() {
    customAdjustments.value = { hue: 0, saturation: 0, lightness: 0 }
    applyTheme(currentThemeId.value, false)
  }

  function persistTheme() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        themeId: currentThemeId.value,
        followSystem: followSystem.value,
        adjustments: customAdjustments.value
      }))
    } catch (e) { /* ignore */ }
  }

  function loadTheme() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        const data = JSON.parse(saved)
        if (data.themeId && themePresets[data.themeId]) {
          currentThemeId.value = data.themeId
        }
        if (typeof data.followSystem === 'boolean') {
          followSystem.value = data.followSystem
        }
        if (data.adjustments) {
          customAdjustments.value = data.adjustments
        }
      }

      if (followSystem.value) {
        const isDark = getSystemDarkMode()
        const targetId = isDark ? 'dark' : (currentThemeId.value === 'dark' ? defaultThemeId : currentThemeId.value)
        currentThemeId.value = targetId
      }

      applyTheme(currentThemeId.value, false)
    } catch (e) {
      applyTheme(defaultThemeId, false)
    }
  }

  function toggleFollowSystem() {
    followSystem.value = !followSystem.value
    if (followSystem.value) {
      const isDark = getSystemDarkMode()
      const targetId = isDark ? 'dark' : defaultThemeId
      applyTheme(targetId)
    }
    persistTheme()
  }

  function togglePanel() {
    showPanel.value = !showPanel.value
  }

  function closePanel() {
    showPanel.value = false
  }

  let mediaQueryHandler = null

  function initSystemThemeListener() {
    const mql = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQueryHandler = (e) => {
      if (followSystem.value) {
        const targetId = e.matches ? 'dark' : defaultThemeId
        applyTheme(targetId)
      }
    }
    mql.addEventListener('change', mediaQueryHandler)
  }

  function cleanup() {
    if (mediaQueryHandler) {
      window.matchMedia('(prefers-color-scheme: dark)').removeEventListener('change', mediaQueryHandler)
    }
  }

  return {
    currentThemeId,
    currentTheme,
    followSystem,
    showPanel,
    isTransitioning,
    isDarkMode,
    customAdjustments,
    adjustedColors,
    themePresets,
    themeCategories,
    applyTheme,
    applyCustomAdjustments,
    resetAdjustments,
    loadTheme,
    toggleFollowSystem,
    togglePanel,
    closePanel,
    initSystemThemeListener,
    cleanup
  }
})
