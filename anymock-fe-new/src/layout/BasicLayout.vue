<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <img v-if="!isCollapsed" src="/jztlogo.png" class="logo-img" />
        <el-icon v-else><Monitor /></el-icon>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        :background-color="sidebarBg"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/interface/list">
          <el-icon><Connection /></el-icon>
          <template #title>接口列表</template>
        </el-menu-item>
        <el-menu-item index="/my_interface/list">
          <el-icon><User /></el-icon>
          <template #title>我的接口</template>
        </el-menu-item>
        <el-menu-item index="/space/edit">
          <el-icon><FolderOpened /></el-icon>
          <template #title>空间管理</template>
        </el-menu-item>
        <el-menu-item index="/doc">
          <el-icon><Document /></el-icon>
          <template #title>文档</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="themeStore.setTheme">
            <span class="theme-switcher">
              <el-icon><Brush /></el-icon>
              {{ themeStore.themeColors[themeStore.currentTheme]?.name }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="t in themeStore.themes"
                  :key="t"
                  :command="t"
                  :active="themeStore.currentTheme === t"
                >
                  <span class="theme-dot" :class="'dot-' + t" />
                  {{ themeStore.themeColors[t]?.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'
import { Brush } from '@element-plus/icons-vue'

const route = useRoute()
const themeStore = useThemeStore()
const { currentTheme } = storeToRefs(themeStore)

const isCollapsed = ref(false)
const activeMenu = computed(() => route.path)
const sidebarBg = computed(() => currentTheme.value === 'dark' ? '#16213e' : '#304156')
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.sidebar {
  transition: width 0.3s;
  overflow: hidden;
  height: 100vh;
  background: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #2d4a6f 0%, #1f2d3d 100%);
  padding-bottom: 20px;
}

.logo-img {
  height: 28px;
  width: auto;
}

.sidebar-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--anymock-bg);
  border-bottom: 1px solid var(--anymock-border);
  color: var(--anymock-text-primary);
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.main-content {
  background: var(--anymock-body-bg);
  padding: 20px;
  padding-bottom: 60px;
  min-height: calc(100vh - 60px);
}

.theme-switcher {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  color: var(--anymock-text-regular);
  transition: background 0.2s;
}

.theme-switcher:hover {
  background: var(--anymock-hover-bg);
}

.theme-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 6px;
}

.dot-light { background: #409eff; }
.dot-dark { background: #1a1a2e; border: 1px solid #3a3a5c; }
.dot-purple { background: #8957e5; }
.dot-green { background: #67c23a; }
</style>
