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
          </div>
          <div class="follow-stats">
            <div class="follow-stat-item" @click="openFollowDialog('following')">
              <span class="follow-stat-value">{{ followLoading ? '-' : following.length }}</span>
              <span class="follow-stat-label">关注的人</span>
            </div>
            <div class="follow-stat-item" @click="openFollowDialog('followers')">
              <span class="follow-stat-value">{{ followLoading ? '-' : followers.length }}</span>
              <span class="follow-stat-label">关注我的人</span>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item clickable" @click="goMy('posts')">
              <span class="stat-value">{{ statsLoading ? '-' : userStats.posts }}</span>
              <span class="stat-label">发布</span>
            </div>
            <div class="stat-item clickable" @click="goMy('comments')">
              <span class="stat-value">{{ statsLoading ? '-' : userStats.comments }}</span>
              <span class="stat-label">评论</span>
            </div>
            <div class="stat-item clickable" @click="goMy('likes')">
              <span class="stat-value">{{ statsLoading ? '-' : userStats.likes }}</span>
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
              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" placeholder="你的昵称" maxlength="20" />
              </el-form-item>
<!--              <el-form-item label="匿名ID">-->
<!--                <el-input v-model="userInfo.anonymousId" disabled />-->
<!--                <div class="form-tip">匿名ID由系统自动生成，无法修改</div>-->
<!--              </el-form-item>-->
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
              <el-form-item label="主题颜色">
                <div class="theme-color-picker">
                  <el-color-picker v-model="profileForm.themeColor" @change="onThemeColorChange" />
                  <span class="theme-color-value">{{ profileForm.themeColor }}</span>
                  <el-button size="small" text type="primary" @click="resetThemeColor">恢复默认</el-button>
                </div>
                <div class="form-tip">自定义主题颜色将应用于头像、侧边栏等界面元素</div>
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

    <!-- 关注 / 粉丝列表弹窗 -->
    <el-dialog
      v-model="followDialogVisible"
      :title="followDialogType === 'following' ? '关注的人' : '关注我的人'"
      width="420px"
      append-to-body
    >
      <div class="follow-dialog-list">
        <div v-for="u in followDialogList" :key="u.userId" class="follow-dialog-item">
          <span class="fd-avatar" :style="{ background: colorOf(u.userId) }">{{ (u.nickname || '?').charAt(0) }}</span>
          <span class="fd-name">{{ u.nickname }}</span>
          <el-button
            size="small"
            :type="followBtnType(u)"
            :disabled="isSelf(u)"
            @click="onFollowBtn(u)"
            @mouseenter="hoverFollowId = u.userId"
            @mouseleave="hoverFollowId = null"
          >{{ followBtnText(u) }}</el-button>
          <el-button size="small" @click="goPM(u)">私信</el-button>
        </div>
        <el-empty
          v-if="!followDialogList.length"
          :description="followDialogType === 'following' ? '暂未关注任何人' : '暂无粉丝'"
          :image-size="60"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { updateProfile, getFollowing, getFollowers } from '@/api'

const router = useRouter()
const activeTab = ref('profile')

// 关注 / 粉丝
const following = ref([])
const followers = ref([])
const followLoading = ref(true)
const followDialogVisible = ref(false)
const followDialogType = ref('following') // following | followers
const followDialogList = computed(() => followDialogType.value === 'following' ? following.value : followers.value)

const FOLLOW_COLORS = ['#7C3AED','#3B82F6','#16A34A','#EA580C','#F59E0B','#EF4444','#0EA5E9','#8B5CF6','#EC4899','#14B8A6']
function colorOf(userId) {
  if (!userId) return FOLLOW_COLORS[0]
  const s = String(userId)
  let hash = 0
  for (let i = 0; i < s.length; i++) hash = s.charCodeAt(i) + ((hash << 5) - hash)
  return FOLLOW_COLORS[Math.abs(hash) % FOLLOW_COLORS.length]
}

function openFollowDialog(type) {
  followDialogType.value = type
  followDialogVisible.value = true
}

function goPM(u) {
  followDialogVisible.value = false
  router.push({ path: '/messages', query: { to: u.userId, name: u.nickname } })
}

async function loadFollowData() {
  try {
    const [fRes, frRes] = await Promise.all([getFollowing(), getFollowers()])
    following.value = (fRes.data || []).map(u => ({ userId: String(u.userId), nickname: u.nickname || '用户' }))
    followers.value = (frRes.data || []).map(u => ({ userId: String(u.userId), nickname: u.nickname || '用户' }))
  } catch { /* 忽略 */ }
  followLoading.value = false
}

// 当前登录用户 ID
const currentUserId = computed(() => {
  try { return String(JSON.parse(localStorage.getItem('userInfo') || '{}').userId || '') }
  catch { return '' }
})
const hoverFollowId = ref(null)

function isSelf(u) { return u && String(u.userId) === currentUserId.value }
function isFollowing(u) { return u && following.value.some(f => String(f.userId) === String(u.userId)) }
function isMutual(u) { return isFollowing(u) && followers.value.some(f => String(f.userId) === String(u.userId)) }

function followBtnText(u) {
  if (isSelf(u)) return '我'
  if (hoverFollowId.value === u.userId && isFollowing(u)) return '取消关注'
  if (isMutual(u)) return '互关'
  if (isFollowing(u)) return '已关注'
  return '关注'
}
function followBtnType(u) {
  // 未关注用主色突出，已关注/互关用默认
  return (!isSelf(u) && !isFollowing(u)) ? 'primary' : ''
}

async function onFollowBtn(u) {
  if (isSelf(u)) return
  if (isFollowing(u)) {
    // 取关
    following.value = following.value.filter(f => String(f.userId) !== String(u.userId))
    try {
      await fetch(`/api/chat/follow/${u.userId}`, { method: 'DELETE', headers: { Authorization: localStorage.getItem('token') || '' } })
    } catch {}
  } else {
    // 关注
    following.value = [...following.value, { userId: String(u.userId), nickname: u.nickname }]
    ElMessage.success('已关注 ' + u.nickname)
    try {
      await fetch('/api/chat/follow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: localStorage.getItem('token') || '' },
        body: JSON.stringify({ userId: String(u.userId), nickname: u.nickname })
      })
    } catch {}
  }
  // 关注/取关会改变互关状态，刷新粉丝列表（关注我的人）
  loadFollowData()
}

function goMy(tab) {
  router.push({ path: '/my', query: { tab } })
}

// 用户信息
const userInfo = ref({
  nickname: '匿名用户',
  anonymousId: '匿名#A3F',
  loginTime: ''
})

// 用户统计数据
const userStats = ref({
  posts: 0,
  comments: 0,
  likes: 0
})
const statsLoading = ref(true)

// 个人资料表单
const profileForm = ref({
  nickname: '',
  signature: '',
  avatarColor: 'purple',
  themeColor: '#7C3AED'
})

const DEFAULT_THEME_COLOR = '#7C3AED'

// 头像颜色预设
const avatarColors = [
  { value: 'purple', gradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { value: 'blue', gradient: 'linear-gradient(135deg, #3498db, #2980b9)' },
  { value: 'green', gradient: 'linear-gradient(135deg, #27ae60, #2ecc71)' },
  { value: 'orange', gradient: 'linear-gradient(135deg, #f39c12, #e74c3c)' },
  { value: 'pink', gradient: 'linear-gradient(135deg, #f64f59, #c471ed)' },
  { value: 'teal', gradient: 'linear-gradient(135deg, #1abc9c, #16a085)' },
  { value: 'indigo', gradient: 'linear-gradient(135deg, #6366f1, #4f46e5)' }
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
    profileForm.value.signature = storedInfo.signature || ''
    profileForm.value.avatarColor = storedInfo.avatarColor || 'purple'
    profileForm.value.themeColor = storedInfo.themeColor || DEFAULT_THEME_COLOR
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 菜单选择
const handleMenuSelect = (index) => {
  activeTab.value = index
}

/** 主题颜色变化时实时预览 */
const onThemeColorChange = (color) => {
  if (color) {
    applyThemeColor(color)
  }
}

/** 恢复默认主题颜色 */
const resetThemeColor = () => {
  profileForm.value.themeColor = DEFAULT_THEME_COLOR
  applyThemeColor(DEFAULT_THEME_COLOR)
}

/** 将主题颜色应用到全局 CSS 变量 */
const applyThemeColor = (color) => {
  const root = document.documentElement
  root.style.setProperty('--accent-color', color)
  root.style.setProperty('--accent-dark', adjustColor(color, -20))
  root.style.setProperty('--accent-light', adjustColor(color, 20))
  root.style.setProperty('--sidebar-active-bg', color)
}

/** 简单的颜色明度调整（hex 颜色 ±amount） */
const adjustColor = (hex, amount) => {
  if (!hex || !hex.startsWith('#')) return hex
  let r, g, b
  const num = parseInt(hex.slice(1), 16)
  r = (num >> 16) & 0xff
  g = (num >> 8) & 0xff
  b = num & 0xff
  r = Math.max(0, Math.min(255, r + amount))
  g = Math.max(0, Math.min(255, g + amount))
  b = Math.max(0, Math.min(255, b + amount))
  return `#${((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')}`
}

// 保存个人资料
const saveProfile = async () => {
  if (!profileForm.value.nickname || profileForm.value.nickname.length < 2) {
    ElMessage.warning('昵称至少需要2个字符')
    return
  }

  // 更新本地存储
  const storedInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  storedInfo.nickname = profileForm.value.nickname
  storedInfo.signature = profileForm.value.signature
  storedInfo.avatarColor = profileForm.value.avatarColor
  storedInfo.themeColor = profileForm.value.themeColor
  localStorage.setItem('userInfo', JSON.stringify(storedInfo))

  // 应用主题颜色
  applyThemeColor(profileForm.value.themeColor)

  // 持久化到后端
  try {
    await updateProfile({
      nickname: profileForm.value.nickname,
      themeColor: profileForm.value.themeColor
    })
  } catch {
    // 后端保存失败仅警告，不影响本地使用
    ElMessage.warning('部分设置未能同步到服务器，已保存在本地')
  }

  userInfo.value.nickname = profileForm.value.nickname
  ElMessage.success('资料已更新')
}

// 格式化时间
const formatTime = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 加载用户动态统计
async function loadUserStats() {
  try {
    const { getMyStats } = await import('@/api')
    const res = await getMyStats()
    if (res.data) {
      userStats.value.posts = res.data.posts || 0
      userStats.value.comments = res.data.comments || 0
      userStats.value.likes = res.data.likes || 0
    }
  } catch { /* 忽略 */ }
  statsLoading.value = false
}

// 加载活动记录
async function loadActivityHistory() {
  try {
    const { getTopics } = await import('@/api')
    const res = await getTopics({sort: 'latest', pageNum: 1, pageSize: 10})
    const list = (res.data?.list || []).slice(0, 5)
    activityHistory.value = list.map(item => ({
      id: item.id,
      type: 'post',
      title: item.title || '发布了新话题',
      time: item.createdat
    }))
  } catch {
    // 保持默认空列表
  }
}

onMounted(() => {
  loadUserInfo()
  loadUserStats()
  loadActivityHistory()
  loadFollowData()
  // 初始化时应用已保存的主题颜色
  const storedInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (storedInfo.themeColor) {
    applyThemeColor(storedInfo.themeColor)
  }
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

.stat-item.clickable {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.15s;
}

.stat-item.clickable:hover {
  background: var(--bg-light);
}

/* 关注 / 粉丝统计区 */
.follow-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.follow-stat-item {
  text-align: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.15s;
}

.follow-stat-item:hover {
  background: var(--bg-light);
}

.follow-stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: var(--accent-color);
}

.follow-stat-label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 关注 / 粉丝弹窗列表 */
.follow-dialog-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 360px;
  overflow-y: auto;
}

.follow-dialog-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-radius: 8px;
}

.follow-dialog-item:hover {
  background: var(--bg-light);
}

.fd-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.fd-name {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

/* 头像颜色预设 */
.avatar-colors {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

/* 主题颜色选择器 */
.theme-color-picker {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-color-value {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: monospace;
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