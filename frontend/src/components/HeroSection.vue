<template>
  <section class="hero-section">
    <div class="hero-background"></div>
    <div class="hero-content">
      <div class="hero-text">
        <p class="hero-label">我们在一起的第</p>
        <div class="hero-days">
          <span class="days-number">{{ days }}</span>
          <span class="days-unit">天</span>
        </div>
        <p class="hero-quote">时光荏苒，每一刻都是爱的见证</p>
      </div>
    </div>
    <div class="scroll-hint">
      <span class="scroll-arrow">↓</span>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { calculateDays } from '@/utils/dateUtils'

const days = ref(0)

onMounted(() => {
  days.value = calculateDays('2023-05-20')
})
</script>

<style scoped>
.hero-section {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  z-index: 0;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    #e8e0d8 0%,
    #d4c4bc 25%,
    #c8b8b0 50%,
    #b8a8a0 75%,
    #d0c0b8 100%
  ) !important;
  filter: blur(1px);
  opacity: 0.85;
}

.hero-background::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    ellipse at 50% 50%,
    rgba(255, 255, 255, 0.15) 0%,
    transparent 70%
  );
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px;
}

.hero-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.hero-label {
  font-size: 1.125rem;
  font-weight: 300;
  color: var(--color-text-light);
  letter-spacing: 0.15em;
  margin: 0;
}

.hero-days {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
}

.days-number {
  font-size: 8rem;
  font-weight: 200;
  color: var(--color-title);
  line-height: 1;
  letter-spacing: -0.02em;
}

.days-unit {
  font-size: 1.5rem;
  font-weight: 300;
  color: var(--color-text);
  letter-spacing: 0.1em;
}

.hero-quote {
  font-size: 1rem;
  font-weight: 300;
  color: var(--color-text-light);
  letter-spacing: 0.08em;
  margin: 0;
  padding-top: 16px;
}

.scroll-hint {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.5;
  animation: float 2s ease-in-out infinite;
}

.scroll-arrow {
  font-size: 1.25rem;
  color: var(--color-text-light);
  font-weight: 200;
}

@keyframes float {
  0%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(8px);
  }
}

@media (max-width: 768px) {
  .hero-section {
    min-height: 500px;
  }

  .hero-content {
    padding: 24px;
  }

  .hero-text {
    gap: 20px;
  }

  .hero-label {
    font-size: 1rem;
  }

  .days-number {
    font-size: 5rem;
  }

  .days-unit {
    font-size: 1.25rem;
  }

  .hero-quote {
    font-size: 0.9rem;
    padding-top: 12px;
  }

  .scroll-hint {
    bottom: 24px;
  }
}

@media (max-width: 480px) {
  .hero-section {
    min-height: 450px;
  }

  .hero-content {
    padding: 20px;
  }

  .hero-text {
    gap: 16px;
  }

  .hero-label {
    font-size: 0.9rem;
    letter-spacing: 0.12em;
  }

  .days-number {
    font-size: 4rem;
  }

  .days-unit {
    font-size: 1rem;
  }

  .hero-quote {
    font-size: 0.85rem;
    padding-top: 8px;
  }
}
</style>
