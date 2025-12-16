<template>
  <el-dialog
    v-model="visible"
    title="文章审核"
    width="800px"
    :close-on-click-modal="false"
    @open="handleOpen"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      v-if="formData"
    >
      <el-form-item label="文章标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入文章标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="文章摘要" prop="summary">
        <el-input
          v-model="formData.summary"
          type="textarea"
          :rows="3"
          placeholder="请输入文章摘要"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="文章状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="发布" value="PUBLISHED" />
          <el-option label="删除" value="DELETED" />
        </el-select>
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="formData.categoryId" placeholder="请选择分类">
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="文章内容" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="12"
          placeholder="请输入文章内容"
          maxlength="10000"
          show-word-limit
          @input="checkSensitiveWords"
        />
      </el-form-item>

      <el-form-item label="审核备注">
        <el-input
          v-model="formData.reviewNote"
          type="textarea"
          :rows="2"
          placeholder="请输入审核备注"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <el-alert
          v-if="sensitiveWordWarning"
          title="内容检测到敏感词，请检查后重新提交"
          type="warning"
          show-icon
          :closable="false"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button @click="handleCheckContent">检测敏感词</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :loading="loading"
          :disabled="sensitiveWordWarning"
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
import { useRouter } from 'vue-router'

interface Article {
  id: number
  title: string
  content: string
  summary?: string
  coverImage?: string
  status: string
  categoryId?: number
  categoryName?: string
  authorName?: string
}

interface Category {
  id: number
  name: string
}

const props = defineProps<{
  modelValue: boolean
  article: Article | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const router = useRouter()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const categories = ref<Category[]>([])
const sensitiveWordWarning = ref(false)

const formData = ref<Partial<Article> | null>(null)

const rules: FormRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应在 5 到 100 个字符之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择文章状态', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { min: 10, message: '内容至少需要 10 个字符', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const handleOpen = async () => {
  if (props.article) {
    formData.value = { ...props.article }
    await loadCategories()
    checkSensitiveWords()
  }
}

const loadCategories = async () => {
  try {
    const response = await router.resolve('/api/categories')
    if (response.meta) {
      const mockCategories: Category[] = [
        { id: 1, name: '技术' },
        { id: 2, name: '生活' },
        { id: 3, name: '随笔' },
        { id: 4, name: '其他' }
      ]
      categories.value = mockCategories
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    const mockCategories: Category[] = [
      { id: 1, name: '技术' },
      { id: 2, name: '生活' },
      { id: 3, name: '随笔' },
      { id: 4, name: '其他' }
    ]
    categories.value = mockCategories
  }
}

const checkSensitiveWords = async () => {
  if (!formData.value?.content) {
    sensitiveWordWarning.value = false
    return
  }

  try {
    const response = await adminContentService.checkContent(formData.value.content)
    sensitiveWordWarning.value = response.data
  } catch (error) {
    console.error('检测敏感词失败:', error)
    sensitiveWordWarning.value = false
  }
}

const handleCheckContent = async () => {
  if (!formData.value?.content) {
    ElMessage.warning('请先输入内容')
    return
  }

  try {
    const response = await adminContentService.checkContent(formData.value.content)
    if (response.data) {
      ElMessage.warning('内容包含敏感词，请修改后重新提交')
    } else {
      ElMessage.success('内容检测通过，无敏感词')
    }
  } catch (error) {
    ElMessage.error('内容检测失败')
    console.error('检测敏感词错误:', error)
  }
}

const handleConfirm = async () => {
  if (!formRef.value || !formData.value) return

  try {
    await formRef.value.validate()

    loading.value = true

    await adminContentService.reviewArticle({
      id: formData.value.id!,
      title: formData.value.title!,
      content: formData.value.content!,
      summary: formData.value.summary,
      coverImage: formData.value.coverImage,
      status: formData.value.status!,
      categoryId: formData.value.categoryId,
      reviewNote: formData.value.reviewNote
    })

    ElMessage.success('文章审核成功')
    emit('success')
  } catch (error) {
    ElMessage.error('文章审核失败')
    console.error('文章审核错误:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  formData.value = null
  sensitiveWordWarning.value = false
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

:deep(.el-textarea__inner) {
  resize: vertical;
}
</style>