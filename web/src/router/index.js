import {createRouter, createWebHistory} from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { activeMenu: '/dashboard' }
      },
      // 聊天
      {
        path: 'chat',
        name: 'ChatRoom',
        component: () => import('@/views/ChatRoom.vue')
      },
      // 私信
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue')
      },
      // AI 对话
      {
        path: 'ai',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue')
      },
      // AI 角色
      {
        path: 'ai/agents',
        name: 'AiAgentManage',
        component: () => import('@/views/AiAgentManage.vue')
      },
      // 热门话题
      {
        path: 'hot',
        name: 'HotTopics',
        component: () => import('@/views/HotTopics.vue')
      },
      // 话题分类（明确路由，避免与 /topic/:id 冲突）
      {
        path: 'topic/random',
        name: 'TopicRandom',
        component: () => import('@/views/TopicCategory.vue')
      },
      {
        path: 'topic/tech',
        name: 'TopicTech',
        component: () => import('@/views/TopicCategory.vue')
      },
      {
        path: 'topic/game',
        name: 'TopicGame',
        component: () => import('@/views/TopicCategory.vue')
      },
      {
        path: 'topic/life',
        name: 'TopicLife',
        component: () => import('@/views/TopicCategory.vue')
      },
      {
        path: 'topic/help',
        name: 'TopicHelp',
        component: () => import('@/views/TopicCategory.vue')
      },
        // 动态板块（按 code）
        {
            path: 'topic/board/:code',
            name: 'TopicBoardCode',
            component: () => import('@/views/TopicCategory.vue')
        },
      // 话题详情（数字 id）
      {
        path: 'topic/:id(\\d+)',
        name: 'TopicDetail',
        component: () => import('@/views/TopicDetail.vue')
      },
      // 搜索
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue')
      },
        // 板块管理（仅管理员）
        {
            path: 'board',
            name: 'BoardManage',
            component: () => import('@/views/BoardManage.vue'),
            meta: {requiresAdmin: true, activeMenu: '/board'}
        },
        // 用户管理（仅管理员）
        {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/UserManage.vue'),
            meta: {requiresAdmin: true, activeMenu: '/user'}
        },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      // 我的内容管理（发帖/评论/获赞/消息）
      {
        path: 'my',
        name: 'MyContent',
        component: () => import('@/views/MyContent.vue')
      },
    ]
  },
  // 404 处理
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 登录拦截 + 管理员权限
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)

  if (requiresAuth && !token) {
    // 需要登录但没有 token，跳转登录页
    next('/login')
      return
  }
    if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转首页
    next('/')
        return
    }
    if (requiresAdmin) {
        // 需要管理员权限
        let role = 0
        try {
            role = JSON.parse(localStorage.getItem('userInfo') || '{}').role || 0
        } catch {
        }
        if (role !== 1) {
            next('/')
            return
        }
    }
    next()
})

export default router
