<template>
  <div class="api-tester">
    <!-- 请求配置区 -->
    <div class="request-section">
      <div class="url-bar">
        <el-select v-model="form.method" size="default" style="width: 110px;">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
          <el-option label="HEAD" value="HEAD" />
          <el-option label="OPTIONS" value="OPTIONS" />
        </el-select>
        <el-input
          v-model="form.url"
          placeholder="请输入接口路径，如 /api/users"
          size="default"
          style="flex: 1;"
          @keyup.enter="handleSend"
        />
        <el-button type="primary" size="default" :loading="loading" @click="handleSend">
          发送
        </el-button>
      </div>

      <div class="token-bar">
        <el-input
          v-model="form.token"
          placeholder="Token"
          size="default"
          style="flex: 1;"
        />
      </div>

      <!-- Headers & Body Tabs -->
      <el-tabs v-model="activeTab" class="request-tabs">
        <el-tab-pane label="Headers" name="headers">
          <div class="headers-editor">
            <div v-for="(header, idx) in form.headers" :key="idx" class="header-row">
              <el-input v-model="header.key" placeholder="Header名" size="default" />
              <el-input v-model="header.value" placeholder="Header值" size="default" />
              <el-button type="danger" size="small" :icon="Delete" @click="removeHeader(idx)" />
            </div>
            <el-button size="small" @click="addHeader">+ 添加Header</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="Body" name="body">
          <el-input
            v-model="form.body"
            type="textarea"
            :rows="6"
            placeholder="请求Body（JSON格式）"
            resize="vertical"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 响应区 -->
    <div class="response-section" v-if="response">
      <div class="response-meta">
        <span class="status-badge" :class="statusClass">{{ response.status }}</span>
        <span class="cost-time">耗时: {{ response.costTime }}ms</span>
        <span class="response-time">{{ response.responseTime }}</span>
      </div>
      <el-tabs v-model="responseTab" class="response-tabs">
        <el-tab-pane label="Body" name="body">
          <el-input
            v-model="response.body"
            type="textarea"
            :rows="10"
            readonly
            resize="vertical"
          />
        </el-tab-pane>
        <el-tab-pane label="Headers" name="headers">
          <div class="response-headers">
            <div v-for="(val, key) in response.headers" :key="key" class="resp-header-row">
              <span class="resp-header-key">{{ key }}:</span>
              <span class="resp-header-val">{{ val }}</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 错误提示 -->
    <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon style="margin-top: 12px;" />

    <!-- 调用记录 -->
    <div class="history-section" v-if="history.length > 0">
      <div class="history-header">
        <span class="history-title">调用记录</span>
        <el-button size="small" link type="danger" @click="clearHistory">清空</el-button>
      </div>
      <div class="history-list">
        <div
          v-for="(item, idx) in history"
          :key="item.id"
          class="history-item"
          :class="{ 'history-item-error': item.isError }"
          @click="loadHistory(item)"
        >
          <span class="history-method" :class="'method-' + item.method.toLowerCase()">{{ item.method }}</span>
          <span class="history-url">{{ item.url }}</span>
          <span class="history-status" :class="getHistoryStatusClass(item)">{{ item.isError ? '失败' : item.status }}</span>
          <span class="history-time">{{ item.time }}</span>
          <el-button size="small" link type="danger" :icon="Delete" @click.stop="deleteHistory(idx)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const activeTab = ref('headers')
const responseTab = ref('body')
const loading = ref(false)
const coreUrlPrefix = 'http://localhost:8330'
const errorMsg = ref('')

const CACHE_KEY = 'anymock-api-tester-form'
const HISTORY_KEY = 'anymock-api-tester-history'
const MAX_HISTORY = 10

const form = ref({
  method: 'POST',
  token: '',
  url: '',
  headers: [{ key: '', value: '' }],
  body: ''
})

const response = ref(null)
const history = ref([])

// 页面加载时从 localStorage 恢复数据
onMounted(() => {
  try {
    const cached = localStorage.getItem(CACHE_KEY)
    if (cached) {
      const data = JSON.parse(cached)
      form.value.method = data.method || 'POST'
      form.value.token = data.token || ''
      form.value.url = data.url || ''
      form.value.body = data.body || ''
      form.value.headers = (data.headers && data.headers.length > 0) ? data.headers : [{ key: '', value: '' }]
    }
  } catch (e) {
    // ignore
  }
  try {
    const his = localStorage.getItem(HISTORY_KEY)
    if (his) {
      history.value = JSON.parse(his)
    }
  } catch (e) {
    // ignore
  }
})

// 保存到 localStorage
const saveCache = () => {
  try {
    const data = {
      method: form.value.method,
      token: form.value.token,
      url: form.value.url,
      body: form.value.body,
      headers: form.value.headers.filter(h => h.key.trim() || h.value.trim())
    }
    localStorage.setItem(CACHE_KEY, JSON.stringify(data))
  } catch (e) {
    // ignore
  }
}

const statusClass = computed(() => {
  if (!response.value) return ''
  const s = response.value.status
  if (s >= 200 && s < 300) return 'status-2xx'
  if (s >= 300 && s < 400) return 'status-3xx'
  if (s >= 400 && s < 500) return 'status-4xx'
  return 'status-5xx'
})

const getHistoryStatusClass = (item) => {
  if (item.isError) return 'status-error'
  const s = item.status
  if (s >= 200 && s < 300) return 'status-2xx'
  if (s >= 300 && s < 400) return 'status-3xx'
  if (s >= 400 && s < 500) return 'status-4xx'
  return 'status-5xx'
}

const loadHistory = (item) => {
  form.value.method = item.method
  form.value.token = item.token || ''
  form.value.url = item.url
  form.value.body = item.body || ''
  form.value.headers = (item.headers && item.headers.length > 0) ? item.headers : [{ key: '', value: '' }]
  if (item.response) {
    response.value = item.response
  }
}

const addHistory = (record) => {
  const list = history.value.filter(h => !(h.url === record.url && h.method === record.method))
  list.unshift(record)
  if (list.length > MAX_HISTORY) list.splice(MAX_HISTORY)
  history.value = list
  try {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
  } catch (e) {
    // ignore
  }
}

const deleteHistory = (idx) => {
  history.value.splice(idx, 1)
  try {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
  } catch (e) {
    // ignore
  }
}

const clearHistory = () => {
  history.value = []
  try {
    localStorage.removeItem(HISTORY_KEY)
  } catch (e) {
    // ignore
  }
}

const addHeader = () => {
  form.value.headers.push({ key: '', value: '' })
}

const removeHeader = (idx) => {
  form.value.headers.splice(idx, 1)
}

const handleSend = async () => {
  if (!form.value.url) {
    ElMessage.warning('请输入请求URL')
    return
  }
  errorMsg.value = ''
  response.value = null
  loading.value = true
  saveCache()
  const start = Date.now()
  try {
    const headers = {}
    if (form.value.token.trim()) {
      headers['Authorization'] = form.value.token.trim()
    }
    if (form.value.body.trim()) {
      headers['Content-Type'] = 'application/json'
    }
    form.value.headers.forEach(h => {
      if (h.key.trim()) headers[h.key.trim()] = h.value
    })
    const res = await axios({
      url: form.value.url.startsWith('/') ? coreUrlPrefix + form.value.url : form.value.url,
      method: form.value.method,
      headers,
      data: form.value.body || undefined,
      timeout: 30000
    })
    response.value = {
      status: res.status,
      body: typeof res.data === 'object' ? JSON.stringify(res.data, null, 2) : res.data,
      headers: res.headers,
      costTime: Date.now() - start,
      responseTime: new Date().toLocaleString()
    }
    addHistory({
      id: Date.now(),
      method: form.value.method,
      token: form.value.token,
      url: form.value.url,
      headers: form.value.headers.filter(h => h.key.trim()),
      body: form.value.body,
      status: res.status,
      isError: false,
      response: response.value,
      time: new Date().toLocaleTimeString()
    })
  } catch (e) {
    if (e.response) {
      response.value = {
        status: e.response.status,
        body: typeof e.response.data === 'object' ? JSON.stringify(e.response.data, null, 2) : e.response.data,
        headers: e.response.headers,
        costTime: Date.now() - start,
        responseTime: new Date().toLocaleString()
      }
      addHistory({
        id: Date.now(),
        method: form.value.method,
        token: form.value.token,
        url: form.value.url,
        headers: form.value.headers.filter(h => h.key.trim()),
        body: form.value.body,
        status: e.response.status,
        isError: false,
        response: response.value,
        time: new Date().toLocaleTimeString()
      })
    } else {
      errorMsg.value = e.message
      addHistory({
        id: Date.now(),
        method: form.value.method,
        token: form.value.token,
        url: form.value.url,
        headers: form.value.headers.filter(h => h.key.trim()),
        body: form.value.body,
        status: 0,
        isError: true,
        response: null,
        time: new Date().toLocaleTimeString()
      })
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.api-tester {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.url-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.token-bar {
  margin-bottom: 12px;
}

.request-tabs, .response-tabs {
  margin-top: 12px;
}

.headers-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.response-section {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.response-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
}

.status-2xx { background: #e1f3d8; color: #67c23a; }
.status-3xx { background: #d9ecff; color: #409eff; }
.status-4xx { background: #fef0e9; color: #e6a23c; }
.status-5xx { background: #fde2e2; color: #f56c6c; }

.cost-time, .response-time {
  font-size: 13px;
  color: #909399;
}

.response-headers {
  font-family: monospace;
  font-size: 13px;
}

.resp-header-row {
  display: flex;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.resp-header-key {
  color: #409eff;
  font-weight: bold;
}

.dark .api-tester { background: #1a1a2e; }
.dark .response-section { border-top-color: #3a3a5c; }
.dark .resp-header-row { border-bottom-color: #3a3a5c; }

.history-section {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.dark .history-title { color: #e0e0e0; }

.history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 240px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.history-item:hover {
  background: #ecf5ff;
}

.history-item-error {
  background: #fef0f0;
}

.history-item-error:hover {
  background: #fde2e2;
}

.history-method {
  font-weight: bold;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}

.history-url {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  font-family: monospace;
  font-size: 12px;
}

.history-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 3px;
  flex-shrink: 0;
}

.history-time {
  font-size: 11px;
  color: #909399;
  flex-shrink: 0;
}

.dark .history-item { background: #252540; }
.dark .history-item:hover { background: #1a1a2e; }
.dark .history-url { color: #a0a0c0; }
.dark .history-title { color: #e0e0e0; }

.status-error { background: #fde2e2; color: #f56c6c; }
</style>
