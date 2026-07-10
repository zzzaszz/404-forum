<script>
export default { name: 'TopicDetail' }
</script>

<template>
  <div class="topic-detail">
    <!-- 话题正文 -->
    <div class="topic-card">
      <div class="topic-header">
        <div class="author-info">
          <div class="author-avatar clickable" @click="showUserCard({userId: topic.userId, nickname: topic.author}, $event)">
            <img :src="topic.avatar" alt=""/>
          </div>
          <div class="author-meta">
            <span class="author-name clickable" @click="showUserCard({userId: topic.userId, nickname: topic.author}, $event)">{{ topic.author }}</span>
            <span class="publish-time">{{ topic.time }}</span>
          </div>
        </div>
        <div class="topic-header-right">
          <span class="topic-tag" :style="{ background: topic.tagBg, color: topic.tagColor }">{{ topic.category }}</span>
          <el-button v-if="isTopicOwner" size="small" type="danger" plain @click="handleDeleteTopic">删除</el-button>
        </div>
      </div>

      <h1 class="topic-title">{{ topic.title }}</h1>
      <div class="topic-content">{{ topic.content }}</div>

      <div class="topic-footer">
        <div class="stat-btn">
          <el-icon><View /></el-icon>
          <span>{{ topic.views }} 浏览</span>
        </div>
        <div class="stat-btn">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ comments.length }} 评论</span>
        </div>
        <div :class="{ active: liked }" class="stat-btn action-btn" @click="handleLike">
          <el-icon><Star/></el-icon>
          <span>{{ topic.likes }} 点赞</span>
        </div>
        <div :class="{ active: favorited }" class="stat-btn action-btn" @click="handleFavorite">
          <el-icon><CollectionTag/></el-icon>
          <span>{{ topic.favorites }} 收藏</span>
        </div>
        <div class="stat-btn action-btn" @click="handleReport">
          <el-icon><WarnTriangleFilled/></el-icon>
          <span>举报</span>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comments-section">
      <h3 class="comments-title">评论 ({{ comments.length }})</h3>

      <div class="comments-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item"
          :id="'comment-' + comment.id" :class="{ highlight: highlightId === comment.id }">
          <div class="comment-avatar clickable" @click="showUserCard({userId: comment.userId, nickname: comment.author}, $event)">
            <img :src="comment.avatar" alt=""/>
          </div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-author clickable" @click="showUserCard({userId: comment.userId, nickname: comment.author}, $event)">{{ comment.author }}</span>
              <span class="comment-time">{{ fmtTime(comment.createTime) }}</span>
              <div class="comment-ops">
                <el-button link size="small" @click="setReply(comment)">回复</el-button>
                <el-button v-if="comment.userId === currentUserId" link size="small" type="danger" @click="handleDeleteComment(comment)">删除</el-button>
              </div>
            </div>
            <div v-if="comment.parentId && replySnippet(comment.parentId)" class="reply-quote" @click="scrollToComment(comment.parentId)">
              <span class="rq-arrow">↪</span>
              <span class="rq-text">
                <span class="rq-user">回复 @{{ comment.replyUserName }}</span>
                <span class="rq-content">{{ replySnippet(comment.parentId) }}</span>
              </span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>

        <el-empty v-if="!comments.length" description="暂无评论" :image-size="50" />
      </div>

      <!-- 回复输入框 -->
      <div class="reply-box">
        <div v-if="replyTo" class="reply-target">
          <span>回复 @{{ replyTo.name }}</span>
          <el-button link size="small" @click="cancelReply">取消回复</el-button>
        </div>
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="3"
          :placeholder="replyTo ? `回复 @${replyTo.name}...` : '写下你的回复...'"
          maxlength="500"
          show-word-limit
        />
        <div class="reply-actions">
          <el-button type="primary" @click="submitReply" :disabled="!replyText.trim()" :loading="replyLoading">
            发表评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- 用户浮动卡片（关注/私信） -->
    <teleport to="body">
      <div v-if="floatUser" class="user-float-overlay" @click="floatUser = null"></div>
      <div v-if="floatUser" class="user-float-card" :style="floatStyle">
        <div class="uf-avatar" :style="{ background: colorOf(floatUser.userId) }">
          {{ (floatUser.nickname || '?').charAt(0) }}
        </div>
        <div class="uf-name">{{ floatUser.nickname }}</div>
        <div class="uf-actions">
          <el-button size="small" type="primary" :disabled="isSelf(floatUser)" @click="goPM(floatUser)">私信</el-button>
          <el-button size="small" :disabled="isSelf(floatUser)" @click="toggleFollow(floatUser)">
            {{ isFollowing(floatUser) ? '已关注' : '关注' }}
          </el-button>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import {
  getTopicDetail,
  getTopicComments,
  createComment,
  toggleLike,
  toggleFavorite,
  reportTarget,
  deleteTopic,
  deleteComment,
  mapTopic,
  mapComment,
  getFollowing
} from '@/api'

const router = useRouter()
const route = useRoute()
const replyText = ref('')
const replyLoading = ref(false)
const replyTo = ref(null) // {id, rootId, userId, name}

const rawTopic = ref(null)
const comments = ref([])
const following = ref([])

// 当前登录用户 ID
const currentUserId = computed(() => {
  try { return String(JSON.parse(localStorage.getItem('userInfo') || '{}').userId || '') }
  catch { return '' }
})

const COLORS = ['#7C3AED','#3B82F6','#16A34A','#EA580C','#F59E0B','#EF4444','#0EA5E9','#8B5CF6','#EC4899','#14B8A6']
function colorOf(userId) {
  if (!userId) return COLORS[0]
  const s = String(userId)
  let hash = 0
  for (let i = 0; i < s.length; i++) hash = s.charCodeAt(i) + ((hash << 5) - hash)
  return COLORS[Math.abs(hash) % COLORS.length]
}

const topic = computed(() => {
  const t = mapTopic(rawTopic.value)
  if (!t) {
    return {
      id: route.params.id,
      userId: '',
      title: '加载中...',
      author: '',
      time: '',
      category: '',
      content: '',
      views: 0,
      color: '#7C3AED',
      tagBg: 'rgba(124, 58, 237, 0.08)',
      tagColor: '#7C3AED'
    }
  }
  return {
    ...t,
    time: t.createTime ? dayjs(t.createTime).format('YYYY-MM-DD HH:mm') : '',
    tagBg: 'rgba(124, 58, 237, 0.08)',
    tagColor: t.color || '#7C3AED'
  }
})

const isTopicOwner = computed(() => {
  return currentUserId.value && String(topic.value.userId) === currentUserId.value
})

const liked = ref(false)
const favorited = ref(false)

// 评论高亮（跳转目标）
const highlightId = ref(null)

/** 被回复评论的内容片段 */
function replySnippet(parentId) {
  const c = comments.value.find(x => String(x.id) === String(parentId))
  if (!c || !c.content) return ''
  return c.content.length > 40 ? c.content.slice(0, 40) + '...' : c.content
}

/** 跳转到被回复评论并高亮 */
function scrollToComment(id) {
  const el = document.getElementById('comment-' + id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  highlightId.value = id
  setTimeout(() => { if (highlightId.value === id) highlightId.value = null }, 2000)
}

// 用户浮动卡片
const floatUser = ref(null)
const floatStyle = ref({ top: '0px', left: '0px' })

function showUserCard(user, event) {
  if (!user || !user.userId) return
  if (floatUser.value && floatUser.value.userId === user.userId) {
    floatUser.value = null; return
  }
  floatUser.value = user
  const rect = event.currentTarget.getBoundingClientRect()
  floatStyle.value = {
    position: 'fixed',
    top: Math.min(rect.bottom + 6, window.innerHeight - 200) + 'px',
    left: Math.min(rect.left, window.innerWidth - 280) + 'px',
  }
}

function isSelf(user) {
  return user && String(user.userId) === currentUserId.value
}
function isFollowing(user) {
  return user && following.value.some(f => String(f.userId) === String(user.userId))
}

function goPM(user) {
  floatUser.value = null
  router.push({ path: '/messages', query: { to: user.userId, name: user.nickname } })
}

async function toggleFollow(user) {
  if (isSelf(user)) return
  if (isFollowing(user)) {
    following.value = following.value.filter(f => String(f.userId) !== String(user.userId))
    ElMessage.info('已取消关注')
    try {
      await fetch(`/api/chat/follow/${user.userId}`, { method: 'DELETE', headers: { Authorization: localStorage.getItem('token') || '' } })
    } catch {}
  } else {
    following.value = [...following.value, { userId: String(user.userId), nickname: user.nickname }]
    ElMessage.success('已关注 ' + user.nickname)
    try {
      await fetch('/api/chat/follow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: localStorage.getItem('token') || '' },
        body: JSON.stringify({ userId: String(user.userId), nickname: user.nickname })
      })
    } catch {}
  }
  floatUser.value = null
}

function fmtTime(date) {
  if (!date) return ''
  return dayjs(date).format('MM-DD HH:mm')
}

async function loadDetail() {
  try {
    const res = await getTopicDetail(route.params.id)
    rawTopic.value = res.data
  } catch (e) {
    ElMessage.error('话题加载失败')
  }
}

async function loadComments() {
  try {
    const res = await getTopicComments(route.params.id, {pageNum: 1, pageSize: 100})
    comments.value = (res.data?.list || []).map(mapComment)
  } catch (e) {
    comments.value = []
  }
}

async function loadFollowing() {
  try {
    const res = await getFollowing()
    following.value = (res.data || []).map(u => ({ userId: String(u.userId), nickname: u.nickname }))
  } catch { following.value = [] }
}

function setReply(comment) {
  replyTo.value = {
    id: comment.id,
    rootId: comment.parentId ? comment.rootId : comment.id, // 回复一级评论用其 id 作根，回复回复沿用同根
    userId: comment.userId,
    name: comment.author,
  }
}

function cancelReply() {
  replyTo.value = null
}

async function submitReply() {
  const text = replyText.value.trim()
  if (!text) return
  replyLoading.value = true
  try {
    const payload = { postId: route.params.id, content: text }
    if (replyTo.value) {
      payload.parentId = replyTo.value.id
      payload.rootId = replyTo.value.rootId
      payload.replyUserId = replyTo.value.userId
    }
    await createComment(payload)
    replyText.value = ''
    replyTo.value = null
    ElMessage.success('评论已发表')
    await loadComments() // 重新加载以拿到 @被回复用户昵称
    if (rawTopic.value) {
      rawTopic.value.replycount = (rawTopic.value.replycount || 0) + 1
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    replyLoading.value = false
  }
}

async function handleLike() {
  try {
    const res = await toggleLike({targetType: 1, targetId: route.params.id})
    liked.value = res.data?.liked
    if (rawTopic.value) {
      rawTopic.value.likecount = res.data?.count ?? rawTopic.value.likecount
    }
  } catch (e) { /* 忽略 */
  }
}

async function handleFavorite() {
  try {
    const res = await toggleFavorite({postId: route.params.id})
    favorited.value = res.data?.favorited
    if (rawTopic.value) {
      rawTopic.value.favoritecount = res.data?.count ?? rawTopic.value.favoritecount
    }
  } catch (e) { /* 忽略 */
  }
}

async function handleReport() {
  try {
    const {value} = await ElMessageBox.prompt('请输入举报原因', '举报话题', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '原因不能为空'
    })
    await reportTarget({targetType: 1, targetId: route.params.id, reason: value})
    ElMessage.success('举报已提交')
  } catch (e) {
    // 用户取消或拦截器已提示
  }
}

async function handleDeleteTopic() {
  try {
    await ElMessageBox.confirm('确认删除该话题？删除后不可恢复。', '删除话题', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTopic(route.params.id)
    ElMessage.success('已删除')
    router.back()
  } catch (e) { /* 取消或拦截器已提示 */ }
}

async function handleDeleteComment(comment) {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '删除评论', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(comment.id)
    comments.value = comments.value.filter(c => c.id !== comment.id)
    if (rawTopic.value) {
      rawTopic.value.replycount = Math.max(0, (rawTopic.value.replycount || 0) - 1)
    }
    ElMessage.success('已删除')
  } catch (e) { /* 取消或拦截器已提示 */ }
}

onMounted(() => {
  loadDetail()
  loadComments()
  loadFollowing()
})
</script>

<style scoped>
.topic-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.topic-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 20px;
  flex-shrink: 0;
}

.topic-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-meta {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.publish-time {
  font-size: 11px;
  color: var(--text-muted);
}

.topic-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topic-tag {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.topic-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
  line-height: 1.4;
}

.topic-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
  margin-bottom: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.topic-footer {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}

.stat-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.action-btn {
  cursor: pointer;
  transition: color var(--transition-fast);
}

.action-btn:hover {
  color: var(--primary-light);
}

.action-btn.active {
  color: var(--warning-color);
}

.comments-section {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 16px 20px;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.comments-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 14px;
  flex-shrink: 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.comment-author {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.comment-reply {
  font-size: 12px;
  color: var(--accent-color);
  font-weight: 500;
}

.comment-time {
  font-size: 11px;
  color: var(--text-muted);
}

.comment-ops {
  margin-left: auto;
  display: flex;
  gap: 4px;
}

.comment-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 被回复评论引用块 */
.reply-quote {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  margin-bottom: 6px;
  background: var(--bg-light);
  border-left: 3px solid var(--accent-color);
  border-radius: 4px;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.reply-quote:hover {
  background: rgba(124, 58, 237, 0.08);
}
.rq-arrow {
  color: var(--accent-color);
  font-size: 13px;
  flex-shrink: 0;
}
.rq-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.rq-user {
  font-size: 12px;
  color: var(--accent-color);
  font-weight: 500;
}
.rq-content {
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 跳转高亮 */
.comment-item.highlight {
  background: rgba(124, 58, 237, 0.08);
  border-radius: 8px;
  transition: background 0.3s;
}

.reply-box {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}

.reply-target {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--accent-color);
  margin-bottom: 8px;
}

.reply-box :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  resize: none;
}

.reply-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

/* 可点击头像/昵称 */
.clickable {
  cursor: pointer;
  transition: opacity var(--transition-fast);
}
.clickable:hover {
  opacity: 0.8;
}

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
.uf-name { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 14px; }
.uf-actions { display: flex; gap: 8px; justify-content: center; }
</style>
