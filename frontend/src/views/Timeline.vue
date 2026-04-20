<template>
  <div class="timeline-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">我们的故事</h1>
    </div>
    
    <div class="page-content">
      <div class="timeline-container">
        <div class="timeline-line"></div>
        <div 
          v-for="(item, index) in timelineData" 
          :key="item.id || index" 
          class="timeline-item"
          :style="{ '--delay': index * 0.15 + 's' }"
        >
          <div class="timeline-node"></div>
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="timeline-date">{{ item.date }}</span>
              <span class="timeline-title">{{ item.title }}</span>
              <div class="timeline-actions" v-if="isLoggedIn">
                <button class="edit-btn" @click="editItem(item)">编辑</button>
                <button class="delete-btn" @click="deleteItem(item.id)">删除</button>
              </div>
            </div>
            <p class="timeline-description">{{ item.description }}</p>
          </div>
        </div>
      </div>
      
      <div class="add-timeline-btn" v-if="isLoggedIn" @click="showAddModal = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>添加新事件</span>
      </div>
    </div>

    <div class="modal-overlay" v-if="showAddModal || editingItem" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>{{ editingItem ? '编辑事件' : '添加事件' }}</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>日期</label>
            <input type="date" v-model="formData.date" required />
          </div>
          <div class="form-group">
            <label>标题</label>
            <input type="text" v-model="formData.title" required placeholder="事件标题" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="formData.description" required placeholder="事件描述"></textarea>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="closeModal">取消</button>
            <button type="submit" class="save-btn">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getTimeline, addTimelineItem, updateTimelineItem, deleteTimelineItem } from '@/api/timeline'

const router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isAuthenticated)

const timelineData = ref([])

const showAddModal = ref(false)
const editingItem = ref(null)
const formData = ref({
  date: '',
  title: '',
  description: ''
})

async function loadTimeline() {
  try {
    timelineData.value = await getTimeline()
  } catch (e) {
    console.error('加载时间线失败', e)
  }
}

function goBack() {
  router.push('/')
}

function editItem(item) {
  editingItem.value = item
  formData.value = {
    date: item.date.replace(/\./g, '-'),
    title: item.title,
    description: item.description
  }
}

function closeModal() {
  showAddModal.value = false
  editingItem.value = null
  formData.value = { date: '', title: '', description: '' }
}

async function saveItem() {
  const formattedDate = formData.value.date.replace(/-/g, '.')
  const itemData = {
    date: formattedDate,
    title: formData.value.title,
    description: formData.value.description
  }

  try {
    if (editingItem.value) {
      await updateTimelineItem(editingItem.value.id, itemData)
    } else {
      await addTimelineItem(itemData)
    }
    await loadTimeline()
    closeModal()
  } catch (e) {
    console.error('保存失败', e)
  }
}

async function deleteItem(id) {
  if (!confirm('确定要删除这个事件吗？')) return
  
  try {
    await deleteTimelineItem(id)
    timelineData.value = timelineData.value.filter(item => item.id !== id)
  } catch (e) {
    console.error('删除失败', e)
  }
}

onMounted(() => {
  authStore.checkAuth()
  loadTimeline()
})
</script>

<style scoped>
.timeline-page {
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
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 24px;
}

.timeline-container {
  position: relative;
  padding-left: 30px;
}

.timeline-line {
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--color-border);
}

.timeline-item {
  position: relative;
  padding-bottom: 40px;
  animation: fadeInUp 0.6s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.timeline-node {
  position: absolute;
  left: -24px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-pink);
  box-shadow: 0 0 0 4px var(--color-bg);
}

.timeline-content {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  box-shadow: var(--shadow-soft);
  transition: all 0.3s ease;
}

.timeline-content:hover {
  box-shadow: var(--shadow-medium);
  transform: translateX(4px);
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.timeline-date {
  font-size: 0.85rem;
  color: var(--color-text-light);
  font-weight: 300;
}

.timeline-title {
  font-size: 1.1rem;
  font-weight: 400;
  color: var(--color-title);
}

.timeline-description {
  font-size: 0.95rem;
  color: var(--color-text);
  line-height: 1.7;
  margin: 0;
}

.add-timeline-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 40px;
  padding: 24px;
  background: var(--color-bg-secondary);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-timeline-btn:hover {
  border-color: var(--color-pink);
  background: var(--color-pink-light);
}

.add-timeline-btn svg {
  width: 32px;
  height: 32px;
  color: var(--color-pink);
}

.add-timeline-btn span {
  font-size: 0.9rem;
  color: var(--color-text-light);
}

.timeline-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.edit-btn, .delete-btn {
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-btn {
  background: #e3f2fd;
  color: #1976d2;
}

.edit-btn:hover {
  background: #bbdefb;
}

.delete-btn {
  background: #ffebee;
  color: #d32f2f;
}

.delete-btn:hover {
  background: #ffcdd2;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  width: 90%;
  max-width: 400px;
  box-shadow: var(--shadow-medium);
}

.modal-content h3 {
  margin: 0 0 20px 0;
  color: var(--color-title);
  font-size: 1.25rem;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.9rem;
  color: var(--color-text);
}

.form-group input, .form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.95rem;
  transition: border-color 0.2s ease;
}

.form-group input:focus, .form-group textarea:focus {
  outline: none;
  border-color: var(--color-pink);
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.cancel-btn, .save-btn {
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: #f5f5f5;
  color: var(--color-text);
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.save-btn {
  background: var(--color-pink);
  color: white;
}

.save-btn:hover {
  background: var(--color-pink-dark);
}

@media (max-width: 768px) {
  .timeline-page {
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
  
  .timeline-container {
    padding-left: 24px;
  }
  
  .timeline-line {
    left: 4px;
  }
  
  .timeline-node {
    left: -20px;
    width: 10px;
    height: 10px;
  }
  
  .timeline-content {
    padding: 16px 20px;
  }
  
  .timeline-title {
    font-size: 1rem;
  }
  
  .timeline-description {
    font-size: 0.9rem;
  }
  
  .add-timeline-btn {
    padding: 20px;
  }
}
</style>
