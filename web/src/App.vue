<template>
  <router-view />
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { getMessages } from '@/api'

const router = useRouter()
let pmTimer = null
let lastUnread = 0

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

/** 检查新私信，有则弹出 Notification */
async function checkNewMessages() {
  try {
    const res = await getMessages()
    const unread = res.data?.unread || 0
    if (unread > lastUnread) {
      const list = res.data?.list || []
      const latest = list.find(m => m.isread === 0 || m.isread === false)
      if (latest) {
        const name = latest.sourcename || latest.from || '用户'
        ElNotification({
          title: '新私信',
          message: `${name}: ${(latest.content || '').slice(0, 30)}`,
          type: 'info',
          duration: 5000,
          onClick: () => router.push('/messages'),
        })
      }
    }
    lastUnread = unread
  } catch { /* 忽略 */ }
}

onMounted(() => {
  applyStoredTheme()
  // 首次检查
  const token = localStorage.getItem('token')
  if (token) {
    checkNewMessages()
    // 每 20 秒检查新私信
    pmTimer = setInterval(checkNewMessages, 20000)
  }
})

onBeforeUnmount(() => {
  if (pmTimer) clearInterval(pmTimer)
})
</script>

<style>
/* App.vue只做路由出口，不包含任何布局 */
</style>
