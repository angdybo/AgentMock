<template>
  <div id="app" :class="themeClass">
    <basic-layout>
      <router-view />
    </basic-layout>

    <!-- 更新弹窗 -->
    <el-dialog
      v-model="showUpdateDialog"
      title="🚀 平台更新通知"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="update-content">
        <div class="version-info">
          <span class="version-tag">{{ updateInfo.version }}</span>
          <span class="update-date">{{ updateInfo.date }}</span>
        </div>
        <h3 class="update-title">{{ updateInfo.title }}</h3>
        <ul class="update-list">
          <li v-for="(item, index) in updateInfo.content" :key="index">
            {{ item }}
          </li>
        </ul>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleCloseUpdate">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUpdateStore } from '@/stores/update'
import { ElDialog, ElButton } from 'element-plus'
import BasicLayout from '@/layout/BasicLayout.vue'

const themeStore = useThemeStore()
const updateStore = useUpdateStore()

const themeClass = computed(() => themeStore.currentTheme)
const showUpdateDialog = ref(false)
const updateInfo = ref(updateStore.getUpdateContent())

onMounted(() => {
  if (updateStore.hasNewUpdate) {
    showUpdateDialog.value = true
  }
})

const handleCloseUpdate = () => {
  updateStore.markUpdateAsViewed()
  showUpdateDialog.value = false
}
</script>

<style>
#app {
  height: 100%;
}

.update-content {
  padding: 10px 0;
}

.version-info {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-tag {
  display: inline-block;
  padding: 4px 12px;
  background: var(--el-color-primary, #409eff);
  color: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.update-date {
  color: var(--el-text-color-secondary, #909399);
  font-size: 13px;
}

.update-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--el-text-color-primary, #303133);
}

.update-list {
  margin: 0;
  padding-left: 20px;
}

.update-list li {
  margin-bottom: 10px;
  line-height: 1.6;
  color: var(--el-text-color-regular, #606266);
}

.update-list li:last-child {
  margin-bottom: 0;
}
</style>
