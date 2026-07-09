<script>
export default { name: 'Messages' }
</script>

<template>
  <div class="messages-page">
    <!-- 会话列表 -->
    <div class="msg-list-panel">
      <div class="panel-header"><h3>私信</h3></div>
      <div class="conv-list">
        <!-- 已关注用户 -->
        <div v-if="followedUsers.length" class="followed-section">
          <div class="section-label">已关注</div>
          <div
            v-for="u in followedUsers"
            :key="'f-' + u.userId"
            class="conv-item followed"
            @click="startFollowChat(u)"
          >
            <div class="conv-avatar" :style="{ background: u.color }">{{ u.nickname.charAt(0) }}</div>
            <div class="conv-body">
              <span class="conv-name">{{ u.nickname }}</span>
            </div>
          </div>
        </div>
        <!-- 会话 -->
        <div v-if="conversations.length" class="section-label">消息</div>
        <div v-for="item in conversations" :key="item.id"
          class="conv-item" :class="{ active: activeConv === item.id }"
          @click="openConv(item)">
          <div class="conv-avatar" :style="{ background: item.color }">{{ item.nickname.charAt(0) }}</div>
          <div class="conv-body">
            <div class="conv-header">
              <span class="conv-name">{{ item.nickname }}</span>
              <span class="conv-time">{{ item.time }}</span>
            </div>
            <div class="conv-preview">
              <span class="conv-text">{{ item.lastMessage }}</span>
              <span v-if="item.unread" class="conv-badge">{{ item.unread }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!conversations.length && !followedUsers.length" description="暂无私信" :image-size="50" />
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="msg-chat-panel">
      <template v-if="activeConv">
        <div class="chat-top">
          <span class="chat-to">{{ activeConvName }}</span>
        </div>
        <div ref="chatBox" class="chat-msgs">
          <div v-for="m in activeMessages" :key="m.id"
            class="chat-msg" :class="{ mine: m.mine }">
            <div class="chat-bubble" :class="{ mine: m.mine }">{{ m.content }}</div>
            <div class="chat-time">{{ m.time }}</div>
          </div>
          <div v-if="!activeMessages.length" class="chat-empty">开始聊天吧</div>
        </div>
        <div class="chat-send">
          <el-input v-model="sendText" placeholder="输入消息..." size="small" clearable
            @keydown.enter.prevent="doSend" />
          <el-button type="primary" size="small" @click="doSend">发送</el-button>
        </div>
      </template>
      <div v-else class="chat-placeholder">
        <el-icon :size="40"><ChatDotRound /></el-icon>
        <p>选择一条私信开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getMessages, sendMessage } from '@/api'

const route = useRoute()
const chatBox = ref(null)
const sendText = ref('')
const conversations = ref([])
const activeConv = ref(null)
const activeConvName = ref('')
const activeMessages = ref([])

const COLORS = ['#7C3AED','#3B82F6','#16A34A','#EA580C','#F59E0B','#EF4444','#0EA5E9','#8B5CF6','#EC4899','#14B8A6']

// 已关注用户（从后端加载）
const followedUsers = ref([])

function getToken() { return localStorage.getItem('token') || '' }

async function loadFollowed() {
  try {
    const res = await fetch('/api/chat/follow', { headers: { Authorization: getToken() } })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      followedUsers.value = data.data.map((u, i) => ({ ...u, color: COLORS[i % COLORS.length] }))
    }
  } catch { followedUsers.value = [] }
}

function startFollowChat(user) {
  const exist = conversations.value.find(c => c.userId === user.userId)
  if (exist) { openConv(exist); return }
  const newConv = {
    id: Date.now(), userId: user.userId, nickname: user.nickname,
    color: user.color || COLORS[0], lastMessage: '', time: '', unread: 0,
  }
  conversations.value.unshift(newConv)
  openConv(newConv)
}

async function loadMessages() {
  try {
    const res = await getMessages()
    conversations.value = (res.data?.list || []).map((n, idx) => ({
      id: n.id || idx,
      userId: n.sourceuserid || n.fromUserId || '',
      nickname: n.sourcename || n.from || ('用户' + (idx + 1)),
      color: COLORS[idx % COLORS.length],
      lastMessage: n.content || '',
      time: n.createdat ? dayjs(n.createdat).format('MM-DD HH:mm') : '',
      unread: n.isread === 0 || n.isread === false ? 1 : 0,
    }))
  } catch { conversations.value = [] }
}

function openConv(item) {
  activeConv.value = item.id
  activeConvName.value = item.nickname
  // 模拟加载历史消息
  activeMessages.value = [
    { id: 1, content: item.lastMessage, time: item.time, mine: false },
  ]
  item.unread = 0
  nextTick(() => scrollChatBottom())
}

async function doSend() {
  const text = sendText.value.trim()
  if (!text) return
  const conv = conversations.value.find(c => c.id === activeConv.value)
  if (!conv) return

  const now = dayjs().format('HH:mm')
  activeMessages.value.push({ id: Date.now(), content: text, time: now, mine: true })
  sendText.value = ''
  nextTick(() => scrollChatBottom())

  try {
    await sendMessage({ toUserId: conv.userId, content: text })
    ElMessage.success('发送成功')
    // 刷新会话列表
    loadMessages()
  } catch { ElMessage.warning('发送失败，请稍后重试') }
}

function scrollChatBottom() {
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

// 从聊天室跳转过来时自动打开私信窗口
function openFromQuery() {
  const toUser = route.query.to
  if (!toUser) return
  const uname = route.query.name || '用户'
  const exist = conversations.value.find(c => c.userId === String(toUser))
  if (exist) {
    openConv(exist)
  } else {
    const newConv = {
      id: Date.now(), userId: String(toUser), nickname: uname,
      color: COLORS[Math.abs(String(toUser).length || 0) % COLORS.length],
      lastMessage: '点击开始聊天', time: '', unread: 0,
    }
    conversations.value.unshift(newConv)
    openConv(newConv)
  }
}

watch(() => route.query.to, () => { openFromQuery() })

onMounted(async () => {
  await loadMessages()
  loadFollowed()
  // 等 conversations 加载后再检查 query
  setTimeout(() => openFromQuery(), 100)
})
</script>

<style scoped>
.messages-page {
  height: 100%; display: flex; gap: 1px;
  background: var(--border-light); border-radius: var(--radius-md); overflow: hidden;
}

/* 左侧会话列表 */
.msg-list-panel {
  width: 280px; flex-shrink: 0; background: var(--bg-white);
  display: flex; flex-direction: column;
}
.panel-header { padding: 14px 16px; border-bottom: 1px solid var(--border-light); }
.panel-header h3 { font-size: 14px; font-weight: 600; color: var(--text-primary); margin: 0; }
.conv-list { flex: 1; overflow-y: auto; padding: 4px; }

.section-label {
  font-size: 11px; font-weight: 600; color: var(--text-muted);
  padding: 8px 12px 4px; text-transform: uppercase; letter-spacing: .5px;
}

.conv-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 12px;
  border-radius: 8px; cursor: pointer; transition: background .15s;
}
.conv-item:hover, .conv-item.active { background: var(--bg-light); }
.conv-avatar {
  width: 38px; height: 38px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; color: #fff; flex-shrink: 0;
}
.conv-body { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 3px; }
.conv-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.conv-time { font-size: 11px; color: var(--text-muted); }
.conv-preview { display: flex; justify-content: space-between; align-items: center; }
.conv-text { font-size: 12px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-badge {
  min-width: 18px; height: 18px; padding: 0 5px; background: var(--danger-color);
  border-radius: 9px; font-size: 11px; font-weight: 600; color: #fff;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-left: 8px;
}

/* 右侧聊天窗口 */
.msg-chat-panel {
  flex: 1; background: var(--bg-white); display: flex; flex-direction: column;
}
.chat-top { padding: 14px 16px; border-bottom: 1px solid var(--border-light); }
.chat-to { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.chat-msgs { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.chat-msg { display: flex; flex-direction: column; max-width: 70%; }
.chat-msg.mine { align-self: flex-end; }
.chat-bubble {
  padding: 8px 12px; border-radius: 12px; font-size: 13px; line-height: 1.5;
  background: var(--bg-light); color: var(--text-primary); word-break: break-word;
}
.chat-bubble.mine { background: var(--primary-color); color: #fff; }
.chat-time { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
.chat-msg.mine .chat-time { text-align: right; }
.chat-empty { margin: auto; font-size: 13px; color: var(--text-muted); }

.chat-send {
  padding: 12px 16px; border-top: 1px solid var(--border-light);
  display: flex; gap: 8px; align-items: center;
}
.chat-send .el-input { flex: 1; }

.chat-placeholder {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: var(--text-muted); gap: 10px;
}
.chat-placeholder p { font-size: 14px; margin: 0; }

@media (max-width: 768px) {
  .msg-list-panel { width: 100%; }
  .msg-chat-panel { display: none; }
}
</style>
