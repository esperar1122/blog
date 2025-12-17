<template>
  <div class="sensitive-word-manager">
    <!-- Header Actions -->
    <el-card class="action-card">
      <div class="action-header">
        <div class="filter-section">
          <el-select
            v-model="typeFilter"
            placeholder="筛选类型"
            style="width: 150px"
            @change="handleFilter"
          >
            <el-option label="全部" value="" />
            <el-option label="过滤词" value="FILTER" />
            <el-option label="屏蔽词" value="BLOCK" />
            <el-option label="警告词" value="WARNING" />
          </el-select>
        </div>
        <div class="action-buttons">
          <el-button @click="handleTestFilter">测试过滤</el-button>
          <el-button type="primary" @click="handleAddWord">添加敏感词</el-button>
        </div>
      </div>
    </el-card>

    <!-- Words Table -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="filteredWords">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" width="150">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.word }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replacement" label="替换词" width="150">
          <template #default="{ row }">
            <span v-if="row.replacement">{{ row.replacement }}</span>
            <span v-else class="no-replacement">无替换</span>
          </template>
        </el-table-column>
        <el-table-column prop="pattern" label="匹配模式" min-width="200">
          <template #default="{ row }">
            <el-code>{{ row.pattern }}</el-code>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditWord(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteWord(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredWords.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无敏感词数据" />
      </div>
    </el-card>

    <!-- Add/Edit Word Dialog -->
    <el-dialog
      v-model="wordDialogVisible"
      :title="isEdit ? '编辑敏感词' : '添加敏感词'"
      width="600px"
    >
      <el-form
        ref="wordFormRef"
        :model="wordForm"
        :rules="wordFormRules"
        label-width="100px"
      >
        <el-form-item label="敏感词" prop="word">
          <el-input
            v-model="wordForm.word"
            placeholder="请输入敏感词"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="wordForm.type" style="width: 100%">
            <el-option label="过滤词" value="FILTER" />
            <el-option label="屏蔽词" value="BLOCK" />
            <el-option label="警告词" value="WARNING" />
          </el-select>
        </el-form-item>
        <el-form-item label="替换词">
          <el-input
            v-model="wordForm.replacement"
            placeholder="替换后的内容（仅过滤词需要）"
          />
        </el-form-item>
        <el-form-item label="匹配模式" prop="pattern">
          <el-input
            v-model="wordForm.pattern"
            type="textarea"
            placeholder="正则表达式匹配模式"
            :rows="2"
          />
        </el-form-item>
        <el-form-item label="说明">
          <div class="type-description">
            <div v-if="wordForm.type === 'FILTER'">
              过滤词：将敏感内容替换为指定字符，评论可以正常发布
            </div>
            <div v-else-if="wordForm.type === 'BLOCK'">
              屏蔽词：包含此内容的评论将被阻止发布
            </div>
            <div v-else-if="wordForm.type === 'WARNING'">
              警告词：包含此内容时会给出警告，但仍可发布
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="wordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveWord">
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Test Filter Dialog -->
    <el-dialog
      v-model="testDialogVisible"
      title="敏感词过滤测试"
      width="700px"
    >
      <div class="test-section">
        <el-form label-width="100px">
          <el-form-item label="测试文本">
            <el-input
              v-model="testText"
              type="textarea"
              placeholder="请输入要测试的文本内容"
              :rows="4"
            />
          </el-form-item>
        </el-form>
        <div class="test-actions">
          <el-button type="primary" @click="handleTestFilterText">
            测试过滤
          </el-button>
          <el-button @click="testText = ''">清空</el-button>
        </div>
      </div>

      <div v-if="testResult" class="test-result">
        <h4>测试结果</h4>
        <div class="result-item">
          <label>原始文本:</label>
          <div class="original-text">{{ testOriginalText }}</div>
        </div>
        <div class="result-item">
          <label>过滤后文本:</label>
          <div class="filtered-text">{{ testResult.filteredText }}</div>
        </div>
        <div class="result-item">
          <label>包含屏蔽词:</label>
          <el-tag :type="testResult.containsBlocked ? 'danger' : 'success'">
            {{ testResult.containsBlocked ? '是' : '否' }}
          </el-tag>
        </div>
        <div v-if="testResult.warningWords.length > 0" class="result-item">
          <label>警告词:</label>
          <div class="warning-words">
            <el-tag
              v-for="word in testResult.warningWords"
              :key="word"
              type="warning"
              size="small"
              class="warning-tag"
            >
              {{ word }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import type { SensitiveWord, SensitiveWordRequest } from '@shared/types'

const commentModerationStore = useCommentModerationStore()

// Data
const loading = ref(false)
const typeFilter = ref('')
const sensitiveWords = ref<SensitiveWord[]>([])
const wordDialogVisible = ref(false)
const testDialogVisible = ref(false)
const isEdit = ref(false)
const testText = ref('')
const testOriginalText = ref('')
const testResult = ref<any>(null)

const wordFormRef = ref<FormInstance>()
const wordForm = reactive({
  id: null as number | null,
  word: '',
  replacement: '',
  pattern: '',
  type: 'FILTER'
})

const wordFormRules: FormRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  pattern: [
    { required: true, message: '请输入匹配模式', trigger: 'blur' }
  ]
}

// Computed
const filteredWords = computed(() => {
  if (!typeFilter.value) {
    return sensitiveWords.value
  }
  return sensitiveWords.value.filter(word => word.type === typeFilter.value)
})

// Methods
const fetchWords = async () => {
  loading.value = true
  try {
    await commentModerationStore.fetchSensitiveWords()
    sensitiveWords.value = commentModerationStore.sensitiveWords
  } catch (error) {
    ElMessage.error('获取敏感词失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  // Filter is handled by computed property
}

const handleAddWord = () => {
  isEdit.value = false
  wordForm.id = null
  wordForm.word = ''
  wordForm.replacement = ''
  wordForm.pattern = ''
  wordForm.type = 'FILTER'
  wordDialogVisible.value = true
}

const handleEditWord = (word: SensitiveWord) => {
  isEdit.value = true
  wordForm.id = word.id
  wordForm.word = word.word
  wordForm.replacement = word.replacement || ''
  wordForm.pattern = word.pattern
  wordForm.type = word.type
  wordDialogVisible.value = true
}

const confirmSaveWord = async () => {
  if (!wordFormRef.value) return

  try {
    await wordFormRef.value.validate()
  } catch (error) {
    return
  }

  try {
    const request: SensitiveWordRequest = {
      word: wordForm.word,
      replacement: wordForm.replacement || undefined,
      pattern: wordForm.pattern,
      type: wordForm.type as any
    }

    if (isEdit.value) {
      await commentModerationStore.updateSensitiveWord(wordForm.id!, request)
      ElMessage.success('更新敏感词成功')
    } else {
      await commentModerationStore.addSensitiveWord(request)
      ElMessage.success('添加敏感词成功')
    }

    wordDialogVisible.value = false
    await fetchWords()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新敏感词失败' : '添加敏感词失败')
  }
}

const handleDeleteWord = async (word: SensitiveWord) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除敏感词"${word.word}"吗？`,
      '确认删除',
      { type: 'warning' }
    )

    await commentModerationStore.deleteSensitiveWord(word.id)
    ElMessage.success('删除敏感词成功')
    await fetchWords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除敏感词失败')
    }
  }
}

const handleTestFilter = () => {
  testText.value = ''
  testResult.value = null
  testOriginalText.value = ''
  testDialogVisible.value = true
}

const handleTestFilterText = async () => {
  if (!testText.value.trim()) {
    ElMessage.warning('请输入测试文本')
    return
  }

  testOriginalText.value = testText.value

  try {
    const response = await commentModerationService.filterSensitiveWords(testText.value)
    if (response.success) {
      testResult.value = response.data
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    FILTER: 'warning',
    BLOCK: 'danger',
    WARNING: 'info'
  }
  return types[type] || 'info'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    FILTER: '过滤词',
    BLOCK: '屏蔽词',
    WARNING: '警告词'
  }
  return texts[type] || type
}

const formatDateTime = (date: Date | string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

// Lifecycle
onMounted(() => {
  fetchWords()
})
</script>

<style scoped>
.sensitive-word-manager {
  margin-top: 20px;
}

.action-card {
  margin-bottom: 20px;
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.table-card {
  margin-bottom: 20px;
}

.no-replacement {
  color: #909399;
  font-style: italic;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.type-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.test-section {
  margin-bottom: 20px;
}

.test-actions {
  text-align: center;
  margin-top: 16px;
}

.test-result {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.test-result h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.result-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.result-item label {
  width: 100px;
  font-weight: bold;
  color: #606266;
  flex-shrink: 0;
}

.result-item > div {
  flex: 1;
}

.original-text,
.filtered-text {
  line-height: 1.6;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.original-text {
  background-color: #f5f7fa;
}

.filtered-text {
  background-color: #e1f3d8;
}

.warning-words {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.warning-tag {
  margin: 0;
}
</style>