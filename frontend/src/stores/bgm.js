import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'bgm_state'
const CUSTOM_PLAYLIST_KEY = 'bgm_custom_playlist'
const DATA_VERSION = 'v7.0'
const VERSION_KEY = 'bgm_data_version'

const GITEE_OWNER = 'zhao-zhao-lu123'
const GITEE_REPO = 'yaoyao-music'
const GITEE_BRANCH = 'master'

function buildBuiltinWithUrls() {
  return builtinPlaylist.map(m => ({
    ...m,
    url: `https://gitee.com/api/v5/repos/${GITEE_OWNER}/${GITEE_REPO}/raw/${encodeURIComponent(m.name)}.mp3?ref=${GITEE_BRANCH}`
  }))
}

const builtinPlaylistWithUrls = buildBuiltinWithUrls()

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

  let audio = null
  let playlist = []
  let noticeTimer = null

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
      playlist = [...builtinPlaylistWithUrls]
      apiLoaded.value = true
      if (playlist.length > 0 && currentTrackIndex.value >= playlist.length) {
        currentTrackIndex.value = 0
      }
    } catch (e) {
      playlist = [...builtinPlaylistWithUrls]
      apiLoaded.value = true
    }
  }

  function ensureAudio() {
    if (!audio) {
      audio = new Audio()
      audio.volume = volume.value
      audio.preload = 'auto'

      audio.addEventListener('ended', () => {
        playNext()
      })

      audio.addEventListener('error', (e) => {
        console.error('播放失败:', e)
        bgmLoading.value = false
        isPlaying.value = false
        setTimeout(() => playNext(), 2000)
      })
    }
    return audio
  }

  async function playTrack(index) {
    if (playlist.length === 0) {
      await initFromAPI()
    }

    if (index < 0 || index >= playlist.length) return

    currentTrackIndex.value = index
    const track = playlist[index]
    if (!track || !track.url) {
      playNext()
      return
    }

    const a = ensureAudio()
    bgmLoading.value = true

    try {
      a.src = track.url
      a.load()
      await a.play()
      isPlaying.value = true
      bgmLoading.value = false
      showBgmNotice(`正在播放: ${track.name}`)
    } catch (error) {
      console.error('播放失败:', error)
      bgmLoading.value = false
      isPlaying.value = false
      setTimeout(() => playNext(), 2000)
    }
  }

  function togglePlayPause() {
    const a = ensureAudio()
    if (isPlaying.value) {
      a.pause()
      isPlaying.value = false
    } else {
      if (a.src) {
        a.play().catch(() => {})
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
    apiPlaylist = []
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
    showBgmNotice,
    saveToStorage
  }
})
