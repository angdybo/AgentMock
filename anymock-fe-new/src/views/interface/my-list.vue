<template>
  <div class="my-interface-page">
    <div class="page-header">
      <h2>我的接口</h2>
      <el-button type="primary" @click="$router.push('/interface/create/0')">
        <el-icon><Plus /></el-icon> 创建接口
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 接口列表 Tab -->
      <el-tab-pane label="接口列表" name="list">
        <interface-list-panel />
      </el-tab-pane>
      <!-- API 调试器 Tab -->
      <el-tab-pane label="API 调试器" name="tester">
        <api-tester />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ApiTester from '@/components/ApiTester.vue'
import InterfaceListPanel from '@/components/InterfaceListPanel.vue'

const activeTab = ref('list')
</script>

<style scoped>
.my-interface-page {
  max-width: 1200px;
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

.main-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.dark .page-header h2 { color: #e0e0e0; }
.dark .main-tabs { background: #1a1a2e; }
</style>
