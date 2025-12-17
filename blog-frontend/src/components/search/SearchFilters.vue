<template>
  <div class="search-filters">
    <el-card class="filter-card">
      <template #header>
        <div class="filter-header">
          <span>筛选条件</span>
          <el-button
            type="text"
            size="small"
            @click="clearFilters"
          >
            清空筛选
          </el-button>
        </div>
      </template>

      <el-form :model="filters" label-width="80px" size="small">
        <!-- 搜索字段 -->
        <el-form-item label="搜索范围">
          <el-checkbox-group v-model="filters.fields">
            <el-checkbox label="title">标题</el-checkbox>
            <el-checkbox label="content">内容</el-checkbox>
            <el-checkbox label="tags">标签</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 分类筛选 -->
        <el-form-item label="分类">
          <el-select
            v-model="filters.categoryId"
            placeholder="选择分类"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <!-- 标签筛选 -->
        <el-form-item label="标签">
          <el-select
            v-model="filters.tagIds"
            placeholder="选择标签"
            multiple
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            >
              <span :style="{ color: tag.color }">{{ tag.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 时间范围 -->
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 排序方式 -->
        <el-form-item label="排序方式">
          <el-select
            v-model="filters.sortBy"
            style="width: 100%"
          >
            <el-option label="相关性" value="relevance" />
            <el-option label="发布时间" value="createTime" />
            <el-option label="浏览量" value="viewCount" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="applyFilters"
            :loading="loading"
          >
            应用筛选
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 热门搜索 -->
    <el-card v-if="hotKeywords.length > 0" class="filter-card hot-keywords">
      <template #header>
        <span>热门搜索</span>
      </template>
      <div class="keywords-list">
        <el-tag
          v-for="keyword in hotKeywords"
          :key="keyword.id"
          :type="getKeywordType(keyword.position)"
          class="keyword-tag"
          @click="selectKeyword(keyword.keyword)"
        >
          {{ keyword.keyword }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { Category, Tag, HotKeywords, SearchQuery } from 'blog-shared'
import { getCategories, getTags } from '@/api/article'
import { useSearchStore } from '@/stores/searchStore'

interface Props {
  modelValue?: SearchQuery
  loading?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: SearchQuery): void
  (e: 'filter-change', filters: SearchQuery): void
  (e: 'keyword-select', keyword: string): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

const searchStore = useSearchStore()

// 筛选条件
const filters = reactive({
  fields: ['title', 'content'] as ('title' | 'content' | 'tags')[],
  categoryId: undefined as number | undefined,
  tagIds: [] as number[],
  dateRange: [] as string[],
  sortBy: 'relevance' as 'relevance' | 'createTime' | 'viewCount'
})

// 数据
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const hotKeywords = computed(() => searchStore.hotKeywords)

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    filters.fields = newValue.fields || ['title', 'content']
    filters.categoryId = newValue.categoryId
    filters.tagIds = newValue.tagIds || []
    filters.sortBy = newValue.sortBy || 'relevance'

    if (newValue.startDate && newValue.endDate) {
      filters.dateRange = [newValue.startDate, newValue.endDate]
    } else {
      filters.dateRange = []
    }
  }
}, { immediate: true })

// 获取分类和标签
const fetchData = async () => {
  try {
    const [categoriesRes, tagsRes] = await Promise.all([
      getCategories(),
      getTags()
    ])
    categories.value = categoriesRes
    tags.value = tagsRes
  } catch (error) {
    ElMessage.error('获取分类和标签失败')
  }
}

// 应用筛选
const applyFilters = () => {
  const filterQuery: SearchQuery = {
    q: props.modelValue?.q || '',
    fields: filters.fields,
    categoryId: filters.categoryId,
    tagIds: filters.tagIds,
    startDate: filters.dateRange[0],
    endDate: filters.dateRange[1],
    sortBy: filters.sortBy
  }

  emit('update:modelValue', filterQuery)
  emit('filter-change', filterQuery)
}

// 清空筛选
const clearFilters = () => {
  filters.fields = ['title', 'content']
  filters.categoryId = undefined
  filters.tagIds = []
  filters.dateRange = []
  filters.sortBy = 'relevance'

  applyFilters()
}

// 选择热门关键词
const selectKeyword = (keyword: string) => {
  emit('keyword-select', keyword)
}

// 获取关键词标签类型
const getKeywordType = (position: number) => {
  if (position <= 3) return 'danger'
  if (position <= 6) return 'warning'
  if (position <= 10) return 'success'
  return 'info'
}

// 初始化
fetchData()
searchStore.fetchHotKeywords()
</script>

<style scoped>
.search-filters {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  width: 280px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hot-keywords {
  max-height: 300px;
  overflow-y: auto;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.keyword-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .filter-card {
    width: 100%;
  }
}
</style>