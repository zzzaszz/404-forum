<script>
export default { name: 'MyContent' }
</script>

<template>
  <div class="my-page">
    <div class="my-layout">
      <!-- 左侧 tab -->
      <div class="my-sidebar">
        <el-menu :default-active="activeTab" @select="switchTab" class="my-menu">
          <el-menu-item index="posts">
            <el-icon><Document /></el-icon>
            <span>我的发帖<template v-if="!countsLoading"> ({{ counts.posts }})</template></span>
          </el-menu-item>
          <el-menu-item index="comments">
            <el-icon><ChatDotRound /></el-icon>
            <span>我的评论<template v-if="!countsLoading"> ({{ counts.comments }})</template></span>
          </el-menu-item>
          <el-menu-item index="likes">
            <el-icon><Star /></el-icon>
            <span>我的获赞<template v-if="!countsLoading"> ({{ counts.likes }})</template></span>
          </el-menu-item>
          <el-menu-item index="messages">
            <el-icon><Message /></el-icon>
            <span>我的消息</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="my-content">
        <!-- 发帖 -->
        <div v-show="activeTab === 'posts'" class="content-panel">
          <div class="panel-title">我的发帖</div>
          <div v-if="listLoading" class="loading-tip">加载中...</div>
          <template v-else>
          <div v-for="p in posts" :key="p.id" class="my-item">
            <div class="mi-body clickable" @click="goTopic(p.id)">
              <div class="mi-title">{{ p.title }}</div>
              <div class="mi-meta">{{ fmtTime(p.createdat) }} · {{ p.likecount || 0 }}赞 · {{ p.replycount || 0 }}评</div>
            </div>
            <el-button size="small" type="danger" plain @click="delPost(p)">删除</el-button>
          </div>
          <el-empty v-if="!posts.length" description="暂无发帖" :image-size="60" />
          </template>
        </div>

        <!-- 评论 -->
        <div v-show="activeTab === 'comments'" class="content-panel">
          <div class="panel-title">我的评论</div>
          <div v-if="listLoading" class="loading-tip">加载中...</div>
          <template v-else>
          <div v-for="c in comments" :key="c.id" class="my-item">
            <div class="mi-body clickable" @click="goTopic(c.postId)">
              <div class="mi-title">{{ c.content }}</div>
              <div class="mi-meta">来自：{{ c.postTitle || '已删除帖子' }} · {{ fmtTime(c.createdAt) }}</div>
            </div>
            <el-button size="small" type="danger" plain @click="delComment(c)">删除</el-button>
          </div>
          <el-empty v-if="!comments.length" description="暂无评论" :image-size="60" />
          </template>
        </div>

        <!-- 获赞 -->
        <div v-show="activeTab === 'likes'" class="content-panel">
          <div class="panel-title">我的获赞</div>
          <div v-if="listLoading" class="loading-tip">加载中...</div>
          <template v-else>
          <div v-for="l in likes" :key="l.type + '-' + l.id" class="my-item">
            <div class="mi-body clickable" @click="goTopic(l.postId)">
              <div class="mi-title">{{ l.title }}</div>
              <div class="mi-meta">
                <el-tag size="small" :type="l.type === 'post' ? 'success' : 'warning'">{{ l.type === 'post' ? '帖子' : '评论' }}</el-tag>
                {{ l.likeCount }}赞 · {{ fmtTime(l.createdAt) }}
              </div>
            </div>
          </div>
          <el-empty v-if="!likes.length" description="暂无获赞内容" :image-size="60" />
          </template>
        </div>

        <!-- 消息 -->
        <div v-show="activeTab === 'messages'" class="content-panel">
          <div class="panel-title">我的消息</div>
          <div v-if="listLoading" class="loading-tip">加载中...</div>
          <template v-else>
          <div v-for="m in messages" :key="m.userId" class="my-item clickable" @click="goMsg(m)">
            <div class="mi-body">
              <div class="mi-title">{{ m.nickname }}</div>
              <div class="mi-meta">{{ m.lastMessage || '点击开始聊天' }} · {{ fmtTime(m.time) }}</div>
            </div>
          </div>
          <el-empty v-if="!messages.length" description="暂无私信" :image-size="60" />
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, ChatDotRound, Star, Message } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getMyPosts, getMyComments, getMyLikes, getMyStats,
  deleteTopic, deleteComment, getMessages
} from '@/api'

const router = useRouter()
const route = useRoute()
const activeTab = ref(route.query.tab || 'posts')
const posts = ref([])
const comments = ref([])
const likes = ref([])
const messages = ref([])
const counts = ref({ posts: 0, comments: 0, likes: 0 })
const countsLoading = ref(true)
const listLoading = ref(false)
// 各 tab 是否已加载过：切换已加载的 tab 不重新请求，避免制空闪烁
const loadedMap = ref({ posts: false, comments: false, likes: false, messages: false })

function switchTab(idx) {
  activeTab.value = idx
  router.replace({ path: '/my', query: { tab: idx } })
  loadTab(idx)
}

function fmtTime(t) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''
}

async function loadPosts() {
  try {
    const res = await getMyPosts({ pageNum: 1, pageSize: 50 })
    posts.value = res.data?.list || []
  } catch { posts.value = [] }
}
async function loadComments() {
  try {
    const res = await getMyComments({ pageNum: 1, pageSize: 50 })
    comments.value = res.data?.list || []
  } catch { comments.value = [] }
}
async function loadLikes() {
  try {
    const res = await getMyLikes()
    likes.value = res.data?.list || []
  } catch { likes.value = [] }
}
async function loadMessages() {
  try {
    const res = await getMessages()
    messages.value = (res.data?.list || []).map(c => ({
      userId: String(c.userId),
      nickname: c.nickname || '用户',
      lastMessage: c.lastMessage || '',
      time: c.time,
    }))
  } catch { messages.value = [] }
}
async function loadCounts() {
  try {
    const res = await getMyStats()
    counts.value = {
      posts: res.data?.posts || 0,
      comments: res.data?.comments || 0,
      likes: res.data?.likes || 0,
    }
  } catch { /* 忽略 */ }
  countsLoading.value = false
}

async function loadTab(tab) {
  // 已加载过的 tab 直接展示缓存数据，不重新请求，避免切换制空
  if (loadedMap.value[tab]) return
  listLoading.value = true
  try {
    if (tab === 'posts') await loadPosts()
    else if (tab === 'comments') await loadComments()
    else if (tab === 'likes') await loadLikes()
    else if (tab === 'messages') await loadMessages()
    loadedMap.value[tab] = true
  } finally {
    listLoading.value = false
  }
}

function goTopic(id) {
  if (id) router.push(`/topic/${id}`)
}
function goMsg(m) {
  router.push({ path: '/messages', query: { to: m.userId, name: m.nickname } })
}

async function delPost(p) {
  try {
    await ElMessageBox.confirm('确认删除该帖子？删除后不可恢复。', '删除帖子', { type: 'warning' })
    await deleteTopic(p.id)
    posts.value = posts.value.filter(x => x.id !== p.id)
    counts.value.posts = Math.max(0, counts.value.posts - 1)
    ElMessage.success('已删除')
  } catch { /* 取消或拦截器已提示 */ }
}

async function delComment(c) {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '删除评论', { type: 'warning' })
    await deleteComment(c.id)
    comments.value = comments.value.filter(x => x.id !== c.id)
    counts.value.comments = Math.max(0, counts.value.comments - 1)
    ElMessage.success('已删除')
  } catch { /* 取消或拦截器已提示 */ }
}

watch(() => route.query.tab, (t) => {
  if (t && t !== activeTab.value) {
    activeTab.value = t
    loadTab(t)
  }
})

onMounted(() => {
  loadCounts()
  loadTab(activeTab.value)
})
</script>

<style scoped>
.my-page {
  height: 100%;
  background-color: var(--bg-color);
}

.my-layout {
  display: flex;
  gap: 20px;
  height: 100%;
}

.my-sidebar {
  width: 220px;
  flex-shrink: 0;
}

.my-menu {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

.my-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
}

.my-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(124, 58, 237, 0.08);
  color: var(--accent-color);
}

.my-menu :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
}

.my-content {
  flex: 1;
  overflow-y: auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px 20px;
}

.content-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 6px;
}

.loading-tip {
  padding: 30px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
}

.my-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--bg-light);
}

.my-item.clickable {
  cursor: pointer;
  transition: background 0.15s;
}

.my-item.clickable:hover {
  background: rgba(124, 58, 237, 0.06);
}

.mi-body {
  flex: 1;
  min-width: 0;
}

.mi-body.clickable {
  cursor: pointer;
}

.mi-title {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mi-meta {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 6px;
}

.clickable {
  cursor: pointer;
}
</style>
