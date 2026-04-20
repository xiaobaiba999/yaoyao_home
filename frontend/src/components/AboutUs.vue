<template>
  <div class="about-us">
    <div class="about-container">
      <div class="photo-section">
        <div class="photo-wrapper">
          <img 
            :src="defaultPhotoUrl" 
            alt="我们的合照" 
            class="couple-photo"
          />
        </div>
      </div>
      
      <div class="info-section">
        <div class="info-content">
          <div class="info-item">
            <span class="info-label">姓名</span>
            <span class="info-value">瑶瑶 & 璐璐</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">星座</span>
            <span class="info-value">双子座 & 狮子座</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">在一起</span>
            <span class="info-value">2023.05.20</span>
          </div>
          
          <div class="info-item">
            <span class="info-label">相遇地点</span>
            <span class="info-value">那个特别的夜晚</span>
          </div>
          
          <div class="info-item declaration">
            <span class="info-label">宣言</span>
            <span class="info-value">愿时光温柔，愿我们长久</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const defaultPhotoUrl = '/icons/icon-512x512.png'
</script>

<style scoped>
.about-us {
  padding: var(--spacing-2xl) var(--spacing-lg);
  background: var(--color-bg);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3xl);
  max-width: 1000px;
  width: 100%;
  padding: var(--spacing-xl);
}

.photo-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.photo-wrapper {
  width: 320px;
  height: 320px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(61, 61, 61, 0.06);
  transition: all var(--transition-normal);
  cursor: pointer;
  position: relative;
}

.photo-wrapper:hover {
  box-shadow: 0 12px 48px rgba(61, 61, 61, 0.1);
  transform: translateY(-4px);
}

.couple-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.photo-wrapper:hover .couple-photo {
  transform: scale(1.05);
}

.photo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: var(--color-bg-secondary);
  color: var(--color-text-light);
}

.photo-placeholder svg {
  width: 64px;
  height: 64px;
  opacity: 0.5;
}

.photo-placeholder span {
  font-size: 0.9rem;
  opacity: 0.7;
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.photo-wrapper:hover .photo-overlay {
  opacity: 1;
}

.photo-overlay svg {
  width: 40px;
  height: 40px;
  color: white;
}

.photo-overlay span {
  color: white;
  font-size: 0.9rem;
}

.info-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: var(--spacing-lg) 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.info-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: var(--spacing-md);
  letter-spacing: 0.08em;
}

.info-label {
  font-size: 0.85rem;
  font-weight: 300;
  color: var(--color-text-light);
  opacity: 0.85;
  flex-shrink: 0;
}

.info-value {
  font-size: 1rem;
  font-weight: 300;
  color: var(--color-title);
  line-height: 1.6;
}

.info-item.declaration {
  margin-top: var(--spacing-lg);
}

.info-item.declaration .info-value {
  font-style: italic;
  color: var(--color-title-light);
}

@media (max-width: 1024px) {
  .about-container {
    gap: var(--spacing-2xl);
    padding: var(--spacing-lg);
  }
  
  .photo-wrapper {
    width: 280px;
    height: 280px;
  }
  
  .info-content {
    gap: var(--spacing-lg);
  }
}

@media (max-width: 768px) {
  .about-us {
    padding: var(--spacing-xl) var(--spacing-md);
    min-height: auto;
  }
  
  .about-container {
    flex-direction: column;
    gap: var(--spacing-2xl);
  }
  
  .photo-section {
    order: 1;
  }
  
  .info-section {
    order: 2;
    width: 100%;
    justify-content: center;
  }
  
  .photo-wrapper {
    width: 260px;
    height: 260px;
  }
  
  .info-content {
    writing-mode: horizontal-tb;
    text-orientation: mixed;
    align-items: center;
    gap: var(--spacing-lg);
  }
  
  .info-item {
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-xs);
    text-align: center;
  }
  
  .info-item.declaration {
    margin-top: var(--spacing-md);
  }
  
  .info-label {
    font-size: 0.8rem;
  }
  
  .info-value {
    font-size: 0.95rem;
  }
}

@media (max-width: 480px) {
  .photo-wrapper {
    width: 220px;
    height: 220px;
    border-radius: 20px;
  }
  
  .info-content {
    gap: var(--spacing-md);
  }
  
  .info-label {
    font-size: 0.75rem;
  }
  
  .info-value {
    font-size: 0.9rem;
  }
}
</style>
