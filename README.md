# 企业级 AI 权限管理 Agent 系统

基于 **Spring Boot 3.3 + Spring AI + React 18 + Ant Design 5** 构建的企业级智能权限管理平台。

## 核心能力

- **标准 RBAC 权限体系**：租户、组织、用户、角色、菜单、接口、数据七级细粒度权限
- **AI 权限 Agent**：自然语言权限查询、配置推荐、风险分析（支持 Mock / OpenAI 双模式）
- **权限风险治理**：规则引擎 + AI 巡检，自动识别权限溢出、闲置权限
- **全链路审计**：操作日志、风险工单、权限模板管理
- **现代化前端**：React + Ant Design 中后台，ECharts 可视化大屏

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3, Spring Security, JWT, MyBatis-Plus, Spring AI, Redis |
| 前端 | React 18, TypeScript, Ant Design 5, Zustand, ECharts, Vite |
| 数据库 | MySQL 8.0, Redis 7.2 |
| 部署 | Docker Compose |

## 快速启动

### 方式一：Docker Compose（推荐）

```bash
docker compose up -d --build
```

访问 http://localhost ，默认账号 `admin` / `admin123`

### 方式二：本地开发

**1. 启动基础设施**

```bash
docker compose up -d mysql redis
mysql -h127.0.0.1 -uroot -proot123 < sql/init.sql
```

**2. 启动后端**

```bash
cd backend
mvn spring-boot:run
```

**3. 启动前端**

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

## 配置说明

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `AI_MOCK_ENABLED` | 是否启用 AI Mock 模式（无需 API Key） | `true` |
| `AI_OPENAI_KEY` | OpenAI API Key | - |
| `AI_OPENAI_BASE_URL` | OpenAI 兼容 API 地址 | `https://api.openai.com` |
| `JWT_SECRET` | JWT 签名密钥 | 内置默认值 |
| `DB_*` | 数据库连接配置 | 见 docker-compose |

启用真实 AI：设置 `AI_MOCK_ENABLED=false` 并配置 `AI_OPENAI_KEY`。

## 项目结构

```
permission-agent/
├── backend/          # Spring Boot 后端
├── frontend/         # React 前端
├── sql/              # 数据库初始化脚本
├── docker-compose.yml
└── README.md
```

## API 概览

| 模块 | 路径前缀 |
|------|---------|
| 认证 | `/api/auth` |
| 用户/角色/菜单/部门 | `/api/users`, `/api/roles`, ... |
| AI 对话 | `/api/ai/chat/stream` (SSE) |
| 风险治理 | `/api/risks` |
| 仪表盘 | `/api/dashboard/stats` |

## 默认账号

- 用户名：`admin`
- 密码：`admin123`
