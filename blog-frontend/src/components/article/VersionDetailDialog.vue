<template>
  <el-dialog
    v-model="visible"
    :title="`版本详情 - v${version?.versionNumber}`"
    width="800px"
    class="version-detail-dialog"
  >
    <div v-if="version" class="version-content">
      <div class="version-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="版本号">
            <el-tag type="primary">v{{ version.versionNumber }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(version.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="编辑者">
            <div class="editor-info">
              <el-avatar :size="24" :src="version.editorAvatar" />
              <span>{{ version.editorName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="变更原因">
            {{ version.changeReason || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="version-details">
        <div class="detail-section">
          <h4>标题</h4>
          <div class="title-content">{{ version.title }}</div>
        </div>

        <div class="detail-section" v-if="version.summary">
          <h4>摘要</h4>
          <div class="summary-content">{{ version.summary }}</div>
        </div>

        <div class="detail-section" v-if="version.coverImage">
          <h4>封面图片</h4>
          <el-image
            :src="version.coverImage"
            fit="cover"
            class="cover-image"
            :preview-src-list="[version.coverImage]"
          />
        </div>

        <div class="detail-section">
          <h4>内容预览</h4>
          <div class="content-preview">{{ version.content.substring(0, 200) }}...</div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleRestore">恢复此版本</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElDialog, ElDescriptions, ElDescriptionsItem, ElTag, ElAvatar, ElImage, ElButton } from 'element-plus'
import type { ArticleVersion } from '@/types/article'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  version: {
    type: Object as PropType<ArticleVersion | null>,
    default: null
  }
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'restore': [version: ArticleVersion]
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

function handleRestore() {
  if (props.version) {
    emit('restore', props.version)
    handleClose()
  }
}
</script>

<style scoped>
.version-detail-dialog :deep(.el-dialog__body) {
  max-height: 600px;
  overflow-y: auto;
}

.version-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.version-info {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}

.editor-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.title-content {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
}

.summary-content {
  color: #4b5563;
  line-height: 1.6;
  background: #f9fafb;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #3b82f6;
}

.cover-image {
  width: 200px;
  height: 120px;
  border-radius: 8px;
}

.content-preview {
  color: #6b7280;
  line-height: 1.6;
  font-family: monospace;
  background: #f3f4f6;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}
</style>