<template>
  <div class="login-container">
    <div class="login-bg">
      <img alt="" class="login-bg-img" src="/images/login-bg.png"/>
    </div>

    <div class="login-card">
      <div class="logo-area">
        <div class="logo-box">
          <img alt="404论坛" class="logo-img" src="/images/favicon2.png"/>
        </div>
        <h1>404论坛</h1>
        <p>匿名聊天社区</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              clearable
              maxlength="32"
              placeholder="账号"
              size="large"
          >
            <template #prefix>
              <el-icon>
                <User/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              :placeholder="isRegister ? '设置密码（6-32位）' : '密码'"
              :type="showPassword ? 'text' : 'password'"
              maxlength="32"
              size="large"
              @keydown.enter.prevent="handleSubmit"
          >
            <template #prefix>
              <el-icon>
                <Lock/>
              </el-icon>
            </template>
            <template #suffix>
              <el-icon class="pwd-toggle" @click="showPassword = !showPassword">
                <View v-if="showPassword"/>
                <Hide v-else/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="isRegister" prop="nickname">
          <el-input
              v-model="form.nickname"
              maxlength="32"
            size="large"
              placeholder="昵称（选填，留空则随机）"
            clearable
          >
            <template #prefix>
              <el-icon>
                <EditPen/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          @click="handleSubmit"
          :loading="loading"
        >
          {{ loading ? '处理中...' : (isRegister ? '注册并进入' : '登录') }}
        </el-button>
      </el-form>

      <div class="switch-mode">
        <span v-if="!isRegister">
          还没有账号？
          <el-link :underline="false" type="primary" @click="toggleMode(true)">立即注册</el-link>
        </span>
        <span v-else>
          已有账号？
          <el-link :underline="false" type="primary" @click="toggleMode(false)">返回登录</el-link>
        </span>
      </div>

      <div class="login-footer">
        404论坛 v1.0
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {login, register} from '@/api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isRegister = ref(false)
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

const rules = computed(() => ({
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'},
    {pattern: /^[A-Za-z0-9_]{3,32}$/, message: '账号为 3-32 位字母、数字或下划线', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 32, message: '密码长度应为 6-32 位', trigger: 'blur'}
  ],
  nickname: isRegister.value
      ? [{max: 32, message: '昵称最长 32 位', trigger: 'blur'}]
      : []
}))

function toggleMode(toRegister) {
  isRegister.value = toRegister
  showPassword.value = false
  formRef.value && formRef.value.clearValidate()
}

function persistLogin(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('accessToken', data.accessToken)
  localStorage.setItem('refreshToken', data.refreshToken)
  localStorage.setItem('userInfo', JSON.stringify({
    userId: data.userId,
    nickname: data.nickname,
    avatar: data.avatar,
    role: data.role ?? 0,
    themeColor: data.themeColor || '#7C3AED'
  }))
  // 应用主题颜色到全局 CSS 变量
  applyThemeColor(data.themeColor || '#7C3AED')
}

/** 将主题颜色应用到全局 CSS 变量 */
function applyThemeColor(color) {
  const root = document.documentElement
  root.style.setProperty('--accent-color', color)
  // 生成对应的深色和浅色变体
  root.style.setProperty('--accent-dark', adjustColor(color, -20))
  root.style.setProperty('--accent-light', adjustColor(color, 20))
  root.style.setProperty('--sidebar-active-bg', color)
}

/** 简单的颜色明度调整（hex 颜色 ±amount） */
function adjustColor(hex, amount) {
  let r, g, b
  if (hex.startsWith('#')) {
    const num = parseInt(hex.slice(1), 16)
    r = (num >> 16) & 0xff
    g = (num >> 8) & 0xff
    b = num & 0xff
  } else {
    return hex
  }
  r = Math.max(0, Math.min(255, r + amount))
  g = Math.max(0, Math.min(255, g + amount))
  b = Math.max(0, Math.min(255, b + amount))
  return `#${((r << 16) | (g << 8) | b).toString(16).padStart(6, '0')}`
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const payload = {
      username: form.username.trim(),
      password: form.password
    }
    if (isRegister.value && form.nickname.trim()) {
      payload.nickname = form.nickname.trim()
    }

    const res = isRegister.value
        ? await register(payload)
        : await login(payload)
    const data = res.data

    persistLogin(data)
    ElMessage.success(`${isRegister.value ? '注册成功，' : ''}欢迎，${data.nickname}`)
    router.push('/')
  } catch (error) {
    // 校验失败或接口失败（接口错误由拦截器统一提示）
    if (error && error.message) {
      console.error(isRegister.value ? '注册错误:' : '登录错误:', error)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/')
  }
})
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  background: var(--bg-color);
  overflow: hidden;
}

.login-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 优化放大缩放：减轻插值模糊 */
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
  /* 轻微锐化 + 提饱和，让放大后仍显清晰 */
  filter: contrast(1.06) saturate(1.08);
}

.login-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(15, 15, 26, 0.38);
  z-index: 1;
}

.login-card {
  width: 360px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 48px 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 8px 24px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 1;
  border: 1px solid var(--border-light);
}

.logo-area {
  text-align: center;
  margin-bottom: 36px;
}

.logo-box {
  width: 56px;
  height: 64px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  overflow: hidden;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-area h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}

.logo-area p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item__content) {
  display: flex;
}

.login-form :deep(.el-input) {
  border-radius: var(--radius-md);
  background: var(--bg-light);
  box-shadow: none;
  transition: all var(--transition-fast);
  overflow: hidden;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  padding: 8px 14px;
  background: transparent !important;
  box-shadow: none !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: none !important;
}

.login-form :deep(.el-input.is-focus) {
  background: var(--bg-white);
  box-shadow: none;
}

.login-form :deep(.el-input__inner) {
  font-size: 14px;
  color: var(--text-primary);
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: var(--text-muted);
}

.pwd-toggle {
  cursor: pointer;
  color: var(--text-muted);
}

.pwd-toggle:hover {
  color: var(--primary-light);
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  background: var(--primary-color);
  border: none;
  transition: all var(--transition-fast);
  letter-spacing: 0.02em;
}

.submit-btn:hover {
  background: var(--primary-light);
}

.switch-mode {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: var(--text-secondary);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 480px) {
  .login-card {
    width: calc(100% - 32px);
    padding: 36px 24px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .submit-btn:hover {
    transform: none;
  }
}
</style>
