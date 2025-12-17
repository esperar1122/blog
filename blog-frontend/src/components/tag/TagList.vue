<template>
  <div class="tag-list">
    <!-- 工具栏 -->
    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center space-x-2">
        <el-button
          type="primary"
          @click="handleCreate"
          v-if="showCreate"
        >
          <el-icon><Plus /></el-icon>
          新建标签
        </el-button>

        <el-button
          @click="handleRefresh"
          :loading="loading"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <div class="flex items-center space-x-2">
        <el-input
          v-model="searchQuery"
          placeholder="搜索标签"
          clearable
          @input="handleSearch"
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="sortBy"
          style="width: 150px"
          @change="handleSort"
        >
          <el-option label="文章数量" value="articleCount" />
          <el-option label="创建时间" value="createTime" />
          <el-option label="名称" value="name" />
        </el-select>
      </div>
    </div>

    <!-- 标签列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <el-card
        v-for="tag in sortedTags"
        :key="tag.id"
        class="tag-card"
        :body-style="{ padding: '20px' }"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center mb-2">
              <span
                class="inline-block w-4 h-4 rounded-full mr-2"
                :style="{ backgroundColor: tag.color }"
              ></span>
              <h3 class="text-lg font-semibold">{{ tag.name }}</h3>
            </div>

            <div class="text-sm text-gray-500 space-y-1">
              <div>文章数: {{ tag.articleCount }}</div>
              <div>创建时间: {{ formatDate(tag.createTime) }}</div>
            </div>
          </div>

          <div class="flex space-x-1" v-if="showActions">
            <el-button
              type="primary"
              link
              @click="handleEdit(tag)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(tag)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="mt-3 pt-3 border-t border-gray-200">
          <el-button
            type="primary"
            link
            size="small"
            @click="handleViewArticles(tag)"
          >
            查看文章 ({{ tag.articleCount }})
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && sortedTags.length === 0"
      description="暂无标签"
    />

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '新建标签'"
      width="500px"
      @close="handleDialogClose"
    >
      <TagForm
        ref="tagFormRef"
        :tag="currentTag"
        :loading="submitLoading"
        @submit="handleSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, Search, Edit, Delete } from '@element-plus/icons-vue';
import type { Tag, CreateTagRequest, UpdateTagRequest } from '@blog/shared/types';
import { getAllTags, createTag, updateTag, deleteTag, searchTags } from '@/api/tag';
import TagForm from './TagForm.vue';
import { formatDate } from '@/utils/date';

interface Props {
  showCreate?: boolean;
  showActions?: boolean;
  autoLoad?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showCreate: true,
  showActions: true,
  autoLoad: true,
});

const router = useRouter();

const loading = ref(false);
const submitLoading = ref(false);
const tags = ref<Tag[]>([]);
const searchQuery = ref('');
const sortBy = ref('articleCount');
const dialogVisible = ref(false);
const isEdit = ref(false);
const currentTag = ref<Tag | null>(null);
const tagFormRef = ref();

// 排序后的标签
const sortedTags = computed(() => {
  let filtered = tags.value;

  if (searchQuery.value) {
    filtered = filtered.filter(tag =>
      tag.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }

  return filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'articleCount':
        return b.articleCount - a.articleCount;
      case 'createTime':
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime();
      case 'name':
        return a.name.localeCompare(b.name);
      default:
        return 0;
    }
  });
});

// 获取标签列表
const fetchTags = async () => {
  loading.value = true;
  try {
    if (searchQuery.value) {
      tags.value = await searchTags(searchQuery.value, 100);
    } else {
      tags.value = await getAllTags();
    }
  } catch (error) {
    ElMessage.error('获取标签列表失败');
    console.error('Failed to fetch tags:', error);
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  fetchTags();
};

// 处理排序
const handleSort = () => {
  // 触发计算属性重新计算
};

// 处理刷新
const handleRefresh = () => {
  fetchTags();
};

// 处理创建
const handleCreate = () => {
  isEdit.value = false;
  currentTag.value = null;
  dialogVisible.value = true;
};

// 处理编辑
const handleEdit = (tag: Tag) => {
  isEdit.value = true;
  currentTag.value = tag;
  dialogVisible.value = true;
};

// 处理删除
const handleDelete = async (tag: Tag) => {
  if (tag.articleCount > 0) {
    ElMessage.warning('该标签下还有文章，无法删除');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除标签"${tag.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteTag(tag.id);
    ElMessage.success('删除成功');
    fetchTags();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

// 处理查看文章
const handleViewArticles = (tag: Tag) => {
  router.push({
    name: 'TagDetail',
    params: { id: tag.id },
    query: { name: tag.name },
  });
};

// 处理表单提交
const handleSubmit = async (data: CreateTagRequest | UpdateTagRequest) => {
  submitLoading.value = true;
  try {
    if (isEdit.value && currentTag.value) {
      await updateTag(currentTag.value.id, data as UpdateTagRequest);
      ElMessage.success('更新成功');
    } else {
      await createTag(data as CreateTagRequest);
      ElMessage.success('创建成功');
    }

    dialogVisible.value = false;
    fetchTags();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitLoading.value = false;
  }
};

// 处理对话框关闭
const handleDialogClose = () => {
  if (tagFormRef.value) {
    tagFormRef.value.resetFields();
  }
};

// 初始化
onMounted(() => {
  if (props.autoLoad) {
    fetchTags();
  }
});

// 暴露方法
defineExpose({
  refresh: fetchTags,
});
</script>

<style scoped>
.tag-list {
  padding: 20px;
}

.tag-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

.grid {
  display: grid;
}

.grid-cols-1 {
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.md\:grid-cols-2 {
  @media (min-width: 768px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.lg\:grid-cols-3 {
  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.xl\:grid-cols-4 {
  @media (min-width: 1280px) {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.gap-4 {
  gap: 1rem;
}

.flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}

.space-x-1 > * + * {
  margin-left: 0.25rem;
}

.space-x-2 > * + * {
  margin-left: 0.5rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mt-3 {
  margin-top: 0.75rem;
}

.pt-3 {
  padding-top: 0.75rem;
}

.flex-1 {
  flex: 1 1 0%;
}

.w-4 {
  width: 1rem;
}

.h-4 {
  height: 1rem;
}

.rounded-full {
  border-radius: 50%;
}

.mr-2 {
  margin-right: 0.5rem;
}

.text-lg {
  font-size: 1.125rem;
}

.font-semibold {
  font-weight: 600;
}

.text-sm {
  font-size: 0.875rem;
}

.text-gray-500 {
  color: #6b7280;
}

.space-y-1 > * + * {
  margin-top: 0.25rem;
}

.border-t {
  border-top-width: 1px;
}

.border-gray-200 {
  border-color: #e5e7eb;
}
</style>