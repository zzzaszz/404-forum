<script>
export default { name: 'ChatRoom' }
</script>

<template>
  <div class="chat-room">
    <div class="chat-header">
      <div class="header-left">
        <h2>{{ currentRoomName }}</h2>
        <el-select v-model="currentRoom" size="small" style="width:160px" @change="switchRoom">
          <el-option
            v-for="r in roomList"
            :key="r.roomId"
            :label="`${r.name || '房间' + r.roomId} (${r.online}/${r.max})`"
            :value="r.roomId"
            :disabled="r.online >= r.max"
          />
        </el-select>
      </div>
      <div class="online-info">
        <span class="online-dot"></span>
        <span>{{ onlineCount }} / {{ maxPerRoom }} 人在线</span>
      </div>
    </div>

    <!-- 连接中 / 断线 -->
    <div v-if="!connected" class="chat-status">
      <el-icon :size="40" class="is-loading"><Loading /></el-icon>
      <p>连接中...</p>
      <span class="status-sub">正在建立实时通信</span>
    </div>

    <!-- 消息列表 -->
    <div v-else ref="msgBox" class="chat-messages">
      <div v-for="msg in messages" :key="msg.id" class="message" :class="{ system: msg.type !== 'chat' }">
        <template v-if="msg.type === 'chat'">
          <div class="msg-avatar" :style="{ background: msg.color }">{{ msg.nickname.charAt(0) }}</div>
          <div class="msg-body">
            <div class="msg-header">
              <span class="msg-nickname">{{ msg.nickname }}</span>
              <span class="msg-time">{{ msg.time }}</span>
            </div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
        </template>
        <template v-else>
          <div class="system-msg">{{ msg.content }}</div>
        </template>
      </div>
      <div v-if="messages.length === 0" class="empty-hint">暂无消息，来打个招呼吧</div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputText"
        placeholder="输入消息..."
        size="large"
        clearable
        :disabled="!connected"
        @keydown.enter.prevent="sendMsg"
      >
        <template #append>
          <el-button type="primary" :disabled="!connected" @click="sendMsg">发送</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const msgBox = ref(null)
const inputText = ref('')
const messages = ref([])
const connected = ref(false)
const currentRoom = ref(1)
const onlineCount = ref(0)
const maxPerRoom = ref(200)
const roomList = ref([])

let ws = null
let reconnectTimer = null
const COLORS = ['#7C3AED','#3B82F6','#16A34A','#EA580C','#F59E0B','#EF4444','#0EA5E9','#8B5CF6','#EC4899','#14B8A6']

const currentRoomName = computed(() => {
  const r = roomList.value.find(item => item.roomId === currentRoom.value)
  return r ? (r.name || '房间' + r.roomId) : '聊天室'
})

function getMyNickname() {
  try {
    const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return u.nickname || '匿名用户'
  } catch { return '匿名用户' }
}

function getToken() {
  return localStorage.getItem('token') || ''
}

function connect() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${location.host}/ws/chat?token=${encodeURIComponent(getToken())}&roomId=${currentRoom.value}`
  console.log('[WS] 正在连接:', url)

  try { ws = new WebSocket(url) }
  catch (e) {
    console.error('[WS] 连接失败:', e.message)
    scheduleReconnect()
    return
  }

  ws.onopen = () => {
    console.log('[WS] 连接成功, roomId:', currentRoom.value)
    connected.value = true
    if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'roomList') {
        const list = JSON.parse(msg.content)
        console.log('[WS] 收到房间列表:', list)
        roomList.value = list
        const cur = list.find(r => r.roomId === currentRoom.value)
        if (cur) { onlineCount.value = cur.online; maxPerRoom.value = cur.max }
        return
      }
      if (msg.roomId !== currentRoom.value && msg.type !== 'system') return
      if (msg.type === 'chat' || msg.type === 'join' || msg.type === 'leave') {
        messages.value.push({
          id: Date.now() + Math.random(),
          type: msg.type,
          nickname: msg.nickname || '系统',
          content: msg.content || '',
          time: dayjs(msg.timestamp).format('HH:mm'),
          color: msg.type === 'chat' ? COLORS[Math.abs((msg.nickname || '').length) % COLORS.length] : ''
        })
        if (messages.value.length > 300) messages.value = messages.value.slice(-200)
        scrollBottom()
      }
      if (msg.onlineCount != null) onlineCount.value = msg.onlineCount
    } catch {}
  }

  ws.onclose = (e) => {
    console.log('[WS] 连接断开, code:', e.code, 'reason:', e.reason)
    connected.value = false
    scheduleReconnect()
  }
  ws.onerror = (e) => {
    console.error('[WS] 连接错误')
    connected.value = false
    ws?.close()
  }
}

function scheduleReconnect() {
  if (reconnectTimer) return
  console.log('[WS] 3秒后自动重连...')
  reconnectTimer = setTimeout(() => { reconnectTimer = null; connect() }, 3000)
}

function switchRoom(roomId) {
  console.log('[WS] 切换房间:', currentRoom.value, '->', roomId)
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'switch', newRoomId: roomId }))
    messages.value = []
  } else {
    currentRoom.value = roomId
    connect()
  }
}

function sendMsg() {
  const text = inputText.value.trim()
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({ type: 'chat', content: text }))
  inputText.value = ''
}

function scrollBottom() {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}

onMounted(() => connect())

onBeforeUnmount(() => {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (ws) { ws.onclose = null; ws.close() }
})
</script>

<style scoped>
.chat-room {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
  gap: 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-header h2 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  white-space: nowrap;
}

.online-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.online-dot {
  width: 8px;
  height: 8px;
  background: var(--success-color);
  border-radius: 50%;
}

/* ====== 连接状态（居中大屏） ====== */
.chat-status {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  gap: 12px;
}

.chat-status p {
  font-size: 16px;
  margin: 0;
  font-weight: 500;
}

.status-sub {
  font-size: 13px;
  color: var(--text-muted);
}

/* ====== 消息区 ====== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message { display: flex; gap: 10px; }
.message.system { justify-content: center; }

.system-msg {
  font-size: 12px;
  color: var(--text-muted);
  background: var(--bg-light);
  padding: 4px 12px;
  border-radius: 10px;
}

.empty-hint { margin: auto; font-size: 13px; color: var(--text-muted); }

.msg-avatar {
  width: 34px; height: 34px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600;
  color: #fff; flex-shrink: 0;
}

.msg-body { flex: 1; min-width: 0; }

.msg-header { display: flex; align-items: center; gap: 8px; margin-bottom: 3px; }

.msg-nickname { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.msg-time { font-size: 11px; color: var(--text-muted); }

.msg-content {
  font-size: 13px; color: var(--text-secondary);
  line-height: 1.5; word-break: break-word;
}

/* ====== 输入区 ====== */
.chat-input { padding: 12px 16px; border-top: 1px solid var(--border-light); }

@media (max-width: 768px) {
  .chat-header { flex-direction: column; align-items: flex-start; }
}
</style>
