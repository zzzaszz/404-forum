import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 消息未读数全局状态：由 WebSocket 推送 / 轮询 / 会话已读共同维护，
 * Dashboard 私信入口、Messages 等组件共享同一份数据，避免各自重复请求。
 */
export const useMessageStore = defineStore('message', () => {
  const unread = ref(0)

  function setUnread(n) {
    unread.value = n || 0
  }

  function incr(delta = 1) {
    unread.value += delta
  }

  function decr(delta = 1) {
    unread.value = Math.max(0, unread.value - delta)
  }

  return { unread, setUnread, incr, decr }
})
