<template>
  <div class="interface-edit-page">
    <div class="page-header">
      <h2>{{ isCreate ? '创建接口' : '编辑接口' }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <div class="form-container">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" size="default">

        <!-- ========== 区块1: 基础设置 ========== -->
        <el-collapse v-model="activeNames" class="edit-collapse">
          <el-collapse-item title="基础设置" name="basic">
            <div class="area">
              <el-form-item label="接口名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入接口名称" />
              </el-form-item>

              <el-row :gutter="16">
                <el-col :span="11">
                  <el-form-item label="请求方法" prop="requestMethod">
                    <el-select v-model="form.requestMethod" placeholder="请选择请求方法" style="width:100%">
                      <el-option label="GET" value="GET" />
                      <el-option label="POST" value="POST" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="11">
                  <el-form-item label="所属空间" prop="spaceId">
                    <el-cascader
                      v-model="form.spacePath"
                      :options="spaceOptions"
                      :props="{ checkStrictly: true, label: 'label', value: 'id' }"
                      placeholder="请选择空间"
                      clearable
                      style="width:100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="接口URL" prop="interfaceUrl">
                <el-input v-model="form.interfaceUrl" placeholder="请输入接口URL，以/开头">
                  <template #prepend>
                    <span class="ip-prefix">{{ ipPrefix }}</span>
                  </template>
                  <template #append>
                    <el-button @click="copyUrl" title="复制完整URL">
                      <el-icon><CopyDocument /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>

              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="配置模式" prop="configMode">
                    <el-select v-model="form.configMode" placeholder="请选择模式" style="width:100%">
                      <el-option label="静态文本" value="TEXT" />
                      <el-option label="Groovy脚本" value="GROOVY" />
                      <el-option label="Groovy预设分支" value="GROOVY_TEMPLATE_SWITCH_CASE" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="异步请求">
                    <el-switch v-model="form.needAsyncCallback" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="是否启用">
                    <el-switch v-model="form.start" />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 分支模式: 分支跳转脚本 -->
              <el-form-item v-if="form.configMode === 'GROOVY_TEMPLATE_SWITCH_CASE'" label="分支脚本" prop="branchJumpScript">
                <monaco-editor
                  v-model="form.branchJumpScript"
                  lang="groovy"
                  height="100"
                  theme="vs"
                />
              </el-form-item>
            </div>
          </el-collapse-item>

          <!-- ========== 区块2: 同步响应 ========== -->
          <el-collapse-item title="同步响应" name="sync">
            <div class="area">
              <el-form-item label="同步延时">
                <el-input v-model="form.syncDelay" placeholder="同步响应延时时间">
                  <template #append>ms</template>
                </el-input>
              </el-form-item>

              <el-form-item label="响应头">
                <el-table :data="syncHeaders" border size="small">
                  <el-table-column label="Key">
                    <template #default="{ row }">
                      <el-input v-if="!row.editable" v-model="row.name" size="small" />
                      <span v-else>{{ row.name }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="Value">
                    <template #default="{ row }">
                      <el-input v-if="!row.editable" v-model="row.value" size="small" />
                      <span v-else>{{ row.value }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ row, $index }">
                      <template v-if="!row.editable">
                        <el-button size="small" link type="primary" @click="saveHeader(row, $index, 'sync')">保存</el-button>
                        <el-button size="small" link type="danger" @click="cancelHeader(row, $index, 'sync')">取消</el-button>
                      </template>
                      <template v-else>
                        <el-button size="small" link type="primary" @click="editHeader(row, $index, 'sync')">编辑</el-button>
                        <el-button size="small" link type="danger" :disabled="syncHeaders.length <= 1" @click="deleteHeader($index, 'sync')">删除</el-button>
                      </template>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" style="margin-top:8px" @click="addHeader('sync')">+ 添加响应头</el-button>
              </el-form-item>

              <!-- 静态文本模式 -->
              <el-form-item v-if="form.configMode === 'TEXT'" label="响应内容" prop="responseBody">
                <el-input v-model="form.responseBody" type="textarea" :rows="6" placeholder="请输入响应内容" />
                <el-button type="primary" plain size="small" style="margin-top:8px" @click="showDataGeneratorDialog">
                  <el-icon><MagicStick /></el-icon> 插入数据生成器
                </el-button>
              </el-form-item>

              <!-- Groovy脚本模式 -->
              <el-form-item v-if="form.configMode === 'GROOVY'" label="同步脚本" prop="syncScript">
                <monaco-editor
                  v-model="form.syncScript"
                  lang="groovy"
                  height="150"
                  theme="vs"
                />
              </el-form-item>
            </div>
          </el-collapse-item>

          <!-- ========== 区块3: 异步响应 ========== -->
          <el-collapse-item v-if="form.needAsyncCallback" title="异步响应" name="async">
            <div class="area">
              <el-row :gutter="16">
                <el-col :span="11">
                  <el-form-item label="异步请求方法" prop="callbackRequestMethod">
                    <el-select v-model="form.callbackRequestMethod" placeholder="请选择" style="width:100%">
                      <el-option label="GET" value="GET" />
                      <el-option label="POST" value="POST" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="11">
                  <el-form-item label="异步延时">
                    <el-input v-model="form.asyncDelay" placeholder="异步响应延时">
                      <template #append>ms</template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="异步URL" prop="callbackRequestUrl">
                <el-input v-model="form.callbackRequestUrl" placeholder="请输入完整URL，如 http://api.example.com/callback" />
              </el-form-item>

              <el-form-item label="异步请求头">
                <el-table :data="asyncHeaders" border size="small">
                  <el-table-column label="Key">
                    <template #default="{ row }">
                      <el-input v-if="!row.editable" v-model="row.name" size="small" />
                      <span v-else>{{ row.name }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="Value">
                    <template #default="{ row }">
                      <el-input v-if="!row.editable" v-model="row.value" size="small" />
                      <span v-else>{{ row.value }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ row, $index }">
                      <template v-if="!row.editable">
                        <el-button size="small" link type="primary" @click="saveHeader(row, $index, 'async')">保存</el-button>
                        <el-button size="small" link type="danger" @click="cancelHeader(row, $index, 'async')">取消</el-button>
                      </template>
                      <template v-else>
                        <el-button size="small" link type="primary" @click="editHeader(row, $index, 'async')">编辑</el-button>
                        <el-button size="small" link type="danger" :disabled="asyncHeaders.length <= 1" @click="deleteHeader($index, 'async')">删除</el-button>
                      </template>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" style="margin-top:8px" @click="addHeader('async')">+ 添加请求头</el-button>
              </el-form-item>

              <!-- 静态文本模式 -->
              <el-form-item v-if="form.configMode === 'TEXT'" label="回调响应体" prop="callbackRequestBody">
                <el-input v-model="form.callbackRequestBody" type="textarea" :rows="6" placeholder="接口返回体" />
              </el-form-item>

              <!-- Groovy脚本模式 -->
              <el-form-item v-if="form.configMode === 'GROOVY'" label="异步脚本" prop="asyncScript">
                <monaco-editor
                  v-model="form.asyncScript"
                  lang="groovy"
                  height="150"
                  theme="vs"
                />
              </el-form-item>
            </div>
          </el-collapse-item>

          <!-- ========== 区块4: 分支列表 (Groovy预设分支模式) ========== -->
          <el-collapse-item v-if="form.configMode === 'GROOVY_TEMPLATE_SWITCH_CASE'" title="分支列表" name="branch">
            <div class="area">
              <el-table :data="branchList" border size="small">
                <el-table-column label="Case名称" width="180">
                  <template #default="{ row }">
                    <el-input v-if="!row.editable" v-model="row.name" size="small" placeholder="case名称" />
                    <span v-else>{{ row.name }}</span>
                  </template>
                </el-table-column>
                <el-table-column :label="form.needAsyncCallback ? '同步脚本' : '脚本'">
                  <template #default="{ row }">
                    <monaco-editor
                      v-if="!row.editable"
                      v-model="row.syncScript"
                      lang="groovy"
                      height="80"
                      theme="vs"
                    />
                    <span v-else class="script-preview">{{ row.syncScript }}</span>
                  </template>
                </el-table-column>
                <el-table-column v-if="form.needAsyncCallback" label="异步脚本">
                  <template #default="{ row }">
                    <monaco-editor
                      v-if="!row.editable"
                      v-model="row.asyncScript"
                      lang="groovy"
                      height="80"
                      theme="vs"
                    />
                    <span v-else class="script-preview">{{ row.asyncScript }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="140">
                  <template #default="{ row, $index }">
                    <template v-if="!row.editable">
                      <el-button size="small" link type="primary" @click="saveBranch(row, $index)">保存</el-button>
                      <el-button size="small" link type="danger" @click="cancelBranch(row, $index)">取消</el-button>
                    </template>
                    <template v-else>
                      <el-button size="small" link type="primary" @click="editBranch(row, $index)">编辑</el-button>
                      <el-button size="small" link type="danger" :disabled="branchList.length <= 1" @click="deleteBranch($index)">删除</el-button>
                    </template>
                  </template>
                </el-table-column>
              </el-table>
              <el-button size="small" style="margin-top:8px" @click="addBranch">+ 添加分支</el-button>
            </div>
          </el-collapse-item>
        </el-collapse>

        <!-- 提交按钮 -->
        <div class="submit-area">
          <el-button type="primary" size="large" :loading="submitLoading" @click="handleSubmit">
            {{ submitText }}
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 数据生成器弹窗 -->
    <el-dialog
      v-model="dataGeneratorVisible"
      title="数据生成器"
      width="800px"
      :close-on-click-modal="false"
    >
      <data-generator @insert="handleInsertDataGenerator" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CopyDocument, MagicStick } from '@element-plus/icons-vue'
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import { getSpaceTree, getCoreHostInfo, selectById, insertInterface, updateInterface, conflictDetection } from '@/api/anymock'
import DataGenerator from '@/components/DataGenerator.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

// ============ 状态 ============
const activeNames = ref(['basic'])
const isCreate = computed(() => route.name === 'InterfaceCreate')
const submitText = computed(() => isCreate.value ? '立即创建' : '确认修改')
const submitLoading = ref(false)
const ipPrefix = ref('http://hostname')

// 数据生成器弹窗
const dataGeneratorVisible = ref(false)

const spaceOptions = ref([])

const form = ref({
  name: '',
  requestMethod: 'GET',
  spacePath: [],
  interfaceUrl: '',
  configMode: 'TEXT',
  needAsyncCallback: false,
  start: true,
  description: '',
  branchJumpScript: '',
  syncDelay: '',
  responseBody: '',
  syncScript: '',
  callbackRequestUrl: '',
  callbackRequestMethod: 'POST',
  asyncDelay: '',
  callbackRequestBody: '',
  asyncScript: '',
  branchScriptList: []
})

// 可编辑表格数据
const syncHeaders = ref([{ name: '', value: '', editable: true, _cache: {} }])
const asyncHeaders = ref([{ name: '', value: '', editable: true, _cache: {} }])
const branchList = ref([{ name: '', syncScript: '', asyncScript: '', editable: true, _cache: {} }])

// ============ 校验规则 ============
const validateInterfaceUrl = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入接口URL'))
  } else if (value[0] !== '/' || value.length < 4) {
    callback(new Error('URL必须以/开头且长度大于3位'))
  } else {
    try {
      const res = await conflictDetection({
        id: isCreate.value ? '' : route.params.id,
        uri: value,
        method: form.value.requestMethod
      })
      if (res.data?.conflict) {
        callback(new Error('URL已存在，请重新输入'))
      } else {
        callback()
      }
    } catch {
      callback()
    }
  }
}

const rules = {
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  requestMethod: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  spaceId: [{
    validator: (rule, value, callback) => {
      if (!form.value.spacePath || form.value.spacePath.length === 0) {
        callback(new Error('请选择空间'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }],
  interfaceUrl: [{ required: true, validator: validateInterfaceUrl, trigger: 'blur' }],
  configMode: [{ required: true, message: '请选择配置模式', trigger: 'change' }],
  callbackRequestUrl: [
    { required: true, message: '请输入异步URL', trigger: 'blur' },
    { pattern: /https?:\/\/[^\s]*/, message: '请输入正确URL格式: http://ip:port/path', trigger: 'blur' }
  ],
  callbackRequestMethod: [{ required: true, message: '请选择请求方法', trigger: 'change' }]
}

// ============ 加载数据 ============
const loadCoreInfo = async () => {
  try {
    const res = await getCoreHostInfo()
    if (res.data?.urlPrefix) ipPrefix.value = res.data.urlPrefix
  } catch { /* use default */ }
}

const loadSpaces = async () => {
  try {
    const res = await getSpaceTree()
    const data = res.data || []
    // 过滤不允许创建接口的空间（创建时）
    spaceOptions.value = data
  } catch (e) {
    console.error('加载空间失败', e)
  }
}

const loadInterfaceDetail = async () => {
  if (!isCreate.value && route.params.id) {
    try {
      const res = await selectById({ id: route.params.id })
      const d = res.data
      if (!d) return

      form.value.name = d.name || ''
      form.value.requestMethod = d.requestMethod || 'GET'
      form.value.interfaceUrl = d.requestUri || ''
      form.value.configMode = d.configMode === 'TEXT' ? 'TEXT'
        : d.configMode === 'GROOVY' ? 'GROOVY'
        : 'GROOVY_TEMPLATE_SWITCH_CASE'
      form.value.needAsyncCallback = !!d.needAsyncCallback
      form.value.start = !!d.start
      form.value.syncDelay = d.syncDelay || ''
      form.value.responseBody = d.responseBody || ''
      form.value.syncScript = d.syncScript || ''
      form.value.callbackRequestUrl = d.callbackRequestUrl || ''
      form.value.callbackRequestMethod = d.callbackRequestMethod || 'POST'
      form.value.asyncDelay = d.asyncDelay || ''
      form.value.callbackRequestBody = d.callbackRequestBody || ''
      form.value.asyncScript = d.asyncScript || ''
      form.value.branchJumpScript = d.branchJumpScript || ''

      // 空间path
      if (d.path && d.path.length > 0) {
        form.value.spacePath = d.path
      }

      // 同步响应头
      if (d.responseHeaderList && d.responseHeaderList.length > 0) {
        syncHeaders.value = d.responseHeaderList.map(v => ({ ...v, editable: true, _cache: {} }))
      }

      // 异步请求头
      if (d.callbackRequestHeaderList && d.callbackRequestHeaderList.length > 0) {
        asyncHeaders.value = d.callbackRequestHeaderList.map(v => ({ ...v, editable: true, _cache: {} }))
      }

      // 分支列表
      if (d.branchScriptList && d.branchScriptList.length > 0) {
        branchList.value = d.branchScriptList.map(v => ({ ...v, editable: true, _cache: {} }))
      }

      // 不可编辑情况
      if (d.variable === false) {
        submitText.value = '示例禁止修改'
        submitLoading.value = false
      }
    } catch (e) {
      ElMessage.error('加载接口详情失败: ' + e.message)
    }
  }
}

// ============ 同步响应头操作 ============
const addHeader = (type) => {
  if (type === 'sync') {
    syncHeaders.value.push({ name: '', value: '', editable: false, _cache: {} })
  } else {
    asyncHeaders.value.push({ name: '', value: '', editable: false, _cache: {} })
  }
}

const editHeader = (row, idx, type) => {
  const list = type === 'sync' ? syncHeaders.value : asyncHeaders.value
  list[idx]._cache = { ...list[idx] }
  list[idx].editable = false
}

const saveHeader = (row, idx, type) => {
  if (!row.name || !row.value) {
    ElMessage.warning('字段不能为空')
    return
  }
  const list = type === 'sync' ? syncHeaders.value : asyncHeaders.value
  list[idx].editable = true
}

const cancelHeader = (row, idx, type) => {
  const list = type === 'sync' ? syncHeaders.value : asyncHeaders.value
  if (list[idx]._cache) {
    Object.assign(list[idx], list[idx]._cache)
  }
  list[idx].editable = true
}

const deleteHeader = (idx, type) => {
  const list = type === 'sync' ? syncHeaders.value : asyncHeaders.value
  if (list.length > 1) {
    list.splice(idx, 1)
  }
}

// ============ 分支列表操作 ============
const addBranch = () => {
  branchList.value.push({ name: '', syncScript: '', asyncScript: '', editable: false, _cache: {} })
}

const editBranch = (row, idx) => {
  branchList.value[idx]._cache = { ...branchList.value[idx] }
  branchList.value[idx].editable = false
}

const saveBranch = (row, idx) => {
  if (!row.name || !row.syncScript) {
    ElMessage.warning('case名称和同步脚本不能为空')
    return
  }
  branchList.value[idx].editable = true
}

const cancelBranch = (row, idx) => {
  if (branchList.value[idx]._cache) {
    Object.assign(branchList.value[idx], branchList.value[idx]._cache)
  }
  branchList.value[idx].editable = true
}

const deleteBranch = (idx) => {
  if (branchList.value.length > 1) {
    branchList.value.splice(idx, 1)
  }
}

// ============ 复制URL ============
const copyUrl = () => {
  navigator.clipboard.writeText(ipPrefix.value + form.value.interfaceUrl).then(() => {
    ElMessage.success('复制成功')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// ============ 数据生成器 ============
const showDataGeneratorDialog = () => {
  dataGeneratorVisible.value = true
}

const handleInsertDataGenerator = (template) => {
  // 在光标位置插入模板
  const textarea = document.querySelector('textarea[placeholder*="请输入响应内容"]')
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const value = form.value.responseBody
    const newValue = value.substring(0, start) + template + value.substring(end)
    form.value.responseBody = newValue
    ElMessage.success('已插入')
  } else {
    // 如果无法获取焦点，直接追加到末尾
    form.value.responseBody += template
    ElMessage.success('已插入')
  }
  dataGeneratorVisible.value = false
}

// ============ 提交 ============
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请检查表单填写')
    return
  }

  submitLoading.value = true
  try {
    const payload = {
      name: form.value.name,
      requestUri: form.value.interfaceUrl,
      requestMethod: form.value.requestMethod,
      configMode: form.value.configMode,
      needAsyncCallback: form.value.needAsyncCallback,
      start: form.value.start,
      description: form.value.description || '',
      syncDelay: form.value.syncDelay || 0,
      asyncDelay: form.value.asyncDelay || 0,
      responseBody: form.value.responseBody || '',
      syncScript: form.value.syncScript || '',
      asyncScript: form.value.asyncScript || '',
      branchJumpScript: form.value.branchJumpScript || '',
      callbackRequestUrl: form.value.callbackRequestUrl || '',
      callbackRequestMethod: form.value.callbackRequestMethod || '',
      callbackRequestBody: form.value.callbackRequestBody || '',
      responseHeaderList: syncHeaders.value.filter(h => h.name).map(({ editable, _cache, ...rest }) => rest),
      callbackRequestHeaderList: asyncHeaders.value.filter(h => h.name).map(({ editable, _cache, ...rest }) => rest),
      branchScriptList: branchList.value.filter(b => b.name && b.syncScript).map(({ editable, _cache, ...rest }) => rest),
      spaceId: form.value.spacePath.length > 0 ? form.value.spacePath[form.value.spacePath.length - 1] : ''
    }

    if (!isCreate.value) {
      payload.id = route.params.id
      await updateInterface(payload)
    } else {
      await insertInterface(payload)
    }

    ElMessage.success(isCreate.value ? '创建成功' : '更新成功')
    router.push('/interface/list')
  } catch (e) {
    ElMessage.error((e.response?.data?.message) || e.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

// ============ 初始化 ============
onMounted(async () => {
  await Promise.all([loadCoreInfo(), loadSpaces()])
  if (!isCreate.value) {
    await loadInterfaceDetail()
  }
})

// 监听异步开关，动态调整分支列表列数
watch(() => form.value.needAsyncCallback, (val) => {
  if (val) {
    branchList.value.forEach(b => { if (!b.asyncScript) b.asyncScript = '' })
  }
})
</script>

<style scoped>
.interface-edit-page {
  max-width: 1100px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #303133;
}

.form-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.edit-collapse {
  border: none;
}

.area {
  padding: 0 8px;
}

.ip-prefix {
  font-family: monospace;
  color: #409eff;
  white-space: nowrap;
}

.submit-area {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eee;
  text-align: center;
}

.script-preview {
  font-family: monospace;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.dark .page-header h2 { color: #e0e0e0; }
.dark .form-container { background: #1a1a2e; }
.dark .submit-area { border-top-color: #3a3a5c; }
</style>
