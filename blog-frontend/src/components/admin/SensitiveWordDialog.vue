<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑敏感词' : '添加敏感词'"
    width="500px"
    :close-on-click-modal="false"
    @open="handleOpen"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="敏感词" prop="word">
        <el-input
          v-model="formData.word"
          placeholder="请输入敏感词或正则表达式"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="类型" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio label="WORD">文本</el-radio>
          <el-radio label="REGEX">正则表达式</el-radio>
        </el-radio-group>
        <div class="form-tip">
          文本：精确匹配词汇；正则：支持正则表达式模式匹配
        </div>
      </el-form-item>

      <el-form-item label="级别" prop="level">
        <el-select v-model="formData.level" placeholder="请选择级别">
          <el-option label="低" value="LOW" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="高" value="HIGH" />
        </el-select>
        <div class="form-tip">
          低：一般词汇；中：常见敏感词；高：严重敏感词
        </div>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio label="ACTIVE">启用</el-radio>
          <el-radio label="INACTIVE">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="测试内容" v-if="formData.word">
        <el-input
          v-model="testContent"
          type="textarea"
          :rows="3"
          placeholder="输入测试内容，检测是否包含敏感词"
          @input="handleTest"
        />
        <div class="test-result" v-if="testResult !== null">
          <el-tag :type="testResult ? 'danger' : 'success'">
            {{ testResult ? '检测到敏感词' : '未检测到敏感词' }}
          </el-tag>
          <div class="filtered-content" v-if="filteredContent !== testContent">
            过滤后: {{ filteredContent }}
          </div>
        </div>
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
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import adminContentService from '@/services/adminContentService'
import type { SensitiveWord } from '@/services/adminContentService'

const props = defineProps<{
  modelValue: boolean
  word: SensitiveWord | null
  isEdit: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const testContent = ref('')
const testResult = ref<boolean | null>(null)
const filteredContent = ref('')

const formData = reactive<SensitiveWord>({
  word: '',
  type: 'WORD',
  level: 'MEDIUM',
  status: 'ACTIVE'
})

const rules: FormRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { min: 1, max: 100, message: '长度应在 1 到 100 个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择级别', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const handleOpen = () => {
  if (props.word && props.isEdit) {
    Object.assign(formData, props.word)
  } else {
    Object.assign(formData, {
      word: '',
      type: 'WORD',
      level: 'MEDIUM',
      status: 'ACTIVE'
    })
  }
  testContent.value = ''
  testResult.value = null
  filteredContent.value = ''
}

const handleTest = async () => {
  if (!testContent.value.trim() || !formData.word) {
    testResult.value = null
    filteredContent.value = ''
    return
  }

  try {
    const [checkResponse, filterResponse] = await Promise.all([
      adminContentService.checkContent(testContent.value),
      adminContentService.filterContent(testContent.value)
    ])

    testResult.value = checkResponse.data
    filteredContent.value = filterResponse.data
  } catch (error) {
    console.error('测试敏感词失败:', error)
    testResult.value = null
    filteredContent.value = ''
  }
}

const validateRegex = (pattern: string): boolean => {
  try {
    new RegExp(pattern)
    return true
  } catch {
    return false
  }
}

const handleConfirm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (formData.type === 'REGEX' && !validateRegex(formData.word)) {
      ElMessage.error('正则表达式格式不正确')
      return
    }

    loading.value = true

    if (props.isEdit && props.word) {
      await adminContentService.updateSensitiveWord(props.word.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await adminContentService.addSensitiveWord(formData)
      ElMessage.success('添加成功')
    }

    emit('success')
  } catch (error) {
    ElMessage.error(props.isEdit ? '更新失败' : '添加失败')
    console.error('操作失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  formRef.value?.resetFields()
  testContent.value = ''
  testResult.value = null
  filteredContent.value = ''
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.test-result {
  margin-top: 8px;
}

.filtered-content {
  margin-top: 4px;
  font-size: 12px;
  color: #666;
  background-color: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
}
</style>