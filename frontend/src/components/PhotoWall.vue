<template>
  <div class="photo-wall">
    <h3 class="section-title">美好瞬间</h3>
    
    <div class="carousel-container" v-if="photos.length > 0">
      <div class="carousel-wrapper">
        <div 
          class="carousel-track" 
          :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
        >
          <div 
            v-for="(photo, index) in photos" 
            :key="photo.id" 
            class="carousel-slide"
          >
            <img :src="photo.url" :alt="photo.originalName" @click="openPreview(index)" />
          </div>
        </div>
        
        <button class="carousel-btn prev" @click="prevSlide" :disabled="photos.length <= 1">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>
        <button class="carousel-btn next" @click="nextSlide" :disabled="photos.length <= 1">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>
      </div>
      
      <div class="carousel-indicators" v-if="photos.length > 1">
        <button 
          v-for="(_, index) in photos" 
          :key="index"
          class="indicator"
          :class="{ active: index === currentIndex }"
          @click="goToSlide(index)"
        ></button>
      </div>
    </div>
    
    <div class="empty-state" v-else>
      <p>暂无照片，快来上传吧~</p>
    </div>
    
    <div class="upload-section">
      <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleUpload" />
      <button class="upload-btn" @click="triggerUpload">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="17 8 12 3 7 8"></polyline>
          <line x1="12" y1="3" x2="12" y2="15"></line>
        </svg>
        <span>上传照片</span>
      </button>
    </div>
    
    <Transition name="fade">
      <div v-if="showPreview" class="preview-modal" @click="closePreview">
        <button class="preview-btn prev" @click.stop="prevPhoto">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </button>
        <img :src="photos[previewIndex]?.url" class="preview-image" @click.stop />
        <button class="preview-btn next" @click.stop="nextPhoto">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </button>
        <div class="preview-counter">{{ previewIndex + 1 }} / {{ photos.length }}</div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getPhotos, uploadPhoto } from '@/api/photos'

const photos = ref([])
const fileInput = ref(null)
const currentIndex = ref(0)
const showPreview = ref(false)
const previewIndex = ref(0)
let autoPlayTimer = null

async function loadPhotos() {
  try {
    photos.value = await getPhotos()
    startAutoPlay()
  } catch (e) {
    console.error('加载照片失败', e)
  }
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('photo', file)
  
  try {
    await uploadPhoto(formData)
    await loadPhotos()
  } catch (e) {
    console.error('上传失败', e)
  }
  
  e.target.value = ''
}

function prevSlide() {
  if (photos.value.length <= 1) return
  currentIndex.value = currentIndex.value === 0 
    ? photos.value.length - 1 
    : currentIndex.value - 1
  resetAutoPlay()
}

function nextSlide() {
  if (photos.value.length <= 1) return
  currentIndex.value = currentIndex.value === photos.value.length - 1 
    ? 0 
    : currentIndex.value + 1
  resetAutoPlay()
}

function goToSlide(index) {
  currentIndex.value = index
  resetAutoPlay()
}

function startAutoPlay() {
  if (photos.value.length > 1) {
    autoPlayTimer = setInterval(() => {
      nextSlide()
    }, 4000)
  }
}

function resetAutoPlay() {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    startAutoPlay()
  }
}

function openPreview(index) {
  previewIndex.value = index
  showPreview.value = true
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
  }
}

function closePreview() {
  showPreview.value = false
  startAutoPlay()
}

function prevPhoto() {
  previewIndex.value = previewIndex.value === 0 
    ? photos.value.length - 1 
    : previewIndex.value - 1
}

function nextPhoto() {
  previewIndex.value = previewIndex.value === photos.value.length - 1 
    ? 0 
    : previewIndex.value + 1
}

onMounted(loadPhotos)

onUnmounted(() => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
  }
})
</script>

<style scoped>
.photo-wall {
  padding: 20px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 400;
  color: var(--color-title);
  margin-bottom: 20px;
  text-align: center;
  letter-spacing: 0.05em;
}

.carousel-container {
  position: relative;
}

.carousel-wrapper {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  aspect-ratio: 16/9;
  background: var(--color-bg-tertiary);
}

.carousel-track {
  display: flex;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.carousel-slide {
  flex: 0 0 100%;
  height: 100%;
}

.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.carousel-slide img:hover {
  transform: scale(1.02);
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
}

.carousel-btn:hover {
  background: white;
  box-shadow: var(--shadow-soft);
}

.carousel-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.carousel-btn svg {
  width: 20px;
  height: 20px;
  color: var(--color-text);
}

.carousel-btn.prev {
  left: 12px;
}

.carousel-btn.next {
  right: 12px;
}

.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-border);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator:hover {
  background: var(--color-text-light);
}

.indicator.active {
  width: 24px;
  border-radius: 4px;
  background: var(--color-pink);
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-light);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
}

.upload-section {
  margin-top: 16px;
  text-align: center;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  background: var(--color-pink-light);
  border-color: var(--color-pink);
}

.upload-btn svg {
  width: 18px;
  height: 18px;
}

.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.preview-image {
  max-width: 90%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.preview-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preview-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.preview-btn svg {
  width: 24px;
  height: 24px;
  color: white;
}

.preview-btn.prev {
  left: 20px;
}

.preview-btn.next {
  right: 20px;
}

.preview-counter {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.5);
  padding: 6px 16px;
  border-radius: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .carousel-wrapper {
    aspect-ratio: 4/3;
  }
  
  .carousel-btn {
    width: 32px;
    height: 32px;
  }
  
  .carousel-btn svg {
    width: 16px;
    height: 16px;
  }
  
  .preview-btn {
    width: 40px;
    height: 40px;
  }
  
  .preview-btn.prev {
    left: 10px;
  }
  
  .preview-btn.next {
    right: 10px;
  }
}
</style>
