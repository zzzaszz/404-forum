<script>
export default { name: 'Messages' }
</script>

<template>
  <div class="messages-page">
    <div class="page-header">
      <h2>私信</h2>
    </div>

    <div class="message-list">
      <div
        v-for="item in conversations"
        :key="item.id"
        class="conversation"
        @click="openConversation(item)"
      >
        <div class="conv-avatar" :style="{ background: item.color }">
          {{ item.nickname.charAt(0) }}
        </div>
        <div class="conv-body">
          <div class="conv-header">
            <span class="conv-nickname">{{ item.nickname }}</span>
            <span class="conv-time">{{ item.time }}</span>
          </div>
          <div class="conv-preview">
            <span class="conv-text">{{ item.lastMessage }}</span>
            <span v-if="item.unread > 0" class="conv-unread">{{ item.unread }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="!conversations.length" description="暂无私信" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {getMessages} from '@/api'

const router = useRouter()

const conversations = ref([])

const COLORS = ['#7C3AED', '#3B82F6', '#16A34A', '#EA580C', '#F59E0B']

function mapNotification(n, idx) {
  const suffix = n.sourceuserid ? String(n.sourceuserid).slice(-3).toUpperCase() : '000'
  return {
    id: n.id,
    nickname: `匿名#${suffix}`,
    color: COLORS[idx % COLORS.length],
    lastMessage: n.content || '',
    time: n.createdat ? dayjs(n.createdat).format('MM-DD HH:mm') : '',
    unread: n.isread ? 0 : 1,
  }
}

async function loadMessages() {
  try {
    const res = await getMessages()
    conversations.value = (res.data?.list || []).map(mapNotification)
  } catch (e) {
    conversations.value = []
  }
}

function openConversation(item) {
  ElMessage.info(`打开与 ${item.nickname} 的对话`)
  item.unread = 0
}

onMounted(loadMessages)
</script>

<style scoped>
.messages-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: 0 4px;
  margin-bottom: 12px;
}

.page-header h2 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 4px;
}

.conversation {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.conversation:hover {
  background: var(--bg-light);
}

.conv-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.conv-body {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-nickname {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.conv-time {
  font-size: 11px;
  color: var(--text-muted);
}

.conv-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-text {
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conv-unread {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--danger-color);
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-left: 8px;
}
</style>
