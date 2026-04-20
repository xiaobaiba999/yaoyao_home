<template>
  <div class="anniversary-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">纪念日</h1>
    </div>
    
    <div class="page-content">
      <div class="anniversary-grid">
        <div
          v-for="(item, index) in anniversaries"
          :key="item.id"
          class="anniversary-card"
          :style="{ '--delay': index * 0.1 + 's' }"
        >
          <div class="card-icon">{{ item.icon }}</div>
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-date">{{ item.displayDate }}</p>
          
          <div class="card-number" v-if="item.type === 'passed'">
            <span class="number">{{ item.days }}</span>
            <span class="unit">天</span>
          </div>
          <div class="card-number countdown" v-else>
            <span class="label">还有</span>
            <span class="number">{{ item.countdown }}</span>
            <span class="unit">天</span>
          </div>
        </div>
      </div>
      
      <div class="love-message">
        <div class="message-card">
          <p class="message-text">
            时光荏苒，岁月如歌<br/>
            每一天都是我们爱的见证<br/>
            愿我们的爱情永远甜蜜如初
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAnniversaryInfo } from '@/utils/dateUtils'

const router = useRouter()
const anniversaries = ref([])

onMounted(() => {
  anniversaries.value = getAnniversaryInfo()
})

function goBack() {
  router.push('/')
}
</script>

<style scoped>
.anniversary-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-top: 80px;
}

.page-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(250, 248, 245, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 100;
  border-bottom: 1px solid var(--color-border);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--color-text);
  font-size: 14px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: var(--color-pink-light);
  color: var(--color-pink);
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.page-title {
  flex: 1;
  text-align: center;
  font-size: 1.25rem;
  font-weight: 400;
  color: var(--color-title);
  letter-spacing: 0.05em;
  margin-right: 60px;
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
}

.anniversary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.anniversary-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 32px;
  text-align: center;
  box-shadow: var(--shadow-soft);
  transition: all var(--transition-normal);
  animation: cardEnter 0.6s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
  transform: translateY(20px);
}

@keyframes cardEnter {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.anniversary-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-medium);
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 400;
  color: var(--color-title);
  margin-bottom: 8px;
}

.card-date {
  font-size: 0.9rem;
  color: var(--color-text-light);
  margin-bottom: 20px;
}

.card-number {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
}

.card-number .label {
  font-size: 0.9rem;
  color: var(--color-text-light);
}

.card-number .number {
  font-size: 3rem;
  font-weight: 300;
  color: var(--color-pink);
  line-height: 1;
}

.card-number .unit {
  font-size: 1rem;
  color: var(--color-text-light);
}

.card-number.countdown .number {
  color: #b8a0a0;
}

.love-message {
  text-align: center;
  margin-top: 40px;
}

.message-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-soft);
}

.message-text {
  font-size: 1.1rem;
  color: var(--color-text);
  line-height: 2;
  font-style: italic;
}

@media (max-width: 768px) {
  .anniversary-page {
    padding-top: 70px;
  }
  
  .page-header {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 1.1rem;
    margin-right: 50px;
  }
  
  .page-content {
    padding: 24px 16px;
  }
  
  .anniversary-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .anniversary-card {
    padding: 24px;
  }
  
  .card-icon {
    font-size: 2.5rem;
  }
  
  .card-number .number {
    font-size: 2.5rem;
  }
  
  .message-card {
    padding: 24px;
  }
  
  .message-text {
    font-size: 1rem;
  }
}
</style>
