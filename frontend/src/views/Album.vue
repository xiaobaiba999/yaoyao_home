<template>
  <div class="album-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        <span>返回</span>
      </button>
      <h1 class="page-title">相册</h1>
      <span class="photo-count">{{ photos.length }} 张</span>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/png,image/webp"
      multiple
      style="display: none"
      @change="handleFileSelect"
    />

    <div class="page-content">
      <div v-if="isLoading" class="skeleton-grid">
        <div v-for="i in 6" :key="i" class="skeleton-item">
          <div class="skeleton-image"></div>
          <div class="skeleton-info">
            <div class="skeleton-text"></div>
          </div>
        </div>
      </div>

      <div v-else-if="photos.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <circle cx="8.5" cy="8.5" r="1.5"></circle>
            <polyline points="21 15 16 10 5 21"></polyline>
          </svg>
        </div>
        <p class="empty-text">还没有照片</p>
        <p class="empty-hint">点击下方按钮上传第一张照片吧</p>
      </div>

      <div v-else class="photo-grid">
        <div
          v-for="(photo, index) in photos"
          :key="photo.id"
          class="photo-item"
        >
          <div class="photo-image-wrapper" @click="openViewer(index)">
            <div class="image-placeholder" :class="{ hidden: loadedImages[photo.id] }">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </div>
            <img
              :src="getThumbnailUrl(photo.url)"
              :alt="photo.description || '照片'"
              loading="lazy"
              :class="{ loaded: loadedImages[photo.id] }"
              @load="onImageLoad(photo.id)"
              @error="onImageError(photo.id, $event)"
            />
          </div>
          <div class="photo-info">
            <p class="photo-description">{{ photo.description || '无描述' }}</p>
            <button class="photo-delete-btn" @click.stop="confirmDelete(photo.id)" title="删除">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="page-footer">
      <button class="upload-btn" @click="triggerUpload">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="17 8 12 3 7 8"></polyline>
          <line x1="12" y1="3" x2="12" y2="15"></line>
        </svg>
        <span>上传图片</span>
        <span class="upload-hint">最多9张</span>
      </button>
    </div>

    <Transition name="modal-fade">
      <div v-if="showUploadModal" class="modal-overlay" @click.self="cancelUpload">
        <div class="upload-dialog" @click.stop>
          <div class="modal-header">
            <h3 class="modal-title">批量上传照片</h3>
            <button class="modal-close" @click="cancelUpload">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="batch-list">
              <div
                v-for="(item, idx) in uploadQueue"
                :key="idx"
                class="batch-item"
                :class="{
                  'upload-success': item.status === 'success',
                  'upload-error': item.status === 'error',
                  'uploading': item.status === 'uploading'
                }"
              >
                <div class="batch-item-thumb-wrap">
                  <img :src="item.previewUrl" :alt="item.file.name" class="batch-item-thumb" />
                  <div class="batch-item-overlay" v-if="item.status === 'uploading'">
                    <div class="progress-ring">
                      <svg viewBox="0 0 36 36">
                        <circle class="progress-bg" cx="18" cy="18" r="16" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="3"/>
                        <circle class="progress-fill" cx="18" cy="18" r="16" fill="none" stroke="white" stroke-width="3"
                          :stroke-dasharray="`${item.progress * 100.53} 100.53`"
                          stroke-linecap="round"
                          transform="rotate(-90 18 18)"/>
                      </svg>
                      <span class="progress-text">{{ Math.round(item.progress * 100) }}%</span>
                    </div>
                  </div>
                  <div class="batch-item-status" v-if="item.status === 'success'">
                    <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
                  </div>
                  <div class="batch-item-status error" v-if="item.status === 'error'" :title="item.errorMsg">
                    <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </div>
                  <button class="batch-item-remove" @click="removeFromQueue(idx)" v-if="item.status === 'pending'" type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  </button>
                </div>
                <div class="batch-item-info">
                  <span class="batch-item-name">{{ item.file.name }}</span>
                  <input
                    v-model="item.description"
                    type="text"
                    placeholder="添加描述（最多20字）..."
                    class="batch-item-input"
                    maxlength="20"
                    :disabled="item.status !== 'pending'"
                  />
                </div>
              </div>
            </div>

            <div class="batch-actions" v-if="uploadQueue.length > 0">
              <button class="batch-add-btn" v-if="uploadQueue.length < MAX_BATCH && !isUploading" @click="addMoreFiles" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                <span>继续添加</span>
              </button>
              <div class="batch-summary">
                <span>共 {{ uploadQueue.length }} 张</span>
                <span v-if="successCount > 0" class="success-count">✓ {{ successCount }}</span>
                <span v-if="errorCount > 0" class="error-count">✗ {{ errorCount }}</span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="cancelUpload">取消</button>
            <button
              class="modal-btn confirm"
              @click="executeBatchUpload"
              :disabled="isUploading || pendingCount === 0"
            >
              {{ isUploading ? `上传中 (${successCount}/${uploadQueue.length})` : `上传 ${pendingCount} 张` }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="viewer-fade">
      <div v-if="showViewer" class="viewer-overlay" @click="closeViewer">
        <div class="viewer-header">
          <button class="viewer-close" @click="closeViewer">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
          <span class="viewer-counter">{{ viewerIndex + 1 }} / {{ photos.length }}</span>
          <button class="viewer-delete" @click.stop="confirmDeleteFromViewer">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
          </button>
        </div>

        <div class="viewer-content" @click.stop>
          <button class="viewer-nav prev" @click="prevPhoto" :disabled="photos.length <= 1">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>

          <div class="viewer-image-wrapper">
            <div v-if="!viewerImageLoaded" class="viewer-loader">
              <div class="loader-spinner"></div>
            </div>
            <img
              :src="getImageUrl(currentPhoto?.url)"
              :alt="currentPhoto?.description || '照片'"
              class="viewer-image"
              :class="{ visible: viewerImageLoaded }"
              @load="viewerImageLoaded = true"
            />
            <div v-if="currentPhoto?.description" class="viewer-description">
              {{ currentPhoto.description }}
            </div>
          </div>

          <button class="viewer-nav next" @click="nextPhoto" :disabled="photos.length <= 1">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>

        <div class="viewer-thumbnails">
          <div
            v-for="(photo, index) in photos"
            :key="photo.id"
            class="thumbnail-item"
            :class="{ active: index === viewerIndex }"
            @click.stop="goToPhoto(index)"
          >
            <img :src="getThumbnailUrl(photo.url)" :alt="photo.description || '照片'" />
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="modal-fade">
      <div v-if="showDeleteConfirm" class="modal-overlay" @click="cancelDelete">
        <div class="confirm-dialog" @click.stop>
          <div class="confirm-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          </div>
          <h3 class="confirm-title">确认删除</h3>
          <p class="confirm-message">确定要删除这张照片吗？此操作无法撤销。</p>
          <div class="confirm-actions">
            <button class="confirm-btn cancel" @click="cancelDelete">取消</button>
            <button class="confirm-btn delete" @click="executeDelete">确认删除</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPhotos, uploadPhoto, deletePhoto, validateFileSize } from '@/api/photos'
import { getImageUrl, getThumbnailUrl } from '@/api/request'
import { ElMessage } from 'element-plus'

const MAX_BATCH = 9
const MAX_FILE_SIZE = 50 * 1024 * 1024
const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

const router = useRouter()
const photos = ref([])
const isLoading = ref(true)
const loadedImages = ref({})

const fileInput = ref(null)
const showUploadModal = ref(false)
const showDeleteConfirm = ref(false)
const isUploading = ref(false)
const deleteTargetId = ref(null)

const uploadQueue = ref([])

const successCount = computed(() => uploadQueue.value.filter(i => i.status === 'success').length)
const errorCount = computed(() => uploadQueue.value.filter(i => i.status === 'error').length)
const pendingCount = computed(() => uploadQueue.value.filter(i => i.status === 'pending').length)

const showViewer = ref(false)
const viewerIndex = ref(0)
const viewerImageLoaded = ref(false)

const currentPhoto = computed(() => photos.value[viewerIndex.value])

function onImageLoad(id) {
  loadedImages.value[id] = true
}

function onImageError(id, event) {
  const imgEl = event.target
  console.error('[Album] 图片加载失败, id:', id, 'url:', imgEl?.src)
  loadedImages.value[id] = true
}

function openViewer(index) {
  viewerIndex.value = index
  viewerImageLoaded.value = false
  showViewer.value = true
  document.body.style.overflow = 'hidden'
}

function closeViewer() {
  showViewer.value = false
  document.body.style.overflow = ''
}

function prevPhoto() {
  if (photos.value.length <= 1) return
  viewerIndex.value = viewerIndex.value === 0
    ? photos.value.length - 1
    : viewerIndex.value - 1
  viewerImageLoaded.value = false
}

function nextPhoto() {
  if (photos.value.length <= 1) return
  viewerIndex.value = viewerIndex.value === photos.value.length - 1
    ? 0
    : viewerIndex.value + 1
  viewerImageLoaded.value = false
}

function goToPhoto(index) {
  viewerIndex.value = index
  viewerImageLoaded.value = false
}

async function loadPhotos() {
  isLoading.value = true
  try {
    photos.value = await getPhotos()
    console.log('[Album] 加载照片列表:', photos.value.length, '张')
    photos.value.forEach(p => {
      console.log('[Album] photo:', p.id, 'url:', p.url, '→ resolved:', getThumbnailUrl(p.url))
    })
  } catch (e) {
    console.error('加载照片失败', e)
    photos.value = []
  } finally {
    isLoading.value = false
  }
}

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileSelect(e) {
  const files = Array.from(e.target.files || [])
  e.target.value = ''
  if (files.length === 0) return

  const validFiles = []
  const errors = []

  for (const file of files) {
    if (!ACCEPTED_TYPES.includes(file.type)) {
      errors.push(`${file.name}: 不支持的格式，仅支持 JPG/PNG/WebP`)
      continue
    }
    const sizeCheck = validateFileSize(file, MAX_FILE_SIZE)
    if (!sizeCheck.valid) {
      errors.push(`${file.name}: ${sizeCheck.error}`)
      continue
    }
    validFiles.push(file)
  }

  if (errors.length > 0) {
    ElMessage.warning({ message: errors.slice(0, 3).join('\n'), duration: 4000 })
  }

  const remaining = MAX_BATCH - uploadQueue.value.length
  const toAdd = validFiles.slice(0, remaining)

  if (validFiles.length > remaining) {
    ElMessage.info(`最多同时上传 ${MAX_BATCH} 张，已选取前 ${remaining} 张`)
  }

  for (const file of toAdd) {
    uploadQueue.value.push({
      file,
      previewUrl: URL.createObjectURL(file),
      description: '',
      status: 'pending',
      progress: 0,
      errorMsg: ''
    })
  }

  if (uploadQueue.value.length > 0 && !showUploadModal.value) {
    showUploadModal.value = true
  }
}

function addMoreFiles() {
  fileInput.value?.click()
}

function removeFromQueue(idx) {
  const item = uploadQueue.value[idx]
  if (item.previewUrl) URL.revokeObjectURL(item.previewUrl)
  uploadQueue.value.splice(idx, 1)
}

function cancelUpload() {
  for (const item of uploadQueue.value) {
    if (item.previewUrl) URL.revokeObjectURL(item.previewUrl)
  }
  uploadQueue.value = []
  showUploadModal.value = false
}

async function executeBatchUpload() {
  const pendingItems = uploadQueue.value.filter(i => i.status === 'pending')
  if (pendingItems.length === 0 || isUploading.value) return

  isUploading.value = true

  for (const item of pendingItems) {
    item.status = 'uploading'
    item.progress = 0

    try {
      const formData = new FormData()
      formData.append('photo', item.file)
      formData.append('description', (item.description || '').trim())

      const photo = await uploadPhoto(formData)
      item.status = 'success'
      item.progress = 1
      photos.value.unshift(photo)
    } catch (e) {
      console.error('上传照片失败', e)
      item.status = 'error'
      item.errorMsg = e.response?.data?.message || '上传失败'
    }
  }

  isUploading.value = false

  if (errorCount.value === 0) {
    ElMessage.success(`成功上传 ${successCount.value} 张照片`)
    setTimeout(() => cancelUpload(), 800)
  } else {
    ElMessage.warning(`${successCount.value} 张成功，${errorCount.value} 张失败`)
  }
}

function confirmDelete(id) {
  deleteTargetId.value = id
  showDeleteConfirm.value = true
}

function confirmDeleteFromViewer() {
  if (currentPhoto.value) {
    deleteTargetId.value = currentPhoto.value.id
    showDeleteConfirm.value = true
  }
}

function cancelDelete() {
  showDeleteConfirm.value = false
  deleteTargetId.value = null
}

async function executeDelete() {
  if (!deleteTargetId.value) return

  try {
    await deletePhoto(deleteTargetId.value)
    const index = photos.value.findIndex(p => p.id === deleteTargetId.value)
    if (index > -1) {
      const photoId = photos.value[index].id
      delete loadedImages.value[photoId]
      photos.value.splice(index, 1)
      if (showViewer.value) {
        if (photos.value.length === 0) {
          closeViewer()
        } else if (viewerIndex.value >= photos.value.length) {
          viewerIndex.value = photos.value.length - 1
        }
      }
    }
  } catch (e) {
    console.error('删除照片失败', e)
    ElMessage.error('删除失败，请重试')
  }

  cancelDelete()
}

function goBack() {
  router.push('/')
}

function handleKeydown(e) {
  if (showViewer.value) {
    if (e.key === 'ArrowLeft') prevPhoto()
    else if (e.key === 'ArrowRight') nextPhoto()
    else if (e.key === 'Escape') closeViewer()
  }
}

watch(viewerIndex, () => {
  viewerImageLoaded.value = false
})

onMounted(() => {
  loadPhotos()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.album-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-top: 60px;
  padding-bottom: 80px;
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
  justify-content: space-between;
  padding: 0 20px;
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
  border-radius: 8px;
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
  font-size: 1.2rem;
  font-weight: 400;
  color: var(--color-title);
}

.photo-count {
  font-size: 13px;
  color: var(--color-text-light);
  background: var(--color-bg-secondary);
  padding: 4px 12px;
  border-radius: 12px;
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
  min-height: calc(100vh - 140px);
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.skeleton-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.skeleton-image {
  aspect-ratio: 1;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 8px 10px;
}

.skeleton-text {
  height: 12px;
  width: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  color: var(--color-border);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.empty-text {
  font-size: 1.1rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 0.9rem;
  color: var(--color-border);
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.photo-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.photo-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.photo-image-wrapper {
  aspect-ratio: 1;
  overflow: hidden;
  cursor: pointer;
  background: var(--color-bg-secondary);
  position: relative;
}

.image-placeholder {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-secondary);
  transition: opacity 0.3s ease;
}

.image-placeholder svg {
  width: 40px;
  height: 40px;
  color: var(--color-border);
}

.image-placeholder.hidden {
  opacity: 0;
  pointer-events: none;
}

.photo-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease, opacity 0.3s ease;
  opacity: 0;
}

.photo-image-wrapper img.loaded {
  opacity: 1;
}

.photo-image-wrapper:hover img {
  transform: scale(1.05);
}

.photo-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  gap: 8px;
}

.photo-description {
  flex: 1;
  font-size: 12px;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.photo-delete-btn {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.5;
}

.photo-delete-btn:hover {
  background: #ffe0e0;
  opacity: 1;
}

.photo-delete-btn svg {
  width: 16px;
  height: 16px;
  color: #ff6b6b;
}

.page-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: rgba(250, 248, 245, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--color-border);
  z-index: 100;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-pink);
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 25px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(200, 160, 160, 0.3);
}

.upload-btn:hover {
  background: var(--color-pink-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(200, 160, 160, 0.4);
}

.upload-btn svg {
  width: 18px;
  height: 18px;
}

.upload-hint {
  font-size: 11px;
  opacity: 0.8;
}

.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.upload-dialog {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 85vh;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.modal-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-title);
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: var(--color-bg-tertiary);
}

.modal-close svg {
  width: 20px;
  height: 20px;
  color: var(--color-text);
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.batch-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
  max-height: 50vh;
  overflow-y: auto;
}

.batch-item {
  display: flex;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  background: var(--color-bg);
  border: 1px solid var(--color-border-light);
  transition: all 0.2s ease;
}

.batch-item.upload-success {
  border-color: #4caf50;
  background: rgba(76, 175, 80, 0.05);
}

.batch-item.upload-error {
  border-color: #f44336;
  background: rgba(244, 67, 54, 0.05);
}

.batch-item-thumb-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--color-bg-tertiary);
}

.batch-item-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.batch-item-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-ring {
  position: relative;
  width: 44px;
  height: 44px;
}

.progress-ring svg {
  width: 100%;
  height: 100%;
  transform: scaleX(-1);
}

.progress-fill {
  transition: stroke-dasharray 0.3s ease;
}

.progress-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  color: white;
}

.batch-item-status {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(76, 175, 80, 0.7);
}

.batch-item-status.error {
  background: rgba(244, 67, 54, 0.7);
}

.batch-item-status svg {
  width: 24px;
  height: 24px;
}

.batch-item-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.batch-item-remove svg {
  width: 10px;
  height: 10px;
  color: white;
}

.batch-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.batch-item-name {
  font-size: 12px;
  color: var(--color-text-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.batch-item-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 13px;
  background: var(--color-bg-secondary);
  color: var(--color-text);
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}

.batch-item-input:focus {
  outline: none;
  border-color: var(--color-pink);
}

.batch-item-input:disabled {
  opacity: 0.5;
  background: var(--color-bg-tertiary);
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 8px;
}

.batch-add-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px dashed var(--color-border);
  background: transparent;
  color: var(--color-text-light);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.batch-add-btn:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: var(--color-accent-lighter);
}

.batch-add-btn svg {
  width: 16px;
  height: 16px;
}

.batch-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 16px;
  padding: 8px 12px;
  background: var(--color-bg-tertiary);
  border-radius: 8px;
}

.success-count {
  color: #4caf50;
  font-weight: 500;
}

.error-count {
  color: #f44336;
  font-weight: 500;
}

.form-group {
  position: relative;
}

.form-label {
  display: block;
  font-size: 0.85rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px 14px;
  padding-right: 50px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 14px;
  background: var(--color-bg);
  color: var(--color-text);
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-pink);
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: var(--color-text-lighter);
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--color-border);
  justify-content: flex-end;
  flex-shrink: 0;
}

.modal-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.modal-btn.cancel {
  background: var(--color-bg-tertiary);
  color: var(--color-text);
}

.modal-btn.cancel:hover {
  background: var(--color-border);
}

.modal-btn.confirm {
  background: var(--color-pink);
  color: white;
}

.modal-btn.confirm:hover:not(:disabled) {
  background: var(--color-pink-dark);
}

.modal-btn.confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.confirm-dialog {
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 360px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.confirm-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  color: #ff6b6b;
}

.confirm-icon svg {
  width: 100%;
  height: 100%;
}

.confirm-title {
  font-size: 1.25rem;
  font-weight: 500;
  color: var(--color-title);
  margin-bottom: 8px;
}

.confirm-message {
  font-size: 0.95rem;
  color: var(--color-text-light);
  margin-bottom: 24px;
  line-height: 1.6;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.confirm-btn.cancel {
  background: var(--color-bg-tertiary);
  color: var(--color-text);
}

.confirm-btn.cancel:hover {
  background: var(--color-border);
}

.confirm-btn.delete {
  background: #ff6b6b;
  color: white;
}

.confirm-btn.delete:hover {
  background: #ee5a5a;
}

.viewer-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.viewer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  color: white;
}

.viewer-close,
.viewer-delete {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.viewer-close:hover,
.viewer-delete:hover {
  background: rgba(255, 255, 255, 0.2);
}

.viewer-close svg,
.viewer-delete svg {
  width: 24px;
  height: 24px;
  color: white;
}

.viewer-counter {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
}

.viewer-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  position: relative;
}

.viewer-nav {
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
  flex-shrink: 0;
}

.viewer-nav:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
}

.viewer-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.viewer-nav svg {
  width: 24px;
  height: 24px;
  color: white;
}

.viewer-image-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-height: 100%;
  padding: 0 16px;
  position: relative;
}

.viewer-loader {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.loader-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.viewer-image {
  max-width: 100%;
  max-height: calc(100vh - 200px);
  object-fit: contain;
  border-radius: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.viewer-image.visible {
  opacity: 1;
}

.viewer-description {
  margin-top: 16px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  color: white;
  font-size: 14px;
  max-width: 80%;
  text-align: center;
}

.viewer-thumbnails {
  display: flex;
  gap: 8px;
  padding: 16px 20px;
  overflow-x: auto;
  justify-content: center;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.5;
  transition: all 0.3s ease;
  flex-shrink: 0;
  border: 2px solid transparent;
}

.thumbnail-item:hover {
  opacity: 0.8;
}

.thumbnail-item.active {
  opacity: 1;
  border-color: var(--color-pink);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.viewer-fade-enter-active,
.viewer-fade-leave-active {
  transition: opacity 0.3s ease;
}

.viewer-fade-enter-from,
.viewer-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .skeleton-grid,
  .photo-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .photo-info {
    padding: 6px 8px;
  }

  .photo-description {
    font-size: 11px;
  }

  .photo-delete-btn {
    width: 24px;
    height: 24px;
  }

  .photo-delete-btn svg {
    width: 14px;
    height: 14px;
  }

  .viewer-nav {
    width: 40px;
    height: 40px;
  }

  .viewer-nav svg {
    width: 20px;
    height: 20px;
  }

  .viewer-thumbnails {
    padding: 12px 16px;
  }

  .thumbnail-item {
    width: 50px;
    height: 50px;
  }

  .upload-dialog {
    max-width: 100%;
    max-height: 90vh;
  }

  .batch-preview-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .page-content {
    padding: 10px;
  }

  .skeleton-grid,
  .photo-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .photo-info {
    padding: 5px 6px;
  }

  .photo-description {
    font-size: 10px;
  }

  .photo-delete-btn {
    width: 22px;
    height: 22px;
  }

  .photo-delete-btn svg {
    width: 12px;
    height: 12px;
  }

  .viewer-nav {
    width: 36px;
    height: 36px;
  }

  .viewer-description {
    font-size: 13px;
    padding: 10px 16px;
  }

  .thumbnail-item {
    width: 45px;
    height: 45px;
  }

  .batch-preview-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }
}
</style>
