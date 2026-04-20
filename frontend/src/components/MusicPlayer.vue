<template>
  <div class="music-player" v-if="bgmStore.bgmEnabled" :class="{ minimized: isMinimized }">
    <div class="player-main" @click="handleMainClick">
      <div class="track-info">
        <span class="track-icon">🎵</span>
        <span class="track-name" v-if="!isMinimized">{{ bgmStore.currentTrack?.name || '未知' }}</span>
        <span class="track-index" v-if="!isMinimized">{{ bgmStore.currentTrackIndex + 1 }}/{{ bgmStore.totalTracks }}</span>
      </div>
      <div class="player-status">
        <span v-if="bgmStore.bgmLoading" class="loading">⏳</span>
        <span v-else-if="bgmStore.isPlaying" class="playing">▶</span>
        <span v-else class="paused">⏸</span>
      </div>
    </div>

    <div class="player-controls" v-show="bgmStore.showPlaylist && !isMinimized" @click.stop>
      <div class="controls-top">
        <button @click="bgmStore.playPrev" class="ctrl-btn" title="上一首">⏮</button>
        <button @click="bgmStore.togglePlayPause" class="ctrl-btn play-btn">
          {{ bgmStore.isPlaying ? '⏸' : '▶' }}
        </button>
        <button @click="bgmStore.playNext" class="ctrl-btn" title="下一首">⏭</button>
      </div>

      <div class="shuffle-control">
        <button
          @click="bgmStore.toggleShuffle"
          class="shuffle-btn"
          :class="{ active: bgmStore.shuffleMode }"
          :title="bgmStore.shuffleMode ? '随机播放中' : '切换到随机播放'"
        >
          {{ bgmStore.shuffleMode ? '🔀' : '🔁' }}
        </button>
        <span class="shuffle-text">{{ bgmStore.shuffleMode ? '随机播放' : '顺序播放' }}</span>
      </div>

      <div class="volume-control">
        <span class="volume-icon">🔊</span>
        <input
          type="range"
          min="0"
          max="1"
          step="0.1"
          :value="bgmStore.volume"
          @input="bgmStore.setVolume(parseFloat($event.target.value))"
          class="volume-slider"
        />
        <span class="volume-value">{{ Math.round(bgmStore.volume * 100) }}%</span>
      </div>

      <div class="playlist-container">
        <div class="playlist-header">
          <span>🎵 我的音乐 ({{ bgmStore.totalTracks }}首)</span>
          <button @click="handleRefresh" class="refresh-btn" title="刷新列表">🔄</button>
          <button @click="showAddCustomDialog = true" class="add-custom-btn" title="手动添加链接">+ 添加</button>
        </div>
        <div class="playlist-scroll">
          <div v-if="!bgmStore.apiLoaded && bgmStore.totalTracks === 0" class="loading-tip">
            正在加载音乐列表...
          </div>
          <div v-else-if="bgmStore.totalTracks === 0" class="empty-tip">
            暂无音乐，请先在 <a href="http://localhost:4000/admin.html" target="_blank">音乐管理页</a> 上传
          </div>
          <div
            v-for="(track, index) in bgmStore.playlist"
            :key="index"
            class="playlist-item"
            :class="{ active: index === bgmStore.currentTrackIndex }"
          >
            <span class="item-index">{{ index + 1 }}</span>
            <span class="item-name" @click="bgmStore.playTrack(index)">{{ track.name }}</span>
            <span v-if="index === bgmStore.currentTrackIndex && bgmStore.isPlaying" class="playing-icon">▶</span>
            <button
              v-if="bgmStore.customPlaylist.length > 0"
              @click="bgmStore.removeTrack(index)"
              class="remove-btn"
              title="移除"
            >×</button>
          </div>
        </div>
      </div>
    </div>

    <div class="custom-add-dialog" v-if="showAddCustomDialog" @click.self="showAddCustomDialog = false">
      <div class="dialog-content">
        <h4>手动添加音乐</h4>
        <div class="form-group">
          <label>歌曲名称</label>
          <input v-model="customSongName" type="text" placeholder="输入歌曲名称" />
        </div>
        <div class="form-group">
          <label>音乐链接</label>
          <input v-model="customSongUrl" type="text" placeholder="输入有效的MP3链接" />
        </div>
        <div class="dialog-actions">
          <button @click="showAddCustomDialog = false" class="cancel-btn">取消</button>
          <button
            @click="handleAddCustom"
            class="confirm-btn"
            :disabled="!customSongName.trim() || !customSongUrl.trim()"
          >
            添加
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useBgmStore } from '@/stores/bgm'

const bgmStore = useBgmStore()
const isMinimized = ref(false)
const showAddCustomDialog = ref(false)
const customSongName = ref('')
const customSongUrl = ref('')
let lastScrollY = 0

function handleScroll() {
  const currentScrollY = window.scrollY
  if (currentScrollY > 50) {
    isMinimized.value = true
  }
  lastScrollY = currentScrollY
}

function handleMainClick() {
  if (isMinimized.value) {
    isMinimized.value = false
  } else {
    bgmStore.togglePlaylist()
  }
}

function handleAddCustom() {
  if (!customSongName.value.trim() || !customSongUrl.value.trim()) return

  bgmStore.addTrack(customSongName.value.trim(), customSongUrl.value.trim())
  showAddCustomDialog.value = false
  customSongName.value = ''
  customSongUrl.value = ''
}

async function handleRefresh() {
  await bgmStore.refreshPlaylist()
  bgmStore.showBgmNotice('已刷新音乐列表')
}

onMounted(async () => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  await bgmStore.initFromAPI()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.music-player {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  width: 340px;
  max-height: 520px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.music-player.minimized {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.player-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #667eea11 0%, #764ba211 100%);
}

.music-player.minimized .player-main {
  padding: 0;
  width: 100%;
  height: 100%;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.track-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.music-player.minimized .track-info {
  gap: 0;
}

.track-icon {
  font-size: 20px;
  transition: all 0.3s ease;
}

.music-player.minimized .track-icon {
  font-size: 26px;
  filter: brightness(2);
}

.track-name {
  font-size: 14px;
  color: #333;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.track-index {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.player-status {
  font-size: 18px;
  margin-left: 12px;
}

.music-player.minimized .player-status {
  position: absolute;
  bottom: 3px;
  font-size: 9px;
  color: rgba(255,255,255,0.9);
}

.playing {
  color: #667eea;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.paused, .loading {
  color: #999;
}

.player-controls {
  padding: 16px 18px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  max-height: 440px;
  overflow-y: auto;
}

.controls-top {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}

.ctrl-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ctrl-btn:hover {
  background: #e0e0e0;
  transform: scale(1.08);
}

.play-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}

.play-btn:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4192 100%);
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.45);
}

.shuffle-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 14px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-radius: 10px;
}

.shuffle-btn {
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.25s ease;
}

.shuffle-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.shuffle-text {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.volume-icon {
  font-size: 15px;
}

.volume-slider {
  flex: 1;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: linear-gradient(to right, #667eea, #764ba2);
  border-radius: 3px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.volume-slider:hover {
  opacity: 1;
}

.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  background: white;
  border: 3px solid #667eea;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
  transition: transform 0.2s;
}

.volume-slider::-webkit-slider-thumb:hover {
  transform: scale(1.15);
}

.volume-value {
  font-size: 12px;
  color: #666;
  min-width: 38px;
  text-align: right;
  font-weight: 500;
}

.playlist-container {
  margin-top: 4px;
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #888;
  margin-bottom: 10px;
  font-weight: 600;
}

.add-custom-btn {
  padding: 4px 10px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  font-weight: 500;
  transition: all 0.25s ease;
}

.add-custom-btn:hover {
  transform: scale(1.03);
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.3);
}

.playlist-scroll {
  max-height: 240px;
  overflow-y: auto;
  border-radius: 10px;
  background: white;
  border: 1px solid #f0f0f0;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  gap: 8px;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f8f8f8;
}

.playlist-item:last-child {
  border-bottom: none;
}

.playlist-item:hover {
  background: #f8f9ff;
}

.playlist-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-left: 3px solid #667eea;
}

.item-index {
  width: 24px;
  font-size: 12px;
  color: #aaa;
  font-weight: 500;
}

.item-name {
  flex: 1;
  font-size: 13px;
  color: #333;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-name:hover {
  color: #667eea;
}

.playing-icon {
  font-size: 11px;
  color: #667eea;
  animation: pulse 1s infinite;
}

.remove-btn {
  width: 22px;
  height: 22px;
  border: none;
  background: #fee;
  color: #e74c3c;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s ease;
}

.playlist-item:hover .remove-btn {
  opacity: 1;
}

.remove-btn:hover {
  background: #e74c3c;
  color: white;
  transform: scale(1.1);
}

.loading-tip,
.empty-tip {
  text-align: center;
  padding: 30px 16px;
  color: #999;
  font-size: 13px;
}

.empty-tip a {
  color: #667eea;
  text-decoration: underline;
}

.custom-add-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.dialog-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 380px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.dialog-content h4 {
  margin: 0 0 18px 0;
  font-size: 17px;
  color: #333;
  font-weight: 700;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 13px;
  outline: none;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn {
  padding: 10px 20px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  transition: all 0.25s ease;
}

.cancel-btn:hover {
  border-color: #ccc;
  background: #f8f8f8;
}

.confirm-btn {
  padding: 10px 20px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.25s ease;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 480px) {
  .music-player {
    bottom: 12px;
    right: 12px;
    width: calc(100vw - 24px);
    max-width: 320px;
  }

  .music-player.minimized {
    width: 50px;
    height: 50px;
  }

  .music-player.minimized .track-icon {
    font-size: 22px;
  }
}
</style>
