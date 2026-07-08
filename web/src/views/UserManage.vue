<script>
export default {name: 'UserManage'}
</script>

<template>
  <div class="user-manage">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-title">用户总数</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-card success">
        <div class="stat-title">正常用户</div>
        <div class="stat-value">{{ stats.normal }}</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-title">封禁用户</div>
        <div class="stat-value">{{ stats.banned }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">管理员</div>
        <div class="stat-value">{{ stats.admin }}</div>
      </div>
    </div>

    <!-- 搜索过滤 -->
    <div class="filter-card">
      <div class="filter-row">
        <el-input
            v-model="searchForm.keyword"
            clearable
            placeholder="搜索账号或昵称"
            style="width: 240px"
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
            placeholder="账号状态"
            style="width: 140px"
            @change="handleSearch"
        >
          <el-option :value="null" label="全部"/>
          <el-option :value="1" label="正常"/>
          <el-option :value="2" label="封禁"/>
        </el-select>
        <el-select
            v-model="searchForm.role"
            clearable
            placeholder="角色"
            style="width: 140px"
            @change="handleSearch"
        >
          <el-option :value="null" label="全部"/>
          <el-option :value="0" label="普通用户"/>
          <el-option :value="1" label="管理员"/>
        </el-select>
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
          新增用户
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="userList" border stripe style="width: 100%">
        <el-table-column label="账号" min-width="120" prop="username"/>
        <el-table-column label="昵称" min-width="120" prop="nickname"/>
        <el-table-column align="center" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
              {{ row.role === 1 ? '管理员' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录IP" min-width="130" prop="lastloginip">
          <template #default="{ row }">{{ row.lastloginip || '-' }}</template>
        </el-table-column>
        <el-table-column label="最后登录" min-width="150">
          <template #default="{ row }">{{ formatDate(row.lastloginat) }}</template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="150">
          <template #default="{ row }">{{ formatDate(row.createdat) }}</template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="280">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link size="small" type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button
                v-if="row.status === 1"
                link
                size="small"
                type="danger"
                @click="handleToggleBan(row, 2)"
            >封禁
            </el-button>
            <el-button
                v-else
                link
                size="small"
                type="success"
                @click="handleToggleBan(row, 1)"
            >解封
            </el-button>
            <el-button link size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        destroy-on-close
        width="480px"
        @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item v-if="!isEditing" label="账号" prop="username">
          <el-input v-model="formData.username" maxlength="32" placeholder="3-32位字母数字下划线"/>
        </el-form-item>
        <el-form-item v-if="!isEditing" label="密码" prop="password">
          <el-input v-model="formData.password" maxlength="32" placeholder="6-32位" show-password type="password"/>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" maxlength="32" placeholder="留空则用账号"/>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" style="width: 100%">
            <el-option :value="0" label="普通用户"/>
            <el-option :value="1" label="管理员"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="dialogLoading" type="primary" @click="handleSubmit">
          {{ isEditing ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
        v-model="pwdDialogVisible"
        :close-on-click-modal="false"
        destroy-on-close
        title="重置密码"
        width="420px"
    >
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="账号">
          <el-input :model-value="pwdForm.username" disabled/>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" maxlength="32" placeholder="6-32位" show-password type="password"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button :loading="pwdLoading" type="primary" @click="handleResetSubmit">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search, Refresh, Plus} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getUserPageList,
  createUser,
  deleteUser,
  resetUserPassword,
  updateUserStatus,
  updateUser
} from '@/api'

// 统计
const stats = reactive({total: 0, normal: 0, banned: 0, admin: 0})

// 搜索
const searchForm = reactive({keyword: '', status: null, role: null})

// 列表
const userList = ref([])
const total = ref(0)
const loading = ref(false)
const pagination = reactive({pageNum: 1, pageSize: 10})

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const formData = reactive({username: '', password: '', nickname: '', role: 0})

const formRules = computed(() => ({
  username: isEditing.value
      ? []
      : [
        {required: true, message: '请输入账号', trigger: 'blur'},
        {pattern: /^[A-Za-z0-9_]{3,32}$/, message: '3-32位字母数字下划线', trigger: 'blur'}
      ],
  password: isEditing.value
      ? []
      : [
        {required: true, message: '请输入密码', trigger: 'blur'},
        {min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur'}
      ]
}))

const dialogTitle = computed(() => (isEditing.value ? '编辑用户' : '新增用户'))

// 重置密码对话框
const pwdDialogVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({id: null, username: '', password: ''})
const pwdRules = {
  password: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur'}
  ]
}

async function fetchList() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
    if (searchForm.role !== null && searchForm.role !== '') params.role = searchForm.role

    const res = await getUserPageList(params)
    if (res.code === 200 && res.data) {
      userList.value = res.data.list || []
      total.value = res.data.total || 0
      stats.total = res.data.total || 0
      stats.normal = userList.value.filter((u) => u.status === 1).length
      stats.banned = userList.value.filter((u) => u.status === 2).length
      stats.admin = userList.value.filter((u) => u.role === 1).length
    } else {
      userList.value = []
      total.value = 0
    }
  } catch (e) {
    userList.value = []
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
  searchForm.role = null
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
  Object.assign(formData, {username: '', password: '', nickname: '', role: 0})
  dialogVisible.value = true
}

function handleEdit(row) {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(formData, {
    username: row.username || '',
    password: '',
    nickname: row.nickname || '',
    role: row.role ?? 0
  })
  dialogVisible.value = true
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
    if (isEditing.value) {
      await updateUser({
        id: editingId.value,
        nickname: formData.nickname,
        role: formData.role
      })
      ElMessage.success('编辑成功')
    } else {
      await createUser({
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
        role: formData.role
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    dialogLoading.value = false
  }
}

function handleDialogClosed() {
  Object.assign(formData, {username: '', password: '', nickname: '', role: 0})
  if (formRef.value) formRef.value.resetFields()
}

function handleResetPwd(row) {
  pwdForm.id = row.id
  pwdForm.username = row.username
  pwdForm.password = ''
  pwdDialogVisible.value = true
}

async function handleResetSubmit() {
  if (!pwdFormRef.value) return
  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }
  pwdLoading.value = true
  try {
    await resetUserPassword({id: pwdForm.id, password: pwdForm.password})
    ElMessage.success('密码已重置')
    pwdDialogVisible.value = false
  } finally {
    pwdLoading.value = false
  }
}

async function handleToggleBan(row, status) {
  const action = status === 2 ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.username}」吗？`, `${action}确认`, {
      confirmButtonText: `确定${action}`,
      cancelButtonText: '取消',
      type: status === 2 ? 'warning' : 'success'
    })
    await updateUserStatus({id: row.id, status})
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      // 接口错误由拦截器处理
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      // 接口错误由拦截器处理
    }
  }
}

function formatDate(date) {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(fetchList)
</script>

<style scoped>
.user-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  overflow-y: auto;
  padding-bottom: var(--spacing-lg);
}

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

.table-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  box-shadow: var(--shadow-sm);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: var(--spacing-md);
}

@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-row .el-input,
  .filter-row .el-select {
    width: 100% !important;
  }

  .filter-spacer {
    display: none;
  }
}
</style>
