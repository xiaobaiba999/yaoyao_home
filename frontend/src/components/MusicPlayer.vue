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
    
    <div class="player-controls" v-if="bgmStore.showPlaylist && !isMinimized" @click.stop>
      <div class="controls-top">
        <button @click="bgmStore.playPrev" class="ctrl-btn" title="上一首">⏮</button>
        <button @click="bgmStore.togglePlayPause" class="ctrl-btn play-btn">
          {{ bgmStore.isPlaying ? '⏸' : '▶' }}
        </button>
        <button @click="bgmStore.playNext" class="ctrl-btn" title="下一首">⏭</button>
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
        <div class="playlist-header">播放列表</div>
        <div class="playlist-scroll">
          <div 
            v-for="(track, index) in bgmStore.playlist" 
            :key="index"
            class="playlist-item"
            :class="{ active: index === bgmStore.currentTrackIndex }"
            @click="bgmStore.playTrack(index)"
          >
            <span class="item-index">{{ index + 1 }}</span>
            <span class="item-name">{{ track.name }}</span>
            <span v-if="index === bgmStore.currentTrackIndex && bgmStore.isPlaying" class="playing-icon">▶</span>
          </div>
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
let lastScrollY = 0
let scrollTimeout = null

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

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (scrollTimeout) {
    clearTimeout(scrollTimeout)
  }
})
</script>

<style scoped>
.music-player {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 200px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.music-player.minimized {
  min-width: 50px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.player-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.music-player.minimized .player-main {
  padding: 0;
  width: 100%;
  height: 100%;
  justify-content: center;
}

.track-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.music-player.minimized .track-info {
  gap: 0;
}

.track-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.music-player.minimized .track-icon {
  font-size: 24px;
}

.track-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.track-index {
  font-size: 12px;
  color: #999;
}

.player-status {
  font-size: 16px;
  transition: all 0.3s ease;
}

.music-player.minimized .player-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 10px;
}

.playing {
  color: #667eea;
}

.paused, .loading {
  color: #999;
}

.player-controls {
  padding: 12px 16px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.controls-top {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 12px;
}

.ctrl-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
}

.ctrl-btn:hover {
  background: #e0e0e0;
  transform: scale(1.1);
}

.play-btn {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 14px;
}

.play-btn:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4192 100%);
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.volume-icon {
  font-size: 14px;
}

.volume-slider {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  appearance: none;
  background: #e0e0e0;
  border-radius: 2px;
  cursor: pointer;
}

.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  background: #667eea;
  border-radius: 50%;
  cursor: pointer;
}

.volume-value {
  font-size: 12px;
  color: #666;
  min-width: 35px;
}

.playlist-container {
  margin-top: 10px;
}

.playlist-header {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.playlist-scroll {
  max-height: 200px;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.playlist-item:hover {
  background: #f5f5f5;
}

.playlist-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.item-index {
  width: 25px;
  font-size: 12px;
  color: #999;
}

.item-name {
  flex: 1;
  font-size: 13px;
  color: #333;
}

.playing-icon {
  font-size: 12px;
  color: #667eea;
}

@media (max-width: 480px) {
  .music-player {
    bottom: 10px;
    right: 10px;
    min-width: 180px;
  }
  
  .music-player.minimized {
    min-width: 44px;
    width: 44px;
    height: 44px;
  }
  
  .music-player.minimized .track-icon {
    font-size: 20px;
  }
}
</style>
