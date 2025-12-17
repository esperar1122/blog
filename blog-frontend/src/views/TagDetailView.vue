<template>
  <div class="tag-detail-view">
    <!-- 标签信息头部 -->
    <el-card v-if="tag" class="tag-header-card">
      <div class="tag-header">
        <div class="tag-info">
          <div class="tag-icon-wrapper">
            <span
              class="tag-icon"
              :style="{ backgroundColor: tag.color }"
            ></span>
          </div>
          <div>
            <h1 class="tag-name">{{ tag.name }}</h1>
            <p class="tag-meta">
              共 {{ tag.articleCount }} 篇文章
              <span class="separator">·</span>
              创建于 {{ formatDate(tag.createTime) }}
            </p>
          </div>
        </div>

        <div class="tag-actions" v-if="userStore.isAdmin">
          <el-button type="primary" @click="handleEditTag">
            <el-icon><Edit /></el-icon>
            编辑标签
          </el-button>
          <el-button type="danger" @click="handleDeleteTag">
            <el-icon><Delete /></el-icon>
            删除标签
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 文章列表 -->
    <el-card class="articles-card">
      <template #header>
        <div class="card-header">
          <h2>相关文章</h2>
          <div class="header-actions">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="card">卡片视图</el-radio-button>
              <el-radio-button label="list">列表视图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 卡片视图 -->
        <div v-if="viewMode === 'card'" class="articles-grid">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
            @click="handleArticleClick"
          />
        </div>

        <!-- 列表视图 -->
        <div v-else class="articles-list">
          <ArticleListItem
            v-for="article in articles"
            :key="article.id"
            :article="article"
            @click="handleArticleClick"
          />
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!loading && articles.length === 0"
          description="该标签下暂无文章"
        />

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 编辑标签对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑标签"
      width="500px"
    >
      <TagForm
        ref="tagFormRef"
        :tag="tag"
        :loading="submitLoading"
        @submit="handleUpdateTag"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import type { Tag, Article, UpdateTagRequest } from '@blog/shared/types';
import { getTagById, deleteTag, updateTag } from '@/api/tag';
import { getArticlesByTag } from '@/api/article';
import TagForm from '@/components/tag/TagForm.vue';
import ArticleCard from '@/components/article/ArticleCard.vue';
import ArticleListItem from '@/components/article/ArticleListItem.vue';
import { formatDate } from '@/utils/date';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const tagId = computed(() => Number(route.params.id));
const loading = ref(false);
const submitLoading = ref(false);
const tag = ref<Tag | null>(null);
const articles = ref<Article[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const viewMode = ref<'card' | 'list'>('card');
const editDialogVisible = ref(false);
const tagFormRef = ref();

// 获取标签详情
const fetchTag = async () => {
  try {
    tag.value = await getTagById(tagId.value);
    if (!tag.value) {
      ElMessage.error('标签不存在');
      router.push('/tags');
    }
  } catch (error) {
    ElMessage.error('获取标签详情失败');
    router.push('/tags');
  }
};

// 获取文章列表
const fetchArticles = async () => {
  loading.value = true;
  try {
    const response = await getArticlesByTag(tagId.value, currentPage.value, pageSize.value);
    articles.value = response.content || [];
    total.value = response.totalElements || 0;
  } catch (error) {
    ElMessage.error('获取文章列表失败');
    articles.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理文章点击
const handleArticleClick = (article: Article) => {
  router.push(`/articles/${article.id}`);
};

// 处理编辑标签
const handleEditTag = () => {
  editDialogVisible.value = true;
};

// 处理更新标签
const handleUpdateTag = async (data: UpdateTagRequest) => {
  if (!tag.value) return;

  submitLoading.value = true;
  try {
    const updatedTag = await updateTag(tag.value.id, data);
    tag.value = updatedTag;
    editDialogVisible.value = false;
    ElMessage.success('标签更新成功');
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败');
  } finally {
    submitLoading.value = false;
  }
};

// 处理删除标签
const handleDeleteTag = async () => {
  if (!tag.value) return;

  if (tag.value.articleCount > 0) {
    ElMessage.warning('该标签下还有文章，无法删除');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除标签"${tag.value.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteTag(tag.value.id);
    ElMessage.success('删除成功');
    router.push('/tags');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchArticles();
};

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchArticles();
};

// 监听路由参数变化
watch(
  () => tagId.value,
  (newId) => {
    if (newId) {
      currentPage.value = 1;
      fetchTag();
      fetchArticles();
    }
  },
  { immediate: true }
);

// 监听查询参数中的标签名称
watch(
  () => route.query.name,
  (name) => {
    if (name && tag.value && tag.value.name !== name) {
      // 如果URL中的名称与标签实际名称不匹配，更新URL
      router.replace({
        params: { id: tag.value.id },
        query: { name: tag.value.name },
      });
    }
  }
);

// 初始化
onMounted(() => {
  fetchTag();
  fetchArticles();
});
</script>

<style scoped>
.tag-detail-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.tag-header-card {
  margin-bottom: 20px;
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-info {
  display: flex;
  align-items: center;
}

.tag-icon-wrapper {
  margin-right: 16px;
}

.tag-icon {
  display: inline-block;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tag-name {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
}

.tag-meta {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.separator {
  margin: 0 8px;
}

.tag-actions {
  display: flex;
  gap: 8px;
}

.articles-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .tag-detail-view {
    padding: 16px;
  }

  .tag-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .tag-info {
    width: 100%;
  }

  .tag-icon {
    width: 40px;
    height: 40px;
  }

  .tag-name {
    font-size: 24px;
  }

  .tag-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }
}
</style>