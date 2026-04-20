<template>
  <div class="wishlist-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">心愿清单</h1>
    </div>
    
    <div class="page-content">
      <div class="add-wish-section">
        <div class="input-wrapper">
          <input 
            v-model="newWish" 
            type="text" 
            placeholder="写下你的心愿..."
            class="wish-input"
            @keyup.enter="addWish"
          />
          <button 
            class="add-btn" 
            @click="addWish"
            :disabled="!newWish.trim()"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </button>
        </div>
      </div>
      
      <div class="stats-bar">
        <span class="stat-item">
          <span class="stat-number">{{ totalWishes }}</span>
          <span class="stat-label">个心愿</span>
        </span>
        <span class="stat-divider">|</span>
        <span class="stat-item">
          <span class="stat-number completed">{{ completedWishes }}</span>
          <span class="stat-label">已实现</span>
        </span>
      </div>
      
      <div class="wishes-container">
        <div v-if="wishes.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
          </div>
          <p class="empty-text">还没有心愿</p>
          <p class="empty-hint">在上方输入框写下你的第一个心愿吧</p>
        </div>
        
        <TransitionGroup v-else name="wish-list" tag="div" class="wishes-list">
          <div 
            v-for="wish in wishes" 
            :key="wish.id" 
            class="wish-item"
            :class="{ completed: wish.completed }"
          >
            <button 
              class="check-btn"
              :class="{ checked: wish.completed }"
              @click="toggleWish(wish)"
            >
              <svg v-if="wish.completed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </button>
            <span class="wish-content">{{ wish.content }}</span>
            <span class="wish-date">{{ formatDate(wish.created_at) }}</span>
            <button class="delete-btn" @click="deleteWish(wish.id)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </TransitionGroup>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWishes, createWish, updateWish, deleteWish as deleteWishApi } from '@/api/wishes'

const router = useRouter()
const wishes = ref([])
const newWish = ref('')

const totalWishes = computed(() => wishes.value.length)
const completedWishes = computed(() => wishes.value.filter(w => w.completed).length)

async function loadWishes() {
  try {
    wishes.value = await getWishes()
  } catch (e) {
    console.error('加载心愿失败', e)
    wishes.value = []
  }
}

async function addWish() {
  const content = newWish.value.trim()
  if (!content) return
  
  try {
    const wish = await createWish(content)
    wishes.value.unshift(wish)
    newWish.value = ''
  } catch (e) {
    console.error('创建心愿失败', e)
  }
}

async function toggleWish(wish) {
  try {
    const updated = await updateWish(wish.id, { completed: !wish.completed })
    const index = wishes.value.findIndex(w => w.id === wish.id)
    if (index > -1) {
      wishes.value[index] = updated
    }
  } catch (e) {
    console.error('更新心愿失败', e)
  }
}

async function deleteWish(id) {
  try {
    await deleteWishApi(id)
    const index = wishes.value.findIndex(w => w.id === id)
    if (index > -1) {
      wishes.value.splice(index, 1)
    }
  } catch (e) {
    console.error('删除心愿失败', e)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

function goBack() {
  router.push('/')
}

onMounted(loadWishes)
</script>

<style scoped>
.wishlist-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-top: 60px;
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
  max-width: 640px;
  margin: 0 auto;
  padding: 24px;
}

.add-wish-section {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-soft);
  margin-bottom: 24px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.wish-input {
  flex: 1;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 15px;
  background: var(--color-bg);
  color: var(--color-text);
  transition: all 0.3s ease;
}

.wish-input:focus {
  outline: none;
  border-color: var(--color-pink);
  box-shadow: 0 0 0 3px rgba(200, 160, 160, 0.1);
}

.wish-input::placeholder {
  color: var(--color-text-lighter);
}

.add-btn {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: var(--color-pink);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.add-btn:hover:not(:disabled) {
  background: var(--color-pink-dark);
  transform: scale(1.05);
}

.add-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.add-btn svg {
  width: 20px;
  height: 20px;
}

.stats-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 500;
  color: var(--color-title);
}

.stat-number.completed {
  color: var(--color-pink);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--color-text-light);
}

.stat-divider {
  color: var(--color-border);
}

.wishes-container {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: var(--color-border);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.empty-text {
  font-size: 1.1rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 0.9rem;
  color: var(--color-border);
}

.wishes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.wish-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  transition: all 0.3s ease;
}

.wish-item:hover {
  box-shadow: var(--shadow-soft);
}

.wish-item.completed {
  background: var(--color-pink-light);
}

.wish-item.completed .wish-content {
  text-decoration: line-through;
  color: var(--color-text-light);
}

.check-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.check-btn:hover {
  border-color: var(--color-pink);
}

.check-btn.checked {
  background: var(--color-pink);
  border-color: var(--color-pink);
}

.check-btn svg {
  width: 14px;
  height: 14px;
  color: white;
}

.wish-content {
  flex: 1;
  font-size: 15px;
  color: var(--color-title);
  line-height: 1.5;
}

.wish-date {
  font-size: 12px;
  color: var(--color-text-lighter);
  flex-shrink: 0;
}

.delete-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.wish-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(255, 107, 107, 0.1);
}

.delete-btn svg {
  width: 16px;
  height: 16px;
  color: #ff6b6b;
}

.wish-list-enter-active,
.wish-list-leave-active {
  transition: all 0.3s ease;
}

.wish-list-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.wish-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

@media (max-width: 768px) {
  .page-header {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 1.1rem;
    margin-right: 50px;
  }
  
  .page-content {
    padding: 16px;
  }
  
  .add-wish-section {
    padding: 16px;
  }
  
  .wish-input {
    padding: 12px 14px;
    font-size: 14px;
  }
  
  .add-btn {
    width: 44px;
    height: 44px;
  }
  
  .stats-bar {
    padding: 12px;
    gap: 12px;
  }
  
  .stat-number {
    font-size: 1.25rem;
  }
  
  .wish-item {
    padding: 14px;
  }
  
  .wish-content {
    font-size: 14px;
  }
  
  .delete-btn {
    opacity: 0.7;
  }
}

@media (max-width: 480px) {
  .wish-date {
    display: none;
  }
}
</style>
