<template>
  <div class="doc-page">
    <div class="doc-header">
      <h1>JztMock 更新记录</h1>
      <p class="doc-subtitle">版本迭代与功能变更一览</p>
    </div>

    <el-timeline class="timeline">
      <el-timeline-item
        v-for="(item, idx) in changelogs"
        :key="idx"
        :timestamp="item.version"
        placement="top"
        :type="item.type"
      >
        <el-card shadow="hover" class="version-card">
          <template #header>
            <div class="card-header">
              <span class="version-tag">{{ item.version }}</span>
              <span class="version-date">{{ item.date }}</span>
            </div>
          </template>
          <div class="change-list">
            <div
              v-for="(change, cidx) in item.changes"
              :key="cidx"
              class="change-item"
              :class="'change-' + change.type"
            >
              <el-tag :type="tagType(change.type)" size="small" effect="dark">
                {{ tagText(change.type) }}
              </el-tag>
              <span class="change-desc">{{ change.desc }}</span>
            </div>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
const changelogs = [
  {
    version: 'v1.2.0',
    date: '2026-04-23',
    type: 'primary',
    changes: [
      { type: 'add', desc: '数据生成器功能：支持30+种数据生成器（字符串、数字、日期、布尔、数组、对象、网络、图片、特殊）' },
      { type: 'add', desc: '新增 MockDataGeneratorService 处理数据生成器模板解析' },
      { type: 'add', desc: '支持在响应体中使用 {{functionName()}} 格式的占位符' },
      { type: 'add', desc: '占位符在返回响应前自动替换为生成的真实数据' },
      { type: 'add', desc: '首页弹窗：首次访问时显示最新版本更新内容' },
      { type: 'add', desc: '新增 queryByKeyLightweight() 轻量级查询方法' },
      { type: 'feat', desc: '冲突检测接口性能优化：响应时间从 2.3 秒降低到 70-200ms，提升 10-30 倍' },
      { type: 'feat', desc: '冲突检测接口使用轻量级查询，避免加载不必要的关联数据（响应头、回调请求头、分支脚本）' },
      { type: 'ui', desc: '左侧导航栏铺满屏幕高度，添加深色背景 (#304156)' },
      { type: 'ui', desc: 'Logo 区域使用渐变背景色（#2d4a6f 到）' },
      { type: 'ui', desc: '接口编辑页面集成数据生成器面板' },
    ]
  },
  {
    version: 'v1.1.0',
    date: '2026-04-22',
    type: 'primary',
    changes: [
      { type: 'add', desc: '接口调用记录功能：支持查看每次接口调用的详细日志' },
      { type: 'add', desc: '接口列表新增「调用记录」按钮，点击查看该接口的所有调用历史' },
      { type: 'add', desc: '调用记录展示：时间、接口地址、入参、出参（简化版）' },
      { type: 'add', desc: '数据库新增表 anymock_http_interface_call_log 存储调用日志' },
      { type: 'add', desc: 'Core 服务自动记录每次接口调用的请求和响应信息' },
      { type: 'fix', desc: '修复 MyBatis Mapper 方法名大小写问题（selectByExampleWithRowBounds）' },
      { type: 'add', desc: 'SpaceTreeDropdown 空间树下拉悬浮组件，支持 hover 展开和接口预览' },
      { type: 'add', desc: '接口列表操作列新增「复制」按钮，一键复制完整 Core URL' },
      { type: 'fix', desc: '翻页 page-sizes 修复为 [10, 20]，符合后端最大限制' },
      { type: 'fix', desc: 'edit.vue 空间 cascader label 字段从 name 改为 label' },
      { type: 'fix', desc: 'edit.vue spaceId 校验逻辑，' },
      { type: 'feat', desc: 'ApiTester 默认 POST 请求，Token 输入框独立显示' },
      { type: 'feat', desc: 'ApiTester Body 有内容时自动设置 Content-Type: application/json' },
      { type: 'feat', desc: 'ApiTester 表单数据和调用记录 localStorage 持久化（最多10条）' },
      { type: 'feat', desc: 'ApiTester 调用记录功能：显示方法/URL/状态/时间，点击导入，' },
      { type: 'ui', desc: 'BasicLayout logo 改为「JztMock」' },
      { type: 'ui', desc: 'home/index.vue 标题改为「JztMock 接口Mock平台」' },
    ]
  },
  {
    version: 'v1.0.0',
    date: '2026-04-18',
    type: 'info',
    changes: [
      { type: 'add', desc: 'Vue 3 + Vite 重构版本正式上线' },
      { type: 'add', desc: 'Element Plus 组件库集成' },
      { type: 'add', desc: 'Pinia 状态管理（主题、空间、接口）' },
      { type: 'add', desc: 'Monaco Editor 代码编辑器（Groovy 脚本）' },
      { type: 'add', desc: '四种主题切换：明亮、暗黑、紫色、绿色' },
      { type: 'add', desc: '接口 CRUD（创建/编辑/删除/复制）' },
      { type: 'add', desc: '空间树管理（增删改查）' },
      { type: 'add', desc: '三种配置模式：静态文本、Groovy脚本、Groovy预设分支' },
      { type: 'add', desc: '同步/异步延迟响应支持' },
      { type: 'add', desc: 'API 调试器（HTTP 请求发送与响应查看）' },
      { type: 'add', desc: '首页 Banner 与能力展示' },
    ]
  }
]

const tagType = (type) => {
  const map = { add: 'success', fix: 'warning', feat: 'primary', ui: 'info', remove: 'danger' }
  return map[type] || 'info'
}

const tagText = (type) => {
  const map = { add: '新增', fix: '修复', feat: '功能', ui: '优化', remove: '移除' }
  return map[type] || type
}
</script>

<style scoped>
.doc-page {
  padding: 24px 40px;
  max-width: 900px;
  margin: 0 auto;
}

.doc-header {
  margin-bottom: 32px;
}

.doc-header h1 {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.doc-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.timeline {
  margin-top: 8px;
}

.version-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.version-tag {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.version-date {
  font-size: 13px;
  color: #909399;
}

.change-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.change-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.change-item .el-tag {
  flex-shrink: 0;
  margin-top: 2px;
}

.change-desc {
  flex: 1;
}

:deep(.el-timeline-item__node--primary) {
  background-color: #409eff;
}

:deep(.el-timeline-item__node--info) {
  background-color: #909399;
}

.dark .doc-header h1 {
  color: #e0e0e0;
}

.dark .doc-subtitle {
  color: #909399;
}

.dark .version-tag {
  color: #e0e0e0;
}

.dark .version-date {
  color: #909399;
}

.dark .change-desc {
  color: #c0c4cc;
}

:deep(.dark .el-card) {
  background: #1a1a2e;
  border-color: #3a3a5c;
}

:deep(.dark .el-card__header) {
  background: #252540;
  border-color: #3a3a5c;
}
</style>
