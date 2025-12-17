<template>
  <el-dialog
    v-model="visible"
    title="内容敏感词测试"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form :model="testData" label-width="80px">
      <el-form-item label="测试内容">
        <el-input
          v-model="testData.content"
          type="textarea"
          :rows="6"
          placeholder="请输入要测试的内容"
          maxlength="500"
          show-word-limit
          @input="handleTest"
        />
      </el-form-item>

      <el-form-item label="测试结果" v-if="testResult !== null">
        <div class="test-result">
          <el-tag :type="testResult ? 'danger' : 'success'" size="large">
            <el-icon v-if="testResult"><WarningFilled /></el-icon>
            <el-icon v-else><SuccessFilled /></el-icon>
            {{ testResult ? '检测到敏感词' : '内容安全，未检测到敏感词' }}
          </el-tag>
        </div>
      </el-form-item>

      <el-form-item label="过滤后内容" v-if="filteredContent !== testData.content">
        <div class="filtered-content">
          {{ filteredContent }}
        </div>
      </el-form-item>

      <el-form-item label="批量测试" v-if="batchTestResults.length > 0">
        <div class="batch-results">
          <div
            v-for="(result, index) in batchTestResults"
            :key="index"
            class="batch-item"
          >
            <div class="batch-input">{{ result.input }}</div>
            <div class="batch-output">
              <el-tag :type="result.hasSensitive ? 'danger' : 'success'" size="small">
                {{ result.hasSensitive ? '敏感' : '安全' }}
              </el-tag>
              <span class="filtered">{{ result.filtered }}</span>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button @click="runBatchTest">运行批量测试</el-button>
        <el-button @click="clearResults">清除结果</el-button>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled, SuccessFilled } from '@element-plus/icons-vue'
import adminContentService from '@/services/adminContentService'

interface TestResult {
  input: string
  hasSensitive: boolean
  filtered: string
}

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const visible = ref(false)
const testResult = ref<boolean | null>(null)
const filteredContent = ref('')
const batchTestResults = ref<TestResult[]>([])

const testData = reactive({
  content: ''
})

const batchTestCases = [
  '这是一个正常的测试内容',
  '这里包含一些敏感词汇测试',
  '测试正则表达式匹配功能',
  '正常的文章内容，没有任何问题',
  '包含多个敏感词的内容测试用例'
]

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
  if (!newVal) {
    clearResults()
  }
})

const handleTest = async () => {
  if (!testData.content.trim()) {
    testResult.value = null
    filteredContent.value = ''
    return
  }

  try {
    const [checkResponse, filterResponse] = await Promise.all([
      adminContentService.checkContent(testData.content),
      adminContentService.filterContent(testData.content)
    ])

    testResult.value = checkResponse.data
    filteredContent.value = filterResponse.data
  } catch (error) {
    ElMessage.error('测试失败')
    console.error('测试敏感词错误:', error)
    testResult.value = null
    filteredContent.value = ''
  }
}

const runBatchTest = async () => {
  try {
    ElMessage.info('正在运行批量测试...')
    batchTestResults.value = []

    for (const testCase of batchTestCases) {
      try {
        const [checkResponse, filterResponse] = await Promise.all([
          adminContentService.checkContent(testCase),
          adminContentService.filterContent(testCase)
        ])

        batchTestResults.value.push({
          input: testCase,
          hasSensitive: checkResponse.data,
          filtered: filterResponse.data
        })
      } catch (error) {
        batchTestResults.value.push({
          input: testCase,
          hasSensitive: false,
          filtered: '测试失败'
        })
      }
    }

    ElMessage.success('批量测试完成')
  } catch (error) {
    ElMessage.error('批量测试失败')
    console.error('批量测试错误:', error)
  }
}

const clearResults = () => {
  testData.content = ''
  testResult.value = null
  filteredContent.value = ''
  batchTestResults.value = []
}

const handleClose = () => {
  visible.value = false
  emit('success')
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

.test-result {
  text-align: center;
  padding: 20px 0;
}

.filtered-content {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  line-height: 1.5;
  white-space: pre-wrap;
}

.batch-results {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.batch-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.batch-item:last-child {
  border-bottom: none;
}

.batch-input {
  font-size: 14px;
  margin-bottom: 4px;
  color: #606266;
}

.batch-output {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filtered {
  flex: 1;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>