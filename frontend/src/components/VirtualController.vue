<template>
  <div class="virtual-controller" v-if="isMobile">
    <div class="controller-left">
      <div class="dpad">
        <button 
          class="dpad-btn up" 
          @touchstart.prevent="handlePress('up')"
          @touchend.prevent="handleRelease('up')"
          @mousedown="handlePress('up')"
          @mouseup="handleRelease('up')"
          @mouseleave="handleRelease('up')"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 4l-8 8h16z"/>
          </svg>
        </button>
        <button 
          class="dpad-btn down" 
          @touchstart.prevent="handlePress('down')"
          @touchend.prevent="handleRelease('down')"
          @mousedown="handlePress('down')"
          @mouseup="handleRelease('down')"
          @mouseleave="handleRelease('down')"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 20l8-8H4z"/>
          </svg>
        </button>
        <button 
          class="dpad-btn left" 
          @touchstart.prevent="handlePress('left')"
          @touchend.prevent="handleRelease('left')"
          @mousedown="handlePress('left')"
          @mouseup="handleRelease('left')"
          @mouseleave="handleRelease('left')"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M4 12l8-8v16z"/>
          </svg>
        </button>
        <button 
          class="dpad-btn right" 
          @touchstart.prevent="handlePress('right')"
          @touchend.prevent="handleRelease('right')"
          @mousedown="handlePress('right')"
          @mouseup="handleRelease('right')"
          @mouseleave="handleRelease('right')"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 12l-8 8V4z"/>
          </svg>
        </button>
        <div class="dpad-center"></div>
      </div>
    </div>
    
    <div class="controller-right">
      <button 
        class="action-btn a-btn" 
        @touchstart.prevent="handlePress('action')"
        @touchend.prevent="handleRelease('action')"
        @mousedown="handlePress('action')"
        @mouseup="handleRelease('action')"
      >
        A
      </button>
      <button 
        class="action-btn b-btn" 
        @touchstart.prevent="handlePress('cancel')"
        @touchend.prevent="handleRelease('cancel')"
        @mousedown="handlePress('cancel')"
        @mouseup="handleRelease('cancel')"
      >
        B
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const emit = defineEmits(['input'])

const isMobile = ref(false)

function checkMobile() {
  isMobile.value = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) 
    || window.innerWidth <= 768
    || ('ontouchstart' in window)
}

function handlePress(key) {
  emit('input', { type: 'press', key, timestamp: Date.now() })
  
  const event = new KeyboardEvent('keydown', {
    key: getKeyCode(key),
    code: getKeyCode(key),
    bubbles: true
  })
  window.dispatchEvent(event)
}

function handleRelease(key) {
  emit('input', { type: 'release', key, timestamp: Date.now() })
  
  const event = new KeyboardEvent('keyup', {
    key: getKeyCode(key),
    code: getKeyCode(key),
    bubbles: true
  })
  window.dispatchEvent(event)
}

function getKeyCode(key) {
  const keyMap = {
    up: 'ArrowUp',
    down: 'ArrowDown',
    left: 'ArrowLeft',
    right: 'ArrowRight',
    action: ' ',
    cancel: 'Escape'
  }
  return keyMap[key] || key
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.virtual-controller {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 180px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  pointer-events: none;
  z-index: 100;
}

.controller-left,
.controller-right {
  pointer-events: auto;
}

.dpad {
  position: relative;
  width: 140px;
  height: 140px;
}

.dpad-btn {
  position: absolute;
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 10px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.1s ease;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.dpad-btn:active,
.dpad-btn.pressed {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(0.95);
}

.dpad-btn svg {
  width: 24px;
  height: 24px;
}

.dpad-btn.up {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.dpad-btn.down {
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.dpad-btn.left {
  left: 0;
  top: 50%;
  transform: translateY(-50%);
}

.dpad-btn.right {
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

.dpad-btn.up:active,
.dpad-btn.up.pressed {
  transform: translateX(-50%) scale(0.95);
}

.dpad-btn.down:active,
.dpad-btn.down.pressed {
  transform: translateX(-50%) scale(0.95);
}

.dpad-btn.left:active,
.dpad-btn.left.pressed {
  transform: translateY(-50%) scale(0.95);
}

.dpad-btn.right:active,
.dpad-btn.right.pressed {
  transform: translateY(-50%) scale(0.95);
}

.dpad-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.controller-right {
  display: flex;
  gap: 15px;
}

.action-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.1s ease;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.action-btn:active {
  transform: scale(0.9);
  background: rgba(255, 255, 255, 0.4);
}

.a-btn {
  background: rgba(46, 204, 113, 0.3);
  border-color: rgba(46, 204, 113, 0.6);
}

.b-btn {
  background: rgba(231, 76, 60, 0.3);
  border-color: rgba(231, 76, 60, 0.6);
}

@media (max-width: 480px) {
  .virtual-controller {
    height: 150px;
    padding: 15px 20px;
  }
  
  .dpad {
    width: 120px;
    height: 120px;
  }
  
  .dpad-btn {
    width: 42px;
    height: 42px;
  }
  
  .action-btn {
    width: 50px;
    height: 50px;
    font-size: 18px;
  }
}
</style>
