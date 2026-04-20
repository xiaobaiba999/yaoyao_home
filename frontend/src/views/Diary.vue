<template>
  <div class="diary-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">日记</h1>
      <button class="diy-btn" @click="showDiyPanel = true" title="个性化设置">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="3"></circle>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
        </svg>
      </button>
    </div>
    
    <div class="page-content">
      <div class="diary-stats">
        <span class="stat-item">
          <span class="stat-number">{{ diaries.length }}</span>
          <span class="stat-label">篇日记</span>
        </span>
        <span class="stat-divider">|</span>
        <span class="stat-item">
          <span class="stat-number">{{ currentMonthCount }}</span>
          <span class="stat-label">本月</span>
        </span>
      </div>
      
      <div class="add-diary-section">
        <div class="form-group">
          <label class="form-label">标题</label>
          <input 
            v-model="newDiary.title" 
            type="text" 
            placeholder="给日记起个标题..."
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">内容</label>
          <textarea 
            v-model="newDiary.content" 
            placeholder="写下今天的故事..."
            class="form-textarea"
            :rows="diarySettings.textareaRows"
          ></textarea>
        </div>
        
        <div class="form-group">
          <label class="form-label">心情</label>
          <div class="mood-selector">
            <button 
              v-for="mood in moods" 
              :key="mood.value"
              class="mood-btn"
              :class="{ active: newDiary.mood === mood.value }"
              @click="newDiary.mood = mood.value"
            >
              {{ mood.emoji }}
            </button>
          </div>
        </div>
        
        <div class="form-group">
          <label class="form-label">日期</label>
          <input 
            v-model="newDiary.date" 
            type="date" 
            class="form-input"
          />
        </div>
        
        <button class="submit-btn" @click="addDiary" :disabled="!isFormValid">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>保存日记</span>
        </button>
      </div>
      
      <div class="diaries-container">
        <div v-if="diaries.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </div>
          <p class="empty-text">还没有日记</p>
          <p class="empty-hint">在上方写下你的第一篇日记吧</p>
        </div>
        
        <TransitionGroup v-else name="diary-list" tag="div" class="diaries-list">
          <div 
            v-for="diary in sortedDiaries" 
            :key="diary.id" 
            class="diary-card"
            :style="getDiaryStyle(diary)"
          >
            <div class="diary-header">
              <span class="diary-mood">{{ getMoodEmoji(diary.mood) }}</span>
              <span class="diary-date">{{ formatDate(diary.date) }}</span>
              <div class="diary-actions">
                <button class="action-btn edit" @click="startEdit(diary)" title="编辑">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>
                <button class="action-btn delete" @click="confirmDelete(diary.id)" title="删除">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </div>
            </div>
            <h3 class="diary-title">{{ diary.title }}</h3>
            <p class="diary-content">{{ diary.content }}</p>
          </div>
        </TransitionGroup>
      </div>
    </div>
    
    <Transition name="modal-fade">
      <div v-if="showEditModal" class="modal-overlay" @click="cancelEdit">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">编辑日记</h3>
            <button class="modal-close" @click="cancelEdit">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">标题</label>
              <input v-model="editDiary.title" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">内容</label>
              <textarea v-model="editDiary.content" class="form-textarea" :rows="diarySettings.textareaRows"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">心情</label>
              <div class="mood-selector">
                <button 
                  v-for="mood in moods" 
                  :key="mood.value"
                  class="mood-btn"
                  :class="{ active: editDiary.mood === mood.value }"
                  @click="editDiary.mood = mood.value"
                >
                  {{ mood.emoji }}
                </button>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">日期</label>
              <input v-model="editDiary.date" type="date" class="form-input" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="cancelEdit">取消</button>
            <button class="modal-btn confirm" @click="saveEdit" :disabled="!editDiary.title.trim()">保存</button>
          </div>
        </div>
      </div>
    </Transition>
    
    <Transition name="modal-fade">
      <div v-if="showDeleteConfirm" class="modal-overlay" @click="cancelDelete">
        <div class="confirm-dialog" @click.stop>
          <div class="confirm-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          </div>
          <h3 class="confirm-title">确认删除</h3>
          <p class="confirm-message">确定要删除这篇日记吗？此操作无法撤销。</p>
          <div class="confirm-actions">
            <button class="confirm-btn cancel" @click="cancelDelete">取消</button>
            <button class="confirm-btn delete" @click="executeDelete">确认删除</button>
          </div>
        </div>
      </div>
    </Transition>
    
    <Transition name="panel-slide">
      <div v-if="showDiyPanel" class="diy-panel">
        <div class="diy-header">
          <h3 class="diy-title">个性化设置</h3>
          <button class="diy-close" @click="showDiyPanel = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="diy-content">
          <div class="diy-section">
            <h4 class="diy-section-title">卡片样式</h4>
            <div class="diy-options">
              <label class="diy-option">
                <span class="option-label">卡片圆角</span>
                <input type="range" v-model="diarySettings.cardRadius" min="8" max="24" />
                <span class="option-value">{{ diarySettings.cardRadius }}px</span>
              </label>
              <label class="diy-option">
                <span class="option-label">卡片间距</span>
                <input type="range" v-model="diarySettings.cardGap" min="12" max="32" />
                <span class="option-value">{{ diarySettings.cardGap }}px</span>
              </label>
            </div>
          </div>
          
          <div class="diy-section">
            <h4 class="diy-section-title">编辑器设置</h4>
            <div class="diy-options">
              <label class="diy-option">
                <span class="option-label">文本框行数</span>
                <input type="range" v-model="diarySettings.textareaRows" min="3" max="10" />
                <span class="option-value">{{ diarySettings.textareaRows }}行</span>
              </label>
            </div>
          </div>
          
          <div class="diy-section">
            <h4 class="diy-section-title">主题颜色</h4>
            <div class="color-options">
              <button 
                v-for="color in themeColors" 
                :key="color.value"
                class="color-btn"
                :class="{ active: diarySettings.themeColor === color.value }"
                :style="{ background: color.value }"
                @click="diarySettings.themeColor = color.value"
              >
                <span v-if="diarySettings.themeColor === color.value" class="check">✓</span>
              </button>
            </div>
          </div>
          
          <button class="diy-save-btn" @click="saveDiySettings">保存设置</button>
        </div>
      </div>
    </Transition>
    
    <Transition name="overlay-fade">
      <div v-if="showDiyPanel" class="diy-overlay" @click="showDiyPanel = false"></div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDiaries, createDiary, updateDiary, deleteDiary as deleteDiaryApi } from '@/api/diaries'

const router = useRouter()
const DIY_STORAGE_KEY = 'diary_settings'

const diaries = ref([])
const showEditModal = ref(false)
const showDeleteConfirm = ref(false)
const showDiyPanel = ref(false)
const deleteTargetId = ref(null)
const editingId = ref(null)

const moods = [
  { value: 'happy', emoji: '😊' },
  { value: 'love', emoji: '🥰' },
  { value: 'excited', emoji: '🤩' },
  { value: 'calm', emoji: '😌' },
  { value: 'sad', emoji: '😢' },
  { value: 'angry', emoji: '😤' }
]

const themeColors = [
  { value: '#C8A0A0' },
  { value: '#A0C8B8' },
  { value: '#B8A0C8' },
  { value: '#C8B8A0' },
  { value: '#A0B8C8' },
  { value: '#C8A0B8' }
]

const defaultSettings = {
  cardRadius: 16,
  cardGap: 16,
  textareaRows: 5,
  themeColor: '#C8A0A0'
}

const diarySettings = ref({ ...defaultSettings })

const newDiary = ref({
  title: '',
  content: '',
  mood: 'happy',
  date: new Date().toISOString().split('T')[0]
})

const editDiary = ref({
  title: '',
  content: '',
  mood: 'happy',
  date: ''
})

const isFormValid = computed(() => newDiary.value.title.trim() && newDiary.value.content.trim())

const sortedDiaries = computed(() => {
  return [...diaries.value].sort((a, b) => new Date(b.date) - new Date(a.date))
})

const currentMonthCount = computed(() => {
  const now = new Date()
  const currentMonth = now.getMonth()
  const currentYear = now.getFullYear()
  return diaries.value.filter(d => {
    const date = new Date(d.date)
    return date.getMonth() === currentMonth && date.getFullYear() === currentYear
  }).length
})

async function loadDiaries() {
  try {
    diaries.value = await getDiaries()
  } catch (e) {
    console.error('加载日记失败', e)
    diaries.value = []
  }
}

function loadDiySettings() {
  try {
    const stored = localStorage.getItem(DIY_STORAGE_KEY)
    if (stored) {
      diarySettings.value = { ...defaultSettings, ...JSON.parse(stored) }
    }
  } catch (e) {
    console.error('加载设置失败', e)
  }
}

function saveDiySettings() {
  try {
    localStorage.setItem(DIY_STORAGE_KEY, JSON.stringify(diarySettings.value))
    showDiyPanel.value = false
  } catch (e) {
    console.error('保存设置失败', e)
  }
}

async function addDiary() {
  if (!isFormValid.value) return
  
  try {
    const diary = await createDiary({
      title: newDiary.value.title.trim(),
      content: newDiary.value.content.trim(),
      mood: newDiary.value.mood,
      date: newDiary.value.date
    })
    diaries.value.unshift(diary)
    newDiary.value = {
      title: '',
      content: '',
      mood: 'happy',
      date: new Date().toISOString().split('T')[0]
    }
  } catch (e) {
    console.error('创建日记失败', e)
  }
}

function startEdit(diary) {
  editingId.value = diary.id
  editDiary.value = {
    title: diary.title,
    content: diary.content,
    mood: diary.mood,
    date: diary.date
  }
  showEditModal.value = true
}

function cancelEdit() {
  showEditModal.value = false
  editingId.value = null
}

async function saveEdit() {
  if (!editDiary.value.title.trim()) return
  
  try {
    const updated = await updateDiary(editingId.value, {
      title: editDiary.value.title.trim(),
      content: editDiary.value.content.trim(),
      mood: editDiary.value.mood,
      date: editDiary.value.date
    })
    const index = diaries.value.findIndex(d => d.id === editingId.value)
    if (index > -1) {
      diaries.value[index] = updated
    }
  } catch (e) {
    console.error('更新日记失败', e)
  }
  
  cancelEdit()
}

function confirmDelete(id) {
  deleteTargetId.value = id
  showDeleteConfirm.value = true
}

function cancelDelete() {
  showDeleteConfirm.value = false
  deleteTargetId.value = null
}

async function executeDelete() {
  if (!deleteTargetId.value) return
  
  try {
    await deleteDiaryApi(deleteTargetId.value)
    const index = diaries.value.findIndex(d => d.id === deleteTargetId.value)
    if (index > -1) {
      diaries.value.splice(index, 1)
    }
  } catch (e) {
    console.error('删除日记失败', e)
  }
  
  cancelDelete()
}

function getMoodEmoji(mood) {
  const found = moods.find(m => m.value === mood)
  return found ? found.emoji : '😊'
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${year}年${month}月${day}日 ${weekdays[date.getDay()]}`
}

function getDiaryStyle(diary) {
  return {
    '--card-radius': `${diarySettings.value.cardRadius}px`,
    '--theme-color': diarySettings.value.themeColor
  }
}

function goBack() {
  router.push('/')
}

onMounted(() => {
  loadDiaries()
  loadDiySettings()
})
</script>

<style scoped>
.diary-page {
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
  justify-content: space-between;
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
  font-size: 1.25rem;
  font-weight: 400;
  color: var(--color-title);
  letter-spacing: 0.05em;
}

.diy-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.diy-btn:hover {
  background: var(--color-pink-light);
}

.diy-btn svg {
  width: 20px;
  height: 20px;
  color: var(--color-text-light);
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.diary-stats {
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

.stat-label {
  font-size: 0.9rem;
  color: var(--color-text-light);
}

.stat-divider {
  color: var(--color-border);
}

.add-diary-section {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-soft);
  margin-bottom: 32px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 0.85rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
  font-weight: 300;
}

.form-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--color-bg);
  color: var(--color-text);
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-pink);
  box-shadow: 0 0 0 3px rgba(200, 160, 160, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--color-bg);
  color: var(--color-text);
  resize: vertical;
  min-height: 100px;
  line-height: 1.6;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--color-pink);
  box-shadow: 0 0 0 3px rgba(200, 160, 160, 0.1);
}

.mood-selector {
  display: flex;
  gap: 8px;
}

.mood-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-bg);
  border: 2px solid var(--color-border);
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mood-btn:hover {
  border-color: var(--color-pink);
  transform: scale(1.1);
}

.mood-btn.active {
  border-color: var(--color-pink);
  background: var(--color-pink-light);
}

.submit-btn {
  width: 100%;
  padding: 14px 24px;
  background: var(--color-pink);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-pink-dark);
  transform: translateY(-2px);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn svg {
  width: 18px;
  height: 18px;
}

.diaries-container {
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
  font-size: 1rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 0.85rem;
  color: var(--color-border);
}

.diaries-list {
  display: flex;
  flex-direction: column;
  gap: v-bind('diarySettings.cardGap + "px"');
}

.diary-card {
  background: var(--color-bg-secondary);
  border-radius: var(--card-radius, 16px);
  padding: 20px;
  box-shadow: var(--shadow-soft);
  transition: all 0.3s ease;
  border-left: 4px solid var(--theme-color, var(--color-pink));
}

.diary-card:hover {
  box-shadow: var(--shadow-medium);
  transform: translateX(4px);
}

.diary-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.diary-mood {
  font-size: 24px;
}

.diary-date {
  font-size: 0.85rem;
  color: var(--color-text-light);
  flex: 1;
}

.diary-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.diary-card:hover .diary-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

.action-btn.edit {
  color: var(--color-text-light);
}

.action-btn.edit:hover {
  background: rgba(91, 155, 213, 0.1);
  color: #5b9bd5;
}

.action-btn.delete {
  color: var(--color-text-light);
}

.action-btn.delete:hover {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.diary-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-title);
  margin-bottom: 8px;
}

.diary-content {
  font-size: 0.95rem;
  color: var(--color-text);
  line-height: 1.7;
  white-space: pre-wrap;
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
  padding: 20px;
}

.modal-dialog {
  background: white;
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-title);
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: var(--color-bg-tertiary);
}

.modal-close svg {
  width: 20px;
  height: 20px;
  color: var(--color-text);
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--color-border);
  justify-content: flex-end;
}

.modal-btn {
  padding: 10px 20px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.modal-btn.cancel {
  background: var(--color-bg-tertiary);
  color: var(--color-text);
}

.modal-btn.cancel:hover {
  background: var(--color-border);
}

.modal-btn.confirm {
  background: var(--color-pink);
  color: white;
}

.modal-btn.confirm:hover:not(:disabled) {
  background: var(--color-pink-dark);
}

.modal-btn.confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.confirm-dialog {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  max-width: 360px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.confirm-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  color: #ff6b6b;
}

.confirm-icon svg {
  width: 100%;
  height: 100%;
}

.confirm-title {
  font-size: 1.25rem;
  font-weight: 500;
  color: var(--color-title);
  margin-bottom: 8px;
}

.confirm-message {
  font-size: 0.95rem;
  color: var(--color-text-light);
  margin-bottom: 24px;
  line-height: 1.6;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-btn {
  padding: 10px 24px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.confirm-btn.cancel {
  background: var(--color-bg-tertiary);
  color: var(--color-text);
}

.confirm-btn.cancel:hover {
  background: var(--color-border);
}

.confirm-btn.delete {
  background: #ff6b6b;
  color: white;
}

.confirm-btn.delete:hover {
  background: #ee5a5a;
}

.diy-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 320px;
  height: 100vh;
  background: var(--color-bg-secondary);
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.1);
  z-index: 1001;
  display: flex;
  flex-direction: column;
}

.diy-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.diy-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-title);
}

.diy-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.diy-close:hover {
  background: var(--color-bg-tertiary);
}

.diy-close svg {
  width: 20px;
  height: 20px;
  color: var(--color-text);
}

.diy-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.diy-section {
  margin-bottom: 24px;
}

.diy-section-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-title);
  margin-bottom: 12px;
}

.diy-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.diy-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-label {
  flex: 1;
  font-size: 0.85rem;
  color: var(--color-text);
}

.option-value {
  font-size: 0.85rem;
  color: var(--color-text-light);
  min-width: 40px;
  text-align: right;
}

.diy-option input[type="range"] {
  width: 100px;
  accent-color: var(--color-pink);
}

.color-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.color-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-btn:hover {
  transform: scale(1.1);
}

.color-btn.active {
  border-color: var(--color-text);
}

.color-btn .check {
  color: white;
  font-size: 14px;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.diy-save-btn {
  width: 100%;
  padding: 12px;
  background: var(--color-pink);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 20px;
}

.diy-save-btn:hover {
  background: var(--color-pink-dark);
}

.diy-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

.diary-list-enter-active,
.diary-list-leave-active {
  transition: all 0.3s ease;
}

.diary-list-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.diary-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.modal-fade-enter-active,
.modal-fade-leave-active,
.overlay-fade-enter-active,
.overlay-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to,
.overlay-fade-enter-from,
.overlay-fade-leave-to {
  opacity: 0;
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: transform 0.3s ease;
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  transform: translateX(100%);
}

@media (max-width: 768px) {
  .page-header {
    padding: 0 16px;
  }
  
  .page-content {
    padding: 16px;
  }
  
  .diary-panel {
    width: 100%;
  }
  
  .modal-dialog {
    margin: 16px;
  }
}

@media (max-width: 480px) {
  .diary-stats {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .stat-item {
    flex: 1;
    justify-content: center;
  }
}
</style>
