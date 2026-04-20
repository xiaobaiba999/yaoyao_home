<template>
  <div class="message-board">
    <h3 class="section-title">💬 留言板</h3>
    
    <div class="message-form">
      <input 
        v-model="author" 
        placeholder="你的名字" 
        class="author-input"
      />
      <textarea 
        v-model="content" 
        placeholder="写下你想说的话..." 
        class="content-input"
        rows="3"
      ></textarea>
      <button @click="submitMessage" class="submit-btn" :disabled="!content.trim()">
        发送 💕
      </button>
    </div>
    
    <div class="message-list">
      <div v-for="msg in messages" :key="msg.id" class="message-item">
        <div class="message-header">
          <span class="message-author">{{ msg.author }}</span>
          <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
        </div>
        <p class="message-content">{{ msg.content }}</p>
      </div>
      
      <div v-if="hasMore" class="load-more">
        <button @click="loadMore" class="load-more-btn">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMessages, createMessage } from '@/api/messages'

const messages = ref([])
const author = ref('')
const content = ref('')
const page = ref(1)
const hasMore = ref(false)

async function loadMessages() {
  try {
    const result = await getMessages(page.value)
    if (page.value === 1) {
      messages.value = result.data
    } else {
      messages.value.push(...result.data)
    }
    hasMore.value = result.hasMore
  } catch (e) {
    console.error('加载留言失败', e)
  }
}

async function submitMessage() {
  if (!content.value.trim()) return
  
  try {
    await createMessage({
      content: content.value,
      author: author.value || '匿名'
    })
    content.value = ''
    page.value = 1
    await loadMessages()
  } catch (e) {
    console.error('发送失败', e)
  }
}

function loadMore() {
  page.value++
  loadMessages()
}

function formatTime(dateStr) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(loadMessages)
</script>

<style scoped>
.message-board {
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  margin: 20px 0;
}

.section-title {
  color: white;
  font-size: 1.5rem;
  margin-bottom: 15px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.message-form {
  margin-bottom: 20px;
}

.author-input {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 10px;
  margin-bottom: 10px;
  font-size: 14px;
}

.content-input {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 10px;
  margin-bottom: 10px;
  font-size: 14px;
  resize: none;
}

.submit-btn {
  padding: 10px 25px;
  background: linear-gradient(135deg, #ff6b9d 0%, #e74c3c 100%);
  border: none;
  border-radius: 20px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.message-list {
  max-height: 400px;
  overflow-y: auto;
}

.message-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 10px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.message-author {
  font-weight: bold;
  color: #ff6b9d;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  color: #333;
  margin: 0;
  line-height: 1.5;
}

.load-more {
  text-align: center;
  padding: 10px;
}

.load-more-btn {
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 15px;
  color: white;
  cursor: pointer;
}
</style>
