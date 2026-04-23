<template>
  <div class="interface-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>接口列表</h2>
      <el-button type="primary" @click="$router.push('/interface/create/0')">
        <el-icon><Plus /></el-icon> 创建接口
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SpaceTreeDropdown
        v-model="filterSpaceId"
        :tree-data="spaceTree"
        :space-map="spaceMap"
        @change="handleSpaceChange"
      />
      <el-input v-model="keyword" placeholder="搜索接口名称/URL" clearable style="width: 240px;" @input="debounceSearch" />
    </div>

    <!-- 表格 -->
    <div class="table-container">
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleCopy(row)">复制</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { selectBySpaceId, selectAll, deleteInterface, getSpaceTree, getCoreHostInfo } from '@/api/anymock'
import SpaceTreeDropdown from '@/components/SpaceTreeDropdown.vue'

const router = useRouter()

// 状态
const loading = ref(false)
const tableData = ref([])
const spaceOptions = ref([])
const filterSpaceId = ref('')
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const spaceMap = ref({}) // spaceId -> spaceName
const spaceTree = ref([]) // 完整树结构，用于按path过滤
const coreUrlPrefix = ref('http://localhost:8330')
let searchTimer = null

// 加载空间列表并建立映射
async function loadSpaces() {
  try {
    const res = await getSpaceTree()
    spaceOptions.value = res.data || []
    spaceTree.value = res.data || []
    // 建立 spaceId -> label 的映射（key统一转String，API返回的spaceId是number）
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

async function loadCoreInfo() {
  try {
    const res = await getCoreHostInfo()
    if (res.data?.urlPrefix) coreUrlPrefix.value = res.data.urlPrefix
  } catch (e) {
    // use default
  }
}

// 加载接口数据
async function loadData() {
  loading.value = true
  try {
    let list = []
    let totalCount = 0
    if (filterSpaceId.value) {
      // 有选中空间：用 selectBySpaceId API（服务端过滤，只返回该空间的直属接口）
      const res = await selectBySpaceId({
        criteria: { spaceId: Number(filterSpaceId.value) },
        page: page.value,
        itemsPerPage: pageSize.value
      })
      list = res.data?.list || []
      totalCount = res.data?.total || 0
    } else {
      // 无选中空间：用 selectAll API（分页）
      const res = await selectAll({
        page: page.value,
        itemsPerPage: pageSize.value
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
    total.value = totalCount
  } catch (e) {
    ElMessage.error('加载失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

// 事件处理
function handleSpaceChange() {
  page.value = 1
  loadData()
}

function handleSizeChange() {
  page.value = 1
  loadData()
}

function handlePageChange() {
  loadData()
}

function debounceSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadData()
  }, 400)
}

function handleEdit(row) {
  router.push(`/interface/update/${row.id}`)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除接口「${row.name}」？`, '提示', { type: 'warning' })
    await deleteInterface({ id: row.id })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + e.message)
  }
}

function handleCopy(row) {
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

onMounted(async () => {
  await Promise.all([loadSpaces(), loadCoreInfo()])
  await loadData()
})
</script>

<style scoped>
.interface-list-page { max-width: 1200px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 { font-size: 20px; color: #303133; }

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.method-badge {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
  margin-right: 6px;
}

.method-get { background: #e1f3d8; color: #67c23a; }
.method-post { background: #d9ecff; color: #409eff; }
.method-put { background: #fef0e9; color: #e6a23c; }
.method-delete { background: #fde2e2; color: #f56c6c; }

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.dark .page-header h2 { color: #e0e0e0; }
.dark .filter-bar { background: #1a1a2e; color: #e0e0e0; }
.dark .table-container { background: #1a1a2e; }
</style>
