<template>
  <el-dialog
    v-model="visible"
    title="定时发布设置"
    width="400px"
    :before-close="handleClose"
  >
    <div class="schedule-publish-content">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="发布时间" prop="scheduledTime">
          <el-date-picker
            v-model="form.scheduledTime"
            type="datetime"
            placeholder="选择发布时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :disabled-hours="disabledHours"
            :disabled-minutes="disabledMinutes"
          />
        </el-form-item>

        <el-form-item label="备注" prop="note">
          <el-input
            v-model="form.note"
            type="textarea"
            placeholder="可选：添加定时发布的备注"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-alert
            type="info"
            show-icon
            :closable="false"
          >
            <template #title>
              定时发布说明
            </template>
            <div class="alert-content">
              <p>• 文章将在指定时间自动发布</p>
              <p>• 发布时间必须晚于当前时间</p>
              <p>• 发布前可以随时取消定时设置</p>
            </div>
          </el-alert>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleConfirm"
        >
          确认设置
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, type PropType } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElDatePicker, ElInput, ElAlert, ElButton, ElMessage } from 'element-plus'
import type { Article } from '@/types/article'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  article: {
    type: Object as PropType<Article>,
    required: true
  }
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'confirm': [scheduledTime: string, note?: string]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const formRef = ref()

const form = reactive({
  scheduledTime: '',
  note: ''
})

const rules = {
  scheduledTime: [
    { required: true, message: '请选择发布时间', trigger: 'change' }
  ]
}

// 禁用过去的日期
function disabledDate(time: Date) {
  const now = new Date()
  return time.getTime() < now.getTime()
}

// 禁用过去的小时
function disabledHours() {
  const now = new Date()
  const selectedDate = new Date(form.scheduledTime)

  // 如果选择的是今天
  if (isSameDay(selectedDate, now)) {
    const currentHour = now.getHours()
    return Array.from({ length: currentHour + 1 }, (_, i) => i)
  }

  return []
}

// 禁用过去的分钟
function disabledMinutes(selectedHour: number) {
  const now = new Date()
  const selectedDate = new Date(form.scheduledTime)

  // 如果选择的是今天和当前小时
  if (isSameDay(selectedDate, now) && selectedHour === now.getHours()) {
    const currentMinute = now.getMinutes()
    return Array.from({ length: currentMinute }, (_, i) => i)
  }

  return []
}

// 判断是否是同一天
function isSameDay(date1: Date, date2: Date): boolean {
  return date1.getFullYear() === date2.getFullYear() &&
         date1.getMonth() === date2.getMonth() &&
         date1.getDate() === date2.getDate()
}

// 重置表单
function resetForm() {
  form.scheduledTime = ''
  form.note = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭对话框
function handleClose() {
  resetForm()
  visible.value = false
}

// 确认设置
async function handleConfirm() {
  try {
    await formRef.value.validate()

    // 验证发布时间是否在未来
    const scheduledTime = new Date(form.scheduledTime)
    const now = new Date()

    if (scheduledTime.getTime() <= now.getTime()) {
      ElMessage.error('发布时间必须在未来')
      return
    }

    loading.value = true

    // 模拟API调用延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    emit('confirm', form.scheduledTime, form.note)
    handleClose()
    ElMessage.success('定时发布设置成功')
  } catch (error) {
    console.error('设置定时发布失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听对话框打开/关闭
watch(visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})

// 监听文章变化，如果有已设置的定时发布时间则自动填充
watch(() => props.article.scheduledPublishTime, (newTime) => {
  if (newTime && visible.value) {
    form.scheduledTime = new Date(newTime).toISOString().slice(0, 19).replace('T', ' ')
  }
})
</script>

<style scoped>
.schedule-publish-content {
  padding: 20px 0;
}

.alert-content {
  margin-top: 8px;
}

.alert-content p {
  margin: 4px 0;
  color: #6b7280;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}

:deep(.el-alert__content) {
  width: 100%;
}

:deep(.el-textarea__inner) {
  resize: vertical;
}
</style>