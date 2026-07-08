<script>
export default { name: 'TopicDetail' }
</script>

<template>
  <div class="topic-detail">
    <!-- 话题正文 -->
    <div class="topic-card">
      <div class="topic-header">
        <div class="author-info">
          <div class="author-avatar">
            <img :src="topic.avatar" alt=""/>
          </div>
          <div class="author-meta">
            <span class="author-name">{{ topic.author }}</span>
            <span class="publish-time">{{ topic.time }}</span>
          </div>
        </div>
        <span class="topic-tag" :style="{ background: topic.tagBg, color: topic.tagColor }">{{ topic.category }}</span>
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
          <el-icon>
            <Star/>
          </el-icon>
          <span>{{ topic.likes }} 点赞</span>
        </div>
        <div :class="{ active: favorited }" class="stat-btn action-btn" @click="handleFavorite">
          <el-icon>
            <CollectionTag/>
          </el-icon>
          <span>{{ topic.favorites }} 收藏</span>
        </div>
        <div class="stat-btn action-btn" @click="handleReport">
          <el-icon>
            <WarnTriangleFilled/>
          </el-icon>
          <span>举报</span>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comments-section">
      <h3 class="comments-title">评论 ({{ comments.length }})</h3>

      <div class="comments-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">
            <img :src="comment.avatar" alt=""/>
          </div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-author">{{ comment.author }}</span>
              <span class="comment-time">{{ fmtTime(comment.createTime) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>

        <el-empty v-if="!comments.length" description="暂无评论" :image-size="50" />
      </div>

      <!-- 回复输入框 -->
      <div class="reply-box">
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="3"
          placeholder="写下你的回复..."
          maxlength="500"
          show-word-limit
        />
        <div class="reply-actions">
          <el-button type="primary" @click="submitReply" :disabled="!replyText.trim()">
            发表评论
          </el-button>
        </div>
      </div>
    </div>
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
  mapTopic,
  mapComment
} from '@/api'

const router = useRouter()
const route = useRoute()
const replyText = ref('')
const replyLoading = ref(false)

const rawTopic = ref(null)
const comments = ref([])

const topic = computed(() => {
  const t = mapTopic(rawTopic.value)
  if (!t) {
    return {
      id: route.params.id,
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

const liked = ref(false)
const favorited = ref(false)

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

async function submitReply() {
  const text = replyText.value.trim()
  if (!text) return
  replyLoading.value = true
  try {
    const res = await createComment({postId: route.params.id, content: text})
    comments.value.push(mapComment(res.data))
    // 本地评论数 +1
    if (rawTopic.value) {
      rawTopic.value.replycount = (rawTopic.value.replycount || 0) + 1
    }
    replyText.value = ''
    ElMessage.success('评论已发表')
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

onMounted(() => {
  loadDetail()
  loadComments()
})
</script>

<style scoped>
.topic-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}

.topic-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 20px;
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
}

.comments-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 14px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
}

.comment-author {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  font-size: 11px;
  color: var(--text-muted);
}

.comment-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.reply-box {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
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
</style>
