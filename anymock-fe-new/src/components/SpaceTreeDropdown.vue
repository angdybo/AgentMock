<template>
  <div class="space-tree-dropdown" ref="containerRef">
    <!-- 触发按钮 -->
    <el-button class="space-dropdown-btn" @mouseenter="handleBtnMouseEnter" @mouseleave="handleBtnMouseLeave">
      <span>{{ selectedLabel || '全部接口' }}</span>
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </el-button>

    <!-- 自定义下拉面板 - 绝对定位 -->
    <div
      v-show="isVisible"
      class="space-dropdown-panel"
      @mouseenter="handlePanelMouseEnter"
      @mouseleave="handlePanelMouseLeave"
    >
      <!-- 全部接口选项 -->
      <div class="all-option" @click="handleClearFilter">
        <span class="all-option-icon">◎</span>
        <span>全部接口</span>
      </div>
      <!-- 空间树 -->
      <div class="space-tree-container" @click.stop>
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          :default-expand-all="true"
          :expand-on-click-node="false"
          :highlight-current="true"
          @node-click="handleNodeClick"
          @node-mouse-enter="handleMouseEnter"
          @node-mouse-leave="handleMouseLeave"
        >
          <template #default="{ node, data }">
            <span class="space-node">
              <el-icon v-if="data.children?.length" class="node-icon"><FolderOpened /></el-icon>
              <el-icon v-else class="node-icon"><Folder /></el-icon>
              <span>{{ data.label }}</span>
              <span v-if="hoveredSpaceId === data.id" class="hover-hint">悬浮预览</span>
            </span>
          </template>
        </el-tree>
      </div>
      <!-- 悬浮预览面板 -->
      <div v-if="hoveredSpaceId && previewInterfaces.length > 0" class="hover-preview">
        <div class="preview-title">「{{ hoveredSpaceName }}」的接口</div>
        <div class="preview-list">
          <div v-for="item in previewInterfaces" :key="item.id" class="preview-item" @click.stop="goToEdit(item)">
            <span class="method-badge" :class="'method-' + item.requestMethod.toLowerCase()">{{ item.requestMethod }}</span>
            <span class="preview-name">{{ item.name }}</span>
          </div>
          <div v-if="previewInterfaces.length >= 10" class="preview-more">...共{{ previewTotal }}条，点击查看更多</div>
        </div>
      </div>
      <!-- 无接口提示 -->
      <div v-else-if="hoveredSpaceId && previewInterfaces.length === 0 && !previewLoading" class="hover-preview">
        <div class="preview-title">「{{ hoveredSpaceName }}」</div>
        <div class="preview-empty">该空间暂无接口</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ArrowDown, Folder, FolderOpened } from '@element-plus/icons-vue'
import { selectBySpaceId } from '@/api/anymock'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  treeData: {
    type: Array,
    default: () => []
  },
  spaceMap: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const containerRef = ref(null)
const treeRef = ref(null)
const selectedLabel = ref('')
const hoveredSpaceId = ref(null)
const hoveredSpaceName = ref('')
const previewInterfaces = ref([])
const previewLoading = ref(false)
const previewTotal = ref(0)
const isVisible = ref(false)
let hideTimeout = null
let showTimeout = null

// 监听选中值变化，更新label显示
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    selectedLabel.value = props.spaceMap[String(newVal)] || `空间${newVal}`
  } else {
    selectedLabel.value = ''
  }
}, { immediate: true })

function showPanel() {
  clearTimeout(hideTimeout)
  showTimeout = setTimeout(() => {
    isVisible.value = true
    nextTick(() => {
      positionPanel()
    })
  }, 50)
}

function hidePanel() {
  clearTimeout(showTimeout)
  hideTimeout = setTimeout(() => {
    isVisible.value = false
    hoveredSpaceId.value = null
    previewInterfaces.value = []
  }, 150)
}

function positionPanel() {
  if (!containerRef.value) return
  const btn = containerRef.value.querySelector('.space-dropdown-btn')
  if (!btn) return
  const rect = btn.getBoundingClientRect()
  const panel = containerRef.value.querySelector('.space-dropdown-panel')
  if (!panel) return
  // 定位到按钮下方，左边与按钮对齐
  panel.style.position = 'fixed'
  panel.style.top = `${rect.bottom + 4}px`
  panel.style.left = `${rect.left}px`
  panel.style.zIndex = 9999
}

function handleBtnMouseEnter() {
  showPanel()
}

function handleBtnMouseLeave() {
  hidePanel()
}

function handlePanelMouseEnter() {
  clearTimeout(hideTimeout)
}

function handlePanelMouseLeave() {
  hidePanel()
}

// 点击树节点
function handleNodeClick(data) {
  if (!data.allowCreateInterface) {
    return
  }
  emit('update:modelValue', data.id)
  emit('change', data.id, data)
  hidePanel()
}

// 鼠标进入节点
async function handleMouseEnter(data) {
  hoveredSpaceId.value = data.id
  hoveredSpaceName.value = data.label

  if (!data.allowCreateInterface) {
    previewInterfaces.value = []
    return
  }

  previewLoading.value = true
  try {
    const res = await selectBySpaceId({
      criteria: { spaceId: data.id },
      page: 1,
      itemsPerPage: 10
    })
    previewInterfaces.value = res.data?.list || []
    previewTotal.value = res.data?.total || 0
  } catch (e) {
    previewInterfaces.value = []
  } finally {
    previewLoading.value = false
  }
}

// 鼠标离开节点
function handleMouseLeave() {
  hoveredSpaceId.value = null
  hoveredSpaceName.value = ''
  previewInterfaces.value = []
}

function handleClearFilter() {
  emit('update:modelValue', '')
  emit('change', '', null)
  hidePanel()
}

function goToEdit(item) {
  window.location.href = `/interface/update/${item.id}`
}

// 点击外部关闭
function handleClickOutside(e) {
  if (containerRef.value && !containerRef.value.contains(e.target)) {
    isVisible.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('resize', positionPanel)
  window.addEventListener('scroll', positionPanel)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('resize', positionPanel)
  window.removeEventListener('scroll', positionPanel)
})
</script>

<style scoped>
.space-tree-dropdown {
  position: relative;
  display: inline-block;
}

.space-dropdown-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.space-dropdown-panel {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 280px;
  max-height: 500px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.all-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
}

.all-option:hover {
  background: #ecf5ff;
}

.all-option-icon {
  font-size: 12px;
}

.space-tree-container {
  max-height: 400px;
  overflow-y: auto;
}

.space-node {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.node-icon {
  margin-right: 4px;
  color: #909399;
}

.hover-hint {
  margin-left: auto;
  font-size: 11px;
  color: #909399;
  font-style: italic;
}

.hover-preview {
  border-top: 1px solid #ebeef5;
  padding: 8px 12px;
  max-height: 200px;
  overflow-y: auto;
  background: #fafafa;
}

.preview-title {
  font-size: 12px;
  color: #606266;
  margin-bottom: 6px;
  font-weight: 500;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.preview-item:hover {
  background: #ecf5ff;
}

.preview-name {
  font-size: 12px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-more {
  font-size: 11px;
  color: #909399;
  text-align: center;
  padding: 4px;
}

.preview-empty {
  font-size: 12px;
  color: #909399;
  text-align: center;
  padding: 8px;
}

.method-badge {
  display: inline-block;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: bold;
  flex-shrink: 0;
}

.method-get { background: #e1f3d8; color: #67c23a; }
.method-post { background: #d9ecff; color: #409eff; }
.method-put { background: #fef0e9; color: #e6a23c; }
.method-delete { background: #fde2e2; color: #f56c6c; }
</style>
