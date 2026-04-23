<template>
  <div class="space-edit-page">
    <div class="page-header">
      <h2>空间管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增空间
      </el-button>
    </div>

    <div class="space-container">
      <el-row :gutter="20">
        <!-- 左侧：树形结构 -->
        <el-col :span="8">
          <div class="tree-panel">
            <el-tree
              ref="treeRef"
              :data="treeData"
              :props="{ label: 'label', children: 'children' }"
              node-key="id"
              highlight-current
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <span>{{ data.label }}</span>
                  <span class="node-actions">
                    <el-icon @click.stop="handleEdit(data)"><Edit /></el-icon>
                    <el-icon @click.stop="handleDelete(data)"><Delete /></el-icon>
                  </span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-col>

        <!-- 右侧：选中空间信息 -->
        <el-col :span="16">
          <div class="info-panel" v-if="currentSpace">
            <h3>空间详情</h3>
            <el-form :model="currentSpace" label-width="100px" size="default">
              <el-form-item label="空间名称">
                <el-input v-model="currentSpace.name" />
              </el-form-item>
              <el-form-item label="空间描述">
                <el-input v-model="currentSpace.description" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="访问权限">
                <el-tag v-if="currentSpace.accessAuthority === 'PUBLIC'" type="success">公开</el-tag>
                <el-tag v-else type="info">私有</el-tag>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSave">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-empty v-else description="点击左侧空间查看详情" />
        </el-col>
      </el-row>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增空间' : '编辑空间'" width="500px">
      <el-form :model="formData" label-width="100px" size="default">
        <el-form-item label="空间名称">
          <el-input v-model="formData.name" placeholder="请输入空间名称" />
        </el-form-item>
        <el-form-item label="上级空间">
          <el-cascader
            v-model="formData.parentId"
            :options="cascaderOptions"
            :props="{ checkStrictly: true, label: 'label', value: 'id' }"
            placeholder="顶级空间无需选择"
            clearable
          />
        </el-form-item>
        <el-form-item label="访问权限">
          <el-radio-group v-model="formData.accessAuthority">
            <el-radio :label="0">私有</el-radio>
            <el-radio :label="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="允许创建接口">
          <el-switch v-model="formData.allowCreateInterface" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Plus } from '@element-plus/icons-vue'
import { getSpaceTree, addSpace, updateSpace, deleteSpace } from '@/api/anymock'

const treeRef = ref(null)
const treeData = ref([])
const currentSpace = ref(null)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const formData = ref({
  id: null,
  name: '',
  parentId: null,
  accessAuthority: 0,
  allowCreateInterface: 0,
  description: ''
})

const cascaderOptions = computed(() => treeData.value)

const loadTree = async () => {
  try {
    const res = await getSpaceTree()
    treeData.value = res.data || []
  } catch (e) {
    ElMessage.error('加载空间树失败: ' + e.message)
  }
}

const handleNodeClick = (data) => {
  currentSpace.value = {
    id: data.id,
    name: data.label,
    description: data.description || '',
    accessAuthority: data.accessAuthority || 'PRIVATE',
    allowCreateInterface: data.allowCreateInterface,
    allowCreateSpace: data.allowCreateSpace,
    parentId: data.parentId,
    path: data.path || [],
    variable: data.variable
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  formData.value = { id: null, name: '', parentId: null, accessAuthority: 0, allowCreateInterface: 0, description: '' }
  dialogVisible.value = true
}

const handleEdit = (data) => {
  dialogMode.value = 'edit'
  formData.value = {
    id: data.id,
    name: data.label,
    parentId: data.parentId || null,
    accessAuthority: data.accessAuthority === 'PUBLIC' ? 1 : 0,
    allowCreateInterface: data.allowCreateInterface ? 1 : 0,
    description: data.description || ''
  }
  dialogVisible.value = true
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(`确定删除空间「${data.label}」？`, '提示', { type: 'warning' })
    await deleteSpace({ id: data.id })
    ElMessage.success('删除成功')
    if (currentSpace.value && currentSpace.value.id === data.id) {
      currentSpace.value = null
    }
    loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

const handleSave = async () => {
  if (!currentSpace.value) return
  try {
    await updateSpace({
      id: currentSpace.value.id,
      label: currentSpace.value.name,
      accessAuthority: currentSpace.value.accessAuthority === 'PUBLIC' ? 'PUBLIC' : 'PRIVATE',
      description: currentSpace.value.description || ''
    })
    ElMessage.success('保存成功')
    loadTree()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleSubmit = async () => {
  if (!formData.value.name) {
    ElMessage.warning('请输入空间名称')
    return
  }
  try {
    const payload = {
      label: formData.value.name,
      accessAuthority: formData.value.accessAuthority === 1 ? 'PUBLIC' : 'PRIVATE',
      allowCreateInterface: formData.value.allowCreateInterface ? 1 : 0,
      description: formData.value.description || ''
    }
    if (formData.value.parentId && formData.value.parentId.length > 0) {
      payload.parentId = formData.value.parentId[formData.value.parentId.length - 1]
    } else {
      payload.parentId = 0
    }
    if (dialogMode.value === 'edit') {
      payload.id = formData.value.id
      await updateSpace(payload)
    } else {
      await addSpace(payload)
    }
    ElMessage.success(dialogMode.value === 'add' ? '新增成功' : '修改成功')
    dialogVisible.value = false
    loadTree()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => { loadTree() })
</script>

<style scoped>
.space-edit-page {
  max-width: 1200px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 20px; color: #303133; }
.space-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.tree-panel {
  border-right: 1px solid #eee;
  min-height: 400px;
  padding-right: 16px;
}
.info-panel {
  padding: 0 16px;
}
.info-panel h3 {
  margin-bottom: 20px;
  color: #303133;
}
.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}
.el-tree-node__content:hover .node-actions { opacity: 1; }
.node-actions .el-icon { cursor: pointer; color: #409eff; }
.node-actions .el-icon:last-child { color: #f56c6c; }
.dark .page-header h2 { color: #e0e0e0; }
.dark .space-container { background: #1a1a2e; }
.dark .tree-panel { border-right-color: #3a3a5c; }
.dark .info-panel h3 { color: #e0e0e0; }
</style>
