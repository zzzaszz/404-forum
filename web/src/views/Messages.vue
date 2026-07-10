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
            :class="{ active: isActive(u.userId) }"
            @click="openChat(u)"
          >
            <div class="conv-avatar" :style="{ background: u.color }">{{ (u.nickname || '?').charAt(0) }}</div>
            <div class="conv-body">
              <span class="conv-name">{{ u.nickname }}</span>
            </div>
          </div>
        </div>
        <!-- 历史会话 -->
        <div v-if="conversations.length" class="section-label">消息</div>
        <div v-for="item in conversations" :key="'c-' + item.userId"
          class="conv-item" :class="{ active: isActive(item.userId) }"
          @click="openChat(item)">
          <div class="conv-avatar" :style="{ background: item.color }">{{ (item.nickname || '?').charAt(0) }}</div>
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
        <!-- 没有历史会话时显示占位（从聊天室跳转过来不会污染此列表） -->
        <el-empty v-if="!conversations.length" description="暂无私信" :image-size="50" />
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="msg-chat-panel">
      <template v-if="activeTarget">
        <div class="chat-top">
          <span class="chat-to">{{ activeTarget.nickname }}</span>
        </div>
        <div ref="chatBox" class="chat-msgs">
          <div v-for="m in activeMessages" :key="m.id"
            class="chat-msg" :class="{ mine: m.mine }">
            <div class="chat-bubble" :class="{ mine: m.mine }">{{ m.content }}</div>
            <div class="chat-time">{{ m.time }}</div>
          </div>
          <div v-if="!activeMessages.length" class="chat-empty">还没有消息，发条私信吧</div>
        </div>
        <div class="chat-send">
          <el-input v-model="sendText" placeholder="输入消息..." size="small" clearable
            @keydown.enter.prevent="doSend" />
          <el-button type="primary" size="small" @click="doSend">发送</el-button>
        </div>
      </template>
      <div v-else class="chat-placeholder">
        <el-icon :size="40"><ChatDotRound /></el-icon>
        <p>选择关注的人或一条私信开始聊天</p>
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
import { getMessages, getMessageHistory, markMessageRead, sendMessage, getFollowing } from '@/api'
import { useMessageStore } from '@/stores/message'

const route = useRoute()
const messageStore = useMessageStore()
const chatBox = ref(null)
const sendText = ref('')
// 历史会话列表（后端按对话对方聚合，双向）
const conversations = ref([])
// 我关注的人
const followedUsers = ref([])
// 当前对话对象（独立于列表，从关注/会话/聊天室跳转均可设置）
const activeTarget = ref(null)
const activeMessages = ref([])

const COLORS = ['#7C3AED','#3B82F6','#16A34A','#EA580C','#F59E0B','#EF4444','#0EA5E9','#8B5CF6','#EC4899','#14B8A6']

/** 按用户 ID 稳定取色 */
function colorOf(userId) {
  if (!userId) return COLORS[0]
  const s = String(userId)
  let hash = 0
  for (let i = 0; i < s.length; i++) hash = s.charCodeAt(i) + ((hash << 5) - hash)
  return COLORS[Math.abs(hash) % COLORS.length]
}

function isActive(userId) {
  return activeTarget.value && String(activeTarget.value.userId) === String(userId)
}

/** 加载历史会话列表 */
async function loadConversations() {
  try {
    const res = await getMessages()
    const list = res.data?.list || []
    conversations.value = list.map(c => ({
      userId: String(c.userId),
      nickname: c.nickname || '用户',
      color: colorOf(c.userId),
      lastMessage: c.lastMessage || '',
      time: c.time ? dayjs(c.time).format('MM-DD HH:mm') : '',
      unread: c.unread || 0,
    }))
    // 同步全局未读数
    messageStore.setUnread(res.data?.unread || 0)
  } catch {
    conversations.value = []
  }
}

/** 加载我关注的人 */
async function loadFollowed() {
  try {
    const res = await getFollowing()
    followedUsers.value = (res.data || []).map(u => ({
      userId: String(u.userId),
      nickname: u.nickname || '用户',
      color: colorOf(u.userId),
    }))
  } catch {
    followedUsers.value = []
  }
}

/** 打开与某用户的对话窗口 */
async function openChat(target) {
  if (!target) return
  const t = {
    userId: String(target.userId),
    nickname: target.nickname || '用户',
    color: target.color || colorOf(target.userId),
  }
  activeTarget.value = t
  await loadHistory(t.userId)
  // 清除该会话未读
  const conv = conversations.value.find(c => c.userId === t.userId)
  if (conv && conv.unread) {
    messageStore.decr(conv.unread)
    conv.unread = 0
    markMessageRead(t.userId).catch(() => {})
  }
}

/** 加载与某用户的双向历史消息 */
async function loadHistory(toUserId) {
  try {
    const res = await getMessageHistory(toUserId)
    activeMessages.value = (res.data?.list || []).map(m => ({
      id: m.id,
      content: m.content || '',
      time: m.time ? dayjs(m.time).format('MM-DD HH:mm') : '',
      mine: !!m.mine,
    }))
  } catch {
    activeMessages.value = []
  }
  nextTick(() => scrollChatBottom())
}

async function doSend() {
  const text = sendText.value.trim()
  if (!text || !activeTarget.value) return
  const t = activeTarget.value

  activeMessages.value.push({ id: Date.now(), content: text, time: dayjs().format('HH:mm'), mine: true })
  sendText.value = ''
  nextTick(() => scrollChatBottom())

  try {
    await sendMessage({ toUserId: t.userId, content: text })
    // 刷新会话列表，让该对话进入"消息"区
    loadConversations()
  } catch {
    ElMessage.warning('发送失败，请稍后重试')
  }
}

function scrollChatBottom() {
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

/** 从聊天室跳转过来时自动打开私信窗口（不污染会话列表，无历史时左侧仍显示"暂无私信"） */
function openFromQuery() {
  const toUser = route.query.to
  if (!toUser) return
  openChat({ userId: String(toUser), nickname: route.query.name || '用户', color: colorOf(toUser) })
}

watch(() => route.query.to, () => { openFromQuery() })

onMounted(async () => {
  await loadConversations()
  loadFollowed()
  nextTick(() => openFromQuery())
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
.panel-header { padding: 14px 16px; border-bottom: 1px solid var(--border-light); flex-shrink: 0; }
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
.chat-top { padding: 14px 16px; border-bottom: 1px solid var(--border-light); flex-shrink: 0; }
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
  display: flex; gap: 8px; align-items: center; flex-shrink: 0;
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
