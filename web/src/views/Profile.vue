<template>
  <div class="profile-page">
    <div class="profile-layout">
      <!-- 左侧用户信息 -->
      <div class="profile-sidebar">
        <div class="user-card">
          <el-avatar :size="80" class="user-avatar">
            {{ userInitial }}
          </el-avatar>
          <div class="user-info">
            <div class="user-nickname">{{ userInfo.nickname }}</div>
            <div class="user-id">{{ userInfo.anonymousId }}</div>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userStats.posts }}</span>
              <span class="stat-label">发布</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.comments }}</span>
              <span class="stat-label">评论</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.likes }}</span>
              <span class="stat-label">获赞</span>
            </div>
          </div>
        </div>

        <el-menu
          :default-active="activeTab"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="privacy">
            <el-icon><Lock /></el-icon>
            <span>隐私设置</span>
          </el-menu-item>
          <el-menu-item index="notifications">
            <el-icon><Bell /></el-icon>
            <span>消息通知</span>
          </el-menu-item>
          <el-menu-item index="history">
            <el-icon><Clock /></el-icon>
            <span>活动记录</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 个人资料 -->
        <div v-show="activeTab === 'profile'" class="content-panel">
          <el-card shadow="never">
            <template #header>
              <div class="panel-header">
                <el-icon><User /></el-icon>
                <span>个人资料</span>
              </div>
            </template>

            <el-form :model="profileForm" label-width="100px" class="profile-form">
              <el-form-item label="匿名昵称">
                <el-input v-model="profileForm.nickname" placeholder="你的匿名昵称" maxlength="20" />
              </el-form-item>
              <el-form-item label="匿名ID">
                <el-input v-model="userInfo.anonymousId" disabled />
                <div class="form-tip">匿名ID由系统自动生成，无法修改</div>
              </el-form-item>
              <el-form-item label="个性签名">
                <el-input v-model="profileForm.signature" type="textarea" :rows="3" placeholder="写一句话介绍自己" maxlength="100" show-word-limit />
              </el-form-item>
              <el-form-item label="头像颜色">
                <div class="avatar-colors">
                  <div
                    v-for="color in avatarColors"
                    :key="color.value"
                    class="color-option"
                    :class="{ active: profileForm.avatarColor === color.value }"
                    :style="{ background: color.gradient }"
                    @click="profileForm.avatarColor = color.value"
                  ></div>
                </div>
              </el-form-item>
            </el-form>

            <div class="panel-actions">
              <el-button type="primary" @click="saveProfile">
                <el-icon><Check /></el-icon>
                保存修改
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 隐私设置 -->
        <div v-show="activeTab === 'privacy'" class="content-panel">
          <el-card shadow="never">
            <template #header>
              <div class="panel-header">
                <el-icon><Lock /></el-icon>
                <span>隐私设置</span>
              </div>
            </template>

            <div class="privacy-list">
              <div class="privacy-item">
                <div class="privacy-left">
                  <div class="privacy-title">隐藏在线状态</div>
                  <div class="privacy-desc">其他用户将无法看到你的在线状态</div>
                </div>
                <el-switch v-model="privacySettings.hideOnline" />
              </div>
              <el-divider />
              <div class="privacy-item">
                <div class="privacy-left">
                  <div class="privacy-title">禁止私信</div>
                  <div class="privacy-desc">其他用户将无法向你发送私信</div>
                </div>
                <el-switch v-model="privacySettings.blockMessages" />
              </div>
              <el-divider />
              <div class="privacy-item">
                <div class="privacy-left">
                  <div class="privacy-title">隐藏活动记录</div>
                  <div class="privacy-desc">你的发帖和评论记录将不对外显示</div>
                </div>
                <el-switch v-model="privacySettings.hideActivity" />
              </div>
            </div>
          </el-card>
        </div>

        <!-- 消息通知 -->
        <div v-show="activeTab === 'notifications'" class="content-panel">
          <el-card shadow="never">
            <template #header>
              <div class="panel-header">
                <el-icon><Bell /></el-icon>
                <span>消息通知</span>
              </div>
            </template>

            <div class="notification-list">
              <div class="notification-item">
                <div class="notification-left">
                  <div class="notification-title">话题回复通知</div>
                  <div class="notification-desc">有人回复你发布的话题时通知你</div>
                </div>
                <el-switch v-model="notificationSettings.topicReply" />
              </div>
              <el-divider />
              <div class="notification-item">
                <div class="notification-left">
                  <div class="notification-title">评论点赞通知</div>
                  <div class="notification-desc">有人点赞你的评论时通知你</div>
                </div>
                <el-switch v-model="notificationSettings.commentLike" />
              </div>
              <el-divider />
              <div class="notification-item">
                <div class="notification-left">
                  <div class="notification-title">私信通知</div>
                  <div class="notification-desc">收到私信时通知你</div>
                </div>
                <el-switch v-model="notificationSettings.privateMessage" />
              </div>
            </div>
          </el-card>
        </div>

        <!-- 活动记录 -->
        <div v-show="activeTab === 'history'" class="content-panel">
          <el-card shadow="never">
            <template #header>
              <div class="panel-header">
                <el-icon><Clock /></el-icon>
                <span>活动记录</span>
              </div>
            </template>

            <div class="history-list">
              <div v-for="item in activityHistory" :key="item.id" class="history-item">
                <div class="history-type" :class="item.type">
                  {{ item.type === 'post' ? '发帖' : item.type === 'comment' ? '评论' : '点赞' }}
                </div>
                <div class="history-content">
                  <div class="history-title">{{ item.title }}</div>
                  <div class="history-time">{{ formatTime(item.time) }}</div>
                </div>
              </div>
              <el-empty v-if="activityHistory.length === 0" description="暂无活动记录" :image-size="60" />
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const activeTab = ref('profile')

// 用户信息
const userInfo = ref({
  nickname: '匿名用户',
  anonymousId: '匿名#A3F',
  loginTime: ''
})

// 用户统计数据
const userStats = ref({
  posts: 12,
  comments: 45,
  likes: 89
})

// 个人资料表单
const profileForm = ref({
  nickname: '',
  signature: '',
  avatarColor: 'purple'
})

// 头像颜色选项
const avatarColors = [
  { value: 'purple', gradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { value: 'blue', gradient: 'linear-gradient(135deg, #3498db, #2980b9)' },
  { value: 'green', gradient: 'linear-gradient(135deg, #27ae60, #2ecc71)' },
  { value: 'orange', gradient: 'linear-gradient(135deg, #f39c12, #e74c3c)' },
  { value: 'pink', gradient: 'linear-gradient(135deg, #f64f59, #c471ed)' }
]

// 隐私设置
const privacySettings = ref({
  hideOnline: false,
  blockMessages: false,
  hideActivity: false
})

// 通知设置
const notificationSettings = ref({
  topicReply: true,
  commentLike: true,
  privateMessage: true
})

// 活动历史
const activityHistory = ref([
  { id: 1, type: 'post', title: '有没有人知道怎么解决这个问题？', time: new Date(Date.now() - 3600000) },
  { id: 2, type: 'comment', title: '回复了话题：今天遇到一个很奇怪的事情', time: new Date(Date.now() - 7200000) },
  { id: 3, type: 'like', title: '点赞了评论：这个观点很有意思', time: new Date(Date.now() - 1080000) }
])

// 用户头像首字母
const userInitial = computed(() => {
  return userInfo.value.nickname?.charAt(0)?.toUpperCase() || '匿'
})

// 加载用户信息
const loadUserInfo = () => {
  try {
    const storedInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    userInfo.value = {
      nickname: storedInfo.nickname || '匿名用户',
      anonymousId: storedInfo.anonymousId || '匿名#???',
      loginTime: storedInfo.loginTime || ''
    }
    profileForm.value.nickname = userInfo.value.nickname
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 菜单选择
const handleMenuSelect = (index) => {
  activeTab.value = index
}

// 保存个人资料
const saveProfile = () => {
  if (!profileForm.value.nickname || profileForm.value.nickname.length < 2) {
    ElMessage.warning('昵称至少需要2个字符')
    return
  }

  // 更新本地存储
  const storedInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  storedInfo.nickname = profileForm.value.nickname
  storedInfo.signature = profileForm.value.signature
  storedInfo.avatarColor = profileForm.value.avatarColor
  localStorage.setItem('userInfo', JSON.stringify(storedInfo))

  userInfo.value.nickname = profileForm.value.nickname
  ElMessage.success('资料已更新')
}

// 格式化时间
const formatTime = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  height: 100%;
  background-color: var(--bg-color);
}

.profile-layout {
  display: flex;
  gap: 20px;
  height: 100%;
}

/* 左侧边栏 */
.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-avatar {
  background: linear-gradient(135deg, var(--accent-color), var(--accent-dark));
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 12px;
}

.user-info .user-nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.user-info .user-id {
  font-size: 13px;
  color: var(--text-secondary);
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.sidebar-menu {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(124, 58, 237, 0.08);
  color: var(--accent-color);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
}

/* 右侧内容区 */
.profile-content {
  flex: 1;
  overflow-y: auto;
}

.content-panel {
  margin-bottom: 20px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.panel-actions {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

/* 个人资料表单 */
.profile-form { padding: 16px 0; }
.form-tip { font-size: 12px; color: var(--text-muted); margin-top: 4px; }

.avatar-colors {
  display: flex;
  gap: 12px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
  border: 2px solid transparent;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.active {
  border-color: var(--text-primary);
  transform: scale(1.1);
}

/* 隐私设置 */
.privacy-list { padding: 10px 0; }

.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
}

.privacy-left { flex: 1; }

.privacy-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.privacy-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 消息通知 */
.notification-list { padding: 10px 0; }

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
}

.notification-left { flex: 1; }

.notification-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 活动记录 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px 0;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--bg-light);
}

.history-type {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.history-type.post { background: rgba(124, 58, 237, 0.1); color: var(--accent-color); }
.history-type.comment { background: rgba(39, 174, 96, 0.1); color: var(--success-color); }
.history-type.like { background: rgba(243, 156, 18, 0.1); color: var(--warning-color); }

.history-content { flex: 1; }

.history-title {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.history-time {
  font-size: 12px;
  color: var(--text-muted);
}

/* 对话框样式调整 */
:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--border-light);
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>