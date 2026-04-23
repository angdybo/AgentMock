# JztMock 更新记录

## v1.2.0 (2026-04-23)

### 新增功能
- 🎲 数据生成器功能：支持30+种数据生成器
  - 字符串类：随机字符串、UUID、邮箱、手机号、身份证号、姓名
  - 数字类：随机整数、随机小数
  - 日期类：日期、日期时间、时间戳
  - 布尔类：随机布尔值
  - 数组类：随机数组
  - 对象类：地址、公司、省份城市
  - 网络类：URL、IP、MAC、颜色
  - 图片类：图片URL、头像URL
  - 特殊类：：null、空值等
- 📝 首页弹窗：首次访问时显示最新版本更新内容

### 性能优化
- ⚡ 冲突检测接口性能优化：响应时间从 2.3 秒降低到 70-200ms
  - 新增 `queryByKeyLightweight()` 轻量级查询方法
  - 避免加载不必要的关联数据（响应头、回调请求头、分支脚本）
  - 性能提升 10-30 倍

### UI 优化
- 🎨 左侧导航栏铺满屏幕高度，添加深色背景 (#304156)
- 🎨 Logo 区域使用渐变背景色（#2d4a6f 到 #1f2d3d）

### 技术改进
- 新增 `MockDataGeneratorService` 处理数据生成器模板解析
- 支持在响应体中使用 `{{functionName()}}` 格式的占位符
- 占位符在返回响应前自动替换为生成的真实数据

### 修改文件
**前端**：
- `anymock-fe-new/src/stores/update.js` - 更新版本信息和内容
- `anymock-fe-new/src/views/interface/edit.vue` - 集成数据生成器面板
- `anymock-fe-new/src/utils/dataGenerator.js` - 数据生成器工具库
- `anymock-fe-new/src/components/DataGenerator.vue` - 数据生成器面板组件
- `anymock-fe-new/src/layout/BasicLayout.vue` - 导航栏和 Logo 样式优化

**后端**：
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/MockDataGeneratorService.java` - 数据生成器服务
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/impl/MockDataGeneratorServiceImpl.java` - 数据生成器实现
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/impl/HttpSyncMockServiceImpl.java` - 集成数据生成器
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/impl/HttpAsyncMockServiceImpl.java` - 集成数据生成器
- `common/dal/src/main/java/com/dxm/anymock/common/dal/dao/HttpInterfaceDao.java` - 新增轻量级查询方法
- `common/dal/src/main/java/com/dxm/anymock/common/dal/dao/impl/HttpInterfaceDaoImpl.java` - 实现轻量级查询
- `manager/biz/src/main/java/com/dxm/anymock/manager/biz/service/impl/HttpInterfaceServiceImpl.java` - 使用轻量级查询优化冲突检测

---

## v1.1.0 (2026-04-23)

### 新增功能
- 🎉 全新 UI 设计，采用 Vue3 + Vite 重构，性能更佳
- ✨ 支持明暗紫色等多种主题切换
- 📝 接口编辑器升级，语法高亮更智能
- 🚀 响应速度大幅提升，操作更流畅
- 🔒 安全性增强，数据更安全

---

## 版本更新流程

详细更新流程请参考：`anymock-version-update.md`

### 快速更新步骤

1. 更新 `anymock-fe-new/src/stores/update.js` 中的版本信息
2. 前端构建：`npm run build`
3. 后端编译（如需）：`mvn clean install -DskipTests`
4. 本地测试：启动 Manager 和 Core 服务
5. 构建并推送到 Harbor：`./build-and-push.sh`
6. 更新本 CHANGELOG.md
7. 部署到生产环境

---

## 计划中的功能

- 📊 统计分析 Dashboard - 接口调用排行、趋势图
- 🎭 场景管理 - 每个接口支持多个场景
- 🔍 测试历史记录 - 保存和重放测试请求
- 📤 Mock 规则导入/导出 - 支持导入 Postman Collection
- ⌨️ 快捷键支持 - 提升操作效率
- 🔀 接口依赖关系 - 显示接口调用链
- 🔐 访问令牌 - 细粒度权限控制

---

## 数据生成器使用示例

### 基础示例

```json
{
  "id": "{{uuid()}}",
  "name": "{{chineseName()}}",
  "email": "{{email()}}",
  "phone": "{{phone()}}",
  "age": {{randomInt(18, 60)}},
  "createDate": "{{date('yyyy-MM-dd')}}",
  "active": {{randomBoolean()}}
}
```

### 高级示例

```json
{
  "userId": "{{uuid()}}",
  "user": {
    "name": "{{chineseName()}}",
    "avatar": "{{avatarUrl()}}",
    "address": "{{address()}}"
  },
  "orders": {{randomIntArray(5, 1, 100)}},
  "tags": {{randomStringArray(3, 10)}},
  "metadata": {
    "ip": "{{ipv4()}}",
    "mac": "{{mac()}}",
    "color": "{{color()}}"
  },
  "createDate": "{{date('yyyy-MM-dd HH:mm:ss')}}",
  "timestamp": {{timestamp()}}
}
```

---

## 反馈与建议

如有功能建议或问题反馈，请联系开发团队。
