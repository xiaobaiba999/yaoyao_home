<template>
  <div class="auth-prompt-overlay" v-if="visible">
    <div class="auth-prompt-card">
      <div class="auth-icon">🔐</div>
      <h3 class="auth-title">{{ title }}</h3>
      <p class="auth-desc">{{ description }}</p>
      <div class="auth-permissions" v-if="permissions.length">
        <p class="permissions-title">将获取以下权限：</p>
        <ul class="permissions-list">
          <li v-for="perm in permissions" :key="perm">{{ perm }}</li>
        </ul>
      </div>
      <div class="auth-actions">
        <button @click="handleReject" class="reject-btn">拒绝</button>
        <button @click="handleAccept" class="accept-btn">同意授权</button>
      </div>
      <p class="privacy-link">
        授权即表示同意 <a href="/privacy" target="_blank">隐私政策</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '授权请求'
  },
  description: {
    type: String,
    default: '我们需要您的授权以提供更好的服务'
  },
  permissions: {
    type: Array,
    default: () => ['获取您的公开信息（昵称、头像）']
  }
})

const emit = defineEmits(['accept', 'reject'])

function handleAccept() {
  emit('accept')
}

function handleReject() {
  emit('reject')
}
</script>

<style scoped>
.auth-prompt-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.auth-prompt-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  max-width: 320px;
  width: 100%;
  text-align: center;
}

.auth-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.auth-title {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px 0;
}

.auth-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 20px 0;
}

.auth-permissions {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  text-align: left;
}

.permissions-title {
  font-size: 13px;
  color: #333;
  margin: 0 0 10px 0;
}

.permissions-list {
  margin: 0;
  padding-left: 20px;
}

.permissions-list li {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.auth-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
}

.reject-btn, .accept-btn {
  flex: 1;
  padding: 12px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reject-btn {
  background: #f0f0f0;
  border: none;
  color: #666;
}

.accept-btn {
  background: #07c160;
  border: none;
  color: white;
}

.accept-btn:hover {
  background: #06ad56;
}

.privacy-link {
  font-size: 11px;
  color: #999;
  margin: 0;
}

.privacy-link a {
  color: #07c160;
  text-decoration: none;
}
</style>
