<template>
  <div class="future-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">未来计划</h1>
    </div>
    
    <div class="page-content">
      <div class="add-plan-section">
        <div class="section-header">
          <h2 class="section-title">创建新计划</h2>
          <button class="toggle-form-btn" @click="showForm = !showForm">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ rotated: showForm }">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>
        </div>
        
        <Transition name="form-slide">
          <div v-if="showForm" class="plan-form">
            <div class="form-group">
              <label class="form-label">计划标题</label>
              <input 
                v-model="newPlan.title" 
                type="text" 
                placeholder="输入计划标题..."
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">计划描述</label>
              <textarea 
                v-model="newPlan.description" 
                placeholder="描述你的计划..."
                class="form-textarea"
                rows="3"
              ></textarea>
            </div>
            
            <div class="form-group">
              <label class="form-label">目标日期</label>
              <input 
                v-model="newPlan.targetDate" 
                type="date" 
                class="form-input"
              />
            </div>
            
            <button class="submit-btn" @click="addPlan" :disabled="!isFormValid">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              <span>创建计划</span>
            </button>
          </div>
        </Transition>
      </div>
      
      <div class="stats-bar">
        <span class="stat-item">
          <span class="stat-number">{{ totalPlans }}</span>
          <span class="stat-label">个计划</span>
        </span>
        <span class="stat-divider">|</span>
        <span class="stat-item">
          <span class="stat-number in-progress">{{ inProgressPlans }}</span>
          <span class="stat-label">进行中</span>
        </span>
        <span class="stat-divider">|</span>
        <span class="stat-item">
          <span class="stat-number completed">{{ completedPlans }}</span>
          <span class="stat-label">已完成</span>
        </span>
      </div>
      
      <div class="plans-container">
        <div v-if="plans.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </div>
          <p class="empty-text">还没有计划</p>
          <p class="empty-hint">点击上方创建你的第一个未来计划</p>
        </div>
        
        <TransitionGroup v-else name="plan-list" tag="div" class="plans-grid">
          <div 
            v-for="plan in sortedPlans" 
            :key="plan.id" 
            class="plan-card"
            :class="{ completed: plan.status === 'completed' }"
          >
            <div class="card-header">
              <button 
                class="status-btn"
                :class="{ completed: plan.status === 'completed' }"
                @click="toggleStatus(plan.id)"
                :title="plan.status === 'completed' ? '标记为进行中' : '标记为已完成'"
              >
                <svg v-if="plan.status === 'completed'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </button>
              <h3 class="card-title">{{ plan.title }}</h3>
              <div class="card-actions">
                <button class="action-btn edit" @click="startEdit(plan)" title="编辑">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>
                <button class="action-btn delete" @click="confirmDelete(plan.id)" title="删除">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                  </svg>
                </button>
              </div>
            </div>
            
            <p class="card-description">{{ plan.description || '暂无描述' }}</p>
            
            <div class="card-footer">
              <div class="date-info">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span>{{ formatDate(plan.target_date) }}</span>
              </div>
              <span class="status-tag" :class="plan.status === 'completed' ? 'completed' : 'in-progress'">
                {{ plan.status === 'completed' ? '已完成' : '进行中' }}
              </span>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </div>
    
    <Transition name="modal-fade">
      <div v-if="showEditModal" class="modal-overlay" @click="cancelEdit">
        <div class="modal-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">编辑计划</h3>
            <button class="modal-close" @click="cancelEdit">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">计划标题</label>
              <input v-model="editPlan.title" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">计划描述</label>
              <textarea v-model="editPlan.description" class="form-textarea" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">目标日期</label>
              <input v-model="editPlan.targetDate" type="date" class="form-input" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="cancelEdit">取消</button>
            <button class="modal-btn confirm" @click="saveEdit" :disabled="!editPlan.title.trim()">保存</button>
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
          <p class="confirm-message">确定要删除这个计划吗？此操作无法撤销。</p>
          <div class="confirm-actions">
            <button class="confirm-btn cancel" @click="cancelDelete">取消</button>
            <button class="confirm-btn delete" @click="executeDelete">确认删除</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPlans, createPlan, updatePlan, deletePlan as deletePlanApi } from '@/api/plans'

const router = useRouter()
const plans = ref([])
const showForm = ref(true)
const showEditModal = ref(false)
const showDeleteConfirm = ref(false)
const deleteTargetId = ref(null)
const editingId = ref(null)

const newPlan = ref({
  title: '',
  description: '',
  targetDate: ''
})

const editPlan = ref({
  title: '',
  description: '',
  targetDate: ''
})

const isFormValid = computed(() => newPlan.value.title.trim())

const totalPlans = computed(() => plans.value.length)
const completedPlans = computed(() => plans.value.filter(p => p.status === 'completed').length)
const inProgressPlans = computed(() => plans.value.filter(p => p.status !== 'completed').length)

const sortedPlans = computed(() => {
  return [...plans.value].sort((a, b) => {
    const aCompleted = a.status === 'completed'
    const bCompleted = b.status === 'completed'
    if (aCompleted !== bCompleted) {
      return aCompleted ? 1 : -1
    }
    return new Date(a.target_date) - new Date(b.target_date)
  })
})

async function loadPlans() {
  try {
    plans.value = await getPlans()
  } catch (e) {
    console.error('加载计划失败', e)
    plans.value = []
  }
}

async function addPlan() {
  if (!isFormValid.value) return
  
  try {
    const plan = await createPlan({
      title: newPlan.value.title.trim(),
      description: newPlan.value.description.trim(),
      target_date: newPlan.value.targetDate || null
    })
    plans.value.unshift(plan)
    newPlan.value = { title: '', description: '', targetDate: '' }
  } catch (e) {
    console.error('创建计划失败', e)
  }
}

async function toggleStatus(id) {
  const plan = plans.value.find(p => p.id === id)
  if (plan) {
    try {
      const newStatus = plan.status === 'completed' ? 'pending' : 'completed'
      const updated = await updatePlan(id, { status: newStatus })
      const index = plans.value.findIndex(p => p.id === id)
      if (index > -1) {
        plans.value[index] = updated
      }
    } catch (e) {
      console.error('更新状态失败', e)
    }
  }
}

function startEdit(plan) {
  editingId.value = plan.id
  editPlan.value = {
    title: plan.title,
    description: plan.description,
    targetDate: plan.target_date
  }
  showEditModal.value = true
}

function cancelEdit() {
  showEditModal.value = false
  editingId.value = null
  editPlan.value = { title: '', description: '', targetDate: '' }
}

async function saveEdit() {
  if (!editPlan.value.title.trim()) return
  
  try {
    const updated = await updatePlan(editingId.value, {
      title: editPlan.value.title.trim(),
      description: editPlan.value.description.trim(),
      target_date: editPlan.value.targetDate || null
    })
    const index = plans.value.findIndex(p => p.id === editingId.value)
    if (index > -1) {
      plans.value[index] = updated
    }
  } catch (e) {
    console.error('更新计划失败', e)
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
    await deletePlanApi(deleteTargetId.value)
    const index = plans.value.findIndex(p => p.id === deleteTargetId.value)
    if (index > -1) {
      plans.value.splice(index, 1)
    }
  } catch (e) {
    console.error('删除计划失败', e)
  }
  
  cancelDelete()
}

function formatDate(dateStr) {
  if (!dateStr) return '未设定'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month}月${day}日`
}

function goBack() {
  router.push('/')
}

onMounted(loadPlans)
</script>

<style scoped>
.future-page {
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
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.add-plan-section {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-soft);
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-title);
}

.toggle-form-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-bg-tertiary);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-form-btn:hover {
  background: var(--color-pink-light);
}

.toggle-form-btn svg {
  width: 18px;
  height: 18px;
  color: var(--color-text);
  transition: transform 0.3s ease;
}

.toggle-form-btn svg.rotated {
  transform: rotate(180deg);
}

.plan-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text);
}

.form-input,
.form-textarea {
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--color-bg);
  color: var(--color-text);
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-pink);
  box-shadow: 0 0 0 3px rgba(200, 160, 160, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--color-pink);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  align-self: flex-end;
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

.stat-number.in-progress {
  color: #5b9bd5;
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

.plans-container {
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

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.plan-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-xs);
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.plan-card:hover {
  box-shadow: var(--shadow-soft);
  transform: translateY(-2px);
}

.plan-card.completed {
  background: var(--color-pink-light);
  border-color: var(--color-pink);
}

.plan-card.completed .card-title {
  text-decoration: line-through;
  color: var(--color-text-light);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.status-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-top: 2px;
}

.status-btn:hover {
  border-color: var(--color-pink);
}

.status-btn.completed {
  background: var(--color-pink);
  border-color: var(--color-pink);
}

.status-btn svg {
  width: 12px;
  height: 12px;
  color: white;
}

.card-title {
  flex: 1;
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-title);
  line-height: 1.4;
}

.card-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.plan-card:hover .card-actions {
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

.card-description {
  font-size: 0.9rem;
  color: var(--color-text-light);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--color-text-lighter);
}

.date-info svg {
  width: 16px;
  height: 16px;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-tag.in-progress {
  background: rgba(91, 155, 213, 0.1);
  color: #5b9bd5;
}

.status-tag.completed {
  background: rgba(200, 160, 160, 0.2);
  color: var(--color-pink-dark);
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
  max-width: 480px;
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
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.form-slide-enter-active,
.form-slide-leave-active {
  transition: all 0.3s ease;
}

.form-slide-enter-from,
.form-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.plan-list-enter-active,
.plan-list-leave-active {
  transition: all 0.3s ease;
}

.plan-list-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

.plan-list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
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
  
  .add-plan-section {
    padding: 16px;
  }
  
  .stats-bar {
    padding: 12px;
    gap: 12px;
  }
  
  .stat-number {
    font-size: 1.25rem;
  }
  
  .plans-grid {
    grid-template-columns: 1fr;
  }
  
  .card-actions {
    opacity: 1;
  }
  
  .modal-dialog {
    margin: 16px;
  }
}

@media (max-width: 480px) {
  .stats-bar {
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
