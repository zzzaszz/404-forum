import axios from 'axios'
import request from '@/utils/request'

const isProd = import.meta.env.PROD
const downloadClient = axios.create({
  baseURL: isProd ? '' : '/api',
  timeout: 120000,
  responseType: 'blob'
})

downloadClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

// ============ 通用 CRUD（管理后台） ============
export function getEntity(url, id) {
  return request({url: `${url}/getEntity`, method: 'get', params: {key: id}})
}

export function getBillEntity(url, id) {
  return request({url: `${url}/getBillEntity`, method: 'get', params: {key: id}})
}

export function getPageList(url, data) {
  return request({url: `${url}/getPageList`, method: 'post', data: {...data}})
}

export function getPageInfo(url, data) {
  return request({url: `${url}/getPageInfo`, method: 'post', data: {...data}})
}

export function createEntity(url, data) {
  return request({url: `${url}/create`, method: 'post', data: {...data}})
}

export function updateEntity(url, data) {
  return request({url: `${url}/update`, method: 'post', data: {...data}})
}

export function deleteEntity(url, id) {
  return request({url: `${url}/delete`, method: 'get', params: {key: id}})
}

export function postJson(url, data) {
  return request({url, method: 'post', data})
}

export function getByParentId(url, parentId, paramName = 'productId') {
  return request({
    url: `${url}/getBy${paramName.charAt(0).toUpperCase() + paramName.slice(1)}`,
    method: 'get',
    params: { [paramName]: parentId }
  })
}

export function downloadPost(url, data) {
  return downloadClient({url, method: 'post', data})
}

// ============ 鉴权 /api/auth ============
export function login(data) {
  return request({url: '/api/auth/login', method: 'post', data})
}

export function register(data) {
  return request({url: '/api/auth/register', method: 'post', data})
}

export function logout(accessToken, refreshToken) {
  return request({url: '/api/auth/logout', method: 'post', data: {accessToken, refreshToken}})
}

export function getUserInfo() {
  return request({url: '/api/auth/getInfo', method: 'get'})
}

export function updatePassword(data) {
  return request({url: '/api/auth/updatePassword', method: 'post', data})
}

/**
 * 更新当前用户个人资料（昵称、主题颜色）
 */
export function updateProfile(data) {
  return request({url: '/api/auth/updateProfile', method: 'post', data})
}

// ============ 板块管理（通用 CRUD） ============
const BOARD_URL = '/board'

export function getBoardPageList(params) {
  return getPageList(BOARD_URL, params)
}

export function createBoard(data) {
  return createEntity(BOARD_URL, data)
}

export function updateBoard(data) {
  return updateEntity(BOARD_URL, data)
}

export function deleteBoard(id) {
  return deleteEntity(BOARD_URL, id)
}

// ============ 用户管理（管理员，/user） ============
const USER_URL = '/user'

export function getUserPageList(params) {
  return request({
    url: `${USER_URL}/getPageList`,
    method: 'post',
    data: {...params}
  })
}

export function createUser(data) {
  return request({url: `${USER_URL}/create`, method: 'post', data})
}

export function deleteUser(id) {
  return request({url: `${USER_URL}/delete`, method: 'get', params: {key: id}})
}

export function resetUserPassword(data) {
  return request({url: `${USER_URL}/resetPassword`, method: 'post', data})
}

export function updateUserStatus(data) {
  return request({url: `${USER_URL}/updateStatus`, method: 'post', data})
}

export function updateUser(data) {
  return request({url: `${USER_URL}/update`, method: 'post', data})
}

// ============ Dashboard 统计（管理员） ============
export function getAdminStats() {
  return request({url: '/forum/stats', method: 'get'})
}

// ============ 论坛业务 /forum（对接 ForumController） ============

/** 话题列表（最新/按板块/搜索） */
export function getTopics(params = {}) {
  return request({url: '/forum/topics', method: 'get', params})
}

/** 热门话题榜 */
export function getHotTopics(limit = 10) {
  return request({url: '/forum/hot', method: 'get', params: {limit}})
}

/** 话题详情 */
export function getTopicDetail(id) {
  return request({url: `/forum/topic/${id}`, method: 'get'})
}

/** 发布话题 */
export function createTopic(data) {
  return request({url: '/forum/topic/create', method: 'post', data})
}

/** 帖子评论列表 */
export function getTopicComments(id, params = {}) {
  return request({url: `/forum/topic/${id}/comments`, method: 'get', params})
}

/** 发表评论 */
export function createComment(data) {
  return request({url: '/forum/comment/create', method: 'post', data})
}

/** 点赞切换（targetType:1帖子 2评论） */
export function toggleLike(data) {
  return request({url: '/forum/like', method: 'post', data})
}

/** 收藏切换 */
export function toggleFavorite(data) {
  return request({url: '/forum/favorite', method: 'post', data})
}

/** 举报 */
export function reportTarget(data) {
  return request({url: '/forum/report', method: 'post', data})
}

/** 启用板块列表 */
export function getBoards() {
  return request({url: '/forum/boards', method: 'get'})
}

/** 统计 */
export function getForumStats() {
  return request({url: '/forum/stats', method: 'get'})
}

/** 在线用户 */
export function getOnlineUsers() {
  return request({url: '/forum/online', method: 'get'})
}

/** 我的私信列表 */
export function getMessages() {
  return request({url: '/forum/messages', method: 'get'})
}

/** 发送私信 */
export function sendMessage(data) {
  return request({url: '/forum/message/send', method: 'post', data})
}

/** 搜索话题 */
export function searchTopics(params = {}) {
  return request({url: '/forum/search', method: 'get', params})
}

// ============ 字段映射工具：后端 Pojo → 前端展示对象 ============

/** 匿名头像图片池（web/public/images/user1~8.png） */
const AVATAR_POOL = [
  '/images/user1.png', '/images/user2.png', '/images/user3.png', '/images/user4.png',
  '/images/user5.png', '/images/user6.png', '/images/user7.png', '/images/user8.png'
]

/**
 * 按 id 稳定映射到头像池（同一对象每次返回同一张）。
 * @param {number|string} id
 * @returns {string} 头像图片 URL
 */
export function pickAvatar(id) {
  if (id == null) return AVATAR_POOL[0]
  const num = typeof id === 'number' ? id : parseInt(String(id).replace(/[^0-9]/g, ''), 10)
  const idx = (Number.isNaN(num) ? 0 : num) % AVATAR_POOL.length
  return AVATAR_POOL[idx]
}

/** 判断是否为 emoji 字符（老数据兼容，emoji 不作文头像 URL 使用） */
function isEmoji(str) {
  if (!str || str.startsWith('/')) return false
  // emoji 通常为 1-4 字节的 Unicode 字符，不以 / 或 http 开头
  return !str.startsWith('http') && str.length <= 4
}

/** 将帖子 Pojo 映射为前端话题卡片对象 */
export function mapTopic(p) {
  if (!p) return null
  return {
    id: p.id,
    boardId: p.boardid,
    boardCode: p.boardcode || '',
    boardName: p.boardname || '',
    title: p.title || '',
    content: p.content || '',
    author: p.anonymousname || '用户',
    // 头像优先使用后端路径（新数据已按用户设置）；emoji 老数据或缺失时按用户 ID 回退
    avatar: (p.anonymousavatar && !isEmoji(p.anonymousavatar))
        ? p.anonymousavatar
        : pickAvatar(p.userid || p.id),
    // 颜色优先后端；缺失时按用户 ID 取色
    color: p.anonymouscolor || pickColor(p.userid || p.id),
    views: p.viewcount || 0,
    comments: p.replycount || 0,
    likes: p.likecount || 0,
    favorites: p.favoritecount || 0,
    heat: p.hotscore || 0,
    isTop: p.istop || 0,
    isEssence: p.isessence || 0,
    createTime: p.createdat,
    // 兼容字段
    category: p.boardname || p.boardcode || '',
  }
}

/** 根据用户 ID 生成稳定的颜色（hash 取色） */
function pickColor(id) {
  if (id == null) return '#7C3AED'
  const COLOR_POOL = [
    '#7C3AED', '#3B82F6', '#16A34A', '#EA580C', '#F59E0B', '#EF4444',
    '#0EA5E9', '#8B5CF6', '#EC4899', '#14B8A6'
  ]
  const num = typeof id === 'number' ? id : parseInt(String(id).replace(/[^0-9]/g, ''), 10)
  const idx = (Number.isNaN(num) ? 0 : Math.abs(num)) % COLOR_POOL.length
  return COLOR_POOL[idx]
}

/** 将评论 Pojo 映射为前端评论对象 */
export function mapComment(c) {
  if (!c) return null
  return {
    id: c.id,
    postId: c.postid,
    userId: c.userid,
    author: c.anonymousname || '用户',
    // 头像按评论用户 ID 稳定映射，评论人固定头像；老评论（无 userid）回退按评论 ID
    avatar: pickAvatar(c.userid || c.id),
    // 颜色按用户 ID 取色，颜色跟着用户走
    color: pickColor(c.userid || c.id),
    content: c.content || '',
    likes: c.likecount || 0,
    replies: c.replycount || 0,
    createTime: c.createdat,
  }
}
