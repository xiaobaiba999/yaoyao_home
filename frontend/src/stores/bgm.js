import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'bgm_state'
const CUSTOM_PLAYLIST_KEY = 'bgm_custom_playlist'
const DATA_VERSION = 'v10.0'
const VERSION_KEY = 'bgm_data_version'

// 音乐服务地址
const MUSIC_SERVICE_URL = import.meta.env.VITE_MUSIC_SERVICE_URL || 'http://localhost:8088'

export const useBgmStore = defineStore('bgm', () => {
  const bgmEnabled = ref(true)
  const isPlaying = ref(false)
  const bgmLoading = ref(false)
  const currentTrackIndex = ref(0)
  const volume = ref(0.5)
  const shuffleMode = ref(true)
  const showPlaylist = ref(false)
  const apiLoaded = ref(false)
  const customPlaylist = ref([])
  const hasUserInteracted = ref(false)

  let audio = null
  let playlist = []
  let noticeTimer = null
  let playLock = false
  let retryCount = 0
  const MAX_RETRIES = 3

  const currentTrack = computed(() => {
    if (playlist.length === 0) return null
    return playlist[currentTrackIndex.value] || null
  })

  const totalTracks = computed(() => playlist.length)

  function showBgmNotice(msg) {
    console.log('[BGM]', msg)
  }

  async function initFromAPI() {
    if (apiLoaded.value) return
    try {
      const response = await fetch(`${MUSIC_SERVICE_URL}/api/music/list`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        playlist = result.data.map(m => ({
          id: `music-${m.id}`,
          name: m.name,
          artist: m.artist,
          url: m.fileUrl
        }))
      }
      
      const customTracks = customPlaylist.value.map(t => ({ ...t }))
      playlist = [...playlist, ...customTracks]
      
      apiLoaded.value = true
      if (playlist.length > 0 && currentTrackIndex.value >= playlist.length) {
        currentTrackIndex.value = 0
      }
    } catch (e) {
      console.error('加载音乐列表失败:', e)
      playlist = []
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
        console.error('播放错误:', audio.error)
        bgmLoading.value = false
        isPlaying.value = false
        retryCount++
        if (retryCount <= MAX_RETRIES) {
          console.log(`重试第${retryCount}次...`)
          setTimeout(() => playNext(), 1000)
        } else {
          console.log('达到最大重试次数，停止播放')
          retryCount = 0
        }
      })
    }
    return audio
  }

  async function playTrack(index) {
    if (playLock) return

    if (!hasUserInteracted.value) {
      console.log('等待用户交互后再播放')
      return
    }

    if (playlist.length === 0) {
      await initFromAPI()
    }

    if (index < 0 || index >= playlist.length) {
      console.log('索引超出范围:', index, '播放列表长度:', playlist.length)
      return
    }

    playLock = true
    currentTrackIndex.value = index
    const track = playlist[index]
    if (!track || !track.url) {
      console.log('歌曲URL为空:', track)
      playLock = false
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
      retryCount = 0
      playLock = false
      showBgmNotice(`正在播放: ${track.name}`)
    } catch (error) {
      console.error('播放失败:', error)
      bgmLoading.value = false
      isPlaying.value = false
      playLock = false
      
      if (error.name === 'NotAllowedError') {
        console.log('需要用户交互才能播放')
        return
      }
      
      if (error.name === 'AbortError') {
        console.log('播放被中断')
        return
      }
      
      retryCount++
      if (retryCount <= MAX_RETRIES) {
        setTimeout(() => playNext(), 1000)
      }
    }
  }

  function setUserInteracted() {
    if (!hasUserInteracted.value) {
      hasUserInteracted.value = true
      // 用户首次交互后自动播放
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
    if (playlist.length === 0) return

    let nextIndex
    if (shuffleMode.value) {
      nextIndex = Math.floor(Math.random() * playlist.length)
      if (playlist.length > 1) {
        while (nextIndex === currentTrackIndex.value) {
          nextIndex = Math.floor(Math.random() * playlist.length)
        }
      }
    } else {
      nextIndex = (currentTrackIndex.value + 1) % playlist.length
    }

    console.log('切换下一首，索引:', nextIndex)
    playTrack(nextIndex)
  }

  function playPrev() {
    if (playlist.length === 0) return

    let prevIndex
    if (shuffleMode.value) {
      prevIndex = Math.floor(Math.random() * playlist.length)
    } else {
      prevIndex = (currentTrackIndex.value - 1 + playlist.length) % playlist.length
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
    playlist.push(newTrack)
    showBgmNotice(`已添加: ${name}`)
  }

  function removeTrack(index) {
    if (index >= 0 && index < playlist.length) {
      const track = playlist[index]
      if (track.id.startsWith('custom-')) {
        playlist.splice(index, 1)
        const customIdx = customPlaylist.value.findIndex(t => t.id === track.id)
        if (customIdx >= 0) customPlaylist.value.splice(customIdx, 1)
        if (currentTrackIndex.value >= playlist.length) {
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
    playlist: computed(() => playlist),
    initFromAPI,
    playTrack,
    togglePlayPause,
    playNext,
    playPrev,
    setVolume,
    toggleShuffle,
    togglePlaylist,
    addTrack,
    removeTrack,
    refreshPlaylist,
    setUserInteracted,
    showBgmNotice,
    saveToStorage
  }
})
