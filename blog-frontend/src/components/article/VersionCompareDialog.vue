<template>
  <el-dialog
    v-model="visible"
    title="版本对比"
    width="1000px"
    class="version-compare-dialog"
  >
    <div v-if="version1 && version2" class="compare-content">
      <div class="compare-header">
        <div class="version-info">
          <h4>版本 {{ version2.versionNumber }} (旧)</h4>
          <p>{{ formatTime(version2.createTime) }} - {{ version2.editorName }}</p>
        </div>
        <el-icon class="compare-arrow"><ArrowRight /></el-icon>
        <div class="version-info">
          <h4>版本 {{ version1.versionNumber }} (新)</h4>
          <p>{{ formatTime(version1.createTime) }} - {{ version1.editorName }}</p>
        </div>
      </div>

      <div class="compare-section">
        <h4>标题</h4>
        <div class="compare-item">
          <div class="old-version">{{ version2.title }}</div>
          <div class="new-version">{{ version1.title }}</div>
        </div>
      </div>

      <div class="compare-section">
        <h4>摘要</h4>
        <div class="compare-item">
          <div class="old-version">{{ version2.summary || '无摘要' }}</div>
          <div class="new-version">{{ version1.summary || '无摘要' }}</div>
        </div>
      </div>

      <div class="compare-section">
        <h4>内容对比</h4>
        <div class="compare-item content-compare">
          <div class="old-version">{{ version2.content }}</div>
          <div class="new-version">{{ version1.content }}</div>
        </div>
      </div>
    </div>

    <div v-else-if="version1" class="single-version">
      <div class="version-info">
        <h4>版本 {{ version1.versionNumber }}</h4>
        <p>{{ formatTime(version1.createTime) }} - {{ version1.editorName }}</p>
      </div>

      <div class="version-details">
        <div class="detail-section">
          <h4>标题</h4>
          <div class="title-content">{{ version1.title }}</div>
        </div>
        <div class="detail-section" v-if="version1.summary">
          <h4>摘要</h4>
          <div class="summary-content">{{ version1.summary }}</div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElDialog, ElIcon } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import type { ArticleVersion } from '@/types/article'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  version1: {
    type: Object as PropType<ArticleVersion | null>,
    default: null
  },
  version2: {
    type: Object as PropType<ArticleVersion | null>,
    default: null
  }
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

function formatTime(time: string): string {
  return new Date(time).toLocaleString()
}

function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.version-compare-dialog :deep(.el-dialog__body) {
  max-height: 700px;
  overflow-y: auto;
}

.compare-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.compare-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  padding: 20px;
  border-radius: 8px;
}

.version-info {
  text-align: center;
  flex: 1;
}

.version-info h4 {
  margin: 0 0 4px 0;
  color: #1f2937;
  font-size: 16px;
}

.version-info p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.compare-arrow {
  font-size: 24px;
  color: #3b82f6;
  margin: 0 16px;
}

.compare-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.compare-section h4 {
  margin: 0;
  padding: 12px 16px;
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.compare-item {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: #e5e7eb;
}

.old-version, .new-version {
  background: #fff;
  padding: 16px;
}

.old-version {
  background: #fef2f2;
}

.new-version {
  background: #f0fdf4;
}

.content-compare .old-version,
.content-compare .new-version {
  max-height: 300px;
  overflow-y: auto;
  font-family: monospace;
  white-space: pre-wrap;
  line-height: 1.6;
  font-size: 13px;
}

.single-version {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.single-version .version-info {
  text-align: center;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}

.single-version .version-info h4 {
  margin: 0 0 4px 0;
  color: #1f2937;
  font-size: 16px;
}

.single-version .version-info p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.version-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.title-content {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.summary-content {
  color: #4b5563;
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  border-left: 4px solid #3b82f6;
}

@media (max-width: 768px) {
  .compare-header {
    flex-direction: column;
    gap: 16px;
  }

  .compare-arrow {
    transform: rotate(90deg);
    margin: 0;
  }

  .compare-item {
    grid-template-columns: 1fr;
  }
}
</style>