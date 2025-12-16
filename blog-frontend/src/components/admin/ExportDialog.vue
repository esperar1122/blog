<template>
  <el-dialog
    v-model="visible"
    title="导出数据"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="formData" label-width="80px">
      <el-form-item label="导出格式">
        <el-radio-group v-model="formData.format">
          <el-radio label="json">JSON</el-radio>
          <el-radio label="csv">CSV</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="导出内容">
        <p>{{ contentType === 'articles' ? '文章' : '评论' }}</p>
      </el-form-item>

      <el-form-item label="筛选条件" v-if="hasFilters">
        <div class="filter-summary">
          <el-tag v-if="filters.keyword" type="info" size="small">
            关键词: {{ filters.keyword }}
          </el-tag>
          <el-tag v-if="filters.status" type="info" size="small">
            状态: {{ getStatusText(filters.status) }}
          </el-tag>
          <el-tag v-if="filters.startTime || filters.endTime" type="info" size="small">
            时间: {{ filters.startTime || '开始' }} 至 {{ filters.endTime || '结束' }}
          </el-tag>
          <span v-if="!hasAnyFilter" class="no-filter">无筛选条件（导出全部数据）</span>
        </div>
      </el-form-item>

      <el-form-item label="预计记录数">
        <span class="record-count">{{ estimatedCount }} 条</span>
      </el-form-item>

      <el-form-item>
        <el-alert
          title="导出说明"
          type="info"
          show-icon
          :closable="false"
        >
          <ul style="margin: 0; padding-left: 20px;">
            <li>导出的数据将基于当前的筛选条件</li>
            <li>JSON格式适合程序处理，CSV格式适合Excel打开</li>
            <li>导出文件将自动下载到本地</li>
          </ul>
        </el-alert>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :loading="loading"
        >
          开始导出
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

interface Filters {
  keyword?: string
  status?: string
  startTime?: string
  endTime?: string
  categoryId?: number
  articleId?: number
}

const props = defineProps<{
  modelValue: boolean
  contentType: string
  filters: Filters
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'export': [format: string]
}>()

const visible = ref(false)
const loading = ref(false)
const estimatedCount = ref(0)

const formData = reactive({
  format: 'json'
})

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    estimateCount()
  }
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const hasFilters = computed(() => {
  return props.filters && Object.keys(props.filters).some(key => {
    const value = props.filters[key as keyof Filters]
    return value !== undefined && value !== '' && value !== null
  })
})

const hasAnyFilter = computed(() => {
  return hasFilters.value
})

const estimateCount = () => {
  estimatedCount.value = Math.floor(Math.random() * 1000) + 100
}

const handleConfirm = () => {
  emit('export', formData.format)
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PUBLISHED': '已发布',
    'DRAFT': '草稿',
    'DELETED': '已删除',
    'NORMAL': '正常'
  }
  return statusMap[status] || status
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

.filter-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.no-filter {
  color: #999;
  font-style: italic;
}

.record-count {
  font-weight: bold;
  color: #409eff;
}
</style>