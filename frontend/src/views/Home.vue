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
      <ThemeSwitcher />
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
}

@media (max-width: 480px) {
  .section {
    padding: 48px 0;
  }
}
</style>
