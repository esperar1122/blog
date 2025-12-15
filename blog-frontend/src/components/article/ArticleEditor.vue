<template>
  <div class="article-editor">
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
      <!-- 标题输入 -->
      <el-form-item label="文章标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入文章标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 文章摘要 -->
      <el-form-item label="文章摘要" prop="summary">
        <el-input
          v-model="formData.summary"
          type="textarea"
          placeholder="请输入文章摘要"
          maxlength="500"
          show-word-limit
          :rows="3"
        />
      </el-form-item>

      <!-- 分类选择 -->
      <el-form-item label="文章分类" prop="categoryId">
        <el-select
          v-model="formData.categoryId"
          placeholder="请选择分类"
          style="width: 200px"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>

      <!-- 标签选择 -->
      <el-form-item label="文章标签" prop="tagIds">
        <el-select
          v-model="formData.tagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请选择或创建标签"
          style="width: 100%"
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </el-form-item>

      <!-- 封面图片 -->
      <el-form-item label="封面图片">
        <el-upload
          class="cover-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleCoverSuccess"
          :before-upload="beforeCoverUpload"
        >
          <img v-if="formData.coverImage" :src="formData.coverImage" class="cover-image" />
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <!-- Markdown 编辑器 -->
      <el-form-item label="文章内容" prop="content">
        <div class="editor-container">
          <!-- 编辑模式切换 -->
          <div class="editor-toolbar">
            <el-button-group>
              <el-button
                :type="editMode === 'edit' ? 'primary' : ''"
                @click="editMode = 'edit'"
              >
                编辑
              </el-button>
              <el-button
                :type="editMode === 'preview' ? 'primary' : ''"
                @click="editMode = 'preview'"
              >
                预览
              </el-button>
              <el-button
                :type="editMode === 'split' ? 'primary' : ''"
                @click="editMode = 'split'"
              >
                分屏
              </el-button>
            </el-button-group>

            <div class="editor-actions">
              <el-button size="small" @click="insertImage">
                <el-icon><Picture /></el-icon>
                插入图片
              </el-button>
              <el-button size="small" @click="insertLink">
                <el-icon><Link /></el-icon>
                插入链接
              </el-button>
            </div>
          </div>

          <!-- 编辑器内容区 -->
          <div class="editor-content" :class="`mode-${editMode}`">
            <!-- 编辑区 -->
            <div v-show="editMode === 'edit' || editMode === 'split'" class="editor-pane">
              <el-input
                v-model="formData.content"
                type="textarea"
                placeholder="请输入 Markdown 内容..."
                :rows="20"
                @input="handleContentChange"
                ref="textareaRef"
              />
            </div>

            <!-- 预览区 -->
            <div v-show="editMode === 'preview' || editMode === 'split'" class="preview-pane">
              <div class="markdown-preview" v-html="previewHtml"></div>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" @click="saveDraft" :loading="saving">
          保存草稿
        </el-button>
        <el-button type="success" @click="publishArticle" :loading="publishing">
          发布文章
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 自动保存提示 -->
    <div v-if="autoSaveStatus" class="auto-save-status">
      <el-tag :type="autoSaveStatus.type" size="small">
        {{ autoSaveStatus.message }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, Link } from '@element-plus/icons-vue'
import { useArticleStore } from '@/stores/article'
import { useUserStore } from '@/stores/user'
import type { FormInstance, UploadProps } from 'element-plus'

// Props
interface Props {
  initialData?: {
    id?: number
    title?: string
    content?: string
    summary?: string
    coverImage?: string
    categoryId?: number
    tagIds?: number[]
    status?: string
  }
  mode?: 'create' | 'edit'
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'create'
})

// Emits
const emit = defineEmits<{
  save: [data: any]
  publish: [data: any]
  contentChange: [content: string]
}>()

// Stores
const articleStore = useArticleStore()
const userStore = useUserStore()

// Refs
const formRef = ref<FormInstance>()
const textareaRef = ref<HTMLTextAreaElement>()

// Data
const editMode = ref<'edit' | 'preview' | 'split'>('edit')
const saving = ref(false)
const publishing = ref(false)
const autoSaveTimer = ref<number | null>(null)
const autoSaveStatus = ref<{ type: 'success' | 'warning' | 'info'; message: string } | null>(null)

// 表单数据
const formData = reactive({
  title: props.initialData?.title || '',
  content: props.initialData?.content || '',
  summary: props.initialData?.summary || '',
  coverImage: props.initialData?.coverImage || '',
  categoryId: props.initialData?.categoryId || undefined,
  tagIds: props.initialData?.tagIds || []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 200, message: '标题不能超过200个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
  ],
  summary: [
    { max: 500, message: '摘要不能超过500个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择文章分类', trigger: 'change' }
  ]
}

// 上传配置
const uploadUrl = computed(() => '/api/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 分类和标签数据
const categories = computed(() => articleStore.categories)
const tags = computed(() => articleStore.tags)

// Markdown 预览
const previewHtml = computed(() => {
  // 简单的 Markdown 转 HTML 实现
  // 在实际项目中，应该使用 marked.js 或其他 Markdown 解析库
  if (!formData.content) return ''

  return formData.content
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/!\[(.*?)\]\((.*?)\)/g, '<img src="$2" alt="$1" style="max-width: 100%;" />')
    .replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/\n/g, '<br>')
})

// 方法
const handleContentChange = () => {
  emit('contentChange', formData.content)
  startAutoSave()
}

const saveDraft = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    saving.value = true
    const articleData = {
      ...formData,
      status: 'DRAFT'
    }

    if (props.mode === 'edit' && props.initialData?.id) {
      await articleStore.updateArticle(props.initialData.id, articleData)
    } else {
      await articleStore.createArticle(articleData)
    }

    showAutoSaveStatus('success', '草稿保存成功')
    emit('save', articleData)
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const publishArticle = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    await ElMessageBox.confirm('确定要发布这篇文章吗？', '确认发布', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    publishing.value = true
    const articleData = {
      ...formData,
      status: 'PUBLISHED'
    }

    if (props.mode === 'edit' && props.initialData?.id) {
      await articleStore.updateArticle(props.initialData.id, articleData)
      await articleStore.publishArticle(props.initialData.id)
    } else {
      const article = await articleStore.createArticle(articleData)
      await articleStore.publishArticle(article.id)
    }

    ElMessage.success('文章发布成功')
    emit('publish', articleData)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '发布失败')
    }
  } finally {
    publishing.value = false
  }
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}

const insertImage = () => {
  const imageUrl = prompt('请输入图片URL：')
  if (imageUrl) {
    insertTextAtCursor(`![图片描述](${imageUrl})`)
  }
}

const insertLink = () => {
  const url = prompt('请输入链接URL：')
  const text = prompt('请输入链接文字：')
  if (url && text) {
    insertTextAtCursor(`[${text}](${url})`)
  }
}

const insertTextAtCursor = (text: string) => {
  if (!textareaRef.value) return

  const textarea = textareaRef.value.$el.querySelector('textarea') as HTMLTextAreaElement
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const currentValue = formData.content

  formData.content = currentValue.substring(0, start) + text + currentValue.substring(end)

  // 重新聚焦并设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + text.length, start + text.length)
  }, 0)
}

const handleCoverSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    formData.coverImage = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const beforeCoverUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！')
  }

  return isImage && isLt10M
}

// 自动保存功能
const startAutoSave = () => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }

  autoSaveTimer.value = window.setTimeout(() => {
    autoSave()
  }, 300000) // 5分钟自动保存
}

const autoSave = async () => {
  if (!formData.title && !formData.content) return

  try {
    const articleData = {
      ...formData,
      status: 'DRAFT'
    }

    if (props.mode === 'edit' && props.initialData?.id) {
      await articleStore.updateArticle(props.initialData.id, articleData)
    } else {
      await articleStore.createArticle(articleData)
    }

    showAutoSaveStatus('success', '自动保存成功')
  } catch (error: any) {
    showAutoSaveStatus('warning', '自动保存失败')
  }
}

const showAutoSaveStatus = (type: 'success' | 'warning' | 'info', message: string) => {
  autoSaveStatus.value = { type, message }
  setTimeout(() => {
    autoSaveStatus.value = null
  }, 3000)
}

// 生命周期
onMounted(async () => {
  // 加载分类和标签数据
  await Promise.all([
    articleStore.fetchCategories(),
    articleStore.fetchTags()
  ])

  // 开始自动保存
  startAutoSave()
})

onUnmounted(() => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
})
</script>

<style scoped>
.article-editor {
  position: relative;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.cover-uploader:hover {
  border-color: var(--el-color-primary);
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.cover-image {
  width: 178px;
  height: 178px;
  object-fit: cover;
  display: block;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.editor-content {
  display: flex;
  min-height: 500px;
}

.editor-content.mode-edit .editor-pane {
  width: 100%;
}

.editor-content.mode-preview .preview-pane {
  width: 100%;
}

.editor-content.mode-split .editor-pane,
.editor-content.mode-split .preview-pane {
  width: 50%;
}

.editor-pane {
  border-right: 1px solid #dcdfe6;
}

.editor-pane :deep(.el-textarea) {
  height: 100%;
}

.editor-pane :deep(.el-textarea__inner) {
  height: 100%;
  border: none;
  resize: none;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.preview-pane {
  padding: 20px;
  background-color: #fff;
  overflow-y: auto;
}

.markdown-preview {
  line-height: 1.6;
}

.markdown-preview h1 {
  font-size: 2em;
  margin: 0.67em 0;
  font-weight: bold;
}

.markdown-preview h2 {
  font-size: 1.5em;
  margin: 0.75em 0;
  font-weight: bold;
}

.markdown-preview h3 {
  font-size: 1.17em;
  margin: 0.83em 0;
  font-weight: bold;
}

.auto-save-status {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 100;
}
</style>