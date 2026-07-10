<script>
export default { name: 'TopicCategory' }
</script>

<template>
  <div class="topic-category">
    <div class="page-header">
      <h2>{{ categoryInfo.title }}</h2>
      <span class="subtitle">{{ categoryInfo.desc }}</span>
    </div>

    <div class="toolbar">
      <el-button round size="small" type="primary" @click="createTopicDialog">
        发布话题
      </el-button>
    </div>

    <!-- Create Topic Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="发布话题"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="70px"
        class="create-form"
      >
        <el-form-item label="分类" prop="category">
          <el-select v-model="createForm.category" placeholder="选择分类" style="width: 100%">
            <el-option
              v-for="opt in categoryOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input
            v-model="createForm.title"
            placeholder="请输入话题标题"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入话题内容"
            maxlength="2000"
            show-word-limit
            resize="none"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="dialogLoading">
          发布
        </el-button>
      </template>
    </el-dialog>

    <div class="topic-list">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <template v-else>
      <div
        v-for="item in topics"
        :key="item.id"
        class="topic-row"
        @click="goTopic(item)"
      >
        <div class="topic-avatar">
          <img :src="item.avatar" alt=""/>
        </div>
        <div class="topic-main">
          <div class="topic-title">{{ item.title }}</div>
          <div class="topic-meta">
            <span class="meta-author">{{ item.author }}</span>
            <span class="meta-time">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        <div class="topic-stats">
          <span><el-icon><ChatDotRound /></el-icon> {{ item.comments }}</span>
          <span><el-icon><View /></el-icon> {{ item.views }}</span>
        </div>
      </div>

      <el-empty v-if="!topics.length" description="暂无话题" :image-size="60" />
      </template>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {getTopics, createTopic, getBoards, mapTopic} from '@/api'

const router = useRouter()
const route = useRoute()

const categoryMap = {
  TopicRandom: { title: '随便聊聊', desc: '无主题闲聊，畅所欲言', color: '#7C3AED' },
  TopicTech: { title: '技术交流', desc: '分享技术心得与经验', color: '#3B82F6' },
  TopicGame: { title: '游戏讨论', desc: '游戏资讯与交流', color: '#16A34A' },
  TopicLife: { title: '生活分享', desc: '分享生活中的点点滴滴', color: '#EA580C' },
  TopicHelp: { title: '求助问答', desc: '提出问题，寻求帮助', color: '#F59E0B' }
}

const routeNameToCategory = {
  TopicRandom: 'random',
  TopicTech: 'tech',
  TopicGame: 'game',
  TopicLife: 'life',
  TopicHelp: 'help',
  TopicBoardCode: '__dynamic__'
}

const categoryCode = computed(() => {
  // 动态板块走 code 参数，固定板块走 route name 映射
  if (route.params.code) return route.params.code
  return routeNameToCategory[route.name] || 'random'
})

const categoryKey = computed(() => {
  // 固定分类用 code，动态板块也用 code
  return categoryCode.value
})

const categoryInfo = computed(() => {
  // 动态板块：优先用板块名称，回退 code
  if (route.params.code) {
    const board = boardList.value.find((b) => b.code === route.params.code)
    return {
      title: board ? board.name : route.params.code,
      desc: board ? board.description : '',
      color: '#64748B'
    }
  }
  return categoryMap[route.name] || { title: '话题板块', desc: '', color: '#64748B' }
})

const topics = ref([])
const loading = ref(false)
const boardList = ref([])

async function loadTopics() {
  loading.value = true
  try {
    const res = await getTopics({boardCode: categoryCode.value, sort: 'latest', pageNum: 1, pageSize: 20})
    topics.value = (res.data?.list || []).map(mapTopic)
  } catch (e) {
    topics.value = []
  } finally {
    loading.value = false
  }
}

async function loadBoards() {
  try {
    const res = await getBoards()
    boardList.value = res.data || []
  } catch (e) {
    boardList.value = []
  }
}

function formatTime(date) {
  if (!date) return ''
  return dayjs(date).format('MM-DD HH:mm')
}

function goTopic(item) {
  router.push(`/topic/${item.id}`)
}

// --- Create Topic Dialog ---
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const createFormRef = ref(null)

const categoryOptions = computed(() =>
    boardList.value.map((b) => ({label: b.name, value: b.code}))
)

const createForm = ref({
  title: '',
  content: '',
  category: ''
})

const createRules = {
  title: [
    { required: true, message: '请输入话题标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应为 2-50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入话题内容', trigger: 'blur' },
    { min: 5, max: 2000, message: '内容长度应为 5-2000 个字符', trigger: 'blur' }
  ]
}

function createTopicDialog() {
  createForm.value.category = categoryCode.value
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  dialogLoading.value = true
  try {
    await createTopic({
      boardCode: createForm.value.category,
      title: createForm.value.title,
      content: createForm.value.content
    })
    ElMessage.success('话题发布成功')
    dialogVisible.value = false
    createForm.value = {title: '', content: '', category: categoryCode.value}
    loadTopics()
  } catch (e) {
    // 拦截器已提示
  } finally {
    dialogLoading.value = false
  }
}

function handleDialogClosed() {
  createForm.value = { title: '', content: '', category: '' }
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
}

onMounted(() => {
  loadBoards()
  loadTopics()
})

watch(categoryCode, () => loadTopics())
</script>

<style scoped>
.topic-category {
  height: 100%;
  display: flex;
  flex-direction: column;
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

.toolbar {
  padding: 0 4px;
  margin-bottom: 10px;
}

.topic-list {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 8px;
}

.loading-tip {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
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
}

.meta-author {
  color: var(--primary-light);
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

/* Create Topic Dialog */
/* 创建表单：外层统一包裹，内层透明 */
.create-form :deep(.el-input) {
  border-radius: var(--radius-md);
  background: var(--bg-light);
  border: 1px solid var(--border-color);
  box-shadow: none;
  overflow: hidden;
}

.create-form :deep(.el-input.is-focus) {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  box-shadow: none;
}

.create-form :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  padding: 8px 14px;
}

.create-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  padding: 10px 14px;
  background: var(--bg-light);
  box-shadow: none;
  transition: all var(--transition-fast);
}

.create-form :deep(.el-textarea__inner:hover) {
  box-shadow: none;
}

.create-form :deep(.el-textarea__inner:focus) {
  background: var(--bg-white);
  box-shadow: none;
}

.create-form :deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}
</style>
