import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'bgm_state'
const CUSTOM_PLAYLIST_KEY = 'bgm_custom_playlist'
const DATA_VERSION = 'v11.0'
const VERSION_KEY = 'bgm_data_version'

const MUSIC_SERVICE_URL = import.meta.env.VITE_MUSIC_SERVICE_URL || 'http://localhost:8088'

export const useBgmStore = defineStore('bgm', () => {
  const bgmEnabled = ref(true)
  const isPlaying = ref(false)
  const bgmLoading = ref(false)
  const currentTrackIndex = ref(0)
  const volume = ref(0.5)
  const shuffleMode = ref(true)
  const showPlaylist = ref(true)
  const apiLoaded = ref(false)
  const customPlaylist = ref([])
  const hasUserInteracted = ref(false)
  const playlist = ref([])

  let audio = null
  let noticeTimer = null
  let playLock = false
  let retryCount = 0
  const MAX_RETRIES = 3
  let wasPlayingBeforeHidden = false
  const failedTracks = new Set()
  let consecutiveFailures = 0

  const currentTrack = computed(() => {
    if (playlist.value.length === 0) return null
    return playlist.value[currentTrackIndex.value] || null
  })

  const totalTracks = computed(() => playlist.value.length)

  function showBgmNotice(msg) {
    console.log('[BGM]', msg)
  }

  async function initFromAPI() {
    if (apiLoaded.value) return
    try {
      console.log('[BGM] 开始从音乐服务获取列表:', MUSIC_SERVICE_URL)
      const response = await fetch(`${MUSIC_SERVICE_URL}/api/music/list`)
      const result = await response.json()
      
      const apiTracks = []
      if (result.code === 200 && result.data && result.data.length > 0) {
        result.data.forEach(m => {
          apiTracks.push({
            id: `music-${m.id}`,
            name: m.name || '未知',
            artist: m.artist || '未知',
            url: m.fileUrl
          })
        })
        console.log('[BGM] 加载音乐列表:', apiTracks.length, '首')
      } else {
        console.warn('[BGM] 音乐列表为空或返回错误')
      }
      
      const customTracks = customPlaylist.value.map(t => ({ ...t }))
      playlist.value = [...apiTracks, ...customTracks]
      
      apiLoaded.value = true
      if (playlist.value.length > 0) {
        currentTrackIndex.value = Math.floor(Math.random() * playlist.value.length)
      }
    } catch (e) {
      console.error('[BGM] 加载音乐列表失败:', e)
      playlist.value = []
      apiLoaded.value = true
    }
  }

  function ensureAudio() {
    if (!audio) {
      audio = new Audio()
      audio.volume = volume.value
      audio.preload = 'auto'

      audio.addEventListener('ended', () => {
        retryCount = 0
        playNext()
      })

      audio.addEventListener('error', (e) => {
        console.error('播放错误:', audio.error?.code, audio.error?.message)
        bgmLoading.value = false
        isPlaying.value = false

        const track = playlist.value[currentTrackIndex.value]
        if (track) {
          failedTracks.add(track.id)
          console.warn('[BGM] 标记为不可播放:', track.name, track.url)
        }

        consecutiveFailures++
        if (consecutiveFailures >= playlist.value.length || consecutiveFailures >= 5) {
          console.warn('[BGM] 连续失败过多，停止播放')
          consecutiveFailures = 0
          return
        }

        setTimeout(() => playNext(), 800)
      })
    }
    return audio
  }

  function handleVisibilityChange() {
    if (document.hidden) {
      if (isPlaying.value && bgmEnabled.value) {
        wasPlayingBeforeHidden = true
        const a = ensureAudio()
        a.pause()
        isPlaying.value = false
      }
    } else {
      if (wasPlayingBeforeHidden && bgmEnabled.value && hasUserInteracted.value) {
        wasPlayingBeforeHidden = false
        const a = ensureAudio()
        if (a.src) {
          a.play().catch(() => {})
          isPlaying.value = true
        }
      }
    }
  }

  function initVisibilityListener() {
    document.addEventListener('visibilitychange', handleVisibilityChange)
  }

  function cleanupVisibilityListener() {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  }

  async function playTrack(index) {
    if (playLock) return

    if (!hasUserInteracted.value) {
      console.log('等待用户交互后再播放')
      return
    }

    if (playlist.value.length === 0) {
      await initFromAPI()
    }

    if (index < 0 || index >= playlist.value.length) {
      return
    }

    const track = playlist.value[index]
    if (track && failedTracks.has(track.id)) {
      console.warn('[BGM] 跳过不可播放的曲目:', track.name)
      const nextIdx = findNextPlayable(index)
      if (nextIdx === -1 || nextIdx === index) {
        console.warn('[BGM] 没有可播放的曲目')
        return
      }
      playTrack(nextIdx)
      return
    }

    playLock = true
    currentTrackIndex.value = index
    if (!track || !track.url) {
      playLock = false
      failedTracks.add(track?.id || 'empty')
      playNext()
      return
    }

    console.log('正在播放:', track.name, track.artist)
    const a = ensureAudio()
    bgmLoading.value = true

    try {
      a.src = track.url
      await a.play()
      isPlaying.value = true
      bgmLoading.value = false
      consecutiveFailures = 0
      playLock = false
      showBgmNotice(`正在播放: ${track.name}`)
    } catch (error) {
      console.error('播放失败:', error)
      bgmLoading.value = false
      isPlaying.value = false
      playLock = false

      if (track) failedTracks.add(track.id)
      
      if (error.name === 'NotAllowedError') {
        console.log('需要用户交互才能播放')
        return
      }

      if (error.name === 'AbortError') {
        console.log('播放被中断')
        return
      }

      consecutiveFailures++
      if (consecutiveFailures < playlist.value.length && consecutiveFailures < 5) {
        setTimeout(() => playNext(), 800)
      }
    }
  }

  function findNextPlayable(fromIndex) {
    const len = playlist.value.length
    if (len === 0) return -1
    for (let i = 1; i <= len; i++) {
      const idx = (fromIndex + i) % len
      const track = playlist.value[idx]
      if (track && !failedTracks.has(track.id) && track.url) {
        return idx
      }
    }
    return -1
  }

  function setUserInteracted() {
    if (!hasUserInteracted.value) {
      hasUserInteracted.value = true
      setTimeout(() => {
        playTrack(currentTrackIndex.value)
      }, 100)
    }
  }

  function togglePlayPause() {
    setUserInteracted()

    const a = ensureAudio()
    if (isPlaying.value) {
      a.pause()
      isPlaying.value = false
    } else {
      if (a.src) {
        a.play().catch((err) => {
          console.log('播放失败，尝试下一首:', err)
          playNext()
        })
        isPlaying.value = true
      } else {
        playTrack(currentTrackIndex.value)
      }
    }
  }

  function playNext() {
    if (playlist.value.length === 0) return

    let nextIndex
    if (shuffleMode.value) {
      nextIndex = Math.floor(Math.random() * playlist.value.length)
      if (playlist.value.length > 1) {
        while (nextIndex === currentTrackIndex.value) {
          nextIndex = Math.floor(Math.random() * playlist.value.length)
        }
      }
    } else {
      nextIndex = (currentTrackIndex.value + 1) % playlist.value.length
    }

    playTrack(nextIndex)
  }

  function playPrev() {
    if (playlist.value.length === 0) return

    let prevIndex
    if (shuffleMode.value) {
      prevIndex = Math.floor(Math.random() * playlist.value.length)
    } else {
      prevIndex = (currentTrackIndex.value - 1 + playlist.value.length) % playlist.value.length
    }

    playTrack(prevIndex)
  }

  function setVolume(v) {
    volume.value = v
    if (audio) audio.volume = v
  }

  function toggleShuffle() {
    shuffleMode.value = !shuffleMode.value
    showBgmNotice(shuffleMode.value ? '随机播放' : '顺序播放')
  }

  function toggleBGM() {
    bgmEnabled.value = !bgmEnabled.value
    if (!bgmEnabled.value) {
      const a = ensureAudio()
      a.pause()
      a.src = ''
      isPlaying.value = false
    }
    showBgmNotice(bgmEnabled.value ? '背景音乐已开启' : '背景音乐已关闭')
  }

  function togglePlaylist() {
    showPlaylist.value = !showPlaylist.value
  }

  function addTrack(name, url) {
    const newTrack = {
      name,
      id: `custom-${Date.now()}`,
      url,
      artist: '自定义'
    }
    customPlaylist.value.push(newTrack)
    playlist.value.push(newTrack)
    showBgmNotice(`已添加: ${name}`)
  }

  function removeTrack(index) {
    if (index >= 0 && index < playlist.value.length) {
      const track = playlist.value[index]
      if (track.id.startsWith('custom-')) {
        playlist.value.splice(index, 1)
        const customIdx = customPlaylist.value.findIndex(t => t.id === track.id)
        if (customIdx >= 0) customPlaylist.value.splice(customIdx, 1)
        if (currentTrackIndex.value >= playlist.value.length) {
          currentTrackIndex.value = 0
        }
      }
    }
  }

  async function refreshPlaylist() {
    apiLoaded.value = false
    await initFromAPI()
  }

  function loadFromStorage() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        const data = JSON.parse(saved)
        volume.value = data.volume ?? 0.5
        shuffleMode.value = data.shuffleMode ?? true
        bgmEnabled.value = data.bgmEnabled ?? true
        currentTrackIndex.value = data.currentTrackIndex ?? 0
      }

      const savedVersion = localStorage.getItem(VERSION_KEY)
      if (savedVersion !== DATA_VERSION) {
        localStorage.removeItem(CUSTOM_PLAYLIST_KEY)
        localStorage.setItem(VERSION_KEY, DATA_VERSION)
      }

      const savedCustom = localStorage.getItem(CUSTOM_PLAYLIST_KEY)
      if (savedCustom) {
        customPlaylist.value = JSON.parse(savedCustom)
      }
    } catch (e) {
      console.error('加载BGM状态失败:', e)
    }
  }

  function saveToStorage() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        volume: volume.value,
        shuffleMode: shuffleMode.value,
        bgmEnabled: bgmEnabled.value,
        currentTrackIndex: currentTrackIndex.value
      }))
      localStorage.setItem(CUSTOM_PLAYLIST_KEY, JSON.stringify(customPlaylist.value))
    } catch (e) {
      console.error('保存BGM状态失败:', e)
    }
  }

  loadFromStorage()

  return {
    bgmEnabled,
    isPlaying,
    bgmLoading,
    currentTrackIndex,
    volume,
    shuffleMode,
    showPlaylist,
    apiLoaded,
    customPlaylist,
    hasUserInteracted,
    currentTrack,
    totalTracks,
    playlist,
    initFromAPI,
    playTrack,
    togglePlayPause,
    playNext,
    playPrev,
    setVolume,
    toggleShuffle,
    togglePlaylist,
    toggleBGM,
    addTrack,
    removeTrack,
    refreshPlaylist,
    setUserInteracted,
    showBgmNotice,
    saveToStorage,
    initVisibilityListener,
    cleanupVisibilityListener
  }
})
