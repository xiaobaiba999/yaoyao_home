<template>
  <Transition name="preloader-fade">
    <div v-if="visible" class="preloader">
      <div class="preloader-content">
        <svg 
          class="heart-icon" 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
        </svg>
        <p v-if="showText" class="loading-text">{{ text }}</p>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  modelValue: {
    type: Boolean,
    default: undefined
  },
  text: {
    type: String,
    default: '加载中...'
  },
  showText: {
    type: Boolean,
    default: true
  }
})

const visible = computed(() => {
  if (props.modelValue !== undefined) {
    return props.modelValue
  }
  return props.loading
})
</script>

<style scoped>
.preloader {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.preloader-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.heart-icon {
  width: 48px;
  height: 48px;
  color: var(--color-accent);
  animation: heartbeat 1.5s ease-in-out infinite, breathe 3s ease-in-out infinite;
}

.loading-text {
  font-size: 14px;
  color: var(--color-text-light);
  letter-spacing: 2px;
  animation: textPulse 2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% {
    transform: scale(1);
  }
  15% {
    transform: scale(1.12);
  }
  30% {
    transform: scale(1);
  }
  45% {
    transform: scale(1.08);
  }
  60% {
    transform: scale(1);
  }
}

@keyframes breathe {
  0%, 100% {
    opacity: 0.7;
  }
  50% {
    opacity: 1;
  }
}

@keyframes textPulse {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

.preloader-fade-enter-active {
  animation: fadeIn 0.3s ease;
}

.preloader-fade-leave-active {
  animation: fadeOut 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
</style>
