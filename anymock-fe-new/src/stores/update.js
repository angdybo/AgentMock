import { defineStore } from 'pinia'
import { ref } from 'vue'

// 当前版本信息和更新内容
const UPDATE_INFO = {
  version: '1.2.0',
  date: '2026-04-23',
  title: 'JztMock v1.2.0 更新说明',
  content: [
    '🎲 新增数据生成器功能 - 支持30+种数据生成器',
    '⚡ 性能优化 - 冲突检测器口速度提升10-30倍',
    '🎨 UI优化 - 左侧导航栏铺满屏幕，Logo区域渐变背景',
    '📝 首页弹窗 - 显示最新版本更新内容'
  ]
}

export const useUpdateStore = defineStore('update', () => {
  const lastViewedVersion = ref(localStorage.getItem('anymock-last-version') || '')
  const currentVersion = UPDATE_INFO.version

  const hasNewUpdate = ref(lastViewedVersion.value !== currentVersion)

  const markUpdateAsViewed = () => {
    localStorage.setItem('anymock-last-version', currentVersion)
    lastViewedVersion.value = currentVersion
    hasNewUpdate.value = false
  }

  const getUpdateContent = () => UPDATE_INFO

  const resetUpdateCheck = () => {
    localStorage.removeItem('anymock-last-version')
    lastViewedVersion.value = ''
    hasNewUpdate.value = true
  }

  return {
    lastViewedVersion,
    currentVersion,
    hasNewUpdate,
    markUpdateAsViewed,
    getUpdateContent,
    resetUpdateCheck
  }
})
