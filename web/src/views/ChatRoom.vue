<script>
export default { name: 'ChatRoom' }
</script>

<template>
  <div class="chat-room">
    <div class="chat-header">
      <h2>公共聊天室</h2>
      <div class="online-info">
        <el-icon><UserFilled /></el-icon>
        <span>{{ onlineCount }} 人在线</span>
      </div>
    </div>

    <div ref="messageContainer" class="chat-messages">
      <div v-for="msg in messages" :key="msg.id" class="message">
        <div class="msg-avatar" :style="{ background: msg.color }">
          {{ msg.nickname.charAt(0) }}
        </div>
        <div class="msg-body">
          <div class="msg-header">
            <span class="msg-nickname">{{ msg.nickname }}</span>
            <span class="msg-time">{{ msg.time }}</span>
          </div>
          <div class="msg-content">{{ msg.content }}</div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputText"
        placeholder="输入消息..."
        size="large"
        clearable
        @keydown.enter.prevent="sendMessage"
      >
        <template #append>
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

const onlineCount = ref(42)
const inputText = ref('')
const messageContainer = ref(null)

const mockUsers = [
  { nickname: '匿名#A3F', color: '#7C3AED' },
  { nickname: '匿名#B72', color: '#3B82F6' },
  { nickname: '匿名#C91', color: '#16A34A' },
  { nickname: '匿名#D45', color: '#EA580C' },
  { nickname: '匿名#E28', color: '#F59E0B' },
  { nickname: '匿名用户', color: '#64748B' }
]

const mockMessages = [
  { content: '大家好，今天天气不错', time: '14:20' },
  { content: '有人玩过最近那个新游戏吗？', time: '14:21' },
  { content: '分享一下，今天看到了一个很有意思的开源项目', time: '14:22' },
  { content: '周末有人想去爬山吗？', time: '14:23' },
  { content: '推荐一本好书，叫《黑客与画家》', time: '14:24' },
  { content: '有没有前端开发的小伙伴，聊聊 Vue3 的使用体验', time: '14:25' },
  { content: '今天面试挂了，求安慰', time: '14:26' },
  { content: '分享一下今天中午吃到的美食', time: '14:27' }
]

const messages = ref(mockMessages.map((m, i) => {
  const user = mockUsers[i % mockUsers.length]
  return { id: i + 1, ...m, nickname: user.nickname, color: user.color }
}))

function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return

  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const nickname = userInfo.nickname || '匿名用户'

  messages.value.push({
    id: Date.now(),
    nickname,
    content: text,
    time,
    color: '#7C3AED'
  })
  inputText.value = ''
  scrollToBottom()
}

onMounted(() => {
  scrollToBottom()
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
}

.chat-header h2 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.online-info {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-secondary);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.message {
  display: flex;
  gap: 10px;
}

.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 3px;
}

.msg-nickname {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.msg-time {
  font-size: 11px;
  color: var(--text-muted);
}

.msg-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  word-break: break-word;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

@media (prefers-reduced-motion: reduce) {
  .message {
    animation: none;
  }
}
</style>
