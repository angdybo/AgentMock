<template>
  <div class="api-tester">
    <!-- 响应结果区（放在上面） -->
    <el-card class="response-card" shadow="hover" style="margin-bottom:15px;">
      <div slot="header" class="card-header">
        <span>响应结果</span>
        <el-tag v-if="statusCode" :type="statusType" size="small">{{ statusCode }}</el-tag>
        <el-tag v-if="responseTime" type="info" size="small" style="margin-left:8px">{{ responseTime }}ms</el-tag>
      </div>
      <pre v-if="responseBody" class="response-body" :class="{ 'error-body': isError }">{{ responseBody }}</pre>
      <div v-else class="empty-tip">点击「发送」按钮查看响应结果</div>
    </el-card>

    <!-- 请求配置区 -->
    <el-card class="request-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>API 调试</span>
      </div>
      
      <!-- URL + 方法 -->
      <el-row :gutter="10" style="margin-bottom: 15px;">
        <el-col :span="4">
          <el-select v-model="method" style="width:100%">
            <el-option label="GET" value="GET"></el-option>
            <el-option label="POST" value="POST"></el-option>
            <el-option label="PUT" value="PUT"></el-option>
            <el-option label="DELETE" value="DELETE"></el-option>
          </el-select>
        </el-col>
        <el-col :span="20">
          <el-input v-model="url" placeholder="请输入接口URL，例如: http://target.com/api/test" size="default">
            <el-button slot="append" icon="el-icon-s-promotion" type="primary"
              @click="sendRequest" :loading="loading" :disabled="loading">发送</el-button>
          </el-input>
        </el-col>
      </el-row>

      <!-- Body 输入（POST/PUT/DELETE 时显示） -->
      <div v-if="method !== 'GET'" style="margin-bottom: 15px;">
        <span style="color:#666;font-size:12px;margin-bottom:5px;display:block;">Request Body (JSON)</span>
        <el-input type="textarea" v-model="body" :rows="4" placeholder='{"key": "value"}' />
      </div>

      <!-- Authorization 输入 -->
      <div style="margin-bottom: 15px;">
        <span style="color:#666;font-size:12px;margin-bottom:5px;display:block;">Authorization</span>
        <el-input v-model="authorization" placeholder="Bearer token 或 Basic Base64" />
      </div>

    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ApiTester',
  data() {
    return {
      url: '',
      method: 'GET',
      body: '',
      authorization: '',
      loading: false,
      statusCode: '',
      responseBody: '',
      responseTime: '',
      isError: false
    }
  },
  computed: {
    statusType() {
      if (!this.statusCode) return 'info'
      const code = parseInt(this.statusCode)
      if (code >= 200 && code < 300) return 'success'
      if (code >= 400 && code < 500) return 'warning'
      if (code >= 500) return 'danger'
      return 'info'
    }
  },
  methods: {
    async sendRequest() {
      if (!this.url || !this.url.startsWith('http')) {
        this.$message.error('请输入有效的 URL（需包含 http:// 或 https://）')
        return
      }

      this.loading = true
      this.responseBody = ''
      this.isError = false
      const startTime = Date.now()

      try {
        const headers = { 'Content-Type': 'application/json' }
        if (this.authorization && this.authorization.trim()) {
          headers['Authorization'] = this.authorization.trim()
        }
        const res = await axios.post('/anymockweb_api/v2/api_proxy/send', {
          url: this.url,
          method: this.method,
          body: this.body,
          headers: headers
        })
        
        this.statusCode = res.status.toString()
        this.responseBody = typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2)
      } catch (e) {
        this.isError = true
        if (e.response) {
          this.statusCode = e.response.status.toString()
          this.responseBody = typeof e.response.data === 'string' ? e.response.data : JSON.stringify(e.response.data, null, 2)
        } else {
          this.statusCode = 'Error'
          this.responseBody = e.message || '请求失败，请检查网络或目标地址是否可达'
        }
      } finally {
        this.loading = false
        this.responseTime = Date.now() - startTime
      }
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.api-tester {
  padding: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.response-body {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
  max-height: 500px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;

  &.error-body {
    background: #2c1810;
    color: #f48771;
  }
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}
</style>
