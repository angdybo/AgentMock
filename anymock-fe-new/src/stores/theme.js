import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('anymock-theme') || 'light')
  const themes = ['light', 'dark', 'purple', 'green']
  const themeColors = {
    light: { primary: '#409eff', name: '明亮' },
    dark: { primary: '#409eff', name: '暗黑' },
    purple: { primary: '#8957e5', name: '紫色' },
    green: { primary: '#67c23a', name: '绿色' }
  }

  watch(currentTheme, (val) => {
    localStorage.setItem('anymock-theme', val)
    document.documentElement.setAttribute('data-theme', val)
    // Apply Element Plus theme override
    document.documentElement.style.setProperty('--el-color-primary', themeColors[val]?.primary || '#409eff')
  }, { immediate: true })

  const setTheme = (theme) => {
    if (themes.includes(theme)) currentTheme.value = theme
  }

  const toggleTheme = () => {
    const idx = themes.indexOf(currentTheme.value)
    currentTheme.value = themes[(idx + 1) % themes.length]
  }

  return { currentTheme, themes, themeColors, setTheme, toggleTheme }
})
