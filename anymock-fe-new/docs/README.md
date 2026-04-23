# JztMock 接口Mock平台 - 前端重构文档

> 本文档详细说明 Vue 3 + Vite 重构后的 AnyMock 前端平台。
> 重构项目路径：`/Users/Zhuanz/IdeaProjects/AnyMock/anymock-fe-new/`

---

## 一、项目概述

### 1.1 项目简介

JztMock 是一个接口 Mock 平台前端，用于可视化管理 HTTP Mock 接口，支持静态文本、Groovy 脚本、Groovy 预设分支三种配置模式。通过 AnyMock，前端开发人员可以在后端接口未就绪时，快速模拟接口响应，加速开发联调。

### 1.2 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API + `<script setup>`) | ^3.4.21 |
| 构建工具 | Vite | ^5.1.6 |
| UI 组件库 | Element Plus | ^2.6.1 |
| 状态管理 | Pinia | ^2.1.7 |
| 路由 | Vue Router | ^4.3.0 |
| HTTP 客户端 | Axios | ^1.6.7 |
| 代码编辑器 | Monaco Editor (@guolao/vue-monaco-editor) | ^1.5.4 |

**对比原项目（Vue 2 + Webpack 4）：**
- 移除了混用的 Ant Design Vue，统一使用 Element Plus
- 移除了 node-sass（ARM64 macOS 编译问题）
- 移除了旧版 ace-editor，改用 Monaco Editor

### 1.3 目录结构

```
anymock-fe-new/
├── src/
│   ├── api/
│   │   └── anymock.js          # API 接口封装层
│   ├── components/
│   │   ├── ApiTester.vue       # API 调试器组件
│   │   ├── InterfaceListPanel.vue  # 接口列表面板组件
│   │   └── SpaceTreeDropdown.vue   # 空间树下拉悬浮组件
│   ├── layout/
│   │   └── BasicLayout.vue     # 基础布局（侧边栏 + 顶部导航）
│   ├── stores/
│   │   ├── theme.js            # 主题状态管理
│   │   ├── space.js            # 空间状态管理
│   │   └── interface.js        # 接口状态管理
│   ├── utils/
│   │   └── request.js          # Axios 实例封装（拦截器）
│   ├── views/
│   │   ├── home/index.vue      # 首页
│   │   ├── interface/
│   │   │   ├── list.vue        # 接口列表页
│   │   │   ├── my-list.vue     # 我的接口页（含 API Tester）
│   │   │   └── edit.vue        # 创建/编辑接口页
│   │   ├── space/edit.vue      # 空间管理页
│   │   ├── doc/index.vue       # 文档页
│   │   └── error/404.vue       # 404 错误页
│   ├── router/index.js         # 路由配置
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── public/
│   ├── favicon.png
│   └── jztlogo.png             # Logo
├── index.html
├── vite.config.js
├── package.json
├── Dockerfile
└── nginx.conf
```

---

## 二、页面功能详解

### 2.1 首页 (`/home`)

入口页面，展示平台核心能力。

**功能：**
- Hero Banner：标题"JztMock 接口Mock平台"，含"创建接口"和"浏览接口"快捷入口
- 统计数据：接口类型数（3）、响应模式数（2）、无限可能
- 核心能力卡片：快速Mock、Groovy脚本、分支路由、延迟响应、API调试、空间管理
- 三种配置模式说明：静态文本（configMode="0"）、Groovy脚本（configMode="1"）、Groovy预设分支（configMode="2"）

**样式：** 渐变 Hero 区 + 卡片式功能展示，支持明/暗主题适配

---

### 2.2 接口列表页 (`/interface/list`)

管理和浏览所有 Mock 接口。

**功能：**
- **空间树下拉筛选**：Hover 触发，position:fixed 悬浮面板，节点悬浮时预览该空间下的接口列表（最多10条），点击"◎ 全部接口"清空过滤
- **关键词搜索**：按接口名称或 URL 实时过滤（400ms 防抖）
- **分页**：每页 10/20 条（后端最大限制 20）
- **表格列**：接口名称、请求路径（方法标签）、所属空间、配置模式
- **操作**：编辑、复制（复制完整 Mock URL）、删除

**API 调用：**
- 全部接口：`selectAll({ page, itemsPerPage })`
- 空间过滤：`selectBySpaceId({ criteria: { spaceId }, page, itemsPerPage })`
- 删除：`deleteInterface({ id })`
- 空间树：`getSpaceTree()`
- Core URL 前缀：`getCoreHostInfo()`

---

### 2.3 我的接口页 (`/my_interface/list`)

双 Tab 结构：左侧"接口列表"（复用了 `InterfaceListPanel` 组件），右侧"API 调试器"。

**InterfaceListPanel 组件：** 与接口列表页共用同一套 `spaceMap` + `loadData` 逻辑，支持空间筛选、关键词搜索、分页、操作按钮（编辑/复制/删除）。

---

### 2.4 API 调试器 (`ApiTester.vue`)

内置 HTTP 请求调试工具，无需使用 Postman。

**功能：**
- **请求区**：Method 下拉（GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS）、URL 输入框、发送按钮
- **Token 输入框**：独立显示，发送时自动附加 `Authorization` Header
- **Headers 编辑**：Key-Value 对列表，支持动态添加/删除
- **Body 编辑**：JSON 格式文本框，有内容时自动附加 `Content-Type: application/json`
- **响应区**：状态码标签（2xx绿色/3xx蓝色/4xx橙色/5xx红色）、耗时、响应时间、Body 和 Headers 两个 Tab
- **调用记录**：localStorage 持久化（最多10条），点击导入完整表单和响应，支持单条删除和清空
- **表单持久化**：localStorage 缓存上次输入（刷新页面不丢失）

**直调 Axios：** 组件内直接 import axios，不走 `request.js` 拦截器，直接请求 Core 服务（`http://localhost:8330` + url）。

---

### 2.5 创建/编辑接口页 (`/interface/create/:id` & `/interface/update/:id`)

最复杂的页面，通过 `route.name === 'InterfaceCreate'` 判断创建/编辑模式。

**表单区块（el-collapse）：**

#### 区块1：基础设置
- 接口名称（必填）
- 请求方法（GET/POST）
- 所属空间（el-cascader，支持任意层级选择，`checkStrictly: true`，`label: 'label'`）
- 接口URL（必填，异步校验冲突，以`/`开头）
- 配置模式：静态文本 / Groovy脚本 / Groovy预设分支
- 异步请求开关（开启后显示异步区块）
- 是否启用开关
- 分支脚本（仅分支模式显示，Monaco Editor）

#### 区块2：同步响应
- 同步延时（ms）
- 响应头表格（可编辑行，保存/取消/删除操作）
- **静态文本模式**：响应内容 textarea
- **Groovy脚本模式**：同步脚本 Monaco Editor

#### 区块3：异步响应（仅 needAsyncCallback=true 时显示）
- 异步请求方法、异步延时、异步URL（必填，带 URL 格式校验）
- 异步请求头表格（可编辑）
- 回调响应体或异步脚本（根据 configMode）

#### 区块4：分支列表（仅 Groovy预设分支 模式显示）
- 动态列：开启异步时 3 列（Case名称/同步脚本/异步脚本），否则 2 列
- 每行一个 Monaco Editor
- 可编辑行操作

**校验规则：**
- 接口名称、请求方法、配置模式必填
- 接口 URL：必填，以`/`开头，异步校验冲突
- 空间：自定义 validator 检查 `form.spacePath.length`
- 异步 URL：必填，URL 格式校验

**提交 Payload 映射：**
```js
{
  name, requestUri, requestMethod, configMode,
  needAsyncCallback, start,
  description: form.description || '',
  syncDelay: form.syncDelay || 0,
  asyncDelay: form.asyncDelay || 0,
  responseBody, syncScript, asyncScript,
  branchJumpScript,
  callbackRequestUrl, callbackRequestMethod, callbackRequestBody,
  responseHeaderList: syncHeaders.filter(h => h.name).map(({editable, _cache, ...rest}) => rest),
  callbackRequestHeaderList: asyncHeaders.filter(...).map(...),
  branchScriptList: branchList.filter(...).map(...),
  spaceId: form.spacePath.length > 0 ? form.spacePath[form.spacePath.length - 1] : ''
}
```

---

### 2.6 空间管理页 (`/space/edit`)

左侧树形结构，右侧详情面板。

**功能：**
- 左侧 el-tree：显示空间层级关系，hover 显示编辑/删除图标按钮
- 右侧详情：选中空间后显示名称、描述、访问权限（公开/私有），可编辑保存
- 新增/编辑弹窗：空间名称、上级空间（el-cascader）、访问权限、允许创建接口开关、描述

**API 接口字段名：**
- `insert/update` 接口字段名为 `label`（不是 `name`）
- 删除：`{ id }`

**已知行为：**
- 后端无 `/space/selectById` 接口，点击树节点直接用树节点 data 填充详情

---

## 三、核心组件

### 3.1 SpaceTreeDropdown.vue（空间树下拉悬浮组件）

**解决的问题：** 原计划使用 `el-dropdown`，但其 popper 面板用 JS 内联样式定位，无法被 CSS `!important` 覆盖，导致覆盖左侧菜单栏。

**实现方案：**
- 触发：按钮 `@mouseenter` → 50ms debounce → 显示面板
- 隐藏：按钮 `@mouseleave` → 150ms debounce → 关闭；面板内 `@mouseleave` → 关闭
- 面板 hover 时 `clearTimeout(hideTimeout)` 防止提前关闭
- 定位：`positionPanel()` 用 `btn.getBoundingClientRect()` 计算，强制 `position: fixed`，`z-index: 9999`
- 点击外部：`document.addEventListener('click', handleClickOutside)` 关闭
- 窗口 resize/scroll：重新计算位置

**交互：**
- "◎ 全部接口" 选项清空过滤
- 节点 hover 时加载该空间的前10条接口预览（`selectBySpaceId`），显示在面板底部
- 点击节点若有 `allowCreateInterface` 则选中并关闭面板

### 3.2 InterfaceListPanel.vue（接口列表面板）

- 被 `my-list.vue` 直接引用
- 与 `list.vue` 共用同一套 `spaceMap` + `loadData` 逻辑
- spaceName 映射：API 返回 `spaceId` 为 number，spaceMap key 统一转 String

### 3.3 ApiTester.vue（API 调试器）

- 默认 POST 请求
- Token 独立输入框，自动附加 `Authorization` Header
- Body 有内容时自动加 `Content-Type: application/json`
- localStorage 缓存表单数据 + 调用历史（最多10条）

---

## 四、API 层

### 4.1 请求封装 (`utils/request.js`)

- Axios 实例，baseURL: `/anymockweb_api/v2`，timeout: 30000
- 请求拦截器（透传）
- 响应拦截器：识别 `resultCode: '000000'` 为成功，否则 reject

### 4.2 接口列表 (`api/anymock.js`)

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `insertInterface` | POST | `/interface_http/insert` | 创建接口 |
| `updateInterface` | POST | `/interface_http/update` | 更新接口 |
| `selectById` | POST | `/interface_http/selectById` | 查询详情 |
| `selectBySpaceId` | POST | `/interface_http/selectBySpaceId` | 按空间分页查询 |
| `selectAll` | POST | `/interface_http/selectAll` | 全部分页查询 |
| `deleteInterface` | POST | `/interface_http/delete` | 删除接口 |
| `conflictDetection` | POST | `/interface_http/conflictDetection` | URL 冲突检测 |
| `addSpace` | POST | `/space/insert` | 新增空间 |
| `updateSpace` | POST | `/space/update` | 更新空间 |
| `deleteSpace` | POST | `/space/delete` | 删除空间 |
| `getSpaceTree` | POST | `/space/tree` | 获取空间树 |
| `getCoreHostInfo` | POST | `/host_info/core` | 获取 Core URL 前缀 |

---

## 五、主题系统

### 5.1 实现方式

- `stores/theme.js` 使用 Pinia 管理当前主题
- 主题列表：`['light', 'dark', 'purple', 'green']`
- 切换时执行：`document.documentElement.setAttribute('data-theme', val)` + CSS 变量覆盖 `--el-color-primary`
- localStorage 持久化（key: `anymock-theme`）

### 5.2 各主题主色

| 主题 | 主色 | 名称 |
|------|------|------|
| light | `#409eff` | 明亮 |
| dark | `#409eff` | 暗黑 |
| purple | `#8957e5` | 紫色 |
| green | `#67c23a` | 绿色 |

### 5.3 主题适配

各页面通过 `.dark .class` 选择器覆盖背景色、边框色、文字色：
```css
.dark .page-header h2 { color: #e0e0e0; }
.dark .filter-bar { background: #1a1a2e; }
.dark .table-container { background: #1a1a2e; }
```

---

## 六、路由配置

| 路径 | 名称 | 组件 | 说明 |
|------|------|------|------|
| `/` | - | redirect `/home` | 重定向 |
| `/home` | Home | home/index.vue | 首页 |
| `/interface/list` | InterfaceList | interface/list.vue | 接口列表 |
| `/my_interface/list` | MyInterface | interface/my-list.vue | 我的接口 |
| `/interface/create/:id` | InterfaceCreate | interface/edit.vue | 创建接口 |
| `/interface/update/:id` | InterfaceUpdate | interface/edit.vue | 编辑接口 |
| `/space/edit` | SpaceEdit | space/edit.vue | 空间管理 |
| `/doc` | Doc | doc/index.vue | 文档页 |
| `/:pathMatch(.*)*` | NotFound | error/404.vue | 404 |

路由使用 `createWebHashHistory()`（Hash 模式）。

---

## 七、构建与部署

### 7.1 开发环境

```bash
npm install --legacy-peer-deps
npm run dev        # 启动 Vite dev server，端口 5173
```

Vite proxy 配置：
```js
server: {
  port: 8329,
  proxy: {
    '/anymockweb_api': {
      target: 'http://localhost:8328',
      changeOrigin: true
    }
  }
}
```

### 7.2 生产构建

```bash
npm run build      # 输出 dist/ 目录
npm run preview    # 预览构建结果
```

构建产物（dist/）：
- 入口文件：`index.html`
- JS 分片：`assets/index-*.js`（按页面拆分的 code splitting）
- CSS 分片：`assets/*-*.css`
- 静态资源：`jztlogo.png`, `favicon.ico`

### 7.3 Docker 部署

提供 `Dockerfile` 和 `nginx.conf`，用于容器化部署。

---

## 八、已知问题与限制

1. **后端 itemsPerPage 最大 20**：超过会报"参数错误"，前端已将 page-sizes 限制为 `[10, 20]`
2. **selectBySpaceId 行为**：只返回该空间的直属接口，不含后代空间接口；选中父节点返回 0 条
3. **API Tester 直调 Axios**：不走 request.js 拦截器，独自处理响应
4. **Monaco Editor 主题**：硬编码 `theme="vs"`，未跟随主题系统切换
5. **空间管理 `/space/selectById` 不存在**：点击树节点直接使用树节点 data，无单独详情接口
6. **接口列表翻页空间过滤失效**：使用 selectAll 分页 + 客户端过滤时，换页后过滤条件会重置（当前 `list.vue` 切换空间后重置 page=1，但换页不重新过滤）
7. **Groovy 脚本字段名映射**：编辑页 loadInterfaceDetail 中未填充 `description` 字段（只写了 responseBody/syncScript 等），可能导致编辑后 description 丢失
8. **SpaceTreeDropdown 组件中 `allowCreateInterface` 判断**：仅允许创建接口的空间才可选中，但 hover 预览接口时没有此限制

---

## 九、启动说明

### 前置条件
- Node.js v16+（建议通过 nvm 管理）
- 后端 Manager 服务运行于 `localhost:8328`（Vite proxy 目标）
- 后端 Core 服务运行于 `localhost:8330`（Mock 请求目标）

### 快速启动

```bash
# 安装依赖
cd /Users/Zhuanz/IdeaProjects/AnyMock/anymock-fe-new
npm install --legacy-peer-deps

# 开发模式启动（端口 8329）
npm run dev

# 访问 http://localhost:8329
```

---

## 十、页面截图说明（文字版）

### 首页
```
┌─────────────────────────────────────────────────────┐
│  JztMock 接口Mock平台（渐变Banner，紫蓝渐变背景）    │
│  轻松模拟接口响应，解决外部依赖...                   │
│  [创建接口]  [浏览接口]                              │
│  ┌────────┐  ┌────────┐  ┌────────┐                  │
│  │  3     │  │  2     │  │  ∞     │                  │
│  │接口类型│  │响应模式│  │无限可能│                  │
│  └────────┘  └────────┘  └────────┘                  │
├─────────────────────────────────────────────────────┤
│  核心能力                                            │
│  [⚡快速Mock] [🖥️Groovy脚本] [🌿分支路由]            │
│  [⏱️延迟响应] [🔗API调试]    [📁空间管理]            │
├─────────────────────────────────────────────────────┤
│  三种配置模式                                        │
│  [静态文本]  [Groovy脚本]  [预设分支]               │
└─────────────────────────────────────────────────────┘
```

### 接口列表页
```
┌─────────────────────────────────────────────────────┐
│  接口列表                           [创建接口]       │
├─────────────────────────────────────────────────────┤
│  [空间树下拉▼]  [搜索接口名称/URL]                    │
├─────────────────────────────────────────────────────┤
│ 接口名称 │ 请求路径      │ 所属空间 │ 模式  │ 操作  │
│──────────┼───────────────┼──────────┼───────┼───────│
│ 获取用户 │ [GET] /api/u  │ 基础资料 │ 静态  │编辑复制删除│
│ 创建订单 │ [POST] /api/o │ 交易空间 │ Groovy│编辑复制删除│
├─────────────────────────────────────────────────────┤
│         total 20    [10] [20]  ◀ 1 2 ▶             │
└─────────────────────────────────────────────────────┘
```

### API 调试器
```
┌─────────────────────────────────────────────────────┐
│  [POST ▼] [请输入接口路径，如 /api/users]  [发送]     │
│  [Token 输入框]                                      │
│  ┌Headers────┐ ┌Body────────────────────────────┐   │
│  │ Key   │ Value   │ [请求Body（JSON）]          │   │
│  │ [+添加]        │                              │   │
│  └───────────────────────────────────────────────┘   │
│  响应：                                              │
│  [200 OK]  耗时: 45ms  2024-04-20 15:30:00           │
│  ┌Body────┐ ┌Headers───────────────────────────┐    │
│  │ { ... } │ │ content-type: application/json  │    │
│  └────────┘ └────────────────────────────────────┘   │
│  调用记录：                                           │
│  [POST] /api/users  200  15:30:01  ✕                │
│  [GET]  /api/users/1 404  15:29:55  ✕                │
└─────────────────────────────────────────────────────┘
```

### 已完成功能

- [x] 自动化返回结果（表单数据 + 响应 localStorage 持久化）
- [x] Token 独立输入框（自动附加 Authorization Header）
- [x] 环境配置（Core URL 前缀可配置）
- [x] 历史调用记录（localStorage 持久化，最多10条，点击导入，单条删除/清空）
