import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const STORAGE_KEY = 'bgm_state'

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
    if (saved) {
      return JSON.parse(saved)
    }
  } catch (e) {
    console.error('加载音乐状态失败', e)
  }
  return null
}

export const useBgmStore = defineStore('bgm', () => {
  const playlist = [
    { name: '音乐 1', path: '/music/1171348260 (1).mp3' },
    { name: '音乐 2', path: '/music/1171348260 (2).mp3' },
    { name: '音乐 3', path: '/music/1171348260 (3).mp3' },
    { name: '音乐 4', path: '/music/1171348260 (4).mp3' },
    { name: '音乐 5', path: '/music/1171348260 (5).mp3' },
    { name: '音乐 6', path: '/music/1171348260 (6).mp3' },
    { name: '音乐 7', path: '/music/1521237415.mp3' },
    { name: '音乐 8', path: '/music/1889406312.mp3' },
    { name: '音乐 9', path: '/music/2044197871.mp3' },
    { name: '音乐 10', path: '/music/2095703260.mp3' },
    { name: '音乐 11', path: '/music/2640583885.mp3' },
    { name: '音乐 12', path: '/music/292700883.mp3' },
    { name: '音乐 13', path: '/music/2958697143.mp3' },
    { name: '音乐 14', path: '/music/349676572.mp3' },
    { name: '音乐 15', path: '/music/M500000AZdTD09A7jL.mp3' },
    { name: '音乐 16', path: '/music/M500000DOpDe2GfLaq.mp3' },
    { name: '音乐 17', path: '/music/M500000ZNCIp1jrP7X.mp3' },
    { name: '音乐 18', path: '/music/M500000bYDlc2XxKLs.mp3' },
    { name: '音乐 19', path: '/music/M500000gGqwl19VBA4.mp3' },
    { name: '音乐 20', path: '/music/M500000mfmWj21qzTt.mp3' },
    { name: '音乐 21', path: '/music/M5000017K7gL4WYnw2 (1).mp3' },
    { name: '音乐 22', path: '/music/M5000017K7gL4WYnw2.mp3' },
    { name: '音乐 23', path: '/music/M500001ldhgp3VP4iO.mp3' },
    { name: '音乐 24', path: '/music/M500001ormn821aChr.mp3' },
    { name: '音乐 25', path: '/music/M500001r8VMR24xJHa.mp3' },
    { name: '音乐 26', path: '/music/M500001xjGaz08GAGC.mp3' },
    { name: '音乐 27', path: '/music/M500001ziKgJ3o5Ipp.mp3' },
    { name: '音乐 28', path: '/music/M500002kr0ss3h76nL.mp3' },
    { name: '音乐 29', path: '/music/M500003aAYrm3GE0Ac.mp3' },
    { name: '音乐 30', path: '/music/M500003c89uw1rfLwc.mp3' },
    { name: '音乐 31', path: '/music/M500004Yi5BD3ksoAN.mp3' },
    { name: '音乐 32', path: '/music/M500004Z8Ihr0JIu5s.mp3' }
  ]

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

  const currentTrack = computed(() => playlist[currentTrackIndex.value])
  const totalTracks = computed(() => playlist.length)

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
    if (playlist.length <= 1) return 0
    
    let newIndex
    do {
      newIndex = Math.floor(Math.random() * playlist.length)
    } while (newIndex === currentTrackIndex.value)
    
    return newIndex
  }

  function initAudio() {
    if (audio) return
    
    audio = new Audio()
    audio.volume = volume.value
    audio.loop = false
    
    audio.addEventListener('ended', () => {
      playNext()
    })
    audio.addEventListener('canplaythrough', () => {
      bgmLoading.value = false
    })
    audio.addEventListener('play', () => {
      isPlaying.value = true
    })
    audio.addEventListener('pause', () => {
      isPlaying.value = false
    })
    audio.addEventListener('error', () => {
      showBgmNotice('音乐加载失败，尝试下一首')
      bgmLoading.value = false
      playNext()
    })
    audio.addEventListener('timeupdate', () => {
      if (audio) {
        currentTime.value = audio.currentTime
      }
    })

    startProgressSaver()
  }

  function startProgressSaver() {
    if (progressInterval) clearInterval(progressInterval)
    progressInterval = setInterval(() => {
      if (audio && isPlaying.value) {
        saveState()
      }
    }, 5000)
  }

  function showBgmNotice(text) {
    noticeText.value = text
    showNotice.value = true
    if (noticeTimeout) clearTimeout(noticeTimeout)
    noticeTimeout = setTimeout(() => {
      showNotice.value = false
    }, 2000)
  }

  function playTrack(index, restoreTime = 0) {
    if (!audio) initAudio()
    
    currentTrackIndex.value = index
    
    audio.src = playlist[index].path
    
    if (restoreTime > 0) {
      audio.addEventListener('loadedmetadata', function onLoaded() {
        audio.currentTime = restoreTime
        audio.removeEventListener('loadedmetadata', onLoaded)
      })
    }
    
    audio.play().then(() => {
      bgmLoading.value = false
      isPlaying.value = true
      showBgmNotice(`正在播放: ${playlist[index].name}`)
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
    if (bgmEnabled.value) {
      playTrack(nextIndex)
    }
  }

  function playPrev() {
    const prevIndex = getRandomTrackIndex()
    playTrack(prevIndex)
  }

  function setVolume(val) {
    volume.value = val
    if (audio) {
      audio.volume = val
    }
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
      if (bgmEnabled.value) {
        playTrack(currentTrackIndex.value)
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
    
    const savedState = loadBgmState()
    const restoreTime = savedState?.currentTime || 0
    
    if (savedState?.wasPlaying) {
      playTrack(currentTrackIndex.value, restoreTime)
    } else {
      if (!audio.src) {
        audio.src = playlist[currentTrackIndex.value].path
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
    if (audio) {
      audio.pause()
    }
    saveState()
  }

  function resumeBGM() {
    if (audio && bgmEnabled.value) {
      audio.play().catch(() => {})
    }
  }

  function togglePlaylist() {
    showPlaylist.value = !showPlaylist.value
  }

  function selectTrack(index) {
    playTrack(index)
    showPlaylist.value = false
  }

  return {
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
    saveState
  }
})
