<template>
  <div class="home-container">
    <HeroSection />
    
    <div class="content-section">
      <section class="section feature-section">
        <div class="section-container">
          <FeatureCards />
        </div>
      </section>
      
      <section class="section about-section">
        <div class="section-container">
          <AboutUs />
        </div>
      </section>
    </div>
    
    <FooterBar />
    
    <div class="floating-actions">
      <button 
        @click.stop="bgmStore.toggleBGM()" 
        class="action-btn bgm-btn" 
        :class="{ active: bgmStore.bgmEnabled }"
        type="button"
        aria-label="背景音乐"
      >
        <span v-if="bgmStore.bgmLoading">...</span>
        <span v-else>{{ bgmStore.bgmEnabled ? '♪' : '♪' }}</span>
      </button>
      <ThemeSwitcher />
    </div>
    
    <div class="bgm-notice" v-if="bgmStore.showNotice">
      <span>{{ bgmStore.noticeText }}</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useBgmStore } from '@/stores/bgm'
import HeroSection from '@/components/HeroSection.vue'
import FeatureCards from '@/components/FeatureCards.vue'
import AboutUs from '@/components/AboutUs.vue'
import FooterBar from '@/components/FooterBar.vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

const bgmStore = useBgmStore()

onMounted(async () => {
  await bgmStore.initFromAPI()
  if (bgmStore.bgmEnabled && bgmStore.playlist.length > 0) {
    bgmStore.playTrack(bgmStore.currentTrackIndex)
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.content-section {
  position: relative;
  background: var(--color-bg);
}

.section {
  padding: 80px 0;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 400;
  color: var(--color-title);
  text-align: center;
  margin-bottom: 48px;
  letter-spacing: 0.05em;
}

.feature-section {
  background: linear-gradient(180deg, var(--color-bg) 0%, var(--color-accent-lighter) 100%);
}

.about-section {
  background: var(--color-bg-tertiary);
}

.floating-actions {
  position: fixed;
  right: 20px;
  bottom: 100px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 100;
}

.action-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: var(--shadow-soft);
  transition: all var(--transition-normal);
  cursor: pointer;
  text-decoration: none;
  color: var(--color-text-light);
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  -webkit-user-select: none;
}

.action-btn:active {
  transform: scale(0.95);
}

.action-btn:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-medium);
}

.bgm-btn.active {
  color: var(--color-pink);
}

.bgm-notice {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 12px 24px;
  border-radius: var(--radius-sm);
  background: var(--color-accent-dark);
  color: white;
  font-size: 14px;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

@media (max-width: 768px) {
  .section {
    padding: 60px 0;
  }
  
  .section-container {
    padding: 0 16px;
  }
  
  .section-title {
    font-size: 1.25rem;
    margin-bottom: 32px;
  }
  
  .floating-actions {
    right: 16px;
    bottom: 80px;
  }
  
  .action-btn {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .section {
    padding: 48px 0;
  }
}
</style>
