<template>
  <div class="guestbook-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">留言板</h1>
    </div>
    
    <div class="page-content">
      <div class="guestbook-intro">
        <p class="intro-text">写下你想说的话，留下属于我们的印记</p>
      </div>
      
      <div class="message-form">
        <div class="form-group">
          <label class="form-label">你的名字</label>
          <input 
            v-model="author" 
            placeholder="匿名访客" 
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">留言内容</label>
          <textarea 
            v-model="content" 
            placeholder="在这里写下你想说的话..." 
            class="form-textarea"
            rows="4"
          ></textarea>
        </div>
        
        <button @click="submitMessage" class="submit-btn" :disabled="!content.trim()">
          <span class="btn-text">发送留言</span>
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
        </button>
      </div>
      
      <div class="messages-section">
        <h2 class="section-title">
          <span class="title-text">留言墙</span>
          <span class="title-count">{{ messages.length }} 条留言</span>
        </h2>
        
        <div class="message-list">
          <TransitionGroup name="message">
            <div 
              v-for="msg in messages" 
              :key="msg.id" 
              class="message-card"
            >
              <div class="message-header">
                <div class="author-info">
                  <div class="author-avatar">{{ getAvatarText(msg.author) }}</div>
                  <span class="author-name">{{ msg.author || '匿名访客' }}</span>
                </div>
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <p class="message-content">{{ msg.content }}</p>
            </div>
          </TransitionGroup>
          
          <div v-if="messages.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <p class="empty-text">还没有留言</p>
            <p class="empty-hint">成为第一个留言的人吧~</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMessages, createMessage } from '@/api/messages'

const router = useRouter()
const author = ref('')
const content = ref('')
const messages = ref([])

async function loadMessages() {
  try {
    const result = await getMessages(1, 50)
    messages.value = result.data || []
  } catch (e) {
    console.error('加载留言失败', e)
  }
}

async function submitMessage() {
  if (!content.value.trim()) return
  
  try {
    await createMessage({
      author: author.value.trim() || '匿名访客',
      content: content.value.trim()
    })
    content.value = ''
    await loadMessages()
  } catch (e) {
    console.error('发送失败', e)
  }
}

function formatTime(time) {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getAvatarText(name) {
  if (!name) return '匿'
  return name.charAt(0).toUpperCase()
}

function goBack() {
  router.push('/')
}

onMounted(loadMessages)
</script>

<style scoped>
.guestbook-page {
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
  max-width: 640px;
  margin: 0 auto;
  padding: 40px 24px;
}

.guestbook-intro {
  text-align: center;
  margin-bottom: 32px;
}

.intro-text {
  font-size: 1rem;
  color: var(--color-text-light);
  font-weight: 300;
  letter-spacing: 0.02em;
}

.message-form {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: 28px;
  box-shadow: var(--shadow-soft);
  margin-bottom: 40px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-of-type {
  margin-bottom: 24px;
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
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 15px;
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
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 15px;
  background: var(--color-bg);
  color: var(--color-text);
  resize: vertical;
  min-height: 120px;
  line-height: 1.6;
  transition: all 0.3s ease;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--color-pink);
  box-shadow: 0 0 0 3px rgba(200, 160, 160, 0.1);
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
  box-shadow: 0 4px 12px rgba(200, 160, 160, 0.3);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.messages-section {
  margin-top: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.title-text {
  font-size: 1.1rem;
  font-weight: 400;
  color: var(--color-title);
}

.title-count {
  font-size: 0.85rem;
  color: var(--color-text-light);
  font-weight: 300;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-soft);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-pink-light), var(--color-pink));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 400;
}

.author-name {
  font-size: 0.95rem;
  font-weight: 400;
  color: var(--color-title);
}

.message-time {
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.message-content {
  font-size: 0.95rem;
  color: var(--color-text);
  line-height: 1.7;
  margin: 0;
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

.message-enter-active,
.message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.message-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

@media (max-width: 768px) {
  .guestbook-page {
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
  
  .message-form {
    padding: 20px;
  }
  
  .message-card {
    padding: 16px;
  }
  
  .author-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
}
</style>
