import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const STORAGE_KEY = 'bgm_state'

const DEFAULT_PLAYLIST = [
  { name: '小幸运', url: 'https://cdn.gequhai.com/mp3/小幸运.mp3' },
  { name: '告白气球', url: 'https://cdn.gequhai.com/mp3/告白气球.mp3' },
  { name: '晴天', url: 'https://cdn.gequhai.com/mp3/晴天.mp3' },
  { name: '七里香', url: 'https://cdn.gequhai.com/mp3/七里香.mp3' },
  { name: '简单爱', url: 'https://cdn.gequhai.com/mp3/简单爱.mp3' },
  { name: '稻香', url: 'https://cdn.gequhai.com/mp3/稻香.mp3' },
  { name: '遇见', url: 'https://cdn.gequhai.com/mp3/遇见.mp3' },
  { name: '后来', url: 'https://cdn.gequhai.com/mp3/后来.mp3' }
]

function saveBgmState(state) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } catch (e) {
    console.error('保存音乐状态失败', e)
  }
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
    const custom = localStorage.getItem('bgm_custom_playlist')
    if (custom) return JSON.parse(custom)
  } catch (e) {}
  return null
}

export function saveCustomPlaylist(playlist) {
  try {
    localStorage.setItem('bgm_custom_playlist', JSON.stringify(playlist))
  } catch (e) {}
}

export const useBgmStore = defineStore('bgm', () => {
  const customPlaylist = ref(loadCustomPlaylist() || [])
  const playlist = computed(() => {
    if (customPlaylist.value.length > 0) return customPlaylist.value
    return DEFAULT_PLAYLIST
  })

  const savedState = loadBgmState()

  const bgmEnabled = ref(savedState?.enabled ?? true)
  const bgmLoading = ref(false)
  const currentTrackIndex = ref(savedState?.trackIndex ?? 0)
  const volume = ref(savedState?.volume ?? 0.5)
  const showNotice = ref(false)
  const noticeText = ref('')
  const isPlaying = ref(false)
  const showPlaylist = ref(false)
  const currentTime = ref(savedState?.currentTime ?? 0)
  const wasPlaying = ref(savedState?.wasPlaying ?? false)

  let audio = null
  let noticeTimeout = null
  let progressInterval = null

  const currentTrack = computed(() => playlist.value[currentTrackIndex.value])
  const totalTracks = computed(() => playlist.value.length)

  function saveState() {
    saveBgmState({
      enabled: bgmEnabled.value,
      trackIndex: currentTrackIndex.value,
      volume: volume.value,
      currentTime: audio ? audio.currentTime : currentTime.value,
      wasPlaying: isPlaying.value
    })
  }

  function getRandomTrackIndex() {
    if (playlist.value.length <= 1) return 0
    let newIndex
    do {
      newIndex = Math.floor(Math.random() * playlist.value.length)
    } while (newIndex === currentTrackIndex.value)
    return newIndex
  }

  function initAudio() {
    if (audio) return
    audio = new Audio()
    audio.volume = volume.value
    audio.loop = false
    audio.crossOrigin = 'anonymous'
    audio.addEventListener('ended', playNext)
    audio.addEventListener('canplaythrough', () => { bgmLoading.value = false })
    audio.addEventListener('play', () => { isPlaying.value = true })
    audio.addEventListener('pause', () => { isPlaying.value = false })
    audio.addEventListener('error', () => {
      showBgmNotice('音乐加载失败，尝试下一首')
      bgmLoading.value = false
      playNext()
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
    noticeTimeout = setTimeout(() => { showNotice.value = false }, 2000)
  }

  function playTrack(index, restoreTime = 0) {
    if (!audio) initAudio()
    currentTrackIndex.value = index
    audio.src = playlist.value[index].url
    if (restoreTime > 0) {
      audio.addEventListener('loadedmetadata', function onLoaded() {
        audio.currentTime = restoreTime
        audio.removeEventListener('loadedmetadata', onLoaded)
      })
    }
    audio.play().then(() => {
      bgmLoading.value = false
      isPlaying.value = true
      showBgmNotice(`正在播放: ${playlist.value[index].name}`)
      saveState()
    }).catch(() => {
      bgmLoading.value = false
      bgmEnabled.value = false
      isPlaying.value = false
      saveState()
    })
  }

  function playNext() {
    const nextIndex = getRandomTrackIndex()
    if (bgmEnabled.value) playTrack(nextIndex)
  }

  function playPrev() {
    const prevIndex = getRandomTrackIndex()
    playTrack(prevIndex)
  }

  function setVolume(val) {
    volume.value = val
    if (audio) audio.volume = val
    saveState()
  }

  function toggleBGM() {
    if (bgmLoading.value) return
    bgmEnabled.value = !bgmEnabled.value
    if (bgmEnabled.value) {
      bgmLoading.value = true
      showBgmNotice('正在加载音乐...')
      playTrack(currentTrackIndex.value)
    } else {
      stopBGM()
      showBgmNotice('音乐已关闭')
    }
    saveState()
  }

  function togglePlayPause() {
    if (!audio) {
      if (bgmEnabled.value) playTrack(currentTrackIndex.value)
      return
    }
    if (isPlaying.value) audio.pause()
    else audio.play().catch(() => {})
    saveState()
  }

  function startBGM() {
    if (!bgmEnabled.value) return
    if (!audio) initAudio()
    bgmLoading.value = true
    const saved = loadBgmState()
    const restoreTime = saved?.currentTime || 0
    if (saved?.wasPlaying) {
      playTrack(currentTrackIndex.value, restoreTime)
    } else {
      if (!audio.src) {
        audio.src = playlist.value[currentTrackIndex.value].url
        if (restoreTime > 0) {
          audio.addEventListener('loadedmetadata', function onLoaded() {
            audio.currentTime = restoreTime
            audio.removeEventListener('loadedmetadata', onLoaded)
          })
        }
      }
      bgmLoading.value = false
    }
  }

  function stopBGM() {
    if (audio) {
      audio.pause()
      audio.currentTime = 0
    }
    isPlaying.value = false
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
    playTrack(index)
    showPlaylist.value = false
  }

  function setCustomPlaylist(tracks) {
    customPlaylist.value = tracks
    saveCustomPlaylist(tracks)
    currentTrackIndex.value = 0
    if (isPlaying.value && audio) {
      playTrack(0)
    }
  }

  function resetToDefaultPlaylist() {
    customPlaylist.value = []
    localStorage.removeItem('bgm_custom_playlist')
    currentTrackIndex.value = 0
    if (isPlaying.value && audio) {
      playTrack(0)
    }
  }

  return {
    customPlaylist,
    playlist,
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
    currentTime,
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
    setVolume,
    togglePlaylist,
    selectTrack,
    setCustomPlaylist,
    resetToDefaultPlaylist,
    saveState
  }
})
