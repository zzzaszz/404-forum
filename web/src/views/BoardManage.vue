<script>
export default {name: 'BoardManage'}
</script>

<template>
  <div class="board-manage">
    <!-- 统计卡片行 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-title">板块总数</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-card success">
        <div class="stat-title">正常板块</div>
        <div class="stat-value">{{ stats.active }}</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-title">隐藏板块</div>
        <div class="stat-value">{{ stats.hidden }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">总帖子数</div>
        <div class="stat-value">{{ stats.totalPosts }}</div>
      </div>
    </div>

    <!-- 搜索过滤卡片 -->
    <div class="filter-card">
      <div class="filter-row">
        <el-input
            v-model="searchForm.keyword"
            clearable
            placeholder="搜索板块名称或编码"
            style="width: 260px"
            @clear="handleSearch"
            @keydown.enter="handleSearch"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
        <el-select
            v-model="searchForm.status"
            clearable
            placeholder="板块状态"
            style="width: 140px"
            @change="handleSearch"
        >
          <el-option :value="null" label="全部"/>
          <el-option :value="1" label="正常"/>
          <el-option :value="0" label="隐藏"/>
        </el-select>
        <el-date-picker
            v-model="searchForm.dateRange"
            end-placeholder="结束日期"
            range-separator="至"
            start-placeholder="开始日期"
            style="width: 260px"
            type="daterange"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon>
            <Search/>
          </el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon>
            <Refresh/>
          </el-icon>
          重置
        </el-button>
        <div class="filter-spacer"></div>
        <el-button type="primary" @click="handleCreate">
          <el-icon>
            <Plus/>
          </el-icon>
          新增板块
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon :size="32" class="is-loading">
        <Loading/>
      </el-icon>
      <p>加载中...</p>
    </div>

    <!-- 板块卡片网格 -->
    <div v-else-if="boardList.length > 0" class="board-grid">
      <div
          v-for="item in boardList"
          :key="item.id"
          class="board-card fade-in"
      >
        <div class="board-card-header">
          <div class="board-icon">
            <img v-if="item.icon" :alt="item.name" :src="item.icon" @error="onIconError"/>
            <el-icon v-else :size="28">
              <Collection/>
            </el-icon>
          </div>
          <div class="board-info">
            <h3 class="board-name">{{ item.name }}</h3>
            <span class="board-code">{{ item.code }}</span>
          </div>
          <el-tag
              :type="item.status === 1 ? 'success' : 'info'"
              effect="plain"
              size="small"
          >
            {{ item.status === 1 ? '正常' : '隐藏' }}
          </el-tag>
        </div>

        <p class="board-desc">{{ item.description || '暂无简介' }}</p>

        <div class="board-meta">
          <div class="meta-item">
            <span class="meta-label">排序</span>
            <span class="meta-value">{{ item.sortorder ?? 0 }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <ChatDotRound/>
            </el-icon>
            <span>{{ item.postcount ?? 0 }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <Star/>
            </el-icon>
            <span>{{ item.followcount ?? 0 }}</span>
          </div>
        </div>

        <div class="board-card-footer">
          <span class="board-time">{{ formatDate(item.updatedat || item.createdat) }}</span>
          <div class="board-actions">
            <el-button link size="small" type="primary" @click="handleEdit(item)">
              <el-icon>
                <Edit/>
              </el-icon>
              编辑
            </el-button>
            <el-button link size="small" type="danger" @click="handleDelete(item)">
              <el-icon>
                <Delete/>
              </el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="暂无板块数据">
        <el-button type="primary" @click="handleCreate">新增板块</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[12, 24, 48, 96]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        destroy-on-close
        width="560px"
        @closed="handleDialogClosed"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="right"
          label-width="100px"
      >
        <el-form-item label="板块名称" prop="name">
          <el-input v-model="formData.name" maxlength="50" placeholder="请输入板块名称" show-word-limit/>
        </el-form-item>
        <el-form-item label="板块编码" prop="code">
          <el-input v-model="formData.code" maxlength="30" placeholder="请输入板块编码（唯一标识）" show-word-limit/>
        </el-form-item>
        <el-form-item label="图标URL" prop="icon">
          <el-input v-model="formData.icon" placeholder="请输入图标URL（可选）">
            <template #append>
              <div v-if="formData.icon" class="icon-preview">
                <img :src="formData.icon" class="icon-preview-img" @error="onPreviewError"/>
              </div>
              <span v-else>预览</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="板块简介" prop="description">
          <el-input
              v-model="formData.description"
              :rows="3"
              maxlength="200"
              placeholder="请输入板块简介"
              show-word-limit
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="排序值" prop="sortorder">
          <el-input-number v-model="formData.sortorder" :max="9999" :min="0" placeholder="越大越靠前"
                           style="width: 100%"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
              v-model="statusSwitch"
              active-text="正常"
              inactive-text="隐藏"
              @change="onStatusChange"
          />
        </el-form-item>
        <el-form-item label="首页快捷">
          <el-switch
              v-model="showOnHomeSwitch"
              active-text="显示"
              inactive-text="不显示"
              @change="onShowOnHomeChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="dialogLoading" type="primary" @click="handleSubmit">
          {{ isEditing ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getBoardPageList, createBoard, updateBoard, deleteBoard} from '@/api'
import dayjs from 'dayjs'

// ---------- 统计 ----------
const stats = reactive({
  total: 0,
  active: 0,
  hidden: 0,
  totalPosts: 0
})

// ---------- 搜索 ----------
const searchForm = reactive({
  keyword: '',
  status: null,
  dateRange: null
})

// ---------- 列表 ----------
const boardList = ref([])
const total = ref(0)
const loading = ref(false)
const pagination = reactive({
  pageNum: 1,
  pageSize: 12
})

// ---------- 对话框 ----------
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const formData = reactive({
  name: '',
  code: '',
  icon: '',
  description: '',
  sortorder: 0,
  status: 1,
  showonhome: 0
})

// 单独用 ref 追踪开关状态，避免 el-switch 与 reactive 对象同步时丢失 falsy 值
const statusSwitch = ref(true)
const onStatusChange = (val) => {
  formData.status = val ? 1 : 0
}
const showOnHomeSwitch = ref(false)
const onShowOnHomeChange = (val) => {
  formData.showonhome = val ? 1 : 0
}

const formRules = {
  name: [
    {required: true, message: '请输入板块名称', trigger: 'blur'},
    {min: 1, max: 50, message: '名称长度 1-50 个字符', trigger: 'blur'}
  ],
  code: [
    {required: true, message: '请输入板块编码', trigger: 'blur'},
    {min: 1, max: 30, message: '编码长度 1-30 个字符', trigger: 'blur'}
  ]
}

const dialogTitle = computed(() => (isEditing.value ? '编辑板块' : '新增板块'))

// ---------- 方法 ----------
async function fetchList() {
  loading.value = true
  try {
    const params = {
      PageNum: pagination.pageNum,
      PageSize: pagination.pageSize,
      OrderType: 1,
      SearchType: 1
    }
    if (searchForm.keyword) {
      params.Key = searchForm.keyword
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.Status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.DateRange = {
        StartDate: searchForm.dateRange[0] + ' 00:00:00',
        EndDate: searchForm.dateRange[1] + ' 23:59:59'
      }
    }
    const res = await getBoardPageList(params)
    if (res.code === 200 && res.data) {
      boardList.value = res.data.list || []
      total.value = res.data.total || 0
      // 更新统计
      stats.total = res.data.total || 0
      stats.active = (res.data.list || []).filter(item => item.status === 1).length
      stats.hidden = (res.data.list || []).filter(item => item.status === 0).length
      stats.totalPosts = (res.data.list || []).reduce((sum, item) => sum + (item.postcount || 0), 0)
    } else {
      boardList.value = []
      total.value = 0
    }
  } catch (e) {
    boardList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  fetchList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  searchForm.dateRange = null
  pagination.pageNum = 1
  fetchList()
}

function handleSizeChange() {
  pagination.pageNum = 1
  fetchList()
}

function handlePageChange() {
  fetchList()
}

function handleCreate() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  statusSwitch.value = true
  showOnHomeSwitch.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  isEditing.value = true
  editingId.value = row.id
  formData.name = row.name || ''
  formData.code = row.code || ''
  formData.icon = row.icon || ''
  formData.description = row.description || ''
  formData.sortorder = row.sortorder ?? 0
  formData.status = row.status ?? 1
  formData.showonhome = row.showonhome ?? 0
  statusSwitch.value = (row.status ?? 1) === 1
  showOnHomeSwitch.value = (row.showonhome ?? 0) === 1
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
        `确定要删除板块「${row.name}」吗？删除后将无法恢复。`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    const res = await deleteBoard(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
    }
  } catch (e) {
    if (e !== 'cancel') {
      // 接口报错由拦截器统一处理
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  dialogLoading.value = true
  try {
    // 使用 JSON 序列化确保 falsy 值（如 status=0）不会在展开 reactive 时丢失
    const payload = JSON.parse(JSON.stringify(formData))
    const res = isEditing.value
        ? await updateBoard({...payload, id: editingId.value})
        : await createBoard(payload)
    if (res.code === 200) {
      ElMessage.success(isEditing.value ? '编辑成功' : '创建成功')
      dialogVisible.value = false
      fetchList()
    }
  } finally {
    dialogLoading.value = false
  }
}

function handleDialogClosed() {
  resetForm()
}

function resetForm() {
  formData.name = ''
  formData.code = ''
  formData.icon = ''
  formData.description = ''
  formData.sortorder = 0
  formData.status = 1
  formData.showonhome = 0
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

function onIconError(e) {
  e.target.style.display = 'none'
}

function onPreviewError(e) {
  e.target.style.display = 'none'
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.board-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  overflow-y: auto;
  padding-bottom: var(--spacing-lg);
}

/* ============ 统计卡片 ============ */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.stat-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  border-left: 4px solid var(--primary-color);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-card.success {
  border-left-color: var(--success-color);
}

.stat-card.warning {
  border-left-color: var(--warning-color);
}

.stat-card .stat-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
}

.stat-card .stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
}

/* ============ 搜索过滤卡片 ============ */
.filter-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-md) var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.filter-spacer {
  flex: 1;
}

/* ============ 加载 / 空状态 ============ */
.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  gap: var(--spacing-sm);
}

.empty-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ============ 板块卡片网格 ============ */
.board-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-md);
  align-content: start;
}

.board-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  transition: all var(--transition-normal);
}

.board-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-light);
}

.board-card-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.board-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--bg-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  color: var(--accent-color);
}

.board-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.board-info {
  flex: 1;
  min-width: 0;
}

.board-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.board-code {
  font-size: 12px;
  color: var(--text-muted);
}

.board-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}

.board-meta {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-label {
  font-size: 12px;
  color: var(--text-muted);
}

.board-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.board-time {
  font-size: 12px;
  color: var(--text-muted);
}

.board-actions {
  display: flex;
  gap: var(--spacing-xs);
}

/* ============ 分页 ============ */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: var(--spacing-sm);
}

/* ============ 对话框图标预览 ============ */
.icon-preview {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.icon-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ============ 响应式 ============ */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .board-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 767px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-row .el-input,
  .filter-row .el-select,
  .filter-row .el-date-picker {
    width: 100% !important;
  }

  .filter-spacer {
    display: none;
  }

  .board-grid {
    grid-template-columns: 1fr;
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .board-card,
  .stat-card {
    transition: none;
  }
}
</style>
