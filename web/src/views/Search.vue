<script>
export default { name: 'Search' }
</script>

<template>
  <div class="search-page">
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="搜索话题、用户..."
        size="large"
        clearable
        @keydown.enter.prevent="doSearch"
      >
        <template #prefix>
          <el-icon><SearchIcon /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="doSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索前 -->
    <div v-if="!hasSearched" class="search-empty">
      <div class="hot-words">
        <span class="label">热门搜索：</span>
        <el-tag
          v-for="word in hotWords"
          :key="word"
          class="hot-word"
          round
          @click="keyword = word; doSearch()"
        >
          {{ word }}
        </el-tag>
      </div>
    </div>

    <!-- 搜索后 -->
    <div v-else class="search-results">
      <div class="result-info">
        找到 <strong>{{ results.length }}</strong> 条结果
      </div>

      <div v-if="results.length" class="result-list">
        <div
          v-for="item in results"
          :key="item.id"
          class="result-item"
          @click="goTopic(item)"
        >
          <div class="result-avatar">
            <img :src="item.avatar" alt=""/>
          </div>
          <div class="result-body">
            <div class="result-title">{{ item.title }}</div>
            <div class="result-meta">
              <span>{{ item.author }}</span>
              <span class="dot">|</span>
              <span>{{ item.category }}</span>
              <span class="dot">|</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="未找到相关内容" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { Search as SearchIcon } from '@element-plus/icons-vue'
import {searchTopics, mapTopic} from '@/api'

const router = useRouter()
const keyword = ref('')
const hasSearched = ref(false)
const results = ref([])
const searching = ref(false)

const hotWords = ['匿名社交', 'Vue3', '游戏推荐', '周末', '求助']

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  hasSearched.value = true
  searching.value = true
  try {
    const res = await searchTopics({keyword: kw, pageNum: 1, pageSize: 30})
    results.value = (res.data?.list || []).map(mapTopic)
  } catch (e) {
    results.value = []
  } finally {
    searching.value = false
  }
}

function formatTime(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

function goTopic(item) {
  router.push(`/topic/${item.id}`)
}
</script>

<style scoped>
.search-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-header {
  padding: 0 4px;
  margin-bottom: 16px;
}

.search-empty {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 24px;
}

.hot-words {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.label {
  font-size: 13px;
  color: var(--text-secondary);
}

.hot-word {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.hot-word:hover {
  transform: translateY(-1px);
}

.search-results {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.result-info {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.result-info strong {
  color: var(--text-primary);
}

.result-list {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 4px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.result-item:hover {
  background: var(--bg-light);
}

.result-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.result-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-body {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.result-meta {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  gap: 4px;
  align-items: center;
}

.dot {
  color: var(--border-light);
}
</style>
