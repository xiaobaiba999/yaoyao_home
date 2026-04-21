import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'bgm_state'
const CUSTOM_PLAYLIST_KEY = 'bgm_custom_playlist'
const DATA_VERSION = 'v7.0'
const VERSION_KEY = 'bgm_data_version'

const MUSIC_API_BASE = import.meta.env.VITE_MUSIC_API_URL || ''

const builtinPlaylist = [
  { name: '晴天', artist: '周杰伦', id: 'm1' },
  { name: '稻香', artist: '周杰伦', id: 'm2' },
  { name: '花海', artist: '周杰伦', id: 'm3' },
  { name: '发如雪', artist: '周杰伦', id: 'm4' },
  { name: '反方向的钟', artist: '周杰伦', id: 'm5' },
  { name: '给我一首歌的时间', artist: '周杰伦', id: 'm6' },
  { name: '明明就', artist: '周杰伦', id: 'm7' },
  { name: '蒲公英的约定', artist: '周杰伦', id: 'm8' },
  { name: '一路向北', artist: '周杰伦', id: 'm9' },
  { name: '修炼爱情', artist: '林俊杰', id: 'm10' },
  { name: '有何不可', artist: '许嵩', id: 'm11' },
  { name: '清明雨上', artist: '许嵩', id: 'm12' },
  { name: '泡沫', artist: '邓紫棋', id: 'm13' },
  { name: '绿光', artist: '孙燕姿', id: 'm14' },
  { name: '开始懂了', artist: '孙燕姿', id: 'm15' },
  { name: '我怀念的', artist: '孙燕姿', id: 'm16' },
  { name: '遇见', artist: '孙燕姿', id: 'm17' },
  { name: '爱丫爱丫', artist: 'BY2', id: 'm18' },
  { name: '倒数', artist: '邓紫棋', id: 'm19' },
  { name: '唯一', artist: '王力宏', id: 'm20' },
  { name: '再见', artist: '邓紫棋', id: 'm21' },
  { name: '过活', artist: '棉子', id: 'm22' },
  { name: 'Aways online', artist: '林俊杰', id: 'm23' }
]

function buildBuiltinWithUrls() {
  const GITEE_OWNER = 'zhao-zhao-lu123'
  const GITEE_REPO = 'yaoyao-music'
  const GITEE_BRANCH = 'master'
  return builtinPlaylist.map(m => ({
    ...m,
    url: `https://gitee.com/${GITEE_OWNER}/${GITEE_REPO}/raw/${GITEE_BRANCH}/${encodeURIComponent(m.name)}.mp3`
  }))
}

const builtinPlaylistWithUrls = buildBuiltinWithUrls()

let apiPlaylist = []

async function loadApiPlaylist() {
  if (apiPlaylist.length > 0) return apiPlaylist
  if (!MUSIC_API_BASE) return []

  try {
    const response = await fetch(`${MUSIC_API_BASE}/api/music/list`)
    const result = await response.json()

    if (result.success && result.data && result.data.length > 0) {
      apiPlaylist = result.data.map(music => ({
        name: music.name,
        id: `api-${music.id}`,
        url: `${MUSIC_API_BASE}/api/music/play/${music.id}`,
        artist: '本地音乐'
      }))
      console.log(`从音乐API加载了 ${apiPlaylist.length} 首歌曲`)
      return apiPlaylist
    }
  } catch (error) {
    console.log('音乐API不可用，使用内置列表')
  }

  return []
}

async function getAllPlaylist() {
  const apiList = await loadApiPlaylist()
  return [...apiList, ...builtinPlaylistWithUrls]
}

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
      playlist = await getAllPlaylist()
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
