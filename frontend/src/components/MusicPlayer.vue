<template>
  <div class="music-player" v-if="bgmStore.bgmEnabled" :class="{ minimized: isMinimized }">
    <div class="player-main" @click="handleMainClick">
      <div class="track-info">
        <div class="track-icon-wrap" :class="{ spinning: bgmStore.isPlaying }">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
            <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55C7.79 13 6 14.79 6 17s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
          </svg>
        </div>
        <div class="track-text" v-if="!isMinimized">
          <span class="track-name">{{ bgmStore.currentTrack?.name || '未在播放' }}</span>
          <span class="track-artist" v-if="bgmStore.currentTrack?.artist">{{ bgmStore.currentTrack.artist }}</span>
        </div>
        <span class="track-index" v-if="!isMinimized && bgmStore.totalTracks > 0 && bgmStore.currentTrack">{{ bgmStore.currentTrackIndex + 1 }}/{{ bgmStore.totalTracks }}</span>
      </div>
      <div class="player-status" v-if="!isMinimized">
        <span v-if="bgmStore.bgmLoading" class="loading-icon">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#667eea" stroke-width="2"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg>
        </span>
        <div v-else-if="bgmStore.isPlaying" class="equalizer">
          <span></span><span></span><span></span><span></span>
        </div>
        <span v-else class="paused-icon">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#999" stroke-width="2"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>
        </span>
      </div>
    </div>

    <div class="player-controls" v-show="bgmStore.showPlaylist" @click.stop>
      <div class="controls-top">
        <button @click="bgmStore.playPrev" class="ctrl-btn" title="上一首">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M6 6h2v12H6zm3.5 6l8.5 6V6z"/></svg>
        </button>
        <button @click="bgmStore.togglePlayPause" class="ctrl-btn play-btn">
          <svg v-if="bgmStore.isPlaying" viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>
          <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
        </button>
        <button @click="bgmStore.playNext" class="ctrl-btn" title="下一首">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/></svg>
        </button>
      </div>

      <div class="shuffle-control">
        <button @click="bgmStore.toggleShuffle" class="shuffle-btn" :class="{ active: bgmStore.shuffleMode }">
          <svg v-if="bgmStore.shuffleMode" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 3 21 3 21 8"/><line x1="4" y1="20" x2="21" y2="3"/><polyline points="21 16 21 21 16 21"/><line x1="15" y1="15" x2="21" y2="21"/><line x1="4" y1="4" x2="9" y2="9"/></svg>
          <svg v-else viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
        </button>
        <span class="shuffle-text">{{ bgmStore.shuffleMode ? '随机播放' : '顺序播放' }}</span>
      </div>

      <div class="volume-control">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#888" stroke-width="2" class="volume-icon"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"/></svg>
        <input type="range" min="0" max="1" step="0.1" :value="bgmStore.volume" @input="bgmStore.setVolume(parseFloat($event.target.value))" class="volume-slider" />
        <span class="volume-value">{{ Math.round(bgmStore.volume * 100) }}%</span>
      </div>

      <div class="playlist-container">
        <div class="playlist-header">
          <span class="playlist-title">🎵 播放列表 ({{ bgmStore.totalTracks }}首)</span>
          <div class="playlist-actions">
            <button @click="handleRefresh" class="icon-btn" title="刷新列表">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
            </button>
            <button @click="showAddCustomDialog = true" class="icon-btn" title="添加音乐">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </button>
          </div>
        </div>
        <div class="playlist-scroll">
          <div v-if="!bgmStore.apiLoaded && bgmStore.totalTracks === 0" class="loading-tip">
            <div class="spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="bgmStore.totalTracks === 0" class="empty-tip">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#ccc" stroke-width="1.5"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>
            <span>暂无音乐</span>
          </div>
          <div
            v-for="(track, index) in bgmStore.playlist"
            :key="track.id || index"
            class="playlist-item"
            :class="{ active: index === bgmStore.currentTrackIndex }"
            @click="bgmStore.playTrack(index)"
          >
            <span class="item-index">{{ index + 1 }}</span>
            <div class="item-info">
              <span class="item-name">{{ track.name }}</span>
              <span class="item-artist" v-if="track.artist">{{ track.artist }}</span>
            </div>
            <div v-if="index === bgmStore.currentTrackIndex && bgmStore.isPlaying" class="playing-eq">
              <span></span><span></span><span></span>
            </div>
            <button v-if="track.id?.startsWith('custom-')" @click.stop="bgmStore.removeTrack(index)" class="remove-btn" title="移除">×</button>
          </div>
        </div>
      </div>
    </div>

    <div class="custom-add-dialog" v-if="showAddCustomDialog" @click.self="showAddCustomDialog = false">
      <div class="dialog-content">
        <h4>添加音乐</h4>
        <div class="form-group">
          <label>歌曲名称</label>
          <input v-model="customSongName" type="text" placeholder="输入歌曲名称" />
        </div>
        <div class="form-group">
          <label>音乐链接</label>
          <input v-model="customSongUrl" type="text" placeholder="输入MP3链接" />
        </div>
        <div class="dialog-actions">
          <button @click="showAddCustomDialog = false" class="cancel-btn">取消</button>
          <button @click="handleAddCustom" class="confirm-btn" :disabled="!customSongName.trim() || !customSongUrl.trim()">添加</button>
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
    if (!bgmStore.showPlaylist) {
      bgmStore.showPlaylist = true
    }
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
  bottom: 30px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 40px rgba(102, 126, 234, 0.15), 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  width: 340px;
  max-height: 560px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.music-player.minimized {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  box-shadow: 0 4px 24px rgba(102, 126, 234, 0.4), 0 0 0 3px rgba(102, 126, 234, 0.1);
  border: none;
  cursor: pointer;
}

.music-player.minimized:hover {
  box-shadow: 0 6px 30px rgba(102, 126, 234, 0.5), 0 0 0 4px rgba(102, 126, 234, 0.15);
  transform: scale(1.08);
}

.player-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.music-player.minimized .player-main {
  padding: 0;
  width: 100%;
  height: 100%;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
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

.track-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.track-icon-wrap.spinning {
  animation: spin 3s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.music-player.minimized .track-icon-wrap {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.track-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.track-name {
  font-size: 13px;
  color: #333;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.track-artist {
  font-size: 11px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.track-index {
  font-size: 11px;
  color: #bbb;
  margin-left: auto;
  flex-shrink: 0;
}

.player-status {
  margin-left: 8px;
  flex-shrink: 0;
}

.equalizer {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 16px;
}

.equalizer span {
  width: 3px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 2px;
  animation: eq 0.8s ease-in-out infinite;
}

.equalizer span:nth-child(1) { height: 60%; animation-delay: 0s; }
.equalizer span:nth-child(2) { height: 100%; animation-delay: 0.2s; }
.equalizer span:nth-child(3) { height: 40%; animation-delay: 0.4s; }
.equalizer span:nth-child(4) { height: 80%; animation-delay: 0.1s; }

@keyframes eq {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}

.playing-eq {
  display: flex;
  align-items: flex-end;
  gap: 1.5px;
  height: 14px;
}

.playing-eq span {
  width: 2.5px;
  background: #667eea;
  border-radius: 1px;
  animation: eq 0.6s ease-in-out infinite;
}

.playing-eq span:nth-child(1) { height: 50%; animation-delay: 0s; }
.playing-eq span:nth-child(2) { height: 100%; animation-delay: 0.15s; }
.playing-eq span:nth-child(3) { height: 60%; animation-delay: 0.3s; }

.player-controls {
  padding: 12px 16px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.6);
  max-height: 440px;
  overflow-y: auto;
}

.controls-top {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.ctrl-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 50%;
  cursor: pointer;
  color: #667eea;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ctrl-btn:hover {
  background: rgba(102, 126, 234, 0.15);
  transform: scale(1.08);
}

.play-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.35);
}

.play-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.45);
}

.shuffle-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 10px;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 20px;
}

.shuffle-btn {
  padding: 4px 8px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #999;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
}

.shuffle-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.shuffle-text {
  font-size: 11px;
  color: #888;
  font-weight: 500;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.volume-icon {
  flex-shrink: 0;
}

.volume-slider {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  appearance: none;
  background: linear-gradient(to right, #667eea, #764ba2);
  border-radius: 2px;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.volume-slider:hover { opacity: 1; }

.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  background: white;
  border: 2px solid #667eea;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
}

.volume-value {
  font-size: 11px;
  color: #999;
  min-width: 32px;
  text-align: right;
  font-weight: 500;
}

.playlist-container { margin-top: 4px; }

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.playlist-title {
  font-size: 12px;
  color: #888;
  font-weight: 600;
}

.playlist-actions {
  display: flex;
  gap: 4px;
}

.icon-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 8px;
  cursor: pointer;
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: rgba(102, 126, 234, 0.15);
  transform: scale(1.05);
}

.playlist-scroll {
  max-height: 200px;
  overflow-y: auto;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.playlist-scroll::-webkit-scrollbar { width: 4px; }
.playlist-scroll::-webkit-scrollbar-thumb { background: #ddd; border-radius: 2px; }

.playlist-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  gap: 8px;
  transition: all 0.2s ease;
  cursor: pointer;
  border-radius: 8px;
  margin: 2px;
}

.playlist-item:hover { background: rgba(102, 126, 234, 0.06); }

.playlist-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.item-index {
  width: 20px;
  font-size: 11px;
  color: #bbb;
  font-weight: 600;
  text-align: center;
  flex-shrink: 0;
}

.playlist-item.active .item-index { color: #667eea; }

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 12px;
  color: #444;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.playlist-item.active .item-name { color: #667eea; font-weight: 600; }

.item-artist {
  font-size: 10px;
  color: #aaa;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.remove-btn {
  width: 20px;
  height: 20px;
  border: none;
  background: #fee;
  color: #e74c3c;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.playlist-item:hover .remove-btn { opacity: 1; }
.remove-btn:hover { background: #e74c3c; color: white; }

.loading-tip, .empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 16px;
  color: #bbb;
  font-size: 12px;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #eee;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.custom-add-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.dialog-content {
  background: white;
  border-radius: 20px;
  padding: 24px;
  width: 90%;
  max-width: 360px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.dialog-content h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
  font-weight: 700;
}

.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 12px; color: #888; margin-bottom: 6px; font-weight: 500; }
.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid #eee;
  border-radius: 10px;
  font-size: 13px;
  outline: none;
  transition: all 0.25s ease;
  box-sizing: border-box;
}
.form-group input:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1); }

.dialog-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.cancel-btn {
  padding: 8px 18px;
  border: 1.5px solid #eee;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  transition: all 0.2s;
}
.cancel-btn:hover { border-color: #ddd; background: #fafafa; }
.confirm-btn {
  padding: 8px 18px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
}
.confirm-btn:hover:not(:disabled) { box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35); }
.confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 480px) {
  .music-player { bottom: 20px; right: 12px; width: calc(100vw - 24px); max-width: 320px; }
  .music-player.minimized { width: 48px; height: 48px; }
}
</style>
