<script>
export default { name: 'Dashboard' }
</script>

<template>
  <div class="dashboard">
    <!-- 顶部状态栏 -->
    <div class="status-bar">
      <span class="greeting">{{ greeting }}，{{ nickname }}</span>
      <div class="status-badges">
        <span class="badge">
          <el-icon><UserFilled /></el-icon>
          {{ onlineCount }} 在线
        </span>
        <span class="badge">
          <el-icon><ChatDotRound /></el-icon>
          {{ activeTopics }} 话题
        </span>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-grid">
      <router-link to="/chat" class="quick-item">
        <div class="quick-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <span class="quick-label">聊天室</span>
      </router-link>
      <router-link to="/topic/random" class="quick-item">
        <div class="quick-icon">
          <el-icon><ChatLineRound /></el-icon>
        </div>
        <span class="quick-label">随便聊聊</span>
      </router-link>
      <router-link to="/hot" class="quick-item">
        <div class="quick-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <span class="quick-label">热门</span>
      </router-link>
      <router-link to="/messages" class="quick-item">
        <div class="quick-icon">
          <el-icon><Message /></el-icon>
        </div>
        <span class="quick-label">私信</span>
        <span v-if="unreadCount > 0" class="unread-dot">{{ unreadCount }}</span>
      </router-link>
    </div>

    <!-- 主内容 -->
    <div class="main-content">
      <!-- 左侧话题列表 -->
      <div class="topics-panel">
        <div class="panel-header">
          <h3>最新话题</h3>
          <el-button type="primary" size="small" round @click="router.push('/topic/random')">
            发布
          </el-button>
        </div>
        <div class="panel-body">
          <div v-for="item in topics" :key="item.id" class="topic-row" @click="goTopic(item)">
            <div class="topic-avatar"><img :src="item.avatar" alt=""/></div>
            <div class="topic-main">
              <div class="topic-title">{{ item.title }}</div>
              <div class="topic-meta">
                <span class="meta-author">{{ item.author }}</span>
                <span class="meta-tag">{{ item.category }}</span>
                <span class="meta-time">{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
            <div class="topic-stats">
              <span><el-icon><View /></el-icon> {{ item.views }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ item.comments }}</span>
            </div>
          </div>
          <el-empty v-if="!topics.length" description="暂无话题" :image-size="60" />
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="side-panel">
        <div class="hot-section">
          <div class="section-title">
            <el-icon><TrendCharts /></el-icon>
            热门榜
          </div>
          <div class="hot-list">
            <div v-for="(item, idx) in hotTopics" :key="item.id" class="hot-row" @click="goTopic(item)">
              <span class="hot-rank" :class="{ top: idx < 3 }">{{ idx + 1 }}</span>
              <span class="hot-title">{{ item.title }}</span>
              <span class="hot-heat">{{ formatHeat(item.heat) }}</span>
            </div>
          </div>
        </div>

        <div class="online-section">
          <div class="section-title">
            <el-icon><UserFilled /></el-icon>
            在线用户
          </div>
          <div class="online-list">
            <div v-for="user in onlineUsers.slice(0, 8)" :key="user.id" class="online-avatar">
              <img :src="onlineAvatar(user.id)" alt=""/>
            </div>
            <span v-if="onlineUsers.length > 8" class="online-more">+{{ onlineUsers.length - 8 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import {
  UserFilled,
  ChatDotRound,
  ChatLineRound,
  Message,
  TrendCharts,
  View
} from '@element-plus/icons-vue'
import {
  getTopics,
  getHotTopics,
  getOnlineUsers,
  getForumStats,
  getMessages,
  mapTopic,
  pickAvatar
} from '@/api'

const router = useRouter()

const greeting = ref('你好')
const nickname = ref('匿名用户')
const onlineCount = ref(0)
const activeTopics = ref(0)
const unreadCount = ref(0)

try {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.nickname) nickname.value = userInfo.nickname
} catch {}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
}
greeting.value = getGreeting()

const topics = ref([])
const hotTopics = ref([])
const onlineUsers = ref([])

function formatTime(date) {
  if (!date) return ''
  return dayjs(date).format('HH:mm')
}

function formatHeat(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return n.toString()
}

function goTopic(item) {
  router.push(`/topic/${item.id}`)
}

function onlineAvatar(id) {
  return pickAvatar(id)
}

async function loadDashboard() {
  try {
    const stats = await getForumStats()
    if (stats.data) {
      onlineCount.value = stats.data.onlineCount || 0
      activeTopics.value = stats.data.activeTopics || 0
    }
  } catch (e) { /* 忽略统计加载失败 */
  }

  try {
    const res = await getTopics({sort: 'latest', pageNum: 1, pageSize: 5})
    topics.value = (res.data?.list || []).map(mapTopic)
  } catch (e) { /* 忽略 */
  }

  try {
    const res = await getHotTopics(5)
    hotTopics.value = (res.data || []).map(mapTopic)
  } catch (e) { /* 忽略 */
  }

  try {
    const res = await getOnlineUsers()
    onlineUsers.value = res.data?.users || []
  } catch (e) { /* 忽略 */
  }

  try {
    const res = await getMessages()
    unreadCount.value = res.data?.unread || 0
  } catch (e) { /* 未登录或无私信 */
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0;
}

/* 状态栏 */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.greeting {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.status-badges {
  display: flex;
  gap: 10px;
}

.badge {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  background: var(--bg-light);
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-secondary);
}

.badge .el-icon {
  font-size: 14px;
}

/* 快捷操作 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  position: relative;
}

.quick-item:hover {
  border-color: var(--primary-light);
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.08);
}

.quick-icon {
  width: 40px;
  height: 40px;
  background: rgba(124, 58, 237, 0.08);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
  font-size: 18px;
}

.quick-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.unread-dot {
  position: absolute;
  top: 10px;
  right: 10px;
  min-width: 18px;
  height: 18px;
  background: var(--warning-color);
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

/* 主内容 */
.main-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 16px;
  min-height: 0;
}

/* 话题面板 */
.topics-panel {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--border-light);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.panel-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.topic-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.topic-row:hover {
  background: var(--bg-light);
}

.topic-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.topic-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.topic-main {
  flex: 1;
  min-width: 0;
}

.topic-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-muted);
  align-items: center;
}

.meta-author {
  color: var(--primary-light);
}

.meta-tag {
  background: rgba(124, 58, 237, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--accent-color);
  font-size: 11px;
}

.topic-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.topic-stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

/* 边栏 */
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-section,
.online-section {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  border: 1px solid var(--border-light);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.section-title .el-icon {
  color: var(--warning-color);
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hot-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-radius: var(--radius-sm);
}

.hot-row:hover {
  background: var(--bg-light);
}

.hot-rank {
  width: 20px;
  height: 20px;
  background: var(--bg-light);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  flex-shrink: 0;
}

.hot-rank.top {
  background: var(--warning-color);
  color: #fff;
}

.hot-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-heat {
  font-size: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.online-list {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.online-avatar {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.online-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.online-more {
  font-size: 12px;
  color: var(--text-muted);
  padding: 4px 8px;
  background: var(--bg-light);
  border-radius: 6px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  .side-panel {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .side-panel {
    grid-template-columns: 1fr;
  }
  .status-bar {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}

@media (prefers-reduced-motion: reduce) {
  .quick-item:hover,
  .topic-row:hover,
  .hot-row:hover {
    transform: none;
  }
}
</style>
