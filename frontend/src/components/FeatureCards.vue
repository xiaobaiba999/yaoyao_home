<template>
  <div class="feature-cards">
    <div 
      v-for="(card, index) in cards" 
      :key="card.id"
      class="feature-card"
      :style="{ '--delay': `${index * 0.1}s` }"
      @click="handleCardClick(card)"
      @touchstart.passive="handleTouchStart"
      @touchend.prevent="handleTouchEnd($event, card)"
    >
      <div class="card-icon-wrapper">
        <component :is="card.icon" class="card-icon" />
      </div>
      <div class="card-content">
        <h3 class="card-title">{{ card.title }}</h3>
        <p class="card-desc">{{ card.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { h, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const touchStartTime = ref(0)
const touchStartPos = ref({ x: 0, y: 0 })

const IconAlbum = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z' }),
      h('circle', { cx: '12', cy: '13', r: '4' })
    ])
  }
}

const IconCalendar = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('rect', { x: '3', y: '4', width: '18', height: '18', rx: '2', ry: '2' }),
      h('line', { x1: '16', y1: '2', x2: '16', y2: '6' }),
      h('line', { x1: '8', y1: '2', x2: '8', y2: '6' }),
      h('line', { x1: '3', y1: '10', x2: '21', y2: '10' }),
      h('path', { d: 'M8 14h.01' }),
      h('path', { d: 'M12 14h.01' }),
      h('path', { d: 'M16 14h.01' }),
      h('path', { d: 'M8 18h.01' }),
      h('path', { d: 'M12 18h.01' })
    ])
  }
}

const IconBook = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M4 19.5A2.5 2.5 0 0 1 6.5 17H20' }),
      h('path', { d: 'M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z' }),
      h('line', { x1: '8', y1: '7', x2: '16', y2: '7' }),
      h('line', { x1: '8', y1: '11', x2: '14', y2: '11' })
    ])
  }
}

const IconStar = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('polygon', { points: '12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2' })
    ])
  }
}

const IconTimeline = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('circle', { cx: '12', cy: '12', r: '10' }),
      h('polyline', { points: '12 6 12 12 16 14' })
    ])
  }
}

const IconRocket = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z' }),
      h('path', { d: 'M12 15l-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z' }),
      h('path', { d: 'M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0' }),
      h('path', { d: 'M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5' })
    ])
  }
}

const IconDiary = {
  render() {
    return h('svg', {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.5',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
      h('polyline', { points: '14 2 14 8 20 8' }),
      h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
      h('line', { x1: '16', y1: '17', x2: '8', y2: '17' }),
      h('polyline', { points: '10 9 9 9 8 9' })
    ])
  }
}

const cards = [
  {
    id: 'anniversary',
    title: '纪念日',
    desc: '铭记重要时刻',
    icon: IconCalendar,
    route: '/anniversary',
    anchor: null
  },
  {
    id: 'diary',
    title: '日记',
    desc: '书写每日点滴',
    icon: IconDiary,
    route: '/diary',
    anchor: null
  },
  {
    id: 'album',
    title: '相册',
    desc: '记录美好瞬间',
    icon: IconAlbum,
    route: '/album',
    anchor: null
  },
  {
    id: 'wishlist',
    title: '心愿清单',
    desc: '收藏小小梦想',
    icon: IconStar,
    route: '/wishlist',
    anchor: null
  },
  {
    id: 'timeline',
    title: '时间线',
    desc: '回顾成长轨迹',
    icon: IconTimeline,
    route: '/timeline',
    anchor: null
  },
  {
    id: 'future',
    title: '未来计划',
    desc: '规划美好明天',
    icon: IconRocket,
    route: '/future',
    anchor: null
  }
]

const handleCardClick = (card) => {
  if (card.anchor) {
    const element = document.getElementById(card.anchor)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' })
      return
    }
  }
  if (card.route) {
    router.push(card.route)
  }
}

const handleTouchStart = (e) => {
  touchStartTime.value = Date.now()
  touchStartPos.value = {
    x: e.touches[0].clientX,
    y: e.touches[0].clientY
  }
}

const handleTouchEnd = (e, card) => {
  const touchEndTime = Date.now()
  const touchDuration = touchEndTime - touchStartTime.value
  
  const touch = e.changedTouches[0]
  const moveX = Math.abs(touch.clientX - touchStartPos.value.x)
  const moveY = Math.abs(touch.clientY - touchStartPos.value.y)
  
  if (touchDuration < 300 && moveX < 10 && moveY < 10) {
    e.preventDefault()
    handleCardClick(card)
  }
}
</script>

<style scoped>
.feature-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  padding: 8px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  background: var(--color-bg-secondary);
  border-radius: 20px;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardEnter 0.6s cubic-bezier(0.4, 0, 0.2, 1) backwards;
  animation-delay: var(--delay);
  border: none;
  min-height: 160px;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  -webkit-user-select: none;
}

@keyframes cardEnter {
  0% {
    opacity: 0;
    transform: perspective(1000px) rotateX(15deg) translateY(30px);
  }
  100% {
    opacity: 1;
    transform: perspective(1000px) rotateX(0) translateY(0);
  }
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-card-hover);
}

.feature-card:active {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

.card-icon-wrapper {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-accent-lighter) 0%, var(--color-accent-light) 100%);
  border-radius: 16px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.feature-card:hover .card-icon-wrapper {
  background: linear-gradient(135deg, var(--color-accent-light) 0%, var(--color-accent) 100%);
  transform: scale(1.05);
}

.card-icon {
  width: 28px;
  height: 28px;
  color: var(--color-accent-dark);
  transition: color 0.3s ease;
}

.feature-card:hover .card-icon {
  color: var(--color-text-light);
}

.card-content {
  text-align: center;
}

.card-title {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-title);
  margin: 0 0 6px 0;
  letter-spacing: 0.05em;
  transition: color 0.3s ease;
}

.feature-card:hover .card-title {
  color: var(--color-title);
}

.card-desc {
  font-size: 12px;
  font-weight: 300;
  color: var(--color-text);
  margin: 0;
  letter-spacing: 0.02em;
  line-height: 1.6;
}

.card-content {
  text-align: center;
}

.card-title {
  font-size: 16px;
  font-weight: 400;
  color: var(--color-title);
  margin: 0 0 6px 0;
  letter-spacing: 0.05em;
  transition: color 0.3s ease;
}

.feature-card:hover .card-title {
  color: var(--color-title);
}

.card-desc {
  font-size: 12px;
  font-weight: 300;
  color: var(--color-text);
  margin: 0;
  letter-spacing: 0.02em;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .feature-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 4px;
  }

  .feature-card {
    padding: 24px 16px;
    min-height: 140px;
  }

  .card-icon-wrapper {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
  }

  .card-icon {
    width: 24px;
    height: 24px;
  }

  .card-title {
    font-size: 14px;
  }

  .card-desc {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .feature-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .feature-card {
    flex-direction: row;
    justify-content: flex-start;
    padding: 20px;
    min-height: auto;
    gap: 16px;
  }

  .card-icon-wrapper {
    margin-bottom: 0;
    flex-shrink: 0;
  }

  .card-content {
    text-align: left;
  }
}

@media (prefers-reduced-motion: reduce) {
  .feature-card {
    animation: none;
  }
  
  .feature-card:hover {
    transform: none;
  }
}
</style>
