<template>
  <div class="data-generator-panel">
    <div class="panel-header">
      <h4>🎲 数据生成器</h4>
      <p class="panel-desc">快速生成各种类型的 Mock 数据</p>
    </div>

    <el-tabs v-model="activeCategory" type="card">
      <el-tab-pane
        v-for="(generators, category) in generatorsByCategory"
        :key="category"
        :label="category"
      >
        <div class="generator-grid">
          <div
            v-for="gen in generators"
            :key="gen.key"
            class="generator-item"
            @click="selectGenerator(gen)"
          >
            <div class="generator-icon">{{ gen.icon }}</div>
            <div class="generator-info">
              <div class="generator-name">{{ gen.name }}</div>
              <div class="generator-desc">{{ gen.desc }}</div>
            </div>
            <div class="generator-preview">
              <code>{{ getPreview(gen) }}</code>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 参数设置弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="设置参数"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="currentParams" label-width="120px" size="small">
        <el-form-item
          v-for="param in currentGenerator?.params"
          :key="param.key"
          :label="param.name"
        >
          <el-input-number
            v-if="typeof param.default === 'number'"
            v-model="currentParams[param.key]"
            :min="param.min"
            :max="param.max"
            :step="1"
            style="width: 100%"
          />
          <el-input
            v-else-if="param.type === 'string'"
            v-model="currentParams[param.key]"
            :placeholder="param.desc"
            style="width: 100%"
          />
          <span v-else>{{ param.default }}</span>
          <div v-if="param.desc" class="param-desc">{{ param.desc }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmParams">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  getAllGenerators,
  getGeneratorsByCategory,
  generateTemplate,
  getDefaultParams
} from '@/utils/dataGenerator'

const emit = defineEmits(['insert'])

const activeCategory = ref('字符串')
const dialogVisible = ref(false)
const currentGenerator = ref(null)
const currentParams = ref({})

const generatorsByCategory = ref(getGeneratorsByCategory())

const selectGenerator = (generator) => {
  currentGenerator.value = generator
  if (generator.params && generator.params.length > 0) {
    // 有参数，显示参数设置弹窗
    currentParams.value = { ...getDefaultParams(generator.key) }
    dialogVisible.value = true
  } else {
    // 无参数，直接插入
    insertTemplate(generator)
  }
}

const getPreview = (generator) => {
  return generator.template(getDefaultParams(generator.key))
}

const confirmParams = () => {
  if (currentGenerator.value) {
    insertTemplate(currentGenerator.value, currentParams.value)
    dialogVisible.value = false
  }
}

const insertTemplate = (generator, params = {}) => {
  const template = generator.template(params)
  emit('insert', template)
}
</script>

<style scoped>
.data-generator-panel {
  padding: 20px;
  background: var(--el-fill-color-blank);
  border-radius: 8px;
  border: 1px solid var(--el-border-color);
}

.panel-header {
  margin-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color);
  padding-bottom: 15px;
}

.panel-header h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.panel-desc {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.generator-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
}

.generator-item {
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 12px;
}

.generator-item:hover {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.generator-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  flex-shrink: 0;
}

.generator-info {
  flex: 1;
  min-width: 0;
}

.generator-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--el-text-color-primary);
}

.generator-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.generator-preview {
  flex-shrink: 0;
}

.generator-preview code {
  display: block;
  background: var(--el-fill-color-light);
  padding: 6px 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: var(--el-color-primary);
  white-space: nowrap;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.param-desc {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>
