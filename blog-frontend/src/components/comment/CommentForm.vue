<template>
  <div class="comment-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      @submit.prevent="handleSubmit"
    >
      <el-form-item prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :placeholder="placeholder"
          :rows="3"
          resize="none"
          maxlength="1000"
          show-word-limit
          :disabled="loading"
        />
      </el-form-item>

      <div class="comment-form-actions">
        <el-button
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ submitText }}
        </el-button>
        <el-button
          v-if="showCancel"
          @click="handleCancel"
        >
          取消
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { CreateCommentRequest } from '@shared/types'
import { useUserStore } from '@/stores/user'
import { useCommentStore } from '@/stores/commentStore'

interface Props {
  articleId: number
  parentId?: number
  placeholder?: string
  submitText?: string
  showCancel?: boolean
  autoFocus?: boolean
}

interface Emits {
  (e: 'success'): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '写下你的评论...',
  submitText: '发表评论',
  showCancel: false,
  autoFocus: false
})

const emit = defineEmits<Emits>()

const userStore = useUserStore()
const commentStore = useCommentStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<CreateCommentRequest>({
  content: '',
  articleId: props.articleId,
  parentId: props.parentId
})

const rules: FormRules = {
  content: [
    { required: true, message: '评论内容不能为空', trigger: 'blur' },
    { min: 1, max: 1000, message: '评论内容长度在1到1000个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后再发表评论')
      return
    }

    loading.value = true

    await commentStore.createComment(form)

    ElMessage.success('评论发表成功')

    // 重置表单
    form.content = ''
    formRef.value.resetFields()

    emit('success')
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  form.content = ''
  formRef.value?.resetFields()
  emit('cancel')
}

// 暴露方法给父组件
defineExpose({
  focus: () => {
    // 如果需要自动聚焦，可以在这里实现
  },
  reset: () => {
    form.content = ''
    formRef.value?.resetFields()
  }
})
</script>

<style scoped>
.comment-form {
  margin-bottom: 20px;
}

.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

:deep(.el-textarea__inner) {
  resize: none;
  line-height: 1.6;
}
</style>