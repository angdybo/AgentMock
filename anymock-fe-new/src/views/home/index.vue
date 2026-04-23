<template>
  <div class="home-page">
    <!-- 顶部 Banner -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">JztMock <span class="highlight">接口Mock平台</span></h1>
        <p class="hero-desc">轻松模拟接口响应，解决外部依赖、异常测试、性能测试、自动化测试等问题</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/interface/create/0')">
            <el-icon><Plus /></el-icon> 创建接口
          </el-button>
          <el-button size="large" @click="$router.push('/interface/list')">
            <el-icon><Search /></el-icon> 浏览接口
          </el-button>
        </div>
      </div>
      <div class="hero-stats">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-num">3</div>
              <div class="stat-label">接口类型</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-num">2</div>
              <div class="stat-label">响应模式</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-num">∞</div>
              <div class="stat-label">无限可能</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 功能特性 -->
    <div class="features-section">
      <h2 class="section-title">核心能力</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="feature-card">
            <el-icon class="feature-icon" :size="40"><Lightning /></el-icon>
            <h3>快速Mock</h3>
            <p>无需等待后端，实时返回自定义响应，加速前端开发</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card">
            <el-icon class="feature-icon" :size="40"><Cpu /></el-icon>
            <h3>Groovy脚本</h3>
            <p>支持Groovy动态脚本，灵活处理复杂业务逻辑</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card">
            <el-icon class="feature-icon" :size="40"><Branches /></el-icon>
            <h3>分支路由</h3>
            <p>预设分支模式，根据条件返回不同响应</p>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <div class="feature-card">
            <el-icon class="feature-icon" :size="40"><Timer /></el-icon>
            <h3>延迟响应</h3>
            <p>配置同步/异步延迟时间，模拟网络延迟场景</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card">
            <el-icon class="feature-icon" :size="40"><Link /></el-icon>
            <h3>API调试</h3>
            <p>内置API Tester，无需Postman即可调试接口</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card">
            <el-icon class="feature-icon" :size="40"><FolderOpened /></el-icon>
            <h3>空间管理</h3>
            <p>多空间隔离，分类管理不同项目的接口</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 接口类型说明 -->
    <div class="types-section">
      <h2 class="section-title">三种配置模式</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="type-card type-static">
            <div class="type-badge">静态文本</div>
            <h3>Static</h3>
            <p>直接配置响应Body，适合固定返回值的接口</p>
            <el-tag type="info">configMode = "0"</el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="type-card type-groovy">
            <div class="type-badge">Groovy脚本</div>
            <h3>Dynamic</h3>
            <p>编写Groovy脚本动态生成响应，支持复杂逻辑</p>
            <el-tag type="warning">configMode = "1"</el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="type-card type-branch">
            <div class="type-badge">预设分支</div>
            <h3>Branch</h3>
            <p>配置多个分支条件，根据条件匹配返回不同响应</p>
            <el-tag type="success">configMode = "2"</el-tag>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 60px 40px;
  color: #fff;
  margin-bottom: 40px;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
}

.highlight {
  color: #ffd700;
}

.hero-desc {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 30px;
  max-width: 600px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
}

.hero-stats {
  margin-top: 30px;
}

.stat-card {
  background: rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
}

.stat-num {
  font-size: 36px;
  font-weight: bold;
  color: #ffd700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

/* Features */
.features-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 28px;
  margin-bottom: 24px;
  color: #303133;
  text-align: center;
}

.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.feature-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 14px;
  color: #606266;
}

/* Types */
.type-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border-top: 4px solid;
}

.type-static { border-top-color: #909399; }
.type-groovy { border-top-color: #e6a23c; }
.type-branch { border-top-color: #67c23a; }

.type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  margin-bottom: 12px;
}

.type-static .type-badge { background: #f4f4f5; color: #909399; }
.type-groovy .type-badge { background: #fdf6ec; color: #e6a23c; }
.type-branch .type-badge { background: #f0f9eb; color: #67c23a; }

.type-card h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.type-card p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.dark .section-title { color: #e0e0e0; }
.dark .feature-card { background: #1a1a2e; }
.dark .feature-card p { color: #b0b0b0; }
.dark .type-card { background: #1a1a2e; }
.dark .type-card p { color: #b0b0b0; }
</style>
