import { isWechat } from './envDetect'

let sdkReady = false
let sdkConfig = null

export function loadWechatSDK() {
  return new Promise((resolve, reject) => {
    if (!isWechat()) {
      resolve(false)
      return
    }
    
    if (window.wx) {
      resolve(true)
      return
    }
    
    const script = document.createElement('script')
    script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
    script.onload = () => resolve(true)
    script.onerror = () => reject(new Error('Failed to load WeChat SDK'))
    document.head.appendChild(script)
  })
}

export async function initWechatSDK(config) {
  if (!isWechat() || !window.wx) {
    return false
  }
  
  sdkConfig = config
  
  return new Promise((resolve) => {
    window.wx.config({
      debug: config.debug || false,
      appId: config.appId,
      timestamp: config.timestamp,
      nonceStr: config.nonceStr,
      signature: config.signature,
      jsApiList: ['chooseImage', 'getLocation'],
      openTagList: ['wx-open-launch-weapp']
    })
    
    window.wx.ready(() => {
      sdkReady = true
      resolve(true)
    })
    
    window.wx.error((err) => {
      console.error('WeChat SDK config error:', err)
      resolve(false)
    })
  })
}

export function isSDKReady() {
  return sdkReady
}

export function getSDKConfig() {
  return sdkConfig
}
