<template>
  <div class="interface-list-panel">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SpaceTreeDropdown
        v-model="filterSpaceId"
        :tree-data="spaceOptions"
        :space-map="spaceMap"
        @change="loadData"
      />
      <el-input v-model="keyword" placeholder="搜索接口名称/URL" clearable style="width: 240px;" @input="debounceSearch" />
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="name" label="接口名称" min-width="150" />
      <el-table-column label="请求路径" min-width="200">
        <template #default="{ row }">
          <span class="method-badge" :class="'method-' + row.requestMethod.toLowerCase()">{{ row.requestMethod }}</span>
          {{ row.requestUri }}
        </template>
      </el-table-column>
      <el-table-column prop="spaceName" label="所属空间" width="150" />
      <el-table-column prop="configMode" label="模式" width="100">
        <template #default="{ row }">
            <el-tag v-if="row.configMode === 'TEXT'" type="info" size="small">静态</el-tag>
            <el-tag v-else-if="row.configMode === 'GROOVY'" type="warning" size="small">Groovy</el-tag>
            <el-tag v-else type="success" size="small">分支</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" link size="small" @click="handleCopy(row)">复制</el-button>
          <el-button type="warning" link size="small" @click="handleCallLog(row)">调用记录</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
    <!-- 调用记录弹窗 -->
    <el-dialog v-model="callLogDialogVisible" title="调用记录" width="900px" destroy-on-close>
      <div v-if="callLogLoading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading"><Loading /></el-icon> 加载中...
      </div>
      <div v-else-if="callLogList.length === 0" style="text-align: center; padding: 40px; color: #909399;">
        暂无调用记录
      </div>
      <div v-else class="call-log-list">
        <div v-for="log in callLogList" :key="log.id" class="call-log-item">
          <div class="call-log-header">
            <span class="call-log-time">{{ formatTime(log.gmtCreate) }}</span>
            <span class="call-log-uri">{{ log.requestUri }}</span>
          </div>
          <div class="call-log-body">
            <div class="call-log-section">
              <div class="call-log-label">入参</div>
              <pre class="call-log-pre">{{ log.requestBody || '(空)' }}</pre>
            </div>
            <div class="call-log-section">
              <div class="call-log-label">出参</div>
              <pre class="call-log-pre">{{ formatJson(log.responseBody) }}</pre>
            </div>
          </div>
        </div>
      </div>
      <template #footer v-if="callLogList.length > 0">
        <el-pagination
          v-model:current-page="callLogPagination.page"
          :page-size="callLogPagination.pageSize"
          :total="callLogPagination.total"
          layout="prev, pager, next"
          @current-change="loadCallLogs"
        />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { selectBySpaceId, selectAll, deleteInterface, getSpaceTree, getCoreHostInfo, getCallLogs } from '@/api/anymock'
import SpaceTreeDropdown from '@/components/SpaceTreeDropdown.vue'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const spaceOptions = ref([])
const filterSpaceId = ref(undefined)
const keyword = ref('')
let searchTimer = null
const spaceMap = ref({}) // spaceId -> label

const pagination = ref({ page: 1, pageSize: 20, total: 0 })
const coreUrlPrefix = ref('http://localhost:8330')

// 调用记录相关
const callLogDialogVisible = ref(false)
const callLogLoading = ref(false)
const callLogList = ref([])
const currentInterfaceId = ref(null)
const callLogPagination = ref({ page: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    let list = []
    let totalCount = 0

    if (filterSpaceId.value) {
      // 有选中空间：用 selectBySpaceId API（服务端过滤，只返回该空间的直属接口）
      const res = await selectBySpaceId({
        criteria: { spaceId: Number(filterSpaceId.value) },
        page: pagination.value.page,
        itemsPerPage: pagination.value.pageSize
      })
      list = res.data?.list || []
      totalCount = res.data?.total || 0
    } else {
      // 无选中空间：用 selectAll API（分页）
      const res = await selectAll({
        page: pagination.value.page,
        itemsPerPage: pagination.value.pageSize
      })
      list = res.data?.list || []
      totalCount = res.data?.total || 0
    }

    // 填充 spaceName（map的key是String，spaceId是number，需统一）
    list.forEach(item => {
      item.spaceName = spaceMap.value[String(item.spaceId)] || item.spaceName || '-'
    })

    // 关键词搜索（客户端过滤）
    if (keyword.value) {
      const kw = keyword.value.toLowerCase()
      list = list.filter(item =>
        item.name.toLowerCase().includes(kw) ||
        (item.requestUri || '').toLowerCase().includes(kw)
      )
    }

    tableData.value = list
    pagination.value.total = totalCount
  } catch (e) {
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

const loadSpaces = async () => {
  try {
    const res = await getSpaceTree()
    spaceOptions.value = res.data || []
    // 建立 spaceId -> label 映射（key统一转String，API返回的spaceId是number）
    const map = {}
    const buildMap = (list) => {
      for (const item of list) {
        map[String(item.id)] = item.label
        if (item.children?.length) buildMap(item.children)
      }
    }
    buildMap(res.data || [])
    spaceMap.value = map
  } catch (e) {
    console.error('加载空间失败', e)
  }
}

const loadCoreInfo = async () => {
  try {
    const res = await getCoreHostInfo()
    if (res.data?.urlPrefix) coreUrlPrefix.value = res.data.urlPrefix
  } catch (e) {
    // use default
  }
}

const debounceSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.value.page = 1
    loadData()
  }, 400)
}

const handleEdit = (row) => router.push(`/interface/update/${row.id}`)

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」？`, '提示', { type: 'warning' })
    await deleteInterface({ id: row.id })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + e.message)
  }
}

const handleCopy = (row) => {
  const url = coreUrlPrefix.value + row.requestUri
  copyToClipboard(url)
}

function copyToClipboard(text) {
  // 优先使用 Clipboard API
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('复制成功：' + text)
    }).catch(() => {
      fallbackCopy(text)
    })
  } else {
    fallbackCopy(text)
  }
}

function fallbackCopy(text) {
  // 创建临时 textarea
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  
  try {
    const successful = document.execCommand('copy')
    if (successful) {
      ElMessage.success('复制成功：' + text)
    } else {
      ElMessage.error('复制失败')
    }
  } catch (err) {
    ElMessage.error('复制失败')
  }
  
  document.body.removeChild(textarea)
}

// 调用记录相关方法
const handleCallLog = async (row) => {
  currentInterfaceId.value = row.id
  callLogPagination.value.page = 1
  callLogDialogVisible.value = true
  await loadCallLogs()
}

const loadCallLogs = async () => {
  callLogLoading.value = true
  try {
    const res = await getCallLogs({
      interfaceId: currentInterfaceId.value,
      page: callLogPagination.value.page,
      itemsPerPage: callLogPagination.value.pageSize
    })
    callLogList.value = res.data?.list || []
    callLogPagination.value.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载调用记录失败: ' + e.message)
  } finally {
    callLogLoading.value = false
  }
}

const formatJson = (str) => {
  if (!str) return ''
  try {
    const obj = typeof str === 'string' ? JSON.parse(str) : str
    return JSON.stringify(obj, null, 2)
  } catch {
    return str
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  // 如果年份是差10年以上（如2026），可能是服务器时间错误
  if (Math.abs(date.getFullYear() - now.getFullYear()) > 10) {
    return now.toLocaleString('zh-CN', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  }
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const getStatusClass = (status) => {
  if (!status) return 'status-error'
  if (status >= 200 && status < 300) return 'status-2xx'
  if (status >= 300 && status < 400) return 'status-3xx'
  if (status >= 400 && status < 500) return 'status-4xx'
  return 'status-5xx'
}

onMounted(async () => {
  await Promise.all([loadSpaces(), loadCoreInfo()])
  await loadData()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.method-badge {
  display: inline-block; padding: 2px 6px; border-radius: 4px;
  font-size: 11px; font-weight: bold; margin-right: 6px;
}
.method-get { background: #e1f3d8; color: #67c23a; }
.method-post { background: #d9ecff; color: #409eff; }
.method-put { background: #fef0e9; color: #e6a23c; }
.method-delete { background: #fde2e2; color: #f56c6c; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.dark .interface-list-panel { background: #1a1a2e; color: #e0e0e0; }

/* 调用记录弹窗样式 */
.call-log-list {
  max-height: 60vh;
  overflow-y: auto;
}
.call-log-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}
.call-log-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
}
.call-log-method {
  font-weight: bold;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}
.call-log-uri {
  flex: 1;
  font-family: monospace;
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.call-log-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 3px;
  flex-shrink: 0;
}
.call-log-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}
.call-log-cost {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}
.call-log-body {
  padding: 12px;
}
.call-log-section {
  margin-bottom: 12px;
}
.call-log-section:last-child {
  margin-bottom: 0;
}
.call-log-label {
  font-size: 12px;
  font-weight: bold;
  color: #909399;
  margin-bottom: 6px;
}
.call-log-pre {
  background: #fafafa;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
  margin: 0;
}
.status-2xx { background: #e1f3d8; color: #67c23a; }
.status-3xx { background: #d9ecff; color: #409eff; }
.status-4xx { background: #fef0e9; color: #e6a23c; }
.status-5xx { background: #fde2e2; color: #f56c6c; }
.status-error { background: #fde2e2; color: #f56c6c; }

.dark .call-log-item { border-color: #3a3a5c; }
.dark .call-log-header { background: #252540; border-color: #3a3a5c; }
.dark .call-log-uri { color: #c0c4cc; }
.dark .call-log-pre { background: #252540; }
</style>
