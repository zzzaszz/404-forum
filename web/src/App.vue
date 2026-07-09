<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

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

onMounted(() => {
  applyStoredTheme()
})
</script>

<style>
/* App.vue只做路由出口，不包含任何布局 */
</style>
