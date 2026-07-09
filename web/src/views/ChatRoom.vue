<script>
export default { name: 'ChatRoom' }
</script>

<template>
  <div class="chat-room">
    <!-- ====== 房间选择页 ====== -->
    <div v-if="!joined" class="room-lobby">
      <div class="lobby-card">
        <div class="lobby-icon">
          <el-icon :size="48"><ChatDotRound /></el-icon>
        </div>
        <h2>选择一个聊天室</h2>
        <p class="lobby-sub">加入房间开始实时聊天，每个房间最多 200 人</p>
        <div class="lobby-rooms">
          <div
            v-for="r in roomList"
            :key="r.roomId"
            class="lobby-room"
            :class="{ full: r.online >= r.max }"
            @click="joinRoom(r)"
          >
            <div class="lr-name">{{ r.name || '房间' + r.roomId }}</div>
            <div class="lr-status">
              <span class="lr-dot" :class="{ full: r.online >= r.max }"></span>
              <span>{{ r.online >= r.max ? '已满' : r.online + '/' + r.max + ' 人在线' }}</span>
            </div>
            <div class="lr-arrow"><el-icon><ArrowRight /></el-icon></div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 聊天界面 ====== -->
    <template v-else>
      <div class="chat-header">
        <div class="header-left">
          <el-button link @click="leaveRoom"><el-icon><ArrowLeft /></el-icon></el-button>
          <h2>{{ currentRoomName }}</h2>
          <el-select v-model="currentRoom" size="small" style="width:170px" @change="switchRoom">
            <el-option v-for="r in roomList" :key="r.roomId"
              :label="`${r.name || '房间' + r.roomId} (${r.online}/${r.max})`"
              :value="r.roomId" :disabled="r.online >= r.max" />
          </el-select>
        </div>
        <div class="header-right">
          <el-popover placement="bottom" :width="240" trigger="click" :show-after="0">
            <template #reference>
              <div class="online-info">
                <span class="online-dot"></span>
                <span>{{ onlineCount }} / {{ maxPerRoom }} 人在线</span>
              </div>
            </template>
            <div class="online-popover">
              <div v-for="u in roomUsers" :key="u.userId" class="op-user" @click="showUserDetail(u, $event)">
                <span class="op-avatar" :style="{ background: getColor(u.nickname) }">{{ (u.nickname || '?')[0] }}</span>
                <span class="op-name">{{ u.nickname }}<span v-if="isSelf(u)" class="op-me"> (我)</span></span>
                <span class="op-time">{{ formatOnlineTime(u.joinTime) }}</span>
              </div>
              <el-empty v-if="!roomUsers.length" description="暂无在线用户" :image-size="40" />
            </div>
          </el-popover>
          <el-button size="small" type="danger" plain @click="leaveRoom">退出聊天</el-button>
        </div>
      </div>

      <div class="chat-body">
        <div v-if="!connected" class="chat-status">
          <el-icon :size="36" class="is-loading"><Loading /></el-icon>
          <p>连接中...</p>
        </div>
        <template v-else>
          <div ref="msgBox" class="chat-messages">
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

          <div class="online-panel">
            <div class="panel-title">在线用户 ({{ roomUsers.length }})</div>
            <div class="panel-list">
              <div v-for="u in roomUsers.slice(0, 30)" :key="u.userId" class="panel-user"
                :class="{ self: isSelf(u) }" @click="showUserDetail(u, $event)">
                <span class="pu-avatar" :style="{ background: getColor(u.nickname) }">{{ (u.nickname || '?')[0] }}</span>
                <span class="pu-name">{{ u.nickname }}<span v-if="isSelf(u)" class="pu-me"> (我)</span></span>
                <span class="pu-time">{{ formatOnlineTime(u.joinTime) }}</span>
              </div>
              <el-empty v-if="!roomUsers.length" description="暂无用户" :image-size="40" />
            </div>
          </div>
        </template>
      </div>

      <div class="chat-input">
        <el-input v-model="inputText" placeholder="输入消息..." size="large" clearable
          :disabled="!connected" @keydown.enter.prevent="sendMsg">
          <template #append>
            <el-button type="primary" :disabled="!connected" @click="sendMsg">发送</el-button>
          </template>
        </el-input>
      </div>
    </template>

    <!-- 用户详情浮动卡片 -->
    <teleport to="body">
      <div v-if="floatUser" class="user-float-overlay" @click="floatUser = null"></div>
      <div v-if="floatUser" class="user-float-card" :style="floatStyle">
        <div class="uf-avatar" :style="{ background: getColor(floatUser.nickname) }">
          {{ (floatUser.nickname || '?')[0] }}
        </div>
        <div class="uf-name">{{ floatUser.nickname }}</div>
        <div class="uf-time">在线时长：{{ formatOnlineTime(floatUser.joinTime) }}</div>
        <div class="uf-actions">
          <el-button size="small" type="primary" :disabled="isSelf(floatUser)" @click="goPM(floatUser)">私信</el-button>
          <el-button size="small" :disabled="isSelf(floatUser)" @click="followUser(floatUser)">
            {{ following.some(f => f.userId === floatUser.userId) ? '已关注' : '关注' }}
          </el-button>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onActivated, onDeactivated, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, ChatDotRound, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const msgBox = ref(null)
const inputText = ref('')
const messages = ref([])
const connected = ref(false)
const joined = ref(false)
const currentRoom = ref(1)
const onlineCount = ref(0)
const maxPerRoom = ref(200)
const roomList = ref([])
const roomUsers = ref([])
const following = ref([])  // 用数组替代 Set，确保 Vue 响应式
const joinTime = ref(Date.now())

// 用户浮动卡片
const floatUser = ref(null)
const floatStyle = ref({ top: '0px', left: '0px' })

function showUserDetail(user, event) {
  if (floatUser.value && floatUser.value.userId === user.userId) {
    floatUser.value = null; return
  }
  floatUser.value = user
  // 计算浮动位置
  const rect = event.currentTarget.getBoundingClientRect()
  floatStyle.value = {
    position: 'fixed',
    top: Math.min(rect.bottom + 6, window.innerHeight - 240) + 'px',
    left: Math.min(rect.left, window.innerWidth - 280) + 'px',
  }
}

let ws = null
let reconnectTimer = null
const COLORS = ['#7C3AED','#3B82F6','#16A34A','#EA580C','#F59E0B','#EF4444','#0EA5E9','#8B5CF6','#EC4899','#14B8A6']

const currentRoomName = computed(() => {
  const r = roomList.value.find(item => item.roomId === currentRoom.value)
  return r ? (r.name || '房间' + r.roomId) : '聊天室'
})

function getColor(name) {
  if (!name) return COLORS[0]
  let hash = 0; for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return COLORS[Math.abs(hash) % COLORS.length]
}

function getMyNickname() {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').nickname || '游客' }
  catch { return '游客' }
}
function getToken() { return localStorage.getItem('token') || '' }

function formatOnlineTime(joinTs) {
  if (!joinTs) return '刚刚'
  const sec = Math.floor((Date.now() - (joinTs || joinTime.value)) / 1000)
  if (sec < 60) return '刚刚'
  if (sec < 3600) return Math.floor(sec / 60) + '分钟'
  return Math.floor(sec / 3600) + '小时'
}

// ====== 房间选择 ======
function joinRoom(room) {
  if (room.online >= room.max) { ElMessage.warning('房间已满'); return }
  currentRoom.value = room.roomId
  joined.value = true
  joinTime.value = Date.now()
  connect()
}

function leaveRoom() {
  if (ws) { ws.onclose = null; ws.close(); ws = null }
  connected.value = false; joined.value = false; messages.value = []
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
}

// ====== WebSocket ======
function connect() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${location.host}/ws/chat?token=${encodeURIComponent(getToken())}&roomId=${currentRoom.value}`
  console.log('[WS] 连接:', url)
  try { ws = new WebSocket(url) } catch (e) { scheduleReconnect(); return }

  ws.onopen = () => {
    console.log('[WS] 已连接'); connected.value = true
    if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  }
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'roomList') {
        const list = JSON.parse(msg.content)
        roomList.value = list
        const cur = list.find(r => r.roomId === currentRoom.value)
        if (cur) {
          onlineCount.value = cur.online; maxPerRoom.value = cur.max
          roomUsers.value = (cur.users || []).map(u => ({
            ...u,
            joinTime: u.joinTime || (u.userId === getMyNickname() ? joinTime.value : Date.now() - 60000)
          }))
        }
        return
      }
      if (msg.roomId !== currentRoom.value && msg.type !== 'system') return
      if (msg.type === 'chat' || msg.type === 'join' || msg.type === 'leave') {
        messages.value.push({
          id: Date.now() + Math.random(), type: msg.type,
          nickname: msg.nickname || '系统', content: msg.content || '',
          time: dayjs(msg.timestamp).format('HH:mm'),
          color: msg.type === 'chat' ? getColor(msg.nickname) : ''
        })
        if (messages.value.length > 300) messages.value = messages.value.slice(-200)
        scrollBottom()
      }
      if (msg.onlineCount != null) onlineCount.value = msg.onlineCount
    } catch {}
  }
  ws.onclose = (e) => { console.log('[WS] 断开'); connected.value = false; scheduleReconnect() }
  ws.onerror = () => { connected.value = false; ws?.close() }
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => { reconnectTimer = null; connect() }, 3000)
}

function switchRoom(roomId) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'switch', newRoomId: roomId })); messages.value = []
  } else { currentRoom.value = roomId; connect() }
}

function sendMsg() {
  const text = inputText.value.trim()
  if (!text) return
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.warning('连接未就绪，请稍后重试')
    return
  }
  ws.send(JSON.stringify({ type: 'chat', content: text })); inputText.value = ''
}

/** 判断是否是自己 */
function isSelf(user) {
  const myName = getMyNickname()
  return user.nickname === myName || user.userId === myName
}

function scrollBottom() {
  nextTick(() => { if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight })
}

// ====== 用户交互 ======
function goPM(user) {
  floatUser.value = null
  router.push({ path: '/messages', query: { to: user.userId, name: user.nickname } })
}

async function followUser(user) {
  const exist = following.value.find(f => f.userId === user.userId)
  if (exist) {
    // 取消关注
    following.value = following.value.filter(f => f.userId !== user.userId)
    ElMessage.info('已取消关注')
    try { await fetch(`/api/chat/follow/${user.userId}`, { method: 'DELETE', headers: { Authorization: getToken() } }) } catch {}
  } else {
    // 关注
    following.value = [...following.value, { userId: user.userId, nickname: user.nickname }]
    ElMessage.success('已关注 ' + user.nickname)
    try {
      await fetch('/api/chat/follow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: getToken() },
        body: JSON.stringify({ userId: user.userId, nickname: user.nickname })
      })
    } catch {}
  }
  floatUser.value = null
}

// ====== 加载房间列表（REST API，WebSocket 未连接时兜底） ======
async function loadRoomList() {
  try {
    const token = getToken()
    const res = await fetch('/api/chat/rooms', {
      headers: token ? { Authorization: token } : {}
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      const list = data.data.map(r => ({
        ...r,
        users: r.users || []
      }))
      roomList.value = list
    }
  } catch { /* WebSocket 连接后会推送 */ }
}

// ====== 生命周期 ======
async function loadFollowList() {
  try {
    const res = await fetch('/api/chat/follow', { headers: { Authorization: getToken() } })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      following.value = data.data || []
    }
  } catch {}
}

onMounted(() => {
  loadRoomList()
  loadFollowList()
})

onBeforeUnmount(() => {
  // keep-alive 时不会触发，只有真正销毁才断开
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (ws) { ws.onclose = null; ws.close(); ws = null }
})
</script>

<style scoped>
.chat-room {
  height: 100%; display: flex; flex-direction: column;
  background: var(--bg-white); border-radius: var(--radius-md);
  border: 1px solid var(--border-light); overflow: hidden;
}

/* ====== 房间选择页 ====== */
.room-lobby {
  flex: 1; display: flex; align-items: center; justify-content: center;
}
.lobby-card {
  text-align: center; max-width: 420px; width: 100%; padding: 40px 32px;
}
.lobby-icon {
  width: 80px; height: 80px; border-radius: 20px; margin: 0 auto 16px;
  background: rgba(124,58,237,0.08); color: var(--accent-color);
  display: flex; align-items: center; justify-content: center;
}
.lobby-card h2 { font-size: 20px; font-weight: 700; color: var(--text-primary); margin: 0 0 8px; }
.lobby-sub { font-size: 13px; color: var(--text-secondary); margin: 0 0 24px; }

.lobby-rooms { display: flex; flex-direction: column; gap: 8px; }
.lobby-room {
  display: flex; align-items: center; padding: 14px 16px;
  border-radius: 10px; border: 1px solid var(--border-light);
  cursor: pointer; transition: all .15s; text-align: left;
}
.lobby-room:hover { border-color: var(--primary-light); background: var(--bg-light); }
.lobby-room.full { opacity: .5; cursor: not-allowed; }
.lr-name { font-size: 14px; font-weight: 600; color: var(--text-primary); flex: 1; }
.lr-status { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-secondary); margin-right: 12px; }
.lr-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--success-color); }
.lr-dot.full { background: var(--text-muted); }
.lr-arrow { color: var(--text-muted); }

/* ====== Header ====== */
.chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px; border-bottom: 1px solid var(--border-light); gap: 8px;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.chat-header h2 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin: 0; }

.online-info {
  display: flex; align-items: center; gap: 6px; font-size: 12px;
  color: var(--text-secondary); cursor: pointer; padding: 4px 10px; border-radius: 6px;
}
.online-info:hover { background: var(--bg-light); }
.online-dot { width: 8px; height: 8px; background: var(--success-color); border-radius: 50%; }

/* popover */
.op-user {
  display: flex; align-items: center; gap: 8px; padding: 8px 4px;
  border-bottom: 1px solid var(--border-light); cursor: pointer; border-radius: 6px;
}
.op-user:hover { background: var(--bg-light); }
.op-user:last-child { border-bottom: none; }
.op-avatar {
  width: 30px; height: 30px; border-radius: 7px; background: var(--accent-color);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600; flex-shrink: 0;
}
.op-name { flex: 1; font-size: 13px; color: var(--text-primary); }
.op-time { font-size: 11px; color: var(--text-muted); }

/* ====== Body ====== */
.chat-body { flex: 1; display: flex; min-height: 0; }

.chat-status {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  justify-content: center; color: var(--text-muted); gap: 10px;
}
.chat-status p { font-size: 15px; margin: 0; }

.chat-messages {
  flex: 1; overflow-y: auto; padding: 16px;
  display: flex; flex-direction: column; gap: 12px;
}
.message { display: flex; gap: 10px; }
.message.system { justify-content: center; }
.system-msg {
  font-size: 12px; color: var(--text-muted); background: var(--bg-light);
  padding: 4px 12px; border-radius: 10px;
}
.empty-hint { margin: auto; font-size: 13px; color: var(--text-muted); }
.msg-avatar {
  width: 34px; height: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; color: #fff; flex-shrink: 0;
}
.msg-body { flex: 1; min-width: 0; }
.msg-header { display: flex; align-items: center; gap: 8px; margin-bottom: 3px; }
.msg-nickname { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.msg-time { font-size: 11px; color: var(--text-muted); }
.msg-content { font-size: 13px; color: var(--text-secondary); line-height: 1.5; word-break: break-word; }

/* 右侧面板 */
.online-panel {
  width: 170px; flex-shrink: 0; border-left: 1px solid var(--border-light);
  padding: 12px; overflow-y: auto; display: flex; flex-direction: column;
}
.panel-title { font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px; }
.panel-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 2px; }
.panel-user {
  display: flex; align-items: center; gap: 5px; padding: 5px 4px;
  border-radius: 6px; cursor: pointer;
}
.panel-user:hover { background: var(--bg-light); }
.pu-avatar {
  width: 26px; height: 26px; border-radius: 6px; background: var(--accent-color);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600; flex-shrink: 0;
}
.pu-name { flex: 1; font-size: 12px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pu-time { font-size: 10px; color: var(--text-muted); flex-shrink: 0; }
.pu-me, .op-me { font-size: 10px; color: var(--text-muted); }
.panel-user.self { opacity: .6; cursor: default; }

/* 输入 */
.chat-input { padding: 12px 16px; border-top: 1px solid var(--border-light); }

/* 用户浮动卡片 */
.user-float-overlay {
  position: fixed; inset: 0; z-index: 999; background: transparent;
}
.user-float-card {
  position: fixed; z-index: 1000;
  width: 240px; background: var(--bg-white); border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,.12); padding: 20px; text-align: center;
}
.uf-avatar {
  width: 52px; height: 52px; border-radius: 13px; margin: 0 auto 10px;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700;
}
.uf-name { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.uf-time { font-size: 12px; color: var(--text-muted); margin-bottom: 14px; }
.uf-actions { display: flex; gap: 8px; justify-content: center; }

@media (max-width: 768px) {
  .online-panel { display: none; }
  .chat-header { flex-direction: column; align-items: flex-start; }
}
</style>
