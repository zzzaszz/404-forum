import {defineConfig} from "vite";
import vue from "@vitejs/plugin-vue";
import {resolve} from "path";

// Mock 数据
function generateAnonymousId() {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let id = "";
  for (let i = 0; i < 3; i++) {
    id += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return `匿名#${id}`;
}

const mockTopics = [
  {
    id: 1,
    title: "有没有人知道怎么解决这个问题？",
    author: "匿名#A3F",
    category: "求助",
    createTime: new Date().toISOString(),
    views: 234,
    comments: 18,
    heat: 12580,
  },
  {
    id: 2,
    title: "今天遇到一个很奇怪的事情",
    author: "匿名#B72",
    category: "闲聊",
    createTime: new Date(Date.now() - 3600000).toISOString(),
    views: 156,
    comments: 12,
    heat: 8960,
  },
  {
    id: 3,
    title: "分享一个很棒的开源项目",
    author: "匿名#C91",
    category: "技术",
    createTime: new Date(Date.now() - 7200000).toISOString(),
    views: 89,
    comments: 7,
    heat: 7230,
  },
  {
    id: 4,
    title: "最近玩的游戏推荐",
    author: "匿名#D45",
    category: "游戏",
    createTime: new Date(Date.now() - 10800000).toISOString(),
    views: 67,
    comments: 5,
    heat: 5420,
  },
  {
    id: 5,
    title: "周末去哪里放松一下？",
    author: "匿名#E28",
    category: "生活",
    createTime: new Date(Date.now() - 14400000).toISOString(),
    views: 45,
    comments: 3,
    heat: 3890,
  },
];

const mockOnlineUsers = [
  { id: "u1", nickname: "匿名#A1" },
  { id: "u2", nickname: "匿名#B2" },
  { id: "u3", nickname: "匿名#C3" },
  { id: "u4", nickname: "匿名#D4" },
  { id: "u5", nickname: "匿名#E5" },
  { id: "u6", nickname: "匿名#F6" },
  { id: "u7", nickname: "匿名#G7" },
  { id: "u8", nickname: "匿名#H8" },
];

const mockMessages = [
  {
    id: 1,
    from: "匿名#B72",
    content: "你好，想和你聊聊技术话题",
    time: new Date(Date.now() - 1800000).toISOString(),
    read: false,
  },
  {
    id: 2,
    from: "匿名#C91",
    content: "谢谢你的分享，很有帮助",
    time: new Date(Date.now() - 3600000).toISOString(),
    read: false,
  },
  {
    id: 3,
    from: "匿名#D45",
    content: "有空一起打游戏吗？",
    time: new Date(Date.now() - 7200000).toISOString(),
    read: true,
  },
];

// Mock 中间件插件
function mockPlugin() {
  return {
    name: "vite-plugin-mock",
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = req.url;
        const method = req.method;

        // 解析请求体
        let body = "";
        req.on("data", (chunk) => (body += chunk));
        req.on("end", () => {
          try {
            body = body ? JSON.parse(body) : {};
          } catch {
            body = {};
          }

          // 设置响应头
          res.setHeader("Content-Type", "application/json");

          // 登录
          if (url === "/api/auth/login" && method === "POST") {
            const nickname = generateAnonymousId();
            const token = `mock_token_${Date.now()}_${Math.random().toString(36).slice(2)}`;
            res.end(
              JSON.stringify({
                code: 200,
                msg: "登录成功",
                data: {
                  token,
                  userId: `user_${Date.now()}`,
                  nickname,
                  avatar: null,
                },
                nickname,
              }),
            );
            return;
          }

          // 获取用户信息
          if (url === "/api/auth/getInfo" && method === "GET") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "success",
                data: {
                  userId: "user_mock",
                  nickname: "匿名#ABC",
                  avatar: null,
                },
              }),
            );
            return;
          }

          // 登出
          if (url === "/api/auth/logout" && method === "DELETE") {
            res.end(JSON.stringify({ code: 200, msg: "退出成功", data: null }));
            return;
          }

          // 获取话题列表
          if (url.startsWith("/api/forum/topics") && method === "GET") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "success",
                data: {
                  list: mockTopics,
                  total: mockTopics.length,
                  page: 1,
                  size: 10,
                },
              }),
            );
            return;
          }

          // 获取热门话题
          if (url === "/api/forum/hot" && method === "GET") {
            const hotList = mockTopics
              .sort((a, b) => b.heat - a.heat)
              .slice(0, 5)
              .map((t) => ({ id: t.id, title: t.title, heat: t.heat }));
            res.end(
              JSON.stringify({ code: 200, msg: "success", data: hotList }),
            );
            return;
          }

          // 获取话题详情
          if (url.match(/^\/api\/forum\/topic\/\d+$/) && method === "GET") {
            const id = parseInt(url.split("/").pop());
            const topic = mockTopics.find((t) => t.id === id);
            if (topic) {
              res.end(
                JSON.stringify({ code: 200, msg: "success", data: topic }),
              );
            } else {
              res.end(
                JSON.stringify({ code: 404, msg: "话题不存在", data: null }),
              );
            }
            return;
          }

          // 发布话题
          if (url === "/api/forum/topic/create" && method === "POST") {
            const newTopic = {
              id: mockTopics.length + 1,
              title: body.title || "新话题",
              category: body.category || "闲聊",
              author: generateAnonymousId(),
              createTime: new Date().toISOString(),
              views: 0,
              comments: 0,
              heat: 100,
            };
            mockTopics.unshift(newTopic);
            res.end(
              JSON.stringify({ code: 200, msg: "发布成功", data: newTopic }),
            );
            return;
          }

          // 获取在线用户
          if (url === "/api/forum/online" && method === "GET") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "success",
                data: { count: 128, users: mockOnlineUsers },
              }),
            );
            return;
          }

          // 获取统计数据
          if (url === "/api/forum/stats" && method === "GET") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "success",
                data: {
                  onlineCount: 128,
                  activeTopics: 56,
                  totalTopics: mockTopics.length,
                  totalUsers: 1000,
                },
              }),
            );
            return;
          }

          // 获取私信列表
          if (url === "/api/forum/messages" && method === "GET") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "success",
                data: {
                  list: mockMessages,
                  unread: mockMessages.filter((m) => !m.read).length,
                },
              }),
            );
            return;
          }

          // 发送私信
          if (url === "/api/forum/message/send" && method === "POST") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "发送成功",
                data: {
                  id: Date.now(),
                  ...body,
                  time: new Date().toISOString(),
                  read: false,
                },
              }),
            );
            return;
          }

          // 发表评论
          if (url === "/api/forum/comment/create" && method === "POST") {
            res.end(
              JSON.stringify({
                code: 200,
                msg: "评论成功",
                data: {
                  id: Date.now(),
                  topicId: body.topicId,
                  content: body.content,
                  author: generateAnonymousId(),
                  createTime: new Date().toISOString(),
                  likes: 0,
                },
              }),
            );
            return;
          }

          next();
        });
      });
    },
  };
}

export default defineConfig({
  plugins: [
    vue(),
    // mockPlugin()
  ],
  resolve: {
    alias: {
      "@": resolve(__dirname, "src"),
    },
  },
  server: {
    port: 3000,
    proxy: {
      "/api": {
          target: "http://localhost:10610/",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    },
  },
});
