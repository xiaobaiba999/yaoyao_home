import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'bgm_state'
const CUSTOM_PLAYLIST_KEY = 'bgm_custom_playlist'
const DATA_VERSION = 'v6.0'
const VERSION_KEY = 'bgm_data_version'

// 音乐API地址（使用Vite代理）
const MUSIC_API_BASE = '/music-api'

let defaultPlaylist = []

async function loadDefaultPlaylist() {
  if (defaultPlaylist.length > 0) return defaultPlaylist

  try {
    const response = await fetch(`${MUSIC_API_BASE}/api/music/list`)
    const result = await response.json()

    if (result.success && result.data.length > 0) {
      defaultPlaylist = result.data.map(music => ({
        name: music.name,
        id: music.id,
        url: `${MUSIC_API_BASE}/api/music/play/${music.id}`,
        artist: '本地音乐'
      }))
      console.log(`从音乐API加载了 ${defaultPlaylist.length} 首歌曲`)
      return defaultPlaylist
    }
  } catch (error) {
    console.error('加载音乐列表失败:', error)
  }

  return []
}

function cleanupOldData() {
  const currentVersion = localStorage.getItem(VERSION_KEY)
  if (currentVersion !== DATA_VERSION) {
    console.log('检测到旧版本数据，清理中...')
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(CUSTOM_PLAYLIST_KEY)
    localStorage.setItem(VERSION_KEY, DATA_VERSION)
    console.log('数据清理完成')
  }
}

function saveBgmState(state) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } catch (e) {}
}

function loadBgmState() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) return JSON.parse(saved)
  } catch (e) {}
  return null
}

export function loadCustomPlaylist() {
  try {
    const custom = localStorage.getItem(CUSTOM_PLAYLIST_KEY)
    if (custom) {
      const parsed = JSON.parse(custom)
      return Array.isArray(parsed) ? parsed : []
    }
  } catch (e) {}
  return []
}

export function saveCustomPlaylist(playlist) {
  try {
    localStorage.setItem(CUSTOM_PLAYLIST_KEY, JSON.stringify(playlist))
  } catch (e) {}
}

export const useBgmStore = defineStore('bgm', () => {
  cleanupOldData()

  const customPlaylist = ref(loadCustomPlaylist())
  const playlist = computed(() => {
    if (customPlaylist.value.length > 0) return customPlaylist.value
    return defaultPlaylist
  })

  const savedState = loadBgmState()

  const bgmEnabled = ref(savedState?.enabled ?? false)
  const bgmLoading = ref(false)
  const currentTrackIndex = ref(savedState?.trackIndex ?? 0)
  const volume = ref(savedState?.volume ?? 0.5)
  const showNotice = ref(false)
  const noticeText = ref('')
  const isPlaying = ref(false)
  const showPlaylist = ref(false)
  const currentTime = ref(savedState?.currentTime ?? 0)
  const shuffleMode = ref(true)
  const apiLoaded = ref(false)

  let audio = null
  let noticeTimeout = null
  let progressInterval = null
  let playedHistory = []
  let consecutiveErrors = 0
  const MAX_CONSECUTIVE_ERRORS = 3

  const currentTrack = computed(() => playlist.value[currentTrackIndex.value])
  const totalTracks = computed(() => playlist.value.length)
  const hasValidTracks = computed(() => playlist.value.some(t => t.url && t.url.trim()))

  function saveState() {
    saveBgmState({
      enabled: bgmEnabled.value,
      trackIndex: currentTrackIndex.value,
      volume: volume.value,
      currentTime: audio ? audio.currentTime : currentTime.value,
      shuffleMode: shuffleMode.value
    })
  }

  function getRandomTrackIndex() {
    if (playlist.value.length === 0) return 0
    if (playlist.value.length === 1) return 0

    let newIndex
    let attempts = 0
    do {
      newIndex = Math.floor(Math.random() * playlist.value.length)
      attempts++
      if (attempts > playlist.value.length * 2) break
    } while (newIndex === currentTrackIndex.value)

    return newIndex
  }

  async function initFromAPI() {
    if (defaultPlaylist.length === 0) {
      await loadDefaultPlaylist()
      apiLoaded.value = true
    }
  }

  function initAudio() {
    if (audio) return
    audio = new Audio()
    audio.volume = volume.value
    audio.loop = false
    audio.preload = 'metadata'
    audio.crossOrigin = 'anonymous'

    audio.addEventListener('ended', handleEnded)
    audio.addEventListener('canplaythrough', () => {
      bgmLoading.value = false
    })
    audio.addEventListener('play', () => {
      isPlaying.value = true
    })
    audio.addEventListener('pause', () => {
      isPlaying.value = false
    })
    audio.addEventListener('error', (e) => {
      console.warn('音频错误:', e)
      handleError()
    })
    audio.addEventListener('timeupdate', () => {
      if (audio) currentTime.value = audio.currentTime
    })
    startProgressSaver()
  }

  function startProgressSaver() {
    if (progressInterval) clearInterval(progressInterval)
    progressInterval = setInterval(() => {
      if (audio && isPlaying.value) saveState()
    }, 5000)
  }

  function showBgmNotice(text) {
    noticeText.value = text
    showNotice.value = true
    if (noticeTimeout) clearTimeout(noticeTimeout)
    noticeTimeout = setTimeout(() => { showNotice.value = false }, 2500)
  }

  function handleError() {
    bgmLoading.value = false

    if (audio) {
      audio.pause()
      audio.removeAttribute('src')
      audio.load()
    }

    consecutiveErrors++
    console.warn(`播放错误 (${consecutiveErrors}/${MAX_CONSECUTIVE_ERRORS})`)

    if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
      showBgmNotice('⚠️ 无法播放，请先上传音乐')
      bgmEnabled.value = false
      isPlaying.value = false
      consecutiveErrors = 0
      saveState()
      return
    }

    showBgmNotice(`错误 (${consecutiveErrors}/3)，切换...`)

    setTimeout(() => {
      if (bgmEnabled.value) {
        playNext()
      }
    }, 1000)
  }

  function handleEnded() {
    consecutiveErrors = 0
    playNext()
  }

  function playTrack(index, restoreTime = 0) {
    if (index < 0 || index >= playlist.value.length) {
      console.error('无效的播放索引:', index)
      return
    }

    if (!audio) initAudio()

    const track = playlist.value[index]
    if (!track || !track.url || !track.url.trim()) {
      showBgmNotice('歌曲无效')
      setTimeout(() => {
        if (bgmEnabled.value) playNext()
      }, 500)
      return
    }

    currentTrackIndex.value = index

    if (!playedHistory.includes(index)) {
      playedHistory.push(index)
      if (playedHistory.length > 50) {
        playedHistory.shift()
      }
    }

    bgmLoading.value = true

    audio.src = track.url

    if (restoreTime > 0) {
      audio.addEventListener('loadedmetadata', function onLoaded() {
        audio.currentTime = Math.min(restoreTime, audio.duration || 0)
        audio.removeEventListener('loadedmetadata', onLoaded)
      })
    }

    audio.play().then(() => {
      bgmLoading.value = false
      isPlaying.value = true
      showBgmNotice('🎵 ' + track.name)
      consecutiveErrors = 0
      saveState()
    }).catch((err) => {
      console.warn('播放失败:', err)
      bgmLoading.value = false
      isPlaying.value = false
      handleError()
    })
  }

  function playNext() {
    if (!bgmEnabled.value || playlist.value.length === 0) return

    if (shuffleMode.value) {
      const nextIndex = getRandomTrackIndex()
      playTrack(nextIndex)
    } else {
      const nextIndex = (currentTrackIndex.value + 1) % playlist.value.length
      playTrack(nextIndex)
    }
  }

  function playPrev() {
    if (!bgmEnabled.value || playlist.value.length === 0) return

    if (playedHistory.length > 1) {
      playedHistory.pop()
      const prevIndex = playedHistory[playedHistory.length - 1] || 0
      playTrack(prevIndex)
    } else {
      const prevIndex = getRandomTrackIndex()
      playTrack(prevIndex)
    }
  }

  function setVolume(val) {
    volume.value = Math.max(0, Math.min(1, val))
    if (audio) audio.volume = volume.value
    saveState()
  }

  function toggleShuffle() {
    shuffleMode.value = !shuffleMode.value
    showBgmNotice(shuffleMode.value ? '🔀 随机播放' : '🔁 顺序播放')
    saveState()
  }

  function toggleBGM() {
    if (bgmLoading.value) return

    if (playlist.value.length === 0) {
      showBgmNotice('⚠️ 暂无音乐，请先上传')
      return
    }

    bgmEnabled.value = !bgmEnabled.value

    if (bgmEnabled.value) {
      bgmLoading.value = true
      showBgmNotice('🎵 加载中...')
      consecutiveErrors = 0
      const startIndex = currentTrackIndex.value >= playlist.value.length ? 0 : currentTrackIndex.value
      playTrack(startIndex)
    } else {
      stopBGM()
      showBgmNotice('音乐已关闭')
    }
    saveState()
  }

  function togglePlayPause() {
    if (!audio) {
      if (bgmEnabled.value && playlist.value.length > 0) {
        const index = currentTrackIndex.value >= playlist.value.length ? 0 : currentTrackIndex.value
        playTrack(index)
      }
      return
    }
    if (isPlaying.value) {
      audio.pause()
    } else {
      audio.play().catch(() => {})
    }
    saveState()
  }

  function startBGM() {
    if (!bgmEnabled.value) return
    if (!audio) initAudio()
    bgmLoading.value = true
    const saved = loadBgmState()
    const restoreTime = saved?.currentTime || 0
    if (!audio.src) {
      const track = playlist.value[currentTrackIndex.value]
      if (track && track.url && track.url.trim()) {
        audio.src = track.url
      }
      if (restoreTime > 0) {
        audio.addEventListener('loadedmetadata', function onLoaded() {
          audio.currentTime = Math.min(restoreTime, audio.duration || 0)
          audio.removeEventListener('loadedmetadata', onLoaded)
        })
      }
    }
    bgmLoading.value = false
  }

  function stopBGM() {
    if (audio) {
      audio.pause()
      audio.removeAttribute('src')
      audio.load()
    }
    isPlaying.value = false
    bgmLoading.value = false
    saveState()
  }

  function pauseBGM() {
    if (audio) audio.pause()
    saveState()
  }

  function resumeBGM() {
    if (audio && bgmEnabled.value) audio.play().catch(() => {})
  }

  function togglePlaylist() {
    showPlaylist.value = !showPlaylist.value
  }

  function selectTrack(index) {
    consecutiveErrors = 0
    playTrack(index)
    showPlaylist.value = false
  }

  function addTrack(name, url, id = '') {
    if (!name || !url || !url.trim()) return
    if (isTrackInPlaylist(url.trim())) {
      showBgmNotice('该歌曲已在播放列表中')
      return
    }
    const newCustom = [...customPlaylist.value, { name: name.trim(), url: url.trim(), id }]
    customPlaylist.value = newCustom
    saveCustomPlaylist(newCustom)
    if (!isPlaying.value && bgmEnabled.value) {
      playTrack(newCustom.length - 1)
    }
  }

  function isTrackInPlaylist(url) {
    if (!url) return false
    return customPlaylist.value.some(track => track.url === url.trim())
  }

  function removeTrack(index) {
    const newCustom = customPlaylist.value.filter((_, i) => i !== index)
    customPlaylist.value = newCustom
    saveCustomPlaylist(newCustom)
  }

  function setCustomPlaylist(tracks) {
    customPlaylist.value = tracks.filter(t => t.name && t.url && t.url.trim())
    saveCustomPlaylist(customPlaylist.value)
    currentTrackIndex.value = 0
    if (isPlaying.value && audio && customPlaylist.value.length > 0) {
      playTrack(0)
    }
  }

  function resetToDefaultPlaylist() {
    customPlaylist.value = []
    localStorage.removeItem(CUSTOM_PLAYLIST_KEY)
    currentTrackIndex.value = 0
    consecutiveErrors = 0
    stopBGM()
  }

  async function refreshPlaylist() {
    defaultPlaylist = []
    await loadDefaultPlaylist()
    if (bgmEnabled.value && playlist.value.length > 0) {
      currentTrackIndex.value = 0
    }
  }

  return {
    customPlaylist,
    playlist,
    defaultPlaylist,
    bgmEnabled,
    bgmLoading,
    currentTrackIndex,
    currentTrack,
    volume,
    showNotice,
    noticeText,
    isPlaying,
    showPlaylist,
    totalTracks,
    hasValidTracks,
    currentTime,
    shuffleMode,
    apiLoaded,
    initFromAPI,
    initAudio,
    toggleBGM,
    togglePlayPause,
    startBGM,
    stopBGM,
    pauseBGM,
    resumeBGM,
    showBgmNotice,
    playNext,
    playPrev,
    playTrack,
    setVolume,
    toggleShuffle,
    togglePlaylist,
    selectTrack,
    addTrack,
    removeTrack,
    isTrackInPlaylist,
    setCustomPlaylist,
    resetToDefaultPlaylist,
    saveState,
    refreshPlaylist
  }
})
