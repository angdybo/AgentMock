---
name: anymock-version-update
description: JztMock 版本更新流程 - 更新记录管理、首页弹窗提醒、构建部署
category: devops
---

# JztMock 版本更新流程

每次更新 JztMock 时，按照以下步骤执行：

## 1. 更新版本记录

在 `anymock-fe-new/src/stores/update.js` 中更新版本信息：

```javascript
const UPDATE_INFO = {
  version: '1.2.0',  // 递增版本号
  date: '2026-04-23',  // 当前日期
  title: 'JztMock v1.2.0 更新说明',
  content: [
    '🎲 新增数据生成器功能 - 支持30+种数据生成器',
    '⚡ 性能优化 - 冲突检测接口速度提升10-30倍',
    '🎨 UI优化 - 左侧导航栏铺满屏幕，Logo区域渐变背景',
    '📝 首页弹窗 - 显示最新版本更新内容'
  ]
}
```

## 2. 前端构建

```bash
cd /Users/Zhuanz/IdeaProjects/AnyMock/anymock-fe-new
npm run build
```

## 3. 后端编译（如果涉及后端修改）

```bash
cd /Users/Zhuanz/IdeaProjects/AnyMock
mvn clean install -DskipTests
```

## 4. 重新编译后端模块（如果修改了特定模块）

```bash
# 只编译修改的模块及其依赖
mvn clean install -DskipTests -pl core/biz,manager/biz -am
```

## 5. 本地测试

```bash
# 停止旧服务
lsof -ti :8329 :8330 | xargs kill -9

# 启动 Core 服务
cd /Users/Zhuanz/IdeaProjects/AnyMock/core/runner
java -jar target/anymock-core-runner-1.0-SNAPSHOT.jar --server.port=8330 &

# 启动 Manager 服务（使用新前端）
cd /Users/Zhuanz/IdeaProjects/AnyMock/manager/runner
mvn spring-boot:run -DskipTests -Dspring-boot.run.arguments="--fe.path=file:/Users/Zhuanz/IdeaProjects/AnyMock/anymock-fe-new/dist/" &
```

## 6. 测试功能

1. 访问 http://localhost:8329/
2. 检查首页更新弹窗是否正确显示
3. 测试新增或修改的功能

## 7. 构建并推送到 Harbor

```bash
cd /Users/Zhuanz/IdeaProjects/AnyMock
./build-and-push.sh
```

或者手动构建：

```bash
# 清理并编译
mvn clean package -DskipTests

# 打包
rm -rf output && mkdir -p output/manager output/core output/fe
cp manager/runner/target/anymock-manager-runner-1.0-SNAPSHOT.jar output/manager/
cp core/runner/target/anymock-core-runner-1.0-SNAPSHOT.jar output/core/
cp -r anymock-fe-new/dist/* output/fe/
cp package-dist/start.sh output/
cp package-dist/stop.sh output/
cp package-dist/docker-entrypoint.sh output/

# 打包 tar
cd output
tar -czf ../anymock-app-v{N}.tar .  # N 为递增的版本号

# 构建 Docker 镜像
docker build --platform linux/amd64 -t spd/anymock-app:v{N} .

# 推送到 Harbor
docker tag spd/anymock-app:v{N} 219.139.241.229:8844/spd/anymock-app:v{N}
docker push 219.139.241.229:8844/spd/anymock-app:v{N}

# 清理
cd ..
rm -rf output
```

## 8. 更新文档

在项目的 README.md 或 CHANGELOG.md 中更新版本记录：

```markdown
## v1.2.0 (2026-04-23)

### 新增功能
- 🎲 数据生成器功能：支持30+种数据生成器（字符串、数字、日期、布尔、数组、对象、网络、图片等）
- 📝 首页弹窗：首次访问时显示最新版本更新内容

### 性能优化
- ⚡ 冲突检测接口性能优化：响应时间从 2.3 秒降低到 70-200ms，提升 10-30 倍

### UI 优化
- 🎨 左侧导航栏铺满屏幕高度，添加深色背景
- 🎨 Logo 区域使用渐变背景色（亮蓝色到深蓝色）

### 技术改进
- 新增 `MockDataGeneratorService` 处理数据生成器模板解析
- 新增 `queryByKeyLightweight()` 轻量级查询方法，避免加载不必要的关联数据
```

## 9. 部署到生产环境

1. 在 Rancher/K8s 中升级镜像版本
2. 监控服务启动状态
3. 验证功能正常

---

## 常见问题

### Q: 更新弹窗一直显示，如何关闭？
A: 更新弹窗使用 localStorage 记录已查看的版本，只有当检测到新版本时才会显示。

### Q: 如何测试新功能？
A: 按照步骤 5 启动本地服务，访问 http://localhost:8329/ 进行测试。

### Q: 版本号如何递增？
A: 查看现有的 `anymock-app-vN.tar` 文件，N 为当前最大版本号，新版本使用 N+1。

---

## 注意事项

1. **版本号管理**：
   - 递增前先检查 `anymock-app-vN.tar` 文件
   - 确保版本号连续且唯一

2. **前端构建**：
   - 使用 `npm run build` 而非 `npm run dev`
   - 构建产物在 `anymock-fe-new/dist/` 目录

3. **后端编译**：
   - 修改代码后必须执行 `mvn clean install`
   - 否则运行时会使用旧的 JAR 文件

4. **测试验证**：
   - 本地测试通过后再推送到 Harbor
   - 重点测试本次修改的功能点

5. **文档同步**：
   - 每次更新都要记录在文档中
   - 保持 CHANGELOG.md 或 README.md 的更新记录完整

---

## 示例：本次更新 v1.2.0

### 修改内容

#### 前端
- `anymock-fe-new/src/stores/update.js` - 更新版本信息和内容
- `anymock-fe-new/src/views/interface/edit.vue` - 集成数据生成器面板
- `anymock-fe-new/src/utils/dataGenerator.js` - 数据生成器工具库
- `anymock-fe-new/src/components/DataGenerator.vue` - 数据生成器面板组件

#### 后端
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/MockDataGeneratorService.java` - 数据生成器服务
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/impl/MockDataGeneratorServiceImpl.java` - 数据生成器实现
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/impl/HttpSyncMockServiceImpl.java` - 集成数据生成器
- `core/biz/src/main/java/com/dxm/anymock/core/biz/service/impl/HttpAsyncMockServiceImpl.java` - 集成数据生成器
- `common/dal/src/main/java/com/dxm/anymock/common/dal/dao/HttpInterfaceDao.java` - 新增轻量级查询方法
- `common/dal/src/main/java/com/dxm/anymock/common/dal/dao/impl/HttpInterfaceDaoImpl.java` - 实现轻量级查询
- `manager/biz/src/main/java/com/dxm/anymock/manager/biz/service/impl/HttpInterfaceServiceImpl.java` - 使用轻量级查询优化冲突检测

### 更新记录

```markdown
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
  - 特殊类：null、空值等
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
```

---

## 下一步

使用此 skill 进行版本更新：
1. 按照步骤 1-8 执行
2. 记录本次更新内容
3. 测试验证
4. 推送部署

如有其他改进建议，先记录在待办事项中，下次更新时一并实现。
