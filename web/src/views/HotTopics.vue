<script>
export default { name: 'HotTopics' }
</script>

<template>
  <div class="hot-topics">
    <div class="page-header">
      <h2>热门话题</h2>
      <span class="subtitle">按热度排序</span>
    </div>

    <div class="topics-list">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <template v-else>
      <div
        v-for="(item, idx) in topics"
        :key="item.id"
        class="topic-card"
        @click="goTopic(item)"
      >
        <div class="topic-rank" :class="{ top: idx < 3 }">{{ idx + 1 }}</div>
        <div class="topic-body">
          <div class="topic-title">{{ item.title }}</div>
          <div class="topic-meta">
            <span class="meta-author">{{ item.author }}</span>
            <span class="meta-tag">{{ item.category }}</span>
            <span class="meta-time">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        <div class="topic-stats">
          <div class="stat">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ formatHeat(item.heat) }}</span>
          </div>
          <div class="stat">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ item.comments }}</span>
          </div>
          <div class="stat">
            <el-icon><View /></el-icon>
            <span>{{ item.views }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!topics.length" description="暂无热门话题" :image-size="60" />
      </template>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { TrendCharts, ChatDotRound, View } from '@element-plus/icons-vue'
import {getHotTopics, mapTopic} from '@/api'

const router = useRouter()

const topics = ref([])
const loading = ref(false)

async function loadHot() {
  loading.value = true
  try {
    const res = await getHotTopics(20)
    topics.value = (res.data || []).map(mapTopic)
  } catch (e) {
    topics.value = []
  } finally {
    loading.value = false
  }
}

function formatHeat(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return n.toString()
}

function formatTime(date) {
  if (!date) return ''
  return dayjs(date).format('MM-DD HH:mm')
}

function goTopic(item) {
  router.push(`/topic/${item.id}`)
}

onMounted(loadHot)
</script>

<style scoped>
.hot-topics {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.loading-tip {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
}

.page-header {
  padding: 0 4px;
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.page-header h2 {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.subtitle {
  font-size: 12px;
  color: var(--text-muted);
}

.topics-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.topic-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.topic-card:hover {
  border-color: var(--primary-light);
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.06);
}

.topic-rank {
  width: 24px;
  height: 24px;
  background: var(--bg-light);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-muted);
  flex-shrink: 0;
  margin-top: 2px;
}

.topic-rank.top {
  background: var(--warning-color);
  color: #fff;
}

.topic-body {
  flex: 1;
  min-width: 0;
}

.topic-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 5px;
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
  padding: 1px 6px;
  border-radius: 4px;
  color: var(--accent-color);
  font-size: 11px;
}

.topic-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}
</style>
