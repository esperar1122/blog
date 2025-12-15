<template>
  <div class="article-editor-page">
    <div class="page-header">
      <h1>{{ isEdit ? '编辑文章' : '创建文章' }}</h1>
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="editor-wrapper">
      <ArticleEditor
        :initial-data="initialData"
        :mode="isEdit ? 'edit' : 'create'"
        @save="handleSave"
        @publish="handlePublish"
        @content-change="handleContentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import ArticleEditor from '@/components/article/ArticleEditor.vue'
import { useArticleStore } from '@/stores/article'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const isEdit = ref(false)
const initialData = ref<any>({})

// 处理保存
const handleSave = async (data: any) => {
  try {
    if (isEdit.value) {
      ElMessage.success('文章更新成功')
    } else {
      ElMessage.success('文章保存成功')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

// 处理发布
const handlePublish = async (data: any) => {
  try {
    ElMessage.success('文章发布成功')
    // 跳转到文章列表或文章详情页
    router.push('/articles')
  } catch (error: any) {
    ElMessage.error(error.message || '发布失败')
  }
}

// 处理内容变化
const handleContentChange = (content: string) => {
  // 可以在这里添加实时保存到 local storage 的逻辑
  localStorage.setItem('article_draft', JSON.stringify({
    ...initialData.value,
    content,
    lastUpdate: new Date().toISOString()
  }))
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 初始化
onMounted(async () => {
  const articleId = route.params.id as string

  if (articleId) {
    isEdit.value = true

    try {
      // 加载文章数据
      const article = await articleStore.fetchArticle(articleId)
      initialData.value = {
        id: article.id,
        title: article.title,
        content: article.content,
        summary: article.summary,
        coverImage: article.coverImage,
        categoryId: article.categoryId,
        tagIds: article.tags?.map((tag: any) => tag.id) || [],
        status: article.status
      }
    } catch (error: any) {
      ElMessage.error('加载文章失败：' + error.message)
      router.push('/articles')
    }
  } else {
    // 检查是否有本地草稿
    const draft = localStorage.getItem('article_draft')
    if (draft) {
      try {
        const draftData = JSON.parse(draft)
        const lastUpdate = new Date(draftData.lastUpdate)
        const now = new Date()
        const diffInHours = (now.getTime() - lastUpdate.getTime()) / (1000 * 60 * 60)

        if (diffInHours < 24) { // 24小时内的草稿
          initialData.value = draftData
        }
      } catch (e) {
        // 忽略解析错误
      }
    }
  }
})

// 页面卸载时清理
onUnmounted(() => {
  // 可选：询问用户是否要保存草稿
})
</script>

<style scoped>
.article-editor-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.editor-wrapper {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>