<template>
  <router-view />
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { getMessages } from '@/api'
import { useMessageStore } from '@/stores/message'

const router = useRouter()
const messageStore = useMessageStore()

let ws = null
let pmTimer = null
let wsConnected = false
let reconnectAttempts = 0
let reconnectTimer = null
const MAX_RECONNECT_ATTEMPTS = 5

/** 从 localStorage 加载并应用主题颜色 */
const applyStoredTheme = () => {
  try {
    const storedInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const color = storedInfo.themeColor
    if (color) {
      const root = document.documentElement
      root.style.setProperty('--accent-color', color)
      root.style.setProperty('--accent-dark', adjustColor(color, -20))
      root.style.setProperty('--accent-light', adjustColor(color, 20))
      root.style.setProperty('--sidebar-active-bg', color)
    }
  } catch { /* ignore */ }
}

const adjustColor = (hex, amount) => {
  if (!hex || !hex.startsWith('#')) return hex
  let r, g, b
  const num = parseInt(hex.slice(1), 16)
  r = (num >> 16) & 0xff
  g = (num >> 8) & 0xff
  b = num & 0xff
  r = Math.max(0, Math.min(255, r + amount))
  g = Math.max(0, Math.min(255, g + amount))
  b = Math.max(0, Math.min(255, b + amount))
  return `#${((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')}`
}

/** 弹出新私信通知 */
function notifyPm(name, content, fromUserId) {
  ElNotification({
    title: '新私信',
    message: `${name || '用户'}: ${(content || '').slice(0, 30)}`,
    type: 'info',
    duration: 5000,
    onClick: () => router.push({ path: '/messages', query: fromUserId ? { to: fromUserId, name: name } : {} }),
  })
}

/** 兜底轮询：仅当 WebSocket 未连接时使用 */
async function checkNewMessages() {
  try {
    const res = await getMessages()
    const unread = res.data?.unread || 0
    if (unread > messageStore.unread) {
      const list = res.data?.list || []
      const latest = list[0]
      if (latest) notifyPm(latest.nickname, latest.lastMessage, latest.userId)
    }
    messageStore.setUnread(unread)
  } catch { /* 忽略 */ }
}

function startPolling() {
  // 停掉重连定时器
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  if (pmTimer) return
  checkNewMessages()
  pmTimer = setInterval(checkNewMessages, 20000)
}

function stopPolling() {
  if (pmTimer) { clearInterval(pmTimer); pmTimer = null }
}

function teardownNotify() {
  stopPolling()
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  if (ws) {
    ws.onclose = null
    ws.onerror = null
    try { ws.close() } catch {}
    ws = null
  }
}

/** 建立通知 WebSocket，接收服务端推送的私信。断线后指数退避重连，重连不上则降级轮询 */
function connectNotify() {
  const token = localStorage.getItem('token')
  if (!token) { startPolling(); return }

  // 清理旧连接
  if (ws) {
    ws.onclose = null
    ws.onerror = null
    try { ws.close() } catch {}
    ws = null
  }

  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${location.host}/ws/notify?token=${encodeURIComponent(token)}`
  try {
    ws = new WebSocket(url)
  } catch (e) {
    scheduleNotifyReconnect()
    return
  }

  ws.onopen = () => {
    wsConnected = true
    reconnectAttempts = 0
    stopPolling()
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'pm') {
        messageStore.incr()
        notifyPm(msg.fromNickname, msg.content, msg.fromUserId)
      }
    } catch { /* 忽略非法消息 */ }
  }

  ws.onclose = () => {
    wsConnected = false
    scheduleNotifyReconnect()
  }

  ws.onerror = () => {
    wsConnected = false
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
      try { ws.close() } catch {}
    }
  }
}

/** 指数退避重连通知 WS；超过最大次数后降级为轮询 */
function scheduleNotifyReconnect() {
  if (reconnectTimer) return
  reconnectAttempts++
  if (reconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
    // 重连次数耗尽，降级为接口轮询
    startPolling()
    return
  }
  const delay = Math.min(1000 * Math.pow(2, reconnectAttempts - 1), 30000) // 1s, 2s, 4s, 8s, 16s, max 30s
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectNotify()
  }, delay)
}

onMounted(() => {
  applyStoredTheme()
  if (localStorage.getItem('token')) {
    // 首次拉取未读数
    getMessages().then(res => messageStore.setUnread(res.data?.unread || 0)).catch(() => {})
    connectNotify()
  }
})

onBeforeUnmount(() => {
  teardownNotify()
})
</script>

<style>
/* App.vue只做路由出口，不包含任何布局 */
</style>
